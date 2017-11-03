package xbus.stream.terminal.zk;

import org.springframework.beans.factory.annotation.Value;

import xbus.stream.terminal.TerminalConfigurator;

/**
 * 默认配置器;需手动删除永久不再上线的节点
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public class DefaultConfigurator extends TerminalConfigurator{
	private String currentNodeName;
	private String currentNodeFullPath;
	@Value("${xbus.stream.zk.rootPath}")
	private String rootPath;
	@Value("${xbus.stream.zk.servers}")
	private String servers;
	private ZookeeperListener zookeeperListener;
	@Override
	protected void listen() {
		//注册当前terminal和node
		String currentTerminalFullPath = new StringBuilder(rootPath).append("/").append(appName).toString();
		currentNodeName = new StringBuilder(ip).append(":").append(port).toString();
		currentNodeFullPath = new StringBuilder(currentTerminalFullPath).append("/").append(currentNodeName).toString();
		zookeeperListener = ZookeeperListener.getInstance(servers, rootPath, currentTerminalFullPath, currentNodeFullPath);
		zookeeperListener.setTerminalUpdater((terminals) -> updateTerminal(terminals));
		zookeeperListener.setTerminalNodeUpdater((terminalName, nodes) -> updateTerminalNode(terminalName, nodes));
		zookeeperListener.start();
	}
	@Override
	protected void release() throws Exception {
		if (zookeeperListener != null) {
			zookeeperListener.stop();
		}
	}
	
}
