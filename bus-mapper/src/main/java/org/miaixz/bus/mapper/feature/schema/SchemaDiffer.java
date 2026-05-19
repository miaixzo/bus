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
package org.miaixz.bus.mapper.feature.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Risk;
import org.miaixz.bus.mapper.behavior.SchemaBehavior;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.ForeignKeyMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.PrimaryKeyMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Computes schema differences.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@RequiredArgsConstructor
public class SchemaDiffer {

    /**
     * Schema behavior used to resolve expected database types.
     */
    private final SchemaBehavior operations;

    /**
     * Optional schema configuration used by rename detection.
     */
    private final SchemaConfig config;

    /**
     * Creates a schema differ without additional configuration.
     *
     * @param operations the schema behavior
     */
    public SchemaDiffer(SchemaBehavior operations) {
        this(operations, null);
    }

    /**
     * Computes schema differences between entity table metadata and a database snapshot.
     *
     * @param table    the entity table metadata
     * @param snapshot the database table snapshot
     * @return the computed schema differences
     */
    public List<SchemaDiff> diff(TableMeta table, TableSnapshot snapshot) {
        List<SchemaDiff> diffs = new ArrayList<>();
        if (snapshot == null || !snapshot.exists()) {
            diffs.add(
                    SchemaDiff
                            .of(Behavior.CREATE_TABLE, Risk.SAFE, table, "Table does not exist: " + table.tableName()));
            diffCreateIndexes(table, diffs);
            diffCreateForeignKeys(table, diffs);
            return diffs;
        }
        diffColumns(table, snapshot, diffs);
        diffPrimaryKey(table, snapshot, diffs);
        diffIndexes(table, snapshot, diffs);
        diffForeignKeys(table, snapshot, diffs);
        return diffs;
    }

    /**
     * Adds index creation differences for a missing table.
     *
     * @param table the entity table metadata
     * @param diffs the difference collector
     */
    private void diffCreateIndexes(TableMeta table, List<SchemaDiff> diffs) {
        for (IndexMeta index : expectedIndexes(table)) {
            diffs.add(
                    SchemaDiff.of(
                            index.unique() ? Behavior.CREATE_UNIQUE : Behavior.CREATE_INDEX,
                            Risk.SAFE,
                            table,
                            "Index does not exist: " + index.name()).index(index));
        }
    }

    /**
     * Adds foreign key creation differences for a missing table.
     *
     * @param table the entity table metadata
     * @param diffs the difference collector
     */
    private void diffCreateForeignKeys(TableMeta table, List<SchemaDiff> diffs) {
        for (ForeignKeyMeta foreignKey : table.foreignKeys()) {
            diffs.add(
                    SchemaDiff.of(
                            Behavior.CREATE_FOREIGN_KEY,
                            Risk.SAFE,
                            table,
                            "Foreign key does not exist: " + foreignKey.name()).foreignKey(foreignKey));
        }
    }

