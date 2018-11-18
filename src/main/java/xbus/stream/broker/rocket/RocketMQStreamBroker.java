package xbus.stream.broker.rocket;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.http.util.Asserts;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import xbus.em.HeaderParams;
import xbus.stream.broker.ConsumeReceipt;
import xbus.stream.message.BusMessage;
import xbus.stream.message.MessageCoverter;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * Rocket消息代理器
 * 
 * @author fuli
 * @date 2018年10月25日
 * @version 1.0.0
 */
public class RocketMQStreamBroker extends StreamBrokerInitializer implements MessageCoverter{
	private Function<List<BusMessage>, List<ConsumeReceipt>> consumer;
	public RocketMQStreamBroker(RocketConfigBean brokerConfig){
		super(brokerConfig);
	}
	
	@Override
	protected void send(Terminal terminal, BusMessage message) throws RuntimeException {
		Asserts.check(!terminal.getName().equals(TerminalConfigurator.getCurrentTerminalName()), "cannot send messages to self!");
		Message msg = new Message();
		msg.setTopic(resolveTopicName(terminal));
		msg.putUserProperty(HeaderParams.XBUS_PATH.name(), message.getPath());
		msg.putUserProperty(HeaderParams.XBUS_SOURCE_TERMINAL.name(), TerminalConfigurator.getCurrentTerminalName());
		msg.putUserProperty(HeaderParams.XBUS_MESSAGE_TYPE.name(), message.getMessageType().name());
		msg.putUserProperty(HeaderParams.XBUS_MESSAGE_CONTENT_TYPE.name(), message.getContentType().name());
		msg.setBody(message.getPayLoad().toBytes());
		msg.setKeys(message.getMessageId());//在控制台根据key查询消息
		try {
			/** send方法是同步调用，只要不抛异常就标识成功。但是发送成功也可会有多种状态 */
			SendResult sendResult = producer.send(msg);
			SendStatus sendStatus = sendResult.getSendStatus();
			//为确保消息不丢失,刷盘失败则认为发送失败(同步刷盘情况下会出现)
			Asserts.check(sendStatus != SendStatus.FLUSH_DISK_TIMEOUT, "send not ok , status:" + sendResult);
		} catch (Exception e) {
			new RuntimeException(e);
		}
	}
	
	private BusMessage makeMessage(MessageExt messageExt){
		String path = messageExt.getUserProperty(HeaderParams.XBUS_PATH.name());
		String sourceTerminal = messageExt.getUserProperty(HeaderParams.XBUS_SOURCE_TERMINAL.name());
		String messageType =messageExt.getUserProperty(HeaderParams.XBUS_MESSAGE_TYPE.name());
		String messageContentType =messageExt.getUserProperty(HeaderParams.XBUS_MESSAGE_CONTENT_TYPE.name());
		return coverter(path, sourceTerminal, messageType, messageContentType, messageExt.getBody());
	}

	@Override
	public void startReceive(long consumeIntervalMills, Function<List<BusMessage>, List<ConsumeReceipt>> consumer)throws Exception {
		String topicName = legalizeName(terminalNode.getTerminalName(),TOPIC_CLUSTERING);
		((DefaultMQPushConsumer) topicConsumer).subscribe(topicName, "*");
		((DefaultMQPushConsumer) topicConsumer).setPullInterval(consumeIntervalMills);
		topicConsumer.start();
		//创建|更新Topic_Clustering
		topicConsumer.createTopic(MixAll.AUTO_CREATE_TOPIC_KEY_TOPIC,topicName , rocketConfig.getTopicQueueNums());
		
		String queueName = legalizeName(terminalNode.getTerminalName(),TOPIC_BROADCASTING);
		((DefaultMQPushConsumer) queueConsumer).subscribe(queueName, "*");
		((DefaultMQPushConsumer) queueConsumer).setPullInterval(consumeIntervalMills);
		queueConsumer.start();
		//创建|更新Topic_BroadCasting
		queueConsumer.createTopic(MixAll.AUTO_CREATE_TOPIC_KEY_TOPIC,queueName , rocketConfig.getTopicQueueNums());
		this.consumer = consumer;
	}
	@Override
	public void stopReceive() {
		if (topicConsumer != null) {
			try {
				topicConsumer.shutdown();
			} catch (Exception e) {
				LOGGER.error(this + ".stopReceive().topicConsumer.shutdown() error!", e);
			}
		}
		if (queueConsumer != null) {
			try {
				queueConsumer.shutdown();
			} catch (Exception e) {
				LOGGER.error(this + ".stopReceive().queueConsumer.shutdown() error!", e);
			}
		}
	}

	@Override
	protected ConsumeConcurrentlyStatus doConsume(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
		if (msgs != null && msgs.size() > 0) {
			try{
				List<ConsumeReceipt> rpts=consumer.apply(msgs.stream().map(this::makeMessage).collect(Collectors.toList()));
				Asserts.check(rpts.size() == msgs.size(), "ConsumeReceipt.size("+rpts.size()+") not match MessageExt.size("+msgs.size()+"), recheck log to know more.");
			} catch (Exception e) {
				LOGGER.error(this + ".doConsume() error!", e);
				return ConsumeConcurrentlyStatus.RECONSUME_LATER;
			}
		}
		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}
}
