package com.lz.components.bus.exception;

import com.lz.components.bus.code.BusExceptionCode;
import com.lz.components.common.exception.LzRuntimeException;

/**
 * 总线异常
 * 
 * @author fuli
 * @date 2018年10月29日
 * @version 1.0.0
 */
public class BusException extends LzRuntimeException {
	private static final long serialVersionUID = -945269075458496051L;

	public BusException() {
		super(BusExceptionCode.BUS_FAILED);
	}

	public BusException(Throwable cause) {
		super(BusExceptionCode.BUS_FAILED, cause);
	}

	public BusException(BusExceptionCode BusExceptionCode) {
		super(BusExceptionCode);
	}

	public BusException(BusExceptionCode BusExceptionCode, Throwable cause) {
		super(BusExceptionCode, cause);
	}
}
