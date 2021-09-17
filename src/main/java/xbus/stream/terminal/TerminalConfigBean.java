package xbus.stream.terminal;

/**
 * 终端配置Bean
 * 
 * @author fuli
 * @date 2018年11月12日
 * @version 1.0.0
 */
public class TerminalConfigBean {
	public static final boolean DEFAULT_ENABLE = Boolean.TRUE;
	public static final int DEFAULT_PRIORITY = 0;
	
	private String serverName;
	private String ip;
	private int port;
	
	private boolean enable;
	private int priority;

	public TerminalConfigBean() {
		enable = DEFAULT_ENABLE;
		priority = DEFAULT_PRIORITY;
	}
	
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
