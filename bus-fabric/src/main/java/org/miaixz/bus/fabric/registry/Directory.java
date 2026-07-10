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
package org.miaixz.bus.fabric.registry;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.registry.connection.ConnectionPool;
import org.miaixz.bus.fabric.registry.connection.ConnectionRegistry;
import org.miaixz.bus.fabric.registry.route.Selector;

/**
 * Registry directory keyed by stable registry names.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Directory implements AutoCloseable {

    /**
     * Connection registry key.
     */
    private static final String CONNECTION = "connection";

    /**
     * Route registry key.
     */
    private static final String ROUTE = "route";

    /**
     * Resolver registry key.
     */
    private static final String RESOLVER = "resolver";

    /**
     * Proxy registry key.
     */
    private static final String PROXY = "proxy";

    /**
     * Policy registry key.
     */
    private static final String POLICY = "policy";

    /**
     * Registered ledgers.
     */
    private final ConcurrentHashMap<String, Ledger<?>> ledgers;

    /**
     * Shared connection registry.
     */
    private volatile ConnectionRegistry connections;

    /**
     * Shared connection pool.
     */
    private volatile ConnectionPool connectionPool;

    /**
     * Shared selector.
     */
    private volatile Selector selector;

    /**
     * Creates a directory with a backing map.
     */
    private Directory() {
        this.ledgers = new ConcurrentHashMap<>();
    }

    /**
     * Creates a registry directory with default registry slots.
     *
     * @return registry directory
     */
    public static Directory create() {
        final Directory directory = new Directory();
        directory.register(CONNECTION, Ledger.create());
        directory.register(ROUTE, Ledger.create());
        directory.register(RESOLVER, Ledger.create());
        directory.register(PROXY, Ledger.create());
        directory.register(POLICY, Ledger.create());
        return directory;
    }

    /**
     * Registers a ledger.
     *
     * @param name   ledger name
     * @param ledger ledger instance
     * @param <T>    value type
     */
    public <T> void register(final String name, final Ledger<T> ledger) {
        if (ledger == null) {
            throw new ValidateException("Registry ledger must not be null");
        }
        ledgers.put(validateName(name), ledger);
    }

    /**
     * Reads a registered ledger.
     *
     * @param name ledger name
     * @param <T>  value type
     * @return ledger or null
     */
    public <T> Ledger<T> ledger(final String name) {
        return (Ledger<T>) ledgers.get(validateName(name));
    }

    /**
     * Returns an immutable directory snapshot.
     *
     * @return directory snapshot
     */
    public Map<String, Ledger<?>> snapshot() {
        return Collections.unmodifiableMap(Map.copyOf(ledgers));
    }

    /**
     * Returns the shared connection registry.
     *
     * @return connection registry
     */
    public synchronized ConnectionRegistry connections() {
        if (connections == null) {
            connections = new ConnectionRegistry();
        }
        return connections;
    }

    /**
     * Returns the shared connection pool.
     *
     * @return connection pool
     */
    public synchronized ConnectionPool connectionPool() {
        if (connectionPool == null) {
            connectionPool = ConnectionPool.create(null);
        }
        return connectionPool;
    }

    /**
     * Returns the shared selector.
     *
     * @return selector
     */
    public synchronized Selector selector() {
        if (selector == null) {
            selector = new Selector();
        }
        return selector;
    }

    /**
     * Closes registered closeable ledgers.
     */
    @Override
    public void close() {
        Exception failure = null;
        final ConnectionPool pool = connectionPool;
        if (pool != null) {
            try {
                pool.close();
            } catch (final RuntimeException e) {
                failure = e;
            }
        }
        for (final Ledger<?> ledger : ledgers.values()) {
            if (ledger instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (final Exception ignored) {
                    if (failure == null) {
                        failure = ignored;
                    }
                }
            }
        }
        ledgers.clear();
        if (failure != null) {
            throw new InternalException("Unable to close registry directory", failure);
        }
    }

    /**
     * Validates registry names.
     *
     * @param name registry name
     * @return valid name
     */
    private static String validateName(final String name) {
        if (StringKit.isBlank(name) || StringKit.containsAny(name, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Registry name must be non-blank and single-line");
        }
        return name.trim();
    }

}
