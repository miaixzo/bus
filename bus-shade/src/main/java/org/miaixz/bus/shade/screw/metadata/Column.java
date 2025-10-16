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
package org.miaixz.bus.shade.screw.metadata;

/**
 * Represents a column in a database table.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Column {

    /**
     * Retrieves the name of the table.
     *
     * @return The table name.
     */
    String getTableName();

    /**
     * Retrieves the 1-based index of the column within the table.
     *
     * @return The ordinal position of the column.
     */
    String getOrdinalPosition();

    /**
     * Retrieves the name of the column.
     *
     * @return The column name.
     */
    String getColumnName();

    /**
     * Retrieves the data source-dependent type name for the column's data type.
     *
     * @return The type name of the column.
     */
    String getTypeName();

    /**
     * Retrieves the column size. For numeric data, this is the maximum precision. For character data, it is the length
     * in characters. For binary data, it is the length in bytes. For ROWID data types, this is the length in bytes.
     * Null is returned for data types where the column size is not applicable.
     *
     * @return The size of the column.
     */
    String getColumnSize();

    /**
     * Retrieves the number of fractional digits for numeric types.
     *
     * @return The number of decimal digits.
     */
    String getDecimalDigits();

    /**
     * Indicates whether the column can contain null values.
     *
     * @return A string indicating nullability.
     */
    String getNullable();

    /**
     * Indicates if the column is part of the primary key.
     *
     * @return A string indicating if it's a primary key (e.g., "Y" or "N").
     */
    String getPrimaryKey();

    /**
     * Retrieves the default value for the column.
     *
     * @return The default value.
     */
    String getColumnDef();

    /**
     * Retrieves the comment describing the column.
     *
     * @return The column remarks.
     */
    String getRemarks();

    /**
     * Retrieves the full column type definition, including length or precision.
     *
     * @return The full column type.
     */
    String getColumnType();

    /**
     * Retrieves the length of the column.
     *
     * @return The column length.
     */
    String getColumnLength();

}
