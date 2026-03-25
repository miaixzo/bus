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
package org.miaixz.bus.office.excel.sax;

/**
 * Enumeration of element names used in Excel SAX parsing.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ElementName {

    /**
     * Row tag name, representing a row.
     */
    row,
    /**
     * Cell tag name, representing a cell.
     */
    c,
    /**
     * Value tag name, representing the value within a cell.
     */
    v,
    /**
     * Formula tag name, representing a cell containing a formula.
     */
    f;

    /**
     * Parses the given element name string into an {@code ElementName} enum.
     *
     * @param elementName The element name string.
     * @return The corresponding {@code ElementName} enum, or {@code null} if no match is found.
     */
    public static ElementName of(final String elementName) {
        try {
            return valueOf(elementName);
        } catch (final Exception ignore) {
        }
        return null;
    }

    /**
     * Checks if the given element name matches the current enum instance.
     *
     * @param elementName The element name string to match.
     * @return {@code true} if the element name matches, {@code false} otherwise.
     */
    public boolean match(final String elementName) {
        return this.name().equals(elementName);
    }

}
