package xbus.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import commons.beanutils.CacheableHashMap;
import commons.variable.SimpleActionGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

import com.alibaba.fastjson.JSON;

import io.reactivex.BackpressureStrategy;
import xbus.BusLoggerHolder;
import xbus.bean.EndpointBean;
import xbus.constants.Keywords;
import xbus.constants.MessageType;
import xbus.constants.PostMode;
import xbus.core.config.BusConfigBean;
import xbus.core.config.ReceiptHandlerRegister;
import xbus.stream.broker.AutoConsumeStreamBroker;
import xbus.stream.broker.ConsumeReceipt;
import xbus.stream.broker.ManualConsumeStreamBroker;
import xbus.stream.broker.StreamBroker;
import xbus.stream.message.payload.BusPayload;
import xbus.stream.terminal.Terminal;

/**
 * 总线管理器<br/>
 * 系统关闭时负责释放资源
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-30 09:28
 */
public final class BusManager implements BusLoggerHolder {
	private static Supplier<CacheableHashMap<String, Consumer<? extends BusPayload>>> IFNULL = () -> new CacheableHashMap<>();
	
	private BusConfigBean busConfig;
	//总线名
	private String name;
	private StreamBroker streamBroker;
	private xbus.stream.terminal.TerminalConfigurator terminalConfigurator;
	private Set<EndpointBean> endpoints;
	private Map<String, BiFunction<String, xbus.stream.message.payload.BusPayload, xbus.stream.message.payload.BusPayload>> endpointHandlers;
	private CacheableHashMap<String, CacheableHashMap<String, Consumer<? extends xbus.stream.message.payload.BusPayload>>> defaultReceiptHandlers;
	private CacheableHashMap<String, CacheableHashMap<String, Consumer<? extends xbus.stream.message.payload.BusPayload>>> runtimeReceiptHandlers;
	private CacheableHashMap<String, Consumer<? extends xbus.stream.message.payload.BusPayload>> globalReceiptHandlers;
	private boolean running;
	private SimpleActionGenerator simpleActionGenerator;

