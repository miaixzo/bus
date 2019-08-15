package org.aoju.bus.trace4j.binding.apache.cxf.interceptor;

import org.aoju.bus.trace4j.Builder;
import org.aoju.bus.trace4j.Backend;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;


public class TraceRequestInInterceptor extends AbstractTraceInInterceptor {

    public TraceRequestInInterceptor(Backend backend) {
        this(backend, TraceConsts.DEFAULT);
    }

    public TraceRequestInInterceptor(Backend backend, String profile) {
        super(Phase.PRE_INVOKE, TraceFilterConfiguration.Channel.IncomingRequest, backend, profile);
    }

    @Override
    public void handleMessage(Message message) {
        super.handleMessage(message);
        if (shouldHandleMessage(message)) {
            Builder.generateInvocationIdIfNecessary(backend);
        }
    }

    @Override
    protected boolean shouldHandleMessage(Message message) {
        return !MessageUtils.isRequestor(message);
    }

}
