/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.http;

import java.io.IOException;

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.spring.ContextBuilder;
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
     * This method initializes the request context, wraps the {@link HttpServletRequest} in a
     * {@link MutableRequestWrapper} for HTTP methods that typically contain a body (POST, PATCH, PUT) to allow for
     * repeatable reads. The {@link HttpServletResponse} is also wrapped to allow for response body manipulation or
     * caching.
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
        try {
            // Initialize the request context (generates request ID and stores in ThreadLocal)
            ContextBuilder.init();

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
        } finally {
            // Clean up the request context (removes ThreadLocal and clears cache)
            ContextBuilder.clear();
        }
    }

}
