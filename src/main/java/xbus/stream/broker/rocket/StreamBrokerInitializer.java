package com.lz.components.bus.stream.broker.rocket;

import java.util.List;
import java.util.Set;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import com.lz.components.bus.stream.broker.AutoConsumeStreamBroker;
import com.lz.components.bus.stream.terminal.Terminal;
import com.lz.components.bus.stream.terminal.TerminalNode;

/**
 * 代理器初始化<br/>
 * 
 * 为当前服务创建一个Topic和一个Queue</p>
 * 当需要发送给所有节点时,发送到Queue;当采用轮询/随机发送到某个节点时,发送到Topic</p>
 * 当前服务的所有节点都订阅此Topic和Queue
 * 
 * @author fuli
 * @date 2018年10月25日
 * @version 1.0.0
 */
public abstract class StreamBrokerInitializer extends AutoConsumeStreamBroker {
	protected static final String TOPIC_CLUSTERING = "_CLUSTERING";
	protected static final String TOPIC_BROADCASTING = "_BROADCASTING";
	
	protected TerminalNode terminalNode;
	protected MQProducer producer;
	protected MQPushConsumer topicConsumer;
	protected MQPushConsumer queueConsumer;
	protected RocketConfigBean rocketConfig;
	
	public StreamBrokerInitializer(RocketConfigBean brokerConfig) {
		super(brokerConfig);
		this.rocketConfig = brokerConfig;
	}

	/**
	 * 根据terminal类型决定发送到那个topic
	 * 
	 * @param terminal
	 * @param path
	 * @return
	 */
	protected String resolveTopicName(final Terminal terminal) {
		// 发送到所有节点
		if (terminal.getClass() == Terminal.class) {
			return terminal.getName() + TOPIC_BROADCASTING;
		} else {
			return terminal.getName() + TOPIC_CLUSTERING;
		}
	}
	
	/**
	 * 针对当前Terminal分别建立集群消费和广播消费group
	 */
	@Override
	public void initializeChannel(TerminalNode terminalNode, Set<String> endpointList) throws Exception {
		this.terminalNode = terminalNode;
		/** Producer */
		//ProducerGroup这个概念发送普通的消息时，作用不大，但是发送分布式事务消息时，比较关键，因为服务器会回查这个Group下的任意一个Producer
		producer = new DefaultMQProducer(legalizeName(terminalNode.getTerminalName()));
		((DefaultMQProducer) producer).setNamesrvAddr(rocketConfig.getNameSrvAddr());
		((DefaultMQProducer) producer).setInstanceName(legalizeName(terminalNode.getName()));
		((DefaultMQProducer) producer).setRetryTimesWhenSendFailed(rocketConfig.getProduceRetryCount());
		((DefaultMQProducer) producer).setRetryTimesWhenSendAsyncFailed(rocketConfig.getProduceRetryCount());
		((DefaultMQProducer) producer).setSendMsgTimeout(Long.valueOf(rocketConfig.getProducerTimeoutMillis()).intValue());
		producer.start();
		/** Consumer */
		MessageListenerConcurrently messageListener = new MessageListenerConcurrently() {
			@Override
			public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
				return doConsume(msgs,context);
			}
		};
		topicConsumer = new DefaultMQPushConsumer(legalizeName(terminalNode.getTerminalName()));
		((DefaultMQPushConsumer) topicConsumer).setNamesrvAddr(rocketConfig.getNameSrvAddr());
		((DefaultMQPushConsumer) topicConsumer).setInstanceName(legalizeName(terminalNode.getName()));
		((DefaultMQPushConsumer) topicConsumer).setMessageModel(MessageModel.CLUSTERING);
		((DefaultMQPushConsumer) topicConsumer).setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
		((DefaultMQPushConsumer) topicConsumer).setConsumeTimeout(rocketConfig.getConsumerTimeoutMillis() / 1000 / 60);
		((DefaultMQPushConsumer) topicConsumer).setMaxReconsumeTimes(rocketConfig.getConsumeRetryCount());
		((DefaultMQPushConsumer) topicConsumer).setPullBatchSize(rocketConfig.getPullBatchSize());
		((DefaultMQPushConsumer) topicConsumer).setConsumeMessageBatchMaxSize(rocketConfig.getConsumeBatchSize());
		((DefaultMQPushConsumer) topicConsumer).registerMessageListener(messageListener);
		queueConsumer = new DefaultMQPushConsumer(legalizeName(terminalNode.getName()));
		((DefaultMQPushConsumer) queueConsumer).setNamesrvAddr(rocketConfig.getNameSrvAddr());
		((DefaultMQPushConsumer) queueConsumer).setInstanceName(legalizeName(terminalNode.getName()));
		((DefaultMQPushConsumer) queueConsumer).setMessageModel(MessageModel.BROADCASTING);
		((DefaultMQPushConsumer) queueConsumer).setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
		((DefaultMQPushConsumer) queueConsumer).setConsumeTimeout(rocketConfig.getConsumerTimeoutMillis() / 1000 / 60);
		((DefaultMQPushConsumer) queueConsumer).setMaxReconsumeTimes(rocketConfig.getConsumeRetryCount());
		((DefaultMQPushConsumer) queueConsumer).setPullBatchSize(rocketConfig.getPullBatchSize());
		((DefaultMQPushConsumer) queueConsumer).setConsumeMessageBatchMaxSize(rocketConfig.getConsumeBatchSize());
		((DefaultMQPushConsumer) queueConsumer).registerMessageListener(messageListener);
	}

	/**
	 * 消费消息并返回结果
	 * 
	 * @param msgs
	 * @param context
	 * @return
	 */
	abstract protected ConsumeConcurrentlyStatus doConsume(List<MessageExt> msgs, ConsumeConcurrentlyContext context);
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
}
