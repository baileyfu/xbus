package xbus.stream.terminal.zk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import xbus.stream.StreamLoggerHolder;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalNode;

/**
 * 监听ZooKeeper变动
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-11-03 10:45
 */
public final class ZookeeperListener implements StreamLoggerHolder{
	private static ZookeeperListener instance;

	private String rootPath;
	private String currentTerminalPath;
	private String currentTerminalNodePath;
	private ZkClient zkClient;
	private boolean running;
	private Consumer<Set<Terminal>> terminalUpdater;
	private BiConsumer<String,Set<TerminalNode>> terminalNodeUpdater;

	private ZookeeperListener(String servers,String rootPath,String currentTerminalPath,String currentTerminalNodePath) {
		this.rootPath = rootPath;
		this.currentTerminalPath = currentTerminalPath;
		this.currentTerminalNodePath = currentTerminalNodePath;
		this.running = false;
		zkClient = new ZkClient(servers, 10000, 10000, new SerializableSerializer());
	}
	public void setTerminalUpdater(Consumer<Set<Terminal>> terminalUpdater) {
		this.terminalUpdater = terminalUpdater;
	}
	public void setTerminalNodeUpdater(BiConsumer<String, Set<TerminalNode>> terminalNodeUpdater) {
		this.terminalNodeUpdater = terminalNodeUpdater;
	}
	synchronized void start() {
		if (!running) {
			if(!zkClient.exists(currentTerminalPath)){
				try{
					//终端有多个节点时可能会抛出异常
					zkClient.createPersistent(currentTerminalPath,currentTerminalPath);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if (!zkClient.exists(currentTerminalNodePath)) {
				zkClient.createPersistent(currentTerminalNodePath, currentTerminalNodePath);
			}
			//监听root的子节点(Terminal)变动
			zkClient.subscribeChildChanges(rootPath, terminalListener);
			zkClient.subscribeStateChanges(stateListener);
			flush(zkClient.getChildren(rootPath));
			running=true;
		}
	}
	synchronized void stop() {
		if (running) {
			//取消所有监听订阅
			zkClient.unsubscribeAll();
			zkClient.close();
		}
	}
	private void flush(List<String> terminals){
		//读取已注册的节点
		terminalUpdater.accept(terminals != null && terminals.size() > 0?terminals.stream().map(nodeValue2Terminal).collect(Collectors.toSet()):null);
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
			terminalNodeUpdater.accept(terminalName, nodes);
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
	public static synchronized ZookeeperListener getInstance(String servers,String rootPath,String currentTerminalPath,String currentTerminalNodePath) {
		if (instance == null)
			instance = new ZookeeperListener(servers, rootPath, currentTerminalPath,currentTerminalNodePath);
		return instance;
	}
}
