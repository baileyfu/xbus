package com.xbus.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import xbus.annotation.BusEndpoint;
import xbus.annotation.BusRoot;
import xbus.core.AsyncBusTemplate;
import xbus.em.MessageContentType;
import xbus.em.PostMode;
import xbus.stream.message.OriginalBusMessage;
import xbus.stream.message.payload.JSONBusPayload;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:24
 */
@Component
@BusRoot("/notice")
public class BusTester {
	@Autowired
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
		busTemplate.post(terminalName, busMessage, PostMode.RANDOM);
	}

	public static void main(String[] args) throws Exception {
	}
}
