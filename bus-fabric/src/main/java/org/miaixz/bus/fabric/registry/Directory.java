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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
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
     * Registered ledgers.
     */
    private final ConcurrentHashMap<String, Ledger<?>> ledgers;

    /**
     * Context-scoped shared services.
     */
    private final ConcurrentHashMap<String, Object> services;

    /**
     * Lock serializing service creation and directory closure.
     */
    private final Object serviceLock;

    /**
     * Closed state.
     */
    private final AtomicBoolean closed;

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
     * Creates a directory with one clock-bound connection pool.
     *
     * @param connectionPool clock-bound connection pool
     */
    private Directory(final ConnectionPool connectionPool) {
        this.ledgers = new ConcurrentHashMap<>();
        this.services = new ConcurrentHashMap<>();
        this.serviceLock = new Object();
        this.closed = new AtomicBoolean();
        this.connectionPool = require(connectionPool, "Connection pool");
    }

    /**
     * Creates a registry directory with default registry slots.
     *
     * @return registry directory
     */
    public static Directory create() {
        return create(Clock.system());
    }

    /**
     * Creates a registry directory whose connection pool uses the supplied clock.
     *
     * @param clock unique directory clock
     * @return registry directory
     */
    public static Directory create(final Clock clock) {
        final ConnectionPool pool = ConnectionPool.create(null, require(clock, "Clock"));
        final Directory directory = new Directory(pool);
        try {
            directory.register(Builder.DIRECTORY_CONNECTION, Ledger.create());
            directory.register(Builder.ROUTE, Ledger.create());
            directory.register(Builder.DIRECTORY_RESOLVER, Ledger.create());
            directory.register(Builder.DIRECTORY_PROXY, Ledger.create());
            directory.register(Builder.DIRECTORY_POLICY, Ledger.create());
            return directory;
        } catch (final RuntimeException | Error failure) {
            try {
                pool.close();
            } catch (final Throwable closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }

    /**
     * Registers a ledger.
     *
     * @param name   ledger name
     * @param ledger ledger instance
     * @param <T>    value type
     */
    public <T> void register(final String name, final Ledger<T> ledger) {
        final String key = validateName(name);
        final Ledger<T> value = require(ledger, "Registry ledger");
        synchronized (serviceLock) {
            ensureOpen();
            ledgers.put(key, value);
        }
    }

    /**
     * Reads a registered ledger.
     *
     * @param name ledger name
     * @param <T>  value type
     * @return ledger or null
     */
    public <T> Ledger<T> ledger(final String name) {
        synchronized (serviceLock) {
            ensureOpen();
            return (Ledger<T>) ledgers.get(validateName(name));
        }
    }

    /**
     * Returns an immutable directory snapshot.
     *
     * @return directory snapshot
     */
    public Map<String, Ledger<?>> snapshot() {
        return Map.copyOf(ledgers);
    }

    /**
     * Returns the shared connection registry.
     *
     * @return connection registry
     */
    public ConnectionRegistry connections() {
        synchronized (serviceLock) {
            ensureOpen();
            if (connections == null) {
                connections = new ConnectionRegistry();
            }
            return connections;
        }
    }

    /**
     * Returns the shared connection pool.
     *
     * @return connection pool
     */
    public ConnectionPool connectionPool() {
        synchronized (serviceLock) {
            ensureOpen();
            return connectionPool;
        }
    }

    /**
     * Returns the shared selector.
     *
     * @return selector
     */
    public Selector selector() {
        synchronized (serviceLock) {
            ensureOpen();
            if (selector == null) {
                selector = new Selector();
            }
            return selector;
        }
    }

    /**
     * Returns one context-scoped service, invoking its factory at most once for a name.
     *
     * @param name    stable service name
     * @param type    required service type
     * @param factory service factory
     * @param <T>     service type
     * @return existing or newly created service
     */
    public <T> T service(final String name, final Class<T> type, final SupplierX<? extends T> factory) {
        final String key = validateName(name);
        final Class<T> expectedType = require(type, "Service type");
        final SupplierX<? extends T> creator = require(factory, "Service factory");
        synchronized (serviceLock) {
            ensureOpen();
            final Object existing = services.get(key);
            if (existing != null) {
                if (!expectedType.isInstance(existing)) {
                    throw new ValidateException("Service " + key + " is not a " + expectedType.getName());
                }
                return expectedType.cast(existing);
            }
            final T created = creator.get();
            if (created == null) {
                throw new ValidateException("Service factory must not return null: " + key);
            }
            if (!expectedType.isInstance(created)) {
                throw new ValidateException("Service " + key + " is not a " + expectedType.getName());
            }
            services.put(key, created);
            return created;
        }
    }

    /**
     * Closes registered closeable ledgers.
     */
    @Override
    public void close() {
        synchronized (serviceLock) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            Throwable failure = null;
            for (final Object service : services.values()) {
                if (service instanceof AutoCloseable closeable) {
                    failure = closeResource(closeable, failure);
                }
            }
            services.clear();
            final ConnectionPool pool = connectionPool;
            if (pool != null) {
                failure = closeResource(pool, failure);
            }
            connectionPool = null;
            for (final Ledger<?> ledger : ledgers.values()) {
                if (ledger instanceof AutoCloseable closeable) {
                    failure = closeResource(closeable, failure);
                }
            }
            ledgers.clear();
            connections = null;
            selector = null;
            if (failure != null) {
                throw new InternalException("Unable to close registry directory", failure);
            }
        }
    }

    /**
     * Closes one directory-owned resource and aggregates failures without skipping later resources.
     *
     * @param resource resource to close
     * @param failure  first failure, or null
     * @return first failure with later failures suppressed
     */
    private static Throwable closeResource(final AutoCloseable resource, final Throwable failure) {
        Throwable result = failure;
        try {
            resource.close();
        } catch (final Throwable current) {
            if (result == null) {
                result = current;
            } else {
                result.addSuppressed(current);
            }
        }
        return result;
    }

    /**
     * Rejects access after directory ownership has ended.
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new StatefulException("Registry directory is closed");
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

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
