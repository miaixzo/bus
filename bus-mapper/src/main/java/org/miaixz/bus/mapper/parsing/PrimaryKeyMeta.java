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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Primary key metadata shared by entity parsing, schema snapshots, and dialect DDL generation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class PrimaryKeyMeta {

    /**
     * Constructs a new PrimaryKeyMeta instance.
     */
    public PrimaryKeyMeta() {
        // No initialization required.
    }

    /**
     * Primary key constraint name.
     */
    private String name;

    /**
     * Primary key column names.
     */
    private List<String> columns = new ArrayList<>();

    /**
     * Creates primary key metadata.
     *
     * @param name    the primary key name
     * @param columns the primary key column names
     * @return the primary key metadata
     */
    public static PrimaryKeyMeta of(String name, List<String> columns) {
        return new PrimaryKeyMeta().name(name).columns(columns == null ? new ArrayList<>() : new ArrayList<>(columns));
    }

    /**
     * Creates primary key metadata from array values.
     *
     * @param name    the primary key name
     * @param columns the primary key column names
     * @return the primary key metadata
     */
    public static PrimaryKeyMeta of(String name, String... columns) {
        return of(name, columns == null ? List.of() : Arrays.asList(columns));
    }

}
