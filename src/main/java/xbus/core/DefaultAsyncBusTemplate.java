package xbus.core;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import xbus.AsyncBusTemplate;
import xbus.constants.PostMode;
import xbus.core.config.BusConfigBean;
import xbus.stream.broker.StreamBroker;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 异步发送消息操作模板
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-05 16:16
 */
public class DefaultAsyncBusTemplate extends xbus.core.DefaultBusTemplate implements AsyncBusTemplate{
	public DefaultAsyncBusTemplate(String busName,StreamBroker streamBroker, TerminalConfigurator terminalConfigurator,BusConfigBean busConfig){
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
