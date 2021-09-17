package xbus.exception;


public class BusExceptionCode{
	/**
	 * 错误码(内部码)
	 */
	public final String errorCode;
	/**
	 * 错误信息
	 */
	public final String errorMsg;
	public static final BusExceptionCode BUS_FAILED = new BusExceptionCode("BUS_FAILED", "总线异常");
	public static final BusExceptionCode BUS_POST_FAILED = new BusExceptionCode("BUS_POST_FAILED", "总线发送异常");
	public static final BusExceptionCode BUS_RECEIVE_FAILED = new BusExceptionCode("BUS_RECEIVE_FAILED", "总线接收异常");
	
	private BusExceptionCode(String errorCode, String errorMsg) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

}
