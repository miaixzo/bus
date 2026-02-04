/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.screw.dialect.mysql;

import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;

import lombok.Getter;
import lombok.Setter;

/**
 * Table primary key metadata for MySQL.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class MySqlPrimaryKey implements PrimaryKey {

    /**
     * Table catalog (may be {@code null}).
     */
    @MappingField(value = "TABLE_CAT")
    private String tableCat;
    /**
     * Table name.
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     * Primary key name (may be {@code null}).
     */
    @MappingField(value = "PK_NAME")
    private String pkName;
    /**
     * Table schema (may be {@code null}).
     */
    @MappingField(value = "TABLE_SCHEM")
    private String tableSchem;
    /**
     * Column name.
     */
    @MappingField(value = "COLUMN_NAME")
    private String columnName;
    /**
     * Sequence number within the primary key (1-based).
     */
    @MappingField(value = "KEY_SEQ")
    private String keySeq;

}
