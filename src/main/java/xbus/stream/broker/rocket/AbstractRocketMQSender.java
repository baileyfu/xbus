package xbus.stream.broker.rocket;

import org.apache.http.util.Asserts;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;

import xbus.em.HeaderParams;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 发送RocketMQ消息
 * 
 * @author BaileyFu
 * @version v1.0
 * @date 2018年11月18日
 */
public abstract class AbstractRocketMQSender extends StreamBrokerInitializer{

	public AbstractRocketMQSender(RocketConfigBean brokerConfig) {
		super(brokerConfig);
	}
	@Override
	protected void send(Terminal terminal, BusMessage message) throws RuntimeException {
		Asserts.check(!terminal.getName().equals(TerminalConfigurator.getCurrentTerminalName()), "cannot send messages to self!");
		Message msg = new Message();
		msg.setTopic(resolveTopicName(terminal,message));
		msg.putUserProperty(HeaderParams.XBUS_PATH.name(), message.getPath());
		msg.putUserProperty(HeaderParams.XBUS_SOURCE_TERMINAL.name(), TerminalConfigurator.getCurrentTerminalName());
		msg.putUserProperty(HeaderParams.XBUS_MESSAGE_TYPE.name(), message.getMessageType().name());
		msg.putUserProperty(HeaderParams.XBUS_MESSAGE_CONTENT_TYPE.name(), message.getContentType().name());
		msg.setBody(message.getPayLoad().toBytes());
		msg.setKeys(message.getMessageId());//在控制台根据key查询消息
		try {
			/** send方法是同步调用，只要不抛异常就标识成功。但是发送成功也可会有多种状态 */
			SendResult sendResult = producer.send(msg);
			SendStatus sendStatus = sendResult.getSendStatus();
			//为确保消息不丢失,刷盘失败则认为发送失败(同步刷盘情况下会出现)
			Asserts.check(sendStatus != SendStatus.FLUSH_DISK_TIMEOUT, "send not ok , status:" + sendResult);
		} catch (Exception e) {
			new RuntimeException(e);
		}
	}
	/**
	 * 根据terminal类型和消息决定发送到那个topic
	 * @param terminal
	 * @param message
	 * @return
	 */
	abstract protected String resolveTopicName(final Terminal terminal,final BusMessage message);
}
