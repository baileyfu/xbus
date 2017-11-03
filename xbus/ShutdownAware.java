package xbus;

/**
 * 系统关闭时需关闭某些资源
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-01 17:45
 */
public abstract class ShutdownAware {
	public ShutdownAware() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try{
					shutdown();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 释放资源
	 */
	abstract protected void shutdown();
}
