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

import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Defines one immutable named datasource after compatible property binding.
 *
 * @param name            unique routing name
 * @param url             JDBC connection URL
 * @param username        authentication username
 * @param password        authentication password
 * @param driverClassName JDBC driver class name
 * @param type            optional datasource implementation type
 * @param hikari          Hikari-compatible pool properties
 * @author Kimi Liu
 * @since Java 21+
 */
public record DataSourceDefinition(String name, String url, String username, String password, String driverClassName,
        String type, Map<String, Object> hikari) {

    /**
     * Hikari-specific JDBC URL property name.
     */
    private static final String JDBC_URL = "jdbc-url";

    /**
     * Normalizes required values and defensively copies pool properties.
     */
    public DataSourceDefinition {
        name = StringKit.trim(name);
        if (StringKit.isEmpty(name)) {
            throw new IllegalArgumentException("Datasource name is required");
        }
        hikari = hikari == null ? Map.of() : Map.copyOf(hikari);
        url = StringKit.trim(url);
        if (StringKit.isEmpty(url)) {
            url = StringKit.trim(StringKit.toString(hikari.get(JDBC_URL)));
        }
        if (StringKit.isEmpty(url)) {
            throw new IllegalArgumentException("Datasource url is required for: " + name);
        }
        type = StringKit.trim(type);
    }

    /**
     * Returns diagnostic text without exposing endpoint or authentication credentials.
     *
     * @return redacted datasource definition summary
     */
    @Override
    public String toString() {
        return "DataSourceDefinition[name=" + name + ", url=***, username=***, password=***, driverClassName="
                + driverClassName + ", type=" + type + ", hikari=" + hikari.keySet() + "]";
    }

}
