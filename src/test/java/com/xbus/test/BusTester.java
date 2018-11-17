package com.lz.components.bus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.lz.components.bus.annotation.BusEndpoint;
import com.lz.components.bus.annotation.BusRoot;
import com.lz.components.bus.core.config.BusConfigurator;
import com.lz.components.bus.em.MessageContentType;
import com.lz.components.bus.em.PostMode;
import com.lz.components.bus.stream.message.OriginalBusMessage;
import com.lz.components.bus.stream.message.payload.JSONBusPayload;


/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:24
 */
@BusRoot("/notice")
@ContextConfiguration(locations="classpath:spring-demo.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BusTester {
	@Autowired
	BusConfigurator ymlConfig;
	@Test
	public void test(){
		System.out.println();
	}
//	@Autowired
	private AsyncBusTemplate busTemplate;

	@BusEndpoint(value = "pay", contentType = MessageContentType.JSON)
	public void payNotice(String sourceTerminal, JSONBusPayload busPayload) {
		JSONObject json = busPayload.getValue();
		String value = json.getString("key");
		System.out.println("key : " + value);
	}
	@BusEndpoint(value = "repay", contentType = MessageContentType.JSON)
	public JSONBusPayload payNoticeReply(String sourceTerminal, JSONBusPayload busPayload) {
		JSONObject json = busPayload.getValue();
		String value = json.getString("key");
		System.out.println("key : " + value);
		
		return new JSONBusPayload("someJson");
	}

	public void post(String terminalName, String jsonString) throws Exception {
		OriginalBusMessage busMessage = new OriginalBusMessage(new JSONBusPayload(jsonString));
		busMessage.setSourceTerminal("currentTerminal");
		busMessage.setTransactional(false);
		busMessage.setPath("/notice/pay");
		busMessage.setRequireReceipt(true);
		busMessage.setReceiptConsumer(System.out::println);
		busTemplate.post(busMessage, PostMode.RANDOM,terminalName);
	}

	public static void main(String[] args) throws Exception {
	}
}
