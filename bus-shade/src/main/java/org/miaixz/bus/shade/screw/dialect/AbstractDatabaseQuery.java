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
package org.miaixz.bus.shade.screw.dialect;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.metadata.Column;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;

import lombok.Getter;

/**
 * Abstract base class for database query implementations. Provides common functionality such as connection management,
 * resource handling, and metadata access.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractDatabaseQuery implements DatabaseQuery {

    /**
     * Cache for storing retrieved column information to avoid redundant database queries.
     */
    protected final Map<String, List<Column>> columnsCaching = new ConcurrentHashMap<>();
    /**
     * The JDBC data source used to obtain database connections.
     */
    @Getter
    private final DataSource dataSource;

    /**
     * The database connection, managed with double-checked locking for thread safety.
     */
    volatile protected Connection connection;

    /**
     * Constructs an {@code AbstractDatabaseQuery} with the given data source.
     *
     * @param dataSource The JDBC data source.
     */
    public AbstractDatabaseQuery(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Safely closes a {@link ResultSet} resource.
     *
     * @param rs The {@link ResultSet} to close.
     * @throws InternalException if a {@link SQLException} occurs during closing.
     */
    public static void close(ResultSet rs) {
        if (!Objects.isNull(rs)) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * Safely closes a {@link Connection} resource.
     *
     * @param conn The {@link Connection} to close.
     * @throws InternalException if a {@link SQLException} occurs during closing.
     */
    public static void close(Connection conn) {
        if (!Objects.isNull(conn)) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * Safely closes a {@link ResultSet} and a {@link Connection} resource.
     *
     * @param rs   The {@link ResultSet} to close.
     * @param conn The {@link Connection} to close.
     * @throws InternalException if a {@link SQLException} occurs during closing.
     */
    public static void close(ResultSet rs, Connection conn) {
        close(rs);
        close(conn);
    }

    /**
     * Safely closes a {@link ResultSet}, a {@link Statement}, and a {@link Connection} resource.
     *
     * @param rs   The {@link ResultSet} to close.
     * @param st   The {@link Statement} to close.
     * @param conn The {@link Connection} to close.
     * @throws InternalException if a {@link SQLException} occurs during closing.
     */
    public static void close(ResultSet rs, Statement st, Connection conn) {
        close(rs);
        if (!Objects.isNull(st)) {
            try {
                st.close();
            } catch (SQLException e) {
                throw new InternalException(e);
            }
        }
        close(conn);
    }

    /**
     * Retrieves a database connection using a thread-safe, double-checked locking pattern. If the connection is null or
     * closed, a new one is obtained from the data source.
     *
     * @return A valid {@link Connection} object.
     * @throws InternalException if a {@link SQLException} occurs while getting the connection.
     */
    private Connection getConnection() throws InternalException {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            synchronized (AbstractDatabaseQuery.class) {
                if (connection == null || connection.isClosed()) {
                    this.connection = this.getDataSource().getConnection();
                }
            }
            return this.connection;
        } catch (SQLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves the catalog name for this database connection.
     *
     * @return The catalog name, or {@code null} if it is blank.
     * @throws InternalException if a {@link SQLException} occurs.
     */
    protected String getCatalog() throws InternalException {
        try {
            String catalog = this.getConnection().getCatalog();
            if (StringKit.isBlank(catalog)) {
                return null;
            }
            return catalog;
        } catch (SQLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves the schema name for this database connection. This method includes special handling for different
     * database types, such as CacheDB.
     *
     * @return The schema name, or {@code null} if it is blank.
     * @throws InternalException if a {@link SQLException} occurs.
     */
    protected String getSchema() throws InternalException {
        try {
            String schema;
            String url = this.getDataSource().getConnection().getMetaData().getURL();
            String name = DatabaseType.getDbType(url).getName();
            if (DatabaseType.CACHEDB.getName().equals(name)) {
                schema = verifySchema(this.getDataSource());
            } else {
                schema = this.getConnection().getSchema();
            }

            if (StringKit.isBlank(schema)) {
                return null;
            }
            return schema;
        } catch (SQLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Verifies the existence of the schema from the data source, particularly for databases like CacheDB.
     *
     * @param dataSource The {@link DataSource} to use for verification.
     * @return The schema name if it exists; otherwise, {@code null}.
     * @throws SQLException if a database access error occurs.
     */
    private String verifySchema(DataSource dataSource) throws SQLException {
        String schema = dataSource.getConnection().getSchema();

        ResultSet resultSet = this.getConnection().getMetaData().getSchemas();
        while (resultSet.next()) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = resultSet.getString(i);
                if (StringKit.isNotBlank(columnValue) && columnValue.contains(schema)) {
                    return schema;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the {@link DatabaseMetaData} for this database connection.
     *
     * @return The {@link DatabaseMetaData} object.
     * @throws InternalException if a {@link SQLException} occurs.
     */
    protected DatabaseMetaData getMetaData() throws InternalException {
        try {
            return this.getConnection().getMetaData();
        } catch (SQLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a {@link PreparedStatement} for the given SQL string.
     *
     * @param sql The SQL query string.
     * @return A new {@link PreparedStatement} object.
     * @throws InternalException if a {@link SQLException} occurs.
     */
    protected PreparedStatement prepareStatement(String sql) throws InternalException {
        Assert.notEmpty(sql, "Sql can not be empty!");
        try {
            return this.getConnection().prepareStatement(sql);
        } catch (SQLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves the primary keys for a table. This method is not supported in the abstract class and must be
     * implemented by subclasses.
     *
     * @return A list of primary keys.
     * @throws InternalException always, as this method is not implemented in the base class.
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys() throws InternalException {
        throw new InternalException(Builder.NOT_SUPPORTED);
    }

}
