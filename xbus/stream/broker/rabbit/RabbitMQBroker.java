package xbus.stream.broker.rabbit;

import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageBuilderSupport;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import commons.beanutils.BeanUtils;
import xbus.stream.broker.AbstractBroker;
import xbus.stream.broker.ConsumerProperties;
import xbus.stream.broker.ProducerProperties;
import xbus.stream.message.BytesMessage;
import xbus.stream.message.JSONMessage;
import xbus.stream.message.Message;
import xbus.stream.message.SerializedObjectMessage;
import xbus.stream.message.TextMessage;
import xbus.stream.message.XMLMessage;
import xbus.stream.terminal.Configurator;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalNode;

/**
 * Rabbit消息处理器
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-22 16:56
 */
public class RabbitMQBroker extends AbstractBroker{
	private static final String HEADER_PATH_NAME="path_value";
	private RabbitTemplate rabbitTemplate;

	public RabbitMQBroker(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	protected Message receive(Terminal terminal, String path, ConsumerProperties consumerProperties) throws RuntimeException {
		org.springframework.amqp.core.Message amqpMessage = rabbitTemplate.receive("queueName", consumerProperties.getTimeoutMillis());
		if (amqpMessage == null || amqpMessage.getBody() == null)
			return null;
		LOGGER.info("xbus.stream.broker.rabbit.RabbitMQBroker received : {}", amqpMessage);
		Message message = null;
		MessageProperties messageProperties = amqpMessage.getMessageProperties();
		switch (messageProperties.getContentType()) {
		case MessageProperties.CONTENT_TYPE_TEXT_PLAIN:
			message = new TextMessage(amqpMessage.getBody());
			break;
		case MessageProperties.CONTENT_TYPE_JSON:
			message = new JSONMessage(amqpMessage.getBody());
			break;
		case MessageProperties.CONTENT_TYPE_XML:
			message = new XMLMessage(amqpMessage.getBody());
			break;
		case MessageProperties.CONTENT_TYPE_BYTES:
			message = new BytesMessage(amqpMessage.getBody());
			break;
		case MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT:
			message = new SerializedObjectMessage(amqpMessage.getBody());
			break;
		default:
			throw new TypeNotPresentException(messageProperties.getContentType(), null);
		}
		message.setHeader(messageProperties.getHeaders());
		return message;
	}

	@Override
	protected void send(Terminal terminal, String finalPath, Message message, ProducerProperties producerProperties) {
		MessageBuilderSupport<org.springframework.amqp.core.Message> messageBuilder = MessageBuilder.withBody(message.toBytes()).setContentType(message.getContentType());
		//path作为header发送到queue
		messageBuilder.setHeader(HEADER_PATH_NAME, finalPath);
		Map<String, Object> header = message.getHeader();
		if (header != null && header.size() > 0) {
			for (String key : header.keySet()) {
				messageBuilder.setHeader(key, header.get(key));
			}
		}
		org.springframework.amqp.core.Message amqpMessage = messageBuilder.build();
		LOGGER.info("xbus.stream.broker.rabbit.RabbitMQBroker sended : {},{}", terminal, amqpMessage);
		terminal.distribute((terminalNode) -> {
			if (producerProperties.isUseCurrentNode() || !terminalNode.equals(Configurator.getCurrentTerminalNode())) {
				// 以terminal的name为exchange,terminalNode的name为routingKey
				rabbitTemplate.send(terminal.getName(), terminalNode.getName(), amqpMessage);
			}
		});
	}
	
	@Override
	public void initializeChannel(TerminalNode terminalNode) throws Exception {
		Connection conn = null;
		Channel channel = null;
		try {
			conn = rabbitTemplate.getConnectionFactory().createConnection();
			channel = conn.createChannel(true);
			try {
				AMQP.Exchange.DeclareOk exchangeDeclareOk = channel.exchangeDeclare(terminalNode.getTerminalName(), "direct", true, false, false, null);
				LOGGER.info("The exchange {} has been created successfully ! detail [{}]", terminalNode.getTerminalName(), BeanUtils.dump(exchangeDeclareOk));
				try {
					AMQP.Queue.DeclareOk queueDeclareOk = channel.queueDeclare(terminalNode.getName(), terminalNode.isDurable(), false, false, null);
					LOGGER.info("The queue {} has been created successfully ! detail [{}]", queueDeclareOk.getQueue(), BeanUtils.dump(queueDeclareOk));
					AMQP.Queue.BindOk bindOk = channel.queueBind(queueDeclareOk.getQueue(), terminalNode.getTerminalName(), terminalNode.getName());
					LOGGER.info("The binding {} has been done successfully ! detail [{}]", "", BeanUtils.dump(bindOk));
				} catch (Exception e) {
					LOGGER.error("channel create queue[" + terminalNode.getName() + "] error!", e);
				}
			} catch (Exception e) {
				LOGGER.error("channel create exchange[" + terminalNode.getTerminalName() + "] error!", e);
			}
		} catch (Exception e) {
			LOGGER.error("create rabbitmq queue error!", e);
			throw e;
		} finally {
			try{
				if (conn != null) {
					conn.close();
				}
				if(channel!=null){
					channel.close();
				}
			} catch (Exception e) {
				LOGGER.error("closing Connection&Channel error!", e);
			}
		}
	}
	private static BiFunction<String, String, String> pathRander = (terminalName, path) -> path;
	@Override
	protected BiFunction<String, String, String> getPathRender() {
		return pathRander;
	}
}
