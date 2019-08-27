package org.aoju.bus.tracer.binding.jms;

import javax.jms.*;

public final class TraceTopicPublisher implements TopicPublisher {

    private final TraceMessageProducer messageProducer;
    private final TopicPublisher delegate;

    public TraceTopicPublisher(TraceMessageProducer messageProducer, TopicPublisher delegate) {
        this.messageProducer = messageProducer;
        this.delegate = delegate;
    }

    @Override
    public Topic getTopic() throws JMSException {
        return delegate.getTopic();
    }

    @Override
    public void publish(Message message) throws JMSException {
        messageProducer.writeTraceContextToMessage(message);
        delegate.publish(message);
    }

    @Override
    public void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        messageProducer.writeTraceContextToMessage(message);
        delegate.publish(message, deliveryMode, priority, timeToLive);
    }

    @Override
    public void publish(Topic topic, Message message) throws JMSException {
        messageProducer.writeTraceContextToMessage(message);
        delegate.publish(topic, message);
    }

    @Override
    public void publish(Topic topic, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        messageProducer.writeTraceContextToMessage(message);
        delegate.publish(topic, message, deliveryMode, priority, timeToLive);
    }

    @Override
    public void send(Message message) throws JMSException {
        messageProducer.send(message);
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        messageProducer.send(message, deliveryMode, priority, timeToLive);
    }

    @Override
    public void send(Destination destination, Message message) throws JMSException {
        messageProducer.send(destination, message);
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        messageProducer.send(destination, message, deliveryMode, priority, timeToLive);
    }

    @Override
    public boolean getDisableMessageID() throws JMSException {
        return messageProducer.getDisableMessageID();
    }

    @Override
    public void setDisableMessageID(boolean value) throws JMSException {
        messageProducer.setDisableMessageID(value);
    }

    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        return messageProducer.getDisableMessageTimestamp();
    }

    @Override
    public void setDisableMessageTimestamp(boolean value) throws JMSException {
        messageProducer.setDisableMessageTimestamp(value);
    }

    @Override
    public int getDeliveryMode() throws JMSException {
        return messageProducer.getDeliveryMode();
    }

    @Override
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        messageProducer.setDeliveryMode(deliveryMode);
    }

    @Override
    public int getPriority() throws JMSException {
        return messageProducer.getPriority();
    }

    @Override
    public void setPriority(int defaultPriority) throws JMSException {
        messageProducer.setPriority(defaultPriority);
    }

    @Override
    public long getTimeToLive() throws JMSException {
        return messageProducer.getTimeToLive();
    }

    @Override
    public void setTimeToLive(long timeToLive) throws JMSException {
        messageProducer.setTimeToLive(timeToLive);
    }

    @Override
    public Destination getDestination() throws JMSException {
        return messageProducer.getDestination();
    }

    @Override
    public void close() throws JMSException {
        messageProducer.close();
    }

}
