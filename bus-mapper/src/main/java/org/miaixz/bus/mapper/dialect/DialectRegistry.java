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
package org.miaixz.bus.mapper.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.apache.ibatis.session.Configuration;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;

/**
 * Registry for database dialects, providing automatic detection and caching.
 *
 * <p>
 * This registry manages all available database dialects and provides methods to detect the appropriate dialect based on
 * DataSource, Connection, or JDBC URL.
 * </p>
 *
 * <p>
 * For applications with multiple data sources, an integration can attach a dialect provider to a specific MyBatis
 * {@link Configuration}. Providers are isolated by configuration and therefore cannot leak routing state between
 * application contexts.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 * // Automatic detection from DataSource
 * Dialect dialect = DialectRegistry.getDialect(dataSource);
 *
 * // Automatic detection from Connection
 * Dialect dialect = DialectRegistry.getDialect(connection);
 *
 * // Detection from JDBC URL
 * Dialect dialect = DialectRegistry.getDialectByUrl("jdbc:mysql://localhost:3306/test");
 *
 * // Obtain the dialect associated with one MyBatis configuration
 * Dialect dialect = DialectRegistry.getDialect(configuration);
 *
 * // Register custom dialect
 * DialectRegistry.registerDialect(new MyCustomDialect());
 *
 * }</pre>
 *
 * @author Kimi Liu
 */
public final class DialectRegistry {

    /**
     * Registered dialects (in registration order for priority)
     */
    private static final CopyOnWriteArrayList<Dialect> DIALECTS = new CopyOnWriteArrayList<>();

    /**
     * DataSource to Dialect cache
     */
    private static final ConcurrentMap<DataSource, Dialect> DATASOURCE_CACHE = new ConcurrentHashMap<>();

    /**
     * JDBC URL to Dialect cache
     */
    private static final ConcurrentMap<String, Dialect> URL_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache of dialects indexed by data source key.
     * <p>
     * This cache is used when a data-access integration supplies its selected key. The dialect is resolved from that
     * key without copying data source state into Mapper runtime context.
     * </p>
     */
    private static final Map<Configuration, Supplier<Dialect>> CONFIGURATION_PROVIDERS = Collections
            .synchronizedMap(new WeakHashMap<>());

    /**
     * Default/unknown dialect (singleton)
     */
    private static final Dialect UNKNOWN = new DefaultDialect();

    /**
     * Registers all built-in dialects during class initialization.
     */
    static {
        // Register all built-in dialects
        // Mainstream databases
        registerDialect(new MySql());
        registerDialect(new PostgreSql());
        // Register product-family dialects after their generic families because
        // registerDialect inserts at the head of the list.
        registerDialect(new Polardb());
        registerDialect(new Dameng());
        registerDialect(new Oracle());
        registerDialect(new SqlServer());
        registerDialect(new SQLite());
        registerDialect(new H2());
        registerDialect(new Db2());
        registerDialect(new Hsqldb());

        // IBM databases
        registerDialect(new AS400());
        registerDialect(new Informix());

        // Legacy/specific versions are not auto-registered because URL-only detection
        // cannot distinguish them reliably from their primary dialects.

        // Chinese domestic databases
        registerDialect(new Oscar());
        registerDialect(new Xugudb());

        // Other databases
        registerDialect(new Firebird());
        registerDialect(new Herddb());
        registerDialect(new CirroData());
    }

    /**
     * Prevents instantiation of this global database dialect lookup registry.
     */
    private DialectRegistry() {
        // No initialization required.
    }

    /**
     * Registers a custom dialect.
     *
     * <p>
     * Custom dialects are checked before built-in dialects.
     * </p>
     *
     * @param dialect the dialect to register
     */
    public static void registerDialect(Dialect dialect) {
        Assert.notNull(dialect, "Dialect cannot be null");
        // Add at the beginning for priority
        DIALECTS.add(0, dialect);
    }

    /**
     * Gets the dialect for the specified DataSource.
     *
     * <p>
     * Results are cached for performance.
     * </p>
     *
     * @param dataSource the data source
     * @return the detected dialect, or UnknownDialect if detection fails
     */
    public static Dialect getDialect(DataSource dataSource) {
        if (dataSource == null) {
            return UNKNOWN;
        }

        // Check cache first
        Dialect cached = DATASOURCE_CACHE.get(dataSource);
        if (cached != null) {
            return cached;
        }

        // Detect from connection
        try (Connection conn = dataSource.getConnection()) {
            Dialect dialect = getDialect(conn);
            DATASOURCE_CACHE.put(dataSource, dialect);
            return dialect;
        } catch (SQLException e) {
            Logger.warn(
                    false,
                    "Mapper",
                    e,
                    "Mapper operation failed: provider={}, exception={}",
                    "DialectRegistry",
                    e.getClass().getSimpleName());
            return UNKNOWN;
        }
    }

