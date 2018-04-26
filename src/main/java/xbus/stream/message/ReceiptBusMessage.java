package xbus.stream.message;

import xbus.em.MessageType;
import xbus.stream.message.payload.BusPayload;

/**
 * 回执消息
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-02 11:23
 */
public class ReceiptBusMessage extends BusMessage {
	public ReceiptBusMessage() {
		super();
		this.messageType=MessageType.RECEIPT;
	}
	public ReceiptBusMessage(BusPayload busPayload) {
		super(busPayload);
		this.messageType=MessageType.RECEIPT;
	}
}
