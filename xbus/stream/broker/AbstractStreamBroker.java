package xbus.stream.broker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

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
public abstract class AbstractStreamBroker implements StreamLoggerHolder, StreamBroker, TerminalInitializingMonitor {
	//consumer
	@Value("${xbus.stiream.broker.producer.retryAble}")
	protected boolean consumeRetryAble;
	@Value("${xbus.stiream.broker.consumer.retryCount}")
	protected int consumeRetryCount;
	@Value("${xbus.stiream.broker.consumer.timeoutMillis}")
	protected long consumerTimeoutMillis;
	//producer
	@Value("${xbus.stiream.broker.producer.retryAble}")
	protected boolean produceRetryAble;
	@Value("${xbus.stiream.broker.producer.retryCount}")
	protected int produceRetryCount;
	@Value("${xbus.stiream.broker.producer.timeoutMillis}")
	protected long producerTimeoutMillis;
	//other
	@Value("${xbus.stiream.broker.durable}")
	protected boolean durable;
	
	private ConcurrentHashMap<String, Integer> producerRetryReferee = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Integer> consumerRetryReferee = new ConcurrentHashMap<>();
	//重试消费次数
	protected Predicate<String> retryConsume = (key) -> {
		Integer counter = consumerRetryReferee.get(key);
		counter = counter == null ? 1 : counter + 1;
		boolean retryAble = counter <= consumeRetryCount;
		if (retryAble) {
			consumerRetryReferee.put(key, counter);
		} else {
			consumerRetryReferee.remove(key);
		}
		return retryAble;
	};
	//重试发送次数
	protected Predicate<String> retryProduce = (key) -> {
		Integer counter = producerRetryReferee.get(key);
		counter = counter == null ? 1 : counter + 1;
		boolean retryAble = counter <= produceRetryCount;
		if (retryAble) {
			producerRetryReferee.put(key, counter);
		} else {
			producerRetryReferee.remove(key);
		}
		return retryAble;
	};
	public AbstractStreamBroker(){
		
	}
	@Override
	public BusMessage consume(TerminalNode terminalNode) throws RuntimeException {
		Asserts.notNull(terminalNode, "terminalNode");
		return receive(terminalNode);
	}
	@Override
	public void consume(TerminalNode terminalNode, Function<BusMessage, Boolean> consumer) throws RuntimeException {
		Asserts.notNull(terminalNode, "terminalNode");
		receive(terminalNode,consumer);
	}
	protected abstract BusMessage receive(TerminalNode terminalNode) throws RuntimeException;
	protected abstract void receive(TerminalNode terminalNode, Function<BusMessage, Boolean> consumer) throws RuntimeException;

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
	public boolean isConsumeRetryAble() {
		return consumeRetryAble;
	}
	public boolean isProduceRetryAble() {
		return produceRetryAble;
	}
	protected abstract void send(Terminal terminal, BusMessage message) throws RuntimeException;
}