    /**
     * Computes column differences.
     *
     * @param table    the entity table metadata
     * @param snapshot the database table snapshot
     * @param diffs    the difference collector
     */
    private void diffColumns(TableMeta table, TableSnapshot snapshot, List<SchemaDiff> diffs) {
        Set<String> expected = new HashSet<>();
        Set<String> renamedActual = new HashSet<>();
        for (ColumnMeta column : table.columns()) {
            expected.add(TableSnapshot.normalizeIdentifier(column.column()));
            ColumnSnapshot actual = snapshot.column(column.column());
            if (actual == null) {
                ColumnSnapshot renameSource = renameSource(table, snapshot, column);
                if (renameSource != null) {
                    renamedActual.add(TableSnapshot.normalizeIdentifier(renameSource.name()));
                    diffs.add(
                            SchemaDiff.of(
                                    Behavior.RENAME_COLUMN,
                                    Risk.DANGEROUS,
                                    table,
                                    "Column rename configured: " + renameSource.name() + " -> " + column.column())
                                    .columnMeta(column).actualColumn(renameSource));
                    continue;
                }
                diffs.add(
                        SchemaDiff
                                .of(Behavior.ADD_COLUMN, Risk.SAFE, table, "Column does not exist: " + column.column())
                                .columnMeta(column));
                continue;
            }
            SqlTypeDescriptor expectedType = operations.resolveType(column);
            SqlTypeDescriptor actualType = actual.type();
            if (!sameType(expectedType, actualType)) {
                diffs.add(
                        SchemaDiff
                                .of(
                                        Behavior.MODIFY_COLUMN_TYPE,
                                        Risk.CAUTION,
                                        table,
                                        "Column type differs: " + column.column())
                                .columnMeta(column).actualColumn(actual));
            } else if (!sameLength(expectedType, actualType)) {
                Risk risk = risk(expectedType.length(), actualType.length());
                diffs.add(
                        SchemaDiff
                                .of(
                                        Behavior.MODIFY_COLUMN_LENGTH,
                                        risk,
                                        table,
                                        "Column length differs: " + column.column())
                                .columnMeta(column).actualColumn(actual));
            } else if (!sameDecimal(expectedType, actualType)) {
                Risk risk = decimalRisk(expectedType, actualType);
                diffs.add(
                        SchemaDiff
                                .of(
                                        Behavior.MODIFY_COLUMN_DECIMAL,
                                        risk,
                                        table,
                                        "Column decimal differs: " + column.column())
                                .columnMeta(column).actualColumn(actual));
            }
            if (column.ddlNullable() != null && actual.nullable() != null
                    && !column.ddlNullable().equals(actual.nullable())) {
                diffs.add(
                        SchemaDiff
                                .of(
                                        Behavior.MODIFY_COLUMN_NULLABLE,
                                        Risk.CAUTION,
                                        table,
                                        "Column nullable differs: " + column.column())
                                .columnMeta(column).actualColumn(actual));
            }
        }
        for (ColumnSnapshot actual : snapshot.columns()) {
            String actualName = TableSnapshot.normalizeIdentifier(actual.name());
            if (!expected.contains(actualName) && !renamedActual.contains(actualName)) {
                diffs.add(
                        SchemaDiff.of(
                                Behavior.DROP_COLUMN,
                                Risk.DANGEROUS,
                                table,
                                "Database column is not mapped: " + actual.name()).actualColumn(actual));
            }
        }
    }

    /**
     * Finds the configured source column for a rename difference.
     *
     * @param table    the entity table metadata
     * @param snapshot the database table snapshot
     * @param column   the target entity column metadata
     * @return the source column snapshot, or {@code null}
     */
    private ColumnSnapshot renameSource(TableMeta table, TableSnapshot snapshot, ColumnMeta column) {
        if (config == null || config.renameMappings() == null || config.renameMappings().isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : config.renameMappings().entrySet()) {
            String source = sourceColumn(entry.getKey(), table);
            String target = entry.getValue();
            if (source != null && target != null && TableSnapshot.normalizeIdentifier(target)
                    .equals(TableSnapshot.normalizeIdentifier(column.column()))) {
                return snapshot.column(source);
            }
        }
        return null;
    }

    /**
     * Resolves a source column name from a rename mapping key.
     *
     * @param mappingKey the rename mapping key
     * @param table      the entity table metadata
     * @return the source column name, or {@code null}
     */
    private String sourceColumn(String mappingKey, TableMeta table) {
        if (mappingKey == null || mappingKey.isBlank()) {
            return null;
        }
        String key = mappingKey.trim();
        int dot = key.lastIndexOf('.');
        if (dot < 0) {
            return key;
        }
        String tableName = key.substring(0, dot);
        if (TableSnapshot.normalizeIdentifier(tableName).equals(TableSnapshot.normalizeIdentifier(table.table()))
                || TableSnapshot.normalizeIdentifier(tableName)
                        .equals(TableSnapshot.normalizeIdentifier(table.tableName()))) {
            return key.substring(dot + 1);
        }
        return null;
    }

    /**
     * Computes index differences.
     *
     * @param table    the entity table metadata
     * @param snapshot the database table snapshot
     * @param diffs    the difference collector
     */
    private void diffIndexes(TableMeta table, TableSnapshot snapshot, List<SchemaDiff> diffs) {
        List<IndexMeta> expected = expectedIndexes(table);
        Set<String> expectedNames = new HashSet<>();
        for (IndexMeta index : expected) {
            expectedNames.add(TableSnapshot.normalizeIdentifier(index.name()));
            IndexMeta actual = snapshot.index(index.name());
            if (actual == null) {
                diffs.add(
                        SchemaDiff.of(
                                index.unique() ? Behavior.CREATE_UNIQUE : Behavior.CREATE_INDEX,
                                Risk.SAFE,
                                table,
                                "Index does not exist: " + index.name()).index(index));
            } else if (!TableSnapshot.sameIndex(actual, index)) {
                diffs.add(
                        SchemaDiff.of(
                                actual.unique() ? Behavior.DROP_UNIQUE : Behavior.DROP_INDEX,
                                Risk.DANGEROUS,
                                table,
                                "Index differs: " + actual.name()).index(actual));
                diffs.add(
                        SchemaDiff.of(
                                index.unique() ? Behavior.CREATE_UNIQUE : Behavior.CREATE_INDEX,
                                Risk.DANGEROUS,
                                table,
                                "Index differs: " + index.name()).index(index));
            }
        }
        for (IndexMeta actual : snapshot.indexes()) {
            if (primaryKeyIndex(table, actual)) {
                continue;
            }
            if (!expectedNames.contains(TableSnapshot.normalizeIdentifier(actual.name()))) {
                diffs.add(
                        SchemaDiff.of(
                                actual.unique() ? Behavior.DROP_UNIQUE : Behavior.DROP_INDEX,
                                Risk.DANGEROUS,
                                table,
                                "Database index is not mapped: " + actual.name()).index(actual));
            }
        }
    }

