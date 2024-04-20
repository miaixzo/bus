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
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tracer.Backend;
import org.miaixz.bus.tracer.Builder;
import org.miaixz.bus.tracer.config.TraceFilterConfig;
import org.miaixz.bus.tracer.transport.HttpHeaderTransport;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
abstract class AbstractTraceOutInterceptor extends AbstractPhaseInterceptor<Message> {

    protected final Backend backend;

    private final HttpHeaderTransport httpSerializer;
    private final TraceFilterConfig.Channel channel;

    private String profile;

    public AbstractTraceOutInterceptor(String phase, TraceFilterConfig.Channel channel, Backend backend, String profile) {
        super(phase);
        this.channel = channel;
        this.backend = backend;
        this.profile = profile;
        this.httpSerializer = new HttpHeaderTransport();
    }

    @Override
    public void handleMessage(final Message message) {
        if (shouldHandleMessage(message)) {
            final TraceFilterConfig filterConfiguration = backend.getConfiguration(profile);
            if (!backend.isEmpty() && filterConfiguration.shouldProcessContext(channel)) {
                final Map<String, String> filteredParams = filterConfiguration.filterDeniedParams(backend.copyToMap(), channel);

                Logger.debug("Interceptor handles message!");
                if (Boolean.TRUE.equals(message.getExchange().get(Message.REST_MESSAGE))) {
                    Map<String, List<String>> responseHeaders = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
                    if (null == responseHeaders) {
                        responseHeaders = new HashMap<>();
                        message.put(Message.PROTOCOL_HEADERS, responseHeaders);
                    }

                    final String contextAsHeader = httpSerializer.render(filteredParams);
                    responseHeaders.put(Builder.TPIC_HEADER, Collections.singletonList(contextAsHeader));
                } else {
                    try {
                        final SoapMessage soapMessage = (SoapMessage) message;
                    } catch (NoClassDefFoundError e) {
                        Logger.error("Should handle SOAP-message but it seems that cxf soap dependency is not on the classpath. Unable to add Builder-Headers: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }


    protected abstract boolean shouldHandleMessage(Message message);

}
