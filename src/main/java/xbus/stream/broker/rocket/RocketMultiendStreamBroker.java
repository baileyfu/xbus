package xbus.stream.broker.rocket;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;

import xbus.stream.broker.ConsumeReceipt;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalNode;

/**
 * Rocket多路径消息代理器<br/>
 * 为节点的每个path分别创建Topic和Queue
 * 
 * @author fuli
 * @date 2018年10月25日
 * @version 1.0.0
 */
public class RocketMultiendStreamBroker extends AbstractRocketMQSender{
	public RocketMultiendStreamBroker(RocketConfigBean brokerConfig){
		super(brokerConfig);
	}
	
	/**
	 * 针对当前Terminal分别对建立集群消费和广播消费group
	 */
	@Override
	public void initializeChannel(TerminalNode terminalNode,Set<String> endpointList) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destoryChannel() throws Exception {
		// TODO Auto-generated method stub
		super.destoryChannel();
	}

	@Override
	protected String resolveTopicName(final Terminal terminal,final BusMessage message) {
		// 发送到所有节点
		if (terminal.getClass() == Terminal.class) {
			return terminal.getName() + message.getPath() + TOPIC_BROADCASTING;
		} else {
			return terminal.getName() + message.getPath() + TOPIC_CLUSTERING;
		}
	}

	@Override
	protected ConsumeConcurrentlyStatus doConsume(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startReceive(long consumeIntervalMills, Function<List<BusMessage>, List<ConsumeReceipt>> consumer)
			throws Exception {
	}

	@Override
	public void stopReceive() {
	}
}
