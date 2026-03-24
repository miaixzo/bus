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
package org.miaixz.bus.shade.screw.dialect.cachedb;

import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents primary key information for a CacheDB table.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class CacheDbPrimaryKey implements PrimaryKey {

    /**
     * The table catalog (may be {@code null}).
     */
    @MappingField(value = "TABLE_CATALOG")
    private String tableCat;
    /**
     * The name of the table.
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     * The schema of the table.
     */
    @MappingField(value = "TABLE_SCHEM")
    private String tableSchem;
    /**
     * The name of the column in the primary key.
     */
    @MappingField(value = "COLUMN_NAME")
    private String columnName;
    /**
     * The sequence number of the column within the primary key (for composite keys).
     */
    @MappingField(value = "KEY_SEQ")
    private String keySeq;
    /**
     * The name of the primary key constraint.
     */
    private String pkName;

}
