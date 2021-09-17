package xbus.stream.terminal.ek;

import xbus.stream.terminal.TerminalConfigBean;

public class EKConfigBean extends TerminalConfigBean{
	private String zone;
	//刷新间隔;0表示不刷新
	private int renewInterval;
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public int getRenewInterval() {
		return renewInterval;
	}
	public void setRenewInterval(int renewInterval) {
		this.renewInterval = renewInterval;
	}
}
