package xbus.constants;

/**
 * 消息头信息参数定义
 * 
 * @author fuli
 * @date 2018年10月24日
 * @version 1.0.0
 */
public enum HeaderParams {
	/** 请求路径 */
	XBUS_PATH, 
	/** 回执消息的原始请求路径(原始消息无此header) */
	XBUS_SOURCE_PATH, 
	/** 发起请求的终端 */
	XBUS_SOURCE_TERMINAL,
	/** 消息类型 */
	XBUS_MESSAGE_TYPE,
	/** 消息内容类型 */
	XBUS_MESSAGE_CONTENT_TYPE,
	/** 是否需要回执(回执消息无此header) */
	XBUS_REQUIRE_RECEIPT,
	/** 附加原始消息 */
	XBUS_ORIGINALS;
}
