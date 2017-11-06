package xbus.stream.message.payload;

import xbus.em.MessageContentType;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public class TextBusPayload extends BusPayload {
	private String value;

	public TextBusPayload() {
		contentType = MessageContentType.TEXT;
	}

	public TextBusPayload(byte[] bytes) {
		contentType = MessageContentType.TEXT;
		this.value = bytes == null ? null : new String(bytes);
	}

	public TextBusPayload(String value) {
		contentType = MessageContentType.TEXT;
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		if (!(value instanceof String))
			throw new IllegalArgumentException("TextBusPayload's value only the accept java.lang.String");
		this.value = (String) value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public byte[] toBytes() {
		return value == null ? null : value.getBytes();
	}
}
