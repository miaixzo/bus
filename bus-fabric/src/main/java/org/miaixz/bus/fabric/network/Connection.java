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
package org.miaixz.bus.fabric.network;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Lifecycle;

/**
 * Protocol-neutral network connection contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Connection extends AutoCloseable, Lifecycle {

    /**
     * Returns the connection destination.
     *
     * @return immutable destination and route metadata used to establish this connection
     */
    Destination destination();

    /**
     * Returns the underlying conduit.
     *
     * @return transport conduit that owns the physical channel
     */
    Conduit conduit();

    /**
     * Returns the protocol-layer read view.
     *
     * @return source exposed to the negotiated protocol, after any transport wrapping
     */
    Source source();

    /**
     * Returns the protocol-layer write view.
     *
     * @return sink exposed to the negotiated protocol, after any transport wrapping
     */
    Sink sink();

    /**
     * Returns whether the connection is healthy.
     *
     * @return true when the physical connection remains usable
     */
    boolean healthy();

    /**
     * Returns whether the connection is idle.
     *
     * @return true when the connection currently has no active logical work
     */
    boolean idle();

    /**
     * Returns the protocol established on this physical connection.
     *
     * <p>
     * Legacy implementations are single-lease HTTP/1.1 connections. Implementations that negotiate another protocol
     * override this method only after the established protocol is known.
     * </p>
     *
     * @return negotiated protocol, defaulting to HTTP/1.1 for legacy implementations
     */
    default Protocol protocol() {
        return Protocol.HTTP_1_1;
    }

    /**
     * Returns whether this physical connection supports concurrent logical leases.
     *
     * @return true when multiple logical operations may share this physical connection
     */
    default boolean multiplex() {
        return false;
    }

    /**
     * Returns the number of additional logical leases currently available.
     *
     * @return number of additional logical leases currently available; one by default
     */
    default int capacity() {
        return 1;
    }

    /**
     * Returns whether this connection can accept at least one new logical lease using only lightweight state reads.
     *
     * @return true when the connection is healthy, not draining, and reports positive capacity
     */
    default boolean reusable() {
        return healthy() && !draining() && capacity() > 0;
    }

    /**
     * Returns whether this connection refuses new logical leases while existing work drains.
     *
     * @return true when existing logical work may finish but new leases must be refused
     */
    default boolean draining() {
        return false;
    }

    /**
     * Returns the multiplex state bridge, if supported.
     *
     * @return protocol-neutral multiplex state bridge, or null when multiplexing is unsupported
     */
    default MultiplexAttachment multiplexAttachment() {
        return null;
    }

    /**
     * Returns the connection-local sequential protocol session, when supported.
     *
     * <p>
     * This attachment is distinct from the multiplex attachment: it is used only while a physical connection has an
     * exclusive lease, allowing protocol buffers to survive across sequential exchanges.
     * </p>
     *
     * @return attached sequential protocol session, or {@code null}
     */
    default Object protocolAttachment() {
        return null;
    }

    /**
     * Atomically installs a connection-local sequential protocol session.
     *
     * @param expected currently expected session
     * @param update   replacement session
     * @return true when the attachment was updated; false when unsupported or concurrently changed
     */
    default boolean compareAndSetProtocolAttachment(final Object expected, final Object update) {
        return false;
    }

    /**
     * Closes this connection.
     */
    @Override
    void close();

    /**
     * Aborts a connection that cannot be reused.
     * <p>
     * Plain transports have no separate abort protocol, so the compatibility default delegates to {@link #close()}.
     * Layered transports may override this method to skip graceful shutdown handshakes after a protocol error,
     * cancellation, or an explicit {@code Connection: close} exchange.
     * </p>
     */
    default void abort() {
        close();
    }

    /**
     * Listener for changes to multiplex stream capacity or draining state.
     */
    @FunctionalInterface
    interface CapacityListener {

        /**
         * Receives the latest usable logical capacity and draining state.
         *
         * @param capacity latest number of additional logical streams that may be opened
         * @param draining true when the connection refuses new streams regardless of reported capacity
         */
        void changed(int capacity, boolean draining);

    }

    /**
     * Closeable listener registration.
     */
    @FunctionalInterface
    interface Registration extends AutoCloseable {

        /**
         * Unregisters the listener without throwing checked exceptions.
         */
        @Override
        void close();

    }

    /**
     * State bridge owned by a concrete multiplex-capable connection.
     *
     * <p>
     * The session remains protocol-neutral at this layer. Its concrete type is agreed by the HTTP transport and HTTP/2
     * owner; the connection pool only observes capacity and draining changes.
     * </p>
     */
    interface MultiplexAttachment {

        /**
         * Returns the currently attached protocol session, or {@code null} before installation.
         *
         * @return currently attached protocol-session object, or null before installation
         */
        Object session();

        /**
         * Atomically changes the attached protocol session.
         *
         * @param expected session reference required to be currently attached
         * @param update   replacement protocol-session reference
         * @return true when the attached reference matched and was replaced atomically
         */
        boolean compareAndSetSession(Object expected, Object update);

        /**
         * Returns the latest usable logical stream capacity.
         *
         * @return latest number of additional logical streams that may be opened
         */
        int capacity();

        /**
         * Returns whether the protocol session is draining.
         *
         * @return true when the attached protocol session refuses new streams
         */
        boolean draining();

        /**
         * Registers a capacity listener.
         *
         * @param listener callback to notify after capacity or draining-state publication
         * @return closeable registration that removes the callback
         */
        Registration listen(CapacityListener listener);

        /**
         * Publishes a protocol-owned capacity snapshot.
         *
         * @param capacity latest number of additional logical streams that may be opened
         * @param draining true when the connection must refuse new streams
         */
        void publish(int capacity, boolean draining);

    }

}
