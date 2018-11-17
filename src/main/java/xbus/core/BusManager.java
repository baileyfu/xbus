package com.lz.components.bus.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.http.util.Asserts;

import com.lz.components.bus.core.config.BusConfigBean;
import com.lz.components.bus.em.MessageType;
import com.lz.components.bus.em.PostMode;
import com.lz.components.bus.stream.broker.AutoConsumeStreamBroker;
import com.lz.components.bus.stream.broker.ConsumeReceipt;
import com.lz.components.bus.stream.broker.ManualConsumeStreamBroker;
import com.lz.components.bus.stream.broker.StreamBroker;
import com.lz.components.bus.stream.message.BusMessage;
import com.lz.components.bus.stream.message.OriginalBusMessage;
import com.lz.components.bus.stream.message.ReceiptBusMessage;
import com.lz.components.bus.stream.message.payload.BusPayload;
import com.lz.components.bus.stream.terminal.Terminal;
import com.lz.components.bus.stream.terminal.TerminalConfigurator;
import com.lz.components.common.log.holder.CommonLoggerHolder;
import com.lz.components.common.util.variable.ActionGenerator;

import io.reactivex.BackpressureStrategy;

/**
 * 总线管理器<br/>
 * 系统关闭时负责释放资源
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-30 09:28
 */
public final class BusManager implements CommonLoggerHolder {
	//总线名
	private String name;
	private StreamBroker streamBroker;
	private TerminalConfigurator terminalConfigurator;
	private Map<String, BiFunction<String,BusPayload, BusPayload>> endpointHandlers;
	private Map<String, Consumer<BusPayload>> endpointReplyHandlers;
	private boolean running;
	private ActionGenerator actionGenerator;
	//消费间隔(默认100ms)
	private long consumeIntervalMills;

	protected BusManager(String name,StreamBroker streamBroker, TerminalConfigurator terminalConfigurator) {
		this.name = name;
		this.streamBroker = streamBroker;
		this.terminalConfigurator = terminalConfigurator;
		endpointHandlers = new HashMap<>();
		endpointReplyHandlers = new HashMap<>();
		running = false;
		consumeIntervalMills = BusConfigBean.DEFAULT_ACCESS_INTERVAL;
	}

