package org.aoju.bus.tracer.binding.spring.soap;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

public final class TraceClientInterceptor extends AbstractTraceInterceptor implements ClientInterceptor {

    public TraceClientInterceptor() {
        this(Builder.getBackend(), TraceConsts.DEFAULT);
    }

    public TraceClientInterceptor(final String profile) {
        this(Builder.getBackend(), profile);
    }

    public TraceClientInterceptor(final Backend backend, final String profile) {
        super(backend, profile);
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        serializeContextToSoapHeader(messageContext.getRequest(), TraceFilterConfiguration.Channel.OutgoingRequest);
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        parseContextFromSoapHeader(messageContext.getResponse(), TraceFilterConfiguration.Channel.IncomingResponse);
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) {
        return handleResponse(messageContext);
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {

    }

}
