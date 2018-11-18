package xbus.exception;


/**
 * 总线异常
 * 
 * @author fuli
 * @date 2018年10月29日
 * @version 1.0.0
 */
public class BusException extends RuntimeException {
	private static final long serialVersionUID = 8781357941561853515L;
	private static final String MESSAGE="BUS_FAILED(总线异常)";
	public BusException() {
		super(MESSAGE);
	}

	public BusException(Throwable cause) {
		super(MESSAGE, cause);
	}
}
