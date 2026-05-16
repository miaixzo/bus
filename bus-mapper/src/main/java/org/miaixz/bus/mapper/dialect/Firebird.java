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

import java.util.EnumSet;

import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.support.paging.Pageable;
import org.miaixz.bus.mapper.support.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.support.schema.SqlTypeDescriptor;

/**
 * Dialect implementation for Firebird databases.
 *
 * <p>
 * This dialect uses Firebird {@code FIRST/SKIP} pagination and maps UPSERT handling to the {@code UPDATE_OR_INSERT}
 * family.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Firebird extends AbstractDialect {

    /**
     * Creates the Firebird dialect.
     */
    public Firebird() {
        super("Firebird", "jdbc:firebirdsql:");
    }

    /**
     * Returns the UPSERT family used by Firebird in this framework.
     *
     * @return {@link Behavior#UPDATE_OR_INSERT}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.UPDATE_OR_INSERT;
    }

    /**
     * Returns the database behavior set advertised by this dialect.
     *
     * @return the supported behavior set
     */
    @Override
    public EnumSet<Behavior> types() {
        return schemaTypes(getUpsertType());
    }

    /**
     * Resolves the SQL type descriptor used by this dialect for the supplied mapper column.
     *
     * @param column the mapper column metadata
     * @return the SQL type descriptor for the column
     */
    @Override
    public SqlTypeDescriptor resolveType(ColumnMeta column) {
        return commonType(column, "INTEGER", "TIMESTAMP", "TIMESTAMP", "BLOB", "BLOB SUB_TYPE TEXT", "DECIMAL");
    }

    /**
     * Builds the DDL used to add a column to an existing table.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated add-column SQL
     */
    @Override
    public String addColumn(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE " + tableName(table) + " ADD " + columnDefinition(column);
    }

    /**
     * Builds the DDL used to change the nullable flag for a column.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated nullable-change SQL
     */
    @Override
    public String modifyColumnNullable(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return alterColumnNullable(table, column);
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
        return "ALTER TABLE " + tableName(table) + " ALTER " + identifier(column.column()) + " TYPE "
                + resolveType(column).definition();
    }

    /**
     * Returns the Firebird keyword used for row limits.
     *
     * @return the keyword {@code FIRST}
     */
    @Override
    public String getLimitKeyword() {
        return "FIRST";
    }

    /**
     * Returns the Firebird keyword used for row offsets.
     *
     * @return the keyword {@code SKIP}
     */
    @Override
    public String getOffsetKeyword() {
        return "SKIP";
    }

    /**
     * Builds paginated SQL by injecting Firebird {@code FIRST/SKIP} clauses after the {@code SELECT} keyword.
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
        int selectIndex = originalSql.toUpperCase().indexOf("SELECT");
        if (selectIndex >= 0) {
            sql.append(originalSql, 0, selectIndex + 6);
            sql.append(" FIRST ").append(pageable.getPageSize());
            if (pageable.getOffset() > 0) {
                sql.append(" SKIP ").append(pageable.getOffset());
            }
            sql.append(originalSql.substring(selectIndex + 6));
        } else {
            sql.append(originalSql);
        }
        return sql.toString();
    }

}
