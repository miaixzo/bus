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
package org.miaixz.bus.spring.web;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.ContextScope;
import org.miaixz.bus.spring.ContextState;

/**
 * Installs and restores an immutable runtime Context for every Servlet dispatch.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ContextBindingFilter implements Filter {

    /**
     * Request attribute carrying immutable context state across dispatches.
     */
    public static final String STATE_ATTRIBUTE = ContextBindingFilter.class.getName() + ".STATE";
    /**
     * Request attribute preventing duplicate filtering within one dispatch.
     */
    private static final String FILTERED_ATTRIBUTE = ContextBindingFilter.class.getName() + ".FILTERED";
    /**
     * Request attribute preventing duplicate asynchronous listener registration.
     */
    private static final String ASYNC_LISTENER_ATTRIBUTE = ContextBindingFilter.class.getName() + ".ASYNC_LISTENER";

    /**
     * Context facade used for state capture and installation.
     */
    private final ContextBuilder contextBuilder;

    /**
     * Creates a Context binding filter using the owning Context facade.
     *
     * @param contextBuilder application-context-scoped facade
     */
    public ContextBindingFilter(ContextBuilder contextBuilder) {
        this.contextBuilder = Objects.requireNonNull(contextBuilder, "contextBuilder");
    }

    /**
     * Installs one dispatch state and always restores the worker thread's parent state.
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
            try (ContextScope ignored = this.contextBuilder.install(state)) {
                try {
                    chain.doFilter(httpRequest, httpResponse);
                    completed = true;
                } finally {
                    httpRequest.setAttribute(STATE_ATTRIBUTE, this.contextBuilder.capture());
                }
            }
        } finally {
            request.removeAttribute(FILTERED_ATTRIBUTE);
            finishDispatch(httpRequest, completed);
        }
    }

    /**
     * Resolves or creates the immutable state for a Servlet dispatch.
     *
     * @param request current HTTP request
     * @return state assigned to the dispatch
     */
    private ContextState resolveState(HttpServletRequest request) {
        Object existing = request.getAttribute(STATE_ATTRIBUTE);
        if (existing instanceof ContextState state) {
            return state;
        }
        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            return ContextState.empty();
        }
        ContextState state;
        try (ContextScope ignored = this.contextBuilder.install(ContextState.empty())) {
            this.contextBuilder.setRequestId();
            Authorize authorize = this.contextBuilder.getAuthorize();
            this.contextBuilder.setAuthorize(authorize);
            state = this.contextBuilder.capture();
        }
        request.setAttribute(STATE_ATTRIBUTE, state);
        return state;
    }

    /**
     * Creates the synchronous completion, error redispatch, and asynchronous continuation.
     *
     * @param request   current HTTP request
     * @param completed whether the downstream filter chain completed normally
     */
    private void finishDispatch(HttpServletRequest request, boolean completed) {
        if (request.isAsyncStarted()) {
            registerAsyncListener(request);
            return;
        }
        DispatcherType type = request.getDispatcherType();
        if (type == DispatcherType.ERROR || completed) {
            cleanup(request);
        }
        // A failed REQUEST retains its final state for the container's ERROR redispatch.
    }

    /**
     * Registers exactly one cleanup listener for an asynchronous request.
     *
     * @param request asynchronous HTTP request
     */
    private void registerAsyncListener(HttpServletRequest request) {
        if (request.getAttribute(ASYNC_LISTENER_ATTRIBUTE) != null) {
            return;
        }
        ContextAsyncListener listener = new ContextAsyncListener(this.contextBuilder);
        request.setAttribute(ASYNC_LISTENER_ATTRIBUTE, listener);
        try {
            request.getAsyncContext().addListener(listener);
        } catch (IllegalStateException ignored) {
            cleanup(request);
        }
    }

    /**
     * Removes all context lifecycle request attributes.
     *
     * @param request Servlet request to clean
     */
    private static void cleanup(ServletRequest request) {
        request.removeAttribute(STATE_ATTRIBUTE);
        request.removeAttribute(ASYNC_LISTENER_ATTRIBUTE);
    }

    /**
     * Maintains the final state across asynchronous cycles and cleans every terminal path.
     */
    private static final class ContextAsyncListener implements AsyncListener {

        /**
         * Context facade used to capture state at asynchronous redispatch.
         */
        private final ContextBuilder contextBuilder;

        /**
         * Creates an asynchronous context lifecycle listener.
         *
         * @param contextBuilder application-context-scoped facade
         */
        private ContextAsyncListener(ContextBuilder contextBuilder) {
            this.contextBuilder = contextBuilder;
        }

        @Override
        public void onComplete(AsyncEvent event) {
            cleanup(event.getSuppliedRequest());
        }

        @Override
        public void onTimeout(AsyncEvent event) {
            cleanup(event.getSuppliedRequest());
        }

        @Override
        public void onError(AsyncEvent event) {
            cleanup(event.getSuppliedRequest());
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
            event.getSuppliedRequest().setAttribute(STATE_ATTRIBUTE, this.contextBuilder.capture());
            event.getAsyncContext().addListener(this);
        }
    }

}
