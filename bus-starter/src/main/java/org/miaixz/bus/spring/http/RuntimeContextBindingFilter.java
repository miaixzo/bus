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
package org.miaixz.bus.spring.http;

import java.io.IOException;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.options.WrapperRuntimeOptions;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Request context filter to initialize and clean up request-scoped data.
 * <p>
 * This filter ensures that each request has a unique request ID and that ThreadLocal variables and caches are properly
 * cleaned up after the request completes to prevent data leakage between requests in thread pools.
 * </p>
 * <p>
 * Implements once-per-request filtering directly so initialization does not emit Spring's inherited non-directional
 * filter debug log.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Component
public class RuntimeContextBindingFilter implements Filter {

    /**
     * Suffix that marks requests already processed by this filter.
     */
    private static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";

    /**
     * Runtime wrapper compatibility snapshot used to decide whether the current request should be wrapped.
     */
    private final WrapperRuntimeOptions options;

    /**
     * Creates a filter using the current shared {@link WrapperRuntimeOptions} snapshot.
     */
    public RuntimeContextBindingFilter() {
        this(WrapperRuntimeOptions.of());
    }

    /**
     * Creates a filter with an explicit runtime compatibility snapshot.
     *
     * @param options The runtime compatibility options. If {@code null}, the current shared snapshot is used.
     */
    public RuntimeContextBindingFilter(WrapperRuntimeOptions options) {
        this.options = options == null ? WrapperRuntimeOptions.of() : options;
    }

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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            throw new ServletException("RuntimeContextBindingFilter only supports HTTP requests");
        }

        String alreadyFilteredAttributeName = getAlreadyFilteredAttributeName();
        boolean hasAlreadyFilteredAttribute = request.getAttribute(alreadyFilteredAttributeName) != null;

        if (skipDispatch(httpRequest) || hasAlreadyFilteredAttribute) {
            filterChain.doFilter(request, response);
            return;
        }

        request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE);
        try {
            doFilterInternal(httpRequest, httpResponse, filterChain);
        } finally {
            request.removeAttribute(alreadyFilteredAttributeName);
        }
    }

    /**
     * Wraps the request and response if necessary.
     *
     * @param request     The original HTTP request.
     * @param response    The original HTTP response.
     * @param filterChain The filter chain.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException      if an I/O error occurs.
     */
    private void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Logger.debug(
                    true,
                    "Starter",
                    "component=http, Runtime context binding started: method={}, uri={}, dispatcher={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getDispatcherType());
            ContextBuilder.init();

            if (this.options.shouldWrap(request) && !(request instanceof MutableRequestWrapper)) {
                request = new MutableRequestWrapper(request);
            }
            if (!(response instanceof MutableResponseWrapper)) {
                response = new MutableResponseWrapper(response);
            }
            filterChain.doFilter(request, response);
            Logger.debug(
                    false,
                    "Starter",
                    "component=http, Runtime context binding completed: method={}, uri={}, status={}, requestWrapped={}, responseWrapped={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    request instanceof MutableRequestWrapper,
                    response instanceof MutableResponseWrapper);
        } finally {
            ContextBuilder.clear();
        }
    }

    /**
     * Returns the request attribute used to detect repeat filtering.
     *
     * @return The already-filtered request attribute name.
     */
    private String getAlreadyFilteredAttributeName() {
        return RuntimeContextBindingFilter.class.getName() + ALREADY_FILTERED_SUFFIX;
    }

    /**
     * Determines whether async or error dispatches should bypass this filter.
     *
     * @param request The current HTTP request.
     * @return {@code true} when this dispatch should bypass the filter.
     */
    private boolean skipDispatch(HttpServletRequest request) {
        return DispatcherType.ASYNC.equals(request.getDispatcherType())
                || request.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE) != null;
    }

}
