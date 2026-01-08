/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.screw.dialect.mysql;

import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * Table metadata for MySQL.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class MySqlTable implements Table {

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
     * Name of the designated "identifier" column of a typed table (may be {@code null}).
     */
    @MappingField(value = "SELF_REFERENCING_COL_NAME")
    private String selfReferencingColName;
    /**
     * Table schema (may be {@code null}).
     */
    @MappingField(value = "TABLE_SCHEM")
    private String tableSchem;
    /**
     * Type schema (may be {@code null}).
     */
    @MappingField(value = "TYPE_SCHEM")
    private String typeSchem;
    /**
     * Type catalog (may be {@code null}).
     */
    @MappingField(value = "TABLE_CAT")
    private Object typeCat;
    /**
     * Table type (e.g., "TABLE", "VIEW", "SYSTEM TABLE").
     */
    @MappingField(value = "TABLE_TYPE")
    private String tableType;
    /**
     * Explanatory comment on the table.
     */
    @MappingField(value = "REMARKS")
    private String remarks;
    /**
     * Specifies how values in SELF_REFERENCING_COL_NAME are created (e.g., "SYSTEM", "USER", "DERIVED").
     */
    @MappingField(value = "REF_GENERATION")
    private String refGeneration;
    /**
     * Typed table type name (may be {@code null}).
     */
    @MappingField(value = "TYPE_NAME")
    private String typeName;

}
