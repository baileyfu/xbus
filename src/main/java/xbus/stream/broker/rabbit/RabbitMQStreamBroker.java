package xbus.stream.broker.rabbit;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.http.util.Asserts;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageBuilderSupport;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactoryUtils;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.PublisherCallbackChannel;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import commons.beanutils.BeanUtils;
import commons.lang.StringUtils;
import xbus.em.MessageType;
import xbus.stream.broker.AbstractStreamBroker;
import xbus.stream.message.BusMessage;
import xbus.stream.message.OriginalBusMessage;
import xbus.stream.message.ReceiptBusMessage;
import xbus.stream.message.payload.BusPayload;
import xbus.stream.message.payload.BytesBusPayload;
import xbus.stream.message.payload.JSONBusPayload;
import xbus.stream.message.payload.SerializedObjectBusPayload;
import xbus.stream.message.payload.TextBusPayload;
import xbus.stream.message.payload.XMLBusPayload;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalConfigurator;
import xbus.stream.terminal.TerminalNode;

/**
 * Rabbit消息处理器
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-22 16:56
 */
public class RabbitMQStreamBroker extends AbstractStreamBroker{
	private static final String HEADER_PATH="XBUS_PATH";
	private static final String HEADER_SOURCE_TERMINAL = "XBUS_SOURCE_TERMINAL";
	private static final String HEADER_MESSAGE_TYPE = "XBUS_MESSAGE_TYPE";
	private RabbitTemplate rabbitTemplate;

