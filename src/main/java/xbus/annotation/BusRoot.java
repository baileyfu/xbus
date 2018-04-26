package xbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 总线根路径
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-02 16:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BusRoot {
	/**
	 * 对应path
	 * 
	 * @return
	 */
	public String value();
}
