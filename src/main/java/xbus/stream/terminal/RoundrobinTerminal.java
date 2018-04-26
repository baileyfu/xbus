package xbus.stream.terminal;

import java.util.function.Consumer;

/**
 * 依次循环发送单个
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-19 14:07
 */
public class RoundrobinTerminal extends Terminal {
	private volatile int currentIndex = 0;
	@Override
	public void distribute(Consumer<TerminalNode> transmitter) {
		if (nodes != null && nodes.length > 0) {
			TerminalNode node = null;
			try {
				node = nodes[currentIndex++];
			} catch (Exception e) {
				node = nodes[0];
				e.printStackTrace();
			}
			transmitter.accept(node);
			currentIndex = currentIndex >= nodes.length ? 0 : currentIndex;
		}
	}

}
