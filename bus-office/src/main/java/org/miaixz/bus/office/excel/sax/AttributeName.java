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
