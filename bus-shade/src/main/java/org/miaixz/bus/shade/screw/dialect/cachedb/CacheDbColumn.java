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
package org.miaixz.bus.shade.screw.dialect.cachedb;

import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.Column;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents column information for a CacheDB database table.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class CacheDbColumn implements Column {

    /**
     * The table name that is the scope of a REFERENCE attribute.
     */
    @MappingField(value = "SCOPE_TABLE")
    private String scopeTable;
    /**
     * The table catalog (may be {@code null}).
     */
    @MappingField(value = "TABLE_CAT")
    private String tableCat;
    /**
     * The buffer length.
     */
    @MappingField(value = "BUFFER_LENGTH")
    private String bufferLength;
    /**
     * Indicates whether the column is nullable ("YES", "NO", or empty string).
     */
    @MappingField(value = "IS_NULLABLE")
    private String isNullable;
    /**
     * The name of the table.
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     * The default value for the column.
     */
    @MappingField(value = "COLUMN_DEF")
    private String columnDef;
    /**
     * The catalog of the table that is the scope of a REFERENCE attribute.
     */
    @MappingField(value = "SCOPE_CATALOG")
    private String scopeCatalog;
    /**
     * The schema of the table.
     */
    @MappingField(value = "TABLE_SCHEM")
    private String tableSchem;
    /**
     * The name of the column.
     */
    @MappingField(value = "COLUMN_NAME")
    private String columnName;
    /**
     * Indicates if null values are allowed in the column.
     */
    @MappingField(value = "NULLABLE")
    private String nullable;
    /**
     * The comment describing the column.
     */
    @MappingField(value = "REMARKS")
    private String remarks;
    /**
     * The number of fractional digits for numeric types.
     */
    @MappingField(value = "DECIMAL_DIGITS")
    private String decimalDigits;
    /**
     * The radix (typically 10 or 2) for numeric types.
     */
    @MappingField(value = "NUM_PREC_RADIX")
    private String numPrecRadix;
    /**
     * The subtype code for datetime and interval data types.
     */
    @MappingField(value = "SQL_DATETIME_SUB")
    private String sqlDatetimeSub;
    /**
     * Indicates whether this is a generated column ("YES", "NO", or empty string).
     */
    @MappingField(value = "IS_GENERATEDCOLUMN")
    private String isGeneratedcolumn;
    /**
     * Indicates whether this column is auto-incrementing ("YES", "NO", or empty string).
     */
    @MappingField(value = "IS_AUTOINCREMENT")
    private String isAutoincrement;
    /**
     * The SQL data type from {@link java.sql.Types}.
     */
    @MappingField(value = "SQL_DATA_TYPE")
    private String sqlDataType;
    /**
     * For character types, the maximum number of bytes in the column.
     */
    @MappingField(value = "CHAR_OCTET_LENGTH")
    private String charOctetLength;
    /**
     * The 1-based index of the column in the table.
     */
    @MappingField(value = "ORDINAL_POSITION")
    private String ordinalPosition;
    /**
     * The schema of the table that is the scope of a REFERENCE attribute.
     */
    @MappingField(value = "SCOPE_SCHEMA")
    private String scopeSchema;
    /**
     * The source data type of a distinct type or user-generated Ref type.
     */
    @MappingField(value = "SOURCE_DATA_TYPE")
    private String sourceDataType;
    /**
     * The SQL data type from {@link java.sql.Types}.
     */
    @MappingField(value = "DATA_TYPE")
    private String dataType;
    /**
     * The data source-dependent type name.
     */
    @MappingField(value = "TYPE_NAME")
    private String typeName;
    /**
     * The size of the column.
     */
    @MappingField(value = "COLUMN_SIZE")
    private String columnSize;
    /**
     * Indicates if the column is part of the primary key ("Y" or "N").
     */
    private String primaryKey;
    /**
     * The full column type definition, including length or precision.
     */
    @MappingField(value = "COLUMN_TYPE")
    private String columnType;

    /**
     * The length of the column.
     */
    @MappingField(value = "COLUMN_LENGTH")
    private String columnLength;

}
