package xbus.core.config;

import java.util.function.Consumer;

import xbus.stream.message.payload.BusPayload;

/**
 * 注册执行器
 * 
 * @author fuli
 * @date 2018年11月28日
 * @version 1.0.0
 */
@FunctionalInterface
public interface RegisterExecuter {
	/**
	 * 注册回执消息处理器
	 * @param terminalName 目标终端名；null则表示注册为全局回执处理器
	 * @param path 目标终端endpoint的path
	 * @param handler 对回执消息的处理
	 */
	void put(String terminalName,String path,Consumer<BusPayload> handler);
}
