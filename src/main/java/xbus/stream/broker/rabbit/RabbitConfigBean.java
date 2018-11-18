package xbus.stream.broker.rabbit;

import xbus.stream.broker.BrokerConfigBean;

/**
 * RabbitMQ相关配置
 * 
 * @author fuli
 * @date 2018年11月12日
 * @version 1.0.0
 */
public class RabbitConfigBean extends BrokerConfigBean {
	private String rabbitTemplateName;

	public String getRabbitTemplateName() {
		return rabbitTemplateName;
	}

	public void setRabbitTemplateName(String rabbitTemplateName) {
		this.rabbitTemplateName = rabbitTemplateName;
	}
}