	BusManager(String name,StreamBroker streamBroker, xbus.stream.terminal.TerminalConfigurator terminalConfigurator) {
		this.name = name;
		this.streamBroker = streamBroker;
		this.terminalConfigurator = terminalConfigurator;
		endpoints = new HashSet<>();
		endpointHandlers = new HashMap<>();
		/** 优先级：runtime > default > global */
		runtimeReceiptHandlers = new CacheableHashMap<>();
		defaultReceiptHandlers = new CacheableHashMap<>();
		globalReceiptHandlers = new CacheableHashMap<>();
		running = false;
	}
	/** 消息处理器 */
	private Function<List<xbus.stream.message.BusMessage>,List<ConsumeReceipt>> consumer=(msgList)->{
		List<ConsumeReceipt> receiptList = new ArrayList<>();
		try{
			for (xbus.stream.message.BusMessage message : msgList) {
				String sourceTerminalName = message.getSourceTerminal();
				if(busConfig.isConsumeDebug()) {
					LOGGER.debug("[Bus-{}] consume from {} with message : {}",name,sourceTerminalName,message);
				}
				String path = message.getPath();
				if (message.getMessageType() == MessageType.ORIGINAL) {
					xbus.stream.message.OriginalBusMessage originalMessage = (xbus.stream.message.OriginalBusMessage) message;
					BiFunction<String, xbus.stream.message.payload.BusPayload, xbus.stream.message.payload.BusPayload> handler = endpointHandlers.get(path);
					Asserts.check(handler != null, "the originalBusMessageHandler of path '" + path + "' of "+sourceTerminalName + " does not exist!");
					xbus.stream.message.payload.BusPayload receipt = handler.apply(originalMessage.getSourceTerminal(),originalMessage.getPayLoad());
					if (originalMessage.isRequireReceipt()) {
						Asserts.check(receipt != null, "the path '" + path + "' of " + sourceTerminalName+ "' require receipt , but the receipt is null!");
						Terminal sourceTerminal = terminalConfigurator.getTerminal(sourceTerminalName);
						Asserts.check(sourceTerminal != null,"receipt error! the source terminal '" + sourceTerminalName + "' does not exist!");
						xbus.stream.message.ReceiptBusMessage receiptMessage=new xbus.stream.message.ReceiptBusMessage(path);
						// 回执消息path为关键字,自定义path不可重复
						receiptMessage.setPath(Keywords.RECEIPT_PATH);
						receiptMessage.setSourceTerminal(xbus.stream.terminal.TerminalConfigurator.getCurrentTerminalName());
						receiptMessage.setPayLoad(receipt);
						receiptMessage.setOriginals(message.getOriginals());
						//回执只发送给原终端的某个节点
						streamBroker.produce(PostMode.ROUNDROBIN.buildTerminal(sourceTerminal), receiptMessage);
						if(busConfig.isPostDebug()) {
							LOGGER.debug("[Bus-{}] post RECEIPT to {} with message : {}",name,sourceTerminalName,receiptMessage);
						}
					}
				} else {
					String sourcePath = ((xbus.stream.message.ReceiptBusMessage) message).getSourcePath();
					Consumer<? extends xbus.stream.message.payload.BusPayload> receiptHandler = runtimeReceiptHandlers.get(sourceTerminalName,IFNULL).get(sourcePath);
					if (receiptHandler == null) {
						receiptHandler = defaultReceiptHandlers.get(sourceTerminalName,IFNULL).get(sourcePath);
					}
					if (receiptHandler == null) {
						receiptHandler = globalReceiptHandlers.get(sourcePath);
					}
					Asserts.check(receiptHandler != null, "the receiptBusMessageHandler of "+sourceTerminalName+"'s path '" + sourcePath + " does not exist!");
					receiptHandler.accept(message.getPayLoad());
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
	 * 消息预处理<br/>
	 * 类型、回执消息处理器的检查等
	 * 
	 * @param message
	 * @return
	 */
	private xbus.stream.message.BusMessage pretreat(Terminal[] terminals, xbus.stream.message.BusMessage message) {
		Asserts.notNull(message, "message");
		Asserts.notEmpty(message.getPath(), "message.path");
		Asserts.notEmpty(message.getSourceTerminal(), "message.sourceTerminal");
		Asserts.notNull(message.getPayLoad(), "message.payLoad");
		// 检查回执消息处理方法
		if (message.getMessageType() == MessageType.ORIGINAL) {
			xbus.stream.message.OriginalBusMessage originalBusMessage = (xbus.stream.message.OriginalBusMessage) message;
			if(originalBusMessage.isRequireReceipt()) {
				for(Terminal terminal:terminals) {
					// 回执处理器以第一次操作时加入的为准,目前不支持某终端下同一path的不同回执消息设置不同的handler
					Consumer<? extends xbus.stream.message.payload.BusPayload> receiptHandler = runtimeReceiptHandlers.get(terminal.getName(),IFNULL).get(message.getPath(),()->originalBusMessage.getReceiptConsumer());
					if (receiptHandler == null) {
						receiptHandler = defaultReceiptHandlers.get(terminal.getName(), IFNULL).get(message.getPath());
					}
					if (receiptHandler == null) {
						receiptHandler = globalReceiptHandlers.get(message.getPath());
					}
					Asserts.check(receiptHandler != null,"the receiptHandler of "+terminal.getName()+"'path '" + message.getPath() + "' can not be null!");
				}
			}
		}
		return message;
	}
	/**
	 * 总线名称
	 * @return
	 */
	public String getName(){
		return name;
	}

	/**
	 * 发送给所有终端的所有节点
	 * @param message
	 */
	public void post(xbus.stream.message.BusMessage message) {
		post(message, PostMode.ALL);
	}

	/**
	 * 按指定发送模式发送给所有已识别的终端
	 * @param message
	 * @param postMode
	 */
	public void post(xbus.stream.message.BusMessage message, PostMode postMode) {
		if(busConfig.isPostDebug()) {
			LOGGER.debug("[Bus-{}] post to all by {} with message : {}", name,postMode, message.toString());
		}
		Terminal[] currentTerminals = terminalConfigurator.getCurrentTerminals();
		if (currentTerminals == null || currentTerminals.length == 0) {
			throw new IllegalStateException("no terminal has been found!");
		}
		Terminal[] terminals=new Terminal[currentTerminals.length];
		for(int i=0;i<currentTerminals.length;i++){
//			if(busConfig.isNodeOriented()) {
				terminals[i]=postMode.buildTerminal(currentTerminals[i]);
			//}else {//不支持面向节点则以轮询的方式发送消息
//				terminals[i]=PostMode.ROUNDROBIN.buildTerminal(currentTerminals[i]);
//			}
		}
		streamBroker.produce(terminals, pretreat(terminals,message));
	}
	/**
	 * 按节点发送策略将消息发送到指定终端
	 * @param message
	 * @param postMode
	 * @param terminalNames 目标终端服务名
	 */
	public void post(xbus.stream.message.BusMessage message, PostMode postMode, String...terminalNames) {
		if (terminalNames.length == 0) {
			throw new IllegalStateException("terminalNames can not be empty");
		}
		if(busConfig.isPostDebug()) {
			LOGGER.debug("[Bus-{}] post to {} by {} with message : {}", name,JSON.toJSONString(terminalNames),postMode, message.toString());
		}
		Set<String> uniqueTerminalName = new HashSet<>();
		for(String terminalName:terminalNames){
			uniqueTerminalName.add(terminalName);
		}
		Terminal[] terminals=new Terminal[uniqueTerminalName.size()];
		int i=0;
		for (String terminalName : uniqueTerminalName) {
			Terminal target = terminalConfigurator.getTerminal(terminalName);
			if (!busConfig.isPostUndefined()) {
				Asserts.check(target != null, "terminal(" + terminalName + ") is undefined !");
			}
			//支持发送到未定义的终端,此时无节点信息
			if (target == null) {
				target = new Terminal();
				target.setName(terminalName);
			}
			terminals[i] = postMode.buildTerminal(target);
		}
		streamBroker.produce(terminals, pretreat(terminals,message));
	}
	/**
	 * 生产/消费失败不可回滚/重试
	 */
	synchronized void start() throws Exception{
		if (!running) {
			running = true;
			terminalConfigurator.start();
			streamBroker.initializeChannel(xbus.stream.terminal.TerminalConfigurator.getCurrentTerminalNode(), endpoints);
			//由Broker负责拉取消息
			if (streamBroker instanceof AutoConsumeStreamBroker) {
				((AutoConsumeStreamBroker) streamBroker).startReceive(busConfig.getAccessInterval(), consumer);
			} else if(streamBroker instanceof ManualConsumeStreamBroker){// 由BusManager拉取消息
				String busAlias = "BusManager(" + name + ")";
				simpleActionGenerator=new SimpleActionGenerator(busConfig.getAccessInterval(), TimeUnit.MILLISECONDS, () -> {
					try {
						int consumed=((ManualConsumeStreamBroker) streamBroker).consume(xbus.stream.terminal.TerminalConfigurator.getCurrentTerminalNode(),consumer);
						// 如果没消费到消息则暂停1秒
						if (busConfig.getPauseIfNoConsume() > 0 && consumed == 0) {
							Thread.sleep(busConfig.getPauseIfNoConsume());
						}
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
				simpleActionGenerator.setName(busAlias);
				simpleActionGenerator.start(BackpressureStrategy.LATEST);
			}else {
				throw new IllegalStateException("no support streamBroker!");
			}
		}
	}
	
	synchronized void stop() throws Exception {
		if (running) {
			if (streamBroker instanceof AutoConsumeStreamBroker) {
				((AutoConsumeStreamBroker) streamBroker).stopReceive();
			} else {
				simpleActionGenerator.stop();
			}
			streamBroker.destoryChannel();
			terminalConfigurator.stop();
			running = false;
		}
	}
	void setBusConfig(BusConfigBean busConfig) {
		//消费间隔不得小于100ms
		Asserts.check(busConfig.getAccessInterval() > 99, name+"'s accessInterval can not less than 100!");
		this.busConfig = busConfig;
	}

	/**
	 * 系统初始化时调用此方法来设置各path的handler
	 * @param endpoint
	 * @param handler
	 */
	void addEndpointHandler(EndpointBean endpoint, BiFunction<String, xbus.stream.message.payload.BusPayload, xbus.stream.message.payload.BusPayload> handler) {
		if (!endpoints.add(endpoint))
			throw new UnsupportedOperationException("duplicate endpointHandler of path '" + endpoint.getFullPath() + "'");
		endpointHandlers.put(endpoint.getFullPath(), handler);
	}
	/**
	 * 系统初始化时调用此方法来设置terminal下各path的回执消息处理器
	 * @param receiptHandlerRegisters
	 */
	void addReceiptHandler(Collection<ReceiptHandlerRegister> receiptHandlerRegisters) {
		for (ReceiptHandlerRegister receiptHandlerRegister : receiptHandlerRegisters) {
			if (receiptHandlerRegister.registerToBus() == null || receiptHandlerRegister.registerToBus().equals(name)) {
				receiptHandlerRegister.register((terminalName, path, handler) -> {
					CacheableHashMap<String, Consumer<? extends xbus.stream.message.payload.BusPayload>> handlers;
					//配置为全局回执处理器
					if(StringUtils.isEmpty(terminalName)) {
						handlers = globalReceiptHandlers;
					} else {
						handlers = defaultReceiptHandlers.get(terminalName, IFNULL);
					}
					Consumer<? extends xbus.stream.message.payload.BusPayload> existedHandler = handlers.get(path);
					//指定总线名的ReceiptHandlerRegister会覆盖已存在的handler,重复定义会以最后一个为准
					if (existedHandler == null || receiptHandlerRegister.registerToBus() != null) {
						handlers.put(path, handler);
						LOGGER.info("[Bus-{}] register ReceiptHandler : {} for terminal {}'path {} , from registerToBus[{}]", name, handler,terminalName, path,receiptHandlerRegister.registerToBus());
					}
				});
			}
		}
	}
}
