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
package org.miaixz.bus.spring.context.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.spring.context.ContextBuilder;
import org.miaixz.bus.spring.context.ContextScope;
import org.miaixz.bus.spring.context.ContextState;
import org.miaixz.bus.spring.context.ContextTransfer;
import org.miaixz.bus.spring.context.resolver.ContextResolver;
import org.miaixz.bus.spring.context.spi.ContextProvider;
import org.miaixz.bus.spring.web.RequestContext;

/**
 * Installs one immutable runtime context around every supported Servlet dispatch.
 *
 * @author Kimi Liu
 */
public final class ContextBindingFilter implements Filter {

    /**
     * Servlet request attribute containing the immutable context state shared across dispatches.
     */
    public static final String STATE_ATTRIBUTE = ContextBindingFilter.class.getName() + ".STATE";

    /**
     * Servlet request attribute preventing duplicate binding within one dispatch.
     */
    private static final String FILTERED_ATTRIBUTE = ContextBindingFilter.class.getName() + ".FILTERED";

    /**
     * Servlet request attribute marking that asynchronous cleanup has been registered.
     */
    private static final String ASYNC_LISTENER_ATTRIBUTE = ContextBindingFilter.class.getName() + ".ASYNC_LISTENER";

    /**
     * Resolver that combines normalized credentials with ordered authentication providers.
     */
    private final ContextResolver resolver;

    /**
     * Stateless request-boundary accessor used to normalize HTTP headers.
     */
    private final RequestContext requestContext;

    /**
     * Creates a filter using a default stateless request accessor.
     *
     * @param providers ordered authenticated-context providers
     */
    public ContextBindingFilter(List<ContextProvider> providers) {
        this(providers, new RequestContext());
    }

    /**
     * Creates a filter from its context providers and request-boundary accessor.
     *
     * @param providers      ordered authenticated-context providers
     * @param requestContext stateless accessor used to normalize HTTP headers
     * @throws NullPointerException when {@code requestContext} is {@code null}
     */
    public ContextBindingFilter(List<ContextProvider> providers, RequestContext requestContext) {
        this.resolver = new ContextResolver(providers);
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext");
    }

    /**
     * Completes synchronous cleanup or registers cleanup for an active asynchronous lifecycle.
     *
     * @param request   current HTTP request
     * @param completed whether the downstream chain returned normally
     */
    private static void finishDispatch(HttpServletRequest request, boolean completed) {
        if (request.isAsyncStarted()) {
            registerAsyncListener(request);
            return;
        }
        DispatcherType type = request.getDispatcherType();
        if (type == DispatcherType.ERROR || completed) {
            cleanup(request);
        }
    }

    /**
     * Registers exactly one listener to retain state across and clean state after asynchronous dispatches.
     *
     * @param request request with an active asynchronous context
     */
    private static void registerAsyncListener(HttpServletRequest request) {
        if (request.getAttribute(ASYNC_LISTENER_ATTRIBUTE) != null) {
            return;
        }
        ContextAsyncListener listener = new ContextAsyncListener();
        request.setAttribute(ASYNC_LISTENER_ATTRIBUTE, listener);
        try {
            request.getAsyncContext().addListener(listener);
        } catch (IllegalStateException ignored) {
            cleanup(request);
        }
    }

    /**
     * Removes context-lifecycle attributes from a completed request.
     *
     * @param request request whose context lifecycle has completed
     */
    private static void cleanup(ServletRequest request) {
        request.removeAttribute(STATE_ATTRIBUTE);
        request.removeAttribute(ASYNC_LISTENER_ATTRIBUTE);
    }

    /**
     * Resolves, installs, captures, and finally removes context state around one Servlet dispatch.
     *
     * @param request  incoming Servlet request
     * @param response outgoing Servlet response
     * @param chain    remaining filter chain
     * @throws ServletException when the request is not HTTP or downstream processing fails
     * @throws IOException      when downstream I/O fails
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            throw new ServletException("ContextBindingFilter only supports HTTP requests");
        }
        if (request.getAttribute(FILTERED_ATTRIBUTE) != null) {
            chain.doFilter(request, response);
            return;
        }

        request.setAttribute(FILTERED_ATTRIBUTE, Boolean.TRUE);
        boolean completed = false;
        try {
            ContextState state = resolveState(httpRequest);
            try (ContextScope ignored = ContextTransfer.install(state)) {
                try {
                    chain.doFilter(httpRequest, httpResponse);
                    completed = true;
                } finally {
                    httpRequest.setAttribute(STATE_ATTRIBUTE, ContextBuilder.capture());
                }
            }
        } finally {
            request.removeAttribute(FILTERED_ATTRIBUTE);
            finishDispatch(httpRequest, completed);
        }
    }

    /**
     * Reuses a state from an earlier dispatch or resolves a new state for an initial request.
     *
     * @param request current HTTP request
     * @return immutable state for this dispatch
     */
    private ContextState resolveState(HttpServletRequest request) {
        Object existing = request.getAttribute(STATE_ATTRIBUTE);
        if (existing instanceof ContextState state) {
            return state;
        }
        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            return ContextState.empty();
        }

        Map<String, String> headers = this.requestContext.getHeaders(request);
        Http.Auth.Credential tokenCredential = Http.Auth.bearerCredential(headers);
        Http.Auth.Credential apiKeyCredential = Http.Auth.apiKeyCredential(headers);
        ContextState state = this.resolver.resolve(ID.objectId(), tokenCredential, apiKeyCredential);
        request.setAttribute(STATE_ATTRIBUTE, state);
        return state;
    }

    /**
     * Retains immutable state across asynchronous restarts and clears it at terminal events.
     */
    private static final class ContextAsyncListener implements AsyncListener {

        /**
         * Creates the stateless asynchronous lifecycle listener.
         */
        private ContextAsyncListener() {
            // No initialization required.
        }

        /**
         * Clears request context attributes after successful asynchronous completion.
         *
         * @param event asynchronous completion event
         */
        @Override
        public void onComplete(AsyncEvent event) {
            cleanup(event.getSuppliedRequest());
        }

        /**
         * Clears request context attributes after an asynchronous timeout.
         *
         * @param event asynchronous timeout event
         */
        @Override
        public void onTimeout(AsyncEvent event) {
            cleanup(event.getSuppliedRequest());
        }

        /**
         * Clears request context attributes after an asynchronous error.
         *
         * @param event asynchronous error event
         */
        @Override
        public void onError(AsyncEvent event) {
            cleanup(event.getSuppliedRequest());
        }

        /**
         * Preserves the current immutable state and follows the restarted asynchronous cycle.
         *
         * @param event asynchronous restart event
         */
        @Override
        public void onStartAsync(AsyncEvent event) {
            ServletRequest request = event.getSuppliedRequest();
            if (ContextBuilder.isPresent()) {
                request.setAttribute(STATE_ATTRIBUTE, ContextBuilder.capture());
            }
            event.getAsyncContext().addListener(this);
        }

    }

}
