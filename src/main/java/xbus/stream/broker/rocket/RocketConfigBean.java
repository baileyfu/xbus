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
	public static final Integer DEFAULT_PRODUCE_RETRY_COUNT = 2;
	public static final Long DEFAULT_PRODUCE_TIMEOUT_MILLIS = 3000L;
	public static final Integer DEFAULT_CONSUME_RETRY_COUNT = 16;
	public static final Long DEFAULT_CONSUME_TIMEOUT_MILLIS = 15 * 60 * 1000L;
	
	public static final Integer ROCKET_DEFAULT_TOPIC_QUEUE_NUMS = 4;
	public static final Integer ROCKET_DEFAULT_PULL_BATCH_SIZE = 32;
	//默认值取-1表示使用accessInterval来定义拉取间隔
	public static final Long ROCKET_DEFAULT_PULL_INTERVAL = -1L;
	public static final Integer ROCKET_DEFAULT_CONSUME_BATCH_SIZE = 1;
	public static final Boolean ROCKET_DEFAULT_MULTIEND = false;

	// 是否启用RocketMultiendStreamBroker
	private boolean multiend;
	private String nameSrvAddr;
	private int topicQueueNums;
	// 每次拉取消息数
	private int pullBatchSize;
	private long pullInterval;
	// 并发消费时每线程每次消费消息数
	private int consumeBatchSize;

	public RocketConfigBean() {
		produceRetryCount = DEFAULT_PRODUCE_RETRY_COUNT;
		producerTimeoutMillis=DEFAULT_PRODUCE_TIMEOUT_MILLIS;
		// 超过则移到死信队列,需人工干预
		consumeRetryCount = DEFAULT_CONSUME_RETRY_COUNT;
		consumerTimeoutMillis = DEFAULT_CONSUME_TIMEOUT_MILLIS;
		topicQueueNums = ROCKET_DEFAULT_TOPIC_QUEUE_NUMS;
		pullBatchSize = ROCKET_DEFAULT_PULL_BATCH_SIZE;
		pullInterval = ROCKET_DEFAULT_PULL_INTERVAL;
		consumeBatchSize = ROCKET_DEFAULT_CONSUME_BATCH_SIZE;
		multiend = ROCKET_DEFAULT_MULTIEND;
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

	public long getPullInterval() {
		return pullInterval;
	}

	public void setPullInterval(long pullInterval) {
		this.pullInterval = pullInterval;
	}

	public int getConsumeBatchSize() {
		return consumeBatchSize;
	}

	public void setConsumeBatchSize(int consumeBatchSize) {
		this.consumeBatchSize = consumeBatchSize;
	}

	public boolean isMultiend() {
		return multiend;
	}

	public void setMultiend(boolean multiend) {
		this.multiend = multiend;
	}
}
