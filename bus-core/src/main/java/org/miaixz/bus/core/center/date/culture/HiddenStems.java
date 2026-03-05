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
 * Represents the types of Hidden Stems (藏干类型) within Earth Branches in Chinese metaphysics.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum HiddenStems {

    /**
     * Residual Qi (余气)
     */
    RESIDUAL(0, "余气"),

    /**
     * Middle Qi (中气)
     */
    MIDDLE(1, "中气"),

    /**
     * Principal Qi (本气)
     */
    PRINCIPAL(2, "本气");

    /**
     * The code representing the Hidden Stem type.
     */
    private final int code;

    /**
     * The name of the Hidden Stem type.
     */
    private final String name;

    /**
     * Constructs a {@code HiddenStems} enum entry.
     *
     * @param code The integer code for the Hidden Stem type.
     * @param name The Chinese character name for the Hidden Stem type.
     */
    HiddenStems(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Retrieves a {@code HiddenStems} enum entry by its code.
     *
     * @param code The code of the Hidden Stem type to retrieve.
     * @return The {@code HiddenStems} enum entry, or {@code null} if not found.
     */
    public static HiddenStems fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (HiddenStems item : values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * Retrieves a {@code HiddenStems} enum entry by its name.
     *
     * @param name The name of the Hidden Stem type to retrieve.
     * @return The {@code HiddenStems} enum entry, or {@code null} if not found.
     */
    public static HiddenStems fromName(String name) {
        if (null == name) {
            return null;
        }
        for (HiddenStems item : values()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Gets the code of this Hidden Stem type.
     *
     * @return The integer code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the name of this Hidden Stem type.
     *
     * @return The Chinese character name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name of this Hidden Stem type.
     *
     * @return The name of this Hidden Stem type.
     */
    @Override
    public String toString() {
        return getName();
    }

}
