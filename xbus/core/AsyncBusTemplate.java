package xbus.core;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import xbus.em.PostMode;
import xbus.stream.broker.StreamBroker;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 可以选择用异步方式发送消息
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-05 16:16
 */
public class AsyncBusTemplate extends BusTemplate{
	public AsyncBusTemplate(StreamBroker streamBroker, TerminalConfigurator terminalConfigurator){
		super(streamBroker,terminalConfigurator);
	}

	public void asyncPost(BusMessage message, Consumer<Exception> exConsumer){
		CompletableFuture.supplyAsync(() -> {
			try {
				busManager.post(message);
			} catch (Exception e) {
				exConsumer.accept(e);
			}
			return null;
		});
	}

	public void asyncPost(BusMessage message, PostMode postMode, Consumer<Exception> exConsumer) {
		CompletableFuture.supplyAsync(() -> {
			try {
				busManager.post(message, postMode);
			} catch (Exception e) {
				exConsumer.accept(e);
			}
			return null;
		});
	}

	public void asyncPost(String terminalName, BusMessage message, PostMode postMode, Consumer<Exception> exConsumer) {
		CompletableFuture.supplyAsync(() -> {
			try {
				busManager.post(terminalName, message, postMode);
			} catch (Exception e) {
				exConsumer.accept(e);
			}
			return null;
		});
	}

	public void asyncPost(Set<String> terminalNames, BusMessage message, PostMode postMode, Consumer<Exception> exConsumer) {
		CompletableFuture.supplyAsync(() -> {
			try {
				busManager.post(terminalNames, message, postMode);
			} catch (Exception e) {
				exConsumer.accept(e);
			}
			return null;
		});
	}
}
