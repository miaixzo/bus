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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures dynamic JDBC data sources and annotation-driven routing.
 * <p>
 * This class configures the primary data source and any additional data sources, setting up a {@link DynamicDataSource}
 * to handle routing. It also provides support for encrypted credentials and configures a transaction manager.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ConditionalOnClass(value = { HikariDataSource.class })
@EnableConfigurationProperties(value = { JdbcProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = GeniusBuilder.JDBC, name = "enabled", havingValue = "true", matchIfMissing = false)
@AutoConfigureBefore(value = { DataSourceAutoConfiguration.class })
public class JdbcConfiguration {

    /**
     * Bound jdbc configuration properties.
     */
    private final JdbcProperties properties;

    /**
     * Stores the data-source definitions used to create dynamic JDBC routing infrastructure.
     *
     * @param properties bound configuration properties
     */
    public JdbcConfiguration(JdbcProperties properties) {
        this.properties = properties;
    }

    /**
     * Aliases for mapping common data source properties.
     */
    private static final ConfigurationPropertyNameAliases aliases;

    static {
        aliases = new ConfigurationPropertyNameAliases();
        aliases.addAliases("url", "jdbc-url");
        aliases.addAliases("username", "user");
    }

    /**
     * Creates dynamic data source routing advice only after the routing data source is present.
     *
     * @return dynamic data source aspect
     */
    @Bean
    @ConditionalOnBean(DynamicDataSource.class)
    @ConditionalOnMissingBean(AspectjJdbcProxy.class)
    public AspectjJdbcProxy aspectjJdbcProxy() {
        return new AspectjJdbcProxy();
    }

    /**
     * Creates and configures the dynamic data source bean.
     * <p>
     * This method initializes the default data source and any additional data sources defined in the configuration. It
     * then sets up the {@link DynamicDataSource} to manage them.
     * </p>
     *
     * @return The configured {@link DynamicDataSource} instance.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    public DynamicDataSource dataSource() {
        DataSourceHolder.remove();
        try {
            Map<Object, Object> sourceMap = new LinkedHashMap<>();
            this.properties.getDatasources().forEach((name, spec) -> sourceMap.put(name, bind(toMap(spec))));

            DynamicDataSource dataSource = new DynamicDataSource();
            dataSource.setPrimary(this.properties.getPrimary());
            dataSource.setTargetDataSources(sourceMap);
            return dataSource;
        } finally {
            // Bean initialization must never leave a routing key on the bootstrap thread.
            DataSourceHolder.remove();
        }
    }

    /**
     * Creates the transaction manager bean.
     *
     * @param dataSource The {@link DataSource} to be used by the transaction manager.
     * @return A {@link DataSourceTransactionManager} instance.
     */
    @Bean
    @ConditionalOnMissingBean(type = "org.springframework.transaction.PlatformTransactionManager")
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * Binds a map of properties to a new {@link DataSource} instance.
     *
     * @param map A map containing the data source properties.
     * @return A configured {@link DataSource} instance.
     * @throws InternalException        if the data source type is not specified.
     * @throws IllegalArgumentException if the specified data source class cannot be found.
     */
    private DataSource bind(Map<String, Object> map) {
        String type = StringKit.toString(map.get("type"));
        if (StringKit.isEmpty(type)) {
            throw new InternalException("The database type is empty");
        }
        try {
            return bind((Class<? extends DataSource>) Class.forName(type), map);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot resolve class with type: " + type, e);
        }
    }

    /**
     * Converts a bean to a map of its properties.
     * <p>
     * This method also handles the decryption of sensitive properties like url, username, and password if a private key
     * is configured.
     * </p>
     *
     * @param spec immutable data source specification to convert
     * @return A map representation of the bean's properties.
     */
    private Map<String, Object> toMap(JdbcProperties.DataSourceSpec spec) {
        Map<String, Object> map = new HashMap<>(spec.properties());
        map.put("url", spec.url());
        map.put("username", spec.username());
        map.put("password", spec.password());
        map.put("driverClassName", spec.driverClassName());
        map.put("type", spec.type());
        return map;
    }

    /**
     * Binds properties to an existing {@link DataSource} instance. This method is inspired by Spring Boot's
     * {@code DataSourceBuilder.bind} to ensure consistent data source configuration.
     *
     * @param result     The {@link DataSource} instance to configure.
     * @param properties A map of properties to bind.
     */
    private void bind(DataSource result, Map<String, Object> properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source.withAliases(aliases));
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(result));
    }

    /**
     * Creates and binds a new {@link DataSource} instance of the specified class. This method is inspired by Spring
     * Boot's {@code DataSourceBuilder.bind} to ensure consistent data source creation.
     *
     * @param clazz      The class of the {@link DataSource} to create.
     * @param properties A map of properties to bind.
     * @param <T>        The type of the data source.
     * @return A new, configured {@link DataSource} instance.
     */
    private <T extends DataSource> T bind(Class<T> clazz, Map<String, Object> properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source.withAliases(aliases));
        return binder.bind(ConfigurationPropertyName.EMPTY, Bindable.of(clazz)).get();
    }

}
