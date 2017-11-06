package xbus.core;

import xbus.stream.broker.StreamBroker;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 总线访问器<br/>
 * 对外提供服务的接口
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-05 16:03
 */
public abstract class AbstractBusAccessor {
	protected BusManager busManager;

	public AbstractBusAccessor(StreamBroker streamBroker, TerminalConfigurator terminalConfigurator) {
		BusManager.create(streamBroker, terminalConfigurator);
	}

	public void init() {
		busManager = BusManager.getInstance();
	}
}
