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
package org.miaixz.bus.spring.jdbc;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Routes JDBC access to a registered datasource using the current {@link DataSourceHolder} key.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DynamicDataSource extends AbstractRoutingDataSource implements AutoCloseable {

    /**
     * Application-context-scoped datasource routing state.
     */
    private final DataSourceHolder dataSourceHolder;

    /**
     * Ordered observers of successful route changes.
     */
    private final List<DataSourceListener> listeners;

    /**
     * Keys of all resolved datasources.
     */
    private final Set<Object> keySet = new LinkedHashSet<>();

    /**
     * Datasources owned by this routing bean.
     */
    private final Map<Object, Object> targetDataSources = new LinkedHashMap<>();

    /**
     * Primary datasource key owned by this routing bean.
     */
    private String primary;

    /**
     * Whether the initial datasource routes have been resolved.
     */
    private boolean initialized;

    /**
     * Whether this routing datasource has released its owned connection pools.
     */
    private boolean closed;

    /**
     * Creates an independent dynamic datasource bean.
     *
     * @param dataSourceHolder application-context-scoped routing state
     * @param listeners        ordered datasource route listeners
     */
    public DynamicDataSource(DataSourceHolder dataSourceHolder, List<DataSourceListener> listeners) {
        this.dataSourceHolder = Objects.requireNonNull(dataSourceHolder, "dataSourceHolder");
        this.listeners = List.copyOf(listeners == null ? List.of() : listeners);
    }

    /**
     * Sets the primary datasource key.
     *
     * @param primary configured primary key
     */
    public void setPrimary(String primary) {
        if (primary == null || primary.isBlank()) {
            throw new IllegalArgumentException("Primary datasource key must not be blank");
        }
        this.primary = primary.trim();
    }

    /**
     * Returns the explicitly selected key or the primary route.
     *
     * @return effective datasource lookup key
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String key = this.dataSourceHolder.getCurrentKey();
        if (key == null) {
            return this.primary;
        }
        if (!this.keySet.contains(key)) {
            throw new IllegalStateException("Unable to locate datasource by key '" + key + "'");
        }
        return key;
    }

    /**
     * Resolves registered routes with strict fallback behavior.
     */
    @Override
    public synchronized void afterPropertiesSet() {
        ensureOpen();
        try {
            refreshRoutes();
            if (!this.initialized) {
                this.initialized = true;
                this.targetDataSources.forEach((key, value) -> notifyAdded(key, (DataSource) value));
            }
        } catch (RuntimeException | Error exception) {
            close();
            throw exception;
        }
    }

    /**
     * Rebuilds Spring's resolved datasource map from the current route definitions.
     */
    private void refreshRoutes() {
        if (this.primary == null || !this.targetDataSources.containsKey(this.primary)) {
            throw new IllegalStateException("Primary datasource key is not registered");
        }
        super.setTargetDataSources(this.targetDataSources);
        super.setDefaultTargetDataSource(this.targetDataSources.get(this.primary));
        super.setLenientFallback(false);
        super.afterPropertiesSet();
        this.keySet.clear();
        this.keySet.addAll(getResolvedDataSources().keySet());
    }

    /**
     * Replaces all configured datasource targets.
     *
     * @param map datasource instances keyed by routing name
     */
    @Override
    public synchronized void setTargetDataSources(Map<Object, Object> map) {
        ensureOpen();
        Objects.requireNonNull(map, "map");
        Map<Object, Object> previous = new LinkedHashMap<>(this.targetDataSources);
        this.targetDataSources.clear();
        this.targetDataSources.putAll(map);
        this.keySet.clear();
        this.keySet.addAll(this.targetDataSources.keySet());
        super.setTargetDataSources(this.targetDataSources);
        if (!this.initialized) {
            return;
        }
        try {
            refreshRoutes();
        } catch (RuntimeException | Error exception) {
            this.targetDataSources.clear();
            this.targetDataSources.putAll(previous);
            this.keySet.clear();
            this.keySet.addAll(previous.keySet());
            super.setTargetDataSources(previous);
            refreshRoutes();
            throw exception;
        }
        previous.forEach((key, value) -> {
            if (!Objects.equals(value, this.targetDataSources.get(key))) {
                removeOwned(key, (DataSource) value);
            }
        });
        this.targetDataSources.forEach((key, value) -> {
            if (!Objects.equals(value, previous.get(key))) {
                notifyAdded(key, (DataSource) value);
            }
        });
    }

    /**
     * Returns an immutable view of configured datasource targets.
     *
     * @return datasource targets keyed by routing name
     */
    public synchronized Map<Object, Object> getAllDataSources() {
        return Map.copyOf(this.targetDataSources);
    }

    /**
     * Adds or replaces one datasource and refreshes resolved routes.
     *
     * @param key        routing key
     * @param dataSource datasource instance
     */
    public synchronized void addDataSource(String key, DataSource dataSource) {
        ensureOpen();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Datasource key must not be blank");
        }
        Objects.requireNonNull(dataSource, "dataSource");
        String normalizedKey = key.trim();
        DataSource previous = (DataSource) this.targetDataSources.put(normalizedKey, dataSource);
        try {
            refreshRoutes();
        } catch (RuntimeException | Error exception) {
            if (previous == null) {
                this.targetDataSources.remove(normalizedKey);
            } else {
                this.targetDataSources.put(normalizedKey, previous);
            }
            refreshRoutes();
            throw exception;
        }
        if (previous != null && previous != dataSource) {
            removeOwned(normalizedKey, previous);
        }
        notifyAdded(normalizedKey, dataSource);
    }

    /**
     * Returns whether a datasource key is registered.
     *
     * @param key datasource key
     * @return {@code true} when the key is registered
     */
    public boolean containsKey(String key) {
        return this.keySet.contains(key);
    }

    /**
     * Removes a non-primary datasource and refreshes resolved routes.
     *
     * @param key datasource key
     */
    public synchronized void remove(String key) {
        ensureOpen();
        if (this.primary.equals(key)) {
            throw new IllegalArgumentException("Primary datasource cannot be removed");
        }
        DataSource removed = (DataSource) this.targetDataSources.remove(key);
        try {
            refreshRoutes();
        } catch (RuntimeException | Error exception) {
            if (removed != null) {
                this.targetDataSources.put(key, removed);
                refreshRoutes();
            }
            throw exception;
        }
        if (removed != null) {
            removeOwned(key, removed);
        }
    }

    /**
     * Releases every datasource owned by this routing bean and clears its thread routing state.
     * <p>
     * Closing is idempotent. Each datasource instance is closed at most once even when multiple routing keys reference
     * it.
     */
    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        Map<Object, Object> owned = new LinkedHashMap<>(this.targetDataSources);
        this.targetDataSources.clear();
        this.keySet.clear();
        this.initialized = false;
        this.dataSourceHolder.remove();

        Set<DataSource> released = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        RuntimeException failure = null;
        for (Map.Entry<Object, Object> entry : owned.entrySet()) {
            DataSource dataSource = (DataSource) entry.getValue();
            try {
                notifyRemoved(entry.getKey(), dataSource);
            } catch (RuntimeException exception) {
                failure = append(failure, exception);
            }
            if (released.add(dataSource)) {
                try {
                    closeDataSource(dataSource);
                } catch (RuntimeException exception) {
                    failure = append(failure, exception);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Returns the datasource selected for the current routing scope.
     *
     * @return active datasource
     */
    public DataSource getCurrentDataSource() {
        return super.determineTargetDataSource();
    }

    /**
     * Notifies ordered listeners that a route is available.
     *
     * @param key        routing key
     * @param dataSource registered datasource
     */
    private void notifyAdded(Object key, DataSource dataSource) {
        this.listeners.forEach(listener -> listener.onAdded(String.valueOf(key), dataSource));
    }

    /**
     * Notifies ordered listeners that a route is no longer available.
     *
     * @param key        routing key
     * @param dataSource removed datasource
     */
    private void notifyRemoved(Object key, DataSource dataSource) {
        this.listeners.forEach(listener -> listener.onRemoved(String.valueOf(key), dataSource));
    }

    /**
     * Notifies listeners and closes a removed datasource when no remaining route references it.
     *
     * @param key        removed routing key
     * @param dataSource removed datasource
     */
    private void removeOwned(Object key, DataSource dataSource) {
        try {
            notifyRemoved(key, dataSource);
        } finally {
            if (!isRegistered(dataSource)) {
                closeDataSource(dataSource);
            }
        }
    }

    /**
     * Returns whether a datasource instance remains registered under another key.
     *
     * @param candidate datasource instance
     * @return {@code true} when the exact instance remains registered
     */
    private boolean isRegistered(DataSource candidate) {
        return this.targetDataSources.values().stream().anyMatch(value -> value == candidate);
    }

    /**
     * Closes an owned datasource when it exposes an explicit close contract.
     *
     * @param dataSource owned datasource
     */
    private static void closeDataSource(DataSource dataSource) {
        if (dataSource instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (RuntimeException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to close datasource: " + dataSource.getClass().getName(),
                        exception);
            }
        }
    }

    /**
     * Adds a secondary lifecycle failure without losing the first failure.
     *
     * @param current   first failure, or {@code null}
     * @param secondary subsequent failure
     * @return retained first failure
     */
    private static RuntimeException append(RuntimeException current, RuntimeException secondary) {
        if (current == null) {
            return secondary;
        }
        current.addSuppressed(secondary);
        return current;
    }

    /**
     * Rejects route mutations after this datasource has been closed.
     */
    private void ensureOpen() {
        if (this.closed) {
            throw new IllegalStateException("Dynamic datasource is closed");
        }
    }

}
