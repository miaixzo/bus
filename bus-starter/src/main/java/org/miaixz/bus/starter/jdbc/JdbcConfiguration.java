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
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.spring.jdbc.AspectjJdbcProxy;
import org.miaixz.bus.spring.jdbc.DataSourceFactory;
import org.miaixz.bus.spring.jdbc.DataSourceHolder;
import org.miaixz.bus.spring.jdbc.DataSourceListener;
import org.miaixz.bus.spring.jdbc.DataSourceMapping;
import org.miaixz.bus.spring.jdbc.DataSourceResolver;
import org.miaixz.bus.spring.jdbc.DynamicDataSource;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableJdbc;

/**
 * Assembles dynamic JDBC routing, annotation advice, and transaction management Beans.
 * <p>
 * Configuration parsing belongs to {@link DataSourceResolver}; concrete pool creation belongs to
 * {@link DataSourceFactory}. This class only connects those collaborators to Spring Bean lifecycle infrastructure.
 *
 * @author Kimi Liu
 */
@ConditionalOnClass(value = { HikariDataSource.class })
@ConditionalOnEnabled(annotation = EnableJdbc.class, prefix = GeniusBuilder.DATASOURCE, matchIfMissing = true)
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(value = { DataSourceAutoConfiguration.class })
public class JdbcConfiguration {

    /**
     * Resolves the selected datasource configuration mapping.
     */
    private final DataSourceResolver dataSourceResolver;

    /**
     * Creates concrete datasource instances.
     */
    private final DataSourceFactory dataSourceFactory;

    /**
     * Creates JDBC Bean assembly collaborators for the current environment.
     *
     * @param environment Spring environment containing compatible datasource properties
     */
    public JdbcConfiguration(Environment environment) {
        JdbcDescriptor descriptor = JdbcDescriptor.defaults();
        this.dataSourceResolver = new DataSourceResolver(environment, descriptor.getPrefixes());
        this.dataSourceFactory = new DataSourceFactory(descriptor.getDefaultType());
    }

    /**
     * Creates dynamic datasource routing advice after the routing datasource is present.
     *
     * @param dataSourceHolder application-context-scoped routing state
     * @return dynamic datasource aspect
     */
    @Bean
    @ConditionalOnMissingBean(AspectjJdbcProxy.class)
    public AspectjJdbcProxy aspectjJdbcProxy(DataSourceHolder dataSourceHolder) {
        return new AspectjJdbcProxy(dataSourceHolder);
    }

    /**
     * Creates the application-context-scoped datasource routing state.
     *
     * @return datasource routing state
     */
    @Bean
    @ConditionalOnMissingBean(DataSourceHolder.class)
    public DataSourceHolder dataSourceHolder() {
        return new DataSourceHolder();
    }

    /**
     * Creates the primary routing datasource from one fully resolved configuration mapping.
     * <p>
     * JDBC owns the application primary key and thread-local route. Optional integrations observe successful route
     * changes through {@link DataSourceListener} without becoming dependencies of JDBC assembly.
     *
     * @param dataSourceHolder datasource routing state
     * @param listeners        ordered observers of datasource route changes
     * @return configured dynamic datasource
     */
    @Bean(destroyMethod = "close")
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    public DynamicDataSource dataSource(
            DataSourceHolder dataSourceHolder,
            ObjectProvider<DataSourceListener> listeners) {
        dataSourceHolder.remove();
        try {
            DataSourceMapping mapping = this.dataSourceResolver.resolve();
            dataSourceHolder.setDefaultKey(mapping.getPrimary());

            Map<Object, Object> sources = new LinkedHashMap<>();
            try {
                mapping.getSources().forEach((name, definition) -> {
                    DataSource source = this.dataSourceFactory.create(definition);
                    sources.put(name, source);
                });
            } catch (RuntimeException | Error exception) {
                try {
                    close(sources);
                } catch (RuntimeException closeException) {
                    exception.addSuppressed(closeException);
                }
                throw exception;
            }

            DynamicDataSource dataSource = new DynamicDataSource(dataSourceHolder, listeners.orderedStream().toList());
            dataSource.setPrimary(mapping.getPrimary());
            dataSource.setTargetDataSources(sources);
            return dataSource;
        } finally {
            // Bean initialization must never leave a routing key on the bootstrap thread.
            dataSourceHolder.remove();
        }
    }

    /**
     * Creates the transaction manager for the effective routing datasource.
     *
     * @param dataSource effective application datasource
     * @return datasource transaction manager
     */
    @Bean
    @ConditionalOnMissingBean(type = "org.springframework.transaction.PlatformTransactionManager")
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * Releases datasource instances created before JDBC assembly failed.
     *
     * @param sources partially created datasource mapping
     */
    private static void close(Map<Object, Object> sources) {
        java.util.Set<Object> released = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        RuntimeException failure = null;
        for (Object source : sources.values()) {
            if (!released.add(source) || !(source instanceof AutoCloseable closeable)) {
                continue;
            }
            try {
                closeable.close();
            } catch (RuntimeException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            } catch (Exception exception) {
                RuntimeException wrapped = new IllegalStateException(
                        "Failed to close datasource after JDBC assembly failure", exception);
                if (failure == null) {
                    failure = wrapped;
                } else {
                    failure.addSuppressed(wrapped);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

}