	/**
	 * 系统初始化时调用此方法将来设置各path的handler
	 * 
	 * @param path
	 * @param handler
	 */
	void addEndpointHandler(String path, BiFunction<String,BusPayload, BusPayload> handler) {
		if (endpointHandlers.containsKey(path))
			throw new UnsupportedOperationException("duplicate endpointHandler of path '" + path + "'");
		endpointHandlers.put(path, handler);
	}
	/**
	 * 消息预处理<br/>
	 * 类型、回执消息处理器的检查等
	 * 
	 * @param message
	 * @return
	 */
	private BusMessage pretreat(BusMessage message) {
		Asserts.notNull(message, "message");
		Asserts.notEmpty(message.getPath(), "message.path");
		Asserts.notEmpty(message.getSourceTerminal(), "message.sourceTerminal");
		Asserts.notNull(message.getPayLoad(), "message.payLoad");
		// 检查回执消息处理方法
		if (message.getMessageType() == MessageType.ORIGINAL) {
			OriginalBusMessage originalBusMessage = (OriginalBusMessage) message;
			Asserts.check(originalBusMessage.getReceiptConsumer() != null, "the receiptHandler of path '" + message.getPath() + "' can not be null!");
			// 回执处理器以第一次操作时加入的为准
			if (!endpointReplyHandlers.containsKey(message.getPath())) {
				endpointReplyHandlers.put(message.getPath(), originalBusMessage.getReceiptConsumer());
			}
		}
		return message;
	}
	/**
	 * 发送给所有终端的所有节点
	 * 
	 * @param path
	 * @param message
	 */
	public void post(BusMessage message) {
		streamBroker.produce(terminalConfigurator.getCurrentTerminals(), pretreat(message));
	}
	/**
	 * 按指定发送模式发送给所有终端
	 * 
	 * @param path
	 * @param message
	 */
	public void post(BusMessage message,PostMode postMode) {
		Terminal[] currentTerminals = terminalConfigurator.getCurrentTerminals();
		if (currentTerminals == null || currentTerminals.length == 0) {
			throw new IllegalStateException("no terminal has been found!");
		}
		Terminal[] terminals=new Terminal[currentTerminals.length];
		for(int i=0;i<currentTerminals.length;i++){
			terminals[i]=postMode.buildTerminal();
			terminals[i].setName(currentTerminals[i].getName());
			terminals[i].referNodes(currentTerminals[i]);
		}
		streamBroker.produce(terminals, pretreat(message));
	}
	/**
	 * 按节点发送策略将消息发送到指定终端
	 * @param message
	 * @param postMode
	 * @param terminalNames 目标终端服务名
	 */
	public void post(BusMessage message, PostMode postMode,String...terminalNames) {
		if (terminalNames.length == 0) {
			throw new IllegalStateException("terminalNames can not be empty");
		}
		Set<String> uniqueTerminalName = new HashSet<>();
		for(String terminalName:terminalNames){
			uniqueTerminalName.add(terminalName);
		}
		Terminal[] terminals=new Terminal[uniqueTerminalName.size()];
		int i=0;
		for (String terminalName:uniqueTerminalName) {
			Terminal target = terminalConfigurator.getTerminal(terminalName);
			Asserts.notNull(target, "terminal(" + terminalName + ")");
			terminals[i]=postMode.buildTerminal();
			terminals[i].setName(target.getName());
			terminals[i++].referNodes(target);
		}
		streamBroker.produce(terminals, pretreat(message));
	}
	/** 消息处理器 */
	private Function<List<BusMessage>,List<ConsumeReceipt>> consumer=(msgList)->{
		List<ConsumeReceipt> receiptList = new ArrayList<>();
		try{
			for (BusMessage message : msgList) {
				String sourceTerminalName = message.getSourceTerminal();
				String path = message.getPath();
				if (message.getMessageType() == MessageType.ORIGINAL) {
					OriginalBusMessage originalMessage = (OriginalBusMessage) message;
					BiFunction<String, BusPayload, BusPayload> handler = endpointHandlers.get(path);
					Asserts.check(handler != null, "the originalBusMessageHandler of path '" + path + "' of "+sourceTerminalName + " does not exist!");
					BusPayload receipt = handler.apply(originalMessage.getSourceTerminal(),originalMessage.getPayLoad());
					if (originalMessage.isRequireReceipt()) {
						Asserts.check(receipt != null, "the path '" + path + "' of " + sourceTerminalName+ "' require receipt , but the receipt is null!");
						Terminal sourceTerminal = terminalConfigurator.getTerminal(sourceTerminalName);
						Asserts.check(sourceTerminal != null,"receipt error! the source terminal '" + sourceTerminalName + "' does not exist!");
						streamBroker.produce(sourceTerminal, new ReceiptBusMessage(receipt));
					}
				} else {
					Consumer<BusPayload> handler = endpointReplyHandlers.get(path);
					Asserts.check(handler != null, "the receiptBusMessageHandler of path '" + path + "' of "+ sourceTerminalName + " does not exist!");
					handler.accept(message.getPayLoad());
				}
				ConsumeReceipt consumeReceipt = new ConsumeReceipt(message.getMessageId());
				consumeReceipt.ackSuccess();
				receiptList.add(consumeReceipt);
			}
		} catch (Exception e) {
			LOGGER.error("consume message error", e);
		}
		return receiptList;
	};
	/**
	 * 生产/消费失败不可回滚/重试
	 */
	synchronized void start() throws Exception{
		if (!running) {
			running = true;
			terminalConfigurator.start();
			streamBroker.initializeChannel(TerminalConfigurator.getCurrentTerminalNode(), endpointReplyHandlers.keySet());
			//由Broker负责拉取消息
			if (streamBroker instanceof AutoConsumeStreamBroker) {
				((AutoConsumeStreamBroker) streamBroker).startReceive(consumeIntervalMills, consumer);
			} else {// 由BusManager拉取消息
				String busAlias = "BusManager(" + name + ")";
				actionGenerator = new ActionGenerator(consumeIntervalMills, TimeUnit.MILLISECONDS, () -> {
					try {
						((ManualConsumeStreamBroker) streamBroker).consume(TerminalConfigurator.getCurrentTerminalNode(),consumer);
					} catch (Exception e) {
						LOGGER.error(busAlias + " consume error !", e);
						// 消费出错后暂停100ms
						try {
							Thread.sleep(100l);
						} catch (InterruptedException ie) {
							LOGGER.error(busAlias + " Thread.sleep() error", ie);
						}
					}
				});
				actionGenerator.setName(busAlias);
				actionGenerator.start(BackpressureStrategy.LATEST);
			}
		}
	}
	
	synchronized void stop() throws Exception {
		if (running) {
			if (streamBroker instanceof AutoConsumeStreamBroker) {
				((AutoConsumeStreamBroker) streamBroker).stopReceive();
			} else {
				actionGenerator.stop();
			}
			streamBroker.destoryChannel();
			terminalConfigurator.stop();
			running = false;
		}
	}

	public long getConsumeIntervalMills() {
		return consumeIntervalMills;
	}

	/**
	 * 消费间隔不得小于100ms
	 * 
	 * @param consumeIntervalMills
	 */
	public void setConsumeIntervalMills(long consumeIntervalMills) {
		Asserts.check(consumeIntervalMills > 99, name+"'s accessInterval can not less than 100!");
		this.consumeIntervalMills = consumeIntervalMills;
	}

	public String getName(){
		return name;
	}
}
