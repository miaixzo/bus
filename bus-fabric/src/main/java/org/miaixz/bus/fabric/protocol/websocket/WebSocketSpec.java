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
package org.miaixz.bus.fabric.protocol.websocket;

import java.net.URI;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.*;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.observe.EventObserver;

/**
 * Immutable execution specification for a WebSocket exchange.
 *
 * @param context  runtime services used by the WebSocket exchange
 * @param uri      original target URI requested by the caller
 * @param address  normalized HTTP address used for the upgrade request
 * @param headers  upgrade request headers
 * @param timeout  connect and session timeout policy
 * @param proxy    outbound proxy policy propagated to the HTTP upgrade
 * @param guard    optional policy guard for WebSocket messages
 * @param filter   optional message filter for WebSocket open, inbound, and outbound messages
 * @param observer observer receiving WebSocket lifecycle events
 * @param handler  inbound message handler
 * @param listener session lifecycle listener
 * @author Kimi Liu
 */
record WebSocketSpec(Context context, URI uri, Address address, Headers headers, Timeout timeout, ProxyPlan proxy,
        GuardRule guard, Filter filter, EventObserver observer, Handler handler,
        Listener<? super WebSocketSession> listener) {

    /**
     * Creates a validated specification.
     *
     * @param context  runtime services used by the exchange
     * @param uri      original target URI
     * @param address  normalized upgrade address
     * @param headers  upgrade request headers
     * @param timeout  WebSocket timeout policy
     * @param proxy    non-null outbound proxy policy
     * @param guard    optional WebSocket message guard
     * @param filter   optional WebSocket message filter
     * @param observer WebSocket lifecycle observer
     * @param handler  inbound message handler
     * @param listener session lifecycle listener
     */
    WebSocketSpec {
        context = require(context, "Context");
        uri = require(uri, "Target URI");
        address = require(address, "Address");
        headers = require(headers, "Headers");
        timeout = require(timeout, "Timeout");
        proxy = require(proxy, "Proxy plan");
        observer = EventObserver.safe(require(observer, "Observer"));
        handler = require(handler, "Handler");
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  field name
     * @param <T>   value type
     * @return the validated reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
