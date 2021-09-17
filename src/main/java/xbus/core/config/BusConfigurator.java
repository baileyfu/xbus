package xbus.core.config;

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
import xbus.stream.broker.rocket.RocketConfigBean;
import xbus.stream.terminal.TerminalConfigBean;
import xbus.stream.terminal.ek.EKConfigBean;
import xbus.stream.terminal.file.FileConfigBean;
import xbus.stream.terminal.file.FileConfigBean.NodeAddress;
import xbus.stream.terminal.file.FileConfigBean.ServerInfo;
import xbus.stream.terminal.zk.ZKConfigBean;
import xbus.BusLoggerHolder;
import xbus.constants.TerminalTypeEnum;
import xbus.stream.broker.BrokerConfigBean;
import xbus.stream.broker.rabbit.RabbitConfigBean;

/**
 * 总线装配
 * 
 * @author fuli
 * @date 2018年11月12日
 * @version 1.0.0
 */
public class BusConfigurator extends YamlPropertiesFactoryBean implements EnvironmentAware, BusLoggerHolder {
	private String xbusPrefix;
	protected Map<String, BusConfigBean> busConfigs;
	protected Map<String, BrokerConfigBean> brokerConfigs;
	protected Map<String,Map<String,TerminalConfigBean>> terminalConfigs;

