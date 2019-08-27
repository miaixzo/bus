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
