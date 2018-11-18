package xbus.stream.message;

import org.apache.http.util.Asserts;

import xbus.em.MessageContentType;
import xbus.em.MessageType;
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
	default BusMessage coverter(String path,String sourceTerminal,String messageType,String messageContentType,byte[] payload){
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
		BusMessage message = mt == MessageType.ORIGINAL ? new OriginalBusMessage() : new ReceiptBusMessage();
		message.setPath(path);
		message.setSourceTerminal(sourceTerminal);
		message.setPayLoad(busPayload);
		return message;
	}
}
