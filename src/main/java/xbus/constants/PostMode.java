package xbus.constants;

import xbus.stream.terminal.RandomTerminal;
import xbus.stream.terminal.RoundrobinTerminal;
import xbus.stream.terminal.Terminal;
import com.lz.components.common.beanutil.CacheableHashMap;

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
	private static CacheableHashMap<String, Terminal> ALL_MAP = new CacheableHashMap<>();
	private static CacheableHashMap<String, RandomTerminal> RANDOM_MAP = new CacheableHashMap<>();
	private static CacheableHashMap<String, RoundrobinTerminal> ROUNDROBIN_MAP = new CacheableHashMap<>();
	public Terminal buildTerminal(Terminal reference) {
		switch (name()) {
		case "ALL":
			return ALL_MAP.get(reference.getName(),()->{
				Terminal t=new Terminal();
				t.setName(reference.getName());
				t.referNodes(reference);
				return t;
			});
		case "RANDOM":
			return RANDOM_MAP.get(reference.getName(), ()->{
				RandomTerminal t=new RandomTerminal();
				t.setName(reference.getName());
				t.referNodes(reference);
				return t;
			});
		case "ROUNDROBIN":
			return ROUNDROBIN_MAP.get(reference.getName(), ()->{
				RoundrobinTerminal t=new RoundrobinTerminal();
				t.setName(reference.getName());
				t.referNodes(reference);
				return t;
			});
		}
		return null;
	}
}
