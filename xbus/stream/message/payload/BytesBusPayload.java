package xbus.stream.message.payload;

import xbus.em.MessageContentType;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public class BytesBusPayload extends BusPayload {
	private byte[] value;

	public BytesBusPayload() {
		contentType = MessageContentType.BYTES;
	}

	public BytesBusPayload(byte[] bytes) {
		contentType = MessageContentType.BYTES;
		this.value = bytes;
	}

	@Override
	public void setValue(Object value) {
		if (!(value instanceof byte[]))
			throw new IllegalArgumentException("BytesMessage's payload only the accept java.lang.byte[]");
		this.value=(byte[])value;
	}
	@Override
	public Object getValue() {
		return value;
	}
	@Override
	public byte[] toBytes() {
		return value;
	}
}
