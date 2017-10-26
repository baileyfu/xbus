package xbus.stream.message;

import java.util.Map;

import commons.lang.StringUtils;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public abstract class Message {
	private static final String SOURCE_SYSTEM = "XBUS_SOURCE_SYSTEM";
	private String sourceSystem;
	private String contentType;
	private Map<String, Object> header;

	public Message() {
	}

	public Message(byte[] bytes) {
		setPayLoad(fromBytes(bytes));
	}

	public Message(Object payLoad) {
		setPayLoad(payLoad);
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public void setSourceSystem(Object sourceSystem) {
		this.sourceSystem = sourceSystem == null ? null : sourceSystem.toString();
	}

	protected void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public Map<String, Object> getHeader() {
		header.put(SOURCE_SYSTEM, this.sourceSystem);
		return header;
	}

	public void setHeader(Map<String, Object> header) {
		this.header = header;
		Object temp = header.get(SOURCE_SYSTEM);
		if (temp != null)
			this.sourceSystem = StringUtils.defaultString(temp);
	}

	public abstract Object getPayLoad();

	public abstract void setPayLoad(Object payLoad);

	public abstract byte[] toBytes();

	protected abstract Object fromBytes(byte[] bytes);
}