    /**
     * Computes primary key differences.
     *
     * @param table    the entity table metadata
     * @param snapshot the database table snapshot
     * @param diffs    the difference collector
     */
    private void diffPrimaryKey(TableMeta table, TableSnapshot snapshot, List<SchemaDiff> diffs) {
        PrimaryKeyMeta expected = table.primaryKey();
        PrimaryKeyMeta actual = snapshot.primaryKey();
        if (expected != null && actual == null) {
            diffs.add(
                    SchemaDiff.of(
                            Behavior.CREATE_PRIMARY_KEY,
                            Risk.SAFE,
                            table,
                            "Primary key does not exist: " + expected.name()).primaryKey(expected));
            return;
        }
        if (expected == null && actual != null) {
            diffs.add(
                    SchemaDiff.of(
                            Behavior.DROP_PRIMARY_KEY,
                            Risk.DANGEROUS,
                            table,
                            "Database primary key is not mapped: " + actual.name()).primaryKey(actual));
            return;
        }
        if (expected != null && !TableSnapshot.samePrimaryKey(actual, expected)) {
            diffs.add(
                    SchemaDiff.of(
                            Behavior.DROP_PRIMARY_KEY,
                            Risk.DANGEROUS,
                            table,
                            "Primary key differs: " + actual.name()).primaryKey(actual));
            diffs.add(
                    SchemaDiff.of(
                            Behavior.CREATE_PRIMARY_KEY,
                            Risk.DANGEROUS,
                            table,
                            "Primary key differs: " + expected.name()).primaryKey(expected));
        }
    }

    /**
     * Computes foreign key differences.
     *
     * @param table    the entity table metadata
     * @param snapshot the database table snapshot
     * @param diffs    the difference collector
     */
    private void diffForeignKeys(TableMeta table, TableSnapshot snapshot, List<SchemaDiff> diffs) {
        List<ForeignKeyMeta> expected = table.foreignKeys();
        Set<String> expectedNames = new HashSet<>();
        for (ForeignKeyMeta foreignKey : expected) {
            expectedNames.add(TableSnapshot.normalizeIdentifier(foreignKey.name()));
            ForeignKeyMeta actual = snapshot.foreignKeys().stream()
                    .filter(
                            item -> TableSnapshot.normalizeIdentifier(item.name())
                                    .equals(TableSnapshot.normalizeIdentifier(foreignKey.name())))
                    .findFirst().orElse(null);
            if (actual == null) {
                diffs.add(
                        SchemaDiff.of(
                                Behavior.CREATE_FOREIGN_KEY,
                                Risk.SAFE,
                                table,
                                "Foreign key does not exist: " + foreignKey.name()).foreignKey(foreignKey));
            } else if (!TableSnapshot.sameForeignKey(actual, foreignKey)) {
                diffs.add(
                        SchemaDiff.of(
                                Behavior.DROP_FOREIGN_KEY,
                                Risk.DANGEROUS,
                                table,
                                "Foreign key differs: " + actual.name()).foreignKey(actual));
                diffs.add(
                        SchemaDiff.of(
                                Behavior.CREATE_FOREIGN_KEY,
                                Risk.DANGEROUS,
                                table,
                                "Foreign key differs: " + foreignKey.name()).foreignKey(foreignKey));
            }
        }
        for (ForeignKeyMeta actual : snapshot.foreignKeys()) {
            if (!expectedNames.contains(TableSnapshot.normalizeIdentifier(actual.name()))) {
                diffs.add(
                        SchemaDiff.of(
                                Behavior.DROP_FOREIGN_KEY,
                                Risk.DANGEROUS,
                                table,
                                "Database foreign key is not mapped: " + actual.name()).foreignKey(actual));
            }
        }
    }