    /**
     * Gets the dialect for the specified Connection.
     *
     * @param connection the database connection
     * @return the detected dialect, or UnknownDialect if detection fails
     */
    public static Dialect getDialect(Connection connection) {
        if (connection == null) {
            return UNKNOWN;
        }

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String jdbcUrl = metaData.getURL();
            return getDialectByUrl(jdbcUrl);
        } catch (SQLException e) {
            Logger.warn(
                    false,
                    "Mapper",
                    e,
                    "Mapper operation failed: provider={}, exception={}",
                    "DialectRegistry",
                    e.getClass().getSimpleName());
            return UNKNOWN;
        }
    }

    /**
     * Gets the dialect for the specified JDBC URL.
     *
     * <p>
     * Results are cached for performance.
     * </p>
     *
     * @param jdbcUrl the JDBC URL
     * @return the detected dialect, or UnknownDialect if no match found
     */
    public static Dialect getDialectByUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return UNKNOWN;
        }

        // Check cache first
        Dialect cached = URL_CACHE.get(jdbcUrl);
        if (cached != null) {
            return cached;
        }

        // Resolve by URL
        for (Dialect dialect : DIALECTS) {
            Dialect resolved = dialect.resolve(jdbcUrl);
            if (resolved != null) {
                URL_CACHE.put(jdbcUrl, resolved);
                return resolved;
            }
        }

        return UNKNOWN;
    }

    /**
     * Gets the dialect for the specified dialect name.
     *
     * <p>
     * Supported dialect names include registered database names such as mysql, postgresql, polardb, oracle, sqlserver,
     * sqlite, h2, db2, and informix.
     * </p>
     *
     * @param dialectName the dialect name (case-insensitive)
     * @return the detected dialect, or UnknownDialect if no match found
     */
    public static Dialect getDialectByName(String dialectName) {
        if (dialectName == null || dialectName.isEmpty()) {
            return UNKNOWN;
        }

        String lowerName = dialectName.toLowerCase();
        for (Dialect dialect : DIALECTS) {
            if (dialect.getDatabase().toLowerCase().equals(lowerName)
                    || dialect.getDatabase().toLowerCase().replace(Symbol.SPACE, Normal.EMPTY).equals(lowerName)) {
                return dialect;
            }
        }

        return UNKNOWN;
    }

    /**
     * Returns the dialect supplied for one MyBatis configuration.
     *
     * @param configuration MyBatis configuration that owns the SQL source
     * @return the registered dialect, or {@link #UNKNOWN} when no provider or dialect is available
     */
    public static Dialect getDialect(Configuration configuration) {
        Supplier<Dialect> provider = configuration == null ? null : CONFIGURATION_PROVIDERS.get(configuration);
        Dialect dialect = provider == null ? null : provider.get();
        return dialect == null ? UNKNOWN : dialect;
    }

    /**
     * Associates a read-only dialect provider with one MyBatis configuration.
     *
     * @param configuration MyBatis configuration that owns the provider
     * @param provider      context-local dialect provider
     */
    public static void setDialectProvider(Configuration configuration, Supplier<Dialect> provider) {
        Assert.notNull(configuration, "MyBatis configuration cannot be null");
        Assert.notNull(provider, "Dialect provider cannot be null");
        CONFIGURATION_PROVIDERS.put(configuration, provider);
    }

    /**
     * Removes the dialect provider owned by one MyBatis configuration.
     *
     * @param configuration MyBatis configuration being released
     */
    public static void removeDialectProvider(Configuration configuration) {
        if (configuration != null) {
            CONFIGURATION_PROVIDERS.remove(configuration);
        }
    }

    /**
     * Removes cached dialect state for a datasource that is no longer available.
     *
     * @param dataSource removed datasource
     */
    public static void removeDialect(DataSource dataSource) {
        if (dataSource != null) {
            DATASOURCE_CACHE.remove(dataSource);
        }
    }

    /**
     * Clears all caches.
     *
     * <p>
     * This method is primarily for testing purposes.
     * </p>
     */
    public static void clearCache() {
        DATASOURCE_CACHE.clear();
        URL_CACHE.clear();
        CONFIGURATION_PROVIDERS.clear();
    }

    /**
     * Gets all registered dialects.
     *
     * @return a list of all registered dialects
     */
    public static List<Dialect> getDialects() {
        return List.copyOf(DIALECTS);
    }

    /**
     * Gets all registered dialects.
     *
     * @return an immutable snapshot of all registered dialects
     */
    public static List<Dialect> getAllDialects() {
        return getDialects();
    }

}
