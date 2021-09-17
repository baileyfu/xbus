package xbus.stream.message.payload;

import xbus.constants.MessageContentType;

/**
 * 需要制定序列化方法;暂不实现
 * @author bailey
 * @version 1.0
 * @date 2017-10-19 10:27
 */
public class SerializedObjectBusPayload extends BusPayload {
	public SerializedObjectBusPayload() {
		contentType = MessageContentType.SERIALIZED_OBJECT;
	}

	public SerializedObjectBusPayload(byte[] bytes) {
		contentType = MessageContentType.SERIALIZED_OBJECT;
	}

	public SerializedObjectBusPayload(Object payLoad) {
		contentType = MessageContentType.SERIALIZED_OBJECT;
	}
	@Override
	public void setValue(Object payLoad) {
		// TODO Auto-generated method stub
	}
	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		return null;
	}
}
