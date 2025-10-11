/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
