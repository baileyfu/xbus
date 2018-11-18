package xbus.stream.terminal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * 终端配置器; <br/>
 * 实时维护终端及其节点信息;终端类型必须为Terminal而非其子类
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public abstract class TerminalConfigurator{
	// 当前服务的TerminalNode;
	public static TerminalNode CURRENT_TERMINAL_NODE;
	//当前所有的Termnal
	private Map<String,Terminal> lastestTerminals = null;
	protected String appName;

	public TerminalConfigurator(String appName, String ip, int port) {
		this.appName = appName;
		lastestTerminals = new HashMap<>();
		CURRENT_TERMINAL_NODE = new TerminalNode(appName);
		CURRENT_TERMINAL_NODE.setIp(ip);
		CURRENT_TERMINAL_NODE.setPort(port);
	}
	protected void fillCurrentNode(String ip,int port) {
		CURRENT_TERMINAL_NODE.setIp(ip);
		CURRENT_TERMINAL_NODE.setPort(port);
	}
	
	public Terminal getTerminal(String terminalName){
		return lastestTerminals.get(terminalName);
	}
	/**
	 * 当前所有Terminal及其节点
	 * 
	 * @return
	 */
	public Terminal[] getCurrentTerminals() {
		return lastestTerminals.values().toArray(new Terminal[lastestTerminals.size()]);
	}
	/**
	 * 更新最新的终端信息
	 * 
	 * @param terminalColl
	 */
	protected void updateTerminal(Set<Terminal> terminals) {
		if (terminals == null || terminals.size() == 0) {
			lastestTerminals.clear();
		} else {
			Map<String, Terminal> temp = new HashMap<>();
			for (Terminal terminal : terminals) {
				temp.put(terminal.getName(), terminal);
			}
			lastestTerminals = temp;
		}
	}
	/***
	 * 更新指定终端最新的节点信息
	 * 
	 * @param terminalName
	 * @param nodes
	 */
	protected synchronized void updateTerminalNode(String terminalName, Set<TerminalNode> nodes) {
		Terminal terminal = lastestTerminals.get(terminalName);
		if (terminal == null) {
			terminal = new Terminal();
			terminal.setName(terminalName);
			terminal.setNodes(nodes);
			lastestTerminals.put(terminalName, terminal);
		}else{
			terminal.setNodes(nodes);
		}
	}
	private boolean listened = false;
	public synchronized void start() throws Exception{
		if (!listened) {
			listen();
			listened = true;
		}
	}

	public void stop() throws Exception{
		release();
		listened = false;
	}
	/**
	 * 初始化完成后应调用的方法<br/>
	 * 监听terminal的变动然后调用update方法
	 */
	protected abstract void listen()throws Exception;
	/**
	 * 释放监听相关资源
	 */
	protected abstract void release()throws Exception;

	/************************************static method**************************************/
	/**
	 * 当前节点所属Terminal
	 * 
	 * @return
	 */
	public static String getCurrentTerminalName() {
		return CURRENT_TERMINAL_NODE==null?StringUtils.EMPTY:CURRENT_TERMINAL_NODE.getTerminalName();
	}
	/**
	 * 当前节点
	 * 
	 * @return
	 */
	public static TerminalNode getCurrentTerminalNode() {
		return CURRENT_TERMINAL_NODE;
	}
}
