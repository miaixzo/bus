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
package org.miaixz.bus.spring.web.routing;

import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.web.ErrorResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import org.miaixz.bus.core.lang.annotation.Nullable;
import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.transport.mvc.MvcRouteErrorResolver;

/**
 * Preserves selected MVC HTTP errors for configured route prefixes before shared exception advice runs.
 * <p>
 * Routing can fail before MVC selects a handler. The resolver uses the request path so that missing routes, unsupported
 * methods, and unsupported media types can receive the same treatment. Matched errors retain their HTTP status and
 * headers with an empty response body. Other errors continue through the resolver chain.
 *
 * @author Kimi Liu
 */
public final class RouteErrorResolver implements HandlerExceptionResolver, Ordered {

    /**
     * Active route error policy, or {@code null} when this listener delegates all errors.
     */
    private MvcRouteErrorResolver delegate;

    /**
     * Compiled policy used when replacing the error boundary, or null to delegate.
     */
    private final RouteErrorPolicy policy;

    /**
     * Creates a resolver for literal, slash-terminated prefixes and an explicit set of client error statuses.
     *
     * @param pathPrefixes nonempty list of literal paths relative to the MVC servlet mapping, each beginning and ending
     *                     with a slash
     * @param statuses     nonempty set of HTTP status codes in the range 400 to 499
     * @param cacheControl optional Cache-Control header value, blank to preserve the existing header
     * @throws IllegalArgumentException if a prefix is invalid, the prefix or status collection is empty, a status is
     *                                  outside the client error range, or the cache value contains a line break
     */
    public RouteErrorResolver(List<String> pathPrefixes, Set<Integer> statuses, String cacheControl) {
        this(new RouteErrorPolicy(pathPrefixes, statuses, cacheControl));
    }

    /**
     * Creates the MVC adapter with the policy selected at startup.
     *
     * @param policy active MVC policy, or {@code null} to delegate all errors
     */
    public RouteErrorResolver(@Nullable RouteErrorPolicy policy) {
        this.policy = policy;
        setErrorBoundary(ErrorBoundary.standard());
    }

    /**
     * Uses the application's optional shared error boundary.
     *
     * @param boundary classifier and observer for MVC
     */
    @Autowired(required = false)
    public void setErrorBoundary(ErrorBoundary boundary) {
        this.delegate = new MvcRouteErrorResolver(this.policy == null ? null : this.policy.compiled(), boundary);
    }

    /**
     * Gives route-specific handling precedence over the standard MVC exception resolver chain.
     *
     * @return the highest resolver precedence
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Writes the status and headers from a matching {@link ErrorResponse}. Repeated header values are preserved and the
     * configured cache policy, when present, overrides Cache-Control.
     *
     * @param request   current servlet request used to match the configured route prefixes
     * @param response  servlet response to update while it is still uncommitted
     * @param handler   handler selected by MVC, or {@code null} when routing failed before selection
     * @param exception exception raised during MVC dispatch
     * @return an empty {@link ModelAndView} when the error was handled, or {@code null} to continue the resolver chain
     */
    @Override
    @Nullable
    public ModelAndView resolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            @Nullable Object handler,
            Exception exception) {
        return this.delegate.resolve(request, response, exception);
    }

}
