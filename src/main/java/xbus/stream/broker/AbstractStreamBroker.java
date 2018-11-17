package com.lz.components.bus.stream.broker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apache.http.util.Asserts;

import com.lz.components.bus.stream.message.BusMessage;
import com.lz.components.bus.stream.terminal.Terminal;
import com.lz.components.common.log.holder.CommonLoggerHolder;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public abstract class AbstractStreamBroker implements CommonLoggerHolder, StreamBroker {
	protected static final String NAME_PREFIX="BUS_";
	//consumer
	protected boolean consumeRetryAble;
	protected int consumeRetryCount;
	protected long consumerTimeoutMillis;
	//producer
	protected boolean produceRetryAble;
	protected int produceRetryCount;
	protected long producerTimeoutMillis;
	//other
	protected boolean durable;
	private ConcurrentHashMap<String, Integer> producerRetryReferee;
	private ConcurrentHashMap<String, Integer> consumerRetryReferee;
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
	// 重试发送次数
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
	
	public AbstractStreamBroker(BrokerConfigBean brokerConfig) {
		consumeRetryAble = brokerConfig.isConsumeRetryAble();
		consumeRetryCount = brokerConfig.getConsumeRetryCount();
		consumerTimeoutMillis = brokerConfig.getConsumerTimeoutMillis();
		produceRetryAble = brokerConfig.isProduceRetryAble();
		produceRetryCount = brokerConfig.getProduceRetryCount();
		producerTimeoutMillis = brokerConfig.getProducerTimeoutMillis();
		durable = brokerConfig.isDurable();
		producerRetryReferee = new ConcurrentHashMap<>();
		consumerRetryReferee = new ConcurrentHashMap<>();
	}
	
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

	protected String legalizeName(String source, String... suffixes) {
		StringBuilder temp = new StringBuilder(NAME_PREFIX);
		temp.append(source.replaceAll("\\/","-").replaceAll("\\\\","-").replaceAll("\\.", "-").replaceAll("\\:", "-"));
		if (suffixes.length > 0) {
			for (String suffix : suffixes) {
				temp.append(suffix);
			}
			return temp.substring(0, temp.length() - 1);
		}
		return temp.toString();
	}
	protected abstract void send(Terminal terminal, BusMessage message) throws RuntimeException;
}
