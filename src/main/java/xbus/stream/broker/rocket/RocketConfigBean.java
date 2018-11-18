package xbus.stream.broker.rocket;

import xbus.stream.broker.BrokerConfigBean;

/**
 * RocketMQ相关配置
 * 
 * @author fuli
 * @date 2018年11月1日
 * @version 1.0.0
 */
public class RocketConfigBean extends BrokerConfigBean {
	public static final Integer ROCKET_DEFAULT_TOPIC_QUEUE_NUMS = 4;
	public static final Integer ROCKET_DEFAULT_PULL_BATCH_SIZE = 32;
	public static final Integer ROCKET_DEFAULT_CONSUME_BATCH_SIZE = 1;

	private String nameSrvAddr;
	private int topicQueueNums;
	// 每次拉取消息数
	private int pullBatchSize;
	// 并发消费时每线程每次消费消息数
	private int consumeBatchSize;

	public RocketConfigBean() {
		produceRetryCount = 2;
		producerTimeoutMillis=3000;
		// 超过则移到死信队列,需人工干预
		consumeRetryCount = 16;
		consumerTimeoutMillis = 15 * 60 * 1000;
		topicQueueNums = ROCKET_DEFAULT_TOPIC_QUEUE_NUMS;
		pullBatchSize = ROCKET_DEFAULT_PULL_BATCH_SIZE;
		consumeBatchSize = ROCKET_DEFAULT_CONSUME_BATCH_SIZE;
	}

	public String getNameSrvAddr() {
		return nameSrvAddr;
	}

	public void setNameSrvAddr(String nameSrvAddr) {
		this.nameSrvAddr = nameSrvAddr;
	}

	public int getTopicQueueNums() {
		return topicQueueNums;
	}

	public void setTopicQueueNums(int topicQueueNums) {
		this.topicQueueNums = topicQueueNums;
	}

	public int getPullBatchSize() {
		return pullBatchSize;
	}

	public void setPullBatchSize(int pullBatchSize) {
		this.pullBatchSize = pullBatchSize;
	}

	public int getConsumeBatchSize() {
		return consumeBatchSize;
	}

	public void setConsumeBatchSize(int consumeBatchSize) {
		this.consumeBatchSize = consumeBatchSize;
	}
}
