package xbus.core.config;

/**
 * 注册回执消息处理器
 * 
 * @author fuli
 * @date 2018年11月28日
 * @version 1.0.0
 */
public interface ReceiptHandlerRegister {
	/**
	 * 注册到指定的总线
	 * </p>
	 * 返回空则表示注册到所有总线
	 * 
	 * @return
	 */
	default String registerToBus() {
		return null;
	};

	/**
	 * 注册回执处理器</p>
	 * 发送消息时指定的处理器优先级高于注册的处理器
	 * @param registerExecuter
	 */
	void register(xbus.core.config.RegisterExecuter registerExecuter);
}
