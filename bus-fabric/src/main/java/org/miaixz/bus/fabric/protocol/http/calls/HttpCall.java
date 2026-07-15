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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * Single-use HTTP call backed by the shared protocol call lifecycle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpCall extends MonoCall<HttpResponse> {

    /**
     * Source exchange.
     */
    private final HttpX exchange;

    /**
     * Request snapshot.
     */
    private final HttpRequest request;

    /**
     * Creates a call.
     *
     * @param exchange source exchange
     */
    private HttpCall(final HttpX exchange) {
        this(exchange, null);
    }

    /**
     * Creates a call.
     *
     * @param exchange   source exchange
     * @param dispatcher dispatcher used by enqueue()
     */
    private HttpCall(final HttpX exchange, final Dispatcher dispatcher) {
        super("http-call", dispatcher, EventObserver.noop());
        this.exchange = require(exchange, "HTTP exchange");
        this.request = require(exchange.request(), "HTTP request");
    }

    /**
     * Creates a call for an exchange.
     *
     * @param exchange source exchange
     * @return HTTP call
     */
    public static HttpCall create(final HttpX exchange) {
        return new HttpCall(exchange);
    }

    /**
     * Creates a call for an exchange.
     *
     * @param exchange   source exchange
     * @param dispatcher dispatcher used by enqueue()
     * @return HTTP call
     */
    public static HttpCall create(final HttpX exchange, final Dispatcher dispatcher) {
        return new HttpCall(exchange, require(dispatcher, "Dispatcher"));
    }

    /**
     * Returns the request snapshot.
     *
     * @return request
     */
    public HttpRequest request() {
        return request;
    }

    /**
     * Performs the HTTP exchange.
     *
     * @return HTTP response
     */
    @Override
    protected HttpResponse perform() {
        return exchange.execute(cancellation());
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
     * Builds a stable async dispatch key for this request authority.
     *
     * @return dispatch key
     */
    @Override
    protected String dispatchKey() {
        final Address address = request.url().address();
        return address.scheme() + Symbol.COLON + Symbol.FORWARDSLASH + address.host() + Symbol.C_COLON + address.port();
    }

    /**
     * Validates required references.
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
