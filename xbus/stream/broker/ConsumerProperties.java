package xbus.stream.broker;

/**
 * 消费者配置
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-09-05 14:53
 */
public class ConsumerProperties {
	private long timeoutMillis=-1l;

	public long getTimeoutMillis() {
		return timeoutMillis;
	}

	public void setTimeoutMillis(long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}
}
