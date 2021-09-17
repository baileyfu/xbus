package xbus.stream.broker;

import java.util.List;
import java.util.function.Function;

import xbus.core.config.BusConfigBean;
import xbus.stream.message.BusMessage;

/**
 * 由Broker负责消费
 * 
 * @author fuli
 * @date 2018年11月6日
 * @version 1.0.0
 */
public abstract class AutoConsumeStreamBroker extends AbstractStreamBroker {
	public AutoConsumeStreamBroker(BusConfigBean busConfig, xbus.stream.broker.BrokerConfigBean brokerConfig) {
		super(busConfig,brokerConfig);
	}

	/**
	 * 开始接收消息
	 */
	public abstract void startReceive(long consumeIntervalMills,Function<List<BusMessage>,List<xbus.stream.broker.ConsumeReceipt>> consumer)throws Exception;

	/**
	 * 停止接收消息
	 */
	public abstract void stopReceive();
}
