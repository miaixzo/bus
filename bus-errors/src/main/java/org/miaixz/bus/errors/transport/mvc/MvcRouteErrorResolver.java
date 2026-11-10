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
package org.miaixz.bus.errors.transport.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.ErrorResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ServletRequestPathUtils;

import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.ErrorContext;
import org.miaixz.bus.errors.FailureFacts;
import org.miaixz.bus.errors.policy.PolicySnapshot.Mode;
import org.miaixz.bus.errors.transport.http.HttpErrorPolicy;
import org.miaixz.bus.errors.transport.http.HttpErrorResponse;
import org.miaixz.bus.errors.transport.servlet.ServletErrorFilter;
import org.miaixz.bus.errors.transport.servlet.ServletErrorHandler;

/**
 * Applies the shared route policy before MVC delegates to local or global Advice.
 *
 * @author Kimi Liu
 */
public final class MvcRouteErrorResolver {

    /**
     * Route policy for the Servlet listener, or null to defer to the resolver chain.
     */
    private final HttpErrorPolicy policy;
    /**
     * Classifier and observer shared by this MVC listener.
     */
    private final ErrorBoundary boundary;

    /**
     * Creates the MVC route adapter.
     *
     * @param policy   listener policy, or {@code null} to delegate
     * @param boundary shared classifier and observer for MVC
     */
    public MvcRouteErrorResolver(HttpErrorPolicy policy, ErrorBoundary boundary) {
        this.policy = policy;
        this.boundary = java.util.Objects.requireNonNull(boundary, "boundary");
    }

    /**
     * Writes a selected HTTP error with an empty body.
     *
     * @param request  current Servlet request
     * @param response current Servlet response
     * @param failure  exception selected by MVC
     * @return empty view after handling, or {@code null} to continue resolution
     */
    public ModelAndView resolve(HttpServletRequest request, HttpServletResponse response, Exception failure) {
        if (this.policy == null || response.isCommitted() || !(failure instanceof ErrorResponse error)
                || !error.getStatusCode().isError()) {
            return null;
        }
        var context = new ErrorContext("mvc", "http", ErrorContext.Phase.OPEN, this.policy.version());
        var operation = ServletErrorHandler.operation(this.boundary, request);
        var decision = operation.decide(
                context,
                new FailureFacts(FailureFacts.Source.LOCAL, null, error.getStatusCode().value()),
                failure);
        if (decision.action() != org.miaixz.bus.errors.ErrorDecision.Action.RENDER) {
            return null;
        }
        try {
            var path = ServletRequestPathUtils.hasParsedRequestPath(request)
                    ? ServletRequestPathUtils.getParsedRequestPath(request)
                    : ServletRequestPathUtils.parse(request);
            if (this.policy.select(path.pathWithinApplication(), decision, true) != Mode.NATIVE_HTTP) {
                return null;
            }
        } catch (IllegalArgumentException invalidPath) {
            return null;
        }
        return operation.complete(context, decision, failure, MvcErrorAdvice.requestId(request), () -> {
            var selected = HttpErrorResponse
                    .empty(error.getStatusCode(), error.getHeaders(), this.policy.cacheControl());
            response.reset();
            selected.headers().forEach((name, values) -> {
                if ("Cache-Control".equalsIgnoreCase(name) && !this.policy.cacheControl().isBlank()) {
                    response.setHeader(name, values.getFirst());
                } else {
                    values.forEach(value -> response.addHeader(name, value));
                }
            });
            ServletErrorFilter.approve(response);
            response.setStatus(selected.status().value());
            return new ModelAndView();
        });
    }

}
