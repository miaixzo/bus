/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.http;

import java.io.IOException;

import org.miaixz.bus.core.net.HTTP;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Request context filter to initialize and clean up request-scoped data.
 * <p>
 * This filter ensures that each request has a unique request ID and that ThreadLocal variables and caches are properly
 * cleaned up after the request completes to prevent data leakage between requests in thread pools.
 * </p>
 * <p>
 * Uses {@link OncePerRequestFilter} to guarantee the filter is executed only once per request, even in the case of
 * request forwarding or including.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
public class RuntimeContextBindingFilter extends OncePerRequestFilter {

    /**
     * Wraps the request and response if necessary.
     * <p>
     * This method wraps the {@link HttpServletRequest} in a {@link MutableRequestWrapper} for HTTP methods that
     * typically contain a body (POST, PATCH, PUT) to allow for repeatable reads. The {@link HttpServletResponse} is
     * also wrapped to allow for response body manipulation or caching.
     * </p>
     *
     * @param request     The original HTTP request.
     * @param response    The original HTTP response.
     * @param filterChain The filter chain.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException      if an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String method = request.getMethod();
        // Only wrap requests with a body to improve performance.
        if (HTTP.POST.equals(method) || HTTP.PATCH.equals(method) || HTTP.PUT.equals(method)) {
            if (!(request instanceof MutableRequestWrapper)) {
                request = new MutableRequestWrapper(request);
            }
        }
        if (!(response instanceof MutableResponseWrapper)) {
            response = new MutableResponseWrapper(response);
        }
        filterChain.doFilter(request, response);
    }

}
