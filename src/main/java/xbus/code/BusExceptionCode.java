package com.lz.components.bus.code;

import com.lz.components.common.code.ExceptionCode;
import com.lz.components.common.code.RespCode;

public class BusExceptionCode extends ExceptionCode {

	public static final BusExceptionCode BUS_FAILED = new BusExceptionCode("BUS_FAILED", "总线异常",RespCode.BUS_FAILED);
	public static final BusExceptionCode BUS_POST_FAILED = new BusExceptionCode("BUS_POST_FAILED", "总线发送异常",RespCode.BUS_FAILED);
	public static final BusExceptionCode BUS_RECEIVE_FAILED = new BusExceptionCode("BUS_RECEIVE_FAILED", "总线接收异常",RespCode.BUS_FAILED);
	
	protected BusExceptionCode(String errorCode, String errorMsg, RespCode respCode) {
		super(errorCode, errorMsg, respCode);
	}

}
