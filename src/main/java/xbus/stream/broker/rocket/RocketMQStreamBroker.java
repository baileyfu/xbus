package xbus.stream.broker.rocket;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.http.util.Asserts;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;

import xbus.stream.broker.ConsumeReceipt;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.Terminal;

/**
 * Rocket消息代理器
 * 
 * @author fuli
 * @date 2018年10月25日
 * @version 1.0.0
 */
public class RocketMQStreamBroker extends AbstractRocketMQSender{
	private Function<List<BusMessage>, List<ConsumeReceipt>> consumer;
	public RocketMQStreamBroker(RocketConfigBean brokerConfig){
		super(brokerConfig);
	}
	@Override
	protected String resolveTopicName(final Terminal terminal,final BusMessage message) {
		// 发送到所有节点
		if (terminal.getClass() == Terminal.class) {
			return terminal.getName() + TOPIC_BROADCASTING;
		} else {
			return terminal.getName() + TOPIC_CLUSTERING;
		}
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
