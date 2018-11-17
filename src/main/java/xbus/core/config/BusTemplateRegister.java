package com.lz.components.bus.core.config;

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

import com.lz.components.bus.AsyncBusTemplate;
import com.lz.components.bus.BusTemplate;
import com.lz.components.bus.stream.broker.BrokerConfigBean;
import com.lz.components.bus.stream.broker.rabbit.RabbitConfigBean;
import com.lz.components.bus.stream.broker.rabbit.RabbitMQStreamBroker;
import com.lz.components.bus.stream.broker.rocket.RocketConfigBean;
import com.lz.components.bus.stream.broker.rocket.RocketMQStreamBroker;
import com.lz.components.bus.stream.terminal.TerminalConfigBean;
import com.lz.components.bus.stream.terminal.file.FileConfigBean;
import com.lz.components.bus.stream.terminal.file.FileConfigurator;
import com.lz.components.bus.stream.terminal.zk.ZKConfigBean;
import com.lz.components.bus.stream.terminal.zk.ZKConfigurator;
/**
 * 
 * @author fuli
 * @date 2018年11月12日
 * @version 1.0.0
 */
@Component
public class BusTemplateRegister extends BusConfigurator implements BeanDefinitionRegistryPostProcessor{
	private BeanDefinitionRegistry registry;
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		this.registry=registry;
	}
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		for (String busName : busConfigs.keySet()) {
			//broker
			String brokerBeanName = busName + "StreamBroker";
			RootBeanDefinition streamBroker = new RootBeanDefinition();
			BrokerConfigBean brokerConfig = brokerConfigs.get(busName);
			if (brokerConfig instanceof RocketConfigBean) {
				streamBroker.setBeanClass(RocketMQStreamBroker.class);
				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
				constructorArgumentValues.addIndexedArgumentValue(0, brokerConfig);
				streamBroker.setConstructorArgumentValues(constructorArgumentValues);
			} else if (brokerConfig instanceof RabbitConfigBean) {
				streamBroker.setBeanClass(RabbitMQStreamBroker.class);
				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
				constructorArgumentValues.addIndexedArgumentValue(0, brokerConfig);
				constructorArgumentValues.addIndexedArgumentValue(1, new RuntimeBeanReference(((RabbitConfigBean)brokerConfig).getRabbitTemplateName()));
				streamBroker.setConstructorArgumentValues(constructorArgumentValues);
			} else {
				throw new IllegalStateException("no streamBroker found!");
			}
			streamBroker.setScope(BeanDefinition.SCOPE_SINGLETON);
			registry.registerBeanDefinition(brokerBeanName, streamBroker);
			//terminal
			String terminalConfiguratorBeanName=busName + "TerminalConfigurator";
			RootBeanDefinition terminalConfigurator = new RootBeanDefinition();
			TerminalConfigBean terminalConfig=terminalConfigs.get(busName);
			if(terminalConfig instanceof ZKConfigBean){
				terminalConfigurator.setBeanClass(ZKConfigurator.class);
				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
				constructorArgumentValues.addIndexedArgumentValue(0, terminalConfig);
				terminalConfigurator.setConstructorArgumentValues(constructorArgumentValues);
			}else if(terminalConfig instanceof FileConfigBean){
				terminalConfigurator.setBeanClass(FileConfigurator.class);
				ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
				constructorArgumentValues.addIndexedArgumentValue(0, terminalConfig);
				terminalConfigurator.setConstructorArgumentValues(constructorArgumentValues);
			}else{
				throw new IllegalStateException("no terminalConfigurator found!");
			}
			terminalConfigurator.setScope(BeanDefinition.SCOPE_SINGLETON);
			registry.registerBeanDefinition(terminalConfiguratorBeanName, terminalConfigurator);
			//busTemplate
			BusConfigBean busConfig=busConfigs.get(busName);
			BeanDefinitionBuilder busTemplateBuilder = BeanDefinitionBuilder.genericBeanDefinition(busConfig.isAsyncAble()?AsyncBusTemplate.class:BusTemplate.class);
			busTemplateBuilder.addConstructorArgValue(busName);
			busTemplateBuilder.addConstructorArgReference(brokerBeanName);
			busTemplateBuilder.addConstructorArgReference(terminalConfiguratorBeanName);
			busTemplateBuilder.addConstructorArgValue(busConfig);
			BeanDefinition busTemplateDefinition = busTemplateBuilder.getBeanDefinition();
			busTemplateDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
	        registry.registerBeanDefinition(busName+"BusTemplate",busTemplateDefinition);
		}
	}
}
