package xbus;

import xbus.constants.PostMode;
import xbus.exception.BusException;
import xbus.stream.message.BusMessage;

/**
 * 同步发送消息操作模板
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-03 09:53
 */
public interface BusTemplate {
	/**
	 * 按轮询节点策略将消息发送到指定终端
	 * 
	 * @param message
	 * @param terminalNames
	 *            指定终端服务名;不能为空
	 * @throws Exception
	 */
	public void post(BusMessage message,String...terminalNames) throws BusException;
	/**
	 * 按节点发送策略将消息发送到指定终端
	 * @param message
	 * @param postMode 重要：仅目标终端支持nodeOriented时才能选择PostMode.ALL,否则目标终端无法消费此消息
	 * @param terminalNames 指定终端服务名;不能为空
	 * @throws Exception
	 */
	public void post(BusMessage message, PostMode postMode,String...terminalNames) throws BusException;
}
