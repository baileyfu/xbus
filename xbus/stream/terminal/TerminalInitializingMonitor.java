package xbus.stream.terminal;

/**
 * 监控终端变动并更新
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:26
 */
public interface TerminalInitializingMonitor {

	/**
	 * 初始化当前终端对应的处理通道
	 * 
	 * @param terminalColl
	 * @throws Exception
	 */
	public void initializeChannel(TerminalNode terminalNode) throws Exception;

}
