package xbus.stream.message;

import xbus.stream.MessageContentType;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public class TextMessage extends Message {
	private String payLoad;

	public TextMessage() {
		setContentType(MessageContentType.TEXT);
	}

	public TextMessage(byte[] bytes) {
		super(bytes);
		setContentType(MessageContentType.TEXT);
	}

	public TextMessage(Object payLoad) {
		super(payLoad);
		setContentType(MessageContentType.TEXT);
	}

	@Override
	public Object getPayLoad() {
		return payLoad;
	}

	@Override
	public void setPayLoad(Object payLoad) {
		if (!(payLoad instanceof String))
			throw new IllegalArgumentException("TextMessage's payload only the accept java.lang.String");
		this.payLoad = (String) payLoad;
	}

	@Override
	public byte[] toBytes() {
		return payLoad == null ? null : payLoad.getBytes();
	}

	@Override
	protected Object fromBytes(byte[] bytes) {
		return bytes == null ? null : new String(bytes);
	}
}
