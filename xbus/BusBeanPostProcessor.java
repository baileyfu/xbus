package xbus;

import java.lang.reflect.Method;

import org.apache.http.util.Asserts;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import xbus.BusManager;
import xbus.annotation.BusEndpoint;
import xbus.annotation.BusRoot;
import xbus.stream.message.OriginalBusMessage;
import xbus.stream.message.payload.BusPayload;

/**
 * 解析bus的注解
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-02 09:43
 */
public class BusBeanPostProcessor implements BeanPostProcessor {
	/**
	 * 必须在BusManager.start之前完成BusManager.addEndpointHandler,所以该操作放在前置处理器
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		BusRoot busRoot = bean.getClass().getAnnotation(BusRoot.class);
		if (busRoot != null) {
			String root = busRoot.value();
			Asserts.notEmpty(root, "the value of BusRoot of class " + bean.getClass().getName());
			Asserts.check(busRoot.value().startsWith("/"), "the path must startwith '/'");
			if (root.length() > 1 && root.endsWith("/"))
				root = root.substring(0, root.length() - 1);
			BusManager busManager = BusManager.getInstance();
			for (Method method : bean.getClass().getDeclaredMethods()) {
				BusEndpoint busEndpoint = method.getAnnotation(BusEndpoint.class);
				if (busEndpoint != null) {
					//检查路径
					String endpoint = busEndpoint.value();
					Asserts.notEmpty(endpoint, "the value of BusEndpoint of method " + method.toString());
					Asserts.check(!endpoint.equals("/"), "the value of BusEndpoint could not be '/'");
					StringBuilder path = new StringBuilder(root);
					if (endpoint.startsWith("/"))
						endpoint = endpoint.substring(1, endpoint.length());
					if (endpoint.endsWith("/"))
						endpoint = endpoint.substring(0, endpoint.length() - 1);
					path.append("/").append(endpoint);
					// 检查方法参数及返回类型
					Class<?>[] paramType = method.getParameterTypes();
					Asserts.check(paramType != null && paramType.length == 1, "the method of endpoint only permit one parameter");
					Asserts.check(paramType[0].isAssignableFrom(OriginalBusMessage.class), "the parameter of method of endpoint only could be "+OriginalBusMessage.class);
					Class<?> returnType = method.getReturnType();
					Asserts.check(returnType == Void.class, "the returnType of endpoint only could be either void or " + BusPayload.class);
					
					busManager.addEndpointHandler(path.toString(), (originalBusMessage)->{
						try {
							if (originalBusMessage.getContentType() != busEndpoint.contentType())
								throw new IllegalArgumentException("bus endpointHandler of path " + path.toString() + " needs "+busEndpoint.contentType()+",but received "+originalBusMessage.getContentType());
							return (BusPayload)method.invoke(bean, originalBusMessage);
						} catch (Exception e) {
							throw new RuntimeException("bus endpointHandler of path " + path.toString() + " handle message error!", e);
						}
					});
				}
			}
		}
		return bean;
	}
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
