package xbus.stream.broker.kafka;


import xbus.stream.broker.BrokerConfigBean;
import xbus.stream.broker.ConsumeReceipt;
import xbus.stream.message.BusMessage;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalNode;

import java.util.List;
import java.util.function.Function;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-09-16 14:37
 * @description
 */
public class KafkaStreamBroker extends StreamBrokerInitializer {
    public KafkaStreamBroker(BrokerConfigBean brokerConfig) {
        super(brokerConfig);
    }

    @Override
    protected void send(Terminal terminal, BusMessage message) throws RuntimeException {
//TODO
    }

    @Override
    public void consume(TerminalNode terminalNode, Function<List<BusMessage>, List<ConsumeReceipt>> consumer) throws RuntimeException {
//TODO
    }
}
