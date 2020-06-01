/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.tracer.binding.servlet;

import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.HttpHeaderTransport;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.9.6
 * @since JDK 1.8+
 */
@WebFilter(filterName = "TraceFilter", urlPatterns = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class TraceFilter implements Filter {

    public static final String PROFILE_INIT_PARAM = "profile";

    private static final String HTTP_HEADER_NAME = TraceConsts.TPIC_HEADER;
    private final Backend backend;
    private final HttpHeaderTransport transportSerialization;
    private String profile = TraceConsts.DEFAULT;

    public TraceFilter() {
        this(Builder.getBackend(), new HttpHeaderTransport());
    }

    TraceFilter(Backend backend, HttpHeaderTransport transportSerialization) {
        this.backend = backend;
        this.transportSerialization = transportSerialization;
    }

    @Override
    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            doFilterHttp((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    final void doFilterHttp(final HttpServletRequest request, final HttpServletResponse response,
                            final FilterChain filterChain) throws IOException, ServletException {

        final TraceFilterConfiguration configuration = backend.getConfiguration(profile);

        try {
            writeContextToResponse(response, configuration);
            filterChain.doFilter(request, response);
        } finally {
            if (!response.isCommitted()) {
                writeContextToResponse(response, configuration);
            }
        }
    }

    private void writeContextToResponse(final HttpServletResponse response, final TraceFilterConfiguration configuration) {
        if (!backend.isEmpty() && configuration.shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingResponse)) {
            final Map<String, String> filteredContext = backend.getConfiguration(profile).filterDeniedParams(backend.copyToMap(), TraceFilterConfiguration.Channel.OutgoingResponse);
            response.setHeader(HTTP_HEADER_NAME, transportSerialization.render(filteredContext));
        }
    }

    @Override
    public final void init(final FilterConfig filterConfig) {
        final String profileInitParameter = filterConfig.getInitParameter(PROFILE_INIT_PARAM);
        if (profileInitParameter != null) {
            profile = profileInitParameter;
        }
    }

    @Override
    public final void destroy() {
    }

}
