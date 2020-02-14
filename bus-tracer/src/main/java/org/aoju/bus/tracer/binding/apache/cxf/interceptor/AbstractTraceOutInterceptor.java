/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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

import org.aoju.bus.logger.Logger;
import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;
import org.aoju.bus.tracer.transport.jaxb.TpicMap;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

import javax.xml.bind.JAXBException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.5.9
 * @since JDK 1.8+
 */
abstract class AbstractTraceOutInterceptor extends AbstractPhaseInterceptor<Message> {

    protected final Backend backend;

    private final HttpHeaderTransport httpSerializer;
    private final TraceFilterConfiguration.Channel channel;

    private String profile;

    public AbstractTraceOutInterceptor(String phase, TraceFilterConfiguration.Channel channel, Backend backend, String profile) {
        super(phase);
        this.channel = channel;
        this.backend = backend;
        this.profile = profile;
        this.httpSerializer = new HttpHeaderTransport();
    }

    @Override
    public void handleMessage(final Message message) {
        if (shouldHandleMessage(message)) {
            final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);
            if (!backend.isEmpty() && filterConfiguration.shouldProcessContext(channel)) {
                final Map<String, String> filteredParams = filterConfiguration.filterDeniedParams(backend.copyToMap(), channel);

                Logger.debug("Interceptor handles message!");
                if (Boolean.TRUE.equals(message.getExchange().get(Message.REST_MESSAGE))) {
                    Map<String, List<String>> responseHeaders = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
                    if (responseHeaders == null) {
                        responseHeaders = new HashMap<>();
                        message.put(Message.PROTOCOL_HEADERS, responseHeaders);
                    }

                    final String contextAsHeader = httpSerializer.render(filteredParams);
                    responseHeaders.put(TraceConsts.TPIC_HEADER, Collections.singletonList(contextAsHeader));
                } else {
                    try {
                        final SoapMessage soapMessage = (SoapMessage) message;
                        addSoapHeader(filteredParams, soapMessage);
                    } catch (NoClassDefFoundError e) {
                        Logger.error("Should handle SOAP-message but it seems that cxf soap dependency is not on the classpath. Unable to add Builder-Headers: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void addSoapHeader(Map<String, String> filteredParams, SoapMessage soapMessage) {
        try {
            final Header tpicHeader = new Header(TraceConsts.SOAP_HEADER_QNAME, TpicMap.wrap(filteredParams),
                    new JAXBDataBinding(TpicMap.class));
            soapMessage.getHeaders().add(tpicHeader);
        } catch (JAXBException e) {
            Logger.warn("Error occured during Builder soap header creation: {}", e.getMessage());
            Logger.debug("Detailed exception", e);
        }
    }

    protected abstract boolean shouldHandleMessage(Message message);

}
