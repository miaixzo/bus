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
import org.miaixz.bus.shade.screw.metadata.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents table information for a CacheDB database.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class CacheDbTable implements Table {

    /**
     * The table catalog (may be {@code null}).
     */
    @MappingField(value = "TABLE_CAT")
    private String tableCat;
    /**
     * The name of the table.
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     * The name of the designated "identifier" column of a typed table (may be {@code null}).
     */
    @MappingField(value = "SELF_REFERENCING_COL_NAME")
    private String selfReferencingColName;
    /**
     * The schema of the table.
     */
    @MappingField(value = "TABLE_SCHEM")
    private String tableSchem;
    /**
     * The schema of the typed table (may be {@code null}).
     */
    @MappingField(value = "TYPE_SCHEM")
    private String typeSchem;
    /**
     * The catalog of the typed table (may be {@code null}).
     */
    @MappingField(value = "TYPE_CAT")
    private Object typeCat;
    /**
     * The type of the table (e.g., "TABLE", "VIEW", etc.).
     */
    @MappingField(value = "TABLE_TYPE")
    private String tableType;
    /**
     * The explanatory comment on the table.
     */
    @MappingField(value = "REMARKS")
    private String remarks;
    /**
     * Specifies how values in SELF_REFERENCING_COL_NAME are created.
     */
    @MappingField(value = "REF_GENERATION")
    private String refGeneration;
    /**
     * The type name of the typed table (may be {@code null}).
     */
    @MappingField(value = "TYPE_NAME")
    private String typeName;

}
