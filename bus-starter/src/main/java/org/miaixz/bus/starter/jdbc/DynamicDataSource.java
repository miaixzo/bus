/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.jdbc;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.logger.Logger;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A dynamic, routing data source that extends {@link AbstractRoutingDataSource}.
 * <p>
 * This class determines the data source to use at runtime based on a lookup key stored in a thread-local variable,
 * managed by {@link DataSourceHolder}. It allows for switching between multiple configured data sources dynamically.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * A set containing the keys of all registered data sources.
     */
    private static final Set<Object> keySet = new LinkedHashSet<>();

    /**
     * A lock object for thread-safe singleton initialization.
     */
    private static final byte[] lock = Normal.EMPTY_BYTE_ARRAY;

    /**
     * The volatile singleton instance of the DynamicDataSource.
     */
    private static volatile DynamicDataSource INSTANCE;

    /**
     * Private constructor to support the singleton pattern.
     */
    private DynamicDataSource() {
    }

    /**
     * Returns the singleton instance of the DynamicDataSource.
     *
     * @return The singleton {@code DynamicDataSource} instance.
     */
    public static synchronized DynamicDataSource getInstance() {
        if (null == INSTANCE) {
            synchronized (lock) {
                if (null == INSTANCE) {
                    INSTANCE = new DynamicDataSource();
                }
            }
        }
        return INSTANCE;
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
        String key = DataSourceHolder.getKey();
        if (!keySet.contains(key)) {
            Logger.warn(true, "DataSource", "Unable to locate datasource by key '{}'. Default will be used.", key);
        }
        Logger.debug(true, "DataSource", "[{}]", key);
        return key;
    }

    /**
     * Populates the internal set of data source keys after properties are set. This method uses reflection to access
     * the underlying map of resolved data sources.
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        try {
            Field sourceMapField = AbstractRoutingDataSource.class.getDeclaredField("resolvedDataSources");
            sourceMapField.setAccessible(true);
            Map<Object, javax.sql.DataSource> sourceMap = (Map<Object, javax.sql.DataSource>) sourceMapField.get(this);
            keySet.addAll(sourceMap.keySet());
            sourceMapField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Sets the target data sources and updates the internal key set.
     *
     * @param map A map of data source keys to data source instances.
     */
    @Override
    public void setTargetDataSources(Map<Object, Object> map) {
        super.setTargetDataSources(map);
        keySet.addAll(map.keySet());
        this.afterPropertiesSet();
    }

    /**
     * Retrieves a map of all configured target data sources. This method uses reflection to access the private
     * {@code targetDataSources} field.
     *
     * @return A map of all data sources.
     */
    public Map<Object, Object> getAllDataSources() {
        try {
            Field targetDataSourcesField = AbstractRoutingDataSource.class.getDeclaredField("targetDataSources");
            targetDataSourcesField.setAccessible(true);
            return (Map<Object, Object>) targetDataSourcesField.get(this);
        } catch (Exception e) {
            Logger.error(false, "DataSource", "Failed to get all datasources", e);
            return new HashMap<>();
        }
    }

    /**
     * Dynamically adds a new data source. Note: This method attempts to modify the resolved data sources map via
     * reflection, which might be fragile and dependent on the Spring Framework's internal implementation.
     *
     * @param key        The unique key for the new data source.
     * @param dataSource The data source instance to add.
     */
    public synchronized void addDataSource(String key, javax.sql.DataSource dataSource) {
        Map<Object, Object> targetDataSources = getAllDataSources();
        targetDataSources.put(key, dataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
        keySet.add(key);
    }

    /**
     * Checks if a data source with the specified key exists.
     *
     * @param key The data source key to check.
     * @return {@code true} if the key exists, {@code false} otherwise.
     */
    public boolean containsKey(String key) {
        return keySet.contains(key);
    }

    /**
     * Dynamically removes a data source.
     *
     * @param key The key of the data source to remove.
     */
    public void remove(String key) {
        Map<Object, Object> targetDataSources = getAllDataSources();
        targetDataSources.remove(key);
        keySet.remove(key);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
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
