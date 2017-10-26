package xbus.stream.message;

import xbus.stream.MessageContentType;

/**
 * 需要制定序列化方法;暂不实现
 * @author bailey
 * @version 1.0
 * @date 2017-10-19 10:27
 */
public class SerializedObjectMessage extends Message {
	public SerializedObjectMessage() {
		setContentType(MessageContentType.SERIALIZED_OBJECT);
	}

	public SerializedObjectMessage(byte[] bytes) {
		super(bytes);
		setContentType(MessageContentType.SERIALIZED_OBJECT);
	}

	public SerializedObjectMessage(Object payLoad) {
		super(payLoad);
		setContentType(MessageContentType.SERIALIZED_OBJECT);
	}

	@Override
	public Object getPayLoad() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPayLoad(Object payLoad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object fromBytes(byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}
}
