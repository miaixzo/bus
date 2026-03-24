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
package org.miaixz.bus.core.lang;

import java.io.Serializable;

/**
 * Generic interface for enum elements, allowing custom enums to implement this interface for data conversion. It is
 * recommended to save {@code code()} values rather than {@code ordinal()} when persisting to a database, to guard
 * against future requirement changes.
 *
 * @param <E> The type of the enum implementing this interface.
 * @author Kimi Liu
 * @since Java 21+
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
