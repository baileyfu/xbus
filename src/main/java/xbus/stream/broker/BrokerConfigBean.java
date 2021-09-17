package xbus.stream.broker;

public class BrokerConfigBean {
	public static final Boolean DEFAULT_CONSUME_RETRYABLE = true;
	public static final Integer DEFAULT_CONSUME_RETRY_COUNT = 1;
	public static final Long DEFAULT_CONSUME_TIMEOUT_MILLIS = 5 * 60 * 1000L;
	public static final Boolean DEFAULT_PRODUCE_RETRYABLE = true;
	public static final Integer DEFAULT_PRODUCE_RETRY_COUNT = 1;
	public static final Long DEFAULT_PRODUCE_TIMEOUT_MILLIS = 3000L;
	public static final Boolean DEFAULT_DURABLE = false;
	//consumer
	protected boolean consumeRetryAble;
	protected int consumeRetryCount;
	protected long consumerTimeoutMillis;
	//producer
	protected boolean produceRetryAble;
	protected int produceRetryCount;
	protected long producerTimeoutMillis;
	//other
	protected boolean durable;

	public BrokerConfigBean() {
		consumeRetryAble = DEFAULT_CONSUME_RETRYABLE;
		consumeRetryCount = DEFAULT_CONSUME_RETRY_COUNT;
		consumerTimeoutMillis = DEFAULT_CONSUME_TIMEOUT_MILLIS;
		produceRetryAble = DEFAULT_PRODUCE_RETRYABLE;
		produceRetryCount = DEFAULT_PRODUCE_RETRY_COUNT;
		producerTimeoutMillis = DEFAULT_PRODUCE_TIMEOUT_MILLIS;
		durable = DEFAULT_DURABLE;

	}
	public boolean isConsumeRetryAble() {
		return consumeRetryAble;
	}
	public void setConsumeRetryAble(boolean consumeRetryAble) {
		this.consumeRetryAble = consumeRetryAble;
	}
	public int getConsumeRetryCount() {
		return consumeRetryCount;
	}
	public void setConsumeRetryCount(int consumeRetryCount) {
		this.consumeRetryCount = consumeRetryCount;
	}
	public long getConsumerTimeoutMillis() {
		return consumerTimeoutMillis;
	}
	public void setConsumerTimeoutMillis(long consumerTimeoutMillis) {
		this.consumerTimeoutMillis = consumerTimeoutMillis;
	}
	public boolean isProduceRetryAble() {
		return produceRetryAble;
	}
	public void setProduceRetryAble(boolean produceRetryAble) {
		this.produceRetryAble = produceRetryAble;
	}
	public int getProduceRetryCount() {
		return produceRetryCount;
	}
	public void setProduceRetryCount(int produceRetryCount) {
		this.produceRetryCount = produceRetryCount;
	}
	public long getProducerTimeoutMillis() {
		return producerTimeoutMillis;
	}
	public void setProducerTimeoutMillis(long producerTimeoutMillis) {
		this.producerTimeoutMillis = producerTimeoutMillis;
	}
	public boolean isDurable() {
		return durable;
	}
	public void setDurable(boolean durable) {
		this.durable = durable;
	}
}
