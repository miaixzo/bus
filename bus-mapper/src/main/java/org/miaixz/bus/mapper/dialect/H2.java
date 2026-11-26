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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Dialect implementation for H2 databases.
 *
 * <p>
 * H2 is aligned with the framework's {@code MERGE INTO ... KEY (...)} UPSERT branch and supports standard multi-values
 * insert statements.
 * </p>
 *
 * @author Kimi Liu
 */
public class H2 extends AbstractDialect {

    /**
     * Creates the H2 dialect.
     */
    public H2() {
        super("H2", "jdbc:h2:");
    }

    /**
     * Returns the UPSERT family used by H2 in this framework.
     *
     * @return {@link Behavior#MERGE_INTO_KEY}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.MERGE_INTO_KEY;
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
     * Reports the native column default removal supported by this dialect.
     *
     * @return true when an ALTER COLUMN DROP DEFAULT statement can be generated
     */
    @Override
    protected boolean supportsStandardColumnDefault() {
        return true;
    }

    /**
     * Restores a named check constraint from a single catalogue definition in this dialect.
     *
     * @param table      physical table receiving the check
     * @param name       actual constraint name
     * @param definition complete CHECK clause from the database catalogue
     * @return native DDL for the named constraint
     */
    @Override
    public String restoreCheck(TableMeta table, String name, String definition) {
        return standardRestoreCheck(table, name, definition);
    }

    /**
     * Creates an H2 schema if it does not already exist.
     *
     * @param schema physical destination schema
     * @return native schema creation SQL
     */
    @Override
    public String createSchema(String schema) {
        return CREATE_SCHEMA_IF_ABSENT + Symbol.SPACE + quotePhysicalIdentifier(schema, schemaQuote());
    }

    /**
     * Resolves the SQL type descriptor used by this dialect for the supplied mapper column.
     *
     * @param column the mapper column metadata
     * @return the SQL type descriptor for the column
     */
    @Override
    public SqlTypeDescriptor resolveType(ColumnMeta column) {
        return commonType(column, "INTEGER", "TIMESTAMP", "TIMESTAMP WITH TIME ZONE", "BLOB", "CLOB", "DECIMAL");
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
        return "ALTER TABLE " + tableName(table) + " ALTER COLUMN " + columnDefinition(column);
    }

    /**
     * Builds the H2 DDL used to change a column nullable constraint.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated nullable modification SQL
     */
    @Override
    public String modifyColumnNullable(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return alterColumnNullable(table, column);
    }

    /**
     * Builds paginated SQL using standard {@code LIMIT/OFFSET} syntax.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement
     */
    @Override
    public String buildPaginationSql(String originalSql, Pageable pageable) {
        return buildPaginatedSql(originalSql, pageable);
    }

}