    /**
     * Tests whether a database index represents the table primary key.
     *
     * @param table the table metadata
     * @param index the database index metadata
     * @return {@code true} when the index covers the primary key columns
     */
    private boolean primaryKeyIndex(TableMeta table, IndexMeta index) {
        if (index == null || index.columns() == null || index.columns().isEmpty()) {
            return false;
        }
        PrimaryKeyMeta primaryKey = table.primaryKey();
        if (primaryKey == null) {
            return false;
        }
        List<String> primaryKeys = primaryKey.columns().stream().map(TableSnapshot::normalizeIdentifier).toList();
        List<String> indexColumns = index.columns().stream().map(TableSnapshot::normalizeIdentifier).toList();
        return !primaryKeys.isEmpty() && primaryKeys.equals(indexColumns);
    }

    /**
     * Builds expected index definitions from table and column metadata.
     *
     * @param table the entity table metadata
     * @return the expected index definitions
     */
    public List<IndexMeta> expectedIndexes(TableMeta table) {
        List<IndexMeta> indexes = new ArrayList<>();
        for (IndexMeta index : table.indexes()) {
            addIndex(indexes, index);
        }
        for (ColumnMeta column : table.columns()) {
            if (Boolean.TRUE.equals(column.unique())) {
                addIndex(indexes, IndexMeta.of(table.table() + "_" + column.column() + "_uk", true, column.column()));
            }
        }
        indexes.sort((left, right) -> Boolean.compare(right.unique(), left.unique()));
        return indexes;
    }

    /**
     * Adds an index definition when an equivalent definition is not already present.
     *
     * @param indexes the index collector
     * @param index   the index metadata
     */
    private void addIndex(List<IndexMeta> indexes, IndexMeta index) {
        if (index == null || index.columns() == null || index.columns().isEmpty()) {
            return;
        }
        boolean exists = indexes.stream().anyMatch(
                current -> current.unique() == index.unique()
                        && TableSnapshot.sameColumns(current.columns(), index.columns()));
        if (!exists) {
            indexes.add(index);
        }
    }

    /**
     * Tests whether expected and actual SQL type names match.
     *
     * @param expected the expected SQL type descriptor
     * @param actual   the actual SQL type descriptor
     * @return {@code true} when the type names match
     */
    private static boolean sameType(SqlTypeDescriptor expected, SqlTypeDescriptor actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return expected.normalizedTypeName().equals(actual.normalizedTypeName());
    }

    /**
     * Tests whether expected and actual type lengths match.
     *
     * @param expected the expected SQL type descriptor
     * @param actual   the actual SQL type descriptor
     * @return {@code true} when lengths match
     */
    private static boolean sameLength(SqlTypeDescriptor expected, SqlTypeDescriptor actual) {
        return equals(expected.length(), actual.length());
    }

    /**
     * Tests whether expected and actual numeric precision and scale match.
     *
     * @param expected the expected SQL type descriptor
     * @param actual   the actual SQL type descriptor
     * @return {@code true} when precision and scale match
     */
    private static boolean sameDecimal(SqlTypeDescriptor expected, SqlTypeDescriptor actual) {
        return equals(expected.precision(), actual.precision()) && equals(expected.scale(), actual.scale());
    }

    /**
     * Computes risk for a numeric size change.
     *
     * @param expected the expected value
     * @param actual   the actual value
     * @return the schema risk level
     */
    private static Risk risk(Integer expected, Integer actual) {
        if (expected == null || actual == null) {
            return Risk.CAUTION;
        }
        return expected < actual ? Risk.DANGEROUS : Risk.SAFE;
    }

    /**
     * Computes risk for a numeric precision or scale change.
     *
     * @param expected the expected SQL type descriptor
     * @param actual   the actual SQL type descriptor
     * @return the schema risk level
     */
    private static Risk decimalRisk(SqlTypeDescriptor expected, SqlTypeDescriptor actual) {
        if (expected.precision() != null && actual.precision() != null && expected.precision() < actual.precision()) {
            return Risk.DANGEROUS;
        }
        if (expected.scale() != null && actual.scale() != null && expected.scale() < actual.scale()) {
            return Risk.DANGEROUS;
        }
        return Risk.SAFE;
    }

    /**
     * Null-safe equality check.
     *
     * @param left  the left value
     * @param right the right value
     * @return {@code true} when values are equal
     */
    private static boolean equals(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }

}
