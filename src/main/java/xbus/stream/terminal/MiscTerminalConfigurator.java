package xbus.stream.terminal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import xbus.BusLoggerHolder;
import xbus.constants.TerminalTypeEnum;
import xbus.stream.terminal.ek.EKConfigBean;
import xbus.stream.terminal.ek.EurekaConfigurator;
import xbus.stream.terminal.file.FileConfigBean;
import xbus.stream.terminal.file.FileConfigurator;
import xbus.stream.terminal.zk.ZKConfigBean;
import xbus.stream.terminal.zk.ZKConfigurator;

/**
 * 多种终端配置器
 * 
 * @author fuli
 * @date 2019年5月27日
 * @version 1.0.0
 */
public class MiscTerminalConfigurator extends TerminalConfigurator implements BusLoggerHolder{
	private Map<TerminalTypeEnum,TerminalConfigurator> terminalConfigurators;
	private Map<String, TerminalConfigBean> terminalConfigs;
	private Map<String, Terminal>[] terminalArray;
	private TerminalConfiguratorListener miscTerminalConfiguratorListener=new TerminalConfiguratorListener() {
		@Override
		public void execute(TerminalTypeEnum terminalType, Map<String, Terminal> lastestTerminals) {
			terminalArray[calIndex(terminalType)] = lastestTerminals;
			Set<Terminal> terminals = new HashSet<>();
			for (Map<String, Terminal> terminalMap : terminalArray) {
				if(terminalMap!=null) {
					for (Terminal terminal : terminalMap.values()) {
						if (!terminals.contains(terminal)) {
							terminals.add(terminal);
						}
					}
				}
			}
			updateTerminal(terminals);
		}
	};

	private int calIndex(TerminalTypeEnum terminalType) {
		int index = 0;
		TerminalConfigBean target = terminalConfigs.get(terminalType.name());
		for (TerminalConfigBean config : terminalConfigs.values()) {
			if (target.getPriority() > config.getPriority()) {
				index++;
			}
		}
		return index;
	}
	@SuppressWarnings("unchecked")
	public MiscTerminalConfigurator(Map<String, TerminalConfigBean> terminalConfigs) {
		super(terminalConfigs.values().iterator().next());
		this.terminalConfigs = terminalConfigs;
		this.terminalArray = new HashMap[terminalConfigs.size()];
		terminalConfigurators = new HashMap<>();
		for (Entry<String, TerminalConfigBean> entry : terminalConfigs.entrySet()) {
			TerminalConfigBean terminalConfigBean=entry.getValue();
			TerminalConfigurator terminalConfigurator = null;
			if (terminalConfigBean instanceof ZKConfigBean) {
				terminalConfigurator = new ZKConfigurator((ZKConfigBean) terminalConfigBean);
			} else if (terminalConfigBean instanceof FileConfigBean) {
				terminalConfigurator = new FileConfigurator((FileConfigBean) terminalConfigBean);
			} else if (terminalConfigBean instanceof EKConfigBean) {
				terminalConfigurator = new EurekaConfigurator((EKConfigBean) terminalConfigBean);
			} else {
				throw new IllegalStateException("Unsupport terminalConfig " + terminalConfigBean);
			}
			terminalConfigurator.setTerminalConfiguratorListener(miscTerminalConfiguratorListener);
			terminalConfigurators.put(TerminalTypeEnum.valueOf(entry.getKey()), terminalConfigurator);
		}
	}

	@Override
	protected void listen() throws Exception {
		for (TerminalConfigurator tc : terminalConfigurators.values()) {
			tc.listen();
		}
	}

	@Override
	protected void release() throws Exception {
		boolean hasError = false;
		for (TerminalConfigurator tc : terminalConfigurators.values()) {
			try {
				tc.release();
			} catch (Exception e) {
				hasError = true;
				LOGGER.error("MiscTerminalConfigurator release " + tc + " error!", e);
			}
		}
		if (hasError) {
			throw new RuntimeException("MiscTerminalConfigurator release error!");
		}
	}

	@Override
	protected TerminalTypeEnum getTerminalType() {
		return TerminalTypeEnum.MISC;
	}
	@Override
	void setTerminalConfiguratorListener(TerminalConfiguratorListener terminalConfiguratorListener) {
		//保持MiscTerminalConfigurator的TerminalConfiguratorListener为null
	}
}
