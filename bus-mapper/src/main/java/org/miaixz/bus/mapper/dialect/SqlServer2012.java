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
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Modify;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Dialect implementation for SQL Server 2012 style pagination.
 *
 * <p>
 * This dialect uses SQL Server {@code OFFSET ... FETCH NEXT} pagination and injects a neutral {@code ORDER BY}
 * expression when required. UPSERT handling is mapped to the {@code MERGE_USING_VALUES} family.
 * </p>
 *
 * @author Kimi Liu
 */
public class SqlServer2012 extends AbstractDialect {

    /**
     * Creates the SQL Server 2012 dialect.
     */
    public SqlServer2012() {
        super("Microsoft SQL Server", "jdbc:sqlserver:");
    }

    /**
     * Returns the UPSERT family used by SQL Server 2012 in this framework.
     *
     * @return {@link Behavior#MERGE_USING_VALUES}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.MERGE_USING_VALUES;
    }

    /**
     * Returns the database behavior set advertised by this dialect.
     *
     * @return the supported behavior set
     */
    @Override
    public EnumSet<Behavior> types() {
        var types = schemaTypes(getUpsertType());
        types.add(Behavior.DROP_COLUMN_DEFAULT);
        return types;
    }

    /**
     * Looks up the SQL Server default constraint attached to the selected physical column.
     *
     * @param connection active connection in the target catalog
     * @param table      mapped physical table
     * @param column     actual column name
     * @return SQL to remove the exact default constraint, or null when none is attached
     * @throws SQLException when the system catalogue cannot be read
     */
    @Override
    public String dropColumnDefault(Connection connection, TableMeta table, String column) throws SQLException {
        return SqlServer.sqlServerDropColumnDefault(connection, table, column);
    }

    /**
     * Reports native support for filtered indexes in SQL Server 2012.
     *
     * @return true when the dialect can generate and inspect integer equality filters
     */
    @Override
    public boolean supportsPartialIndexes() {
        return true;
    }

    /**
     * Reads filtered SQL Server 2012 indexes with their native predicates and key columns.
     *
     * @param connection active SQL Server connection
     * @param table      table whose indexes are requested
     * @return native and ordinary indexes
     * @throws SQLException when the system catalogue cannot be read
     */
    @Override
    public List<IndexMeta> readIndexes(Connection connection, TableMeta table) throws SQLException {
        return SqlServer.filteredIndexes(connection, table, super.readIndexes(connection, table));
    }

    /**
     * Resolves the SQL type descriptor used by this dialect for the supplied mapper column.
     *
     * @param column the mapper column metadata
     * @return the SQL type descriptor for the column
     */
    @Override
    public SqlTypeDescriptor resolveType(ColumnMeta column) {
        return commonType(column, "INT", "DATETIME2", "DATETIMEOFFSET", "VARBINARY(MAX)", "VARCHAR(MAX)", "DECIMAL");
    }

    /**
     * Builds the dialect-specific DDL used to replace or modify a column definition.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated column modification SQL
     */
    @Override
    protected String modifyColumn(TableMeta table, ColumnMeta column) {
        return modifyColumn(table, column, Modify.ALTER_COLUMN);
    }

    /**
     * Builds the DDL used to set a SQL Server table comment.
     *
     * @param table the mapper table metadata
     * @return the generated table-comment SQL
     */
    @Override
    public String modifyTableComment(TableMeta table) {
        return sqlServerModifyTableComment(table);
    }

    /**
     * Builds the DDL used to set a SQL Server column comment.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated column-comment SQL
     */
    @Override
    public String modifyColumnComment(TableMeta table, ColumnMeta column) {
        return sqlServerModifyColumnComment(table, column);
    }

    /**
     * Returns the keyword used by SQL Server 2012 to express row limits.
     *
     * @return the keyword {@code FETCH NEXT}
     */
    @Override
    public String getLimitKeyword() {
        return "FETCH NEXT";
    }

    /**
     * Builds paginated SQL using SQL Server 2012 {@code OFFSET ... FETCH NEXT} syntax.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement
     */
    @Override
    public String buildPaginationSql(String originalSql, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return originalSql;
        }
        StringBuilder sql = new StringBuilder(originalSql.length() + 100);
        sql.append(originalSql);
        if (!StringKit.toUpperCase(originalSql).contains("ORDER BY")) {
            sql.append(" ORDER BY (SELECT NULL)");
        }
        sql.append(" OFFSET ").append(pageable.getOffset()).append(" ROWS");
        sql.append(" FETCH NEXT ").append(pageable.getPageSize()).append(" ROWS ONLY");
        return sql.toString();
    }

}
