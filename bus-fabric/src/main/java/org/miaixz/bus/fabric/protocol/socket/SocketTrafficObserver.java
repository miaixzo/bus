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

import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.event.FabricEvent;

/**
 * Stateless socket traffic event forwarding extension.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketTrafficObserver implements EventObserver {

    /**
     * Downstream observer.
     */
    private final EventObserver delegate;

    /**
     * Creates an observer that forwards socket wire events without owning aggregation state.
     *
     * @param delegate downstream observer
     */
    private SocketTrafficObserver(final EventObserver delegate) {
        this.delegate = EventObserver.safe(delegate);
    }

    /**
     * Creates a socket traffic observer with no downstream observer.
     *
     * @return socket traffic observer
     */
    public static SocketTrafficObserver create() {
        return new SocketTrafficObserver(EventObserver.noop());
    }

    /**
     * Creates a socket traffic observer that forwards events to a downstream observer.
     *
     * @param delegate downstream observer
     * @return socket traffic observer
     */
    public static SocketTrafficObserver create(final EventObserver delegate) {
        return new SocketTrafficObserver(delegate);
    }

    /**
     * Forwards a socket event unchanged so the downstream meter remains the sole owner of byte validation and
     * aggregation. Physical Socket and WebSocket events therefore remain distinct from logical STOMP frame events.
     *
     * @param event fabric event
     */
    @Override
    public void emit(final FabricEvent event) {
        if (event == null) {
            return;
        }
        delegate.emit(event);
    }

}
