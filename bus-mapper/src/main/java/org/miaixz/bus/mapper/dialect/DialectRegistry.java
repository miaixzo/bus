/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.mapper.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.Holder;
import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * Registry for database dialects, providing automatic detection and caching.
 *
 * <p>
 * This registry manages all available database dialects and provides methods to detect the appropriate dialect based on
 * DataSource, Connection, or JDBC URL.
 * </p>
 *
 * <p>
 * For multi-datasource scenarios, this registry also caches dialects per datasource key to support dynamic datasource
 * switching.
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
 * // Multi-datasource: get dialect for current thread's datasource
 * Dialect dialect = DialectRegistry.getDialect();
 *
 * // Register custom dialect
 * DialectRegistry.registerDialect(new MyCustomDialect());
 *
 * // Initialize dialect for a specific datasource key
 * DialectRegistry.initializeDialect("mysql_ds", mysqlDataSource);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class DialectRegistry {

    /**
     * Registered dialects (in registration order for priority)
     */
    private static final List<Dialect> DIALECTS = new ArrayList<>();

    /**
     * DataSource to Dialect cache
     */
    private static final ConcurrentMap<DataSource, Dialect> DATASOURCE_CACHE = new ConcurrentHashMap<>();

    /**
     * JDBC URL to Dialect cache
     */
    private static final ConcurrentMap<String, Dialect> URL_CACHE = new ConcurrentHashMap<>();

    /**
     * Datasource key to Dialect cache for multi-datasource scenarios.
     * <p>
     * This cache is used when the application uses dynamic datasource switching via {@link Holder#setKey(String)}. The
     * dialect is resolved based on the current datasource key.
     * </p>
     */
    private static final ConcurrentMap<String, Dialect> DS_KEY_CACHE = new ConcurrentHashMap<>();

    /**
     * Default/unknown dialect (singleton)
     */
    private static final Dialect UNKNOWN = new DefaultDialect();

    static {
        // Register all built-in dialects
        // Mainstream databases
        registerDialect(new MySql());
        registerDialect(new PostgreSql());
        registerDialect(new Oracle());
        registerDialect(new SqlServer());
        registerDialect(new SQLite());
        registerDialect(new H2());
        registerDialect(new Db2());
        registerDialect(new Hsqldb());

        // IBM databases
        registerDialect(new AS400());
        registerDialect(new Informix());

        // Legacy/specific versions
        registerDialect(new Oracle9i());
        registerDialect(new SqlServer2012());

        // Chinese domestic databases
        registerDialect(new Oscar());
        registerDialect(new Xugudb());

        // Other databases
        registerDialect(new Firebird());
        registerDialect(new HerdDB());
        registerDialect(new CirroData());
    }

    private DialectRegistry() {
        // Utility class
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
    public static synchronized void registerDialect(Dialect dialect) {
        if (dialect == null) {
            throw new IllegalArgumentException("Dialect cannot be null");
        }
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
            String productName = metaData.getDatabaseProductName();
            String jdbcUrl = metaData.getURL();

            // Try to match by product name first
            for (Dialect dialect : DIALECTS) {
                if (dialect.supportsProduct(productName)) {
                    return dialect;
                }
            }

            // Fallback to URL matching
            return getDialectByUrl(jdbcUrl);
        } catch (SQLException e) {
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

        // Try to match by URL
        for (Dialect dialect : DIALECTS) {
            if (dialect.supportsUrl(jdbcUrl)) {
                URL_CACHE.put(jdbcUrl, dialect);
                return dialect;
            }
        }

        return UNKNOWN;
    }

    /**
     * Gets the dialect for the specified database product name.
     *
     * @param productName the database product name (from DatabaseMetaData)
     * @return the detected dialect, or UnknownDialect if no match found
     */
    public static Dialect getDialectByProductName(String productName) {
        if (productName == null || productName.isEmpty()) {
            return UNKNOWN;
        }

        for (Dialect dialect : DIALECTS) {
            if (dialect.supportsProduct(productName)) {
                return dialect;
            }
        }

        return UNKNOWN;
    }

    /**
     * Gets the dialect for the specified dialect name.
     *
     * <p>
     * Supported dialect names: mysql, postgresql, oracle, sqlserver, sqlite, h2, db2, informix, etc.
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
     * Gets the database dialect for the current datasource.
     *
     * <p>
     * This method determines the current datasource key from {@link Holder#getKey()} and returns the corresponding
     * dialect from the cache. If not cached, it will attempt to detect from DynamicDataSource (if available).
     * </p>
     *
     * <p>
     * This is the recommended method for multi-datasource scenarios with dynamic datasource switching.
     * </p>
     *
     * @return the database dialect, or {@link #UNKNOWN} if not found
     */
    public static Dialect getDialect() {
        String dsKey = Holder.getKey();
        return DS_KEY_CACHE.computeIfAbsent(dsKey, DialectRegistry::detectDialectFromKey);
    }

    /**
     * Detects dialect for a specific datasource key.
     *
     * @param dsKey the datasource key
     * @return the detected dialect
     */
    private static Dialect detectDialectFromKey(String dsKey) {
        try {
            // Try to get DataSource from DynamicDataSource (if available)
            DataSource dataSource = getDataSource(dsKey);
            if (dataSource != null) {
                return getDialect(dataSource);
            }
        } catch (Exception e) {
            // Ignore: DynamicDataSource may not be available
        }
        return UNKNOWN;
    }

    /**
     * Gets DataSource by key from DynamicDataSource (if available).
     *
     * @param dsKey the datasource key
     * @return the DataSource, or null if not found
     */
    private static DataSource getDataSource(String dsKey) {
        try {
            // Try to load DynamicDataSource class (may not be available in all environments)
            Class<?> ddsClass = Class.forName("org.miaixz.bus.starter.jdbc.DynamicDataSource");
            Object instance = ddsClass.getMethod("getInstance").invoke(null);
            if (instance != null) {
                Map<Object, Object> dataSources = (Map<Object, Object>) ddsClass.getMethod("getAllDataSources")
                        .invoke(instance);

                Object ds = dataSources.get(dsKey);
                if (ds instanceof DataSource) {
                    return (DataSource) ds;
                }
            }
        } catch (Exception e) {
            // DynamicDataSource not available
        }
        return null;
    }

    /**
     * Initializes the dialect for a specific datasource key.
     *
     * <p>
     * This method detects the dialect from the given DataSource and caches it for the specified datasource key. Should
     * be called during application startup for each configured datasource.
     * </p>
     *
     * @param dsKey      the datasource key (e.g., "master", "slave", "tenant_001")
     * @param dataSource the datasource
     */
    public static void initializeDialect(String dsKey, DataSource dataSource) {
        if (dsKey != null && dataSource != null) {
            Dialect dialect = getDialect(dataSource);
            DS_KEY_CACHE.put(dsKey, dialect);
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
        DS_KEY_CACHE.clear();
    }

    /**
     * Gets all registered dialects.
     *
     * @return a list of all registered dialects
     */
    public static List<Dialect> getAllDialects() {
        return new ArrayList<>(DIALECTS);
    }

    /**
     * Default dialect for unknown databases
     */
    private static class DefaultDialect extends AbstractDialect {

        public DefaultDialect() {
            super("Unknown", Normal.EMPTY);
        }

        @Override
        public boolean supportsProduct(String productName) {
            return false;
        }

        @Override
        public boolean supportsUrl(String jdbcUrl) {
            return false;
        }

        @Override
        public String getPaginationSql(String originalSql, Pageable pageable) {
            return originalSql;
        }

        @Override
        public boolean supportsMultiValuesInsert() {
            return false;
        }

        @Override
        public boolean supportsUpsert() {
            return false;
        }

        @Override
        public String getUpsertTemplate() {
            return null;
        }
    }

}
