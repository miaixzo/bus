/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.tracer.binding.spring.web;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8+
 */
@Component
public final class TraceInterceptor implements HandlerInterceptor {

    private final Backend backend;
    private final HttpHeaderTransport httpHeaderSerialization;
    private String outgoingHeaderName = TraceConsts.TPIC_HEADER;
    private String incomingHeaderName = TraceConsts.TPIC_HEADER;
    private String profileName;

    public TraceInterceptor() {
        this(Builder.getBackend());
    }

    public TraceInterceptor(Backend backend) {
        this.backend = backend;
        httpHeaderSerialization = new HttpHeaderTransport();
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object o) {

        final TraceFilterConfiguration configuration = backend.getConfiguration(profileName);

        if (configuration.shouldProcessContext(TraceFilterConfiguration.Channel.IncomingRequest)) {
            final Enumeration<String> headers = request.getHeaders(incomingHeaderName);
            if (headers != null && headers.hasMoreElements()) {
                final Map<String, String> parsedContext = httpHeaderSerialization.parse(Collections.list(headers));
                backend.putAll(configuration.filterDeniedParams(parsedContext, TraceFilterConfiguration.Channel.IncomingResponse));
            }
        }

        Builder.generateInvocationIdIfNecessary(backend);

        final HttpSession session = request.getSession(false);
        if (session != null) {
            Builder.generateSessionIdIfNecessary(backend, session.getId());
        }

        writeHeaderIfUncommitted(response);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) {
        try {
            writeHeaderIfUncommitted(response);
        } finally {
            backend.clear();
        }
    }

    private void writeHeaderIfUncommitted(HttpServletResponse response) {
        if (!response.isCommitted() && !backend.isEmpty()) {
            final TraceFilterConfiguration configuration = backend.getConfiguration(profileName);

            if (configuration.shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingResponse)) {
                final Map<String, String> filteredContext = configuration.filterDeniedParams(backend.copyToMap(), TraceFilterConfiguration.Channel.OutgoingResponse);
                response.setHeader(outgoingHeaderName, httpHeaderSerialization.render(filteredContext));
            }
        }
    }

    public void setOutgoingHeaderName(String outgoingHeaderName) {
        this.outgoingHeaderName = outgoingHeaderName;
    }

    public void setIncomingHeaderName(String incomingHeaderName) {
        this.incomingHeaderName = incomingHeaderName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

}
