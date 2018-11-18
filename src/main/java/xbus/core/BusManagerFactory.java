package xbus.core;

import java.util.HashMap;
import java.util.Map;

import xbus.stream.broker.StreamBroker;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 总线管理器创建工厂
 * 
 * @author fuli
 * @date 2018年9月30日
 * @version 1.0.0
 */
public class BusManagerFactory {
	public static final String DEFAULT_CACHE_MANAGER_NAME = "DEFAULT_BUS_MANAGER";
	private static BusManagerFactory instance = new BusManagerFactory();
	private static Map<String, BusManager> MANAGER_HOLDER = new HashMap<>();

	public BusManager create(StreamBroker streamBroker, TerminalConfigurator terminalConfigurator) {
		return create(DEFAULT_CACHE_MANAGER_NAME, streamBroker, terminalConfigurator);
	}

	public synchronized BusManager create(String busName, StreamBroker streamBroker, TerminalConfigurator terminalConfigurator) {
		BusManager BusManager = MANAGER_HOLDER.get(busName);
		if (BusManager == null) {
			BusManager = new BusManager(busName, streamBroker, terminalConfigurator);
		}
		return BusManager;
	}

	public static BusManager get() {
		return MANAGER_HOLDER.get(DEFAULT_CACHE_MANAGER_NAME);
	}

	public static BusManager get(String busName) {
		return MANAGER_HOLDER.get(busName);
	}
	
	public static BusManagerFactory getInstance() {
		return instance;
	}
}
