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

import java.util.ArrayList;
import java.util.List;
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
import org.miaixz.bus.fabric.observe.metrics.FabricMeter;
import org.miaixz.bus.fabric.registry.connection.ConnectionPool;
import org.miaixz.bus.fabric.registry.connection.ConnectionRegistry;
import org.miaixz.bus.fabric.registry.policy.PoolPolicy;
import org.miaixz.bus.fabric.registry.route.Selector;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * Registry directory keyed by stable registry names.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Directory implements AutoCloseable {

    /**
     * Ledgers indexed by normalized stable registry name.
     */
    private final ConcurrentHashMap<String, Ledger<?>> ledgers;

    /**
     * Lazily created context-scoped services indexed by normalized name.
     */
    private final ConcurrentHashMap<String, Object> services;

    /**
     * Lock serializing service creation and directory closure.
     */
    private final Object serviceLock;

    /**
     * One-way state preventing new registry and service access after ownership ends.
     */
    private final AtomicBoolean closed;

    /**
     * Shared connection registry.
     */
    private final ConnectionRegistry connections;

    /**
     * Shared connection pool.
     */
    private final ConnectionPool connectionPool;

    /**
     * Lazily initialized route selector, cleared when the directory closes.
     */
    private volatile Selector selector;

    /**
     * Creates a directory with one clock-bound connection pool.
     *
     * @param connectionPool clock-bound connection pool
     * @param connections    shared connection registry
     */
    private Directory(final ConnectionPool connectionPool, final ConnectionRegistry connections) {
        this.ledgers = new ConcurrentHashMap<>();
        this.services = new ConcurrentHashMap<>();
        this.serviceLock = new Object();
        this.closed = new AtomicBoolean();
        this.connectionPool = require(connectionPool, "Connection pool");
        this.connections = require(connections, "Connection registry");
    }

    /**
     * Creates a registry directory with default registry slots.
     *
     * @return initialized directory using the system clock, default pool policy, and a new meter
     */
    public static Directory create() {
        return create(Clock.system());
    }

    /**
     * Creates a registry directory whose connection pool uses the supplied clock.
     *
     * @param clock time source shared by the directory meter and connection pool
     * @return initialized directory with default pool policy and a new meter
     * @throws ValidateException if {@code clock} is {@code null}
     */
    public static Directory create(final Clock clock) {
        final Clock currentClock = require(clock, "Clock");
        final FabricMeter meter = FabricMeter.create(currentClock);
        return create(currentClock, PoolPolicy.defaults(), meter, null);
    }

    /**
     * Creates a directory whose registries share the supplied runtime services.
     *
     * @param clock      directory and connection-pool time source
     * @param policy     connection-pool policy, or {@code null} to use the pool defaults
     * @param meter      meter borrowed by the directory and its registries
     * @param dispatcher dispatcher borrowed by the connection pool, or {@code null} for pool-owned dispatch
     * @return initialized registry directory
     * @throws ValidateException if {@code clock} or {@code meter} is {@code null}
     */
    public static Directory create(
            final Clock clock,
            final PoolPolicy policy,
            final FabricMeter meter,
            final Dispatcher dispatcher) {
        final Clock currentClock = require(clock, "Clock");
        final FabricMeter currentMeter = require(meter, "Fabric meter");
        final ConnectionPool pool = dispatcher == null ? ConnectionPool.create(policy, currentClock, currentMeter)
                : ConnectionPool.create(policy, currentClock, currentMeter, dispatcher);
        final ConnectionRegistry registry = new ConnectionRegistry(currentMeter);
        final Directory directory = new Directory(pool, registry);
        try {
            directory.register(Builder.DIRECTORY_CONNECTION, Ledger.create());
            directory.register(Builder.ROUTE, Ledger.create());
            directory.register(Builder.DIRECTORY_RESOLVER, Ledger.create());
            directory.register(Builder.DIRECTORY_PROXY, Ledger.create());
            directory.register(Builder.DIRECTORY_POLICY, Ledger.create());
            return directory;
        } catch (final RuntimeException | Error failure) {
            try {
                registry.close();
                pool.close();
            } catch (final Throwable closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }

    /**
     * Registers or replaces a ledger under a normalized name.
     * <p>
     * A replaced ledger is not closed by this operation.
     * </p>
     *
     * @param name   non-blank, single-line registry name
     * @param ledger non-null ledger stored under the name
     * @param <T>    ledger value type
     * @throws ValidateException if the name is invalid or {@code ledger} is {@code null}
     * @throws StatefulException if the directory is closed
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
     * @param name non-blank, single-line registry name
     * @param <T>  expected ledger value type, not checked at runtime
     * @return registered ledger cast to the requested generic type, or {@code null} when absent
     * @throws ValidateException if the name is invalid
     * @throws StatefulException if the directory is closed
     */
    public <T> Ledger<T> ledger(final String name) {
        ensureOpen();
        return (Ledger<T>) ledgers.get(validateName(name));
    }

    /**
     * Returns an immutable weakly consistent snapshot of currently registered ledgers.
     *
     * @return immutable name-to-ledger map; this method remains available after close and then normally returns empty
     */
    public Map<String, Ledger<?>> snapshot() {
        return Map.copyOf(ledgers);
    }

    /**
     * Returns the shared connection registry.
     *
     * @return connection registry owned by this directory
     * @throws StatefulException if the directory is closed
     */
    public ConnectionRegistry connections() {
        ensureOpen();
        return connections;
    }

    /**
     * Returns the shared connection pool.
     *
     * @return connection pool owned by this directory
     * @throws StatefulException if the directory is closed
     */
    public ConnectionPool connectionPool() {
        ensureOpen();
        return connectionPool;
    }

    /**
     * Returns the shared selector.
     *
     * @return lazily created selector shared by this directory
     * @throws StatefulException if the directory is closed
     */
    public Selector selector() {
        ensureOpen();
        Selector current = selector;
        if (current == null) {
            synchronized (serviceLock) {
                ensureOpen();
                current = selector;
                if (current == null) {
                    current = new Selector();
                    selector = current;
                }
            }
        }
        return current;
    }

    /**
     * Returns one context-scoped service, invoking its factory at most once for a name.
     *
     * @param name    non-blank, single-line service name
     * @param type    runtime type required for existing and newly created values
     * @param factory factory invoked under the service lock only when no value is stored
     * @param <T>     service type
     * @return existing service of the requested type or the newly stored factory result
     * @throws ValidateException if an argument is invalid, the factory returns null, or a value has the wrong type
     * @throws StatefulException if the directory is closed
     */
    public <T> T service(final String name, final Class<T> type, final SupplierX<? extends T> factory) {
        final String key = validateName(name);
        final Class<T> expectedType = require(type, "Service type");
        final SupplierX<? extends T> creator = require(factory, "Service factory");
        final Object cached = services.get(key);
        if (cached != null) {
            if (!expectedType.isInstance(cached)) {
                throw new ValidateException("Service " + key + " is not a " + expectedType.getName());
            }
            return expectedType.cast(cached);
        }
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
     * Atomically prevents new access, detaches services and ledgers, then closes closeable services, the connection
     * pool, the connection registry, and closeable ledgers outside the service lock.
     *
     * @throws InternalException if one or more owned resources fail to close; later failures are suppressed
     */
    @Override
    public void close() {
        final List<AutoCloseable> resources = new ArrayList<>();
        synchronized (serviceLock) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            for (final Object service : services.values()) {
                if (service instanceof AutoCloseable closeable) {
                    resources.add(closeable);
                }
            }
            services.clear();
            resources.add(connectionPool);
            resources.add(connections);
            for (final Ledger<?> ledger : ledgers.values()) {
                if (ledger instanceof AutoCloseable closeable) {
                    resources.add(closeable);
                }
            }
            ledgers.clear();
            selector = null;
        }
        Throwable failure = null;
        for (final AutoCloseable resource : resources) {
            failure = closeResource(resource, failure);
        }
        if (failure != null) {
            throw new InternalException("Unable to close registry directory", failure);
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
     *
     * @throws StatefulException if this directory is closed
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new StatefulException("Registry directory is closed");
        }
    }

    /**
     * Validates registry names.
     *
     * @param name candidate registry or service name
     * @return trimmed, non-blank, single-line name
     * @throws ValidateException if {@code name} is blank or contains a carriage return or line feed
     */
    private static String validateName(final String name) {
        if (StringKit.isBlank(name) || StringKit.containsAny(name, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Registry name must be non-blank and single-line");
        }
        return name.trim();
    }

    /**
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
