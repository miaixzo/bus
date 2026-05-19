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

import org.miaixz.bus.mapper.parsing.IndexMeta;

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
        String normalized = normalizeIdentifier(index.name());
        return indexes.stream().anyMatch(actual -> normalizeIdentifier(actual.name()).equals(normalized));
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
