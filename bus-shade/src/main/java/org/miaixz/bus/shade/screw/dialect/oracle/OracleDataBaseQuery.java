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
package org.miaixz.bus.shade.screw.dialect.oracle;

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
 * Oracle database query implementation.
 * <p>
 * This implementation uses data from the database driver. It is important to note that the configuration parameter
 * "remarks" must be set to true, otherwise table and column comments will not be retrieved. For HikariCP, this can be
 * set via: {@code config.addDataSourceProperty("remarks", "true");}.
 * <p>
 * However, this method of querying can be very slow. For performance reasons, especially for retrieving comments, this
 * implementation uses custom SQL queries against Oracle's data dictionary views. See Oracle's documentation on
 * performance extensions for more details: <a href=
 * "https://docs.oracle.com/en/database/oracle/oracle-database/20/jjdbc/performance-extensions.html#GUID-15865071-39F2-430F-9EDA-EB34D0B2D560">Performance
 * Extensions</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OracleDataBaseQuery extends AbstractDatabaseQuery {

    /**
     * Constructs an {@code OracleDataBaseQuery}.
     *
     * @param dataSource The JDBC data source.
     */
    public OracleDataBaseQuery(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Retrieves the database information.
     *
     * @return A {@link Database} object containing the database schema name.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public Database getDataBase() throws InternalException {
        OracleDatabase model = new OracleDatabase();
        // The current database name in Oracle corresponds to the schema.
        model.setDatabase(getSchema());
        return model;
    }

    /**
     * Retrieves information for all tables in the database.
     *
     * @return A list of {@link OracleTable} objects, each representing a table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<OracleTable> getTables() throws InternalException {
        ResultSet resultSet = null;
        try {
            // Initial query to get basic table information.
            resultSet = getMetaData()
                    .getTables(getCatalog(), getSchema(), Builder.PERCENT_SIGN, new String[] { "TABLE" });
            List<OracleTable> list = Mapping.convertList(resultSet, OracleTable.class);

            // Custom SQL to fetch table comments for performance reasons.
            String sql = "SELECT TABLE_NAME, COMMENTS AS REMARKS FROM USER_TAB_COMMENTS WHERE TABLE_TYPE = 'TABLE'";
            if (isDda()) {
                // If the user has DBA privileges, query DBA_TAB_COMMENTS to handle different schemas.
                sql = "SELECT TABLE_NAME, COMMENTS AS REMARKS FROM DBA_TAB_COMMENTS WHERE TABLE_TYPE = 'TABLE' AND OWNER = '"
                        + getSchema() + "'";
            }
            resultSet = prepareStatement(String.format(sql, getSchema())).executeQuery();
            List<OracleTable> inquires = Mapping.convertList(resultSet, OracleTable.class);

            // Merge comments into the table list.
            list.forEach(
                    model -> inquires.stream().filter(inquire -> model.getTableName().equals(inquire.getTableName()))
                            .forEachOrdered(inquire -> model.setRemarks(inquire.getRemarks())));
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
     * @return A list of {@link OracleColumn} objects for the specified table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<OracleColumn> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        ResultSet resultSet = null;
        try {
            // Initial query to get basic column information.
            resultSet = getMetaData().getColumns(getCatalog(), getSchema(), table, Builder.PERCENT_SIGN);
            List<OracleColumn> list = Mapping.convertList(resultSet, OracleColumn.class);

            // Custom SQL to fetch column comments and types for performance.
            List<String> tableNames = list.stream().map(OracleColumn::getTableName).distinct()
                    .collect(Collectors.toList());
            if (CollKit.isEmpty(columnsCaching)) {
                String sql;
                if (table.equals(Builder.PERCENT_SIGN)) {
                    // SQL for all tables.
                    sql = "SELECT ut.TABLE_NAME, ut.COLUMN_NAME, uc.comments as REMARKS, concat(concat(concat(ut.DATA_TYPE, '('), ut.DATA_LENGTH), ')') AS COLUMN_TYPE, ut.DATA_LENGTH as COLUMN_LENGTH FROM user_tab_columns ut INNER JOIN user_col_comments uc ON ut.TABLE_NAME = uc.table_name AND ut.COLUMN_NAME = uc.column_name";
                    if (isDda()) {
                        sql = "SELECT ut.TABLE_NAME, ut.COLUMN_NAME, uc.comments as REMARKS, concat(concat(concat(ut.DATA_TYPE, '('), ut.DATA_LENGTH), ')') AS COLUMN_TYPE, ut.DATA_LENGTH as COLUMN_LENGTH FROM dba_tab_columns ut INNER JOIN dba_col_comments uc ON ut.TABLE_NAME = uc.table_name AND ut.COLUMN_NAME = uc.column_name and ut.OWNER = uc.OWNER WHERE ut.OWNER = '"
                                + getDataBase() + "'";
                    }
                    PreparedStatement statement = prepareStatement(sql);
                    resultSet = statement.executeQuery();
                    int fetchSize = 4284;
                    if (resultSet.getFetchSize() < fetchSize) {
                        resultSet.setFetchSize(fetchSize);
                    }
                } else {
                    // SQL for a single table.
                    sql = "SELECT ut.TABLE_NAME, ut.COLUMN_NAME, uc.comments as REMARKS, concat(concat(concat(ut.DATA_TYPE, '('), ut.DATA_LENGTH), ')') AS COLUMN_TYPE, ut.DATA_LENGTH as COLUMN_LENGTH FROM user_tab_columns ut INNER JOIN user_col_comments uc ON ut.TABLE_NAME = uc.table_name AND ut.COLUMN_NAME = uc.column_name WHERE ut.Table_Name = '%s'";
                    if (isDda()) {
                        sql = "SELECT ut.TABLE_NAME, ut.COLUMN_NAME, uc.comments as REMARKS, concat(concat(concat(ut.DATA_TYPE, '('), ut.DATA_LENGTH), ')') AS COLUMN_TYPE, ut.DATA_LENGTH as COLUMN_LENGTH FROM dba_tab_columns ut INNER JOIN dba_col_comments uc ON ut.TABLE_NAME = uc.table_name AND ut.COLUMN_NAME = uc.column_name and ut.OWNER = uc.OWNER WHERE ut.Table_Name = '%s' and ut.OWNER = '"
                                + getDataBase() + "'";
                    }
                    resultSet = prepareStatement(String.format(sql, table)).executeQuery();
                }
                List<OracleColumn> inquires = Mapping.convertList(resultSet, OracleColumn.class);
                // Cache the results.
                tableNames.forEach(
                        name -> columnsCaching.put(
                                name,
                                inquires.stream().filter(i -> i.getTableName().equals(name))
                                        .collect(Collectors.toList())));
            }

            // Merge comments and types from cache.
            for (OracleColumn i : list) {
                List<Column> columns = columnsCaching.get(i.getTableName());
                columns.forEach(j -> {
                    if (i.getColumnName().equals(j.getColumnName()) && i.getTableName().equals(j.getTableName())) {
                        i.setRemarks(j.getRemarks());
                        i.setColumnLength(j.getColumnLength());
                        i.setColumnType(j.getColumnType());
                    }
                });
            }
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
            return Mapping.convertList(resultSet, OraclePrimaryKey.class);
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
            String sql = "SELECT NULL AS TABLE_CAT, C.OWNER AS TABLE_SCHEM, C.TABLE_NAME, C.COLUMN_NAME, C.POSITION AS KEY_SEQ, C.CONSTRAINT_NAME AS PK_NAME FROM ALL_CONS_COLUMNS C, ALL_CONSTRAINTS K WHERE K.CONSTRAINT_TYPE = 'P' AND K.OWNER LIKE '%s' ESCAPE '/' AND K.CONSTRAINT_NAME = C.CONSTRAINT_NAME AND K.TABLE_NAME = C.TABLE_NAME AND K.OWNER = C.OWNER ORDER BY COLUMN_NAME ";
            resultSet = prepareStatement(String.format(sql, getDataBase().getDatabase())).executeQuery();
            return Mapping.convertList(resultSet, OraclePrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

    /**
     * Checks if the current user has DBA privileges.
     *
     * @return {@code true} if the user is a DBA, {@code false} otherwise.
     */
    private boolean isDda() {
        ResultSet resultSet = null;
        try {
            resultSet = prepareStatement("SELECT USERENV('isdba') as IS_DBA FROM DUAL").executeQuery();
            String dbaColumn = "IS_DBA";
            resultSet.next();
            return resultSet.getBoolean(dbaColumn);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

}
