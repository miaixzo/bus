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
package org.miaixz.bus.cache.collect;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * A minimal plain-JDBC runner replacing Spring's {@code JdbcTemplate} / {@code JdbcOperations}.
 * <p>
 * Supports three operations: DDL execution, parameterized updates, and parameterized list queries. Two static factory
 * methods are provided:
 * <ul>
 * <li>{@link #forDataSource(DataSource)} — for pooled connections (HikariCP).</li>
 * <li>{@link #forSingleConnection(String, String, String, String)} — for a persistent single connection suitable for
 * in-memory databases (H2, SQLite).</li>
 * </ul>
 * The single-connection variant wraps the raw {@link Connection} in a no-close proxy so that try-with-resources blocks
 * do not terminate the underlying connection (and thus the in-memory database state).
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
class JdbcRunner {

    /** The underlying data source from which all connections are obtained. */
    private final DataSource dataSource;

    /**
     * Private constructor; use static factory methods to create instances.
     *
     * @param dataSource the underlying data source
     */
    private JdbcRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates a {@code JdbcRunner} backed by the supplied pooled {@link DataSource}.
     *
     * @param dataSource the pooled data source (e.g., HikariCP)
     * @return a new {@code JdbcRunner}
     */
    static JdbcRunner forDataSource(DataSource dataSource) {
        return new JdbcRunner(dataSource);
    }

    /**
     * Creates a {@code JdbcRunner} that holds a single persistent connection.
     * <p>
     * Suitable for embedded / in-memory databases such as H2 and SQLite, where closing the connection would destroy all
     * in-memory data. The returned connection is wrapped in a no-op-close proxy.
     * </p>
     *
     * @param driverClassName fully-qualified JDBC driver class name
     * @param url             JDBC URL
     * @param username        database user (may be {@code null})
     * @param password        database password (may be {@code null})
     * @return a new {@code JdbcRunner} backed by a single connection
     */
    static JdbcRunner forSingleConnection(String driverClassName, String url, String username, String password) {
        try {
            Class.forName(driverClassName);
            Properties props = new Properties();
            if (username != null) {
                props.setProperty("user", username);
            }
            if (password != null) {
                props.setProperty("password", password);
            }
            Connection raw = DriverManager.getConnection(url, props);
            // Wrap in a no-close proxy so try-with-resources blocks do not kill the in-memory DB.
            Connection noClose = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[] { Connection.class },
                    (proxy, method, args) -> {
                        if ("close".equals(method.getName())) {
                            return null;
                        }
                        return method.invoke(raw, args);
                    });
            return new JdbcRunner(new SingleConnectionSource(noClose));
        } catch (Exception e) {
            throw new RuntimeException("Failed to open JDBC connection: " + url, e);
        }
    }

    /**
     * Executes a DDL statement (e.g., {@code CREATE TABLE}).
     *
     * @param sql the SQL statement to execute
     */
    void execute(String sql) {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL: " + sql, e);
        }
    }

    /**
     * Executes a parameterized DML statement (INSERT, UPDATE, DELETE).
     *
     * @param sql    the parameterized SQL string
     * @param params positional bind parameters
     * @return the number of affected rows
     */
    int update(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute update: " + sql, e);
        }
    }

    /**
     * Executes a parameterized SELECT and returns all rows as a list of column-name-to-value maps.
     * <p>
     * Column names are taken from {@link ResultSetMetaData#getColumnLabel(int)} and are returned in the case provided
     * by the JDBC driver (H2 uses upper-case; MySQL/PostgreSQL/SQLite use lower-case). Callers should access columns
     * accordingly.
     * </p>
     *
     * @param sql    the parameterized SQL string
     * @param params positional bind parameters
     * @return a list of rows, each row being a {@code Map} from column label to value
     */
    List<Map<String, Object>> queryForList(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>(cols * 2);
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query: " + sql, e);
        }
    }

    /**
     * Minimal {@link DataSource} implementation backed by a single persistent {@link Connection}.
     * <p>
     * The connection is wrapped via a dynamic proxy that turns {@code close()} into a no-op, so that try-with-resources
     * blocks do not terminate the underlying connection and thereby destroy the in-memory database state (H2, SQLite).
     * </p>
     */
    private static final class SingleConnectionSource implements DataSource {

        /** The single persistent connection (wrapped in a no-close proxy). */
        private final Connection connection;

        /**
         * Constructs a {@code SingleConnectionSource} with the supplied connection.
         *
         * @param connection a {@link Connection} already wrapped in a no-close proxy
         */
        SingleConnectionSource(Connection connection) {
            this.connection = connection;
        }

        /**
         * Returns the single persistent connection.
         *
         * @return the persistent connection
         */
        @Override
        public Connection getConnection() {
            return connection;
        }

        /**
         * Returns the single persistent connection, ignoring the supplied credentials.
         *
         * @param username ignored
         * @param password ignored
         * @return the persistent connection
         */
        @Override
        public Connection getConnection(String username, String password) {
            return connection;
        }

        /**
         * Not supported; always throws {@link UnsupportedOperationException}.
         *
         * @return never returns normally
         */
        @Override
        public PrintWriter getLogWriter() {
            throw new UnsupportedOperationException();
        }

        /**
         * Not supported; always throws {@link UnsupportedOperationException}.
         *
         * @param out ignored
         */
        @Override
        public void setLogWriter(PrintWriter out) {
            throw new UnsupportedOperationException();
        }

        /**
         * Not supported; always throws {@link UnsupportedOperationException}.
         *
         * @param seconds ignored
         */
        @Override
        public void setLoginTimeout(int seconds) {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns the login timeout, fixed at {@code 0}.
         *
         * @return {@code 0}
         */
        @Override
        public int getLoginTimeout() {
            return 0;
        }

        /**
         * Not supported; always throws {@link UnsupportedOperationException}.
         *
         * @return never returns normally
         */
        @Override
        public Logger getParentLogger() {
            throw new UnsupportedOperationException();
        }

        /**
         * Not a wrapper; always throws {@link SQLException}.
         *
         * @param iface ignored
         * @param <T>   target type
         * @return never returns normally
         * @throws SQLException always
         */
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Not a wrapper");
        }

        /**
         * Always returns {@code false}; this class does not wrap any interface.
         *
         * @param iface ignored
         * @return {@code false}
         */
        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }
    }

}
