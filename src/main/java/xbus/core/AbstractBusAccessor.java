package xbus.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextClosedEvent;

import xbus.BusLoggerHolder;
import xbus.core.config.BusConfigBean;
import xbus.stream.broker.StreamBroker;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 总线访问器<br/>
 * 对外提供服务的接口<br/>
 * 负责创建BusManager并启动,系统关闭时释放资源</p>
 * 改为Spring负责初始化与关闭资源
 * 
 * @author bailey
 * @version 1.1
 * @date 2017-11-05 16:03
 * @update 2018-11-16
 */
public abstract class AbstractBusAccessor implements ApplicationContextAware,InitializingBean,DisposableBean,BusLoggerHolder{
	private ApplicationContext applicationContext;
	protected BusConfigBean busConfig;
	protected BusManager busManager;

	public AbstractBusAccessor(StreamBroker streamBroker, TerminalConfigurator terminalConfigurator,BusConfigBean busConfig) {
		busManager=BusManagerFactory.getInstance().create(streamBroker, terminalConfigurator);
		this.busConfig = busConfig;
	}
	public AbstractBusAccessor(String busName,StreamBroker streamBroker, TerminalConfigurator terminalConfigurator,BusConfigBean busConfig) {
		busManager=BusManagerFactory.getInstance().create(busName,streamBroker, terminalConfigurator);
		this.busConfig = busConfig;
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	public boolean isEnable() {
		return busConfig.isEnable();
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		if (busConfig.isEnable()) {
			try {
				busManager.start();
				LOGGER.info("Bus " + busManager.getName() + " is running now !");
			} catch (Exception e) {
				LOGGER.error("The error of the Bus " + busManager.getName() + ".init() has caused System break down...",e);
				applicationContext.publishEvent(new ContextClosedEvent(applicationContext));
				System.exit(-1);
			}
		} else {
			LOGGER.warn("Bus " + busManager.getName() + " was disabled !");
		}
	}
	@Override
	public void destroy() throws Exception {
		if (busManager != null) {
			try{
				busManager.stop();
				LOGGER.info("Bus " + busManager.getName() + " has stopped !");
			} catch (Exception e) {
				LOGGER.error(this+".destroy() error!", e);
			}
			busManager = null;
		}
	}
}
