package xbus.stream.terminal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import xbus.core.ShutdownAware;
import xbus.stream.StreamLoggerHolder;

/**
 * 终端配置器; <br/>
 * 实时维护终端及其节点信息;系统关闭时会释放资源
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:25
 */
public abstract class TerminalConfigurator extends ShutdownAware implements StreamLoggerHolder,ApplicationContextAware {
	// 当前Terminal名字
	private static String CURRENT_TERMINAL_NAME;
	// 当前服务的TerminalNode;
	private static TerminalNode CURRENT_TERMINAL_NODE;
	
	private ApplicationContext applicationContext;
	//当前所有的Termnal
	private Terminal[] lastest_terminals = null;
	private TerminalInitializingMonitor terminalInitializingMonitor;
	@Value("${xbus.stream.errorExit}")
	private boolean errorExit;
	@Value("${xbus.appName}")
	protected String appName;
	protected String host;
	protected String ip;
	protected String port;

	public TerminalConfigurator() {
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
	public void setTerminalInitializingMonitor(TerminalInitializingMonitor terminalInitializingMonitor) {
		this.terminalInitializingMonitor = terminalInitializingMonitor;
	}
	
	public Terminal getTerminal(String terminalName){
		if(lastest_terminals==null||lastest_terminals.length==0)
			return null;
		for (Terminal t : lastest_terminals) {
			if (terminalName.equals(t.getName())) {
				return t;
			}
		}
		return null;
	}
	/**
	 * 当前所有Terminal及其节点
	 * 
	 * @return
	 */
	public Terminal[] takeCurrentTerminals() {
		if (lastest_terminals == null)
			return null;
		return Arrays.copyOf(lastest_terminals, lastest_terminals.length);
	}
	public Terminal[] getCurrentTerminals() {
		return lastest_terminals;
	}
	/**
	 * 更新最新的终端信息
	 * 
	 * @param terminalColl
	 */
	protected void updateTerminal(Set<Terminal> terminals) {
		if (terminals == null || terminals.size() == 0) {
			lastest_terminals = null;
		} else {
			lastest_terminals = terminals.toArray(new Terminal[terminals.size()]);
		}
	}
	/***
	 * 更新指定终端最新的节点信息
	 * 
	 * @param terminalName
	 * @param nodes
	 */
	protected synchronized void updateTerminalNode(String terminalName, Set<TerminalNode> nodes) {
		if (lastest_terminals == null) {
			Terminal terminal = new Terminal();
			terminal.setName(terminalName);
			terminal.setNodes(nodes);
			lastest_terminals = new Terminal[]{terminal};
		} else {
			for (Terminal t : lastest_terminals) {
				if (terminalName.equals(t.getName())) {
					t.setNodes(nodes);
					break;
				}
			}
		}
	}

	private boolean initiated = false;
	public synchronized void init() {
		if (!initiated) {
			try {
				terminalInitializingMonitor.initializeChannel(CURRENT_TERMINAL_NODE);
				listen();
				initiated = true;
			} catch (Exception e) {
				LOGGER.error("xbus.stream.terminal.Configurator update terminals error!", e);
				//初始化失败则终止程序
				if (errorExit) {
					LOGGER.info("The error of the xbus.stream.Configurator.init() has caused System break down...");
					((ConfigurableApplicationContext) applicationContext).close();
					System.exit(-1);
				}
			}
		}
	}
	@Override
	protected void shutdown() {
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
	@Override
	public void setApplicationContext(ApplicationContext paramApplicationContext) throws BeansException {
		applicationContext = paramApplicationContext;
	}
	
	/************************************static method**************************************/
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
}
