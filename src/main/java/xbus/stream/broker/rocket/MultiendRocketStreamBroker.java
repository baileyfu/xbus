package xbus.stream.broker.rocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Rocket多路径消息代理器</p>
 * 为节点的每个path分别创建Topic和Queue</p>
 * 启用nodeOriented时一个服务共需创建：(topic + DSQ_topic + RETRY_topic + queue + (DSQ_queue + RETRY_queue) * nodes) * paths
 * @author fuli
 * @date 2018年10月25日
 * @version 1.0.0
 */
public class MultiendRocketStreamBroker extends AbstractRocketStreamBroker{
	protected Map<String, MQPushConsumer> pathTopicConsumers;
	protected Map<String, MQPushConsumer> pathQueueConsumers;
	public MultiendRocketStreamBroker(BusConfigBean busConfig, xbus.stream.broker.rocket.RocketConfigBean brokerConfig){
		super(busConfig,brokerConfig);
	}
	/**
	 * 针对当前Terminal分别建立集群消费和广播消费group
	 */
	@Override
	public void initializeChannel(TerminalNode currentTerminalNode, Set<EndpointBean> endpointList) throws Exception {
		super.initializeChannel(currentTerminalNode, endpointList);
		pathTopicConsumers = new HashMap<>();
		for (EndpointBean endpoint : endpointList) {
			DefaultMQPushConsumer topicConsumer = new DefaultMQPushConsumer(legalizeTopicName(terminalNode.getTerminalName(),endpoint.getFullPath()));
			topicConsumer.setNamesrvAddr(rocketConfig.getNameSrvAddr());
			topicConsumer.setInstanceName(legalizeTopicName(terminalNode.getName(),endpoint.getFullPath()));
			topicConsumer.setMessageModel(MessageModel.CLUSTERING);
			topicConsumer.setConsumeFromWhere(DEFAULT_CONSUME_FROM_WHERE);
			topicConsumer.setConsumeTimeout(rocketConfig.getConsumerTimeoutMillis() / 1000 / 60);
			topicConsumer.setMaxReconsumeTimes(rocketConfig.getConsumeRetryCount());
			topicConsumer.setPullBatchSize(rocketConfig.getPullBatchSize());
			topicConsumer.setConsumeMessageBatchMaxSize(rocketConfig.getConsumeBatchSize());
			topicConsumer.registerMessageListener(messageListener);
			pathTopicConsumers.put(endpoint.getFullPath(), topicConsumer);
		}
		//面向节点总开关
		if(busConfig.isNodeOriented()) {
			pathQueueConsumers = new HashMap<>();
			for (EndpointBean endpoint : endpointList) {
				//开启面向节点的path才生成广播类型Topic
				if(endpoint.isNodeOriented()) {
					DefaultMQPushConsumer queueConsumer = new DefaultMQPushConsumer(legalizeTopicName(terminalNode.getName(),endpoint.getFullPath()));
					queueConsumer.setNamesrvAddr(rocketConfig.getNameSrvAddr());
					queueConsumer.setInstanceName(legalizeTopicName(terminalNode.getName(),endpoint.getFullPath()));
					queueConsumer.setMessageModel(MessageModel.BROADCASTING);
					queueConsumer.setConsumeFromWhere(DEFAULT_CONSUME_FROM_WHERE);
					queueConsumer.setConsumeTimeout(rocketConfig.getConsumerTimeoutMillis() / 1000 / 60);
					queueConsumer.setMaxReconsumeTimes(rocketConfig.getConsumeRetryCount());
					queueConsumer.setPullBatchSize(rocketConfig.getPullBatchSize());
					queueConsumer.setConsumeMessageBatchMaxSize(rocketConfig.getConsumeBatchSize());
					queueConsumer.registerMessageListener(messageListener);
					pathQueueConsumers.put(endpoint.getFullPath(), queueConsumer);
				}
			}
		}
	}
	@Override
	public void startReceive(long consumeIntervalMills, Function<List<BusMessage>, List<ConsumeReceipt>> consumer)throws Exception {
		long pullInterval = rocketConfig.getPullInterval() > -1 ? rocketConfig.getPullInterval() : consumeIntervalMills;
		super.startReceive(pullInterval, consumer);
		for (String path : pathTopicConsumers.keySet()) {
			DefaultMQPushConsumer topicConsumer = (DefaultMQPushConsumer) pathTopicConsumers.get(path);
			String topicName = legalizeTopicName(terminalNode.getTerminalName(),path,TOPIC_CLUSTERING);
			topicConsumer.subscribe(topicName, "*");
			topicConsumer.setPullInterval(pullInterval);
			topicConsumer.start();
			//创建|更新Topic_Clustering
			topicConsumer.createTopic(MixAll.AUTO_CREATE_TOPIC_KEY_TOPIC,topicName , rocketConfig.getTopicQueueNums());
		}
		if (pathQueueConsumers != null) {
			for (String path : pathQueueConsumers.keySet()) {
				DefaultMQPushConsumer queueConsumer = (DefaultMQPushConsumer)pathQueueConsumers.get(path);
				String queueName = legalizeTopicName(terminalNode.getTerminalName(),path,TOPIC_BROADCASTING);
				queueConsumer.subscribe(queueName, "*");
				queueConsumer.setPullInterval(pullInterval);
				queueConsumer.start();
				//创建|更新Topic_BroadCasting
				queueConsumer.createTopic(MixAll.AUTO_CREATE_TOPIC_KEY_TOPIC,queueName , rocketConfig.getTopicQueueNums());
			}
		}
	}
	@Override
	public void stopReceive() {
		super.stopReceive();
		for(MQPushConsumer topicConsumer : pathTopicConsumers.values()) {
			try {
				topicConsumer.shutdown();
			} catch (Exception e) {
				LOGGER.error(this + ".stopReceive().pathTopicConsumers[{}].shutdown() error!",((DefaultMQPushConsumer) topicConsumer).getConsumerGroup(), e);
			}
		}
		if (pathQueueConsumers != null) {
			for(MQPushConsumer queueConsumer : pathQueueConsumers.values()) {
				try {
					queueConsumer.shutdown();
				} catch (Exception e) {
					LOGGER.error(this + ".stopReceive().pathQueueConsumers[{}].shutdown() error!",((DefaultMQPushConsumer) queueConsumer).getConsumerGroup(), e);
				}
			}
		}
	}
	@Override
	protected String makeTopic(final Terminal terminal, final String path){
		// 发送到所有节点
		if (terminal.getClass() == Terminal.class) {
			return legalizeTopicName(terminal.getName() , path, TOPIC_BROADCASTING);
		} else {
			return legalizeTopicName(terminal.getName() , path, TOPIC_CLUSTERING);
		}
	}
}
