package org.aoju.bus.tracer.binding.spring.soap;

import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;


public final class TracendpointInterceptor extends AbstractTraceInterceptor implements EndpointInterceptor {

    public TracendpointInterceptor() {
        this(Builder.getBackend(), TraceConsts.DEFAULT);
    }

    public TracendpointInterceptor(final String profile) {
        this(Builder.getBackend(), profile);
    }

    public TracendpointInterceptor(final Backend backend, final String profile) {
        super(backend, profile);
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object o) {
        parseContextFromSoapHeader(messageContext.getRequest(), TraceFilterConfiguration.Channel.IncomingRequest);

        Builder.generateInvocationIdIfNecessary(backend);
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object o) {
        serializeContextToSoapHeader(messageContext.getResponse(), TraceFilterConfiguration.Channel.OutgoingResponse);
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object o) {
        return handleResponse(messageContext, o);
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object o, Exception e) {
        backend.clear();
    }

}
