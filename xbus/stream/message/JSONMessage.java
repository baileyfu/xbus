package xbus.stream.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import xbus.stream.MessageContentType;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public class JSONMessage extends Message {
	private JSONObject payLoad;

	public JSONMessage() {
		setContentType(MessageContentType.JSON);
	}

	public JSONMessage(byte[] bytes) {
		super(bytes);
		setContentType(MessageContentType.JSON);
	}

	public JSONMessage(JSONObject payLoad) {
		super(payLoad);
		setContentType(MessageContentType.JSON);
	}

	@Override
	public Object getPayLoad() {
		return payLoad;
	}

	@Override
	public void setPayLoad(Object payLoad) {
		if (!(payLoad instanceof JSONObject))
			throw new IllegalArgumentException("JSONMessage's payload only the accept com.alibaba.fastjson.JSONObject");
		this.payLoad = (JSONObject) payLoad;
	}

	@Override
	public byte[] toBytes() {
		return payLoad == null ? null : payLoad.toJSONString().getBytes();
	}

	@Override
	protected JSONObject fromBytes(byte[] bytes) {
		return bytes == null ? null : JSON.parseObject(new String(bytes));
	}
}
