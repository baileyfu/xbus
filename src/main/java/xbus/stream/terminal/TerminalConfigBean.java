package xbus.stream.terminal;

/**
 * 终端配置Bean
 * 
 * @author fuli
 * @date 2018年11月12日
 * @version 1.0.0
 */
public class TerminalConfigBean {
	private String serverName;
	private String ip;
	private int port;

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
}
