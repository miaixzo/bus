/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.screw.metadata;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the schema information for a database column. This class is a domain object that holds detailed properties
 * of a table column.
 *
 * @author Kimi Liu
 * @since Java 21+
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
