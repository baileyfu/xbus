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
	public static final Long DEFAULT_ACCESS_INTERVAL = 100L;
	
	private boolean enable;
	private boolean asyncAble;
	private long accessInterval;
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
	public long getAccessInterval() {
		return accessInterval;
	}
	public void setAccessInterval(long accessInterval) {
		this.accessInterval = accessInterval;
	}
}
