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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Represents an ANSI 8-bit foreground or background color. These colors are part of the 256-color palette available in
 * many terminal emulators. The 256 colors are categorized as follows:
 * <ul>
 * <li>0-7: Standard colors (equivalent to ESC [ 30–37 m)</li>
 * <li>8-15: High-intensity colors (equivalent to ESC [ 90–97 m)</li>
 * <li>16-231: 6x6x6 color cube (216 colors), calculated as 16 + 36 * r + 6 * g + b (where 0 &lt;= r, g, b &lt;= 5)</li>
 * <li>232-255: 24 grayscale shades from black to white</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ansi8BitColor implements AnsiElement {

    /**
     * The prefix for 8-bit foreground color ANSI escape sequences.
     */
    private static final String PREFIX_FORE = "38;5;";
    /**
     * The prefix for 8-bit background color ANSI escape sequences.
     */
    private static final String PREFIX_BACK = "48;5;";
    /**
     * The prefix indicating whether it's a foreground or background color.
     */
    private final String prefix;
    /**
     * The 8-bit color code (0-255).
     */
    private final int code;

    /**
     * Constructs an {@code Ansi8BitColor} instance with the specified prefix and color code.
     *
     * @param prefix The prefix for the ANSI escape sequence (e.g., {@link #PREFIX_FORE} or {@link #PREFIX_BACK}).
     * @param code   The 8-bit color code (0-255).
     * @throws IllegalArgumentException if the color code is not within the range 0-255.
     */
    private Ansi8BitColor(String prefix, int code) {
        Assert.isTrue(code >= 0 && code <= 255, "Code must be between 0 and 255");
        this.prefix = prefix;
        this.code = code;
    }

    /**
     * Creates an {@code Ansi8BitColor} instance representing a foreground color.
     *
     * @param code The 8-bit color code (0-255).
     * @return A new {@code Ansi8BitColor} instance for a foreground color.
     */
    public static Ansi8BitColor foreground(int code) {
        return new Ansi8BitColor(PREFIX_FORE, code);
    }

    /**
     * Creates an {@code Ansi8BitColor} instance representing a background color.
     *
     * @param code The 8-bit color code (0-255).
     * @return A new {@code Ansi8BitColor} instance for a background color.
     */
    public static Ansi8BitColor background(int code) {
        return new Ansi8BitColor(PREFIX_BACK, code);
    }

    /**
     * Retrieves the 8-bit color code (0-255) of this ANSI color.
     *
     * @return The color code (0-255).
     */
    @Override
    public int getCode() {
        return this.code;
    }

    /**
     * Converts this {@code Ansi8BitColor} to its foreground representation. If this instance is already a foreground
     * color, it returns itself.
     *
     * @return An {@code Ansi8BitColor} instance representing the foreground version of this color.
     */
    public Ansi8BitColor asForeground() {
        if (PREFIX_FORE.equals(this.prefix)) {
            return this;
        }
        return Ansi8BitColor.foreground(this.code);
    }

    /**
     * Converts this {@code Ansi8BitColor} to its background representation. If this instance is already a background
     * color, it returns itself.
     *
     * @return An {@code Ansi8BitColor} instance representing the background version of this color.
     */
    public Ansi8BitColor asBackground() {
        if (PREFIX_BACK.equals(this.prefix)) {
            return this;
        }
        return Ansi8BitColor.background(this.code);
    }

    /**
     * Compares this {@code Ansi8BitColor} to the specified object. The result is {@code true} if and only if the
     * argument is not {@code null} and is an {@code Ansi8BitColor} object that has the same prefix and code as this
     * object.
     *
     * @param object The object to compare this {@code Ansi8BitColor} against.
     * @return {@code true} if the given object represents an {@code Ansi8BitColor} equivalent to this ANSI color,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Ansi8BitColor other = (Ansi8BitColor) object;
        return ObjectKit.equals(this.prefix, other.prefix) && this.code == other.code;
    }

    /**
     * Returns a hash code for this {@code Ansi8BitColor} object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return this.prefix.hashCode() * 31 + this.code;
    }

    /**
     * Returns the string representation of this ANSI 8-bit color, which is its ANSI escape sequence part.
     *
     * @return The ANSI escape sequence part (e.g., "38;5;255" for white foreground).
     */
    @Override
    public String toString() {
        return this.prefix + this.code;
    }

}
