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
