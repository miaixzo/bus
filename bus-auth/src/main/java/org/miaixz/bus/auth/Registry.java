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
package org.miaixz.bus.auth;

import java.util.Collection;

/**
 * Aggregates registration lifecycle operations for keyed {@link Connector} declarations during framework assembly.
 * <p>
 * Implementations invoke {@link Connector#connect(Registry)} only inside an atomic build-time registration operation. A
 * failed callback must leave the registry unchanged. Unregistration removes the complete set owned by a stable key
 * without calling back into the connector. A registry does not execute adapters, perform network I/O, load project
 * data, or expose the committed runtime {@link Roster}; mutation must be rejected after assembly is frozen.
 * </p>
 *
 * @param <K> stable registration key type
 * @param <C> exact connector type accepted by the registry
 * @author Kimi Liu
 */
public interface Registry<K, C extends Registry.Connector<K, ?>> {

    /**
     * Registers one complete registration.
     *
     * @param connector connector whose complete registration set is registered
     * @return this registry
     */
    Registry<K, C> register(C connector);

    /**
     * Atomically registers all supplied registrations in iteration order.
     *
     * @param connectors connectors whose complete registration sets are registered
     * @return this registry
     */
    Registry<K, C> registerAll(Collection<? extends C> connectors);

    /**
     * Removes the complete registration owned by one stable key.
     *
     * @param key registration key to remove
     * @return this registry
     */
    Registry<K, C> unregister(K key);

    /**
     * Atomically removes every registration owned by the supplied stable keys.
     *
     * @param keys registration keys to remove
     * @return this registry
     */
    Registry<K, C> unregisterAll(Collection<? extends K> keys);

    /**
     * Reports whether a registration is registered under one stable key.
     *
     * @param key registration key to inspect
     * @return {@code true} when the key is registered
     */
    boolean contains(K key);

    /**
     * Declares one complete, independently removable registration set for a build-scoped {@link Registry}.
     * <p>
     * A connector owns one stable key and declares every registration associated with that key through the supplied
     * typed registry. {@link #connect(Registry)} is a synchronous build-time callback: it does not establish a remote
     * connection, perform network I/O, load project data, access a runtime {@link Roster}, or retain the registry after
     * the callback returns. Registration removal remains exclusively owned by {@link Registry#unregister(Object)}.
     * </p>
     *
     * @param <K> stable registration key type
     * @param <R> exact build-scoped registry accepted by the connector
     * @author Kimi Liu
     */
    interface Connector<K, R extends Registry<K, ?>> {

        /**
         * Returns the stable key that owns every registration emitted by this connector.
         *
         * @return stable registration key
         */
        K key();

        /**
         * Connects the complete registration set owned by this connector to the supplied build-scoped registry.
         * <p>
         * Implementations may invoke only the binding operations exposed by the registry. They must not retain the
         * registry or perform runtime, project-data, persistence, cache, adapter-execution, or network
         * responsibilities.
         * </p>
         *
         * @param registry mutable build-scoped registry
         */
        void connect(R registry);

    }

}
