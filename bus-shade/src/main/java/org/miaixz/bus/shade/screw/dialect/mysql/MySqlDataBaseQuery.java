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
package org.miaixz.bus.shade.screw.dialect.mysql;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.dialect.AbstractDatabaseQuery;
import org.miaixz.bus.shade.screw.mapping.Mapping;
import org.miaixz.bus.shade.screw.metadata.Column;
import org.miaixz.bus.shade.screw.metadata.Database;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MySQL database query implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MySqlDataBaseQuery extends AbstractDatabaseQuery {

    /**
     * Constructs a {@code MySqlDataBaseQuery}.
     *
     * @param dataSource The JDBC data source.
     */
    public MySqlDataBaseQuery(DataSource dataSource) {
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
        MySqlDatabase model = new MySqlDatabase();
        // Get the current database name (catalog)
        model.setDatabase(getCatalog());
        return model;
    }

    /**
     * Retrieves information for all tables in the database.
     *
     * @return A list of {@link MySqlTable} objects, each representing a table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<MySqlTable> getTables() throws InternalException {
        ResultSet resultSet = null;
        try {
            // Query for tables
            resultSet = getMetaData()
                    .getTables(getCatalog(), getSchema(), Builder.PERCENT_SIGN, new String[] { "TABLE" });
            // Map the result set to a list of MySqlTable objects
            return Mapping.convertList(resultSet, MySqlTable.class);
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
     * @return A list of {@link MySqlColumn} objects for the specified table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<MySqlColumn> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        ResultSet resultSet = null;
        try {
            // Query for columns
            resultSet = getMetaData().getColumns(getCatalog(), getSchema(), table, Builder.PERCENT_SIGN);
            // Map the result set to a list of MySqlColumn objects
            List<MySqlColumn> list = Mapping.convertList(resultSet, MySqlColumn.class);
            // Get unique table names from the result
            List<String> tableNames = list.stream().map(MySqlColumn::getTableName).distinct()
                    .collect(Collectors.toList());
            if (CollKit.isEmpty(columnsCaching)) {
                // If querying for all tables
                if (table.equals(Builder.PERCENT_SIGN)) {
                    String sql = "SELECT A.TABLE_NAME, A.COLUMN_NAME, A.COLUMN_TYPE, case when LOCATE('(', A.COLUMN_TYPE) > 0 then replace(substring(A.COLUMN_TYPE, LOCATE('(', A.COLUMN_TYPE) + 1), ')', '') else null end COLUMN_LENGTH FROM INFORMATION_SCHEMA.COLUMNS A WHERE A.TABLE_SCHEMA = '%s'";
                    PreparedStatement statement = prepareStatement(String.format(sql, getDataBase().getDatabase()));
                    resultSet = statement.executeQuery();
                    int fetchSize = 4284;
                    if (resultSet.getFetchSize() < fetchSize) {
                        resultSet.setFetchSize(fetchSize);
                    }
                }
                // If querying for a single table
                else {
                    String sql = "SELECT A.TABLE_NAME, A.COLUMN_NAME, A.COLUMN_TYPE, case when LOCATE('(', A.COLUMN_TYPE) > 0 then replace(substring(A.COLUMN_TYPE, LOCATE('(', A.COLUMN_TYPE) + 1), ')', '') else null end COLUMN_LENGTH FROM INFORMATION_SCHEMA.COLUMNS A WHERE A.TABLE_SCHEMA = '%s' and A.TABLE_NAME = '%s'";
                    resultSet = prepareStatement(String.format(sql, getDataBase().getDatabase(), table)).executeQuery();
                }
                List<MySqlColumn> inquires = Mapping.convertList(resultSet, MySqlColumn.class);
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
                        i.setColumnType(j.getColumnType());
                        i.setColumnLength(j.getColumnLength());
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
            // Map the result set to a list of MySqlPrimaryKey objects
            return Mapping.convertList(resultSet, MySqlPrimaryKey.class);
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
            String sql = "SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, SEQ_IN_INDEX AS KEY_SEQ, 'PRIMARY' AS PK_NAME FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = '%s' AND INDEX_NAME = 'PRIMARY' ORDER BY TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX";
            // Format SQL with the database name
            resultSet = prepareStatement(String.format(sql, getDataBase().getDatabase())).executeQuery();
            return Mapping.convertList(resultSet, MySqlPrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

}
