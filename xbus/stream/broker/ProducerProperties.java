package xbus.stream.broker;

/**
 * 生产者配置
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-09-05 15:11
 */
public class ProducerProperties {
	private long timeoutMillis = -1l;
	private boolean userTranscation;
	private boolean useCurrentNode;
	public long getTimeoutMillis() {
		return timeoutMillis;
	}
	public void setTimeoutMillis(long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}
	public boolean isUserTranscation() {
		return userTranscation;
	}
	public void setUserTranscation(boolean userTranscation) {
		this.userTranscation = userTranscation;
	}
	public boolean isUseCurrentNode() {
		return useCurrentNode;
	}
	public void setUseCurrentNode(boolean useCurrentNode) {
		this.useCurrentNode = useCurrentNode;
	}
}
