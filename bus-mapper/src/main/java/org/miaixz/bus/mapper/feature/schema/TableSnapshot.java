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
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.miaixz.bus.mapper.parsing.ForeignKeyMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.PrimaryKeyMeta;

/**
 * Database table snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class TableSnapshot {

    /**
     * Constructs a new TableSnapshot instance.
     */
    public TableSnapshot() {
        // No initialization required.
    }

    /**
     * Database table name.
     */
    private String name;

    /**
     * Whether the table exists in the database.
     */
    private boolean exists;

    /**
     * Database column snapshots.
     */
    private List<ColumnSnapshot> columns = new ArrayList<>();

    /**
     * Database index snapshots.
     */
    private List<IndexMeta> indexes = new ArrayList<>();

    /**
     * Database primary key snapshot.
     */
    private PrimaryKeyMeta primaryKey;

    /**
     * Database foreign key snapshots.
     */
    private List<ForeignKeyMeta> foreignKeys = new ArrayList<>();

    /**
     * Finds a column snapshot by name.
     *
     * @param name the column name
     * @return the matched column snapshot, or {@code null}
     */
    public ColumnSnapshot column(String name) {
        String normalized = normalizeIdentifier(name);
        return columns.stream().filter(column -> normalizeIdentifier(column.name()).equals(normalized)).findFirst()
                .orElse(null);
    }

    /**
     * Finds an index snapshot by name.
     *
     * @param name the index name
     * @return the matched index metadata, or {@code null}
     */
    public IndexMeta index(String name) {
        String normalized = normalizeIdentifier(name);
        return indexes.stream().filter(index -> normalizeIdentifier(index.name()).equals(normalized)).findFirst()
                .orElse(null);
    }

    /**
     * Tests whether the table has an index matching the expected index name.
     *
     * @param index the expected index metadata
     * @return {@code true} when the index exists
     */
    public boolean hasIndex(IndexMeta index) {
        return indexes.stream().anyMatch(actual -> sameIndex(actual, index));
    }

    /**
     * Tests whether the table has a primary key matching the expected metadata.
     *
     * @param expected the expected primary key metadata
     * @return {@code true} when the primary key matches
     */
    public boolean hasPrimaryKey(PrimaryKeyMeta expected) {
        return samePrimaryKey(this.primaryKey, expected);
    }

    /**
     * Tests whether the table has a foreign key matching the expected metadata.
     *
     * @param expected the expected foreign key metadata
     * @return {@code true} when the foreign key matches
     */
    public boolean hasForeignKey(ForeignKeyMeta expected) {
        return foreignKeys.stream().anyMatch(actual -> sameForeignKey(actual, expected));
    }

    /**
     * Tests whether two index definitions match.
     *
     * @param actual   the database index metadata
     * @param expected the expected index metadata
     * @return {@code true} when the definitions match
     */
    public static boolean sameIndex(IndexMeta actual, IndexMeta expected) {
        if (actual == null || expected == null) {
            return false;
        }
        return normalizeIdentifier(actual.name()).equals(normalizeIdentifier(expected.name()))
                && actual.unique() == expected.unique() && sameColumns(actual.columns(), expected.columns());
    }

    /**
     * Tests whether two primary key definitions match.
     *
     * @param actual   the database primary key metadata
     * @param expected the expected primary key metadata
     * @return {@code true} when the definitions match
     */
    public static boolean samePrimaryKey(PrimaryKeyMeta actual, PrimaryKeyMeta expected) {
        if (actual == null || expected == null) {
            return false;
        }
        return sameColumns(actual.columns(), expected.columns());
    }

    /**
     * Tests whether two foreign key definitions match.
     *
     * @param actual   the database foreign key metadata
     * @param expected the expected foreign key metadata
     * @return {@code true} when the definitions match
     */
    public static boolean sameForeignKey(ForeignKeyMeta actual, ForeignKeyMeta expected) {
        if (actual == null || expected == null) {
            return false;
        }
        return normalizeIdentifier(actual.name()).equals(normalizeIdentifier(expected.name()))
                && sameTable(actual.referencedTable(), expected.referencedTable())
                && sameColumns(actual.columns(), expected.columns())
                && sameColumns(actual.referencedColumns(), expected.referencedColumns());
    }

    /**
     * Tests whether two table names match after identifier normalization.
     *
     * @param actual   the database table name
     * @param expected the expected table name
     * @return {@code true} when the table names match
     */
    public static boolean sameTable(String actual, String expected) {
        String normalizedActual = normalizeIdentifier(actual);
        String normalizedExpected = normalizeIdentifier(expected);
        return normalizedActual.equals(normalizedExpected) || normalizedActual.endsWith("." + normalizedExpected)
                || normalizedExpected.endsWith("." + normalizedActual);
    }

    /**
     * Tests whether two ordered column lists match after identifier normalization.
     *
     * @param actual   the database column names
     * @param expected the expected column names
     * @return {@code true} when the lists match
     */
    public static boolean sameColumns(List<String> actual, List<String> expected) {
        if (actual == null || expected == null || actual.size() != expected.size()) {
            return false;
        }
        for (int i = 0; i < actual.size(); i++) {
            if (!normalizeIdentifier(actual.get(i)).equals(normalizeIdentifier(expected.get(i)))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Normalizes an identifier for case-insensitive snapshot matching.
     *
     * @param value the identifier value
     * @return the normalized identifier
     */
    public static String normalizeIdentifier(String value) {
        return value == null ? "" : value.replace("`", "").replace("\"", "").toLowerCase(Locale.ROOT);
    }

}
