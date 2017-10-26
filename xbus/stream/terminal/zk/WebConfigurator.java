package xbus.stream.terminal.zk;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public class WebConfigurator extends DefaultConfigurator {

	public WebConfigurator() {
		super();
		try{
			MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
			Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            port =objectNames.iterator().next().getKeyProperty("port");
		}catch(Exception e){
			LOGGER.error("xbus.stream.terminal.zk.WebConfigurator getPort error!", e);
			throw new RuntimeException(e);
		}
		fillCurrentNode();
	}

}
