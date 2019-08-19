package org.aoju.bus.tracer.binding.apache.httpclient;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import java.util.Map;


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
