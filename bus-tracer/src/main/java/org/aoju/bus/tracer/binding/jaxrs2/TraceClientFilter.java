/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.tracer.binding.jaxrs2;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
@Provider
public class TraceClientFilter implements ClientRequestFilter, ClientResponseFilter {

    private final Backend backend;
    private final HttpHeaderTransport transportSerialization;

    public TraceClientFilter() {
        this(Builder.getBackend());
    }

    TraceClientFilter(Backend backend) {
        this.backend = backend;
        this.transportSerialization = new HttpHeaderTransport();
    }

    @Override
    public void filter(final ClientRequestContext requestContext) {
        if (!backend.isEmpty() && backend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingRequest)) {
            final Map<String, String> filtered = backend.getConfiguration().filterDeniedParams(backend.copyToMap(), TraceFilterConfiguration.Channel.OutgoingRequest);
            requestContext.getHeaders().putSingle(TraceConsts.TPIC_HEADER, transportSerialization.render(filtered));
        }
    }

    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
        final List<String> serializedHeaders = responseContext.getHeaders().get(TraceConsts.TPIC_HEADER);
        if (serializedHeaders != null && backend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.IncomingResponse)) {
            final Map<String, String> parsed = transportSerialization.parse(serializedHeaders);
            backend.putAll(backend.getConfiguration().filterDeniedParams(parsed, TraceFilterConfiguration.Channel.IncomingResponse));
        }
    }

}
