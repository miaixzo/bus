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
import org.miaixz.bus.mapper.feature.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.feature.schema.TableSnapshot;
import org.miaixz.bus.mapper.parsing.*;

/**
 * Schema behavior exposed by a database dialect.
 *
 * @author Kimi Liu
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
     * Reads primary key metadata for a table.
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return the primary key metadata, or {@code null} when the table has no primary key
     * @throws SQLException when metadata access fails
     */
    default PrimaryKeyMeta readPrimaryKey(Connection connection, TableMeta table) throws SQLException {
        throw unsupportedSchema("READ_PRIMARY_KEY_METADATA", table);
    }

    /**
     * Reads foreign key metadata for a table.
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return the foreign key metadata
     * @throws SQLException when metadata access fails
     */
    default List<ForeignKeyMeta> readForeignKeys(Connection connection, TableMeta table) throws SQLException {
        throw unsupportedSchema("READ_FOREIGN_KEY_METADATA", table);
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
     * Builds table comment modification SQL.
     *
     * @param table the table metadata
     * @return the table comment modification SQL
     */
    default String modifyTableComment(TableMeta table) {
        throw unsupportedSchema("MODIFY_TABLE_COMMENT", table);
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
     * Builds column comment modification SQL.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column comment modification SQL
     */
    default String modifyColumnComment(TableMeta table, ColumnMeta column) {
        throw unsupportedSchema("MODIFY_COLUMN_COMMENT", table);
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
     * Builds primary key constraint creation SQL.
     *
     * @param table      the table metadata
     * @param primaryKey the primary key metadata
     * @return the primary key constraint creation SQL
     */
    default String createPrimaryKey(TableMeta table, PrimaryKeyMeta primaryKey) {
        throw unsupportedSchema("CREATE_PRIMARY_KEY", table);
    }

    /**
     * Builds primary key constraint drop SQL.
     *
     * @param table      the table metadata
     * @param primaryKey the primary key metadata
     * @return the primary key constraint drop SQL
     */
    default String dropPrimaryKey(TableMeta table, PrimaryKeyMeta primaryKey) {
        throw unsupportedSchema("DROP_PRIMARY_KEY", table);
    }

    /**
     * Builds foreign key constraint creation SQL.
     *
     * @param table      the table metadata
     * @param foreignKey the foreign key metadata
     * @return the foreign key constraint creation SQL
     */
    default String createForeignKey(TableMeta table, ForeignKeyMeta foreignKey) {
        throw unsupportedSchema("CREATE_FOREIGN_KEY", table);
    }

    /**
     * Builds foreign key constraint drop SQL.
     *
     * @param table      the table metadata
     * @param foreignKey the foreign key metadata
     * @return the foreign key constraint drop SQL
     */
    default String dropForeignKey(TableMeta table, ForeignKeyMeta foreignKey) {
        throw unsupportedSchema("DROP_FOREIGN_KEY", table);
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

    /**
     * Compares a physical metadata identifier with a mapped identifier using this snapshot's connection rules. Customer
     * dialects may override this comparison without changing shared entity metadata.
     *
     * @param snapshot the snapshot supplying database naming and quoting rules
     * @param actual   the physical name returned by JDBC, without mapping delimiters
     * @param mapped   the mapped name, which may contain explicit delimiters
     * @return {@code true} when both names identify the same object
     */
    default boolean sameIdentifier(TableSnapshot snapshot, String actual, String mapped) {
        return snapshot.matches(actual, mapped);
    }

    /**
     * Compares semantic type names while leaving native SQL definitions unchanged. Missing descriptors, missing type
     * names, and opaque native definitions produce {@code UNKNOWN}; customer dialects may refine that result.
     *
     * @param expected the resolved entity type descriptor, or {@code null} when unavailable
     * @param actual   the database type descriptor, or {@code null} when unavailable
     * @return the type-name comparison, independently of capacity and numeric precision
     */
    default SqlTypeDescriptor.Comparison compareType(SqlTypeDescriptor expected, SqlTypeDescriptor actual) {
        if (expected == null || actual == null || expected.typeName() == null || actual.typeName() == null
                || expected.nativeDefinition() != null && !expected.nativeDefinition().isBlank()) {
            return SqlTypeDescriptor.Comparison.UNKNOWN;
        }
        return expected.normalizedTypeName().equals(actual.normalizedTypeName()) ? SqlTypeDescriptor.Comparison.SAME
                : SqlTypeDescriptor.Comparison.DIFFERENT;
    }

    /**
     * Compares capacities without assuming that bytes and characters are interchangeable. Two absent capacities mean no
     * capacity comparison is required; missing values, unknown units, or different units remain {@code UNKNOWN}.
     *
     * @param expected the non-null resolved entity type descriptor
     * @param actual   the non-null database type descriptor
     * @return the capacity comparison after the caller has established compatible type names
     */
    default SqlTypeDescriptor.Comparison compareLength(SqlTypeDescriptor expected, SqlTypeDescriptor actual) {
        if (expected.length() == null && actual.length() == null) {
            return SqlTypeDescriptor.Comparison.SAME;
        }
        if (expected.length() == null || actual.length() == null
                || expected.lengthUnit() == SqlTypeDescriptor.LengthUnit.UNKNOWN
                || actual.lengthUnit() == SqlTypeDescriptor.LengthUnit.UNKNOWN) {
            return SqlTypeDescriptor.Comparison.UNKNOWN;
        }
        if (expected.lengthUnit() != actual.lengthUnit()) {
            return SqlTypeDescriptor.Comparison.UNKNOWN;
        }
        return expected.length().equals(actual.length()) ? SqlTypeDescriptor.Comparison.SAME
                : SqlTypeDescriptor.Comparison.DIFFERENT;
    }

    /**
     * Compares numeric precision and scale while preserving unavailable JDBC values. Two absent precisions mean no
     * decimal comparison is required; a required but missing precision or scale remains {@code UNKNOWN}.
     *
     * @param expected the non-null resolved entity type descriptor
     * @param actual   the non-null database type descriptor
     * @return the numeric comparison after the caller has established compatible type names
     */
    default SqlTypeDescriptor.Comparison compareDecimal(SqlTypeDescriptor expected, SqlTypeDescriptor actual) {
        if (expected.precision() == null && actual.precision() == null) {
            return SqlTypeDescriptor.Comparison.SAME;
        }
        if (expected.precision() == null || actual.precision() == null || expected.scale() == null
                || actual.scale() == null) {
            return SqlTypeDescriptor.Comparison.UNKNOWN;
        }
        return expected.precision().equals(actual.precision()) && expected.scale().equals(actual.scale())
                ? SqlTypeDescriptor.Comparison.SAME
                : SqlTypeDescriptor.Comparison.DIFFERENT;
    }

    /**
     * Indicates whether CREATE TABLE already includes a new table's primary key. The differ uses this capability to
     * avoid generating a second ADD PRIMARY KEY operation; configuration still controls whether creation is allowed.
     *
     * @return {@code true} when this dialect renders the primary key inline; {@code false} by default
     */
    default boolean inlinePrimaryKey() {
        return false;
    }

}
