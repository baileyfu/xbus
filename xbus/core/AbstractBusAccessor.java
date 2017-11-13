package xbus.core;

import xbus.stream.broker.StreamBroker;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 总线访问器<br/>
 * 对外提供服务的接口<br/>
 * 负责创建BusManager并启动,系统关闭时释放资源
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-05 16:03
 */
public abstract class AbstractBusAccessor extends ShutdownAware {
	protected BusManager busManager;

	public AbstractBusAccessor() {
		busManager = null;
	}
	public AbstractBusAccessor(StreamBroker streamBroker, TerminalConfigurator terminalConfigurator) {
		BusManager.create(streamBroker, terminalConfigurator);
	}

	public void init() {
		if (busManager == null) {
			busManager = BusManager.getInstance();
			busManager.start();
		}
	}

	@Override
	protected void shutdown() {
		if (busManager != null) {
			busManager.stop();
			busManager = null;
		}
	}
}
