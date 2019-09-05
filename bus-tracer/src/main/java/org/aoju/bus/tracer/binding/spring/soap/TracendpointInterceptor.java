/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.aoju.bus.tracer.binding.spring.soap;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

/**
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
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
