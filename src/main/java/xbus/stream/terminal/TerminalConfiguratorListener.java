package xbus.stream.terminal;

import java.util.Map;

import xbus.constants.TerminalTypeEnum;
/**
 * 监听终端变动
 * 
 * @author fuli
 * @date 2019年5月27日
 * @version 1.0.0
 */
public interface TerminalConfiguratorListener {
	void execute(TerminalTypeEnum terminalType, Map<String, Terminal> lastestTerminals);
}
