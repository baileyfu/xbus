package xbus.stream.broker.rocket;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import xbus.bean.EndpointBean;
import xbus.constants.HeaderParams;
import xbus.constants.Keywords;
import xbus.constants.MessageType;
import xbus.core.config.BusConfigBean;
import xbus.stream.broker.AutoConsumeStreamBroker;
import xbus.stream.broker.ConsumeReceipt;
import xbus.stream.message.BusMessage;
import xbus.stream.message.MessageCoverter;
import xbus.stream.message.OriginalBusMessage;
import xbus.stream.message.ReceiptBusMessage;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalConfigurator;
import xbus.stream.terminal.TerminalNode;

/**
 * RocketMQ代理抽象基类
 * </p>
 * 生成RECEIPT类型的topic
 * 
 * @author fuli
 * @date 2018年11月26日
 * @version 1.0.0
 */
public abstract class AbstractRocketStreamBroker extends AutoConsumeStreamBroker implements MessageCoverter{
	protected static final String TOPIC_RECEIPT = "RECEIPT";
	protected static final String TOPIC_CLUSTERING = "CLUSTERING";
	protected static final String TOPIC_BROADCASTING = "BROADCASTING";
	
	protected static final ConsumeFromWhere DEFAULT_CONSUME_FROM_WHERE=ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
	
