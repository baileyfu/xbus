package xbus.stream.broker;

import java.util.List;
import java.util.function.Function;

import xbus.core.config.BusConfigBean;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.TerminalConfigurator;
import xbus.stream.terminal.TerminalNode;

/**
 * 手动消费,由BusManager负责拉取
 * 
 * @author fuli
 * @date 2018年11月6日
 * @version 1.0.0
 */
public abstract class ManualConsumeStreamBroker extends AbstractStreamBroker {
	public ManualConsumeStreamBroker(BusConfigBean busConfig,BrokerConfigBean brokerConfig) {
		super(busConfig,brokerConfig);
	}
	/**
	 * 消费当前节点的消息
	 * 
	 * @param consumer
	 * @throws RuntimeException
	 */
	public int consume(Function<List<BusMessage>, List<ConsumeReceipt>> consumer) throws RuntimeException {
		return consume(TerminalConfigurator.getCurrentTerminalNode(), consumer);
	}
	/**
	 * 消费指定节点的消息
	 * @param terminalNode
	 */
	public abstract int consume(TerminalNode terminalNode, Function<List<BusMessage>, List<ConsumeReceipt>> consumer) throws RuntimeException;
}
