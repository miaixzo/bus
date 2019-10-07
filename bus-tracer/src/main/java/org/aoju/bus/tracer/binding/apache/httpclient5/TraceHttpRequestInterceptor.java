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
package org.aoju.bus.tracer.binding.apache.httpclient5;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.6.5
 * @since JDK 1.8
 */
public class TraceHttpRequestInterceptor implements HttpRequestInterceptor {

    private final Backend backend;
    private final HttpHeaderTransport transportSerialization;
    private final String profile;

    public TraceHttpRequestInterceptor() {
        this(TraceConsts.DEFAULT);
    }

    public TraceHttpRequestInterceptor(String profile) {
        this(Builder.getBackend(), profile);
    }

    TraceHttpRequestInterceptor(Backend backend, String profile) {
        this.backend = backend;
        this.transportSerialization = new HttpHeaderTransport();
        this.profile = profile;
    }

    @Override
    public final void process(final HttpRequest httpRequest, final HttpContext httpContext) {
        final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);
        if (!backend.isEmpty() && filterConfiguration.shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingRequest)) {
            final Map<String, String> filteredParams = filterConfiguration.filterDeniedParams(backend.copyToMap(),
                    TraceFilterConfiguration.Channel.OutgoingRequest);
            httpRequest.setHeader(TraceConsts.TPIC_HEADER, transportSerialization.render(filteredParams));
        }
    }

}
