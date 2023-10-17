/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.tracer.binding.jms;

import javax.jms.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
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
