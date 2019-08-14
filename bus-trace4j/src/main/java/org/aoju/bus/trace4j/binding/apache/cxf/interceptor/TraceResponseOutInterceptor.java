package org.aoju.bus.trace4j.binding.apache.cxf.interceptor;

import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;


public class TraceResponseOutInterceptor extends AbstractTraceOutInterceptor {

    public TraceResponseOutInterceptor(TraceBackend backend) {
        this(backend, TraceConsts.DEFAULT);
    }

    public TraceResponseOutInterceptor(TraceBackend backend, String profile) {
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
