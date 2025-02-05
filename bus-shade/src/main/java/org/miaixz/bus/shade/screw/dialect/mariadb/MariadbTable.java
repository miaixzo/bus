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
 * 表信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class MariadbTable implements Table {

    /**
     * tableCat
     */
    @MappingField(value = "TABLE_CAT")
    private String tableCat;
    /**
     * 表名
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     *
     */
    @MappingField(value = "SELF_REFERENCING_COL_NAME")
    private Object selfReferencingColName;
    /**
     *
     */
    @MappingField(value = "TABLE_CAT")
    private Object tableSchem;
    /**
     *
     */
    @MappingField(value = "TYPE_SCHEM")
    private Object typeSchem;
    /**
     *
     */
    @MappingField(value = "TABLE_CAT")
    private Object typeCat;
    /**
     * 表类型
     */
    @MappingField(value = "TABLE_TYPE")
    private String tableType;
    /**
     * 备注
     */
    @MappingField(value = "REMARKS")
    private String remarks;
    /**
     *
     */
    @MappingField(value = "REF_GENERATION")
    private Object refGeneration;
    /**
     * 类型名称
     */
    @MappingField(value = "TYPE_NAME")
    private Object typeName;

}
