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
package org.aoju.bus.tracer.binding.servlet;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.Tracer;
import org.aoju.bus.tracer.config.TraceFilterConfig;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
@WebListener("TraceServletRequestListener to read incoming TPICs into Builder backend")
public final class TraceServletRequestListener implements ServletRequestListener {

    private static final String HTTP_HEADER_NAME = Builder.TPIC_HEADER;

    private final Backend backend;

    private final HttpHeaderTransport transportSerialization;

    protected TraceServletRequestListener(Backend backend, HttpHeaderTransport transportSerialization) {
        this.backend = backend;
        this.transportSerialization = transportSerialization;
    }

    public TraceServletRequestListener() {
        this(Tracer.getBackend(), new HttpHeaderTransport());
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
        final TraceFilterConfig configuration = backend.getConfiguration();

        if (configuration.shouldProcessContext(TraceFilterConfig.Channel.IncomingRequest)) {
            final Enumeration<String> headers = request.getHeaders(HTTP_HEADER_NAME);

            if (null != headers && headers.hasMoreElements()) {
                final Map<String, String> contextMap = transportSerialization.parse(Collections.list(headers));
                backend.putAll(backend.getConfiguration().filterDeniedParams(contextMap, TraceFilterConfig.Channel.IncomingRequest));
            }
        }

        org.aoju.bus.tracer.Builder.generateInvocationIdIfNecessary(backend);

        final HttpSession session = request.getSession(false);
        if (null != session) {
            org.aoju.bus.tracer.Builder.generateSessionIdIfNecessary(backend, session.getId());
        }
    }

}
