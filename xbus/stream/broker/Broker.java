package xbus.stream.broker;

import java.util.function.BiFunction;

import xbus.stream.message.Message;
import xbus.stream.terminal.Terminal;

/**
 * 消息处理接口
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-09-05 15:18
 */
public interface Broker {
	Message consume(Terminal terminal, String path, ConsumerProperties consumerProperties) throws RuntimeException;

	default void produce(Terminal terminal, String path, Message message, ProducerProperties producerProperties) throws RuntimeException {
		produce(new Terminal[] { terminal }, path, message, producerProperties);
	}
	
	default void produce(Terminal terminal, String path, BiFunction<String, String, String> pathRander, Message message, ProducerProperties producerProperties) throws RuntimeException {
		produce(new Terminal[] { terminal }, path, pathRander, message, producerProperties);
	}

	void produce(Terminal[] terminals, String path, Message message, ProducerProperties producerProperties) throws RuntimeException;
	
	void produce(Terminal[] terminals, String path, BiFunction<String, String, String> pathRander,Message message, ProducerProperties producerProperties) throws RuntimeException;
}
