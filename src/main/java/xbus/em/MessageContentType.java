package com.lz.components.bus.em;

/**
 * 消息类型
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-31 15:02
 */
public enum MessageContentType {
	TEXT("text/plain"), 
	JSON("application/json"),
	/**
	 * 因text/html默认使用us-ascii编码，仅支持application/xml；XML内容需有encoding
	 */
	XML("application/xml"), 
	BYTES("application/octet-stream"), 
	SERIALIZED_OBJECT("application/x-java-serialized-object");
	public String value;
	private MessageContentType(String value) {
		this.value = value;
	}
}