	private MQProducer producer;
	private MQPushConsumer receiptConsumer;
	protected TerminalNode terminalNode;
	protected xbus.stream.broker.rocket.RocketConfigBean rocketConfig;
	protected MessageListenerConcurrently messageListener;
	private Function<List<BusMessage>, List<ConsumeReceipt>> consumer;
	public AbstractRocketStreamBroker(BusConfigBean busConfig, xbus.stream.broker.rocket.RocketConfigBean brokerConfig) {
		super(busConfig, brokerConfig);
		this.rocketConfig = brokerConfig;
		/** Consumer */
		messageListener = new MessageListenerConcurrently() {
			@Override
			public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
				return doConsume(msgs,context);
			}
		};
	}
	
	@Override
	public void initializeChannel(TerminalNode currentTerminalNode, Set<EndpointBean> endpoints) throws Exception {
		this.terminalNode = currentTerminalNode;
		/** Producer */
		//ProducerGroup这个概念发送普通的消息时，作用不大，但是发送分布式事务消息时，比较关键，因为服务器会回查这个Group下的任意一个Producer
		producer = new DefaultMQProducer(legalizeTopicName(terminalNode.getTerminalName()));
		((DefaultMQProducer) producer).setNamesrvAddr(rocketConfig.getNameSrvAddr());
		((DefaultMQProducer) producer).setInstanceName(legalizeTopicName(terminalNode.getName()));
		((DefaultMQProducer) producer).setRetryTimesWhenSendFailed(rocketConfig.getProduceRetryCount());
		((DefaultMQProducer) producer).setRetryTimesWhenSendAsyncFailed(rocketConfig.getProduceRetryCount());
		((DefaultMQProducer) producer).setSendMsgTimeout(Long.valueOf(rocketConfig.getProducerTimeoutMillis()).intValue());
		producer.start();
		/** Receipt Consumer */
		receiptConsumer = new DefaultMQPushConsumer(legalizeTopicName(terminalNode.getTerminalName(),TOPIC_RECEIPT));
		((DefaultMQPushConsumer) receiptConsumer).setNamesrvAddr(rocketConfig.getNameSrvAddr());
		((DefaultMQPushConsumer) receiptConsumer).setInstanceName(legalizeTopicName(terminalNode.getName(),TOPIC_RECEIPT));
		((DefaultMQPushConsumer) receiptConsumer).setMessageModel(MessageModel.CLUSTERING);
		((DefaultMQPushConsumer) receiptConsumer).setConsumeFromWhere(DEFAULT_CONSUME_FROM_WHERE);
		((DefaultMQPushConsumer) receiptConsumer).setConsumeTimeout(rocketConfig.getConsumerTimeoutMillis() / 1000 / 60);
		((DefaultMQPushConsumer) receiptConsumer).setMaxReconsumeTimes(rocketConfig.getConsumeRetryCount());
		((DefaultMQPushConsumer) receiptConsumer).setPullBatchSize(rocketConfig.getPullBatchSize());
		((DefaultMQPushConsumer) receiptConsumer).setConsumeMessageBatchMaxSize(rocketConfig.getConsumeBatchSize());
		((DefaultMQPushConsumer) receiptConsumer).registerMessageListener(messageListener);
	}
	
	@Override
	public void startReceive(long consumeIntervalMills, Function<List<BusMessage>, List<ConsumeReceipt>> consumer)
			throws Exception {
		this.consumer=consumer;
		String topicName = legalizeTopicName(terminalNode.getTerminalName(),TOPIC_RECEIPT);
		((DefaultMQPushConsumer) receiptConsumer).subscribe(topicName, "*");
		((DefaultMQPushConsumer) receiptConsumer).setPullInterval(consumeIntervalMills);
		receiptConsumer.start();
		//创建|更新Topic_Clustering
		receiptConsumer.createTopic(MixAll.AUTO_CREATE_TOPIC_KEY_TOPIC,topicName , rocketConfig.getTopicQueueNums());
	}
	
	@Override
	public void stopReceive() {
		if (receiptConsumer != null) {
			try {
				receiptConsumer.shutdown();
			} catch (Exception e) {
				LOGGER.error(this + ".stopReceive().receiptConsumer.shutdown() error!", e);
			}
		}
	}

	@Override
	protected void send(Terminal terminal, BusMessage message) throws RuntimeException {
		Asserts.check(!terminal.getName().equals(TerminalConfigurator.getCurrentTerminalName()), "cannot send messages to self!");
		if(message instanceof OriginalBusMessage) {
			Asserts.check(!Keywords.RECEIPT_PATH.equals(message.getPath()), "cannot post OriginalBusMessage with path '"+Keywords.RECEIPT_PATH+"'");
		}else {
			Asserts.check(Keywords.RECEIPT_PATH.equals(message.getPath()), "cannot post ReceiptBusMessage without path '"+Keywords.RECEIPT_PATH+"'");
		}
		Message msg = new Message();
		msg.setTopic(resolveTopicName(terminal, message.getPath()));
		msg.putUserProperty(HeaderParams.XBUS_PATH.name(), message.getPath());
		msg.putUserProperty(HeaderParams.XBUS_SOURCE_TERMINAL.name(), TerminalConfigurator.getCurrentTerminalName());
		msg.putUserProperty(HeaderParams.XBUS_MESSAGE_TYPE.name(), message.getMessageType().name());
		msg.putUserProperty(HeaderParams.XBUS_MESSAGE_CONTENT_TYPE.name(), message.getContentType().name());
		if (message.getMessageType() == MessageType.ORIGINAL) {
			msg.putUserProperty(HeaderParams.XBUS_REQUIRE_RECEIPT.name(), Boolean.valueOf(((OriginalBusMessage)message).isRequireReceipt()).toString());
		}else {
			msg.putUserProperty(HeaderParams.XBUS_SOURCE_PATH.name(), ((ReceiptBusMessage)message).getSourcePath());
		}
		JSONObject originals = message.getOriginals();
		if (originals!=null) {
			msg.setTags(originals.getOrDefault("tags",StringUtils.EMPTY).toString());
			msg.setKeys(originals.getOrDefault("keys",StringUtils.EMPTY).toString());
			msg.putUserProperty(HeaderParams.XBUS_ORIGINALS.name(), originals.toJSONString());
		}else {
			msg.setKeys(message.getMessageId());//在控制台根据key查询消息
		}
		msg.setBody(message.getPayLoad().toBytes());
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
	
	@Override
	public void destoryChannel() throws Exception {
		if (producer != null) {
			try {
				producer.shutdown();
			} catch (Exception e) {
				LOGGER.error(this + ".destoryChannel().producer.shutdown() error!", e);
			}
		}
	}
	abstract protected String makeTopic(final Terminal terminal, final String path);
	/**
	 * 根据terminal类型和path决定发送到那个topic
	 * 
	 * @param terminal
	 * @param path
	 * @return
	 */
	private String resolveTopicName(final Terminal terminal, final String path) {
		/** 重要：回执消息全部发送到当前服务的RECEIPT,由receiptConsumer负责消费 */
		if (Keywords.RECEIPT_PATH.equals(path)) {
			return legalizeTopicName(terminal.getName(), TOPIC_RECEIPT);
		}
		return makeTopic(terminal,path);
	}
	/**
	 * 消费消息并返回结果
	 * 
	 * @param msgs
	 * @param context
	 * @return
	 */
	private ConsumeConcurrentlyStatus doConsume(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
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
	private BusMessage makeMessage(MessageExt messageExt){
		String path = messageExt.getUserProperty(HeaderParams.XBUS_PATH.name());
		String sourcePath = messageExt.getUserProperty(HeaderParams.XBUS_SOURCE_PATH.name());
		String sourceTerminal = messageExt.getUserProperty(HeaderParams.XBUS_SOURCE_TERMINAL.name());
		String messageType =messageExt.getUserProperty(HeaderParams.XBUS_MESSAGE_TYPE.name());
		String messageContentType =messageExt.getUserProperty(HeaderParams.XBUS_MESSAGE_CONTENT_TYPE.name());
		String requireReceipt=Optional.ofNullable(messageExt.getUserProperty(HeaderParams.XBUS_REQUIRE_RECEIPT.name())).orElse(Boolean.FALSE.toString());
		String originals = messageExt.getUserProperty(HeaderParams.XBUS_ORIGINALS.name());
		return coverter(path, sourcePath,sourceTerminal, messageType, messageContentType, messageExt.getBody(),StringUtils.isBlank(originals)?null:JSON.parseObject(originals),Boolean.valueOf(requireReceipt));
	}
}
