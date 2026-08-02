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
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Routes JDBC access to a registered datasource using the current {@link DataSourceHolder} key.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

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
     * Creates an independent dynamic datasource bean.
     */
    public DynamicDataSource() {
        // No initialization required.
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
     * Resolves registered routes with strict fallback behavior.
     */
    @Override
    public void afterPropertiesSet() {
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
    public void setTargetDataSources(Map<Object, Object> map) {
        this.targetDataSources.clear();
        this.targetDataSources.putAll(map);
        this.keySet.clear();
        this.keySet.addAll(this.targetDataSources.keySet());
        super.setTargetDataSources(this.targetDataSources);
    }

    /**
     * Returns an immutable view of configured datasource targets.
     *
     * @return datasource targets keyed by routing name
     */
    public Map<Object, Object> getAllDataSources() {
        return Map.copyOf(this.targetDataSources);
    }

    /**
     * Adds or replaces one datasource and refreshes resolved routes.
     *
     * @param key        routing key
     * @param dataSource datasource instance
     */
    public synchronized void addDataSource(String key, DataSource dataSource) {
        this.targetDataSources.put(key, dataSource);
        afterPropertiesSet();
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
        if (this.primary.equals(key)) {
            throw new IllegalArgumentException("Primary datasource cannot be removed");
        }
        this.targetDataSources.remove(key);
        afterPropertiesSet();
    }

    /**
     * Returns the datasource selected for the current routing scope.
     *
     * @return active datasource
     */
    public DataSource getCurrentDataSource() {
        return super.determineTargetDataSource();
    }

}
