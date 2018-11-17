package com.lz.components.bus;

import com.lz.components.bus.code.BusExceptionCode;
import com.lz.components.bus.core.AbstractBusAccessor;
import com.lz.components.bus.core.config.BusConfigBean;
import com.lz.components.bus.em.PostMode;
import com.lz.components.bus.exception.BusException;
import com.lz.components.bus.stream.broker.StreamBroker;
import com.lz.components.bus.stream.message.BusMessage;
import com.lz.components.bus.stream.terminal.TerminalConfigurator;

/**
 * 同步发送消息操作模板
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-03 09:53
 */
public class BusTemplate extends AbstractBusAccessor {
	public BusTemplate(String busName,StreamBroker streamBroker, TerminalConfigurator terminalConfigurator,BusConfigBean busConfig) {
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
