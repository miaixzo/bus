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
package org.miaixz.bus.shade.screw.dialect.mariadb;

import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * MariaDB table information.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class MariadbTable implements Table {

    /**
     * Table catalog.
     */
    @MappingField(value = "TABLE_CAT")
    private String tableCat;
    /**
     * Table name.
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     * Self-referencing column name.
     */
    @MappingField(value = "SELF_REFERENCING_COL_NAME")
    private Object selfReferencingColName;
    /**
     * Table schema.
     */
    @MappingField(value = "TABLE_CAT")
    private Object tableSchem;
    /**
     * Type schema.
     */
    @MappingField(value = "TYPE_SCHEM")
    private Object typeSchem;
    /**
     * Type catalog.
     */
    @MappingField(value = "TABLE_CAT")
    private Object typeCat;
    /**
     * Table type.
     */
    @MappingField(value = "TABLE_TYPE")
    private String tableType;
    /**
     * Remarks or comments about the table.
     */
    @MappingField(value = "REMARKS")
    private String remarks;
    /**
     * Reference generation method.
     */
    @MappingField(value = "REF_GENERATION")
    private Object refGeneration;
    /**
     * Type name.
     */
    @MappingField(value = "TYPE_NAME")
    private Object typeName;

}
