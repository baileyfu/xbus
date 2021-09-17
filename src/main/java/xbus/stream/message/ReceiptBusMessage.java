package xbus.stream.message;

import xbus.constants.MessageType;
import xbus.stream.message.payload.BusPayload;

/**
 * 回执消息
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-02 11:23
 */
public class ReceiptBusMessage extends BusMessage {
	private String sourcePath;

	public ReceiptBusMessage(String sourcePath) {
		super();
		this.sourcePath = sourcePath;
		this.messageType = MessageType.RECEIPT;
	}

	public ReceiptBusMessage(String sourcePath, BusPayload busPayload) {
		super(busPayload);
		this.sourcePath = sourcePath;
		this.messageType = MessageType.RECEIPT;
	}
	public String getSourcePath() {
		return sourcePath;
	}

	@Override
	public String toString() {
		return "ReceiptBusMessage [sourcePath=" + sourcePath + ", " + super.toString() + "]";
	}
}
