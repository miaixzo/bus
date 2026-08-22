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

import java.util.List;

import com.zaxxer.hikari.HikariDataSource;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Describes the Starter parameters supplied to reusable Spring JDBC infrastructure.
 *
 * @author Kimi Liu
 */
public class JdbcDescriptor {

    /**
     * Compatible Spring Boot datasource prefix.
     */
    private static final String SPRING_DATASOURCE = "spring.datasource";

    /**
     * Datasource prefixes in descending priority order.
     */
    private final List<String> prefixes;

    /**
     * Default datasource implementation class name.
     */
    private final String defaultType;

    /**
     * Creates a validated JDBC descriptor.
     *
     * @param prefixes    datasource prefixes in descending priority order
     * @param defaultType default datasource implementation class name
     */
    public JdbcDescriptor(List<String> prefixes, String defaultType) {
        if (prefixes == null || prefixes.isEmpty() || defaultType == null || defaultType.isBlank()) {
            throw new IllegalArgumentException("JDBC prefixes and default type are required");
        }
        this.prefixes = List.copyOf(prefixes);
        this.defaultType = defaultType.trim();
    }

    /**
     * Creates the authoritative Bus-before-Spring JDBC descriptor.
     *
     * @return default JDBC descriptor
     */
    static JdbcDescriptor defaults() {
        return new JdbcDescriptor(List.of(GeniusBuilder.DATASOURCE, SPRING_DATASOURCE),
                HikariDataSource.class.getName());
    }

    /**
     * Returns datasource prefixes in descending priority order.
     *
     * @return immutable datasource prefix list
     */
    List<String> getPrefixes() {
        return this.prefixes;
    }

    /**
     * Returns the default datasource implementation class name.
     *
     * @return default datasource implementation class name
     */
    String getDefaultType() {
        return this.defaultType;
    }

}
