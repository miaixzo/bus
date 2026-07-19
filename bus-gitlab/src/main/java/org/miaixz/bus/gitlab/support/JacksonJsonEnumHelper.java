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
package org.miaixz.bus.gitlab.support;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The jackson JSON enum helper class.
 *
 * @param <E> the enum type
 * @author Kimi Liu
 * @since Java 21+
 */
public class JacksonJsonEnumHelper<E extends Enum<E>> {

    /**
     * Maps serialized enum names to enum values.
     */
    private Map<String, E> valuesMap;

    /**
     * Maps enum values to serialized enum names.
     */
    private Map<E, String> namesMap;

    /**
     * Initializes enum name mappings with lower-case serialized values.
     *
     * @param enumType the enum type
     */
    public JacksonJsonEnumHelper(Class<E> enumType) {
        this(enumType, false);
    }

    /**
     * Initializes enum name mappings with configurable first-letter capitalization.
     *
     * @param enumType               the enum type
     * @param firstLetterCapitalized whether the first letter is capitalized
     */
    public JacksonJsonEnumHelper(Class<E> enumType, boolean firstLetterCapitalized) {

        valuesMap = new HashMap<>();
        namesMap = new HashMap<>();

        for (E e : enumType.getEnumConstants()) {

            String name = e.name().toLowerCase();
            if (firstLetterCapitalized) {
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            }

            valuesMap.put(name, e);
            namesMap.put(e, name);
        }
    }

    /**
     * Initializes enum name mappings with optional first-letter capitalization and camel-case conversion.
     *
     * @param enumType               the enum type
     * @param firstLetterCapitalized whether the first letter is capitalized
     * @param camelCased             whether underscore-separated names are converted to camel case
     */
    public JacksonJsonEnumHelper(Class<E> enumType, boolean firstLetterCapitalized, boolean camelCased) {
        this(enumType, firstLetterCapitalized, camelCased, false);
    }

    /**
     * Initializes enum name mappings with configurable capitalization, camel-case conversion, and underscore
     * preservation.
     *
     * @param enumType               the enum type
     * @param firstLetterCapitalized whether the first letter is capitalized
     * @param camelCased             whether underscore-separated names are converted to camel case
     * @param preserveUnderscores    whether underscores are preserved
     */
    public JacksonJsonEnumHelper(Class<E> enumType, boolean firstLetterCapitalized, boolean camelCased,
            boolean preserveUnderscores) {

        valuesMap = new HashMap<>();
        namesMap = new HashMap<>();

        for (E e : enumType.getEnumConstants()) {

            char[] chars = e.name().toLowerCase().toCharArray();
            StringBuilder nameBuf = new StringBuilder(chars.length);
            boolean nextCharIsCapitalized = firstLetterCapitalized;
            for (char ch : chars) {
                if (ch == '_') {
                    if (preserveUnderscores) {
                        nameBuf.append(ch);
                    } else {
                        if (camelCased) {
                            nextCharIsCapitalized = true;
                        } else {
                            nameBuf.append(' ');
                        }
                    }
                } else if (nextCharIsCapitalized) {
                    nextCharIsCapitalized = false;
                    nameBuf.append(Character.toUpperCase(ch));
                } else {
                    nameBuf.append(ch);
                }
            }

            String name = nameBuf.toString();
            valuesMap.put(name, e);
            namesMap.put(e, name);
        }
    }

    /**
     * Add an enum that has a specialized name that does not fit the standard naming conventions.
     *
     * @param e    the enum to add
     * @param name the name for the enum
     */
    public void addEnum(E e, String name) {
        valuesMap.put(name, e);
        namesMap.put(e, name);
    }

    /**
     * Resolves the enum constant from a JSON value.
     *
     * @param value the JSON value
     * @return the matching enum constant
     */
    @JsonCreator
    public E forValue(String value) {
        return valuesMap.get(value);
    }

    /**
     * Get the string used by the API for this enum.
     *
     * @param e the enum value to get the API string for
     * @return the string used by the API for this enum
     */
    public String toString(E e) {
        return (namesMap.get(e));
    }

}
