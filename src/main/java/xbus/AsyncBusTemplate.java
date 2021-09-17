package xbus;

import java.util.function.Consumer;

import xbus.stream.message.BusMessage;
import xbus.constants.PostMode;
import xbus.exception.BusException;

/**
 * 异步发送消息操作模板
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-05 16:16
 */
public interface AsyncBusTemplate extends xbus.BusTemplate {
	public void asyncPost(BusMessage message, Consumer<Exception> exConsumer, String... terminalNames)throws BusException;

	public void asyncPost(BusMessage message, PostMode postMode, Consumer<Exception> exConsumer, String... terminalNames)throws BusException;
}
