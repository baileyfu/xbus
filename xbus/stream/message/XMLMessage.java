package xbus.stream.message;

import xbus.stream.MessageContentType;

/**
 * 暂时保存为String
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-19 10:40
 */
public class XMLMessage extends TextMessage {

	public XMLMessage() {
		setContentType(MessageContentType.XML);
	}

	public XMLMessage(byte[] bytes) {
		super(bytes);
		setContentType(MessageContentType.XML);
	}

	public XMLMessage(Object payLoad) {
		super(payLoad);
		setContentType(MessageContentType.XML);
	}
}
