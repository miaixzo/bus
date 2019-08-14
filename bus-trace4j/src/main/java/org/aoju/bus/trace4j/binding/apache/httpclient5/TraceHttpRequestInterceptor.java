package org.aoju.bus.trace4j.binding.apache.httpclient5;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.util.Map;

public class TraceHttpRequestInterceptor implements HttpRequestInterceptor {

    private final TraceBackend backend;
    private final HttpHeaderTransport transportSerialization;
    private final String profile;

    public TraceHttpRequestInterceptor() {
        this(TraceConsts.DEFAULT);
    }

    public TraceHttpRequestInterceptor(String profile) {
        this(Trace.getBackend(), profile);
    }

    TraceHttpRequestInterceptor(TraceBackend backend, String profile) {
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
