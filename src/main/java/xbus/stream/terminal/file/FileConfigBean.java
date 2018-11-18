package xbus.stream.terminal.file;

import java.util.List;

import xbus.stream.terminal.TerminalConfigBean;

public class FileConfigBean extends TerminalConfigBean{
	private List<ServerInfo> servers;
	public List<ServerInfo> getServers() {
		return servers;
	}
	public void setServers(List<ServerInfo> servers) {
		this.servers = servers;
	}
	public static class ServerInfo{
		private String serverName;
		private List<NodeAddress> nodeInfo;
		public String getServerName() {
			return serverName;
		}
		public void setServerName(String serverName) {
			this.serverName = serverName;
		}
		public List<NodeAddress> getNodeInfo() {
			return nodeInfo;
		}
		public void setNodeInfo(List<NodeAddress> nodeInfo) {
			this.nodeInfo = nodeInfo;
		}
	}
	public static class NodeAddress{
		private String ip;
		private int port;
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
}
