package org.aoju.bus.trace4j.binding.jms;

import org.aoju.bus.trace4j.Builder;
import org.aoju.bus.trace4j.Backend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import java.util.Map;

import static org.aoju.bus.trace4j.config.TraceFilterConfiguration.Channel.AsyncDispatch;

public class TraceMessageProducer implements MessageProducer {

    private final MessageProducer delegate;
    private final Backend backend;
    private final HttpHeaderTransport httpHeaderSerialization;

    TraceMessageProducer(MessageProducer delegate, Backend backend) {
        this.delegate = delegate;
        this.backend = backend;
        this.httpHeaderSerialization = new HttpHeaderTransport();
    }

    public TraceMessageProducer(MessageProducer delegate) {
        this.delegate = delegate;
        this.backend = Builder.getBackend();
        this.httpHeaderSerialization = new HttpHeaderTransport();
    }

    protected void writeTraceContextToMessage(Message message) throws JMSException {

        if (!backend.isEmpty() && backend.getConfiguration().shouldProcessContext(AsyncDispatch)) {
            final Map<String, String> filteredContext = backend.getConfiguration().filterDeniedParams(backend.copyToMap(), AsyncDispatch);
            final String contextAsString = httpHeaderSerialization.render(filteredContext);

            message.setStringProperty(TraceConsts.TPIC_HEADER, contextAsString);
        }
    }

    @Override
    public void send(Message message) throws JMSException {
        writeTraceContextToMessage(message);
        delegate.send(message);
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        writeTraceContextToMessage(message);
        delegate.send(message, deliveryMode, priority, timeToLive);
    }

    @Override
    public void send(Destination destination, Message message) throws JMSException {
        writeTraceContextToMessage(message);
        delegate.send(destination, message);
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        writeTraceContextToMessage(message);
        delegate.send(destination, message, deliveryMode, priority, timeToLive);
    }

    @Override
    public boolean getDisableMessageID() throws JMSException {
        return delegate.getDisableMessageID();
    }

    @Override
    public void setDisableMessageID(boolean value) throws JMSException {
        delegate.setDisableMessageID(value);
    }

    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        return delegate.getDisableMessageTimestamp();
    }

    @Override
    public void setDisableMessageTimestamp(boolean value) throws JMSException {
        delegate.setDisableMessageTimestamp(value);
    }

    @Override
    public int getDeliveryMode() throws JMSException {
        return delegate.getDeliveryMode();
    }

    @Override
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        delegate.setDeliveryMode(deliveryMode);
    }

    @Override
    public int getPriority() throws JMSException {
        return delegate.getPriority();
    }

    @Override
    public void setPriority(int defaultPriority) throws JMSException {
        delegate.setPriority(defaultPriority);
    }

    @Override
    public long getTimeToLive() throws JMSException {
        return delegate.getTimeToLive();
    }

    @Override
    public void setTimeToLive(long timeToLive) throws JMSException {
        delegate.setTimeToLive(timeToLive);
    }

    @Override
    public Destination getDestination() throws JMSException {
        return delegate.getDestination();
    }

    @Override
    public void close() throws JMSException {
        delegate.close();
    }

}
