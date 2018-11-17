package com.lz.components.bus.core.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.base.Optional;
import com.lz.components.bus.stream.broker.BrokerConfigBean;
import com.lz.components.bus.stream.broker.rabbit.RabbitConfigBean;
import com.lz.components.bus.stream.broker.rocket.RocketConfigBean;
import com.lz.components.bus.stream.terminal.TerminalConfigBean;
import com.lz.components.bus.stream.terminal.file.FileConfigBean;
import com.lz.components.bus.stream.terminal.file.FileConfigBean.NodeAddress;
import com.lz.components.bus.stream.terminal.file.FileConfigBean.ServerInfo;
import com.lz.components.bus.stream.terminal.zk.ZKConfigBean;
import com.lz.components.common.log.holder.CommonLoggerHolder;

/**
 * 总线装配
 * 
 * @author fuli
 * @date 2018年11月12日
 * @version 1.0.0
 */
public class BusConfigurator extends YamlPropertiesFactoryBean implements EnvironmentAware,CommonLoggerHolder{
	private String xbusPrefix;
	protected Map<String,BusConfigBean> busConfigs;
	protected Map<String,BrokerConfigBean> brokerConfigs;
	protected Map<String,TerminalConfigBean> terminalConfigs;

	public BusConfigurator() {
		xbusPrefix = "components.bus";
		brokerConfigs = new HashMap<>();
		terminalConfigs = new HashMap<>();
		busConfigs = new HashMap<>();
	}
	private void parseProps(Properties properties){
		xbusPrefix = Optional.fromNullable(environment.getProperty("xbusPrefix")).or(xbusPrefix);
		//总开关
		Boolean enable=Boolean.valueOf(Optional.fromNullable(properties.get(xbusPrefix+".enable")).or(BusConfigBean.DEFAULT_ENABLE).toString());
		for(Object key:properties.keySet()){
			String propName = key.toString();
			if (propName.startsWith(xbusPrefix)) {
				String busName = StringUtils.substringBefore(propName.substring(15), ".");
				String prefixName = xbusPrefix +"."+ busName;
				BusConfigBean busConfig=new BusConfigBean();
				busConfig.setAccessInterval(Long.valueOf(Optional.fromNullable(properties.get(prefixName+".accessInterval")).or(BusConfigBean.DEFAULT_ACCESS_INTERVAL).toString()));
				busConfig.setEnable(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName+".enable")).or(enable).toString()));
				busConfig.setAsyncAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName+".asyncAble")).or(BusConfigBean.DEFAULT_ASYNCABLE).toString()));
				busConfigs.put(busName, busConfig);
				/** broker */
				if(propName.startsWith(prefixName + ".broker")){
					if (brokerConfigs.get(busName) == null) {
						if(propName.startsWith(prefixName + ".broker.rocket")){//rocket
							RocketConfigBean rocketConfig = new RocketConfigBean();
							rocketConfig.setDurable(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.durable")).or(BrokerConfigBean.DEFAULT_DURABLE).toString()));
							rocketConfig.setConsumeRetryAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.consumeRetryAble")).or(BrokerConfigBean.DEFAULT_CONSUME_RETRYABLE).toString()));
							rocketConfig.setConsumeRetryCount(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.consumeRetryCount")).or(BrokerConfigBean.DEFAULT_CONSUME_RETRY_COUNT).toString()));
							rocketConfig.setConsumerTimeoutMillis(Long.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.consumerTimeoutMillis")).or(BrokerConfigBean.DEFAULT_CONSUME_TIMEOUT_MILLIS).toString()));
							rocketConfig.setProduceRetryAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.produceRetryAble")).or(BrokerConfigBean.DEFAULT_PRODUCE_RETRYABLE).toString()));
							rocketConfig.setProduceRetryCount(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.produceRetryCount")).or(BrokerConfigBean.DEFAULT_PRODUCE_RETRY_COUNT).toString()));
							rocketConfig.setProducerTimeoutMillis(Long.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.producerTimeoutMillis")).or(BrokerConfigBean.DEFAULT_PRODUCE_TIMEOUT_MILLIS).toString()));
							rocketConfig.setNameSrvAddr(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.nameSrvAddr")).or(StringUtils.EMPTY).toString());
							Asserts.notEmpty(rocketConfig.getNameSrvAddr(), prefixName+".broker.rocket.nameSrvAddr");
							rocketConfig.setTopicQueueNums(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.topicQueueNums")).or(RocketConfigBean.ROCKET_DEFAULT_TOPIC_QUEUE_NUMS).toString()));
							rocketConfig.setPullBatchSize(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.pullBatchSize")).or(RocketConfigBean.ROCKET_DEFAULT_PULL_BATCH_SIZE).toString()));
							rocketConfig.setConsumeBatchSize(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.consumeBatchSize")).or(RocketConfigBean.ROCKET_DEFAULT_CONSUME_BATCH_SIZE).toString()));
							brokerConfigs.put(busName, rocketConfig);
						}else if(propName.startsWith(prefixName + ".broker.rabbit")){//rabbit
							RabbitConfigBean rabbitConfig = new RabbitConfigBean();
							rabbitConfig.setDurable(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.durable")).or(BrokerConfigBean.DEFAULT_DURABLE).toString()));
							rabbitConfig.setConsumeRetryAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.consumeRetryAble")).or(BrokerConfigBean.DEFAULT_CONSUME_RETRYABLE).toString()));
							rabbitConfig.setConsumeRetryCount(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.consumeRetryCount")).or(BrokerConfigBean.DEFAULT_CONSUME_RETRY_COUNT).toString()));
							rabbitConfig.setConsumerTimeoutMillis(Long.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.consumerTimeoutMillis")).or(BrokerConfigBean.DEFAULT_CONSUME_TIMEOUT_MILLIS).toString()));
							rabbitConfig.setProduceRetryAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.produceRetryAble")).or(BrokerConfigBean.DEFAULT_PRODUCE_RETRYABLE).toString()));
							rabbitConfig.setProduceRetryCount(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.produceRetryCount")).or(BrokerConfigBean.DEFAULT_PRODUCE_RETRY_COUNT).toString()));
							rabbitConfig.setProducerTimeoutMillis(Long.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.producerTimeoutMillis")).or(BrokerConfigBean.DEFAULT_PRODUCE_TIMEOUT_MILLIS).toString()));
							rabbitConfig.setRabbitTemplateName(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.rabbitTemplateName")).or(StringUtils.EMPTY).toString());
							Asserts.notEmpty(rabbitConfig.getRabbitTemplateName(), prefixName + ".broker.rabbit.rabbitTemplateName");
							brokerConfigs.put(busName, rabbitConfig);
						}
					}
					continue;
				}
				/** terminal */
				if(propName.startsWith(prefixName + ".terminal")){
					if (terminalConfigs.get(busName) == null) {
						String serverName = Optional.fromNullable(properties.get(prefixName + ".terminal.serverName")).or(StringUtils.EMPTY).toString();
						Asserts.notEmpty(serverName, prefixName + ".terminal.serverName");
						String ip = Optional.fromNullable(properties.get(prefixName + ".terminal.ip")).or(StringUtils.EMPTY).toString();
						int port=Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".terminal.port")).or("0").toString());
						if(propName.startsWith(prefixName + ".terminal.file")){//file
							FileConfigBean fileConfig = new FileConfigBean();
							fileConfig.setServerName(serverName);
							fileConfig.setIp(ip);
							fileConfig.setPort(port);
							List<ServerInfo> servers = new ArrayList<>();
							ServerInfo serverInfo;
							int i=0;
							do{
								serverInfo=null;
								Object sName=properties.get(prefixName + ".terminal.file.servers[" + i + "].serverName");
								if (sName!=null) {
									serverInfo = new ServerInfo();
									serverInfo.setServerName(sName.toString());
									List<NodeAddress> nodeInfo=new ArrayList<>();
									int j = 0;
									NodeAddress nodeAddress;
									do {
										nodeAddress=null;
										Object nIp=properties.get(prefixName + ".terminal.file.servers[" + i + "].nodeInfo["+j+"].ip");
										Object nPort=properties.get(prefixName + ".terminal.file.servers[" + i + "].nodeInfo["+j+"].port");
										if (nIp != null || nPort != null) {
											nodeAddress=new NodeAddress();
											nodeAddress.setIp(nIp.toString());
											nodeAddress.setPort(nPort == null ? 0 : Integer.valueOf(nPort.toString()));
											nodeInfo.add(nodeAddress);
										}
										j++;
									}while(nodeAddress!=null);
									
									serverInfo.setNodeInfo(nodeInfo);
									servers.add(serverInfo);
								}
								i++;
							} while (serverInfo != null);
							fileConfig.setServers(servers);
							terminalConfigs.put(busName, fileConfig);
						}else if(propName.startsWith(prefixName + ".terminal.zk")){//zookeeper
							ZKConfigBean zkConfig = new ZKConfigBean();
							zkConfig.setServerName(serverName);
							zkConfig.setIp(ip);
							zkConfig.setPort(port);
							zkConfig.setRootPath(Optional.fromNullable(properties.get(prefixName + ".terminal.zk.rootPath")).or(StringUtils.EMPTY).toString());
							Asserts.notEmpty(zkConfig.getRootPath(), prefixName+".terminal.zk.rootPath");
							zkConfig.setServers(Optional.fromNullable(properties.get(prefixName + ".terminal.zk.servers")).or(StringUtils.EMPTY).toString());
							Asserts.notEmpty(zkConfig.getServers(), prefixName+".terminal.zk.servers");
							terminalConfigs.put(busName, zkConfig);
						}
					}
					continue;
				}
			}
		}
	}
	@Override
	public void afterPropertiesSet(){
		Resource resource = new ClassPathResource("/bus.yml");
		if (!resource.exists()) {
			resource = new ClassPathResource("/config/bus.yml");
		}
		if (!resource.exists()) {
			resource = new ClassPathResource("/config/application.yml");
		}
		if (resource.exists()) {
			setResources(resource);
		}
		super.afterPropertiesSet();
		Properties sourceProps = createProperties();
		Properties properties=new Properties();
		for (Object propName : sourceProps.keySet()) {
			properties.put(propName, environment.getProperty(propName.toString()));
		}
		parseProps(properties);
	}
	Environment environment;
	@Override
	public void setEnvironment(Environment environment) {
		this.environment=environment;
	}
}
