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
package org.miaixz.bus.mapper.support.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Risk;
import org.miaixz.bus.mapper.behavior.SchemaBehavior;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

import lombok.RequiredArgsConstructor;

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
            return diffs;
        }
        diffColumns(table, snapshot, diffs);
        diffIndexes(table, snapshot, diffs);
        return diffs;
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
            if (!snapshot.hasIndex(index)) {
                diffs.add(
                        SchemaDiff.of(
                                index.unique() ? Behavior.CREATE_UNIQUE : Behavior.CREATE_INDEX,
                                Risk.SAFE,
                                table,
                                "Index does not exist: " + index.name()).index(index));
            }
        }
        for (IndexMeta actual : snapshot.indexes()) {
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
     * Builds expected index definitions from table and column metadata.
     *
     * @param table the entity table metadata
     * @return the expected index definitions
     */
    public List<IndexMeta> expectedIndexes(TableMeta table) {
        List<IndexMeta> indexes = new ArrayList<>(table.indexes());
        for (ColumnMeta column : table.columns()) {
            if (Boolean.TRUE.equals(column.unique())) {
                indexes.add(IndexMeta.of(table.table() + "_" + column.column() + "_uk", true, column.column()));
            }
        }
        return indexes;
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
