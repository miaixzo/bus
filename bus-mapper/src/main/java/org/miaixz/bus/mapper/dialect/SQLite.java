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
import org.miaixz.bus.mapper.Charter.Modify;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.ForeignKeyMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Dialect implementation for SQLite databases.
 *
 * <p>
 * This dialect uses standard {@code LIMIT/OFFSET} pagination and the SQLite {@code INSERT OR REPLACE} UPSERT family.
 * </p>
 *
 * @author Kimi Liu
 */
public class SQLite extends AbstractDialect {

    /**
     * Creates the SQLite dialect.
     */
    public SQLite() {
        super("SQLite", "jdbc:sqlite:");
    }

    /**
     * Returns the UPSERT family used by SQLite.
     *
     * @return {@link Behavior#INSERT_OR_REPLACE}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.INSERT_OR_REPLACE;
    }

    /**
     * Returns the database behavior set advertised by this dialect.
     *
     * @return the supported behavior set
     */
    @Override
    public EnumSet<Behavior> types() {
        EnumSet<Behavior> types = schemaTypes(getUpsertType());
        types.removeAll(
                EnumSet.of(
                        Behavior.CREATE_FOREIGN_KEY,
                        Behavior.DROP_FOREIGN_KEY,
                        Behavior.MODIFY_TABLE_COMMENT,
                        Behavior.MODIFY_COLUMN_COMMENT,
                        Behavior.MODIFY_COLUMN_TYPE,
                        Behavior.MODIFY_COLUMN_LENGTH,
                        Behavior.MODIFY_COLUMN_DECIMAL,
                        Behavior.MODIFY_COLUMN_NULLABLE,
                        Behavior.CREATE_PRIMARY_KEY,
                        Behavior.DROP_PRIMARY_KEY));
        return types;
    }

    /**
     * Indicates that a new table's primary key is emitted inside CREATE TABLE, because SQLite cannot add it to an
     * existing table with the generic ALTER TABLE operation.
     *
     * @return true so the differ omits a duplicate post-creation primary-key operation
     */
    @Override
    public boolean inlinePrimaryKey() {
        return true;
    }

    /**
     * Builds the column definitions and appends the mapped primary key inside the new table definition. This supports
     * table creation only; existing-table primary-key changes remain unsupported.
     *
     * @param table the mapped table and its ordered primary-key columns
     * @return CREATE TABLE SQL containing the primary key when one is mapped
     */
    @Override
    public String createTable(TableMeta table) {
        String base = super.createTable(table);
        if (table.primaryKey() == null)
            return base;
        return base.substring(0, base.length() - 1) + ", PRIMARY KEY (" + columnList(table.primaryKey().columns())
                + "))";
    }

    /**
     * Resolves the SQL type descriptor used by this dialect for the supplied mapper column.
     *
     * @param column the mapper column metadata
     * @return the SQL type descriptor for the column
     */
    @Override
    public SqlTypeDescriptor resolveType(ColumnMeta column) {
        return commonType(column, "INTEGER", "TEXT", "TEXT", "BLOB", "TEXT", "NUMERIC");
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
        return modifyColumn(table, column, Modify.ALTER_COLUMN_TYPE);
    }

    /**
     * Rejects foreign key creation on existing SQLite tables.
     *
     * @param table      the mapper table metadata
     * @param foreignKey the mapper foreign key metadata
     * @return never returns normally
     */
    @Override
    public String createForeignKey(TableMeta table, ForeignKeyMeta foreignKey) {
        throw unsupportedSchema("CREATE_FOREIGN_KEY", table);
    }

    /**
     * Rejects foreign key dropping on existing SQLite tables.
     *
     * @param table      the mapper table metadata
     * @param foreignKey the mapper foreign key metadata
     * @return never returns normally
     */
    @Override
    public String dropForeignKey(TableMeta table, ForeignKeyMeta foreignKey) {
        throw unsupportedSchema("DROP_FOREIGN_KEY", table);
    }

    /**
     * Builds paginated SQL using SQLite {@code LIMIT/OFFSET} syntax.
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