	public BusConfigurator() {
		xbusPrefix = "components.xbus";
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
				String busName = StringUtils.substringBefore(propName.substring(xbusPrefix.length()+1), ".");
				String prefixName = xbusPrefix +"."+ busName;
				BusConfigBean busConfig=new BusConfigBean();
				busConfig.setAccessInterval(Long.valueOf(Optional.fromNullable(properties.get(prefixName+".accessInterval")).or(BusConfigBean.DEFAULT_ACCESS_INTERVAL).toString()));
				busConfig.setPauseIfNoConsume(Long.valueOf(Optional.fromNullable(properties.get(prefixName+".pauseIfNoConsume")).or(BusConfigBean.DEFAULT_PAUSE_IF_NOCONSUME).toString()));
				busConfig.setEnable(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName+".enable")).or(enable).toString()));
				busConfig.setAsyncAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName+".asyncAble")).or(BusConfigBean.DEFAULT_ASYNCABLE).toString()));
				busConfig.setNodeOriented(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName+".nodeOriented")).or(BusConfigBean.DEFAULT_NODE_ORIENTED).toString()));
				busConfig.setPostUndefined(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName+".postUndefined")).or(BusConfigBean.DEFAULT_POST_UNDEFINED).toString()));
				busConfig.setPostDebug(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName+".postDebug")).or(BusConfigBean.DEFAULT_POST_DEBUG).toString()));
				busConfig.setConsumeDebug(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName+".consumeDebug")).or(BusConfigBean.DEFAULT_CONSUME_DEBUG).toString()));
				busConfigs.put(busName, busConfig);
				/** broker */
				if(propName.startsWith(prefixName + ".broker")){
					if (brokerConfigs.get(busName) == null) {
						if(propName.startsWith(prefixName + ".broker.rocket")){//rocket
							RocketConfigBean rocketConfig = new RocketConfigBean();
							rocketConfig.setDurable(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.durable")).or(RocketConfigBean.DEFAULT_DURABLE).toString()));
							rocketConfig.setConsumeRetryAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.consumeRetryAble")).or(RocketConfigBean.DEFAULT_CONSUME_RETRYABLE).toString()));
							rocketConfig.setConsumeRetryCount(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.consumeRetryCount")).or(RocketConfigBean.DEFAULT_CONSUME_RETRY_COUNT).toString()));
							rocketConfig.setConsumerTimeoutMillis(Long.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.consumerTimeoutMillis")).or(RocketConfigBean.DEFAULT_CONSUME_TIMEOUT_MILLIS).toString()));
							rocketConfig.setProduceRetryAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.produceRetryAble")).or(RocketConfigBean.DEFAULT_PRODUCE_RETRYABLE).toString()));
							rocketConfig.setProduceRetryCount(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.produceRetryCount")).or(RocketConfigBean.DEFAULT_PRODUCE_RETRY_COUNT).toString()));
							rocketConfig.setProducerTimeoutMillis(Long.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.producerTimeoutMillis")).or(RocketConfigBean.DEFAULT_PRODUCE_TIMEOUT_MILLIS).toString()));
							rocketConfig.setMultiend(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.multiend")).or(RocketConfigBean.ROCKET_DEFAULT_MULTIEND).toString()));
							rocketConfig.setNameSrvAddr(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.nameSrvAddr")).or(StringUtils.EMPTY).toString());
							Asserts.notEmpty(rocketConfig.getNameSrvAddr(), prefixName+".broker.rocket.nameSrvAddr");
							rocketConfig.setTopicQueueNums(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.topicQueueNums")).or(RocketConfigBean.ROCKET_DEFAULT_TOPIC_QUEUE_NUMS).toString()));
							rocketConfig.setPullBatchSize(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.pullBatchSize")).or(RocketConfigBean.ROCKET_DEFAULT_PULL_BATCH_SIZE).toString()));
							rocketConfig.setPullInterval(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.pullInterval")).or(RocketConfigBean.ROCKET_DEFAULT_PULL_INTERVAL).toString()));
							rocketConfig.setConsumeBatchSize(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rocket.consumeBatchSize")).or(RocketConfigBean.ROCKET_DEFAULT_CONSUME_BATCH_SIZE).toString()));
							brokerConfigs.put(busName, rocketConfig);
						}else if(propName.startsWith(prefixName + ".broker.rabbit")){//rabbit
							RabbitConfigBean rabbitConfig = new RabbitConfigBean();
							rabbitConfig.setDurable(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.durable")).or(RabbitConfigBean.DEFAULT_DURABLE).toString()));
							rabbitConfig.setConsumeRetryAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.consumeRetryAble")).or(RabbitConfigBean.DEFAULT_CONSUME_RETRYABLE).toString()));
							rabbitConfig.setConsumeRetryCount(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.consumeRetryCount")).or(RabbitConfigBean.DEFAULT_CONSUME_RETRY_COUNT).toString()));
							rabbitConfig.setConsumerTimeoutMillis(Long.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.consumerTimeoutMillis")).or(RabbitConfigBean.DEFAULT_CONSUME_TIMEOUT_MILLIS).toString()));
							rabbitConfig.setProduceRetryAble(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.produceRetryAble")).or(RabbitConfigBean.DEFAULT_PRODUCE_RETRYABLE).toString()));
							rabbitConfig.setProduceRetryCount(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.produceRetryCount")).or(RabbitConfigBean.DEFAULT_PRODUCE_RETRY_COUNT).toString()));
							rabbitConfig.setProducerTimeoutMillis(Long.valueOf(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.producerTimeoutMillis")).or(RabbitConfigBean.DEFAULT_PRODUCE_TIMEOUT_MILLIS).toString()));
							rabbitConfig.setRabbitTemplateName(Optional.fromNullable(properties.get(prefixName + ".broker.rabbit.rabbitTemplateName")).or(StringUtils.EMPTY).toString());
							Asserts.notEmpty(rabbitConfig.getRabbitTemplateName(), prefixName + ".broker.rabbit.rabbitTemplateName");
							brokerConfigs.put(busName, rabbitConfig);
						}
					}
					continue;
				}
				/** terminal */
				if(propName.startsWith(prefixName + ".terminal")){
					Map<String, TerminalConfigBean> configMap = terminalConfigs.get(busName);
					if (configMap == null) {
						configMap = new HashMap<>();
						terminalConfigs.put(busName, configMap);
					}
					String serverName = Optional.fromNullable(properties.get(prefixName + ".terminal.serverName")).or(StringUtils.EMPTY).toString();
					Asserts.notEmpty(serverName, prefixName + ".terminal.serverName");
					String ip = Optional.fromNullable(properties.get(prefixName + ".terminal.ip")).or(StringUtils.EMPTY).toString();
					int port=Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".terminal.port")).or("0").toString());
					if(propName.startsWith(prefixName + ".terminal.file")){//file
						if (configMap.get(TerminalTypeEnum.FILE.name()) != null) {
							continue;
						}
						FileConfigBean fileConfig = new FileConfigBean();
						fileConfig.setServerName(serverName);
						fileConfig.setIp(ip);
						fileConfig.setPort(port);
						fileConfig.setEnable(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".terminal.file.enable")).or(TerminalConfigBean.DEFAULT_ENABLE).toString()));
						fileConfig.setPriority(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".terminal.file.priority")).or(TerminalConfigBean.DEFAULT_PRIORITY).toString()));
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
										nodeAddress.setIp(nIp == null ? "0.0.0.0" : nIp.toString());
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
						if (fileConfig.isEnable()) {
							checkIfDuplicatedPriority(configMap, fileConfig.getPriority());
							configMap.put(TerminalTypeEnum.FILE.name(), fileConfig);
						}
					}else if(propName.startsWith(prefixName + ".terminal.zk")){//zookeeper
						if (configMap.get(TerminalTypeEnum.ZOOKEEPER.name()) != null) {
							continue;
						}
						ZKConfigBean zkConfig = new ZKConfigBean();
						zkConfig.setServerName(serverName);
						zkConfig.setIp(ip);
						zkConfig.setPort(port);
						zkConfig.setEnable(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".terminal.zk.enable")).or(TerminalConfigBean.DEFAULT_ENABLE).toString()));
						zkConfig.setPriority(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".terminal.zk.priority")).or(TerminalConfigBean.DEFAULT_PRIORITY).toString()));
						zkConfig.setRootPath(Optional.fromNullable(properties.get(prefixName + ".terminal.zk.rootPath")).or(StringUtils.EMPTY).toString());
						Asserts.notEmpty(zkConfig.getRootPath(), prefixName+".terminal.zk.rootPath");
						zkConfig.setServers(Optional.fromNullable(properties.get(prefixName + ".terminal.zk.servers")).or(StringUtils.EMPTY).toString());
						Asserts.notEmpty(zkConfig.getServers(), prefixName+".terminal.zk.servers");
						if(zkConfig.isEnable()) {
							checkIfDuplicatedPriority(configMap, zkConfig.getPriority());
							configMap.put(TerminalTypeEnum.ZOOKEEPER.name(), zkConfig);
						}
					}else if(propName.startsWith(prefixName + ".terminal.ek")){//eureka
						if (configMap.get(TerminalTypeEnum.EUREKA.name()) != null) {
							continue;
						}
						EKConfigBean ekConfig=new EKConfigBean();
						ekConfig.setServerName(serverName);
						ekConfig.setIp(ip);
						ekConfig.setPort(port);
						ekConfig.setEnable(Boolean.valueOf(Optional.fromNullable(properties.get(prefixName + ".terminal.ek.enable")).or(TerminalConfigBean.DEFAULT_ENABLE).toString()));
						ekConfig.setPriority(Integer.valueOf(Optional.fromNullable(properties.get(prefixName + ".terminal.ek.priority")).or(TerminalConfigBean.DEFAULT_PRIORITY).toString()));
						ekConfig.setRenewInterval(Integer.parseInt(Optional.fromNullable(properties.get(prefixName + ".terminal.ek.renewInterval")).or("0").toString()));
						ekConfig.setZone(Optional.fromNullable(properties.get(prefixName + ".terminal.ek.zone")).or(StringUtils.EMPTY).toString());
						Asserts.notEmpty(ekConfig.getZone(), prefixName+".terminal.ek.zone");
						if(ekConfig.isEnable()) {
							checkIfDuplicatedPriority(configMap, ekConfig.getPriority());
							configMap.put(TerminalTypeEnum.EUREKA.name(), ekConfig);
						}
					}
				}
			}
		}
	}
	//同一个bus下不同的终端配置优先级必须不同
	private void checkIfDuplicatedPriority(Map<String, TerminalConfigBean> configMap,int priority) {
		for (TerminalConfigBean tcb : configMap.values()) {
			if (tcb.getPriority() == priority) {
				throw new IllegalStateException("the xbus.terminal config has duplicated priority : "+priority) ;
			}
		}
	}
	@Override
	public void afterPropertiesSet(){
		Resource resource = new ClassPathResource("/xbus.yml");
		if (!resource.exists()) {
			resource = new ClassPathResource("/config/xbus.yml");
		}
		if (!resource.exists()) {
			resource = new ClassPathResource("/application.yml");
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
