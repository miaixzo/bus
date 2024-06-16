/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.screw.dialect.cachedb;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.Table;

/**
 * 表信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class CacheDbTable implements Table {

    /**
     *
     */
    @MappingField(value = "TABLE_CAT")
    private String tableCat;
    /**
     * TABLE_NAME
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     *
     */
    @MappingField(value = "SELF_REFERENCING_COL_NAME")
    private String selfReferencingColName;
    /**
     * TABLE_SCHEM
     */
    @MappingField(value = "TABLE_SCHEM")
    private String tableSchem;
    /**
     *
     */
    @MappingField(value = "TYPE_SCHEM")
    private String typeSchem;
    /**
     *
     */
    @MappingField(value = "TYPE_CAT")
    private Object typeCat;
    /**
     * TABLE_TYPE
     */
    @MappingField(value = "TABLE_TYPE")
    private String tableType;
    /**
     * REMARKS
     */
    @MappingField(value = "REMARKS")
    private String remarks;
    /**
     *
     */
    @MappingField(value = "REF_GENERATION")
    private String refGeneration;
    /**
     *
     */
    @MappingField(value = "TYPE_NAME")
    private String typeName;
}
