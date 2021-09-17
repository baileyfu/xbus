package xbus.stream.message;

import java.util.function.Consumer;

import xbus.constants.MessageType;
import xbus.stream.message.payload.BusPayload;

/**
 * 原消息
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-02 11:22
 */
public class OriginalBusMessage extends BusMessage {
	/**
	 * 是否发送到当前节点;默认true
	 */
	private boolean useCurrentNode;
	/**
	 * 需要回执;默认false
	 */
	private boolean requireReceipt;
	/**
	 * 回执消息处理器
	 */
	private Consumer<? extends BusPayload> receiptConsumer;
	public OriginalBusMessage() {
		super();
		init();
	}
	public OriginalBusMessage(BusPayload busPayload) {
		super(busPayload);
		init();
	}
	private void init(){
		useCurrentNode = true;
		requireReceipt = false;
		this.messageType=MessageType.ORIGINAL;
	}
	public boolean isUseCurrentNode() {
		return useCurrentNode;
	}

	public void setUseCurrentNode(boolean useCurrentNode) {
		this.useCurrentNode = useCurrentNode;
	}
	public boolean isRequireReceipt() {
		return requireReceipt;
	}

	public void setRequireReceipt(boolean requireReceipt) {
		this.requireReceipt = requireReceipt;
	}
	public Consumer<? extends BusPayload> getReceiptConsumer() {
		return receiptConsumer;
	}
	public void setReceiptConsumer(Consumer<? extends BusPayload> receiptConsumer) {
		this.receiptConsumer = receiptConsumer;
	}
	@Override
	public String toString() {
		return "OriginalBusMessage [useCurrentNode=" + useCurrentNode + ", requireReceipt=" + requireReceipt
				+ ", receiptConsumer=" + receiptConsumer + ", " + super.toString() + "]";
	}
}
