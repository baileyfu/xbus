package xbus.exception;


/**
 * 总线接收异常
 * 
 * @author fuli
 * @date 2018年10月29日
 * @version 1.0.0
 */
public class BusReceiveException extends RuntimeException {
	private static final long serialVersionUID = -6178964040056893130L;
	private static final String MESSAGE="BUS_RECEIVE_FAILED(总线接收异常)";
	public BusReceiveException() {
		super(MESSAGE);
	}

	public BusReceiveException(Throwable cause) {
		super(MESSAGE, cause);
	}
}
