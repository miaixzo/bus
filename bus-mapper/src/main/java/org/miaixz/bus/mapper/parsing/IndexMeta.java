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
package org.miaixz.bus.mapper.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Index metadata shared by entity parsing and database schema snapshots.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@Accessors(fluent = true)
public class IndexMeta {

    /**
     * Equality grammar shared by the entity annotation and PostgreSQL's index predicate metadata.
     */
    private static final Pattern INTEGER_EQUALITY = Pattern.compile(
            "(?:\\(([a-z_][a-z_0-9]*)\\s*=\\s*(-?(?:0|[1-9][0-9]*))\\)|([a-z_][a-z_0-9]*)\\s*=\\s*(-?(?:0|[1-9][0-9]*)))");

    /**
     * Index name.
     */
    private String name;

    /**
     * Whether the index is unique.
     */
    private boolean unique;

    /**
     * Canonical PostgreSQL index predicate, or null for an unfiltered index.
     */
    private String predicate;

    /**
     * Index column names.
     */
    private List<String> columns = new ArrayList<>();

    /**
     * Constructs a new IndexMeta instance.
     */
    public IndexMeta() {
        // No initialization required.
    }

    /**
     * Parses a partial index predicate consisting of a column and an integral equality value.
     *
     * @param filter SQL predicate from entity options or JDBC metadata
     * @return canonical predicate, or null for an unsupported expression
     */
    public static String integerEquality(String filter) {
        var match = INTEGER_EQUALITY.matcher(filter.strip());
        if (!match.matches()) {
            return null;
        }
        return match.group(1) == null ? match.group(3) + " = " + match.group(4)
                : match.group(1) + " = " + match.group(2);
    }

    /**
     * Creates index metadata.
     *
     * @param name    the index name
     * @param unique  whether the index is unique
     * @param columns the index column names
     * @return the index metadata
     */
    public static IndexMeta of(String name, boolean unique, String... columns) {
        return new IndexMeta().name(name).unique(unique).columns(new ArrayList<>(Arrays.asList(columns)));
    }

}
