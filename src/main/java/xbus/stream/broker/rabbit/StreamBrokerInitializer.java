package xbus.stream.broker.rabbit;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import commons.beanutils.BeanUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.support.PublisherCallbackChannel;

import xbus.bean.EndpointBean;
import xbus.core.config.BusConfigBean;
import xbus.stream.broker.BrokerConfigBean;
import xbus.stream.broker.ManualConsumeStreamBroker;
import xbus.stream.terminal.TerminalNode;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

public abstract class StreamBrokerInitializer extends ManualConsumeStreamBroker{
	protected RabbitTemplate rabbitTemplate;

	public StreamBrokerInitializer(BusConfigBean busConfig,BrokerConfigBean brokerConfig,RabbitTemplate rt) {
		super(busConfig,brokerConfig);
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
	public void initializeChannel(TerminalNode terminalNode,Set<EndpointBean> endpoints) throws Exception {
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

	@Override
	public void destoryChannel() throws Exception {
//		rabbitTemplate.stop();
	}
}
