package xbus.stream.message.payload;

import xbus.em.MessageContentType;

/**
 * 消息负载
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-02 10:19
 */
public abstract class BusPayload {
	protected MessageContentType contentType;

	public BusPayload() {
	}

	public MessageContentType getContentType() {
		return contentType;
	}

	public abstract void setValue(Object value);
	public abstract Object getValue();
	public abstract byte[] toBytes();
}
