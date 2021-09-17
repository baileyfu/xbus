package xbus.annotation;

import xbus.constants.MessageContentType;
import xbus.core.BusManagerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


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
	public String busName() default BusManagerFactory.DEFAULT_BUS_NAME;
	/**
	 * 对应path
	 * 
	 * @return
	 */
	public String value();
	/**
	 * 是否开启面向节点
	 * @return
	 */
	public boolean nodeOriented()default false;
	/**
	 * 消息类型<br/>
	 * 默认为JSON
	 * 
	 * @return
	 */
	public MessageContentType contentType() default MessageContentType.JSON;
}
