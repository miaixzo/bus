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
import org.springframework.cglib.beans.BeanMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.Resource;

/**
 * 数据源配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ConditionalOnClass(value = { HikariDataSource.class })
@EnableConfigurationProperties(value = { JdbcProperties.class })
@AutoConfigureBefore(value = { DataSourceAutoConfiguration.class })
@Import(AspectjJdbcProxy.class)
public class JdbcConfiguration {

    private static final ConfigurationPropertyNameAliases aliases;

    static {
        aliases = new ConfigurationPropertyNameAliases();
        aliases.addAliases("url", "jdbc-url");
        aliases.addAliases("username", "user");
    }

    private final Map<Object, Object> sourceMap = new HashMap<>();
    @Resource
    JdbcProperties properties;

    /**
     * 初始化数据源/多数据源
     *
     * @return 数据源
     */
    @Bean
    @Primary
    public DynamicDataSource dataSource() {
        Map defaultConfig = beanToMap(this.properties);
        DataSource defaultDatasource = bind(defaultConfig);
        sourceMap.put("dataSource", defaultDatasource);
        if (ObjectKit.isNotEmpty(this.properties.getMulti())) {
            Logger.info("Enabled Multiple DataSource");
            List<JdbcProperties> list = this.properties.getMulti();
            for (int i = 0; i < list.size(); i++) {
                Map config = beanToMap(list.get(i));
                if ((boolean) config.getOrDefault("extend", Boolean.TRUE)) {
                    Map properties = new HashMap(defaultConfig);
                    properties.putAll(config);
                }
                sourceMap.put(config.get("key").toString(), bind(config));
            }
        }
        DynamicDataSource dataSource = new DynamicDataSource();
        dataSource.setDefaultTargetDataSource(defaultDatasource);
        dataSource.setTargetDataSources(sourceMap);
        return dataSource;
    }

    /**
     * 事务支持
     *
     * @param dataSource 数据源
     * @return 事务信息
     */
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 绑定数据源信息
     *
     * @param map 数据库信息
     * @return 数据库连接
     */
    private DataSource bind(Map<String, Object> map) {
        String type = StringKit.toString(map.get("type"));
        if (StringKit.isEmpty(type)) {
            throw new InternalException("The database type is empty");
        }
        try {
            return bind((Class<? extends DataSource>) Class.forName(type), map);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot resolve class with type: " + type);
        }
    }

    /**
     * 将对象装换为map
     *
     * @param bean 对象
     * @return the object
     */
    private <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = new HashMap<>();
        if (null != bean) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                Object value = beanMap.get(key);
                if (StringKit.isNotEmpty(this.properties.getPrivateKey())) {
                    Logger.info("The database connection is securely enabled");
                    if ("url".equals(key)) {
                        value = Builder.decrypt(Algorithm.AES.getValue(), this.properties.getPrivateKey(),
                                value.toString(), Charset.UTF_8);
                        beanMap.put("url", value);
                    } else if ("username".equals(key)) {
                        value = Builder.decrypt(Algorithm.AES.getValue(), this.properties.getPrivateKey(),
                                value.toString(), Charset.UTF_8);
                        beanMap.put("username", value);
                    } else if ("password".equals(key)) {
                        value = Builder.decrypt(Algorithm.AES.getValue(), this.properties.getPrivateKey(),
                                value.toString(), Charset.UTF_8);
                        beanMap.put("password", value);
                    }
                }
                map.put(StringKit.toString(key), value);
            }
        }
        return map;
    }

    /**
     * 绑定参数:以下三个方法都是参考DataSourceBuilder的bind方法实现的， 目的是尽量保证我们自己添加的数据源构造过程与springboot保持一致
     *
     * @param result     数据源
     * @param properties 配置信息
     */
    private void bind(DataSource result, Map properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source.withAliases(aliases));
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(result));
    }

    /**
     * 绑定参数:以下三个方法都是参考DataSourceBuilder的bind方法实现的， 目的是尽量保证我们自己添加的数据源构造过程与springboot保持一致
     *
     * @param clazz      连接池信息
     * @param properties 配置信息
     */
    private <T extends DataSource> T bind(Class<T> clazz, Map properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source.withAliases(aliases));
        return binder.bind(ConfigurationPropertyName.EMPTY, Bindable.of(clazz)).get();
    }

}
