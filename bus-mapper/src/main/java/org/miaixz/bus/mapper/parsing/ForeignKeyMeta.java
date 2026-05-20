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
 * Foreign key metadata shared by entity parsing, schema snapshots, and dialect DDL generation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class ForeignKeyMeta {

    /**
     * Constructs a new ForeignKeyMeta instance.
     */
    public ForeignKeyMeta() {
        // No initialization required.
    }

    /**
     * Foreign key constraint name.
     */
    private String name;

    /**
     * Local column names participating in the foreign key.
     */
    private List<String> columns = new ArrayList<>();

    /**
     * Referenced table name.
     */
    private String referencedTable;

    /**
     * Referenced column names.
     */
    private List<String> referencedColumns = new ArrayList<>();

    /**
     * Optional ON DELETE action.
     */
    private String onDelete;

    /**
     * Optional ON UPDATE action.
     */
    private String onUpdate;

    /**
     * Creates foreign key metadata.
     *
     * @param name              the foreign key name
     * @param referencedTable   the referenced table name
     * @param columns           the local column names
     * @param referencedColumns the referenced column names
     * @return the foreign key metadata
     */
    public static ForeignKeyMeta of(
            String name,
            String referencedTable,
            List<String> columns,
            List<String> referencedColumns) {
        return new ForeignKeyMeta().name(name).referencedTable(referencedTable)
                .columns(columns == null ? new ArrayList<>() : new ArrayList<>(columns))
                .referencedColumns(referencedColumns == null ? new ArrayList<>() : new ArrayList<>(referencedColumns));
    }

    /**
     * Creates foreign key metadata from array values.
     *
     * @param name              the foreign key name
     * @param referencedTable   the referenced table name
     * @param columns           the local column names
     * @param referencedColumns the referenced column names
     * @return the foreign key metadata
     */
    public static ForeignKeyMeta of(String name, String referencedTable, String[] columns, String[] referencedColumns) {
        return of(
                name,
                referencedTable,
                columns == null ? List.of() : Arrays.asList(columns),
                referencedColumns == null ? List.of() : Arrays.asList(referencedColumns));
    }

}
