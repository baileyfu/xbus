package xbus.stream.broker;

import java.util.function.Function;

import xbus.stream.message.BusMessage;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalConfigurator;
import xbus.stream.terminal.TerminalNode;

/**
 * 消息处理接口
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-09-05 15:18
 */
public interface StreamBroker {
	/**
	 * 消费当前节点的消息
	 * @return
	 * @throws RuntimeException
	 */
	default BusMessage consume() throws RuntimeException {
		return consume(TerminalConfigurator.getCurrentTerminalNode());
	}
	/**
	 * 消费指定节点的消息
	 * @param terminalNode
	 * @return
	 * @throws RuntimeException
	 */
	BusMessage consume(TerminalNode terminalNode) throws RuntimeException;

	default void consume(Function<BusMessage, Boolean> consumer) throws RuntimeException {
		consume(TerminalConfigurator.getCurrentTerminalNode(), consumer);
	}
	
	void consume(TerminalNode terminalNode, Function<BusMessage, Boolean> consumer) throws RuntimeException;

	default void produce(Terminal terminal, BusMessage message) throws RuntimeException {
		produce(new Terminal[] { terminal }, message);
	}
	
	void produce(Terminal[] terminals, BusMessage message) throws RuntimeException;
}
