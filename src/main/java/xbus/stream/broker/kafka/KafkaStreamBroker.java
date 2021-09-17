package xbus.stream.broker.kafka;


import xbus.core.config.BusConfigBean;
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
    public KafkaStreamBroker(BusConfigBean busConfig, BrokerConfigBean brokerConfig) {
        super(busConfig,brokerConfig);
    }

    @Override
    public int consume(TerminalNode terminalNode, Function<List<BusMessage>, List<ConsumeReceipt>> consumer) throws RuntimeException {
        return 0;
    }

    @Override
    protected void send(Terminal terminal, BusMessage message) throws RuntimeException {
//TODO
    }

}
