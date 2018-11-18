package xbus.stream.terminal.file;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalConfigurator;
import xbus.stream.terminal.TerminalNode;
import xbus.stream.terminal.file.FileConfigBean.NodeAddress;
import xbus.stream.terminal.file.FileConfigBean.ServerInfo;

/**
 * 基于配置文件的terminal配置器
 * 
 * @author fuli
 * @date 2018年10月25日
 * @version 1.0.0
 */
public class FileConfigurator extends TerminalConfigurator{
	private Set<Terminal> specifiedByfile;
	/**
	 * @param appName
	 * @param currentServer
	 * @param identifiableServerList 与当前系统有通讯需求的服务与其节点信息
	 */
	public FileConfigurator(FileConfigBean fileConfig) {
		super(fileConfig.getServerName(),fileConfig.getIp(),fileConfig.getPort());
		specifiedByfile = new HashSet<>();
		for (ServerInfo serverInfo : fileConfig.getServers()) {
			Terminal terminal = new Terminal();
			terminal.setName(fileConfig.getServerName());
			Set<TerminalNode> nodes = new HashSet<>();
			List<NodeAddress> nodeInfo = serverInfo.getNodeInfo();
			//配置了子节点信息
			if (nodeInfo != null && nodeInfo.size() > 0) {
				for(NodeAddress na:nodeInfo){
					TerminalNode terminalNode=new TerminalNode(terminal.getName());
					terminalNode.setIp(na.getIp());
					terminalNode.setPort(na.getPort());
					nodes.add(terminalNode);
				}
			}
			terminal.setNodes(nodes);
			specifiedByfile.add(terminal);
		}
	}

	@Override
	protected void listen() throws Exception {
		this.updateTerminal(specifiedByfile);
	}

	@Override
	protected void release() throws Exception {
	}
}
