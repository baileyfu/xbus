package xbus.stream.terminal.zk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import xbus.stream.terminal.Configurator;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalNode;

/**
 * 默认配置器;需手动删除永久不再上线的节点
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public class DefaultConfigurator extends Configurator{
	private String currentNodeName;
	private String currentNodeFullPath;
	private ZkClient zkClient;
	private String rootPath;
	private String servers;

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
	public void setServers(String servers) {
		this.servers = servers;
	}
	@Override
	protected void listen() {
		zkClient = new ZkClient(servers, 10000, 10000, new SerializableSerializer());
		//注册当前terminal和node
		String currentTerminalFullPath = new StringBuilder(rootPath).append("/").append(appName).toString();
		currentNodeName = new StringBuilder(ip).append(":").append(port).toString();
		currentNodeFullPath = new StringBuilder(currentTerminalFullPath).append("/").append(currentNodeName).toString();
		if(!zkClient.exists(currentTerminalFullPath)){
			try{
				//终端有多个节点时可能会抛出异常
				zkClient.createPersistent(currentTerminalFullPath,appName);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (!zkClient.exists(currentNodeFullPath)) {
			zkClient.createPersistent(currentNodeFullPath, currentNodeName);
		}
		//监听root的子节点(Terminal)变动
		zkClient.subscribeChildChanges(rootPath, terminalListener);
		zkClient.subscribeStateChanges(stateListener);
		flush(zkClient.getChildren(rootPath));
	}
	@Override
	protected void release() throws Exception {
		if (zkClient != null) {
			//取消所有监听订阅
			zkClient.unsubscribeAll();
			zkClient.close();
		}
	}
	private void flush(List<String> terminals){
		//读取已注册的节点
		updateTerminal(terminals != null && terminals.size() > 0?terminals.stream().map(nodeValue2Terminal).collect(Collectors.toSet()):null);
	}
	private IZkChildListener terminalListener=new IZkChildListener(){
		@Override
		public void handleChildChange(String parentPath, List<String> terminals) throws Exception {
			flush(terminals);
		}
	};
	private IZkStateListener stateListener = new IZkStateListener() {
		@Override
		public void handleStateChanged(KeeperState state) throws Exception {
			LOGGER.info("DefaultConfigurator.handleStateChanged() zookeeper connection state has changed ({})!", state.name());
		}
		@Override
		public void handleNewSession() throws Exception {
			/**
			 * session重置后重新读取最新节点已防止session失效期间节点发生变动
			 */
			flush(zkClient.getChildren(rootPath));
		}
		@Override
		public void handleSessionEstablishmentError(Throwable error) throws Exception {
			LOGGER.error("DefaultConfigurator.handleSessionEstablishmentError() 重新建立zookeeper连接异常!", error);
		}
	};
	private IZkChildListener terminalNodeListener=new IZkChildListener(){
		@Override
		public void handleChildChange(String terminalFullPath, List<String> nodeValues) throws Exception {
			String terminalName=terminalFullPath.replace(rootPath + "/","");
			Set<TerminalNode> nodes=new HashSet<>();
			if (nodeValues != null && nodeValues.size() > 0) {
				for (String nodeValue : nodeValues) {
					String[] node = nodeValue.split(":");
					if (node.length == 2) {
						TerminalNode terminalNode = new TerminalNode(terminalName);
						terminalNode.setIp(node[0]);
						terminalNode.setPort(node[1]);
						nodes.add(terminalNode);
					}
				}
			}
			updateTerminalNode(terminalName, nodes);
		}
	};
	private Function<String,Terminal> nodeValue2Terminal=(terminalName)->{
		String terminalPath = rootPath + "/" + terminalName;
		//监听所有Terminal节点
		zkClient.subscribeChildChanges(terminalPath, terminalNodeListener);
		Set<TerminalNode> nodes=new HashSet<>();
		List<String> nodeValues = zkClient.getChildren(terminalPath);
		if (nodeValues != null && nodeValues.size() > 0) {
			for (String nodeValue : nodeValues) {
				String[] node = nodeValue.split(":");
				if(node.length==2){
					TerminalNode terminalNode = new TerminalNode(terminalName);
					terminalNode.setIp(node[0]);
					terminalNode.setPort(node[1]);
					nodes.add(terminalNode);
				}
			}
		}
		Terminal terminal=new Terminal();
		terminal.setName(terminalName);
		terminal.setNodes(nodes);
		return terminal;
	};
}
