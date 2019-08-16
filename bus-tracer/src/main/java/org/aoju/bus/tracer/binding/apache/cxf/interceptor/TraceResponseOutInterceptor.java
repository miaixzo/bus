package org.aoju.bus.tracer.binding.apache.cxf.interceptor;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;


public class TraceResponseOutInterceptor extends AbstractTraceOutInterceptor {

    public TraceResponseOutInterceptor(Backend backend) {
        this(backend, TraceConsts.DEFAULT);
    }

    public TraceResponseOutInterceptor(Backend backend, String profile) {
        super(Phase.USER_LOGICAL, TraceFilterConfiguration.Channel.OutgoingResponse, backend, profile);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        super.handleMessage(message);
        if (shouldHandleMessage(message)) {
            backend.clear();
        }
    }

    @Override
    protected boolean shouldHandleMessage(Message message) {
        return !MessageUtils.isRequestor(message);
    }

}
