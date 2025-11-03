/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.miaixz.bus.core.center.map.BeanMap;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.Resource;

/**
 * Auto-configuration for data sources.
 * <p>
 * This class configures the primary data source and any additional data sources, setting up a {@link DynamicDataSource}
 * to handle routing. It also provides support for encrypted credentials and configures a transaction manager.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ConditionalOnClass(value = { HikariDataSource.class })
@EnableConfigurationProperties(value = { JdbcProperties.class })
@AutoConfigureBefore(value = { DataSourceAutoConfiguration.class })
@Import(AspectjJdbcProxy.class)
public class JdbcConfiguration {

    /**
     * Injected JDBC configuration properties.
     */
    @Resource
    JdbcProperties properties;

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
    public DynamicDataSource dataSource() {
        Map<String, Object> defaultConfig = beanToMap(this.properties);
        DataSource defaultDatasource = bind(defaultConfig);
        Map<Object, Object> sourceMap = new HashMap<>();
        sourceMap.put(this.properties.getName(), defaultDatasource);
        DataSourceHolder.setKey(this.properties.getName());

        if (ObjectKit.isNotEmpty(this.properties.getMulti())) {
            Logger.info("Enable support for multiple data sources");
            List<JdbcProperties> list = this.properties.getMulti();
            for (JdbcProperties prop : list) {
                Map<String, Object> config = beanToMap(prop);
                if ((boolean) config.getOrDefault("extend", Boolean.TRUE)) {
                    Map<String, Object> mergedConfig = new HashMap<>(defaultConfig);
                    mergedConfig.putAll(config);
                    config = mergedConfig;
                }
                sourceMap.put(config.get("name").toString(), bind(config));
            }
        }

        DynamicDataSource dataSource = DynamicDataSource.getInstance();
        dataSource.setDefaultTargetDataSource(defaultDatasource);
        dataSource.setTargetDataSources(sourceMap);
        dataSource.afterPropertiesSet();

        // Set the default data source name in the context holder
        DataSourceHolder.setDefault(this.properties.getName());

        return dataSource;
    }

    /**
     * Creates the transaction manager bean.
     *
     * @param dataSource The {@link DataSource} to be used by the transaction manager.
     * @return A {@link DataSourceTransactionManager} instance.
     */
    @Bean
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
     * @param bean The bean to convert.
     * @param <T>  The type of the bean.
     * @return A map representation of the bean's properties.
     */
    private <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = new HashMap<>();
        if (null != bean) {
            BeanMap beanMap = BeanMap.of(bean);
            for (String key : beanMap.keySet()) {
                Object value = beanMap.get(key);
                if (StringKit.isNotEmpty(this.properties.getPrivateKey())) {
                    Logger.info("The database connection is securely enabled");
                    if ("url".equals(key) || "username".equals(key) || "password".equals(key)) {
                        value = Builder.decrypt(
                                Algorithm.AES.getValue(),
                                this.properties.getPrivateKey(),
                                value.toString(),
                                Charset.UTF_8);
                        beanMap.put(key, value);
                    }
                }
                map.put(StringKit.toString(key), value);
            }
        }
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
