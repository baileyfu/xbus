package xbus.core.config;

/**
 * 总段配置总线名称转换
 * </p>
 * 适用于BusEndpoint配置了别名busName需要转换时实现
 * 
 * @author fuli
 * @date 2019年5月17日
 * @version 1.0.0
 */
public interface EndpointBusNameTranslator {
	/**
	 * 总线别名转换
	 * 
	 * @param busNameAlias 别名
	 * @return 真实总线名
	 */
	String transform(String busNameAlias);

}
