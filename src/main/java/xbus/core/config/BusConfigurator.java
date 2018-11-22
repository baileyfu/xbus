package xbus.core.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import xbus.BusLoggerHolder;
import xbus.stream.broker.BrokerConfigBean;
import xbus.stream.broker.rabbit.RabbitConfigBean;
import xbus.stream.broker.rocket.RocketConfigBean;
import xbus.stream.terminal.TerminalConfigBean;
import xbus.stream.terminal.file.FileConfigBean;
import xbus.stream.terminal.file.FileConfigBean.NodeAddress;
import xbus.stream.terminal.file.FileConfigBean.ServerInfo;
import xbus.stream.terminal.zk.ZKConfigBean;

/**
 * 总线装配
 * 
 * @author fuli
 * @date 2018年11月12日
 * @version 1.0.0
 */
public class BusConfigurator extends YamlPropertiesFactoryBean implements EnvironmentAware,BusLoggerHolder{
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
		xbusPrefix = Optional.ofNullable(environment.getProperty("xbusPrefix")).orElse(xbusPrefix);
		//总开关
		Boolean enable=Boolean.valueOf(Optional.ofNullable(properties.get(xbusPrefix+".enable")).orElse(BusConfigBean.DEFAULT_ENABLE).toString());
		for(Object key:properties.keySet()){
			String propName = key.toString();
			if (propName.startsWith(xbusPrefix)) {
				String busName = StringUtils.substringBefore(propName.substring(xbusPrefix.length()+1), ".");
				String prefixName = xbusPrefix +"."+ busName;
				BusConfigBean busConfig=new BusConfigBean();
				busConfig.setAccessInterval(Long.valueOf(Optional.ofNullable(properties.get(prefixName+".accessInterval")).orElse(BusConfigBean.DEFAULT_ACCESS_INTERVAL).toString()));
				busConfig.setEnable(Boolean.valueOf(Optional.ofNullable(properties.get(prefixName+".enable")).orElse(enable).toString()));
				busConfig.setAsyncAble(Boolean.valueOf(Optional.ofNullable(properties.get(prefixName+".asyncAble")).orElse(BusConfigBean.DEFAULT_ASYNCABLE).toString()));
				busConfigs.put(busName, busConfig);
				/** broker */
				if(propName.startsWith(prefixName + ".broker")){
					if (brokerConfigs.get(busName) == null) {
						if(propName.startsWith(prefixName + ".broker.rocket")){//rocket
							RocketConfigBean rocketConfig = new RocketConfigBean();
							rocketConfig.setDurable(Boolean.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.durable")).orElse(BrokerConfigBean.DEFAULT_DURABLE).toString()));
							rocketConfig.setConsumeRetryAble(Boolean.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.consumeRetryAble")).orElse(BrokerConfigBean.DEFAULT_CONSUME_RETRYABLE).toString()));
							rocketConfig.setConsumeRetryCount(Integer.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.consumeRetryCount")).orElse(BrokerConfigBean.DEFAULT_CONSUME_RETRY_COUNT).toString()));
							rocketConfig.setConsumerTimeoutMillis(Long.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.consumerTimeoutMillis")).orElse(BrokerConfigBean.DEFAULT_CONSUME_TIMEOUT_MILLIS).toString()));
							rocketConfig.setProduceRetryAble(Boolean.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.produceRetryAble")).orElse(BrokerConfigBean.DEFAULT_PRODUCE_RETRYABLE).toString()));
							rocketConfig.setProduceRetryCount(Integer.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.produceRetryCount")).orElse(BrokerConfigBean.DEFAULT_PRODUCE_RETRY_COUNT).toString()));
							rocketConfig.setProducerTimeoutMillis(Long.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.producerTimeoutMillis")).orElse(BrokerConfigBean.DEFAULT_PRODUCE_TIMEOUT_MILLIS).toString()));
							rocketConfig.setNameSrvAddr(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.nameSrvAddr")).orElse(StringUtils.EMPTY).toString());
							Asserts.notEmpty(rocketConfig.getNameSrvAddr(), prefixName+".broker.rocket.nameSrvAddr");
							rocketConfig.setTopicQueueNums(Integer.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.topicQueueNums")).orElse(RocketConfigBean.ROCKET_DEFAULT_TOPIC_QUEUE_NUMS).toString()));
							rocketConfig.setPullBatchSize(Integer.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.pullBatchSize")).orElse(RocketConfigBean.ROCKET_DEFAULT_PULL_BATCH_SIZE).toString()));
							rocketConfig.setConsumeBatchSize(Integer.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rocket.consumeBatchSize")).orElse(RocketConfigBean.ROCKET_DEFAULT_CONSUME_BATCH_SIZE).toString()));
							brokerConfigs.put(busName, rocketConfig);
						}else if(propName.startsWith(prefixName + ".broker.rabbit")){//rabbit
							RabbitConfigBean rabbitConfig = new RabbitConfigBean();
							rabbitConfig.setDurable(Boolean.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rabbit.durable")).orElse(BrokerConfigBean.DEFAULT_DURABLE).toString()));
							rabbitConfig.setConsumeRetryAble(Boolean.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rabbit.consumeRetryAble")).orElse(BrokerConfigBean.DEFAULT_CONSUME_RETRYABLE).toString()));
							rabbitConfig.setConsumeRetryCount(Integer.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rabbit.consumeRetryCount")).orElse(BrokerConfigBean.DEFAULT_CONSUME_RETRY_COUNT).toString()));
							rabbitConfig.setConsumerTimeoutMillis(Long.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rabbit.consumerTimeoutMillis")).orElse(BrokerConfigBean.DEFAULT_CONSUME_TIMEOUT_MILLIS).toString()));
							rabbitConfig.setProduceRetryAble(Boolean.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rabbit.produceRetryAble")).orElse(BrokerConfigBean.DEFAULT_PRODUCE_RETRYABLE).toString()));
							rabbitConfig.setProduceRetryCount(Integer.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rabbit.produceRetryCount")).orElse(BrokerConfigBean.DEFAULT_PRODUCE_RETRY_COUNT).toString()));
							rabbitConfig.setProducerTimeoutMillis(Long.valueOf(Optional.ofNullable(properties.get(prefixName + ".broker.rabbit.producerTimeoutMillis")).orElse(BrokerConfigBean.DEFAULT_PRODUCE_TIMEOUT_MILLIS).toString()));
							rabbitConfig.setRabbitTemplateName(Optional.ofNullable(properties.get(prefixName + ".broker.rabbit.rabbitTemplateName")).orElse(StringUtils.EMPTY).toString());
							Asserts.notEmpty(rabbitConfig.getRabbitTemplateName(), prefixName + ".broker.rabbit.rabbitTemplateName");
							brokerConfigs.put(busName, rabbitConfig);
						}
					}
					continue;
				}
				/** terminal */
				if(propName.startsWith(prefixName + ".terminal")){
					if (terminalConfigs.get(busName) == null) {
						String serverName = Optional.ofNullable(properties.get(prefixName + ".terminal.serverName")).orElse(StringUtils.EMPTY).toString();
						Asserts.notEmpty(serverName, prefixName + ".terminal.serverName");
						String ip = Optional.ofNullable(properties.get(prefixName + ".terminal.ip")).orElse(StringUtils.EMPTY).toString();
						int port=Integer.valueOf(Optional.ofNullable(properties.get(prefixName + ".terminal.port")).orElse("0").toString());
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
							zkConfig.setRootPath(Optional.ofNullable(properties.get(prefixName + ".terminal.zk.rootPath")).orElse(StringUtils.EMPTY).toString());
							Asserts.notEmpty(zkConfig.getRootPath(), prefixName+".terminal.zk.rootPath");
							zkConfig.setServers(Optional.ofNullable(properties.get(prefixName + ".terminal.zk.servers")).orElse(StringUtils.EMPTY).toString());
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
