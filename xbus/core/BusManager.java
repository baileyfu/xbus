package xbus.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.http.util.Asserts;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import commons.fun.NAFunction;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.schedulers.Schedulers;
import xbus.BusLoggerHolder;
import xbus.em.MessageType;
import xbus.em.PostMode;
import xbus.stream.broker.AbstractStreamBroker;
import xbus.stream.broker.StreamBroker;
import xbus.stream.message.BusMessage;
import xbus.stream.message.OriginalBusMessage;
import xbus.stream.message.ReceiptBusMessage;
import xbus.stream.message.payload.BusPayload;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalConfigurator;

/**
 * 总线管理器<br/>
 * 单例实现;系统关闭时会释放资源
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-30 09:28
 */
public final class BusManager implements BusLoggerHolder {
	private static BusManager instance;
	
	private StreamBroker streamBroker;
	private TerminalConfigurator terminalConfigurator;
	private Map<String, BiFunction<String,BusPayload, BusPayload>> endpointHandlers;
	private Map<String, Consumer<BusPayload>> endpointReplyHandlers;
	private boolean running;
	private FlowableEmitter<BusMessage> emitter;
	private Subscription subscription;

	protected BusManager(StreamBroker streamBroker, TerminalConfigurator terminalConfigurator) {
		this.streamBroker = streamBroker;
		this.terminalConfigurator = terminalConfigurator;
		endpointHandlers = new HashMap<>();
		endpointReplyHandlers = new HashMap<>();
		running = false;
		emitter = null;
		subscription = null;
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
	 * 按指定发送模式发送给指定终端
	 * 
	 * @param terminalName
	 * @param message
	 * @param postMode
	 */
	public void post(String terminalName, BusMessage message, PostMode postMode) {
		Asserts.notEmpty(terminalName, "terminalName");
		Terminal target = terminalConfigurator.getTerminal(terminalName);
		Asserts.notNull(target, "terminal(" + terminalName + ")");
		Terminal terminal=postMode.buildTerminal();
		terminal.setName(target.getName());
		terminal.referNodes(target);
		streamBroker.produce(terminal, pretreat(message));
	}
	/**
	 * 按节点发送策略将消息发送到指定终端
	 * 
	 * @param terminalNames
	 * @param path
	 * @param message
	 * @param postMode
	 */
	public void post(Set<String> terminalNames, BusMessage message, PostMode postMode) {
		if (terminalNames == null || terminalNames.size() == 0) {
			throw new IllegalStateException("terminalNames can not be empty");
		}
		Terminal[] terminals=new Terminal[terminalNames.size()];
		int i=0;
		for (String terminalName:terminalNames) {
			Terminal target = terminalConfigurator.getTerminal(terminalName);
			Asserts.notNull(target, "terminal(" + terminalName + ")");
			terminals[i]=postMode.buildTerminal();
			terminals[i].setName(target.getName());
			terminals[i++].referNodes(target);
		}
		streamBroker.produce(terminals, pretreat(message));
	}

	/**
	 * 处理接收到的消息
	 * 
	 * @param message
	 * @throws Exception
	 */
	private void receive(BusMessage message) throws Exception {
		String sourceTerminalName = message.getSourceTerminal();
		String path = message.getPath();
		if(message.getMessageType()==MessageType.ORIGINAL){
			OriginalBusMessage originalMessage = (OriginalBusMessage) message;
			BiFunction<String,BusPayload, BusPayload> handler = endpointHandlers.get(path);
			Asserts.check(handler != null, "the originalBusMessageHandler of path '" + path + "' of " + sourceTerminalName + " does not exist!");
			BusPayload receipt = handler.apply(originalMessage.getSourceTerminal(), originalMessage.getPayLoad());
			if (originalMessage.isRequireReceipt()) {
				Asserts.check(receipt != null, "the path '" + path + "' of " + sourceTerminalName + "' require receipt , but the receipt is null!");
				Terminal sourceTerminal = terminalConfigurator.getTerminal(sourceTerminalName);
				Asserts.check(sourceTerminal != null, "receipt error! the source terminal '" + sourceTerminalName + "' does not exist!");
				streamBroker.produce(sourceTerminal, new ReceiptBusMessage(receipt));
			}
		}else{
			Consumer<BusPayload> handler = endpointReplyHandlers.get(path);
			Asserts.check(handler != null, "the receiptBusMessageHandler of path '" + path + "' of " + sourceTerminalName + " does not exist!");
			handler.accept(message.getPayLoad());
		}
	}
	/**
	 * 生产/消费失败不可回滚/重试
	 */
	synchronized void start() {
		doStartWithRx(((AbstractStreamBroker) streamBroker).isConsumeRetryAble() ? 
				() -> {
					streamBroker.consume((busMessage) -> {
						try {
							receive(busMessage);
							return true;
						} catch (Exception e) {
							LOGGER.error(BusManager.class + " receive error ! xbus will rollback this consumption and try consume again .", e);
						}
						return false;
					});
				} : () -> {
					BusMessage message = streamBroker.consume();
					if (message != null) {
						emitter.onNext(message);
						subscription.request(Long.MAX_VALUE);
					}
				});
	}
	private void doStartWithRx(NAFunction doEmit) {
		if (!running) {
			stop();
			running = true;
			Flowable.<BusMessage>create((emt) -> {
						this.emitter = emt;
						while (running) {
							try {
								doEmit.apply();
							} catch (Exception e) {
								LOGGER.error(BusManager.class + " consume error !", e);
								// 出错后暂停100ms
								try {
									Thread.sleep(100l);
								} catch (InterruptedException ie) {
									LOGGER.error(BusManager.class + " Thread.sleep() error", ie);
								}
							}
						}
					}, BackpressureStrategy.BUFFER)
					.observeOn(Schedulers.newThread())
					.subscribeOn(Schedulers.io())
					.subscribe(new Subscriber<BusMessage>(){
						@Override
						public void onSubscribe(Subscription s) {
							subscription = s;
						}
						@Override
						public void onNext(BusMessage message) {
							try {
								receive(message);
							} catch (Exception e) {
								LOGGER.error(BusManager.class
										+ " receive error ! there is a advise that you had better catch the Exception in yourown endpoint method .", e);
							}
						}
						@Override
						public void onError(Throwable t) {
							LOGGER.error(BusManager.class + " onError()", t);
						}
						@Override
						public void onComplete() {
							LOGGER.info(BusManager.class + " onComplete(");
						}
					});
		}
	}
	
	synchronized void stop() {
		running = false;
		if (emitter != null)
			emitter.onComplete();
		emitter = null;
		if (subscription != null)
			subscription.cancel();
		subscription = null;
	}
	public static synchronized void create(StreamBroker streamBroker, TerminalConfigurator terminalConfigurator){
		if (instance == null) {
			instance = new BusManager(streamBroker, terminalConfigurator);
		}
	}
	public static BusManager getInstance() {
		return instance;
	}
}
