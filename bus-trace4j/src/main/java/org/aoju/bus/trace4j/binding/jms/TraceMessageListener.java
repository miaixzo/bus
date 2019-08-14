package org.aoju.bus.trace4j.binding.jms;

import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.Utilities;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.JMSException;
import javax.jms.Message;
import java.lang.reflect.Method;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.aoju.bus.trace4j.config.TraceFilterConfiguration.Channel.AsyncProcess;

public final class TraceMessageListener {

    private final TraceBackend backend;
    private final HttpHeaderTransport httpHeaderSerialization;

    TraceMessageListener(TraceBackend backend) {
        this.backend = backend;
        this.httpHeaderSerialization = new HttpHeaderTransport();
    }

    public TraceMessageListener() {
        this(Trace.getBackend());
    }

    @AroundInvoke
    public Object intercept(final InvocationContext ctx) throws Exception {
        final boolean isMdbInvocation = isMessageListenerOnMessageMethod(ctx.getMethod());
        try {
            if (isMdbInvocation) {
                beforeProcessing(extractMessageParameter(ctx.getParameters()));
            }
            return ctx.proceed();
        } finally {
            if (isMdbInvocation) {
                cleanUp();
            }
        }
    }

    public void beforeProcessing(final Message message) throws JMSException {
        if (backend.getConfiguration().shouldProcessContext(AsyncProcess)) {
            final String encodedTraceContext = message.getStringProperty(TraceConsts.TPIC_HEADER);
            if (encodedTraceContext != null) {
                final Map<String, String> contextFromMessage = httpHeaderSerialization.parse(singletonList(encodedTraceContext));
                backend.putAll(backend.getConfiguration().filterDeniedParams(contextFromMessage, AsyncProcess));
            }
        }
        Utilities.generateInvocationIdIfNecessary(backend);
    }

    void cleanUp() {
        if (backend.getConfiguration().shouldProcessContext(AsyncProcess)) {
            backend.clear();
        }
    }

    Message extractMessageParameter(final Object[] parameters) {
        return (Message) parameters[0];
    }

    boolean isMessageListenerOnMessageMethod(final Method method) {
        return "onMessage".equals(method.getName())
                && method.getParameterTypes().length == 1
                && method.getParameterTypes()[0] == Message.class;
    }

}
