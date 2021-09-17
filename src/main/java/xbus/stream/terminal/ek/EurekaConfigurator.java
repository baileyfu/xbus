package xbus.stream.terminal.ek;

import xbus.constants.TerminalTypeEnum;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 自动更新注册的服务信息
 * 
 * @author fuli
 * @date 2019年5月23日
 * @version 1.0.0
 */
public class EurekaConfigurator extends TerminalConfigurator {
	private EKConfigBean ekConfigBean;
	private EurekaListener eurekaListener;

	public EurekaConfigurator(EKConfigBean ekConfigBean) {
		super(ekConfigBean.getServerName(), ekConfigBean.getIp(), ekConfigBean.getPort());
		this.ekConfigBean = ekConfigBean;
	}

	@Override
	protected void listen() throws Exception {
		eurekaListener = new EurekaListener(ekConfigBean.getZone(),ekConfigBean.getRenewInterval());
		eurekaListener.setTerminalUpdater((terminals) -> updateTerminal(terminals));
		eurekaListener.start();
	}

	@Override
	protected void release() throws Exception {
		if (eurekaListener != null) {
			eurekaListener.stop();
		}
	}

	@Override
	protected TerminalTypeEnum getTerminalType() {
		return TerminalTypeEnum.EUREKA;
	}

}
