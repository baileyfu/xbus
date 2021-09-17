package xbus.stream.terminal;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 默认发送到所有节点
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-19 14:06
 */
public class Terminal {
	private String name;
	protected TerminalNode[] nodes;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TerminalNode[] getNodes() {
		return nodes;
	}
	public void setNodes(Set<TerminalNode> nodes) {
		if (nodes == null || nodes.size() == 1)
			this.nodes = null;
		this.nodes = new TerminalNode[nodes.size()];
		nodes.toArray(this.nodes);
	}

	/**
	 * 直接将当前Terminal的nodes指向source的nodes,以减少创建对象的开销
	 * 
	 * @param source
	 */
	public void referNodes(Terminal source) {
		this.nodes = source.nodes;
	}
	public void distribute(Consumer<TerminalNode> transmitter) {
		if (nodes != null && nodes.length > 0) {
			for (TerminalNode node : nodes) {
				transmitter.accept(node);
			}
		}
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof Terminal)
			return ((Terminal) obj).getName().equals(name);
		return super.equals(obj);
	}
	@Override
	public int hashCode() {
		return new StringBuilder("TERMINAL-").append(name).toString().hashCode();
	}
	@Override
	public String toString() {
		return "Terminal [name=" + name + ", nodes=" + Arrays.toString(nodes) + "]";
	}
	
}
