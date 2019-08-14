package org.aoju.bus.trace4j.binding.apache.httpclient;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import java.util.ArrayList;
import java.util.List;


public class TraceHttpResponseInterceptor implements HttpResponseInterceptor {

    private final TraceBackend backend;
    private final HttpHeaderTransport transportSerialization;
    private final String profile;

    public TraceHttpResponseInterceptor() {
        this(TraceConsts.DEFAULT);
    }

    public TraceHttpResponseInterceptor(String profile) {
        this(Trace.getBackend(), profile);
    }

    TraceHttpResponseInterceptor(TraceBackend backend, String profile) {
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
