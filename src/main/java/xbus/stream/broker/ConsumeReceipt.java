package xbus.stream.broker;

/**
 * 消费回执<br/>
 * 标记消息消费状态
 * 
 * @author fuli
 * @date 2018年11月1日
 * @version 1.0.0
 */
public class ConsumeReceipt {
	private static final int SUCCESS = 1;
	private static final int FAILED = 0;
	/** 原消息标记ID;各实现不同,各自分析 */
	private String messageId;
	/** 是否消费成功 */
	private int ack;

	public ConsumeReceipt(String messageId) {
		this.messageId = messageId;
		ack = FAILED;
	}

	public String getMessageId() {
		return messageId;
	}

	public void ackSuccess() {
		ack = SUCCESS;
	}

	public void ackFailed() {
		ack = FAILED;
	}

	public boolean isAckSuccess() {
		return ack == SUCCESS;
	}
}
