package xbus.stream.terminal;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public class TerminalNode {
	private String terminalName;
	private String ip;
	private String port;
	private boolean durable;

	public TerminalNode(String terminalName) {
		this.terminalName = terminalName;
	}

	public String getTerminalName() {
		return terminalName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public String getName(){
		return new StringBuilder(terminalName).append("_").append(ip).append(":").append(port).toString();
	}
	
	@Override
	public String toString() {
		return "TerminalNode [terminalName=" + terminalName + ", ip=" + ip + ", port=" + port + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof TerminalNode)
			return ((TerminalNode) obj).getName().equals(this.getName());
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return new StringBuilder("TERMINAL_NODE-").append(getName()).toString().hashCode();
	}
	
}
