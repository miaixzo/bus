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
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.observe.EventObserver;

/**
 * Immutable execution snapshot for a WebSocket exchange.
 *
 * @param context  runtime services used by the WebSocket exchange
 * @param uri      original target URI requested by the caller
 * @param address  normalized HTTP address used for the upgrade request
 * @param headers  upgrade request headers
 * @param timeout  connect and session timeout policy
 * @param guard    optional policy guard for WebSocket messages
 * @param filter   optional message filter for WebSocket open, inbound, and outbound messages
 * @param observer observer receiving WebSocket lifecycle events
 * @param callback callback receiving the opened WebSocket session or failure
 * @param handler  inbound message handler
 * @param listener session lifecycle listener
 * @author Kimi Liu
 * @since Java 21+
 */
record WebSocketSnapshot(Context context, URI uri, Address address, Headers headers, Timeout timeout, GuardRule guard,
        Filter filter, EventObserver observer, Callback<WebSocketSession> callback, Handler handler,
        Listener<? super WebSocketSession> listener) {

    /**
     * Creates a validated snapshot.
     */
    WebSocketSnapshot {
        context = require(context, "Context");
        uri = require(uri, "Target URI");
        address = require(address, "Address");
        headers = require(headers, "Headers");
        timeout = require(timeout, "Timeout");
        observer = EventObserver.safe(require(observer, "Observer"));
        callback = Wiring.safeCallback(require(callback, "Callback"), observer);
        handler = require(handler, "Handler");
        listener = Wiring.safe(require(listener, "Listener"), observer);
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
