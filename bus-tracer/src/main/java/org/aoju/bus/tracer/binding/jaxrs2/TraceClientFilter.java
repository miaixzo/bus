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
