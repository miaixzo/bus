package org.aoju.bus.tracer.binding.jaxrs2;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;

import static org.aoju.bus.tracer.config.TraceFilterConfiguration.Channel.IncomingRequest;
import static org.aoju.bus.tracer.config.TraceFilterConfiguration.Channel.OutgoingResponse;

@Provider
public class TraceContainerFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final Backend backend;
    private final HttpHeaderTransport transportSerialization;

    public TraceContainerFilter() {
        this(Builder.getBackend());
    }

    TraceContainerFilter(Backend backend) {
        this.backend = backend;
        this.transportSerialization = new HttpHeaderTransport();
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) {

        if (backend.getConfiguration().shouldProcessContext(IncomingRequest)) {
            final List<String> serializedTraceHeaders = containerRequestContext.getHeaders().get(TraceConsts.TPIC_HEADER);
            if (serializedTraceHeaders != null && !serializedTraceHeaders.isEmpty()) {
                final Map<String, String> parsed = transportSerialization.parse(serializedTraceHeaders);
                backend.putAll(backend.getConfiguration().filterDeniedParams(parsed, IncomingRequest));
            }
        }

        Builder.generateInvocationIdIfNecessary(backend);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        if (backend.getConfiguration().shouldProcessContext(OutgoingResponse)) {
            final Map<String, String> filtered = backend.getConfiguration().filterDeniedParams(backend.copyToMap(), OutgoingResponse);
            responseContext.getHeaders().putSingle(TraceConsts.TPIC_HEADER, transportSerialization.render(filtered));
        }

        backend.clear();
    }

}
