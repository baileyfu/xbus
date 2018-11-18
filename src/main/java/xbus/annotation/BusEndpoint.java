package xbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import xbus.core.BusManagerFactory;
import xbus.em.MessageContentType;

/**
 * 总线子路径
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-02 16:40
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BusEndpoint {
	public String busName() default BusManagerFactory.DEFAULT_CACHE_MANAGER_NAME;

	/**
	 * 对应path
	 * 
	 * @return
	 */
	public String value();

	/**
	 * 消息类型<br/>
	 * 默认为JSON
	 * 
	 * @return
	 */
	public MessageContentType contentType() default MessageContentType.JSON;
}
