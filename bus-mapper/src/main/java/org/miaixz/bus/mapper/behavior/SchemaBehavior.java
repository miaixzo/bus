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
package org.miaixz.bus.mapper.behavior;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.feature.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.feature.schema.TableSnapshot;

/**
 * Schema behavior exposed by a database dialect.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SchemaBehavior {

    /**
     * Tests whether a table exists.
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return {@code true} when the table exists
     * @throws SQLException when metadata access fails
     */
    default boolean existsTable(Connection connection, TableMeta table) throws SQLException {
        throw unsupportedSchema("READ_TABLE_METADATA", table);
    }

    /**
     * Reads a table snapshot.
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return the table snapshot
     * @throws SQLException when metadata access fails
     */
    default TableSnapshot readTable(Connection connection, TableMeta table) throws SQLException {
        throw unsupportedSchema("READ_TABLE_METADATA", table);
    }

    /**
     * Reads column snapshots for a table.
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return the column snapshots
     * @throws SQLException when metadata access fails
     */
    default List<ColumnSnapshot> readColumns(Connection connection, TableMeta table) throws SQLException {
        throw unsupportedSchema("READ_COLUMN_METADATA", table);
    }

    /**
     * Reads index metadata for a table.
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return the index metadata
     * @throws SQLException when metadata access fails
     */
    default List<IndexMeta> readIndexes(Connection connection, TableMeta table) throws SQLException {
        throw unsupportedSchema("READ_INDEX_METADATA", table);
    }

    /**
     * Acquires a database-native temporary lock for schema initialization.
     *
     * <p>
     * The default implementation returns a no-op lock. Dialects with database-native temporary lock support may
     * override this method. Implementations must not create persistent version or migration tables.
     * </p>
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return the acquired lock handle
     * @throws SQLException when lock acquisition fails
     */
    default AutoCloseable acquireSchemaInitializationLock(Connection connection, TableMeta table) throws SQLException {
        return () -> {
        };
    }

    /**
     * Resolves a column SQL type.
     *
     * @param column the column metadata
     * @return the SQL type descriptor
     */
    default SqlTypeDescriptor resolveType(ColumnMeta column) {
        throw unsupportedSchema("RESOLVE_TYPE", column == null ? null : column.tableMeta());
    }

    /**
     * Builds table creation SQL.
     *
     * @param table the table metadata
     * @return the table creation SQL
     */
    default String createTable(TableMeta table) {
        throw unsupportedSchema("CREATE_TABLE", table);
    }

    /**
     * Builds column add SQL.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column add SQL
     */
    default String addColumn(TableMeta table, ColumnMeta column) {
        throw unsupportedSchema("ADD_COLUMN", table);
    }

    /**
     * Builds column type modification SQL.
     *
     * @param table        the table metadata
     * @param column       the column metadata
     * @param actualColumn the database column snapshot
     * @return the column type modification SQL
     */
    default String modifyColumnType(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        throw unsupportedSchema("MODIFY_COLUMN_TYPE", table);
    }

    /**
     * Builds column length modification SQL.
     *
     * @param table        the table metadata
     * @param column       the column metadata
     * @param actualColumn the database column snapshot
     * @return the column length modification SQL
     */
    default String modifyColumnLength(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        throw unsupportedSchema("MODIFY_COLUMN_LENGTH", table);
    }

    /**
     * Builds column decimal modification SQL.
     *
     * @param table        the table metadata
     * @param column       the column metadata
     * @param actualColumn the database column snapshot
     * @return the column decimal modification SQL
     */
    default String modifyColumnDecimal(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        throw unsupportedSchema("MODIFY_COLUMN_DECIMAL", table);
    }

    /**
     * Builds column nullable modification SQL.
     *
     * @param table        the table metadata
     * @param column       the column metadata
     * @param actualColumn the database column snapshot
     * @return the column nullable modification SQL
     */
    default String modifyColumnNullable(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        throw unsupportedSchema("MODIFY_COLUMN_NULLABLE", table);
    }

    /**
     * Builds column rename SQL.
     *
     * @param table     the table metadata
     * @param oldColumn the existing database column name
     * @param column    the target column metadata
     * @return the column rename SQL
     */
    default String renameColumn(TableMeta table, String oldColumn, ColumnMeta column) {
        throw unsupportedSchema("RENAME_COLUMN", table);
    }

    /**
     * Builds column drop SQL.
     *
     * @param table  the table metadata
     * @param column the database column snapshot
     * @return the column drop SQL
     */
    default String dropColumn(TableMeta table, ColumnSnapshot column) {
        throw unsupportedSchema("DROP_COLUMN", table);
    }

    /**
     * Builds index creation SQL.
     *
     * @param table the table metadata
     * @param index the index metadata
     * @return the index creation SQL
     */
    default String createIndex(TableMeta table, IndexMeta index) {
        throw unsupportedSchema("CREATE_INDEX", table);
    }

    /**
     * Builds index drop SQL.
     *
     * @param table the table metadata
     * @param index the index metadata
     * @return the index drop SQL
     */
    default String dropIndex(TableMeta table, IndexMeta index) {
        throw unsupportedSchema("DROP_INDEX", table);
    }

    /**
     * Builds unique constraint creation SQL.
     *
     * @param table the table metadata
     * @param index the index metadata
     * @return the unique constraint creation SQL
     */
    default String createUnique(TableMeta table, IndexMeta index) {
        throw unsupportedSchema("CREATE_UNIQUE", table);
    }

    /**
     * Builds unique constraint drop SQL.
     *
     * @param table the table metadata
     * @param index the index metadata
     * @return the unique constraint drop SQL
     */
    default String dropUnique(TableMeta table, IndexMeta index) {
        throw unsupportedSchema("DROP_UNIQUE", table);
    }

    /**
     * Creates the standard unsupported schema operation exception.
     *
     * @param operation the schema operation name
     * @param table     the table metadata
     * @return the unsupported operation exception
     */
    default UnsupportedOperationException unsupportedSchema(String operation, TableMeta table) {
        String database = this instanceof Dialect dialect ? dialect.getDatabase() : getClass().getName();
        return new UnsupportedOperationException("Schema operation is unsupported: database=" + database
                + ", operation=" + operation + ", table=" + (table == null ? null : table.tableName()));
    }

}
