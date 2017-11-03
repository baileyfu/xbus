package xbus;

import java.util.Set;

import xbus.em.PostMode;
import xbus.stream.broker.StreamBroker;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 总线模板<br/>
 * 唯一对外提供服务的接口
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-03 09:53
 */
public class BusTemplate {
	private BusManager busManager;

	public BusTemplate(StreamBroker streamBroker, TerminalConfigurator terminalConfigurator) {
		BusManager.create(streamBroker, terminalConfigurator);
	}

	public void init() {
		busManager = BusManager.getInstance();
		busManager.start();
	}

	/**
	 * 发送给所有终端的所有节点
	 * 
	 * @param message
	 * @throws Exception
	 */
	public void post(BusMessage message) throws Exception {
		busManager.post(message);
	}

	/**
	 * 按指定发送模式发送给所有终端
	 * 
	 * @param message
	 * @param postMode
	 * @throws Exception
	 */
	public void post(BusMessage message, PostMode postMode) throws Exception {
		busManager.post(message, postMode);
	}

	/**
	 * 按指定发送模式发送给指定终端
	 * 
	 * @param terminalName
	 * @param message
	 * @param postMode
	 * @throws Exception
	 */
	public void post(String terminalName, BusMessage message, PostMode postMode) throws Exception {
		busManager.post(terminalName, message, postMode);
	}

	/**
	 * 按节点发送策略将消息发送到指定终端
	 * 
	 * @param terminalNames
	 * @param message
	 * @param postMode
	 * @throws Exception
	 */
	public void post(Set<String> terminalNames, BusMessage message, PostMode postMode) throws Exception {
		busManager.post(terminalNames, message, postMode);
	}
}
