package com.lz.components.bus.stream.broker;

import java.util.List;
import java.util.function.Function;

import com.lz.components.bus.stream.message.BusMessage;
import com.lz.components.bus.stream.terminal.TerminalConfigurator;
import com.lz.components.bus.stream.terminal.TerminalNode;

/**
 * 手动消费,由BusManager负责拉取
 * 
 * @author fuli
 * @date 2018年11月6日
 * @version 1.0.0
 */
public abstract class ManualConsumeStreamBroker extends AbstractStreamBroker {
	public ManualConsumeStreamBroker(BrokerConfigBean brokerConfig) {
		super(brokerConfig);
	}
	/**
	 * 消费当前节点的消息
	 * 
	 * @param consumer
	 * @throws RuntimeException
	 */
	public void consume(Function<List<BusMessage>, List<ConsumeReceipt>> consumer) throws RuntimeException {
		consume(TerminalConfigurator.getCurrentTerminalNode(), consumer);
	}
	/**
	 * 消费指定节点的消息
	 * @param terminalNode
	 */
	public abstract void consume(TerminalNode terminalNode, Function<List<BusMessage>, List<ConsumeReceipt>> consumer) throws RuntimeException;
}
