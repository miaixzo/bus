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
     * HTTP protocol operation.
     */
    private final Function<Cancellation, HttpResponse> operation;

    /**
     * Request snapshot used to build the dispatch key.
     */
    private final HttpRequest request;

    /**
     * Creates an HTTP call.
     *
     * @param request    immutable HTTP request
     * @param dispatcher dispatcher used by enqueue()
     * @param callback   callback managed by the call lifecycle
     * @param operation  HTTP protocol operation
     */
    private HttpCall(final HttpRequest request, final Dispatcher dispatcher,
            final Callback<? super HttpResponse> callback, final Function<Cancellation, HttpResponse> operation) {
        super("http-call", dispatcher, EventObserver.noop(), callback);
        this.request = require(request, "HTTP request");
        this.operation = require(operation, "HTTP operation");
    }

    /**
     * Creates an HTTP call.
     *
     * @param request    immutable HTTP request
     * @param dispatcher dispatcher used by enqueue()
     * @param callback   callback managed by the call lifecycle
     * @param operation  HTTP protocol operation
     * @return HTTP call
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
     * @return HTTP response
     */
    @Override
    protected HttpResponse perform() {
        return operation.apply(cancellation());
    }

    /**
     * Closes a response produced after cancellation.
     *
     * @param response produced response
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
     * @return dispatch key
     */
    @Override
    protected String dispatchKey() {
        final Address address = request.url().address();
        return address.scheme() + Symbol.COLON + Symbol.FORWARDSLASH + address.host() + Symbol.C_COLON + address.port();
    }

    /**
     * Validates a required reference.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
