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
import java.util.Objects;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.feature.identifier.IdentifierValidator;
import org.miaixz.bus.mapper.parsing.ForeignKeyMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.PrimaryKeyMeta;

/**
 * Database table snapshot.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@Accessors(fluent = true)
public class TableSnapshot {

    /**
     * Database table name.
     */
    private String name;

    /**
     * Physical owning schema used to resolve unqualified references; null for catalog-only databases.
     */
    private String schema;

    /**
     * Physical owning catalog used to resolve unqualified references; null when the driver has no catalog scope.
     */
    private String catalog;

    /**
     * Database table comment.
     */
    private String comment;

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
     * Connection-specific quote and case rules; null for legacy or custom snapshots without captured JDBC rules.
     */
    private IdentifierValidator.MetadataRules identifierRules;

    /**
     * Metadata limitations that prevent complete verification and must not be treated as confirmed equality.
     */
    private List<String> issues = new ArrayList<>();

    /**
     * Physical comparison keys of indexes whose full definition cannot be represented; excluded from automatic
     * replacement.
     */
    private Set<String> incompleteIndexes = new HashSet<>();

    /**
     * Constructs a new TableSnapshot instance.
     */
    public TableSnapshot() {
        // No initialization required.
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
                && actual.unique() == expected.unique() && Objects.equals(actual.predicate(), expected.predicate())
                && sameColumns(actual.columns(), expected.columns());
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
     * Tests exact normalized table names without inferring an owning namespace or accepting suffix matches.
     *
     * @param actual   the database table name
     * @param expected the expected table name
     * @return {@code true} when the table names match
     */
    public static boolean sameTable(String actual, String expected) {
        String normalizedActual = normalizeIdentifier(actual);
        String normalizedExpected = normalizeIdentifier(expected);
        return normalizedActual.equals(normalizedExpected);
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
     * Applies the compatibility normalization used when no JDBC naming context was captured. Unquoted names are
     * lowercased; a complete outer delimiter is removed while preserving the quoted content's case.
     *
     * @param value the identifier value
     * @return the normalized identifier
     */
    public static String normalizeIdentifier(String value) {
        if (value == null)
            return Normal.EMPTY;
        String plain = IdentifierValidator.unquoteIdentifier(value);
        return plain.equals(value.trim()) ? StringKit.toLowerCase(plain) : plain;
    }

    /**
     * Finds a column snapshot by name.
     *
     * @param name the column name
     * @return the matched column snapshot, or {@code null}
     */
    public ColumnSnapshot column(String name) {
        return columns.stream().filter(column -> matches(column.name(), name)).findFirst().orElse(null);
    }

    /**
     * Finds an index snapshot by name.
     *
     * @param name the index name
     * @return the matched index metadata, or {@code null}
     */
    public IndexMeta index(String name) {
        return indexes.stream().filter(index -> matches(index.name(), name)).findFirst().orElse(null);
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
     * Returns the comparison key of a mapped component using captured rules or the legacy normalization fallback.
     *
     * @param mapped the mapped component, optionally quoted
     * @return its physical comparison key
     */
    public String key(String mapped) {
        return identifierRules == null ? normalizeIdentifier(mapped) : identifierRules.key(mapped);
    }

    /**
     * Returns the comparison key of an already physical component without repeating mapping-side folding.
     *
     * @param actual the physical JDBC component
     * @return its physical comparison key
     */
    public String actualKey(String actual) {
        return identifierRules == null ? normalizeIdentifier(actual) : identifierRules.actualKey(actual);
    }

    /**
     * Compares a physical component with its mapped counterpart in this snapshot's naming context.
     *
     * @param actual the physical JDBC component
     * @param mapped the mapped component, optionally quoted
     * @return true when both components match under the captured rules or legacy fallback
     */
    public boolean matches(String actual, String mapped) {
        return identifierRules == null ? normalizeIdentifier(actual).equals(normalizeIdentifier(mapped))
                : identifierRules.matches(actual, mapped);
    }

    /**
     * Compares ordered physical and mapped column lists; column order is significant for keys and indexes.
     *
     * @param actual   the ordered physical column names
     * @param expected the ordered mapped column names
     * @return true when both non-null lists have the same size and matching components
     */
    public boolean matchesColumns(List<String> actual, List<String> expected) {
        if (actual == null || expected == null || actual.size() != expected.size())
            return false;
        for (int i = 0; i < actual.size(); i++)
            if (!matches(actual.get(i), expected.get(i)))
                return false;
        return true;
    }

    /**
     * Compares an index's name, uniqueness, predicate and ordered columns using this snapshot's naming rules. The
     * caller must separately reject indexes listed as incomplete.
     *
     * @param actual   the observed index
     * @param expected the mapped index
     * @return true when both non-null definitions match in the represented dimensions
     */
    public boolean matchesIndex(IndexMeta actual, IndexMeta expected) {
        return actual != null && expected != null && matches(actual.name(), expected.name())
                && actual.unique() == expected.unique() && Objects.equals(actual.predicate(), expected.predicate())
                && matchesColumns(actual.columns(), expected.columns());
    }

    /**
     * Compares table identities after resolving omitted qualifiers against this snapshot's owning namespace. Without
     * captured rules, comparison uses the exact legacy normalized name rather than a suffix match.
     *
     * @param actual   the observed qualified table name, retaining delimiters around physical components
     * @param expected the mapped table reference
     * @return true when the resolved catalog, schema and table identities agree
     */
    public boolean matchesTable(String actual, String expected) {
        if (identifierRules == null)
            return sameTable(actual, expected);
        return qualifiedKey(actual).equals(qualifiedKey(expected));
    }

    /**
     * Resolves one-, two-, or three-component references into catalog/schema/table comparison keys. Two components mean
     * catalog/table when this snapshot has no schema; otherwise they mean schema/table.
     *
     * @param name the qualified or unqualified reference
     * @return three comparison components with absent qualifiers represented by empty strings
     */
    private List<String> qualifiedKey(String name) {
        List<String> parts = identifierRules.parts(name).stream().map(identifierRules::key).toList();
        String c = catalog == null ? Normal.EMPTY : identifierRules.actualKey(catalog);
        String s = schema == null ? Normal.EMPTY : identifierRules.actualKey(schema);
        if (parts.size() == 3)
            return parts;
        if (parts.size() == 2)
            return schema == null ? List.of(parts.getFirst(), Normal.EMPTY, parts.getLast())
                    : List.of(c, parts.getFirst(), parts.getLast());
        return List.of(c, s, parts.getFirst());
    }

}