	public RabbitMQStreamBroker(RabbitTemplate rt) {
		this.rabbitTemplate = rt;
		if (!this.rabbitTemplate.isConfirmListener()) {
			//消息无法发送到Exchange时被触发
			this.rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
				@Override
				public void confirm(CorrelationData correlationData, boolean ack, String cause) {
					if (!ack)
						LOGGER.error(this + " send message to exchange({}) error ! cause: {}", correlationData.getId(), cause);
				}
			});
		}
		if (!this.rabbitTemplate.isReturnListener()) {
			// 消息无法发送到指定的消息队列时被触发
			this.rabbitTemplate.setReturnCallback(new ReturnCallback(){
				@Override
				public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
					LOGGER.error(this+" send message to queue(exchange:{},routingKey:{}) error ! replyCode:{},replyText:{} ; the message content : {}",exchange,routingKey,replyCode,replyText,new String(message.getBody()));
					if (produceRetryAble) {
						String uuidObject = message.getMessageProperties().getHeaders().get(PublisherCallbackChannel.RETURN_CORRELATION_KEY).toString();
						if (retryProduce.test(uuidObject)) {
							CompletableFuture.supplyAsync(() -> {
								String id = exchange + "-" + routingKey;
								if (LOGGER.isDebugEnabled()) {
									LOGGER.debug(this + " resending message [id:{} , body:{}]", id, message);
								}
								try {
									rabbitTemplate.send(exchange, routingKey, message, new CorrelationData(id));
								} catch (Exception e) {
									LOGGER.error("resend message error !", e);
								}
								return null;
							});
						}
					}
				}
			});
		}
	}

	@Override
	protected BusMessage receive(TerminalNode terminalNode) throws RuntimeException {
		org.springframework.amqp.core.Message amqpMessage = rabbitTemplate.receive(terminalNode.getName(), consumerTimeoutMillis);
		if (amqpMessage == null || amqpMessage.getBody() == null)
			return null;
		return convert(amqpMessage);
	}
	private MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
	@Override
	protected void receive(TerminalNode terminalNode, Function<BusMessage, Boolean> consumer) throws RuntimeException {
		rabbitTemplate.execute(new ChannelCallback<Void>(){
			@Override
			public Void doInRabbit(Channel channel) throws Exception {
				long t = System.currentTimeMillis();
				GetResponse response = null;
				while(response==null){
					response = channel.basicGet(terminalNode.getName(), false);
					if (response != null) {
						long deliveryTag = response.getEnvelope().getDeliveryTag();
						int msgCount = response.getMessageCount();
						MessageProperties messageProps = messagePropertiesConverter.toMessageProperties(response.getProps(), response.getEnvelope(), "utf-8");
						if (msgCount >= 0) {
							messageProps.setMessageCount(msgCount);
						}
						//不考虑使用外部事务
						boolean transactional=rabbitTemplate.isChannelTransacted() && !ConnectionFactoryUtils.isChannelTransactional(channel, rabbitTemplate.getConnectionFactory());
						if (consumer.apply(convert(new Message(response.getBody(), messageProps)))) {
							channel.basicAck(deliveryTag, false);
							if (transactional) {
								channel.txCommit();
							}
						} else {
							channel.basicNack(deliveryTag, false, consumeRetryAble && retryConsume.test(response.getEnvelope().toString()));
							if (transactional) {
								channel.txRollback();
							}
						}
					}
					if (consumerTimeoutMillis > -1 && System.currentTimeMillis() - t >= consumerTimeoutMillis)
						break;
				}
				return null;
			}
		});
	}
	private BusMessage convert(Message amqpMessage){
		LOGGER.info(this + " received : {}", amqpMessage);
		MessageProperties messageProperties = amqpMessage.getMessageProperties();
		Map<String,Object> headers=messageProperties.getHeaders();
		Asserts.check(headers != null && headers.size() > 0, "Received amqpMessage's headers should not be null or empty!");
		String path = StringUtils.defaultString(headers.get(HEADER_PATH));
		String sourceTerminal = StringUtils.defaultString(headers.get(HEADER_SOURCE_TERMINAL));
		String messageType = StringUtils.defaultString(headers.get(HEADER_MESSAGE_TYPE));
		Asserts.notEmpty(path, "the value of path of amqpMessage's headers");
		Asserts.notEmpty(sourceTerminal, "the value of sourceTerminal of amqpMessage's headers");
		Asserts.notEmpty(messageType, "the value of messageType of amqpMessage's headers");
		MessageType mt = MessageType.valueOf(messageType);
		Asserts.check(mt != null, "Illegal messageType '" + messageType + "'");
		BusPayload busPayload=null;
		switch (messageProperties.getContentType()) {
		case MessageProperties.CONTENT_TYPE_TEXT_PLAIN:
			busPayload = new TextBusPayload(amqpMessage.getBody());
			break;
		case MessageProperties.CONTENT_TYPE_JSON:
			busPayload = new JSONBusPayload(amqpMessage.getBody());
			break;
		case MessageProperties.CONTENT_TYPE_XML:
			busPayload = new XMLBusPayload(amqpMessage.getBody());
			break;
		case MessageProperties.CONTENT_TYPE_BYTES:
			busPayload = new BytesBusPayload(amqpMessage.getBody());
			break;
		case MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT:
			busPayload = new SerializedObjectBusPayload(amqpMessage.getBody());
			break;
		default:
			throw new TypeNotPresentException(messageProperties.getContentType(), null);
		}
		BusMessage message = mt == MessageType.ORIGINAL ? new OriginalBusMessage() : new ReceiptBusMessage();
		message.setPath(path);
		message.setSourceTerminal(sourceTerminal);
		message.setPayLoad(busPayload);
		return message;
	}
	@Override
	protected void send(Terminal terminal, BusMessage message) {
		MessageBuilderSupport<org.springframework.amqp.core.Message> messageBuilder = MessageBuilder.withBody(message.payload2Bytes()).setContentType(message.getContentType().value);
		//path、sourceTerminal、messageType作为header发送到queue
		messageBuilder.setHeader(HEADER_PATH, message.getPath());
		messageBuilder.setHeader(HEADER_SOURCE_TERMINAL, TerminalConfigurator.getCurrentTerminalName());
		messageBuilder.setHeader(HEADER_MESSAGE_TYPE,message.getMessageType().name());
		org.springframework.amqp.core.Message amqpMessage = messageBuilder.build();
		LOGGER.info(this+" has sended : {},{}", terminal, amqpMessage);
		terminal.distribute((terminalNode) -> {
			if (message.getMessageType() == MessageType.RECEIPT||((OriginalBusMessage)message).isUseCurrentNode() || !terminalNode.equals(TerminalConfigurator.getCurrentTerminalNode())) {
				// 以terminal的name为exchange,terminalNode的name为routingKey
				rabbitTemplate.send(terminal.getName(), terminalNode.getName(), amqpMessage, new CorrelationData(terminal.getName()+"-"+terminalNode.getName()));
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
				AMQP.Exchange.DeclareOk exchangeDeclareOk = channel.exchangeDeclare(terminalNode.getTerminalName(), "direct", durable, false, false, null);
				LOGGER.info("The exchange {} has been created successfully ! detail [{}]", terminalNode.getTerminalName(), BeanUtils.dump(exchangeDeclareOk));
				try {
					AMQP.Queue.DeclareOk queueDeclareOk = channel.queueDeclare(terminalNode.getName(), durable, false, false, null);
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
}
