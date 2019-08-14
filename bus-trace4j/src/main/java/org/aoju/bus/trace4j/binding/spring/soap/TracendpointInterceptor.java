package org.aoju.bus.trace4j.binding.spring.soap;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.Utilities;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;


public final class TracendpointInterceptor extends AbstractTraceInterceptor implements EndpointInterceptor {

    public TracendpointInterceptor() {
        this(Trace.getBackend(), TraceConsts.DEFAULT);
    }

    public TracendpointInterceptor(final String profile) {
        this(Trace.getBackend(), profile);
    }

    public TracendpointInterceptor(final TraceBackend backend, final String profile) {
        super(backend, profile);
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object o) {
        parseContextFromSoapHeader(messageContext.getRequest(), TraceFilterConfiguration.Channel.IncomingRequest);

        Utilities.generateInvocationIdIfNecessary(backend);
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
