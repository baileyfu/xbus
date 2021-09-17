package xbus.core.config;

/**
 * 总线配置
 * 
 * @author fuli
 * @date 2018年11月12日
 * @version 1.0.0
 */
public class BusConfigBean {
	public static final Boolean DEFAULT_ENABLE = true;
	public static final Boolean DEFAULT_ASYNCABLE = false;
	public static final Boolean DEFAULT_NODE_ORIENTED = false;
	public static final Long DEFAULT_ACCESS_INTERVAL = 100L;
	public static final Long DEFAULT_PAUSE_IF_NOCONSUME = 1000L;
	public static final Boolean DEFAULT_POST_UNDEFINED = false;
	public static final Boolean DEFAULT_POST_DEBUG = false;
	public static final Boolean DEFAULT_CONSUME_DEBUG = false;
	
	private boolean enable;
	private boolean asyncAble;
	private boolean nodeOriented;
	private long accessInterval;
	private long pauseIfNoConsume;
	private boolean postUndefined;
	private boolean postDebug;
	private boolean consumeDebug;

	public BusConfigBean() {
		enable = DEFAULT_ENABLE;
		asyncAble = DEFAULT_ASYNCABLE;
		nodeOriented = DEFAULT_NODE_ORIENTED;
		accessInterval = DEFAULT_ACCESS_INTERVAL;
		pauseIfNoConsume = DEFAULT_PAUSE_IF_NOCONSUME;
		postUndefined = DEFAULT_POST_UNDEFINED;
		postDebug = DEFAULT_POST_DEBUG;
		consumeDebug = DEFAULT_CONSUME_DEBUG;
	}
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public boolean isAsyncAble() {
		return asyncAble;
	}
	public void setAsyncAble(boolean asyncAble) {
		this.asyncAble = asyncAble;
	}
	public boolean isNodeOriented() {
		return nodeOriented;
	}
	public void setNodeOriented(boolean nodeOriented) {
		this.nodeOriented = nodeOriented;
	}
	public long getAccessInterval() {
		return accessInterval;
	}
	public void setAccessInterval(long accessInterval) {
		this.accessInterval = accessInterval;
	}
	public long getPauseIfNoConsume() {
		return pauseIfNoConsume;
	}
	public void setPauseIfNoConsume(long pauseIfNoConsume) {
		this.pauseIfNoConsume = pauseIfNoConsume;
	}
	public boolean isPostUndefined() {
		return postUndefined;
	}
	public void setPostUndefined(boolean postUndefined) {
		this.postUndefined = postUndefined;
	}
	public boolean isPostDebug() {
		return postDebug;
	}
	public void setPostDebug(boolean postDebug) {
		this.postDebug = postDebug;
	}
	public boolean isConsumeDebug() {
		return consumeDebug;
	}
	public void setConsumeDebug(boolean consumeDebug) {
		this.consumeDebug = consumeDebug;
	}
}
