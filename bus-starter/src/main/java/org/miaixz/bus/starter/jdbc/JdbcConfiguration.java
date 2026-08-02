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

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import org.miaixz.bus.mapper.dialect.DialectRegistry;
import org.miaixz.bus.spring.jdbc.AspectjJdbcProxy;
import org.miaixz.bus.spring.jdbc.DataSourceFactory;
import org.miaixz.bus.spring.jdbc.DataSourceHolder;
import org.miaixz.bus.spring.jdbc.DataSourceMapping;
import org.miaixz.bus.spring.jdbc.DataSourceResolver;
import org.miaixz.bus.spring.jdbc.DynamicDataSource;

/**
 * Assembles dynamic JDBC routing, annotation advice, and transaction management Beans.
 * <p>
 * Configuration parsing belongs to {@link DataSourceResolver}; concrete pool creation belongs to
 * {@link DataSourceFactory}. This class only connects those collaborators to Spring Bean lifecycle infrastructure.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ConditionalOnClass(value = { HikariDataSource.class })
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
     * @return dynamic datasource aspect
     */
    @Bean
    @ConditionalOnBean(DynamicDataSource.class)
    @ConditionalOnMissingBean(AspectjJdbcProxy.class)
    public AspectjJdbcProxy aspectjJdbcProxy() {
        return new AspectjJdbcProxy();
    }

    /**
     * Creates the primary routing datasource from one fully resolved configuration mapping.
     * <p>
     * JDBC owns the application primary key and thread-local route. Mapper receives only a read-only key supplier for
     * dialect resolution and never modifies routing state.
     *
     * @return configured dynamic datasource
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    public DynamicDataSource dataSource() {
        DataSourceHolder.remove();
        try {
            DataSourceMapping mapping = this.dataSourceResolver.resolve();
            DataSourceHolder.setDefaultKey(mapping.primary());
            DialectRegistry.setKeyProvider(DataSourceHolder::getKey);

            Map<Object, Object> sources = new LinkedHashMap<>();
            mapping.sources().forEach((name, definition) -> {
                DataSource source = this.dataSourceFactory.create(definition);
                sources.put(name, source);
                DialectRegistry.initializeDialect(name, source);
            });

            DynamicDataSource dataSource = new DynamicDataSource();
            dataSource.setPrimary(mapping.primary());
            dataSource.setTargetDataSources(sources);
            return dataSource;
        } finally {
            // Bean initialization must never leave a routing key on the bootstrap thread.
            DataSourceHolder.remove();
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

}
