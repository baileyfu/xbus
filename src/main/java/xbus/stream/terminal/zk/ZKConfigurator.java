package xbus.stream.terminal.zk;

import xbus.constants.TerminalTypeEnum;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 默认配置器;需手动删除永久不再上线的节点
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public class ZKConfigurator extends TerminalConfigurator{
	private String currentNodeName;
	private String currentNodeFullPath;
	private ZKConfigBean zkConfig;
	private ZookeeperListener zookeeperListener;

	public ZKConfigurator(ZKConfigBean zkConfig) {
		super(zkConfig.getServerName(),zkConfig.getIp(),zkConfig.getPort());
		this.zkConfig = zkConfig;
	}
	@Override
	protected void listen() throws Exception{
		//注册当前terminal和node
		String currentTerminalFullPath = new StringBuilder(zkConfig.getRootPath()).append("/").append(appName).toString();
		currentNodeName = new StringBuilder(CURRENT_TERMINAL_NODE.getIp()).append(":").append(CURRENT_TERMINAL_NODE.getPort()).toString();
		currentNodeFullPath = new StringBuilder(currentTerminalFullPath).append("/").append(currentNodeName).toString();
		zookeeperListener = ZookeeperListener.getInstance(zkConfig.getServers(), zkConfig.getRootPath(), currentTerminalFullPath, currentNodeFullPath);
		zookeeperListener.setTerminalUpdater((terminals) -> updateTerminal(terminals));
		zookeeperListener.setTerminalNodeUpdater((terminalName, nodes) -> updateTerminalNode(terminalName, nodes));
		zookeeperListener.start();
	}
	@Override
	protected void release() throws Exception{
		if (zookeeperListener != null) {
			zookeeperListener.stop();
			zookeeperListener = null;
		}
	}
	@Override
	protected TerminalTypeEnum getTerminalType() {
		return TerminalTypeEnum.ZOOKEEPER;
	}
}
