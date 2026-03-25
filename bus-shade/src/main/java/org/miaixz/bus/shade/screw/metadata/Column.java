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

/**
 * Represents a column in a database table.
 *
 * @author Kimi Liu
 * @since Java 21+
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
