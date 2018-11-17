package com.lz.components.bus.stream.terminal.zk;

import com.lz.components.bus.stream.terminal.TerminalConfigBean;

public class ZKConfigBean extends TerminalConfigBean{
	private String rootPath;
	private String servers;

	public ZKConfigBean() {

	}

	public ZKConfigBean(String rootPath, String servers) {
		this.rootPath = rootPath;
		this.servers = servers;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getServers() {
		return servers;
	}

	public void setServers(String servers) {
		this.servers = servers;
	}
}
