package xbus.em;

import xbus.stream.terminal.RandomTerminal;
import xbus.stream.terminal.RoundrobinTerminal;
import xbus.stream.terminal.Terminal;

/**
 * 终端发送模式
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-30 17:06
 */
public enum PostMode {
	/**
	 * 发送给所有节点
	 */
	ALL,
	/**
	 * 随机发送给一个节点
	 */
	RANDOM,
	/**
	 * 轮询发送给一个节点
	 */
	ROUNDROBIN;
	
	public Terminal buildTerminal() {
		switch (name()) {
		case "ALL":
			return new Terminal();
		case "RANDOM":
			return new RandomTerminal();
		case "ROUNDROBIN":
			return new RoundrobinTerminal();
		}
		return null;
	}
}
