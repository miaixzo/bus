/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.tracer.binding.apache.cxf.interceptor;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;
import org.aoju.bus.tracer.transport.SoapHeaderTransport;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.6.0
 * @since JDK 1.8
 */
abstract class AbstractTraceInInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTraceInInterceptor.class);
    protected final Backend backend;
    private final HttpHeaderTransport httpJsonSerializer;
    private final SoapHeaderTransport httpSoapSerializer;
    private final TraceFilterConfiguration.Channel channel;
    private String profile;

    public AbstractTraceInInterceptor(String phase, TraceFilterConfiguration.Channel channel, Backend backend,
                                      String profile) {
        super(phase);
        this.channel = channel;
        this.backend = backend;
        this.profile = profile;
        this.httpJsonSerializer = new HttpHeaderTransport();
        this.httpSoapSerializer = new SoapHeaderTransport();
    }

    protected abstract boolean shouldHandleMessage(Message message);

    @Override
    public void handleMessage(final Message message) {
        if (shouldHandleMessage(message)) {
            final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);

            LOGGER.debug("Interceptor handles message!");
            if (filterConfiguration.shouldProcessContext(channel)) {
                if (Boolean.TRUE.equals(message.getExchange().get(Message.REST_MESSAGE))) {
                    handleHttpMessage(message, filterConfiguration);
                } else {
                    try {
                        handleSoapMessage((SoapMessage) message, filterConfiguration);
                    } catch (NoClassDefFoundError e) {
                        LOGGER.error("Should handle SOAP-message but it seems that cxf soap dependency is not on the classpath. Unable to parse Builder-Headers: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void handleHttpMessage(final Message message, final TraceFilterConfiguration filterConfiguration) {
        final Map<String, List<String>> requestHeaders = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            final List<String> TraceHeader = requestHeaders.get(TraceConsts.TPIC_HEADER);

            if (TraceHeader != null && !TraceHeader.isEmpty()) {
                final Map<String, String> parsedContext = httpJsonSerializer.parse(TraceHeader);
                backend.putAll(filterConfiguration.filterDeniedParams(parsedContext, channel));
            }
        }
    }

    private void handleSoapMessage(final SoapMessage message, final TraceFilterConfiguration filterConfiguration) {
        final Header soapHeader = message.getHeader(TraceConsts.SOAP_HEADER_QNAME);
        if (soapHeader != null) {
            final Map<String, String> parsedContext = httpSoapSerializer.parseTpicHeader((Element) soapHeader.getObject());
            backend.putAll(filterConfiguration.filterDeniedParams(parsedContext, channel));
        }
    }

}
