package xbus.stream.broker.rocket;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import xbus.bean.EndpointBean;
import xbus.core.config.BusConfigBean;
import xbus.stream.broker.ConsumeReceipt;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalNode;

/**
 * Rocket消息代理器<br/>
 * 为当前服务创建一个Topic和一个Queue</p>
 * 当需要发送给所有节点时,发送到Queue;当采用轮询/随机发送到某个节点时,发送到Topic</p>
 * 当前服务的所有节点都订阅此Topic和Queue</p>
 * 启用nodeOriented时一个服务共需创建：topic + DSQ_topic + RETRY_topic + queue + (DSQ_queue + RETRY_queue) * nodes
 * @author fuli
 * @date 2018年10月25日
 * @version 1.0.0
 */
public class DefaultRocketStreamBroker extends AbstractRocketStreamBroker{
	protected MQPushConsumer topicConsumer;
	protected MQPushConsumer queueConsumer;
	public DefaultRocketStreamBroker(BusConfigBean busConfig, xbus.stream.broker.rocket.RocketConfigBean brokerConfig){
		super(busConfig,brokerConfig);
	}
	/**
	 * 针对当前Terminal分别建立集群消费和广播消费group
	 */
	@Override
	public void initializeChannel(TerminalNode currentTerminalNode, Set<EndpointBean> endpoints) throws Exception {
		super.initializeChannel(currentTerminalNode, endpoints);
		topicConsumer = new DefaultMQPushConsumer(legalizeTopicName(terminalNode.getTerminalName()));
		((DefaultMQPushConsumer) topicConsumer).setNamesrvAddr(rocketConfig.getNameSrvAddr());
		((DefaultMQPushConsumer) topicConsumer).setInstanceName(legalizeTopicName(terminalNode.getName()));
		((DefaultMQPushConsumer) topicConsumer).setMessageModel(MessageModel.CLUSTERING);
		((DefaultMQPushConsumer) topicConsumer).setConsumeFromWhere(DEFAULT_CONSUME_FROM_WHERE);
		((DefaultMQPushConsumer) topicConsumer).setConsumeTimeout(rocketConfig.getConsumerTimeoutMillis() / 1000 / 60);
		((DefaultMQPushConsumer) topicConsumer).setMaxReconsumeTimes(rocketConfig.getConsumeRetryCount());
		((DefaultMQPushConsumer) topicConsumer).setPullBatchSize(rocketConfig.getPullBatchSize());
		((DefaultMQPushConsumer) topicConsumer).setConsumeMessageBatchMaxSize(rocketConfig.getConsumeBatchSize());
		((DefaultMQPushConsumer) topicConsumer).registerMessageListener(messageListener);
		//面向节点时才生成广播类型Topic
		if(busConfig.isNodeOriented()) {
			queueConsumer = new DefaultMQPushConsumer(legalizeTopicName(terminalNode.getName()));
			((DefaultMQPushConsumer) queueConsumer).setNamesrvAddr(rocketConfig.getNameSrvAddr());
			((DefaultMQPushConsumer) queueConsumer).setInstanceName(legalizeTopicName(terminalNode.getName()));
			((DefaultMQPushConsumer) queueConsumer).setMessageModel(MessageModel.BROADCASTING);
			((DefaultMQPushConsumer) queueConsumer).setConsumeFromWhere(DEFAULT_CONSUME_FROM_WHERE);
			((DefaultMQPushConsumer) queueConsumer).setConsumeTimeout(rocketConfig.getConsumerTimeoutMillis() / 1000 / 60);
			((DefaultMQPushConsumer) queueConsumer).setMaxReconsumeTimes(rocketConfig.getConsumeRetryCount());
			((DefaultMQPushConsumer) queueConsumer).setPullBatchSize(rocketConfig.getPullBatchSize());
			((DefaultMQPushConsumer) queueConsumer).setConsumeMessageBatchMaxSize(rocketConfig.getConsumeBatchSize());
			((DefaultMQPushConsumer) queueConsumer).registerMessageListener(messageListener);
		}
	}
	@Override
	public void startReceive(long consumeIntervalMills, Function<List<BusMessage>, List<ConsumeReceipt>> consumer)throws Exception {
		long pullInterval = rocketConfig.getPullInterval() > -1 ? rocketConfig.getPullInterval() : consumeIntervalMills;
		super.startReceive(pullInterval, consumer);
		String topicName = legalizeTopicName(terminalNode.getTerminalName(),TOPIC_CLUSTERING);
		((DefaultMQPushConsumer) topicConsumer).subscribe(topicName, "*");
		((DefaultMQPushConsumer) topicConsumer).setPullInterval(pullInterval);
		topicConsumer.start();
		//创建|更新Topic_Clustering
		topicConsumer.createTopic(MixAll.AUTO_CREATE_TOPIC_KEY_TOPIC,topicName , rocketConfig.getTopicQueueNums());
		
		if (queueConsumer != null) {
			String queueName = legalizeTopicName(terminalNode.getTerminalName(),TOPIC_BROADCASTING);
			((DefaultMQPushConsumer) queueConsumer).subscribe(queueName, "*");
			((DefaultMQPushConsumer) queueConsumer).setPullInterval(pullInterval);
			queueConsumer.start();
			//创建|更新Topic_BroadCasting
			queueConsumer.createTopic(MixAll.AUTO_CREATE_TOPIC_KEY_TOPIC,queueName , rocketConfig.getTopicQueueNums());
		}
	}
	@Override
	public void stopReceive() {
		super.stopReceive();
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
	protected String makeTopic(final Terminal terminal, final String path) {
		// 发送到所有节点
		if (terminal.getClass() == Terminal.class) {
			return legalizeTopicName(terminal.getName(), TOPIC_BROADCASTING);
		} else {
			return legalizeTopicName(terminal.getName(), TOPIC_CLUSTERING);
		}
	}
}
