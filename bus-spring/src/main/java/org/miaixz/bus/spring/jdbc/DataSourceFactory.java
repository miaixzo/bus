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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Creates concrete datasource instances from normalized definitions without reading the Spring environment.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DataSourceFactory {

    /**
     * Hikari-specific JDBC URL property removed before canonical URL binding.
     */
    private static final String JDBC_URL = "jdbc-url";

    /**
     * Aliases accepted while binding common datasource properties.
     */
    private static final ConfigurationPropertyNameAliases ALIASES;

    /**
     * Default datasource type supplied by the consuming integration.
     */
    private final String defaultType;

    static {
        ALIASES = new ConfigurationPropertyNameAliases();
        ALIASES.addAliases("url", JDBC_URL);
        ALIASES.addAliases("username", "user");
    }

    /**
     * Creates a datasource factory with an integration-defined default type.
     *
     * @param defaultType default datasource implementation class name
     */
    public DataSourceFactory(String defaultType) {
        this.defaultType = StringKit.trim(defaultType);
        if (StringKit.isEmpty(this.defaultType)) {
            throw new IllegalArgumentException("Default datasource type is required");
        }
    }

    /**
     * Creates and binds one concrete datasource.
     *
     * @param definition normalized datasource definition
     * @return configured datasource instance
     */
    public DataSource create(DataSourceDefinition definition) {
        String typeName = StringKit.isEmpty(definition.getType()) ? this.defaultType : definition.getType();
        try {
            Class<? extends DataSource> type = Class.forName(typeName).asSubclass(DataSource.class);
            Map<String, Object> properties = properties(definition);
            ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
            Binder binder = new Binder(source.withAliases(ALIASES));
            return binder.bind(ConfigurationPropertyName.EMPTY, Bindable.of(type)).get();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot create datasource '" + definition.getName() + "' with type: " + typeName, e);
        }
    }

    /**
     * Converts one definition to canonical bindable pool properties.
     *
     * @param definition normalized datasource definition
     * @return mutable binding properties
     */
    private static Map<String, Object> properties(DataSourceDefinition definition) {
        Map<String, Object> properties = new HashMap<>(definition.getHikari());
        properties.remove(JDBC_URL);
        put(properties, "url", definition.getUrl());
        put(properties, "username", definition.getUsername());
        put(properties, "password", definition.getPassword());
        put(properties, "driverClassName", definition.getDriverClassName());
        return properties;
    }

    /**
     * Adds a non-null property to a binding map.
     *
     * @param properties target binding properties
     * @param name       canonical property name
     * @param value      optional property value
     */
    private static void put(Map<String, Object> properties, String name, Object value) {
        if (value != null) {
            properties.put(name, value);
        }
    }

}
