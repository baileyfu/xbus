package xbus.stream.message;

import org.apache.http.util.Asserts;

import com.alibaba.fastjson.JSONObject;
import xbus.constants.MessageContentType;
import xbus.constants.MessageType;
import xbus.stream.message.payload.BusPayload;
import xbus.stream.message.payload.BytesBusPayload;
import xbus.stream.message.payload.JSONBusPayload;
import xbus.stream.message.payload.SerializedObjectBusPayload;
import xbus.stream.message.payload.TextBusPayload;
import xbus.stream.message.payload.XMLBusPayload;

/**
 * 原消息转换为BusMessage
 * 
 * @author fuli
 * @date 2018年11月2日
 * @version 1.0.0
 */
public interface MessageCoverter {
	/**
	 * 消息转换
	 * @param path 请求路径
	 * @param sourcePath 回执消息的请求路径
	 * @param sourceTerminal 请求终端
	 * @param messageType 消息类型
	 * @param messageContentType 消息负载类型
	 * @param payload 负载
	 * @param requireReceipt 是否需要回执
	 * @return
	 */
	default BusMessage coverter(String path,String sourcePath,String sourceTerminal,String messageType,String messageContentType,byte[] payload,JSONObject originals,boolean requireReceipt){
		Asserts.notEmpty(path, "the value of path of message's headers");
		Asserts.notEmpty(sourceTerminal, "the value of sourceTerminal of message's headers");
		Asserts.notEmpty(messageType, "the value of messageType of message's headers");
		Asserts.notEmpty(messageContentType, "the value of messageContentType of message's headers");
		MessageType mt = MessageType.valueOf(messageType);
		Asserts.check(mt != null, "Illegal messageType '" + messageType + "'");
		MessageContentType mct = MessageContentType.valueOf(messageContentType);
		Asserts.check(mct != null, "Illegal messageContentType '" + messageContentType + "'");
		BusPayload busPayload = null;
		switch (mct) {
		case TEXT:
			busPayload = new TextBusPayload(payload);
			break;
		case JSON:
			busPayload = new JSONBusPayload(payload);
			break;
		case XML:
			busPayload = new XMLBusPayload(payload);
			break;
		case BYTES:
			busPayload = new BytesBusPayload(payload);
			break;
		case SERIALIZED_OBJECT:
			busPayload = new SerializedObjectBusPayload(payload);
			break;
		default:
			throw new TypeNotPresentException(messageContentType, null);
		}
		BusMessage message = null;
		if (mt == MessageType.ORIGINAL) {
			message = new OriginalBusMessage();
			((OriginalBusMessage) message).setRequireReceipt(requireReceipt);
		} else {
			Asserts.notEmpty(sourcePath, "the value of sourcePath of ReceiptBusMessage's headers");
			message = new ReceiptBusMessage(sourcePath);
		}
		message.setPath(path);
		message.setSourceTerminal(sourceTerminal);
		message.setPayLoad(busPayload);
		message.setOriginals(originals);
		return message;
	}
}
