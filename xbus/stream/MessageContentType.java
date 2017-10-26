package xbus.stream;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-18 13:58
 */
public class MessageContentType {
	public static final String TEXT = "text/plain";
	public static final String JSON = "application/json";
	/**
	 * 因text/html默认使用us-ascii编码，仅支持application/xml；XML内容需有encoding
	 */
	public static final String XML = "application/xml";
	public static final String BYTES = "application/octet-stream";
	public static final String SERIALIZED_OBJECT = "application/x-java-serialized-object";
}
