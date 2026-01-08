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
package org.miaixz.bus.shade.screw.dialect.sqlserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.dialect.AbstractDatabaseQuery;
import org.miaixz.bus.shade.screw.mapping.Mapping;
import org.miaixz.bus.shade.screw.metadata.Column;
import org.miaixz.bus.shade.screw.metadata.Database;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;

/**
 * SQL Server database query implementation.
 * <p>
 * This class addresses a limitation in the SQL Server JDBC driver where the `REMARKS` field is not returned for tables
 * and columns when using standard metadata methods. To work around this, custom SQL queries are used to fetch comments
 * and other details directly from system tables like `sysobjects` and `sys.extended_properties`.
 * <p>
 * See Microsoft's documentation for more details: <a href=
 * "https://docs.microsoft.com/en-us/sql/connect/jdbc/reference/getcolumns-method-sqlserverdatabasemetadata?view=sql-server-ver15">getColumns
 * Method</a> <a href=
 * "https://docs.microsoft.com/en-us/sql/connect/jdbc/reference/gettables-method-sqlserverdatabasemetadata?view=sql-server-ver15">getTables
 * Method</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlServerDataBaseQuery extends AbstractDatabaseQuery {

    /**
     * Constructs a {@code SqlServerDataBaseQuery}.
     *
     * @param dataSource The JDBC data source.
     */
    public SqlServerDataBaseQuery(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Retrieves the database information.
     *
     * @return A {@link Database} object containing the database name.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public Database getDataBase() throws InternalException {
        SqlServerDatabase model = new SqlServerDatabase();
        // The current database name in SQL Server corresponds to the catalog.
        model.setDatabase(getCatalog());
        return model;
    }

    /**
     * Retrieves information for all tables in the database.
     *
     * @return A list of {@link SqlServerTable} objects, each representing a table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<SqlServerTable> getTables() {
        ResultSet resultSet = null;
        try {
            // Initial query to get basic table information.
            resultSet = getMetaData()
                    .getTables(getCatalog(), getSchema(), Builder.PERCENT_SIGN, new String[] { "TABLE" });
            List<SqlServerTable> list = Mapping.convertList(resultSet, SqlServerTable.class);

            // Custom SQL to fetch table comments, as the driver does not provide them.
            String sql = "select cast(so.name as varchar(500)) as TABLE_NAME, cast(sep.value as varchar(500)) as REMARKS from sysobjects so left JOIN sys.extended_properties sep on sep.major_id = so.id and sep.minor_id = 0 where (xtype = 'U' or xtype = 'v')";
            resultSet = prepareStatement(String.format(sql, getCatalog())).executeQuery();
            List<SqlServerTable> inquires = Mapping.convertList(resultSet, SqlServerTable.class);

            // Merge comments into the table list.
            for (SqlServerTable model : list) {
                for (SqlServerTable inquire : inquires) {
                    if (model.getTableName().equals(inquire.getTableName())) {
                        model.setRemarks(inquire.getRemarks());
                    }
                }
            }
            return list;
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet, this.connection);
        }
    }

    /**
     * Retrieves column information for a specific table.
     *
     * @param table The name of the table.
     * @return A list of {@link SqlServerColumn} objects for the specified table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<SqlServerColumn> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        ResultSet resultSet = null;
        try {
            // Initial query to get basic column information.
            resultSet = getMetaData().getColumns(getCatalog(), getSchema(), table, Builder.PERCENT_SIGN);
            List<SqlServerColumn> list = Mapping.convertList(resultSet, SqlServerColumn.class);

            // Get unique table names from the result.
            List<String> tableNames = list.stream().map(SqlServerColumn::getTableName).distinct()
                    .collect(Collectors.toList());
            if (CollKit.isEmpty(columnsCaching)) {
                String sql;
                if (table.equals(Builder.PERCENT_SIGN)) {
                    // SQL for all tables to get detailed column info including comments.
                    sql = "SELECT cast(a.name AS VARCHAR(500)) AS TABLE_NAME, cast(b.name AS VARCHAR(500)) AS COLUMN_NAME, cast(c.VALUE AS NVARCHAR(500)) AS REMARKS, cast(sys.types.name AS VARCHAR(500)) + '(' + cast(b.max_length AS NVARCHAR(500)) + ')' AS COLUMN_TYPE, cast(b.max_length AS NVARCHAR(500)) AS COLUMN_LENGTH FROM(SELECT name, object_id FROM sys.tables UNION all SELECT name, object_id FROM sys.views) a INNER JOIN sys.columns b ON b.object_id = a.object_id LEFT JOIN sys.types ON b.user_type_id = sys.types.user_type_id LEFT JOIN sys.extended_properties c ON c.major_id = b.object_id AND c.minor_id = b.column_id";
                    PreparedStatement statement = prepareStatement(sql);
                    resultSet = statement.executeQuery();
                    int fetchSize = 4284;
                    if (resultSet.getFetchSize() < fetchSize) {
                        resultSet.setFetchSize(fetchSize);
                    }
                } else {
                    // SQL for a single table.
                    sql = "SELECT cast(a.name AS VARCHAR(500)) AS TABLE_NAME, cast(b.name AS VARCHAR(500)) AS COLUMN_NAME, cast(c.VALUE AS NVARCHAR(500)) AS REMARKS, cast(sys.types.name AS VARCHAR(500)) + '(' + cast(b.max_length AS NVARCHAR(500)) + ')' AS COLUMN_TYPE, cast(b.max_length AS NVARCHAR(500)) AS COLUMN_LENGTH FROM(SELECT name, object_id FROM sys.tables UNION all SELECT name, object_id FROM sys.views) a INNER JOIN sys.columns b ON b.object_id = a.object_id LEFT JOIN sys.types ON b.user_type_id = sys.types.user_type_id LEFT JOIN sys.extended_properties c ON c.major_id = b.object_id AND c.minor_id = b.column_id WHERE a.name = '%s'";
                    resultSet = prepareStatement(String.format(sql, table)).executeQuery();
                }
                List<SqlServerColumn> inquires = Mapping.convertList(resultSet, SqlServerColumn.class);
                // Cache the results.
                tableNames.forEach(
                        name -> columnsCaching.put(
                                name,
                                inquires.stream().filter(i -> i.getTableName().equals(name))
                                        .collect(Collectors.toList())));
            }

            // Merge comments and types from cache.
            list.forEach(i -> {
                List<Column> columns = columnsCaching.get(i.getTableName());
                columns.forEach(j -> {
                    if (i.getColumnName().equals(j.getColumnName()) && i.getTableName().equals(j.getTableName())) {
                        i.setRemarks(j.getRemarks());
                        i.setColumnLength(j.getColumnLength());
                        i.setColumnType(j.getColumnType());
                    }
                });
            });
            return list;
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet, this.connection);
        }
    }

    /**
     * Retrieves column information for all tables.
     *
     * @return A list of {@link Column} objects for all tables.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<? extends Column> getTableColumns() throws InternalException {
        return getTableColumns(Builder.PERCENT_SIGN);
    }

    /**
     * Retrieves primary key information for a specific table.
     *
     * @param table The name of the table.
     * @return A list of {@link PrimaryKey} objects for the specified table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys(String table) throws InternalException {
        ResultSet resultSet = null;
        try {
            resultSet = getMetaData().getPrimaryKeys(getCatalog(), getSchema(), table);
            return Mapping.convertList(resultSet, SqlServerPrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet, this.connection);
        }
    }

    /**
     * Retrieves primary key information for all tables.
     *
     * @return A list of {@link PrimaryKey} objects for all tables.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys() throws InternalException {
        ResultSet resultSet = null;
        try {
            // Custom SQL for better performance when querying all primary keys.
            String sql = "SELECT TABLE_CATALOG AS 'TABLE_QUALIFIER', TABLE_SCHEMA AS 'TABLE_OWNER', TABLE_NAME AS 'TABLE_NAME', COLUMN_NAME AS 'COLUMN_NAME', ORDINAL_POSITION AS 'KEY_SEQ', CONSTRAINT_NAME AS 'PK_NAME' FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_CATALOG = '%s' AND TABLE_SCHEMA = '%s' ORDER BY KEY_SEQ";
            resultSet = prepareStatement(String.format(sql, getCatalog(), getSchema())).executeQuery();
            return Mapping.convertList(resultSet, SqlServerPrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

}
