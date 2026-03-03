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
package org.miaixz.bus.shade.screw.dialect.postgresql;

import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents table information for a PostgreSQL database.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class PostgreSqlTable implements Table {

    /**
     * Specifies how values in SELF_REFERENCING_COL_NAME are created.
     */
    @MappingField(value = "ref_generation")
    private String refGeneration;
    /**
     * The type name of the typed table (may be {@code null}).
     */
    @MappingField(value = "type_name")
    private String typeName;
    /**
     * The schema of the typed table (may be {@code null}).
     */
    @MappingField(value = "type_schem")
    private String typeSchem;
    /**
     * The schema of the table.
     */
    @MappingField(value = "table_schem")
    private String tableSchem;
    /**
     * The catalog of the typed table (may be {@code null}).
     */
    @MappingField(value = "type_cat")
    private String typeCat;
    /**
     * The table catalog (may be {@code null}).
     */
    @MappingField(value = "table_cat")
    private Object tableCat;
    /**
     * The name of the table.
     */
    @MappingField(value = "table_name")
    private String tableName;
    /**
     * The name of the designated "identifier" column of a typed table (may be {@code null}).
     */
    @MappingField(value = "self_referencing_col_name")
    private String selfReferencingColName;
    /**
     * The explanatory comment on the table.
     */
    @MappingField(value = "remarks")
    private String remarks;
    /**
     * The type of the table (e.g., "TABLE", "VIEW", etc.).
     */
    @MappingField(value = "table_type")
    private String tableType;

}
