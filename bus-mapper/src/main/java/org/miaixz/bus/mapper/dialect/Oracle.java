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
import org.miaixz.bus.mapper.support.schema.SqlTypeDescriptor;

/**
 * Dialect implementation for Oracle databases.
 *
 * <p>
 * This dialect uses Oracle-style {@code OFFSET ... FETCH NEXT} pagination and maps UPSERT handling to the
 * {@code MERGE_USING_DUAL} family.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Oracle extends AbstractDialect {

    /**
     * Creates the Oracle dialect.
     */
    public Oracle() {
        super("Oracle", "jdbc:oracle:");
    }

    /**
     * Returns the UPSERT family used by Oracle in this framework.
     *
     * @return {@link Behavior#MERGE_USING_DUAL}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.MERGE_USING_DUAL;
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
        return oracleType(column);
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
        return oracleAddColumn(table, column);
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
        return oracleModifyColumn(table, column);
    }

    /**
     * Returns the keyword used by Oracle to express row limits.
     *
     * @return the keyword {@code FETCH NEXT}
     */
    @Override
    public String getLimitKeyword() {
        return "FETCH NEXT";
    }

    /**
     * Builds a count query for Oracle by wrapping the original SQL as a subquery.
     *
     * @param originalSql the original SQL statement
     * @return the generated count SQL
     */
    @Override
    public String buildCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM (" + originalSql + ")";
    }

    /**
     * Builds paginated SQL using Oracle {@code OFFSET ... FETCH NEXT} syntax.
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
        if (pageable.getOffset() > 0) {
            sql.append(" OFFSET ").append(pageable.getOffset()).append(" ROWS");
        }
        sql.append(" FETCH NEXT ").append(pageable.getPageSize()).append(" ROWS ONLY");
        return sql.toString();
    }

}
