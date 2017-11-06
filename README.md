# 消息总线 V1.0.0
基于Spring对异步消息进行抽象，将对生产者/消费者的操作抽象为对终端的操作；相关概念如下：<br/>
Terminal：某个服务或应用<br/>
TerminalNode：服务或应用的某个具体节点；一般指当前节点<br/>
BusMessage：消息；包含消息类型、消息内容类型、endpoint的value、消息载体、指定是否需要消息回执以及对回执的处理<br/>
BusPayload：消息载体<br/>
Endpoint：某服务或应用中的一个服务端点；类似SpringMVC的Controller；用于描述对消息的处理；严格按照文件目录结构构建；方法的返回只能是Void(不需要回执)或BusPayload(需要回执)<br/>

默认Terminal的注册管理是ZooKeeper，MQ是RabbitMQ<br/>
##### Notice：若消息需要回执而消费端处理消息后未返回回执或返回Null，则消费端会抛出异常；但并不影响系统运行

###版本变更记录
<table>
	<tr align='center'>
		<th>版本</th>
		<th>日期</th>
		<th>描述</th>
	</tr>
	<tr align='center'>
		<td>V1.0.0</td>
		<td>2017-11-03</td>
		<td align="left">完成预期功能,可投入使用</td>
	</tr>
	<tr align='center'>
		<td>V1.0.1</td>
		<td>2017-11-05</td>
		<td align="left">新增异步消息模板AsyncBusTemplate</td>
	</tr>
	<tr align='center'>
		<td>V1.0.2</td>
		<td>2017-11-06</td>
		<td align="left">1.endpoint的处理方法参数由OriginalBusMessage修改为sourceTerminal和具体的BusPayload<br/>
			2.各BusPayload的getValue返回不再是Object而是对应的数据类型
		</td>
	</tr>
</table>

---

### e.g
Spring配置：

	<bean id="xbusProperties" class="org.springframework.beans.factory.config.YamlPropertiesFactoryBean">
		<!-- 定义当前服务名(全局唯一)和zk地址等 -->
		<property name="resources" value="classpath*:xbus.yml"/>
	</bean>
	<context:property-placeholder properties-ref="xbusProperties"/>
	
	<!-- 需要先配置org.springframework.amqp.rabbit.core.RabbitTemplate -->
	<bean id="streamBroker" class="xbus.stream.broker.rabbit.RabbitMQBroker">
		<constructor-arg ref="rabbitTemplate"/>
	</bean>
	<bean id="terminalConfigurator" class="xbus.stream.terminal.zk.WebConfigurator" init-method="init">
		<property name="terminalInitializingMonitor" ref="streamBroker"/>
	</bean>
	<bean id="busTemplate" class="xbus.core.BusTemplate" init-method="init">
		<constructor-arg index="0" ref="streamBroker"/>
		<constructor-arg index="1" ref="terminalConfigurator"/>
	</bean>
	<bean class="xbus.core.BusBeanPostProcessor"/>
	
使用：
	
	发送操作：
	...
	BusPayload busPayload=new JSONBusPayload(someJSONObject);
	OriginalBusMessage busMessage=new OriginalBusMessage(busPayload);
	//设置endpoint的值
	busMessage.setPath("/notice/pay");
	//需要回执
	busMessage.setRequireReceipt(true);
	//对回执的处理
	busMessage.setReceiptConsumer((BusPayload)->{...});
	busTemplate.post(busMessage);
	//或者指定目标服务或应用(名字全局唯一)
	//busTemplate.post("terminal",busMessage,PostMode.RANDOM);
	...
	
	Endpoint定义：
	@Component
	@BusRoot("/notice")
	public class NoticeEndpoint{
		//支付结果的处理(需要回执)
		@BusEndpoint(value = "pay", contentType = MessageContentType.JSON)
		public BusPayload payNotice(OriginalBusMessage message){
			BusPayload receipt=null;
			...
			return receipt;
		}
		//绑卡结果的处理(不需要回执)
		@BusEndpoint(value = "bind", contentType = MessageContentType.JSON)
		public void payNotice(OriginalBusMessage message){
			...
		}
	}

---

### 配置详解
@BusRoot:配置端点的根路径<br/>
@BusEndpoint:配置端点的具体路劲<br/>
<table>
	<tr>
		<td>参数名</td>
		<td>类型</td>
		<td>默认值</td>
		<td>描述</td>
	</tr>
	<tr>
		<td>value</td>
		<td>String</td>
		<td></td>
		<td>不能为空；BusRoot的value可以是'/'</td>
	</tr>
	<tr>
		<td>contentType</td>
		<td>xbus.em.MessageContentType</td>
		<td>JSON</td>
		<td>消息载体的数据类型</td>
	</tr>
</table>

PostMode<br/>
消息发送模式；当服务部署有多个节点时决定了消息将发送到服务的那些具体节点<br/>
<table>
	<tr>
		<td>名称</td>
		<td>描述</td>
	</tr>
	<tr>
		<td>ALL</td>
		<td>发送到所有节点</td>
	</tr>
	<tr>
		<td>RANDOM</td>
		<td>随机发送到某一个节点</td>
	</tr>
	<tr>
		<td>ROUNDROBIN</td>
		<td>顺序发送到某一个节点</td>
	</tr>
</table>
