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

import org.xml.sax.Attributes;

/**
 * Enumeration of attribute names used in Excel XML for SAX parsing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum AttributeName {

    /**
     * Row and column number attribute. For row tags, this is the row number attribute name; for cell tags, it is the
     * column number attribute name.
     */
    r,
    /**
     * Index of the StylesTable (ST), used to get row or cell styles.
     */
    s,
    /**
     * Type attribute for cells, see {@link CellDataType}.
     */
    t;

    /**
     * Checks if the given attribute name string matches this enum instance.
     *
     * @param attributeName The attribute name string to match.
     * @return {@code true} if the attribute name matches, {@code false} otherwise.
     */
    public boolean match(final String attributeName) {
        return this.name().equals(attributeName);
    }

    /**
     * Retrieves the value of the attribute corresponding to this enum from the given {@link Attributes} list.
     *
     * @param attributes The list of attributes.
     * @return The attribute value, or {@code null} if the attribute is not found.
     */
    public String getValue(final Attributes attributes) {
        return attributes.getValue(name());
    }

}
