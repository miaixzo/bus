package org.aoju.bus.trace4j.binding.apache.cxf.interceptor;

import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.Utilities;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;


public class TraceRequestInInterceptor extends AbstractTraceInInterceptor {

    public TraceRequestInInterceptor(TraceBackend backend) {
        this(backend, TraceConsts.DEFAULT);
    }

    public TraceRequestInInterceptor(TraceBackend backend, String profile) {
        super(Phase.PRE_INVOKE, TraceFilterConfiguration.Channel.IncomingRequest, backend, profile);
    }

    @Override
    public void handleMessage(Message message) {
        super.handleMessage(message);
        if (shouldHandleMessage(message)) {
            Utilities.generateInvocationIdIfNecessary(backend);
        }
    }

    @Override
    protected boolean shouldHandleMessage(Message message) {
        return !MessageUtils.isRequestor(message);
    }

}
