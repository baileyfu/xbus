package xbus.stream.broker;

import java.util.function.BiFunction;

import org.apache.http.util.Asserts;

import xbus.stream.StreamLoggerHolder;
import xbus.stream.message.Message;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalInitializingMonitor;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public abstract class AbstractBroker implements StreamLoggerHolder, Broker, TerminalInitializingMonitor {

	@Override
	public Message consume(Terminal terminal, String path, ConsumerProperties consumerProperties) throws RuntimeException {
		Asserts.notNull(terminal, "terminal");
		Asserts.notEmpty(path, "path");
		return receive(terminal, path, consumerProperties);
	}

	protected abstract Message receive(Terminal terminal, String path, ConsumerProperties consumerProperties) throws RuntimeException;

	@Override
	public void produce(Terminal terminal, String path, Message message, ProducerProperties producerProperties) throws RuntimeException {
		produce(new Terminal[] { terminal }, path, getPathRender(), message, producerProperties);
	}

	@Override
	public void produce(Terminal[] terminals, String path, Message message, ProducerProperties producerProperties) throws RuntimeException {
		produce(terminals, path, getPathRender(), message, producerProperties);
	}

	@Override
	public void produce(Terminal[] terminals, String path, BiFunction<String, String, String> pathRander, Message message,ProducerProperties producerProperties) throws RuntimeException {
		if (terminals == null || terminals.length == 0)
			throw new IllegalStateException("terminals can not be empty");
		Asserts.notEmpty(path, "path");
		Asserts.notNull(pathRander, "pathRander");
		for (Terminal terminal : terminals) {
			// 不发送消息到当前节点(放到send里面去做以减少创建List的开销)
			/*if (!producerProperties.isUseCurrentNode() && terminal.getName().equals(Configurator.getCurrentTerminalName())) {
				List<TerminalNode> source=terminal.getNodes();
				if (source != null && source.size() > 0) 
					terminal.setNodes(source.stream().filter((node) -> !node.equals(Configurator.getCurrentTerminalNode())).collect(Collectors.toList()));
			}*/
			send(terminal, pathRander.apply(terminal.getName(), path), message, producerProperties);
		}
	}
	protected abstract void send(Terminal terminal, String finalPath, Message message, ProducerProperties producerProperties) throws RuntimeException;
	protected abstract BiFunction<String, String, String> getPathRender();
}
