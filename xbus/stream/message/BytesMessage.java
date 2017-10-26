package xbus.stream.message;

import xbus.stream.MessageContentType;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public class BytesMessage extends Message {
	private byte[] payLoad;

	public BytesMessage() {
		setContentType(MessageContentType.BYTES);
	}

	public BytesMessage(byte[] bytes) {
		super(bytes);
		setContentType(MessageContentType.BYTES);
	}

	public BytesMessage(Object payLoad) {
		super(payLoad);
		setContentType(MessageContentType.BYTES);
	}

	@Override
	public Object getPayLoad() {
		return payLoad;
	}

	@Override
	public void setPayLoad(Object payLoad) {
		if (!(payLoad instanceof byte[]))
			throw new IllegalArgumentException("BytesMessage's payload only the accept java.lang.byte[]");
		this.payLoad=(byte[])payLoad;
	}

	@Override
	public byte[] toBytes() {
		return payLoad;
	}

	@Override
	protected Object fromBytes(byte[] bytes) {
		return bytes;
	}
}
