/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for Druid DataSource.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.DATASOURCE)
public class JdbcProperties {

    /**
     * The name of the data source.
     */
    private String name;

    /**
     * The connection URL for the data source.
     */
    private String url;

    /**
     * The username for the database.
     */
    private String username;

    /**
     * The password for the database.
     */
    private String password;

    /**
     * The type of the connection pool (e.g., com.alibaba.druid.pool.DruidDataSource).
     */
    private String type;

    /**
     * The fully qualified name of the JDBC driver.
     */
    private String driverClassName;

    /**
     * Filters for monitoring and statistics (e.g., stat, wall, log4j).
     */
    private String filters;

    /**
     * The minimum number of idle connections in the pool.
     */
    private String minIdle;

    /**
     * The minimum time a connection can be idle before it is eligible for eviction (default: 30 minutes).
     */
    private String minEvictableIdleTimeMillis;

    /**
     * The maximum time a connection can be idle before it is eligible for eviction (default: 7 hours).
     */
    private String maxEvictableIdleTimeMillis;

    /**
     * The SQL query used to validate connections.
     */
    private String validationQuery;

    /**
     * Whether to cache PreparedStatements.
     */
    private boolean poolPreparedStatements;

    /**
     * The maximum number of open PreparedStatements to cache.
     */
    private String maxOpenPreparedStatements;

    /**
     * The interval in milliseconds for the evictor thread to run and check for idle connections.
     */
    private String timeBetweenEvictionRunsMillis;

    /**
     * The maximum number of active connections in the pool.
     */
    private int maxActive;

    /**
     * The initial number of connections to create when the pool is started.
     */
    private int initialSize;

    /**
     * The maximum time in milliseconds to wait for a connection when the pool is full.
     */
    private int maxWait;

    /**
     * Recommended to be true for security and reliability. It checks if a connection is valid before borrowing it if it
     * has been idle for longer than timeBetweenEvictionRunsMillis.
     */
    private boolean testWhileIdle;

    /**
     * Whether to validate the connection with validationQuery when borrowing it from the pool. Can impact performance.
     */
    private boolean testOnBorrow;

    /**
     * Whether to validate the connection with validationQuery when returning it to the pool. Can impact performance.
     */
    private boolean testOnReturn;

    /**
     * The private key for decrypting data source information.
     */
    private String privateKey;

    /**
     * A unique key to identify this data source in a multi-data-source configuration.
     */
    private String key;

    /**
     * A list of properties for configuring multiple data sources.
     */
    private List<JdbcProperties> multi;

}
