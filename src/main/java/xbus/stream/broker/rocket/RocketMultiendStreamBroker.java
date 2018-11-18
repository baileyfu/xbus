package xbus.stream.broker.rocket;

import java.util.Set;

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
public class RocketMultiendStreamBroker extends RocketMQStreamBroker{
	public RocketMultiendStreamBroker(RocketConfigBean brokerConfig){
		super(brokerConfig);
	}

	/**
	 * 根据terminal类型和path决定发送到那个topic
	 * 
	 * @param terminal
	 * @param path
	 * @return
	 */
	protected String resolveTopicName(final Terminal terminal, final String path) {
		// 发送到所有节点
		if (terminal.getClass() == Terminal.class) {
			return terminal.getName() + path + TOPIC_BROADCASTING;
		} else {
			return terminal.getName() + path + TOPIC_CLUSTERING;
		}
	}
	
	@Override
	protected void send(Terminal terminal, BusMessage message) throws RuntimeException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 针对当前Terminal分别对建立集群消费和广播消费group
	 */
	@Override
	public void initializeChannel(TerminalNode terminalNode,Set<String> endpointList) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
