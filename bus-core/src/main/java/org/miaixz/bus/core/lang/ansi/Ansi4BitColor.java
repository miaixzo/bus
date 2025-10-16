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
package org.miaixz.bus.core.lang.ansi;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Represents standard 4-bit ANSI colors, including foreground and background colors. These colors are commonly used in
 * terminal applications for text styling.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Ansi4BitColor implements AnsiElement {

    /**
     * Default foreground color.
     */
    DEFAULT(39),

    /**
     * Black foreground color.
     */
    BLACK(30),

    /**
     * Red foreground color.
     */
    RED(31),

    /**
     * Green foreground color.
     */
    GREEN(32),

    /**
     * Yellow foreground color.
     */
    YELLOW(33),

    /**
     * Blue foreground color.
     */
    BLUE(34),

    /**
     * Magenta foreground color.
     */
    MAGENTA(35),

    /**
     * Cyan foreground color.
     */
    CYAN(36),

    /**
     * White foreground color.
     */
    WHITE(37),

    /**
     * Bright black foreground color.
     */
    BRIGHT_BLACK(90),

    /**
     * Bright red foreground color.
     */
    BRIGHT_RED(91),

    /**
     * Bright green foreground color.
     */
    BRIGHT_GREEN(92),

    /**
     * Bright yellow foreground color.
     */
    BRIGHT_YELLOW(93),

    /**
     * Bright blue foreground color.
     */
    BRIGHT_BLUE(94),

    /**
     * Bright magenta foreground color.
     */
    BRIGHT_MAGENTA(95),

    /**
     * Bright cyan foreground color.
     */
    BRIGHT_CYAN(96),

    /**
     * Bright white foreground color.
     */
    BRIGHT_WHITE(97),

    /**
     * Default background color.
     */
    BG_DEFAULT(49),

    /**
     * Black background color.
     */
    BG_BLACK(40),

    /**
     * Red background color.
     */
    BG_RED(41),

    /**
     * Green background color.
     */
    BG_GREEN(42),

    /**
     * Yellow background color.
     */
    BG_YELLOW(43),

    /**
     * Blue background color.
     */
    BG_BLUE(44),

    /**
     * Magenta background color.
     */
    BG_MAGENTA(45),

    /**
     * Cyan background color.
     */
    BG_CYAN(46);

    /**
     * The ANSI code associated with this color.
     */
    private final int code;

    /**
     * Constructs an {@code Ansi4BitColor} enum constant with the specified ANSI code.
     *
     * @param code The ANSI code for the color.
     */
    Ansi4BitColor(int code) {
        this.code = code;
    }

    /**
     * Retrieves the {@code Ansi4BitColor} enum constant corresponding to the given ANSI code.
     *
     * @param code The 4-bit ANSI color code.
     * @return The {@code Ansi4BitColor} enum constant.
     * @throws IllegalArgumentException if no matching {@code Ansi4BitColor} instance is found for the given code.
     */
    public static Ansi4BitColor of(int code) {
        for (Ansi4BitColor item : Ansi4BitColor.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        throw new IllegalArgumentException(StringKit.format("No matched Ansi4BitColor instance, code={}", code));
    }

    /**
     * Retrieves the ANSI code for this color, assuming it's a foreground color.
     *
     * @return The ANSI code for the foreground color.
     */
    @Override
    public int getCode() {
        return getCode(false);
    }

    /**
     * Retrieves the ANSI code for this color, optionally converting it to a background color code. Foreground color
     * codes are typically in the range 30-37 or 90-97. Background color codes are typically in the range 40-47 or
     * 100-107. The conversion adds 10 to the foreground code to get the corresponding background code.
     *
     * @param isBackground {@code true} to get the background color code, {@code false} for foreground.
     * @return The ANSI code for the color (foreground or background).
     */
    public int getCode(boolean isBackground) {
        return isBackground ? this.code + 10 : this.code;
    }

    /**
     * Returns the corresponding background color for this foreground color. If this enum constant already represents a
     * background color, it returns itself.
     *
     * @return An {@code Ansi4BitColor} representing the background version of this color.
     */
    public Ansi4BitColor asBackground() {
        return Ansi4BitColor.of(getCode(true));
    }

    /**
     * Returns the string representation of the ANSI code for this color.
     *
     * @return The ANSI code as a string.
     */
    @Override
    public String toString() {
        return StringKit.toString(this.code);
    }

}
