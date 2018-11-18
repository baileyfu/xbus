package xbus.exception;


/**
 * 总线发送异常
 * 
 * @author fuli
 * @date 2018年10月29日
 * @version 1.0.0
 */
public class BusPostException extends RuntimeException {
	private static final long serialVersionUID = 1684986393701491399L;
	private static final String MESSAGE="BUS_POST_FAILED(总线发送异常)";
	public BusPostException() {
		super(MESSAGE);
	}

	public BusPostException(Throwable cause) {
		super(MESSAGE, cause);
	}
}
