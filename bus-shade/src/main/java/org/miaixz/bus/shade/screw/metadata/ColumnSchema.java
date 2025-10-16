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

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the schema information for a database column. This class is a domain object that holds detailed properties
 * of a table column.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class ColumnSchema {

    /**
     * The 1-based index of the column within the table.
     */
    private String ordinalPosition;
    /**
     * The name of the column.
     */
    private String columnName;
    /**
     * The SQL data type, including length or precision (e.g., VARCHAR(255)).
     */
    private String columnType;
    /**
     * The name of the SQL data type (e.g., VARCHAR).
     */
    private String typeName;
    /**
     * The length of the column.
     */
    private String columnLength;
    /**
     * The size of the column (precision for numeric types, length for character types).
     */
    private String columnSize;
    /**
     * The number of fractional digits for numeric types.
     */
    private String decimalDigits;
    /**
     * Indicates whether the column can contain null values.
     */
    private String nullable;
    /**
     * Indicates if the column is part of the primary key.
     */
    private String primaryKey;
    /**
     * The default value of the column.
     */
    private String columnDef;
    /**
     * The comment or description of the column.
     */
    private String remarks;

}
