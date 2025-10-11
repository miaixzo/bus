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
package org.miaixz.bus.shade.screw.dialect.oracle;

import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.Column;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents column information for an Oracle database table.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class OracleColumn implements Column {

    /**
     * The name of the table that is the scope of a REFERENCE attribute (null if DATA_TYPE is not REF).
     */
    @MappingField(value = "SCOPE_TABLE")
    private Object scopeTable;
    /**
     * The table catalog (may be {@code null}).
     */
    @MappingField(value = "TABLE_CAT")
    private Object tableCat;
    /**
     * Not used.
     */
    @MappingField(value = "BUFFER_LENGTH")
    private String bufferLength;
    /**
     * ISO rules are used to determine if the column can include nulls.
     */
    @MappingField(value = "IS_NULLABLE")
    private String isNullable;
    /**
     * The name of the table.
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     * The default value for the column, which should be interpreted as a string when the value is enclosed in single
     * quotes (may be null).
     */
    @MappingField(value = "COLUMN_DEF")
    private String columnDef;
    /**
     * Catalog of the table that is the scope of a REFERENCE attribute (null if DATA_TYPE is not REF).
     */
    @MappingField(value = "SCOPE_CATALOG")
    private Object scopeCatalog;
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
     * Whether NULL values are allowed.
     */
    @MappingField(value = "NULLABLE")
    private String nullable;
    /**
     * Comment describing the column (may be null).
     */
    @MappingField(value = "REMARKS")
    private String remarks;
    /**
     * The number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable.
     */
    @MappingField(value = "DECIMAL_DIGITS")
    private String decimalDigits;
    /**
     * Radix (typically either 10 or 2).
     */
    @MappingField(value = "NUM_PREC_RADIX")
    private String numPrecRadix;
    /**
     * Subtype code for datetime and interval data types.
     */
    @MappingField(value = "SQL_DATETIME_SUB")
    private String sqlDatetimeSub;
    /**
     * Indicates whether this is a generated column.
     */
    @MappingField(value = "IS_GENERATEDCOLUMN")
    private String isGeneratedColumn;
    /**
     * Indicates whether this column is auto-incrementing. YES --- if the column is auto-incrementing, NO --- if it is
     * not.
     */
    @MappingField(value = "IS_AUTOINCREMENT")
    private String isAutoIncrement;
    /**
     * SQL data type from java.sql.Types.
     */
    @MappingField(value = "SQL_DATA_TYPE")
    private String sqlDataType;
    /**
     * For char types, the maximum number of bytes in the column.
     */
    @MappingField(value = "CHAR_OCTET_LENGTH")
    private String charOctetLength;
    /**
     * The 1-based index of the column in the table.
     */
    @MappingField(value = "ORDINAL_POSITION")
    private String ordinalPosition;
    /**
     * Schema of the table that is the scope of a REFERENCE attribute (null if DATA_TYPE is not REF).
     */
    @MappingField(value = "SCOPE_SCHEMA")
    private String scopeSchema;
    /**
     * Source type of a distinct type or user-generated Ref type, from java.sql.Types (null if DATA_TYPE is not DISTINCT
     * or a user-generated REF).
     */
    @MappingField(value = "SOURCE_DATA_TYPE")
    private String sourceDataType;
    /**
     * SQL type from java.sql.Types.
     */
    @MappingField(value = "DATA_TYPE")
    private String dataType;
    /**
     * Data source dependent type name; for a UDT, the type name is fully qualified.
     */
    @MappingField(value = "TYPE_NAME")
    private String typeName;
    /**
     * The specified column size. For numeric data, this is the maximum precision. For character data, this is the
     * length in characters. For datetime data types, this is the length in characters of the String representation. For
     * binary data, this is the length in bytes. For ROWID data types, this is the length in bytes. Null is returned for
     * data types where the column size is not applicable.
     */
    @MappingField(value = "COLUMN_SIZE")
    private String columnSize;

    /**
     * Indicates if the column is part of the primary key.
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
