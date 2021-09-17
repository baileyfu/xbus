package xbus.stream.message.payload;

import xbus.constants.MessageContentType;

/**
 * 暂时保存为String<br/>
 * 因text/html默认使用us-ascii编码，仅支持application/xml；XML内容需有encoding
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-19 10:40
 */
public class XMLBusPayload extends BusPayload {
	private String value;
	public XMLBusPayload() {
		contentType = MessageContentType.XML;
	}

	public XMLBusPayload(byte[] bytes) {
		contentType = MessageContentType.XML;
		this.value = bytes == null ? null : new String(bytes);
	}

	public XMLBusPayload(String value) {
		contentType = MessageContentType.XML;
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		if (!(value instanceof String))
			throw new IllegalArgumentException("XMLBusPayload's value only accept java.lang.String");
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
