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
package org.miaixz.bus.shade.screw.dialect.cachedb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
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
import org.miaixz.bus.shade.screw.metadata.Table;

/**
 * Database query implementation for CacheDB.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheDbDataBaseQuery extends AbstractDatabaseQuery {

    /**
     * Constructs a {@code CacheDbDataBaseQuery}.
     *
     * @param dataSource The JDBC data source.
     */
    public CacheDbDataBaseQuery(DataSource dataSource) {
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
        CacheDbDatabase model = new CacheDbDatabase();
        // Get the current database name (schema)
        model.setDatabase(getSchema());
        return model;
    }

    /**
     * Retrieves information for all tables in the database.
     *
     * @return A list of {@link Table} objects, each representing a table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<? extends Table> getTables() throws InternalException {
        ResultSet resultSet = null;
        try {
            // Query for tables
            resultSet = getMetaData().getTables(getCatalog(), getSchema(), null, new String[] { "TABLE" });
            // Map the result set to a list of CacheDbTable objects
            return Mapping.convertList(resultSet, CacheDbTable.class);
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
     * @return A list of {@link Column} objects for the specified table.
     * @throws InternalException if an error occurs during the query.
     */
    @Override
    public List<? extends Column> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        ResultSet resultSet = null;
        try {
            // Query for columns
            resultSet = getMetaData().getColumns(getCatalog(), getSchema(), table, Builder.PERCENT_SIGN);
            // Map the result set to a list of CacheDbColumn objects
            final List<CacheDbColumn> list = Mapping.convertList(resultSet, CacheDbColumn.class);
            // Get unique table names from the result
            List<String> tableNames = list.stream().map(CacheDbColumn::getTableName).distinct()
                    .collect(Collectors.toList());
            if (CollKit.isEmpty(columnsCaching)) {
                // If querying for all tables
                if (table.equals(Builder.PERCENT_SIGN)) {
                    String sql = MessageFormat.format(
                            "select TABLE_NAME as \"TABLE_NAME\",COLUMN_NAME as "
                                    + "\"COLUMN_NAME\",DESCRIPTION as \"REMARKS\","
                                    + "case when CHARACTER_MAXIMUM_LENGTH is null then DATA_TYPE  || '''' "
                                    + "else DATA_TYPE  || ''(''||CHARACTER_MAXIMUM_LENGTH ||'')'' end as \"COLUMN_TYPE\" "
                                    + "from INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ''{0}''",
                            getSchema());
                    PreparedStatement statement = prepareStatement(sql);
                    resultSet = statement.executeQuery();
                    int fetchSize = 4284;
                    if (resultSet.getFetchSize() < fetchSize) {
                        resultSet.setFetchSize(fetchSize);
                    }
                }
                // If querying for a single table
                else {
                    String sql = MessageFormat.format(
                            "select TABLE_NAME as \"TABLE_NAME\",COLUMN_NAME as "
                                    + "\"COLUMN_NAME\",DESCRIPTION as \"REMARKS\","
                                    + "case when CHARACTER_MAXIMUM_LENGTH is null then DATA_TYPE  || ''''"
                                    + "else DATA_TYPE  || ''(''||CHARACTER_MAXIMUM_LENGTH ||'')'' end as \"COLUMN_TYPE\" "
                                    + "from INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ''{0}'' and TABLE_NAME = "
                                    + "''{1}''",
                            getSchema(),
                            table);
                    resultSet = prepareStatement(sql).executeQuery();
                }
                List<CacheDbColumn> inquires = Mapping.convertList(resultSet, CacheDbColumn.class);
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
                        i.setRemarks(j.getRemarks());
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
            // Map the result set to a list of CacheDbPrimaryKey objects
            return Mapping.convertList(resultSet, CacheDbPrimaryKey.class);
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
            String sql = "select TABLE_CATALOG ,TABLE_NAME as \"TABLE_NAME\",TABLE_SCHEMA as \"TABLE_SCHEM\",COLUMN_NAME as \"COLUMN_NAME\",ORDINAL_POSITION as \"KEY_SEQ\" from INFORMATION_SCHEMA.COLUMNS where PRIMARY_KEY='YES' and TABLE_SCHEMA='%s'";
            // Format SQL with the database name
            resultSet = prepareStatement(String.format(sql, getDataBase().getDatabase())).executeQuery();
            return Mapping.convertList(resultSet, CacheDbPrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

}
