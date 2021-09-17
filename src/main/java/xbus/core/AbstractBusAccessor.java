package xbus.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import xbus.stream.terminal.TerminalConfigurator;
import xbus.BusLoggerHolder;
import xbus.core.config.BusConfigBean;
import xbus.stream.broker.StreamBroker;

/**
 * 总线访问器<br/>
 * 负责创建BusManager并启动,系统关闭时释放资源
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-05 16:03
 */
public abstract class AbstractBusAccessor implements BusLoggerHolder {
	@Autowired
	private ApplicationContext applicationContext;
	protected BusConfigBean busConfig;
	xbus.core.BusManager busManager;

	public AbstractBusAccessor(StreamBroker streamBroker, TerminalConfigurator terminalConfigurator, BusConfigBean busConfig) {
		busManager= xbus.core.BusManagerFactory.getInstance().create(streamBroker, terminalConfigurator);
		busManager.setBusConfig(busConfig);
		this.busConfig = busConfig;
	}
	public AbstractBusAccessor(String busName,StreamBroker streamBroker, TerminalConfigurator terminalConfigurator,BusConfigBean busConfig) {
		busManager= xbus.core.BusManagerFactory.getInstance().create(busName,streamBroker, terminalConfigurator);
		busManager.setBusConfig(busConfig);
		this.busConfig = busConfig;
	}
	public boolean isEnable() {
		return busConfig.isEnable();
	}
	/**
	 * 初始化总线,失败则退出程序</p>
	 * 由BusBeanPostProcessor负责调用
	 */
	void initialize(){
		if (busConfig.isEnable()) {
			try {
				busManager.start();
				LOGGER.info("[Bus {}] is running now !",busManager.getName());
			} catch (Exception e) {
				LOGGER.error("The error of the [Bus {}].initialize() has caused System break down...",busManager.getName(),e);
				applicationContext.publishEvent(new ContextClosedEvent(applicationContext));
				System.exit(-1);
			}
		} else {
			LOGGER.warn("[Bus {}] was disabled !",busManager.getName());
		}
	}
	void destroy() throws Exception {
		if (busManager != null) {
			try {
				busManager.stop();
				LOGGER.info("[Bus {}] has stopped !", busManager.getName());
			} catch (Exception e) {
				LOGGER.error("[Bus {}].destroy() error!", busManager.getName(), e);
			}
			busManager = null;
		}
	}
}
