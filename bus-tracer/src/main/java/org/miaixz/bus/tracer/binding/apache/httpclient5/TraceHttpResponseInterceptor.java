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
package org.miaixz.bus.tracer.binding.apache.httpclient5;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.miaixz.bus.tracer.Backend;
import org.miaixz.bus.tracer.Builder;
import org.miaixz.bus.tracer.Tracer;
import org.miaixz.bus.tracer.config.TraceFilterConfig;
import org.miaixz.bus.tracer.transport.HttpHeaderTransport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class TraceHttpResponseInterceptor implements HttpResponseInterceptor {

    private final Backend backend;
    private final HttpHeaderTransport transportSerialization;
    private final String profile;

    public TraceHttpResponseInterceptor() {
        this(Builder.DEFAULT);
    }

    public TraceHttpResponseInterceptor(String profile) {
        this(Tracer.getBackend(), profile);
    }

    TraceHttpResponseInterceptor(Backend backend, String profile) {
        this.backend = backend;
        this.profile = profile;
        transportSerialization = new HttpHeaderTransport();
    }

    @Override
    public final void process(final HttpResponse response, final EntityDetails entityDetails, final HttpContext httpContext) {
        final TraceFilterConfig filterConfiguration = backend.getConfiguration(profile);
        final Iterator<Header> headerIterator = response.headerIterator(Builder.TPIC_HEADER);
        if (null != headerIterator && headerIterator.hasNext()
                && filterConfiguration.shouldProcessContext(TraceFilterConfig.Channel.IncomingResponse)) {
            final List<String> stringTraceHeaders = new ArrayList<>();
            while (headerIterator.hasNext()) {
                stringTraceHeaders.add(headerIterator.next().getValue());
            }
            backend.putAll(filterConfiguration.filterDeniedParams(transportSerialization.parse(stringTraceHeaders),
                    TraceFilterConfig.Channel.IncomingResponse));
        }
    }

}
