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
import org.miaixz.bus.shade.screw.metadata.Column;

/**
 * 表字段信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class CacheDbColumn implements Column {

    /**
     *
     */
    @MappingField(value = "SCOPE_TABLE")
    private String scopeTable;
    /**
     *
     */
    @MappingField(value = "TABLE_CAT")
    private String tableCat;
    /**
     * BUFFER_LENGTH
     */
    @MappingField(value = "BUFFER_LENGTH")
    private String bufferLength;
    /**
     * IS_NULLABLE
     */
    @MappingField(value = "IS_NULLABLE")
    private String isNullable;
    /**
     * TABLE_NAME
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     *
     */
    @MappingField(value = "COLUMN_DEF")
    private String columnDef;
    /**
     *
     */
    @MappingField(value = "SCOPE_CATALOG")
    private String scopeCatalog;
    /**
     * TABLE_SCHEM
     */
    @MappingField(value = "TABLE_SCHEM")
    private String tableSchem;
    /**
     * COLUMN_NAME
     */
    @MappingField(value = "COLUMN_NAME")
    private String columnName;
    /**
     * nullable
     */
    @MappingField(value = "NULLABLE")
    private String nullable;
    /**
     * REMARKS
     */
    @MappingField(value = "REMARKS")
    private String remarks;
    /**
     * DECIMAL_DIGITS
     */
    @MappingField(value = "DECIMAL_DIGITS")
    private String decimalDigits;
    /**
     *
     */
    @MappingField(value = "NUM_PREC_RADIX")
    private String numPrecRadix;
    /**
     *
     */
    @MappingField(value = "SQL_DATETIME_SUB")
    private String sqlDatetimeSub;
    /**
     * IS_GENERATEDCOLUMN
     */
    @MappingField(value = "IS_GENERATEDCOLUMN")
    private String isGeneratedcolumn;
    /**
     * IS_AUTOINCREMENT
     */
    @MappingField(value = "IS_AUTOINCREMENT")
    private String isAutoincrement;
    /**
     * SQL_DATA_TYPE
     */
    @MappingField(value = "SQL_DATA_TYPE")
    private String sqlDataType;
    /**
     * CHAR_OCTET_LENGTH
     */
    @MappingField(value = "CHAR_OCTET_LENGTH")
    private String charOctetLength;
    /**
     * ORDINAL_POSITION
     */
    @MappingField(value = "ORDINAL_POSITION")
    private String ordinalPosition;
    /**
     *
     */
    @MappingField(value = "SCOPE_SCHEMA")
    private String scopeSchema;
    /**
     *
     */
    @MappingField(value = "SOURCE_DATA_TYPE")
    private String sourceDataType;
    /**
     * DATA_TYPE
     */
    @MappingField(value = "DATA_TYPE")
    private String dataType;
    /**
     * TYPE_NAME
     */
    @MappingField(value = "TYPE_NAME")
    private String typeName;
    /**
     * COLUMN_SIZE
     */
    @MappingField(value = "COLUMN_SIZE")
    private String columnSize;
    /**
     * 是否主键
     */
    private String primaryKey;
    /**
     * 列类型（带长度）
     */
    @MappingField(value = "COLUMN_TYPE")
    private String columnType;

    /**
     * 列长度
     */
    @MappingField(value = "COLUMN_LENGTH")
    private String columnLength;
}
