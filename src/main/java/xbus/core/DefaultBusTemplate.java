package xbus.core;

import xbus.BusTemplate;
import xbus.constants.PostMode;
import xbus.core.config.BusConfigBean;
import xbus.exception.BusException;
import xbus.exception.BusExceptionCode;
import xbus.stream.broker.StreamBroker;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 同步发送消息操作模板
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-03 09:53
 */
public class DefaultBusTemplate extends AbstractBusAccessor implements BusTemplate{
	public DefaultBusTemplate(String busName,StreamBroker streamBroker, TerminalConfigurator terminalConfigurator,BusConfigBean busConfig) {
		super(busName,streamBroker, terminalConfigurator,busConfig);
	}

	/**
	 * 按轮询节点策略将消息发送到指定终端
	 * 
	 * @param message
	 * @param terminalNames
	 *            指定终端服务名;不能为空
	 * @throws Exception
	 */
	public void post(BusMessage message,String...terminalNames) throws BusException {
		if (busConfig.isEnable()) {
			try {
				busManager.post(message, PostMode.ROUNDROBIN, terminalNames);
			} catch (Exception e) {
				throw new BusException(BusExceptionCode.BUS_POST_FAILED, e);
			}
		}
	}
	/**
	 * 按节点发送策略将消息发送到指定终端
	 * @param message
	 * @param postMode
	 * @param terminalNames 指定终端服务名;不能为空
	 * @throws Exception
	 */
	public void post(BusMessage message, PostMode postMode,String...terminalNames) throws BusException {
		if (busConfig.isEnable()) {
			try{
				busManager.post(message, postMode,terminalNames);
			} catch (Exception e) {
				throw new BusException(BusExceptionCode.BUS_POST_FAILED,e);
			}
		}
	}
}
