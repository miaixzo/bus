/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang;

import java.io.Serializable;

/**
 * Generic interface for enum elements, allowing custom enums to implement this interface for data conversion. It is
 * recommended to save {@code code()} values rather than {@code ordinal()} when persisting to a database, to guard
 * against future requirement changes.
 *
 * @param <E> The type of the enum implementing this interface.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Enumers<E extends Enumers<E>> extends Serializable {

    /**
     * Returns the code associated with this enum constant.
     *
     * @return The enum code.
     */
    int code();

    /**
     * Returns the name of this enum constant.
     *
     * @return The enum name.
     */
    String name();

    /**
     * Returns a descriptive text for this enum constant. In a Chinese context, enums often have a corresponding Chinese
     * description.
     *
     * @return The descriptive text, defaulting to the enum name if not overridden.
     */
    default String text() {
        return name();
    }

    /**
     * Returns an array containing all of the enum constants of this enum type, in the order they're declared.
     *
     * @return An array containing all the enum constants.
     */
    default E[] items() {
        return (E[]) this.getClass().getEnumConstants();
    }

    /**
     * Retrieves an enum constant by its integer code.
     *
     * @param intVal The integer value to search for.
     * @return The enum constant corresponding to the given integer value, or {@code null} if not found.
     */
    default E from(final Integer intVal) {
        if (intVal == null) {
            return null;
        }
        final E[] vs = items();
        for (final E enumItem : vs) {
            if (enumItem.code() == intVal) {
                return enumItem;
            }
        }
        return null;
    }

    /**
     * Retrieves an enum constant by its string value. This method can convert based on name or text, depending on the
     * implementation.
     *
     * @param strVal The string value to search for.
     * @return The enum constant corresponding to the given string value, or {@code null} if not found.
     */
    default E from(final String strVal) {
        if (strVal == null) {
            return null;
        }
        final E[] vs = items();
        for (final E enumItem : vs) {
            if (strVal.equalsIgnoreCase(enumItem.name())) {
                return enumItem;
            }
        }
        return null;
    }

}
