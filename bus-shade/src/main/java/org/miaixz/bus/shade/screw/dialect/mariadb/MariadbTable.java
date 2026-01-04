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
package org.miaixz.bus.shade.screw.dialect.mariadb;

import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * MariaDB table information.
 *
 * @author Kimi Liu
 * @since Java 17+
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
