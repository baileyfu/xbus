package xbus.exception;


import org.apache.commons.lang3.StringUtils;

/**
 * 总线异常
 *
 * @author fuli
 * @version 1.0.0
 * @date 2018年10月29日
 */
public class BusException extends RuntimeException {
    private static final long serialVersionUID = -945269075458496051L;
    private String errorCode;
    private String errorMsg;

    private void init(BusExceptionCode exc) {
        errorCode = exc.errorCode;
        errorMsg = exc.errorMsg;
    }

    public BusException() {
        init(BusExceptionCode.BUS_FAILED);
    }

    public BusException(Throwable cause) {
        super(cause);
        init(BusExceptionCode.BUS_FAILED);
    }

    public BusException(BusExceptionCode busExceptionCode) {
        init(busExceptionCode);
    }

    public BusException(BusExceptionCode busExceptionCode, Throwable cause) {
        super(cause);
        init(busExceptionCode);
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder();
        if (StringUtils.isNotBlank(errorMsg)) {
            message.append(errorMsg);
        }
        if (StringUtils.isNotBlank(errorCode)) {
            message.append("(");
            message.append(errorCode);
            message.append(")");
        }
        return message.toString();
    }

    @Override
    public String toString() {
        return "BusException [errorCode=" + errorCode + ", errorMsg=" + errorMsg + "]";
    }
}
