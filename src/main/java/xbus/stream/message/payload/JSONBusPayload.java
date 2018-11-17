package com.lz.components.bus.stream.message.payload;

import org.apache.http.util.Asserts;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lz.components.bus.em.MessageContentType;

/**
 * 基于fastjson的实现
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public class JSONBusPayload extends BusPayload {
	private JSONObject value;

	public JSONBusPayload() {
		contentType = MessageContentType.JSON;
	}

	public JSONBusPayload(byte[] bytes) {
		contentType = MessageContentType.JSON;
		value = bytes == null ? null : JSON.parseObject(new String(bytes));
	}
	public JSONBusPayload(String value) {
		contentType = MessageContentType.JSON;
		this.value = JSONObject.parseObject(value);
	}
	public JSONBusPayload(JSONObject value) {
		contentType = MessageContentType.JSON;
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		Asserts.notNull(value, "value");
		if (value instanceof String) {
			this.value = JSONObject.parseObject(value.toString());
		} else if (value instanceof JSONObject) {
			this.value = (JSONObject) value;
		} else {
			throw new IllegalArgumentException("JSONBusPayload's value only accept com.alibaba.fastjson.JSONObject");
		}
	}
	@Override
	public JSONObject getValue() {
		return value;
	}
	@Override
	public byte[] toBytes() {
		return value == null ? null : value.toJSONString().getBytes();
	}
}
