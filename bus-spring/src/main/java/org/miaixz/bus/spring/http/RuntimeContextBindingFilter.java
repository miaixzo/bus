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

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.RuntimeContextScope;
import org.miaixz.bus.spring.RuntimeContextSnapshot;

import java.io.IOException;

/**
 * Binds the generic runtime context to every servlet dispatch.
 * <p>
 * The initial request captures one immutable snapshot. Async and error dispatches reinstall that snapshot, while every
 * dispatch restores the worker thread's previous context in a {@code finally} block.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RuntimeContextBindingFilter implements Filter {

    /**
     * Request attribute containing the immutable runtime context.
     */
    public static final String SNAPSHOT_ATTRIBUTE = RuntimeContextBindingFilter.class.getName() + ".SNAPSHOT";

    private static final String ALREADY_FILTERED_ATTRIBUTE =
            RuntimeContextBindingFilter.class.getName() + ".FILTERED";

    private static final String ASYNC_LISTENER_ATTRIBUTE =
            RuntimeContextBindingFilter.class.getName() + ".ASYNC_LISTENER";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            throw new ServletException("RuntimeContextBindingFilter only supports HTTP requests");
        }
        if (request.getAttribute(ALREADY_FILTERED_ATTRIBUTE) != null) {
            filterChain.doFilter(request, response);
            return;
        }

        request.setAttribute(ALREADY_FILTERED_ATTRIBUTE, Boolean.TRUE);
        boolean completed = false;
        try {
            RuntimeContextSnapshot snapshot = resolveSnapshot(httpRequest);
            try (RuntimeContextScope ignored = RuntimeContextScope.open(snapshot)) {
                filterChain.doFilter(httpRequest, httpResponse);
                completed = true;
            }
        } finally {
            request.removeAttribute(ALREADY_FILTERED_ATTRIBUTE);
            finishDispatch(httpRequest, completed);
        }
    }

    private RuntimeContextSnapshot resolveSnapshot(HttpServletRequest request) {
        Object existing = request.getAttribute(SNAPSHOT_ATTRIBUTE);
        if (existing instanceof RuntimeContextSnapshot snapshot) {
            return snapshot;
        }
        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            return RuntimeContextSnapshot.empty();
        }

        RuntimeContextSnapshot snapshot;
        try (RuntimeContextScope ignored = RuntimeContextScope.open(RuntimeContextSnapshot.empty())) {
            ContextBuilder.init();
            Authorize authorize = ContextBuilder.getAuthorize();
            ContextBuilder.setAuthorize(authorize);
            snapshot = RuntimeContextSnapshot.capture();
        }
        request.setAttribute(SNAPSHOT_ATTRIBUTE, snapshot);
        return snapshot;
    }

    private void finishDispatch(HttpServletRequest request, boolean completed) {
        if (request.isAsyncStarted()) {
            registerAsyncListener(request);
            return;
        }
        DispatcherType dispatcherType = request.getDispatcherType();
        if (completed || dispatcherType == DispatcherType.ERROR) {
            cleanup(request);
        }
    }

    private void registerAsyncListener(HttpServletRequest request) {
        if (request.getAttribute(ASYNC_LISTENER_ATTRIBUTE) != null) {
            return;
        }
        RuntimeContextAsyncListener listener = new RuntimeContextAsyncListener();
        request.setAttribute(ASYNC_LISTENER_ATTRIBUTE, listener);
        try {
            request.getAsyncContext().addListener(listener);
        } catch (IllegalStateException ignored) {
            cleanup(request);
        }
    }

    private static void cleanup(ServletRequest request) {
        Object value = request.getAttribute(SNAPSHOT_ATTRIBUTE);
        if (value instanceof RuntimeContextSnapshot snapshot) {
            ContextBuilder.clear(snapshot.getRequestId());
        }
        request.removeAttribute(SNAPSHOT_ATTRIBUTE);
        request.removeAttribute(ASYNC_LISTENER_ATTRIBUTE);
    }

    private static final class RuntimeContextAsyncListener implements AsyncListener {

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
            event.getAsyncContext().addListener(this);
        }

    }

}
