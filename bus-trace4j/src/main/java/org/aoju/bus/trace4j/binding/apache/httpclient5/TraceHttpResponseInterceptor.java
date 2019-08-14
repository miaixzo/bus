package org.aoju.bus.trace4j.binding.apache.httpclient5;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.util.ArrayList;
import java.util.Iterator;
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
        final Iterator<Header> headerIterator = response.headerIterator(TraceConsts.TPIC_HEADER);
        if (headerIterator != null && headerIterator.hasNext()
                && filterConfiguration.shouldProcessContext(TraceFilterConfiguration.Channel.IncomingResponse)) {
            final List<String> stringTraceHeaders = new ArrayList<>();
            while (headerIterator.hasNext()) {
                stringTraceHeaders.add(headerIterator.next().getValue());
            }
            backend.putAll(filterConfiguration.filterDeniedParams(transportSerialization.parse(stringTraceHeaders),
                    TraceFilterConfiguration.Channel.IncomingResponse));
        }
    }

}
