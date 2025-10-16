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
