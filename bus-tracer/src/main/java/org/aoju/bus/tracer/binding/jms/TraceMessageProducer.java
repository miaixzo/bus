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

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.Tracer;
import org.aoju.bus.tracer.config.TraceFilterConfig;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
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
        this.backend = Tracer.getBackend();
        this.httpHeaderSerialization = new HttpHeaderTransport();
    }

    protected void writeTraceContextToMessage(Message message) throws JMSException {

        if (!backend.isEmpty() && backend.getConfiguration().shouldProcessContext(TraceFilterConfig.Channel.AsyncDispatch)) {
            final Map<String, String> filteredContext = backend.getConfiguration().filterDeniedParams(backend.copyToMap(), TraceFilterConfig.Channel.AsyncDispatch);
            final String contextAsString = httpHeaderSerialization.render(filteredContext);

            message.setStringProperty(Builder.TPIC_HEADER, contextAsString);
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
