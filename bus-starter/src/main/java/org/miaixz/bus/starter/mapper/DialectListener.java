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
package org.miaixz.bus.starter.mapper;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.ibatis.session.Configuration;

import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.dialect.DialectRegistry;
import org.miaixz.bus.spring.jdbc.DataSourceHolder;
import org.miaixz.bus.spring.jdbc.DataSourceListener;

/**
 * Synchronizes Mapper dialect registrations with datasource routes owned by JDBC.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DialectListener implements DataSourceListener, AutoCloseable {

    /**
     * Application-context-scoped datasource routing state.
     */
    private final DataSourceHolder dataSourceHolder;

    /**
     * Dialects detected for routes owned by this application context.
     */
    private final ConcurrentMap<String, Dialect> dialects = new ConcurrentHashMap<>();

    /**
     * MyBatis configurations bound to this context-local route listener.
     */
    private final Set<Configuration> configurations = ConcurrentHashMap.newKeySet();

    /**
     * Creates a dialect listener for one datasource routing context.
     *
     * @param dataSourceHolder datasource routing state
     */
    public DialectListener(DataSourceHolder dataSourceHolder) {
        this.dataSourceHolder = Objects.requireNonNull(dataSourceHolder, "dataSourceHolder");
    }

    /**
     * Detects and registers the dialect for an available datasource route.
     *
     * @param key        routing key
     * @param dataSource registered datasource
     */
    @Override
    public void onAdded(String key, DataSource dataSource) {
        this.dialects.put(key, DialectRegistry.getDialect(dataSource));
    }

    /**
     * Removes dialect state belonging to a removed datasource route.
     *
     * @param key        routing key
     * @param dataSource removed datasource
     */
    @Override
    public void onRemoved(String key, DataSource dataSource) {
        this.dialects.remove(key);
        DialectRegistry.removeDialect(dataSource);
    }

    /**
     * Binds one MyBatis configuration to this application context's effective datasource route.
     *
     * @param configuration MyBatis configuration created by the current application context
     */
    public void bind(Configuration configuration) {
        Configuration value = Objects.requireNonNull(configuration, "configuration");
        this.configurations.add(value);
        DialectRegistry.setDialectProvider(value, this::currentDialect);
    }

    /**
     * Removes all MyBatis configuration bindings owned by this application context.
     */
    @Override
    public void close() {
        this.configurations.forEach(DialectRegistry::removeDialectProvider);
        this.configurations.clear();
        this.dialects.clear();
    }

    /**
     * Resolves the dialect for the effective route without publishing context state globally.
     *
     * @return current route dialect, or {@code null} when the route has not been registered
     */
    private Dialect currentDialect() {
        return this.dialects.get(this.dataSourceHolder.getKey());
    }

}
