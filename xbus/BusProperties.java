package xbus;

import java.lang.reflect.Method;

import org.springframework.stereotype.Component;

import xbus.annotation.BusEndpoint;
import xbus.annotation.BusRoot;
import xbus.em.MessageContentType;
import xbus.stream.message.OriginalBusMessage;
import xbus.stream.message.payload.BusPayload;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:24
 */
@Component
@BusRoot("/notice")
public class BusProperties{
	
	@BusEndpoint(value = "pay", contentType = MessageContentType.JSON)
	public void payNotice(OriginalBusMessage message){
	}
	
	public static void main(String[] args) throws Exception{
		BusProperties b=new BusProperties();
		Method m = b.getClass().getMethod("payNotice", OriginalBusMessage.class);
		Object o=m.invoke(b,new OriginalBusMessage());
		System.out.println((BusPayload)o);
		System.out.println(String.format("a%sb%s---%s","A","B",OriginalBusMessage.class));
	}
}
