package xbus.constants;

import org.apache.http.util.Asserts;

/**
 * 系统关键字定义
 * 
 * @author fuli
 * @date 2018年11月23日
 * @version 1.0.0
 */
public final class Keywords {
	public static final String SYSTEM_PATH = "/%SYSTEM";
	public static final String RECEIPT_PATH = SYSTEM_PATH + "/%RECEIPT";

	public static void checkPath(String endpointPath) throws RuntimeException {
		Asserts.check(!endpointPath.startsWith(SYSTEM_PATH), "the endpoint can not starts with '"+SYSTEM_PATH+"'");
	}
}
;