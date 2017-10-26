package xbus.stream.terminal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import xbus.stream.StreamLoggerHolder;

/**
 * 终端配置器; <br/>
 * 实时维护终端及其节点信息
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public abstract class Configurator implements StreamLoggerHolder,ApplicationContextAware {
	//当前所有的Termnal
	private static Set<Terminal> LASTEST_TERMINALS = null;
	// 当前Terminal名字
	private static String CURRENT_TERMINAL_NAME;
	// 当前服务的TerminalNode;
	private static TerminalNode CURRENT_TERMINAL_NODE;
	
	private ApplicationContext applicationContext;
	private TerminalInitializingMonitor terminalInitializingMonitor;
	private boolean errorExit;
	protected String appName;
	protected String host;
	protected String ip;
	protected String port;

	public Configurator() {
		errorExit = true;
		port = "0";
		try {
			InetAddress address = InetAddress.getLocalHost();
			ip = address.getHostAddress();
			host = address.getHostName();
		} catch (UnknownHostException e) {
			LOGGER.error("xbus.stream.terminal.Configurator getLocalHost error!", e);
			throw new RuntimeException(e);
		}
		CURRENT_TERMINAL_NAME = appName;
		CURRENT_TERMINAL_NODE = new TerminalNode(appName);
		fillCurrentNode();
	}

	protected void fillCurrentNode() {
		CURRENT_TERMINAL_NODE.setIp(ip);
		CURRENT_TERMINAL_NODE.setPort(port);
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public void setErrorExit(boolean errorExit) {
		this.errorExit = errorExit;
	}
	public void setTerminalInitializingMonitor(TerminalInitializingMonitor terminalInitializingMonitor) {
		this.terminalInitializingMonitor = terminalInitializingMonitor;
	}
	/**
	 * 更新最新的终端信息
	 * 
	 * @param terminalColl
	 */
	protected void updateTerminal(Set<Terminal> terminals) {
		LASTEST_TERMINALS = terminals;
	}
	/***
	 * 更新指定终端最新的节点信息
	 * 
	 * @param terminalName
	 * @param nodes
	 */
	protected void updateTerminalNode(String terminalName, Set<TerminalNode> nodes) {
		if (LASTEST_TERMINALS == null) {
			Set<Terminal> result = new HashSet<>();
			Terminal terminal = new Terminal();
			terminal.setName(terminalName);
			terminal.setNodes(nodes);
			result.add(terminal);
			LASTEST_TERMINALS = result;
		} else {
			Iterator<Terminal> it = LASTEST_TERMINALS.iterator();
			while (it.hasNext()) {
				Terminal t = it.next();
				if (terminalName.equals(t.getName())) {
					t.setNodes(nodes);
					break;
				}
			}
		}
	}

	public void init() {
		try {
			terminalInitializingMonitor.initializeChannel(CURRENT_TERMINAL_NODE);
			listen();
		} catch (Exception e) {
			LOGGER.error("xbus.stream.terminal.Configurator update terminals error!", e);
			//初始化失败则终止程序
			if (errorExit) {
				LOGGER.info("The error of the xbus.stream.Configurator.init() cause System break down...");
				((ConfigurableApplicationContext) applicationContext).close();
				System.exit(-1);
			}
		}
	}
	
	public void destory(){
		try{
			release();
		}catch(Exception e){
			LOGGER.error("Configurator.destory() error!", e);
		}
	}
	
	/**
	 * 初始化完成后应调用的方法<br/>
	 * 监听terminal的变动然后调用update方法
	 */
	protected abstract void listen();
	/**
	 * 释放监听相关资源
	 */
	protected abstract void release() throws Exception;
	/**
	 * 当前节点所属Terminal
	 * 
	 * @return
	 */
	public static String getCurrentTerminalName() {
		return CURRENT_TERMINAL_NAME;
	}
	/**
	 * 当前节点
	 * 
	 * @return
	 */
	public static TerminalNode getCurrentTerminalNode() {
		return CURRENT_TERMINAL_NODE;
	}
	/**
	 * 当前所有Terminal及其节点
	 * 
	 * @return
	 */
	public static Set<Terminal> takeCurrentTerminals() {
		if (LASTEST_TERMINALS == null)
			return null;
		Set<Terminal> result = new HashSet<>();
		result.addAll(LASTEST_TERMINALS);
		return result;
	}
	@Override
	public void setApplicationContext(ApplicationContext paramApplicationContext) throws BeansException {
		applicationContext = paramApplicationContext;
	}
}
