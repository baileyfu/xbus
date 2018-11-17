package com.lz.components.bus.stream.broker;

import java.util.Set;

import com.lz.components.bus.stream.message.BusMessage;
import com.lz.components.bus.stream.terminal.Terminal;
import com.lz.components.bus.stream.terminal.TerminalNode;

/**
 * 消息流代理
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-09-05 15:18
 */
public interface StreamBroker {
	/**
	 * 初始化当前终端对应的处理通道<br/>
	 * 
	 * MQ实现方式：针对terminalName+endpoint分别建立Topic和Queue，各MQ实现细节不同
	 * 
	 * @param currentTerminalNode 当前服务节点
	 * @param endpointList 当前服务所提供的endpoint
	 * @throws Exception
	 */
	public void initializeChannel(TerminalNode currentTerminalNode,Set<String> endpointList) throws Exception;
	/**
	 * 释放通道相关资源
	 * @throws Exception
	 */
	public void destoryChannel() throws Exception;
	
	default void produce(Terminal terminal, BusMessage message) throws RuntimeException {
		produce(new Terminal[] { terminal }, message);
	}
	
	void produce(Terminal[] terminals, BusMessage message) throws RuntimeException;
}
