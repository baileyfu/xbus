package xbus.stream.message;

import xbus.em.MessageContentType;
import xbus.em.MessageType;
import xbus.stream.message.payload.BusPayload;

/**
 * 消息定义
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public abstract class BusMessage {
	/**
	 * 是否用事务方式发送
	 */
	private boolean transactional;
	protected MessageType messageType;
	private String path;
	private String sourceTerminal;
	private BusPayload busPayload;

	public BusMessage() {
		this.transactional = false;
	}
	public BusMessage(BusPayload busPayload) {
		this.transactional = false;
		this.busPayload = busPayload;
	}
	public boolean isTransactional() {
		return transactional;
	}

	public void setTransactional(boolean transactional) {
		this.transactional = transactional;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSourceTerminal() {
		return sourceTerminal;
	}

	public void setSourceTerminal(String sourceTerminal) {
		this.sourceTerminal = sourceTerminal;
	}

	public MessageContentType getContentType() {
		return busPayload == null ? null : busPayload.getContentType();
	}
	
	public BusPayload getPayLoad(){
		return busPayload;
	}

	public void setPayLoad(BusPayload busPayload) {
		this.busPayload = busPayload;
	}
	
	public byte[] payload2Bytes() {
		return busPayload == null ? null : busPayload.toBytes();
	}
}
