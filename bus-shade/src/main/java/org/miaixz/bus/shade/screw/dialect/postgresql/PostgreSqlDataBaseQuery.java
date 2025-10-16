/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.screw.dialect.postgresql;

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
 * PostgreSQL database query implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PostgreSqlDataBaseQuery extends AbstractDatabaseQuery {

    /**
     * Constructs a {@code PostgreSqlDataBaseQuery}.
     *
     * @param dataSource The JDBC data source.
     */
    public PostgreSqlDataBaseQuery(DataSource dataSource) {
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
        PostgreSqlDatabase model = new PostgreSqlDatabase();
        // Get the current database name (catalog)
        model.setDatabase(getCatalog());
        return model;
    }

    /**
     * Retrieves information for all tables in the database.
     *
     * @return A list of {@link PostgreSqlTable} objects, each representing a table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<PostgreSqlTable> getTables() throws InternalException {
        ResultSet resultSet = null;
        try {
            // Query for tables
            resultSet = getMetaData()
                    .getTables(getCatalog(), getSchema(), Builder.PERCENT_SIGN, new String[] { "TABLE" });
            // Map the result set to a list of PostgreSqlTable objects
            return Mapping.convertList(resultSet, PostgreSqlTable.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }

    }

    /**
     * Retrieves column information for a specific table.
     *
     * @param table The name of the table.
     * @return A list of {@link PostgreSqlColumn} objects for the specified table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<PostgreSqlColumn> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        ResultSet resultSet = null;
        try {
            // Query for columns
            resultSet = getMetaData().getColumns(getCatalog(), getSchema(), table, Builder.PERCENT_SIGN);
            // Map the result set to a list of PostgreSqlColumn objects
            List<PostgreSqlColumn> list = Mapping.convertList(resultSet, PostgreSqlColumn.class);
            // Get unique table names from the result
            List<String> tableNames = list.stream().map(PostgreSqlColumn::getTableName).distinct()
                    .collect(Collectors.toList());
            if (CollKit.isEmpty(columnsCaching)) {
                // If querying for all tables
                if (table.equals(Builder.PERCENT_SIGN)) {
                    String sql = "SELECT \"TABLE_NAME\", \"TABLE_SCHEMA\", \"COLUMN_NAME\", \"LENGTH\", concat(\"UDT_NAME\", case when \"LENGTH\" isnull then '' else concat('(', concat(\"LENGTH\", ')')) end) \"COLUMN_TYPE\" FROM(select table_schema as \"TABLE_SCHEMA\", column_name as \"COLUMN_NAME\", table_name as \"TABLE_NAME\", udt_name as \"UDT_NAME\", case when coalesce(character_maximum_length, numeric_precision, -1) = -1 then null else coalesce(character_maximum_length, numeric_precision, -1) end as \"LENGTH\" from information_schema.columns a where  table_schema = '%s' and table_catalog = '%s') t";
                    PreparedStatement statement = prepareStatement(
                            String.format(sql, getSchema(), getDataBase().getDatabase()));
                    resultSet = statement.executeQuery();
                    int fetchSize = 4284;
                    if (resultSet.getFetchSize() < fetchSize) {
                        resultSet.setFetchSize(fetchSize);
                    }
                }
                // If querying for a single table
                else {
                    String sql = "SELECT \"TABLE_NAME\", \"TABLE_SCHEMA\", \"COLUMN_NAME\", \"LENGTH\", concat(\"UDT_NAME\", case when \"LENGTH\" isnull then '' else concat('(', concat(\"LENGTH\", ')')) end) \"COLUMN_TYPE\" FROM(select table_schema as \"TABLE_SCHEMA\", column_name as \"COLUMN_NAME\", table_name as \"TABLE_NAME\", udt_name as \"UDT_NAME\", case when coalesce(character_maximum_length, numeric_precision, -1) = -1 then null else coalesce(character_maximum_length, numeric_precision, -1) end as \"LENGTH\" from information_schema.columns a where table_name = '%s' and table_schema = '%s' and table_catalog = '%s') t";
                    resultSet = prepareStatement(String.format(sql, table, getSchema(), getDataBase().getDatabase()))
                            .executeQuery();
                }
                List<PostgreSqlColumn> inquires = Mapping.convertList(resultSet, PostgreSqlColumn.class);
                // Cache the column information by table name
                tableNames.forEach(
                        name -> columnsCaching.put(
                                name,
                                inquires.stream().filter(i -> i.getTableName().equals(name))
                                        .collect(Collectors.toList())));
            }
            // Populate remarks and other details from the cached or freshly queried data
            list.forEach(i -> {
                List<Column> columns = columnsCaching.get(i.getTableName());
                columns.forEach(j -> {
                    if (i.getColumnName().equals(j.getColumnName()) && i.getTableName().equals(j.getTableName())) {
                        i.setColumnLength(j.getColumnLength());
                        i.setColumnType(j.getColumnType());
                    }
                });
            });
            return list;
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
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
            // Query for primary keys
            resultSet = getMetaData().getPrimaryKeys(getCatalog(), getSchema(), table);
            // Map the result set to a list of PostgreSqlPrimaryKey objects
            return Mapping.convertList(resultSet, PostgreSqlPrimaryKey.class);
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
            // Custom SQL for better performance when querying all primary keys
            String sql = "SELECT result.TABLE_CAT, result.TABLE_SCHEM, result.TABLE_NAME, result.COLUMN_NAME, result.KEY_SEQ, result.PK_NAME FROM(SELECT NULL AS TABLE_CAT, n.nspname AS TABLE_SCHEM, ct.relname AS TABLE_NAME, a.attname AS COLUMN_NAME, (information_schema._pg_expandarray(i.indkey)).n AS KEY_SEQ, ci.relname AS PK_NAME, information_schema._pg_expandarray(i.indkey) AS KEYS, a.attnum AS A_ATTNUM FROM pg_catalog.pg_class ct JOIN pg_catalog.pg_attribute a ON (ct.oid = a.attrelid) JOIN pg_catalog.pg_namespace n ON (ct.relnamespace = n.oid) JOIN pg_catalog.pg_index i ON (a.attrelid = i.indrelid) JOIN pg_catalog.pg_class ci ON (ci.oid = i.indexrelid) WHERE true AND n.nspname = 'public' AND i.indisprimary) result where result.A_ATTNUM = (result.KEYS).x ORDER BY result.table_name, result.pk_name, result.key_seq";
            // Execute the query
            resultSet = prepareStatement(sql).executeQuery();
            return Mapping.convertList(resultSet, PostgreSqlPrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

}
