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
     * @return connection destination
     */
    Destination destination();

    /**
     * Returns the underlying conduit.
     *
     * @return network conduit
     */
    Conduit conduit();

    /**
     * Returns the protocol-layer read view.
     *
     * @return protocol-layer source
     */
    Source source();

    /**
     * Returns the protocol-layer write view.
     *
     * @return protocol-layer sink
     */
    Sink sink();

    /**
     * Returns whether the connection is healthy.
     *
     * @return true when healthy
     */
    boolean healthy();

    /**
     * Returns whether the connection is idle.
     *
     * @return true when idle
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
     * @return established protocol
     */
    default Protocol protocol() {
        return Protocol.HTTP_1_1;
    }

    /**
     * Returns whether this physical connection supports concurrent logical leases.
     *
     * @return true when multiplex capable
     */
    default boolean multiplex() {
        return false;
    }

    /**
     * Returns the maximum number of concurrent logical leases currently supported.
     *
     * @return logical lease capacity
     */
    default int capacity() {
        return 1;
    }

    /**
     * Returns whether this connection refuses new logical leases while existing work drains.
     *
     * @return true when draining
     */
    default boolean draining() {
        return false;
    }

    /**
     * Returns the multiplex state bridge, if supported.
     *
     * @return multiplex attachment or null
     */
    default MultiplexAttachment multiplexAttachment() {
        return null;
    }

    /**
     * Closes this connection.
     */
    @Override
    void close();

    /**
     * Listener for changes to multiplex stream capacity or draining state.
     */
    @FunctionalInterface
    interface CapacityListener {

        /**
         * Receives the latest usable logical capacity and draining state.
         *
         * @param capacity usable logical capacity
         * @param draining true when no new streams may be opened
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
         * @return attached session or null
         */
        Object session();

        /**
         * Atomically changes the attached protocol session.
         *
         * @param expected expected session
         * @param update   replacement session
         * @return true when the session was changed
         */
        boolean compareAndSetSession(Object expected, Object update);

        /**
         * Returns the latest usable logical stream capacity.
         *
         * @return usable stream capacity
         */
        int capacity();

        /**
         * Returns whether the protocol session is draining.
         *
         * @return true when draining
         */
        boolean draining();

        /**
         * Registers a capacity listener.
         *
         * @param listener listener
         * @return closeable registration
         */
        Registration listen(CapacityListener listener);

        /**
         * Publishes a protocol-owned capacity snapshot.
         *
         * @param capacity usable stream capacity
         * @param draining true when draining
         */
        void publish(int capacity, boolean draining);

    }

}
