package xbus.core;

import java.util.ArrayList;
import java.util.List;

import xbus.BusLoggerHolder;

/**
 * 系统关闭时需关闭某些资源
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-01 17:45
 */
public abstract class ShutdownAware implements BusLoggerHolder {
	private static final List<ShutdownAware> NEED_POST_HANDLE = new ArrayList<>();
	static{
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				NEED_POST_HANDLE.forEach((x) -> {
					try {
						x.shutdown();
					} catch (Exception e) {
						LOGGER.error("shutdown component " + x + " error!", e);
					}
				});
			}
		});
	}

	public ShutdownAware() {
		NEED_POST_HANDLE.add(this);
	}

	/**
	 * 释放资源
	 */
	abstract protected void shutdown();
}
