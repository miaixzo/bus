package org.aoju.bus.trace4j.binding.apache.cxf.interceptor;

import org.aoju.bus.trace4j.Backend;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;

public class TraceResponseInInterceptor extends AbstractTraceInInterceptor {

    public TraceResponseInInterceptor(Backend backend) {
        this(backend, TraceConsts.DEFAULT);
    }

    public TraceResponseInInterceptor(Backend backend, String profile) {
        super(Phase.PRE_INVOKE, TraceFilterConfiguration.Channel.IncomingResponse, backend, profile);
    }

    @Override
    protected boolean shouldHandleMessage(Message message) {
        return MessageUtils.isRequestor(message);
    }

}
