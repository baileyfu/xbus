package xbus.stream.terminal;

import org.apache.commons.lang3.StringUtils;

import xbus.constants.TerminalTypeEnum;

/**
 * 空终端配置器,不做任何操作
 * 
 * @author fuli
 * @date 2019年5月27日
 * @version 1.0.0
 */
public class VoidTerminalConfigurator extends TerminalConfigurator{

	public VoidTerminalConfigurator() {
		super(StringUtils.EMPTY, StringUtils.EMPTY, 0);
	}

	@Override
	protected void listen() throws Exception {
	}

	@Override
	protected void release() throws Exception {
	}

	@Override
	protected TerminalTypeEnum getTerminalType() {
		return TerminalTypeEnum.VOID;
	}

}
