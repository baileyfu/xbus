package xbus.stream.broker.rabbit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageBuilderSupport;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactoryUtils;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;

import com.alibaba.fastjson.JSON;
import xbus.constants.HeaderParams;
import xbus.constants.MessageType;
import xbus.core.config.BusConfigBean;
import xbus.stream.broker.BrokerConfigBean;
import xbus.stream.broker.ConsumeReceipt;
import xbus.stream.message.BusMessage;
import xbus.stream.message.MessageCoverter;
import xbus.stream.message.OriginalBusMessage;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalConfigurator;
import xbus.stream.terminal.TerminalNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

/**
 * Rabbit消息处理器
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-22 16:56
 */
public class RabbitMQStreamBroker extends StreamBrokerInitializer implements MessageCoverter{
	public RabbitMQStreamBroker(BusConfigBean busConfig,BrokerConfigBean brokerConfig,RabbitTemplate rt) {
		super(busConfig,brokerConfig,rt);
	}
	private MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
	@Override
	public int consume(TerminalNode terminalNode, Function<List<BusMessage>, List<ConsumeReceipt>> consumer) throws RuntimeException {
		return rabbitTemplate.execute(new ChannelCallback<Integer>(){
			@Override
			public Integer doInRabbit(Channel channel) throws Exception {
				int msgCount = 0;
				long t = System.currentTimeMillis();
				GetResponse response = null;
				while(response==null){
					response = channel.basicGet(terminalNode.getName(), false);
					if (response != null) {
						long deliveryTag = response.getEnvelope().getDeliveryTag();
						msgCount = response.getMessageCount();
						MessageProperties messageProps = messagePropertiesConverter.toMessageProperties(response.getProps(), response.getEnvelope(), "utf-8");
						if (msgCount >= 0) {
							messageProps.setMessageCount(msgCount);
						}
						//不考虑使用外部事务
						boolean transactional=rabbitTemplate.isChannelTransacted() && !ConnectionFactoryUtils.isChannelTransactional(channel, rabbitTemplate.getConnectionFactory());
						List<BusMessage> msgList=new ArrayList<>();
						msgList.add(makeMessage(new Message(response.getBody(), messageProps)));
						if (consumer.apply(msgList).get(0).isAckSuccess()) {
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
				return msgCount;
			}
		});
	}
	private BusMessage makeMessage(Message amqpMessage){
		LOGGER.info(this + " received : {}", amqpMessage);
		MessageProperties messageProperties = amqpMessage.getMessageProperties();
		Map<String,Object> headers=messageProperties.getHeaders();
		Asserts.check(headers != null && headers.size() > 0, "Received amqpMessage's headers should not be null or empty!");
		String path = StringUtils.defaultString((String)headers.get(HeaderParams.XBUS_PATH.name()));
		String sourcePath = StringUtils.defaultString((String)headers.get(HeaderParams.XBUS_SOURCE_PATH.name()));
		String sourceTerminal = StringUtils.defaultString((String)headers.get(HeaderParams.XBUS_SOURCE_TERMINAL.name()));
		String messageType = StringUtils.defaultString((String)headers.get(HeaderParams.XBUS_MESSAGE_TYPE.name()));
		String messageContentType = StringUtils.defaultString((String)headers.get(HeaderParams.XBUS_MESSAGE_CONTENT_TYPE.name()));
		String originals = StringUtils.defaultString((String)headers.get(HeaderParams.XBUS_ORIGINALS.name()));
		String requireReceipt=Optional.ofNullable((String)headers.get(HeaderParams.XBUS_REQUIRE_RECEIPT.name())).orElse(Boolean.FALSE.toString());
		return coverter(path, sourcePath,sourceTerminal, messageType, messageContentType, amqpMessage.getBody(),StringUtils.isBlank(originals)?null:JSON.parseObject(originals),Boolean.valueOf(requireReceipt));
	}
	@Override
	protected void send(Terminal terminal, BusMessage message) {
		MessageBuilderSupport<org.springframework.amqp.core.Message> messageBuilder = MessageBuilder.withBody(message.payload2Bytes()).setContentType(message.getContentType().value);
		//path、sourceTerminal、messageType作为header发送到queue
		messageBuilder.setHeader(HeaderParams.XBUS_PATH.name(), message.getPath());
		messageBuilder.setHeader(HeaderParams.XBUS_SOURCE_TERMINAL.name(), TerminalConfigurator.getCurrentTerminalName());
		messageBuilder.setHeader(HeaderParams.XBUS_MESSAGE_TYPE.name(),message.getMessageType().name());
		messageBuilder.setHeader(HeaderParams.XBUS_MESSAGE_CONTENT_TYPE.name(), message.getContentType().name());
		org.springframework.amqp.core.Message amqpMessage = messageBuilder.build();
		LOGGER.info(this+" has sended : {},{}", terminal, amqpMessage);
		terminal.distribute((terminalNode) -> {
			if (message.getMessageType() == MessageType.RECEIPT||((OriginalBusMessage)message).isUseCurrentNode() || !terminalNode.equals(TerminalConfigurator.getCurrentTerminalNode())) {
				// 以terminal的name为exchange,terminalNode的name为routingKey
				rabbitTemplate.send(terminal.getName(), terminalNode.getName(), amqpMessage, new CorrelationData(terminal.getName()+"-"+terminalNode.getName()));
			}
		});
	}
}
