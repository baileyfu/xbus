package xbus.stream.broker.kafka;

import xbus.stream.broker.BrokerConfigBean;
import xbus.stream.broker.ManualConsumeStreamBroker;
import xbus.stream.message.MessageCoverter;
import xbus.stream.terminal.TerminalNode;

import java.util.Set;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-09-16 14:30
 * @description
 */
public abstract class StreamBrokerInitializer extends ManualConsumeStreamBroker implements MessageCoverter {

    public StreamBrokerInitializer(BrokerConfigBean brokerConfig) {
        super(brokerConfig);
    }

    @Override
    public void initializeChannel(TerminalNode currentTerminalNode, Set<String> endpointList) throws Exception {
//TODO
    }

    @Override
    public void destoryChannel() throws Exception {
//TODO
    }
}
