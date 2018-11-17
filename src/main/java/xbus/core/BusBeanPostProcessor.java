package com.lz.components.bus.core;

import java.lang.reflect.Method;

import org.apache.http.util.Asserts;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.lz.components.bus.annotation.BusEndpoint;
import com.lz.components.bus.annotation.BusRoot;
import com.lz.components.bus.code.BusExceptionCode;
import com.lz.components.bus.exception.BusException;
import com.lz.components.bus.stream.message.payload.BusPayload;

/**
 * 解析bus的注解
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-02 09:43
 */
@Component
public class BusBeanPostProcessor implements BeanPostProcessor {
	/**
	 * 必须在BusManager.start之前完成BusManager.addEndpointHandler,所以该操作放在前置处理器
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		BusRoot busRoot = bean.getClass().getAnnotation(BusRoot.class);
		if (busRoot != null) {
			String root = busRoot.value();
			try {
				Asserts.notEmpty(root, "the value of BusRoot of class " + bean.getClass().getName());
				Asserts.check(busRoot.value().startsWith("/"), "the path must startwith '/'");
				if (root.length() > 1 && root.endsWith("/"))
					root = root.substring(0, root.length() - 1);
				for (Method method : bean.getClass().getDeclaredMethods()) {
					BusEndpoint busEndpoint = method.getAnnotation(BusEndpoint.class);
					if (busEndpoint != null) {
						BusManager busManager = BusManagerFactory.get(busEndpoint.busName());
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
						String endpointPath = path.toString();
						// 检查方法参数及返回类型
						Class<?>[] paramType = method.getParameterTypes();
						Asserts.check(paramType != null && paramType.length == 2, "the method of endpoint only permit two parameters , the first is sourceTerminal which is String and the second is a BusPayload .");
						Asserts.check(paramType[0].isAssignableFrom(String.class), "the first parameter of method of endpoint only could be "+String.class);
						Asserts.check(paramType[1].isAssignableFrom(BusPayload.class), "the second parameter of method of endpoint only could be "+BusPayload.class);
						Class<?> returnType = method.getReturnType();
						Asserts.check(returnType == Void.class||BusPayload.class.isAssignableFrom(returnType), "the returnType of endpoint only could be either void or " + BusPayload.class);
						busManager.addEndpointHandler(endpointPath, (sourceTerminal,busPayload)->{
							try {
								if (busPayload.getContentType() != busEndpoint.contentType())
									throw new IllegalArgumentException("bus endpointHandler of path " + endpointPath + " needs "+busEndpoint.contentType()+",but received "+busPayload.getContentType());
								return (BusPayload) method.invoke(bean, sourceTerminal, busPayload);
							} catch (Exception e) {
								throw new BusException(BusExceptionCode.BUS_RECEIVE_FAILED, e);
							}
						});
					}
				}
			} catch (Exception e) {
				throw new BusException(BusExceptionCode.BUS_FAILED, e);
			}
		}
		return bean;
	}
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
