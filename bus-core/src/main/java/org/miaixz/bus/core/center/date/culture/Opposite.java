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
