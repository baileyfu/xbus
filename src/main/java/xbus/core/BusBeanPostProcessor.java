package xbus.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import xbus.BusLoggerHolder;
import xbus.annotation.BusEndpoint;
import xbus.annotation.BusRoot;
import xbus.bean.EndpointBean;
import xbus.constants.Keywords;
import xbus.core.config.EndpointBusNameTranslator;
import xbus.core.config.ReceiptHandlerRegister;
import xbus.exception.BusException;
import xbus.exception.BusExceptionCode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

/**
 * 解析bus的注解
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-02 09:43
 */
@Component
public class BusBeanPostProcessor implements BeanPostProcessor,ApplicationListener<ContextRefreshedEvent>, BusLoggerHolder {
	Map<String,Map<EndpointBean,BiFunction<String, xbus.stream.message.payload.BusPayload, xbus.stream.message.payload.BusPayload>>> endpointHandlerMap=new HashMap<>();
	Collection<ReceiptHandlerRegister> receiptHandlerRegisters = new ArrayList<>();
	Collection<xbus.core.AbstractBusAccessor> busAccessors = new ArrayList<>();
	Collection<EndpointBusNameTranslator> busNameTranslators = new ArrayList<>();
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof ReceiptHandlerRegister) {
			receiptHandlerRegisters.add((ReceiptHandlerRegister)bean);
			return bean;
		} else if (bean instanceof EndpointBusNameTranslator) {
			busNameTranslators.add((EndpointBusNameTranslator)bean);
			return bean;
		}
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
						String busName = busEndpoint.busName();
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
						Keywords.checkPath(endpointPath);
						LOGGER.info("[Bus-{}] mapped endpoint : {} , contentType[{}]",busName,endpointPath,busEndpoint.contentType());
						// 检查方法参数及返回类型
						Class<?>[] paramType = method.getParameterTypes();
						Asserts.check(paramType != null && paramType.length == 2, "the method of endpoint only permit two parameters , the first is sourceTerminal which is String and the second is a BusPayload .");
						Asserts.check(String.class.isAssignableFrom(paramType[0]), "the first parameter of method of endpoint only could be "+String.class);
						Asserts.check(xbus.stream.message.payload.BusPayload.class.isAssignableFrom(paramType[1]), "the second parameter of method of endpoint only could be "+ xbus.stream.message.payload.BusPayload.class);
						Class<?> returnType = method.getReturnType();
						Asserts.check(returnType == Void.class|| xbus.stream.message.payload.BusPayload.class.isAssignableFrom(returnType), "the returnType of endpoint only could be either void or " + xbus.stream.message.payload.BusPayload.class);
						//收集当前服务定义的终端处理器
						Map<EndpointBean, BiFunction<String, xbus.stream.message.payload.BusPayload, xbus.stream.message.payload.BusPayload>> endpointHandler=endpointHandlerMap.get(busName);
						if(endpointHandler==null) {
							endpointHandler=new HashMap<>();
							endpointHandlerMap.put(busName, endpointHandler);
						}
						EndpointBean endpointBean = new EndpointBean();
						endpointBean.setFullPath(endpointPath);
						endpointBean.setNodeOriented(busEndpoint.nodeOriented());
						endpointHandler.put(endpointBean, (sourceTerminal,busPayload)->{
							try {
								if (busPayload.getContentType() != busEndpoint.contentType())
									throw new IllegalArgumentException("xbus endpointHandler of path " + endpointPath + " needs "+busEndpoint.contentType()+",but received "+busPayload.getContentType());
								return (xbus.stream.message.payload.BusPayload) method.invoke(bean, sourceTerminal, busPayload);
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
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof AbstractBusAccessor) {
			busAccessors.add((AbstractBusAccessor)bean);
		}
		return bean;
	}

	boolean initialized = false;
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(!initialized) {
			initialized = true;
			if (busNameTranslators.size() > 0) {
				Map<String, Map<EndpointBean, BiFunction<String, xbus.stream.message.payload.BusPayload, xbus.stream.message.payload.BusPayload>>> transformed = new HashMap<>();
				Collection<String> beTransformed = new ArrayList<>();
				for(EndpointBusNameTranslator busNameTranslator:busNameTranslators) {
					for(Entry<String, Map<EndpointBean, BiFunction<String, xbus.stream.message.payload.BusPayload, xbus.stream.message.payload.BusPayload>>> entry:endpointHandlerMap.entrySet()) {
						String busName = entry.getKey();
						String transformedBusName = busNameTranslator.transform(busName);
						//null或空则忽略
						if(!StringUtils.isEmpty(transformedBusName)) {
							Asserts.check(!beTransformed.contains(busName), "two EndpointBusNameTranslator be found for one endpointBusName '"+busName+"'");
							Map<EndpointBean, BiFunction<String, xbus.stream.message.payload.BusPayload, xbus.stream.message.payload.BusPayload>> values=transformed.get(transformedBusName);
							if (values == null) {
								transformed.put(transformedBusName, entry.getValue());
							} else {
								values.putAll(entry.getValue());
							}
							beTransformed.add(busName);
						}
					}
				}
				for (String busName : transformed.keySet()) {
					transformed.get(busName).putAll(endpointHandlerMap.get(busName));
				}
				for (String busName : beTransformed) {
					endpointHandlerMap.remove(busName);
				}
				endpointHandlerMap.putAll(transformed);
			}
			for (AbstractBusAccessor busAccessor : busAccessors) {
				Map<EndpointBean, BiFunction<String, xbus.stream.message.payload.BusPayload, xbus.stream.message.payload.BusPayload>> endpointHandler=endpointHandlerMap.get(busAccessor.busManager.getName());
				if(endpointHandler!=null) {
					for(EndpointBean endpointBean:endpointHandler.keySet()) {
						busAccessor.busManager.addEndpointHandler(endpointBean, endpointHandler.get(endpointBean));
					}
				}
				busAccessor.busManager.addReceiptHandler(receiptHandlerRegisters);
				busAccessor.initialize();
			}
		}
	}
}
