/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.tracer.binding.jms;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.Tracer;
import org.aoju.bus.tracer.config.TraceFilterConfig;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.JMSException;
import javax.jms.Message;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class TraceMessageListener {

    private final Backend backend;
    private final HttpHeaderTransport httpHeaderSerialization;

    TraceMessageListener(Backend backend) {
        this.backend = backend;
        this.httpHeaderSerialization = new HttpHeaderTransport();
    }

    public TraceMessageListener() {
        this(Tracer.getBackend());
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
        if (backend.getConfiguration().shouldProcessContext(TraceFilterConfig.Channel.AsyncProcess)) {
            final String encodedTraceContext = message.getStringProperty(Builder.TPIC_HEADER);
            if (null != encodedTraceContext) {
                final Map<String, String> contextFromMessage = httpHeaderSerialization.parse(Collections.singletonList(encodedTraceContext));
                backend.putAll(backend.getConfiguration().filterDeniedParams(contextFromMessage, TraceFilterConfig.Channel.AsyncProcess));
            }
        }
        org.aoju.bus.tracer.Builder.generateInvocationIdIfNecessary(backend);
    }

    void cleanUp() {
        if (backend.getConfiguration().shouldProcessContext(TraceFilterConfig.Channel.AsyncProcess)) {
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
