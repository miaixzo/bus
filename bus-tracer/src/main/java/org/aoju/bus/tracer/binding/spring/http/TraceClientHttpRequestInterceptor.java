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
package org.aoju.bus.tracer.binding.spring.http;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.Tracer;
import org.aoju.bus.tracer.config.TraceFilterConfig;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class TraceClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Backend backend;
    private final HttpHeaderTransport transportSerialization;
    private final String profile;

    public TraceClientHttpRequestInterceptor() {
        this(Tracer.getBackend(), new HttpHeaderTransport(), Builder.DEFAULT);
    }

    public TraceClientHttpRequestInterceptor(String profile) {
        this(Tracer.getBackend(), new HttpHeaderTransport(), profile);
    }

    public TraceClientHttpRequestInterceptor(Backend backend, HttpHeaderTransport transportSerialization, String profile) {
        this.backend = backend;
        this.transportSerialization = transportSerialization;
        this.profile = profile;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        preRequest(request);
        final ClientHttpResponse response = execution.execute(request, body);
        postResponse(response);
        return response;
    }

    private void preRequest(final HttpRequest request) {
        final TraceFilterConfig filterConfiguration = backend.getConfiguration(profile);
        if (!backend.isEmpty() && filterConfiguration.shouldProcessContext(TraceFilterConfig.Channel.OutgoingRequest)) {
            final Map<String, String> filteredParams = filterConfiguration.filterDeniedParams(backend.copyToMap(), TraceFilterConfig.Channel.OutgoingRequest);
            request.getHeaders().add(Builder.TPIC_HEADER, transportSerialization.render(filteredParams));
        }
    }

    private void postResponse(ClientHttpResponse response) {
        final List<String> headers = response.getHeaders().get(Builder.TPIC_HEADER);
        if (null != headers) {
            final TraceFilterConfig filterConfiguration = backend.getConfiguration(profile);

            if (filterConfiguration.shouldProcessContext(TraceFilterConfig.Channel.IncomingResponse)) {
                backend.putAll(filterConfiguration.filterDeniedParams(transportSerialization.parse(headers), TraceFilterConfig.Channel.IncomingResponse));
            }
        }
    }

}
