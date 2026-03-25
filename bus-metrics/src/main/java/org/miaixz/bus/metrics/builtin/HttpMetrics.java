/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.metrics.builtin;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Metrics;
import org.miaixz.bus.metrics.metric.Sample;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Servlet Filter that automatically records HTTP request metrics.
 * <p>
 * Zero Spring dependency — works with any Jakarta Servlet container. Register as a Filter in web.xml or
 * programmatically via {@code FilterRegistration}.
 * <p>
 * URI template normalization is not available at the Servlet level; raw request URI is used with truncation +
 * CardinalityGuard to prevent cardinality explosion. For Spring MVC URI template normalization ({@code /user/123} →
 * {@code /user/{id}}), use the {@code HttpMetricsInterceptor} in bus-starter instead.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HttpMetrics implements Filter {

    /**
     * Servlet request attribute key used to pass the in-flight {@link Sample} through the filter chain.
     */
    private static final String ATTR_SAMPLE = Builder.HTTP_ATTR_SAMPLE;

    /**
     * Intercepts each HTTP request, starts a timer, and records duration/status/method/uri tags on completion.
     *
     * @param request  the incoming servlet request
     * @param response the outgoing servlet response
     * @param chain    the filter chain to invoke
     * @throws IOException      if an I/O error occurs during filtering
     * @throws ServletException if a servlet error occurs during filtering
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest req) || !(response instanceof HttpServletResponse res)) {
            chain.doFilter(request, response);
            return;
        }
        Sample sample = Metrics.timer(Builder.HTTP_SERVER_REQUESTS).start();
        req.setAttribute(ATTR_SAMPLE, sample);
        Throwable caught = null;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException e) {
            caught = e;
            throw e;
        } finally {
            record(sample, req.getMethod(), req.getRequestURI(), res.getStatus(), caught);
        }
    }

    /**
     * Stops the timer sample and records duration, method, URI, status, and exception tags.
     *
     * @param sample the in-flight timer sample to stop
     * @param method HTTP method (e.g. "GET", "POST")
     * @param uri    request URI, truncated if longer than {@link Builder#HTTP_URI_MAX_LENGTH}
     * @param status HTTP response status code
     * @param ex     exception thrown during request handling, or {@code null} if none
     */
    private static void record(Sample sample, String method, String uri, int status, Throwable ex) {
        long durationNs = sample.stop();
        String exceptionName = ex == null ? "none" : ex.getClass().getSimpleName();
        String uriNormalized = uri == null ? "unknown"
                : (uri.length() > Builder.HTTP_URI_MAX_LENGTH ? uri.substring(0, Builder.HTTP_URI_MAX_LENGTH) : uri);

        Metrics.timer(
                Builder.HTTP_SERVER_REQUESTS,
                Builder.TAG_METHOD,
                method,
                Builder.TAG_URI,
                uriNormalized,
                Builder.TAG_STATUS,
                String.valueOf(status),
                Builder.TAG_EXCEPTION,
                exceptionName).record(durationNs, TimeUnit.NANOSECONDS);

        Metrics.meter(
                Builder.HTTP_SERVER_REQUESTS_RATE,
                Builder.TAG_METHOD,
                method,
                Builder.TAG_URI,
                uriNormalized,
                Builder.TAG_STATUS,
                String.valueOf(status)).increment();
    }

}
