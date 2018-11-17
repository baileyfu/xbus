package com.lz.components.bus;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.lz.components.bus.core.config.BusConfigBean;
import com.lz.components.bus.em.PostMode;
import com.lz.components.bus.stream.broker.StreamBroker;
import com.lz.components.bus.stream.message.BusMessage;
import com.lz.components.bus.stream.terminal.TerminalConfigurator;

/**
 * 异步发送消息操作模板
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-05 16:16
 */
public class AsyncBusTemplate extends BusTemplate{
	public AsyncBusTemplate(String busName,StreamBroker streamBroker, TerminalConfigurator terminalConfigurator,BusConfigBean busConfig){
		super(busName,streamBroker,terminalConfigurator,busConfig);
	}
	public void asyncPost(BusMessage message, Consumer<Exception> exConsumer,String...terminalNames) {
		if(busConfig.isEnable()){
			CompletableFuture.supplyAsync(() -> {
				try {
					busManager.post(message, PostMode.ROUNDROBIN,terminalNames);
				} catch (Exception e) {
					exConsumer.accept(e);
				}
				return null;
			});
		}
	}
	public void asyncPost(BusMessage message, PostMode postMode, Consumer<Exception> exConsumer,String...terminalNames) {
		if(busConfig.isEnable()){
			CompletableFuture.supplyAsync(() -> {
				try {
					busManager.post(message, postMode,terminalNames);
				} catch (Exception e) {
					exConsumer.accept(e);
				}
				return null;
			});
		}
	}
}
