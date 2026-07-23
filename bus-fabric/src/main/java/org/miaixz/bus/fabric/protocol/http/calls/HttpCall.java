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
package org.miaixz.bus.fabric.protocol.http.calls;

import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Single-use HTTP call backed by the shared protocol call lifecycle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpCall extends MonoCall<HttpResponse> {

    /**
     * One-shot protocol operation, cleared immediately before invocation.
     */
    private Function<Cancellation, HttpResponse> operation;

    /**
     * Request authority retained until execution starts or the dispatch key is materialized.
     */
    private Address address;

    /**
     * Lazily cached scheme, host, and port key used for asynchronous dispatch grouping.
     */
    private volatile String dispatchKey;

    /**
     * Creates an HTTP call.
     *
     * @param request    non-null HTTP request whose destination supplies the dispatch authority
     * @param dispatcher non-null dispatcher used by parameterless asynchronous submission
     * @param callback   optional terminal callback managed by the call lifecycle
     * @param operation  non-null one-shot HTTP operation receiving the lifecycle cancellation handle
     */
    private HttpCall(final HttpRequest request, final Dispatcher dispatcher,
            final Callback<? super HttpResponse> callback, final Function<Cancellation, HttpResponse> operation) {
        super("http-call", dispatcher, EventObserver.noop(), callback, timeout(request));
        final HttpRequest current = require(request, "HTTP request");
        this.address = current.url().address();
        this.operation = require(operation, "HTTP operation");
    }

    /**
     * Creates an HTTP call.
     *
     * @param request    non-null HTTP request whose destination supplies the dispatch authority
     * @param dispatcher non-null dispatcher used by parameterless asynchronous submission
     * @param callback   optional terminal callback managed by the call lifecycle
     * @param operation  non-null one-shot HTTP operation receiving the lifecycle cancellation handle
     * @return new single-use HTTP call
     */
    public static HttpCall create(
            final HttpRequest request,
            final Dispatcher dispatcher,
            final Callback<? super HttpResponse> callback,
            final Function<Cancellation, HttpResponse> operation) {
        return new HttpCall(request, require(dispatcher, "Dispatcher"), callback, operation);
    }

    /**
     * Executes the HTTP protocol operation.
     *
     * @return response produced by the configured protocol operation
     */
    @Override
    protected HttpResponse perform() {
        final Function<Cancellation, HttpResponse> current = operation;
        operation = null;
        try {
            return current.apply(cancellation());
        } finally {
            address = null;
        }
    }

    /**
     * Closes a response produced after cancellation.
     *
     * @param response response produced after cancellation, or null
     */
    @Override
    protected void closeAfterCancelled(final HttpResponse response) {
        if (response != null) {
            response.close();
        }
    }

    /**
     * Builds a stable asynchronous dispatch key for the request authority.
     *
     * @return cached authority key in {@code scheme://host:port} form
     */
    @Override
    protected String dispatchKey() {
        String current = dispatchKey;
        if (current == null) {
            final Address target = require(address, "HTTP dispatch address");
            current = target.scheme() + Symbol.COLON + Symbol.FORWARDSLASH + target.host() + Symbol.C_COLON
                    + target.port();
            dispatchKey = current;
        }
        return current;
    }

    /**
     * Validates a required reference.
     *
     * @param value reference to validate
     * @param name  field label included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Returns the complete timeout policy from a validated request.
     *
     * @param request request candidate
     * @return complete request timeout policy
     */
    private static Timeout timeout(final HttpRequest request) {
        return require(request, "HTTP request").timeout();
    }

}
