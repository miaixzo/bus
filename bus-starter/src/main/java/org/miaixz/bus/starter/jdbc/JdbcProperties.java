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

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable Bus dynamic-datasource properties bound only from {@code bus.jdbc}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.JDBC)
public final class JdbcProperties {

    /**
     * Whether the jdbc integration is enabled.
     */
    private final boolean enabled;
    /**
     * Routing key of the data source selected when no explicit key is active.
     */
    private final String primary;
    /**
     * Data source definitions keyed by their routing names.
     */
    private final Map<String, DataSourceSpec> datasources;

    /**
     * Creates and validates JDBC properties.
     *
     * @param enabled     whether Bus dynamic JDBC is enabled
     * @param primary     primary datasource name
     * @param datasources named datasource specifications
     */
    public JdbcProperties(@DefaultValue("false") boolean enabled, String primary,
            @DefaultValue Map<String, DataSourceSpec> datasources) {
        LinkedHashMap<String, DataSourceSpec> normalized = new LinkedHashMap<>();
        if (datasources != null) {
            datasources.forEach((name, spec) -> {
                String key = name == null ? Normal.EMPTY : name.trim();
                if (key.isEmpty() || spec == null || normalized.putIfAbsent(key, spec) != null) {
                    throw new IllegalArgumentException("bus.jdbc.datasources requires unique nonblank names and specs");
                }
            });
        }
        String normalizedPrimary = primary == null ? null : primary.trim();
        if (enabled && (normalized.isEmpty() || normalizedPrimary == null || normalizedPrimary.isEmpty()
                || !normalized.containsKey(normalizedPrimary))) {
            throw new IllegalArgumentException("Enabled bus.jdbc requires datasources and a matching primary name");
        }
        this.enabled = enabled;
        this.primary = normalizedPrimary;
        this.datasources = Map.copyOf(normalized);
    }

    /**
     * One immutable datasource specification.
     *
     * @param url             service endpoint URL
     * @param username        authentication username
     * @param password        authentication password
     * @param driverClassName driver class name
     * @param type            data source implementation or pool type
     * @param properties      bound feature configuration properties
     */
    public record DataSourceSpec(String url, String username, String password, String driverClassName, String type,
            Map<String, Object> properties) {

        /**
         * Validates a datasource specification and defensively copies vendor properties.
         */
        public DataSourceSpec {
            if (blank(url) || blank(driverClassName) || blank(type)) {
                throw new IllegalArgumentException("Datasource url, driver-class-name and type are required");
            }
            properties = properties == null ? Map.of() : Map.copyOf(properties);
        }

        /**
         * Returns whether a data source property is absent or blank.
         *
         * @param value data source property
         * @return {@code true} when the property is absent or blank
         */
        private static boolean blank(String value) {
            return value == null || value.isBlank();
        }

        /**
         * @return safe diagnostic text
         */
        @Override
        public String toString() {
            return "DataSourceSpec[url=" + url + ", username=***, password=***, driverClassName=" + driverClassName
                    + ", type=" + type + ", properties=" + properties.keySet() + "]";
        }
    }

}
