/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.tracer.binding.apache.cxf.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tracer.Backend;
import org.miaixz.bus.tracer.Builder;
import org.miaixz.bus.tracer.config.TraceFilterConfig;
import org.miaixz.bus.tracer.transport.HttpHeaderTransport;
import org.miaixz.bus.tracer.transport.SoapHeaderTransport;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
abstract class AbstractTraceInInterceptor extends AbstractPhaseInterceptor<Message> {

    protected final Backend backend;
    private final HttpHeaderTransport httpJsonSerializer;
    private final SoapHeaderTransport httpSoapSerializer;
    private final TraceFilterConfig.Channel channel;
    private String profile;

    public AbstractTraceInInterceptor(String phase, TraceFilterConfig.Channel channel, Backend backend,
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
            final TraceFilterConfig filterConfiguration = backend.getConfiguration(profile);

            Logger.debug("Interceptor handles message!");
            if (filterConfiguration.shouldProcessContext(channel)) {
                if (Boolean.TRUE.equals(message.getExchange().get(Message.REST_MESSAGE))) {
                    handleHttpMessage(message, filterConfiguration);
                } else {
                    try {
                        handleSoapMessage((SoapMessage) message, filterConfiguration);
                    } catch (NoClassDefFoundError e) {
                        Logger.error("Should handle SOAP-message but it seems that cxf soap dependency is not on the classpath. Unable to parse Builder-Headers: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void handleHttpMessage(final Message message, final TraceFilterConfig filterConfiguration) {
        final Map<String, List<String>> requestHeaders = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
        if (null != requestHeaders && !requestHeaders.isEmpty()) {
            final List<String> traceHeader = requestHeaders.get(Builder.TPIC_HEADER);

            if (null != traceHeader && !traceHeader.isEmpty()) {
                final Map<String, String> parsedContext = httpJsonSerializer.parse(traceHeader);
                backend.putAll(filterConfiguration.filterDeniedParams(parsedContext, channel));
            }
        }
    }

    private void handleSoapMessage(final SoapMessage message, final TraceFilterConfig filterConfiguration) {
        final Header soapHeader = message.getHeader(Builder.SOAP_HEADER_QNAME);
        if (null != soapHeader) {
            final Map<String, String> parsedContext = httpSoapSerializer.parseTpicHeader((Element) soapHeader.getObject());
            backend.putAll(filterConfiguration.filterDeniedParams(parsedContext, channel));
        }
    }

}
