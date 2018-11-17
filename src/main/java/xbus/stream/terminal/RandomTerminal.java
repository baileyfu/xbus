package com.lz.components.bus.stream.terminal;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * 随机发送单个
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-19 14:08
 */
public class RandomTerminal extends Terminal {

	@Override
	public void distribute(Consumer<TerminalNode> transmitter) {
		if (nodes != null && nodes.length > 0) {
			TerminalNode node = null;
			try {
				node = nodes[ThreadLocalRandom.current().nextInt(nodes.length)];
			} catch (Exception e) {
				node = nodes[0];
				e.printStackTrace();
			}
			transmitter.accept(node);
		}
	}

}
