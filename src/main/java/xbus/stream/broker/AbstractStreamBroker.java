package xbus.stream.broker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apache.http.util.Asserts;

import xbus.BusLoggerHolder;
import xbus.core.config.BusConfigBean;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.Terminal;

/**
 * 抽象总线代理器
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public abstract class AbstractStreamBroker implements BusLoggerHolder, xbus.stream.broker.StreamBroker {
	protected BusConfigBean busConfig;
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
	
	public AbstractStreamBroker(BusConfigBean busConfig, xbus.stream.broker.BrokerConfigBean brokerConfig) {
		this.busConfig = busConfig;
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

	static protected String legalizeTopicName(String source, String... suffixes) {
		String result;
		StringBuilder temp = new StringBuilder(xbus.stream.broker.BrokerConst.NAME_PREFIX);
		temp.append(source);
		if (suffixes.length > 0) {
			temp.append(xbus.stream.broker.BrokerConst.SIGN_UNDERLINE);
			for (String suffix : suffixes) {
				temp.append(suffix).append(xbus.stream.broker.BrokerConst.SIGN_UNDERLINE);
			}
			result = temp.substring(0, temp.length() - 1);
		} else {
			result = temp.toString();
		}
		//去除'/'和'\','.'和':'转换为'-'
		return result.replaceAll("\\/","").replaceAll("\\\\","").replaceAll("\\.", "-").replaceAll("\\:", "-");
	}
	protected abstract void send(Terminal terminal, BusMessage message) throws RuntimeException;
}
