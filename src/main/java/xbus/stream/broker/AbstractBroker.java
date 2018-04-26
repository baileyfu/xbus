package xbus.stream.broker;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Value;

import xbus.stream.StreamLoggerHolder;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalInitializingMonitor;
import xbus.stream.terminal.TerminalNode;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public abstract class AbstractBroker implements StreamLoggerHolder, StreamBroker, TerminalInitializingMonitor {
	//consumer
	@Value("${xbus.stiream.broker.consumer.timeoutMillis}")
	protected long consumerTimeoutMillis;
	//producer
	@Value("${xbus.stiream.broker.producer.timeoutMillis}")
	protected long producerTimeoutMillis;
	//other
	@Value("${xbus.stiream.broker.durable}")
	protected boolean durable;

	@Override
	public BusMessage consume(TerminalNode terminalNode) throws RuntimeException {
		Asserts.notNull(terminalNode, "terminalNode");
		return receive(terminalNode);
	}
	protected abstract BusMessage receive(TerminalNode terminalNode) throws RuntimeException;

	@Override
	public void produce(Terminal[] terminals, BusMessage message) throws RuntimeException {
		Asserts.check(terminals != null && terminals.length > 0, "terminals can not be empty");
		for (Terminal terminal : terminals) {
			// 不发送消息到当前节点(放到send里面去做以减少创建List的开销)
			/*if (!producerProperties.isUseCurrentNode() && terminal.getName().equals(Configurator.getCurrentTerminalName())) {
				List<TerminalNode> source=terminal.getNodes();
				if (source != null && source.size() > 0) 
					terminal.setNodes(source.stream().filter((node) -> !node.equals(Configurator.getCurrentTerminalNode())).collect(Collectors.toList()));
			}*/
			send(terminal, message);
		}
	}
	protected abstract void send(Terminal terminal, BusMessage message) throws RuntimeException;
}
