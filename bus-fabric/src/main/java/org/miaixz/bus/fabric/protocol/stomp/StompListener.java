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
package org.miaixz.bus.fabric.protocol.stomp;

import org.miaixz.bus.fabric.Listener;

/**
 * STOMP lifecycle listener adapter.
 * <p>
 * This adapter exposes STOMP-specific lifecycle callbacks while bridging them to the shared fabric listener contract.
 * Subclasses override {@link #connected(StompSession)}, {@link #disconnected(StompSession)} or
 * {@link #error(StompSession, Throwable)} to handle session events.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class StompListener implements Listener<StompSession> {

    /**
     * Creates a STOMP lifecycle listener adapter.
     */
    public StompListener() {
        // Listener adapter.
    }

    /**
     * Handles a successfully established STOMP session.
     *
     * @param session session
     */
    public void connected(final StompSession session) {
        // Default listener intentionally performs no action.
    }

    /**
     * Handles a closed STOMP session.
     *
     * @param session session
     */
    public void disconnected(final StompSession session) {
        // Default listener intentionally performs no action.
    }

    /**
     * Handles a STOMP session failure.
     *
     * @param session session, when available
     * @param cause   failure cause
     */
    public void error(final StompSession session, final Throwable cause) {
        // Default listener intentionally performs no action.
    }

    @Override
    public final void open(final StompSession source) {
        connected(source);
    }

    @Override
    public final void close(final StompSession source) {
        disconnected(source);
    }

    @Override
    public final void failure(final StompSession source, final Throwable cause) {
        error(source, cause);
    }

}
