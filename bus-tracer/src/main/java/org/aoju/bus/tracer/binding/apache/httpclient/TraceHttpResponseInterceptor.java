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
package org.aoju.bus.tracer.binding.apache.httpclient;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public class TraceHttpResponseInterceptor implements HttpResponseInterceptor {

    private final Backend backend;
    private final HttpHeaderTransport transportSerialization;
    private final String profile;

    public TraceHttpResponseInterceptor() {
        this(TraceConsts.DEFAULT);
    }

    public TraceHttpResponseInterceptor(String profile) {
        this(Builder.getBackend(), profile);
    }

    TraceHttpResponseInterceptor(Backend backend, String profile) {
        this.backend = backend;
        this.profile = profile;
        transportSerialization = new HttpHeaderTransport();
    }

    @Override
    public final void process(HttpResponse response, HttpContext context) {
        final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);
        final Header[] responseHeaders = response.getHeaders(TraceConsts.TPIC_HEADER);
        if (responseHeaders != null && responseHeaders.length > 0
                && filterConfiguration.shouldProcessContext(TraceFilterConfiguration.Channel.IncomingResponse)) {
            final List<String> stringTraceHeaders = new ArrayList<>();
            for (Header header : responseHeaders) {
                stringTraceHeaders.add(header.getValue());
            }
            backend.putAll(filterConfiguration.filterDeniedParams(transportSerialization.parse(stringTraceHeaders),
                    TraceFilterConfiguration.Channel.IncomingResponse));
        }
    }

}
