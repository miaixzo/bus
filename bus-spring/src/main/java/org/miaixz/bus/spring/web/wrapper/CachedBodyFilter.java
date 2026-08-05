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
package org.miaixz.bus.spring.web.wrapper;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;

import org.miaixz.bus.core.lang.exception.RelevantException;

/**
 * Applies optional hard-bounded request and response body caching.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CachedBodyFilter implements Filter, Ordered {

    /**
     * Immutable cache policy for this filter.
     */
    private final BodyCacheOptions options;

    /**
     * Creates a body-cache filter for one Context's immutable options.
     *
     * @param options immutable body-cache policy
     */
    public CachedBodyFilter(BodyCacheOptions options) {
        this.options = Objects.requireNonNull(options, "options");
    }

    /**
     * Runs body caching before request-context credential resolution.
     *
     * @return a filter order before the Context binding filter
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }

    /**
     * Wraps supported requests and responses with bounded repeatable-body access.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            throw new ServletException("CachedBodyFilter only supports HTTP requests");
        }
        boolean cacheRequest = this.options.shouldCacheRequest(httpRequest);
        long contentLength = httpRequest.getContentLengthLong();
        if (cacheRequest && contentLength > this.options.getRequestLimit()) {
            httpResponse.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }

        HttpServletRequest effectiveRequest = httpRequest;
        HttpServletResponse effectiveResponse = httpResponse;
        if (cacheRequest && !(httpRequest instanceof CachedBodyRequestWrapper)) {
            try {
                effectiveRequest = new CachedBodyRequestWrapper(httpRequest, this.options.getRequestLimit());
            } catch (RelevantException ignored) {
                httpResponse.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                return;
            }
        }
        if (this.options.isResponseCacheEnabled() && !(httpResponse instanceof CachedBodyResponseWrapper)) {
            effectiveResponse = new CachedBodyResponseWrapper(httpResponse, this.options.getResponseLimit());
        }
        chain.doFilter(effectiveRequest, effectiveResponse);
    }

}
