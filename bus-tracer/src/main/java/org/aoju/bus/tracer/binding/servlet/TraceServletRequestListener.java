/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.tracer.binding.servlet;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import static org.aoju.bus.tracer.config.TraceFilterConfiguration.Channel.IncomingRequest;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
@WebListener("TraceServletRequestListener to read incoming TPICs into Builder backend")
public final class TraceServletRequestListener implements ServletRequestListener {

    private static final String HTTP_HEADER_NAME = TraceConsts.TPIC_HEADER;

    private final Backend backend;

    private final HttpHeaderTransport transportSerialization;

    protected TraceServletRequestListener(Backend backend, HttpHeaderTransport transportSerialization) {
        this.backend = backend;
        this.transportSerialization = transportSerialization;
    }

    public TraceServletRequestListener() {
        this(Builder.getBackend(), new HttpHeaderTransport());
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent sre) {
        backend.clear();
    }

    @Override
    public void requestInitialized(final ServletRequestEvent sre) {
        final ServletRequest servletRequest = sre.getServletRequest();
        if (servletRequest instanceof HttpServletRequest) {
            httpRequestInitialized((HttpServletRequest) servletRequest);
        }
    }

    private void httpRequestInitialized(final HttpServletRequest request) {
        final TraceFilterConfiguration configuration = backend.getConfiguration();

        if (configuration.shouldProcessContext(IncomingRequest)) {
            final Enumeration<String> headers = request.getHeaders(HTTP_HEADER_NAME);

            if (headers != null && headers.hasMoreElements()) {
                final Map<String, String> contextMap = transportSerialization.parse(Collections.list(headers));
                backend.putAll(backend.getConfiguration().filterDeniedParams(contextMap, IncomingRequest));
            }
        }

        Builder.generateInvocationIdIfNecessary(backend);

        final HttpSession session = request.getSession(false);
        if (session != null) {
            Builder.generateSessionIdIfNecessary(backend, session.getId());
        }
    }
}
