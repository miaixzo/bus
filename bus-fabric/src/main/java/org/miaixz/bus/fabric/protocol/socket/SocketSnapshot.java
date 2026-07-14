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
package org.miaixz.bus.fabric.protocol.socket;

import java.net.URI;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;
import org.miaixz.bus.fabric.guard.GuardRule;
import org.miaixz.bus.fabric.network.proxy.ProxyHeader;
import org.miaixz.bus.fabric.observe.EventObserver;

/**
 * Immutable execution snapshot for a socket exchange.
 *
 * @param context       runtime services used by the socket exchange
 * @param uri           original target URI requested by the caller
 * @param address       normalized transport address derived from the target URI
 * @param headers       handshake or first-message headers associated with the exchange
 * @param timeout       connect and session timeout policy
 * @param frameCodec    codec used to delimit socket messages
 * @param handler       inbound message handler for the created session
 * @param guard         optional policy guard for socket messages
 * @param observer      observer receiving socket lifecycle and traffic events
 * @param proxyHeader   PROXY protocol metadata to send or inject, or {@code null}
 * @param socketOptions channel and session tuning options
 * @param callback      callback receiving the opened socket session or failure
 * @param listener      session lifecycle listener
 * @param pooled        whether the exchange may use pooled transport resources
 * @author Kimi Liu
 * @since Java 21+
 */
record SocketSnapshot(Context context, URI uri, Address address, Headers headers, Timeout timeout,
        FrameCodec frameCodec, Handler handler, GuardRule guard, EventObserver observer, ProxyHeader proxyHeader,
        SocketOptions socketOptions, Callback<SocketSession> callback, Listener<? super SocketSession> listener,
        boolean pooled) {

    /**
     * Creates a validated snapshot.
     */
    SocketSnapshot {
        context = require(context, "Context");
        uri = require(uri, "Target URI");
        address = require(address, "Address");
        headers = require(headers, "Headers");
        timeout = require(timeout, "Timeout");
        frameCodec = require(frameCodec, "Frame codec");
        handler = require(handler, "Handler");
        observer = EventObserver.safe(require(observer, "Observer"));
        socketOptions = require(socketOptions, "Socket options");
        callback = Wiring.safeCallback(require(callback, "Callback"), observer);
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
