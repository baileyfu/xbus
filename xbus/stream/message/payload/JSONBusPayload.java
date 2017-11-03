package xbus.stream.message.payload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import xbus.em.MessageContentType;

/**
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

	public JSONBusPayload(JSONObject value) {
		contentType = MessageContentType.JSON;
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		if (!(value instanceof JSONObject))
			throw new IllegalArgumentException("JSONBusPayload's value only the accept com.alibaba.fastjson.JSONObject");
		this.value = (JSONObject) value;
	}
	@Override
	public Object getValue() {
		return value;
	}
	@Override
	public byte[] toBytes() {
		return value == null ? null : value.toJSONString().getBytes();
	}
}
