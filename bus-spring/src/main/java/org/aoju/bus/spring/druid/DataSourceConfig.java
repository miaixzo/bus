package org.aoju.bus.spring.druid;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.Mode;
import org.aoju.bus.logger.Logger;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableConfigurationProperties({DataSourceProperties.class})
public class DataSourceConfig {

    private static final ConfigurationPropertyNameAliases aliases;

    private Map<Object, Object> sourceMap = new HashMap<>();

    @Autowired
    DataSourceProperties dataSourceProperties;

    static {
        aliases = new ConfigurationPropertyNameAliases();
        aliases.addAliases("url", new String[]{"jdbc-url"});
        aliases.addAliases("username", new String[]{"user"});
    }

    /**
     * 将对象装换为map
     *
     * @param bean
     * @return
     */
    private <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                if ("driverClassName".equals(key)) {
                    key = "driver-class-name";
                }
                Object value = beanMap.get(key);
                if (StringUtils.isNotEmpty(this.dataSourceProperties.getPrivateKey())) {
                    if ("url".equals(key)) {
                        value = CryptoUtils.decrypt(Mode.AES, this.dataSourceProperties.getPrivateKey(), value.toString(), Charset.UTF_8);
                        beanMap.put("url", value);
                    } else if ("username".equals(key)) {
                        value = CryptoUtils.decrypt(Mode.AES, this.dataSourceProperties.getPrivateKey(), value.toString(), Charset.UTF_8);
                        beanMap.put("username", value);
                    } else if ("password".equals(key)) {
                        value = CryptoUtils.decrypt(Mode.AES, this.dataSourceProperties.getPrivateKey(), value.toString(), Charset.UTF_8);
                        beanMap.put("password", value);
                    }
                }
                map.put(key + "", value);
            }
        }
        return map;
    }

    @Bean
    @Primary
    public MultiDataSource dataSource() {
        Map defaultConfig = beanToMap(this.dataSourceProperties);
        DataSource defaultDatasource = bind(defaultConfig);
        sourceMap.put("dataSource", defaultDatasource);
        if (ObjectUtils.isNotEmpty(dataSourceProperties.getMulti())) {
            Logger.info("Enabled Multiple DataSource");
            List<DataSourceProperties> list = this.dataSourceProperties.getMulti();
            for (int i = 0; i < list.size(); i++) {
                Map config = beanToMap(list.get(i));
                if ((boolean) config.getOrDefault("extend", Boolean.TRUE)) {
                    Map properties = new HashMap(defaultConfig);
                    properties.putAll(config);
                }
                sourceMap.put(config.get("key").toString(), bind(config));
            }
        }
        MultiDataSource dataSource = new MultiDataSource();
        dataSource.setDefaultTargetDataSource(defaultDatasource);
        dataSource.setTargetDataSources(sourceMap);
        return dataSource;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    public DataSource bind(Map<String, Object> map) {
        String type = StringUtils.toString(map.get("type"));
        try {
            if (!StringUtils.isEmpty(type)) {
                Class<? extends DataSource> dataSourceType = (Class<? extends DataSource>) Class.forName(type);
                DataSourceBuilder factory = DataSourceBuilder.create()
                        .driverClassName((String) map.get("driverClassName"))
                        .url((String) map.get("url"))
                        .username(StringUtils.toString(map.get("username")))
                        .password(StringUtils.toString(map.get("password")))
                        .type(dataSourceType);
                return factory.build();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("can not resolve class with type: " + type);
        }
        return null;
    }

}