package xbus.core.config;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

import xbus.core.DefaultAsyncBusTemplate;
import xbus.core.DefaultBusTemplate;
import xbus.stream.broker.BrokerConfigBean;
import xbus.stream.broker.rabbit.RabbitConfigBean;
import xbus.stream.broker.rabbit.RabbitMQStreamBroker;
import xbus.stream.broker.rocket.DefaultRocketStreamBroker;
import xbus.stream.broker.rocket.MultiendRocketStreamBroker;
import xbus.stream.broker.rocket.RocketConfigBean;
import xbus.stream.terminal.MiscTerminalConfigurator;
import xbus.stream.terminal.TerminalConfigBean;
import xbus.stream.terminal.VoidTerminalConfigurator;
/**
 * 
 * @author fuli
 * @date 2018年11月12日
 * @version 1.0.0
 */
@Component
public class BusTemplateRegister extends BusConfigurator implements BeanDefinitionRegistryPostProcessor{
	public static final String DEFAULT_BUS_NAME = "busTemplate";
	public static final String BUS_NAME_SUFFIX = "BusTemplate";
	private BeanDefinitionRegistry registry;
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		this.registry=registry;
	}
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		for (String busName : busConfigs.keySet()) {
			BusConfigBean busConfig=busConfigs.get(busName);
			//broker
			String brokerBeanName = busName + "StreamBroker";
			RootBeanDefinition streamBroker = new RootBeanDefinition();
			BrokerConfigBean brokerConfig = brokerConfigs.get(busName);
			if (brokerConfig instanceof RocketConfigBean) {
				streamBroker.setBeanClass(DefaultRocketStreamBroker.class);
				// 启用Rocket多端总线
				if (((RocketConfigBean) brokerConfig).isMultiend()) {
					streamBroker.setBeanClass(MultiendRocketStreamBroker.class);
				}
				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
				constructorArgumentValues.addIndexedArgumentValue(0, busConfig);
				constructorArgumentValues.addIndexedArgumentValue(1, brokerConfig);
				streamBroker.setConstructorArgumentValues(constructorArgumentValues);
			} else if (brokerConfig instanceof RabbitConfigBean) {
				streamBroker.setBeanClass(RabbitMQStreamBroker.class);
				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
				constructorArgumentValues.addIndexedArgumentValue(0, busConfig);
				constructorArgumentValues.addIndexedArgumentValue(1, brokerConfig);
				constructorArgumentValues.addIndexedArgumentValue(2, new RuntimeBeanReference(((RabbitConfigBean)brokerConfig).getRabbitTemplateName()));
				streamBroker.setConstructorArgumentValues(constructorArgumentValues);
			} else {
				throw new IllegalStateException("no streamBroker found!");
			}
			streamBroker.setScope(BeanDefinition.SCOPE_SINGLETON);
			registry.registerBeanDefinition(brokerBeanName, streamBroker);
			//terminal
			String terminalConfiguratorBeanName=busName + "TerminalConfigurator";
			RootBeanDefinition terminalConfigurator = new RootBeanDefinition();
//			TerminalConfigBean terminalConfig=terminalConfigs.get(busName);
//			if(terminalConfig instanceof ZKConfigBean){
//				terminalConfigurator.setBeanClass(ZKConfigurator.class);
//				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
//				constructorArgumentValues.addIndexedArgumentValue(0, terminalConfig);
//				terminalConfigurator.setConstructorArgumentValues(constructorArgumentValues);
//			}else if(terminalConfig instanceof FileConfigBean){
//				terminalConfigurator.setBeanClass(FileConfigurator.class);
//				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
//				constructorArgumentValues.addIndexedArgumentValue(0, terminalConfig);
//				terminalConfigurator.setConstructorArgumentValues(constructorArgumentValues);
//			}else if(terminalConfig instanceof EKConfigBean) {
//				terminalConfigurator.setBeanClass(EurekaConfigurator.class);
//				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
//				constructorArgumentValues.addIndexedArgumentValue(0, terminalConfig);
//				terminalConfigurator.setConstructorArgumentValues(constructorArgumentValues);
//			}else{
//				throw new IllegalStateException("no terminalConfigurator found!");
//			}
			Map<String, TerminalConfigBean> terminalConfigMap = terminalConfigs.get(busName);
			if(terminalConfigMap == null || terminalConfigMap.size() == 0) {
				if (busConfig.isPostUndefined()) {
					terminalConfigurator.setBeanClass(VoidTerminalConfigurator.class);
				} else {
					throw new IllegalStateException("no terminalConfigurator found!");
				}
			}else {
				terminalConfigurator.setBeanClass(MiscTerminalConfigurator.class);
				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
				constructorArgumentValues.addIndexedArgumentValue(0, terminalConfigMap);
				terminalConfigurator.setConstructorArgumentValues(constructorArgumentValues);
			}
			terminalConfigurator.setScope(BeanDefinition.SCOPE_SINGLETON);
			registry.registerBeanDefinition(terminalConfiguratorBeanName, terminalConfigurator);
			//busTemplate
			BeanDefinitionBuilder busTemplateBuilder = BeanDefinitionBuilder.genericBeanDefinition(busConfig.isAsyncAble()?DefaultAsyncBusTemplate.class:DefaultBusTemplate.class);
			busTemplateBuilder.addConstructorArgValue(busName);
			busTemplateBuilder.addConstructorArgReference(brokerBeanName);
			busTemplateBuilder.addConstructorArgReference(terminalConfiguratorBeanName);
			busTemplateBuilder.addConstructorArgValue(busConfig);
//			busTemplateBuilder.setInitMethodName("initialize");
			busTemplateBuilder.setDestroyMethodName("destroy");
			BeanDefinition busTemplateDefinition = busTemplateBuilder.getBeanDefinition();
			busTemplateDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
	        registry.registerBeanDefinition(busName+BUS_NAME_SUFFIX,busTemplateDefinition);
		}
	}
}
