/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel.sax;

/**
 * Enumeration of cell data types for SAX parsing of Excel files.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum CellDataType {

    /**
     * Boolean type.
     */
    BOOL("b"),
    /**
     * Error type.
     */
    ERROR("e"),
    /**
     * Formula result type. This type is determined by the 'f' tag, not by an attribute.
     */
    FORMULA("formula"),
    /**
     * Inline string (rich text) type.
     */
    INLINESTR("inlineStr"),
    /**
     * Shared string table index type.
     */
    SSTINDEX("s"),
    /**
     * Numeric type.
     */
    NUMBER(""),
    /**
     * Date type. This type is determined by value, not by an attribute.
     */
    DATE("m/d/yy"),
    /**
     * Null type.
     */
    NULL("");

    /**
     * The attribute value corresponding to the cell data type.
     */
    private final String name;

    /**
     * Constructs a {@code CellDataType} enum with the specified attribute value.
     *
     * @param name The attribute value for the type.
     */
    CellDataType(final String name) {
        this.name = name;
    }

    /**
     * Converts a type string to its corresponding {@code CellDataType} enum.
     *
     * @param name The type string.
     * @return The {@code CellDataType} enum, or {@link #NULL} if no match is found or the name is {@code null}.
     */
    public static CellDataType of(final String name) {
        if (null == name) {
            // Default to null
            return NULL;
        }

        if (BOOL.name.equals(name)) {
            return BOOL;
        } else if (ERROR.name.equals(name)) {
            return ERROR;
        } else if (INLINESTR.name.equals(name)) {
            return INLINESTR;
        } else if (SSTINDEX.name.equals(name)) {
            return SSTINDEX;
        } else if (FORMULA.name.equals(name)) {
            return FORMULA;
        } else {
            return NULL;
        }
    }

    /**
     * Gets the attribute value corresponding to this type.
     *
     * @return The attribute value.
     */
    public String getName() {
        return name;
    }

}
