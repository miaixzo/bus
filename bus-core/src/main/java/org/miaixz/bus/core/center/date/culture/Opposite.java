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
package org.miaixz.bus.core.center.date.culture;

/**
 * Represents the concept of Yin and Yang (阴阳) in Chinese philosophy.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Opposite {

    /**
     * Yin (阴)
     */
    YIN(0, "阴"),

    /**
     * Yang (阳)
     */
    YANG(1, "阳");

    /**
     * The code representing the Yin or Yang.
     */
    private final int code;

    /**
     * The name of the Yin or Yang.
     */
    private final String name;

    /**
     * Constructs an {@code Opposite} enum entry.
     *
     * @param code The integer code for the Yin or Yang.
     * @param name The Chinese character name for the Yin or Yang.
     */
    Opposite(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Retrieves an {@code Opposite} enum entry by its code.
     *
     * @param code The code of the Yin or Yang to retrieve.
     * @return The {@code Opposite} enum entry, or {@code null} if not found.
     */
    public static Opposite fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (Opposite item : values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * Retrieves an {@code Opposite} enum entry by its name.
     *
     * @param name The name of the Yin or Yang to retrieve.
     * @return The {@code Opposite} enum entry, or {@code null} if not found.
     */
    public static Opposite fromName(String name) {
        if (null == name) {
            return null;
        }
        for (Opposite item : values()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Gets the code of this Yin or Yang.
     *
     * @return The integer code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the name of this Yin or Yang.
     *
     * @return The Chinese character name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name of this Yin or Yang.
     *
     * @return The name of this Yin or Yang.
     */
    @Override
    public String toString() {
        return getName();
    }

}
