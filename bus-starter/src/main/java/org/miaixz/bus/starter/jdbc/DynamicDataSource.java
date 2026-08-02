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
package org.miaixz.bus.starter.jdbc;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import org.miaixz.bus.mapper.dialect.DialectRegistry;

/**
 * A dynamic, routing data source that extends {@link AbstractRoutingDataSource}.
 * <p>
 * This class determines the data source to use at runtime based on a lookup key stored in a thread-local variable,
 * managed by {@link DataSourceHolder}. It allows for switching between multiple configured data sources dynamically.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * A set containing the keys of all registered data sources.
     */
    private final Set<Object> keySet = new LinkedHashSet<>();

    /**
     * Target data sources owned by this routing bean.
     */
    private final Map<Object, Object> targetDataSources = new LinkedHashMap<>();

    /**
     * Primary data source key owned by this routing bean.
     */
    private String primary;

    /**
     * Creates an independent dynamic data source bean.
     */
    public DynamicDataSource() {
        // No initialization required.
    }

    /**
     * Sets the primary data source key for this routing bean.
     *
     * @param primary configured primary key
     */
    public void setPrimary(String primary) {
        if (primary == null || primary.isBlank()) {
            throw new IllegalArgumentException("Primary data source key must not be blank");
        }
        this.primary = primary.trim();
    }

    /**
     * Determines the current lookup key for the data source.
     * <p>
     * This method is called by the framework to decide which data source to use. It retrieves the key from the
     * thread-local {@link DataSourceHolder}.
     * </p>
     *
     * @return The lookup key for the current data source.
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String key = DataSourceHolder.getCurrentKey();
        if (key == null) {
            return this.primary;
        }
        if (!this.keySet.contains(key)) {
            throw new IllegalStateException("Unable to locate datasource by key '" + key + "'");
        }
        return key;
    }

    /**
     * Populates the bean-local set of registered keys after data source resolution.
     */
    @Override
    public void afterPropertiesSet() {
        if (this.primary == null || !this.targetDataSources.containsKey(this.primary)) {
            throw new IllegalStateException("Primary data source key is not registered");
        }
        super.setTargetDataSources(this.targetDataSources);
        super.setDefaultTargetDataSource(this.targetDataSources.get(this.primary));
        super.setLenientFallback(false);
        super.afterPropertiesSet();
        this.keySet.clear();
        this.keySet.addAll(getResolvedDataSources().keySet());
        getResolvedDataSources()
                .forEach((key, dataSource) -> DialectRegistry.initializeDialect(String.valueOf(key), dataSource));
    }

    /**
     * Sets the target data sources and updates the internal key set.
     *
     * @param map A map of data source keys to data source instances.
     */
    @Override
    public void setTargetDataSources(Map<Object, Object> map) {
        this.targetDataSources.clear();
        this.targetDataSources.putAll(map);
        this.keySet.clear();
        this.keySet.addAll(this.targetDataSources.keySet());
        super.setTargetDataSources(this.targetDataSources);
    }

    /**
     * Retrieves the mutable backing map of all configured target data sources.
     * <p>
     * The live-map behavior is retained for compatibility. Call {@link #afterPropertiesSet()} after direct mutations,
     * or prefer {@link #addDataSource(String, javax.sql.DataSource)} and {@link #remove(String)}.
     *
     * @return A map of all data sources.
     */
    public Map<Object, Object> getAllDataSources() {
        return Map.copyOf(this.targetDataSources);
    }

    /**
     * Dynamically adds a new data source. Note: This method attempts to modify the resolved data sources map via
     * reflection, which might be fragile and dependent on the Spring Framework's internal implementation.
     *
     * @param key        The unique key for the new data source.
     * @param dataSource The data source instance to add.
     */
    public synchronized void addDataSource(String key, javax.sql.DataSource dataSource) {
        this.targetDataSources.put(key, dataSource);
        afterPropertiesSet();
    }

    /**
     * Checks if a data source with the specified key exists.
     *
     * @param key The data source key to check.
     * @return {@code true} if the key exists, {@code false} otherwise.
     */
    public boolean containsKey(String key) {
        return this.keySet.contains(key);
    }

    /**
     * Dynamically removes a data source.
     *
     * @param key The key of the data source to remove.
     */
    public synchronized void remove(String key) {
        if (this.primary.equals(key)) {
            throw new IllegalArgumentException("Primary data source cannot be removed");
        }
        this.targetDataSources.remove(key);
        afterPropertiesSet();
    }

    /**
     * Retrieves the currently resolved target data source.
     *
     * @return The active {@link javax.sql.DataSource} instance.
     */
    public javax.sql.DataSource getCurrentDataSource() {
        return super.determineTargetDataSource();
    }

}
