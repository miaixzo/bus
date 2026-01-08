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
package org.miaixz.bus.core.io.file;

/**
 * Enumeration for common line separator types. Line separators include:
 * 
 * <pre>
 * Mac system line separator: "\r"
 * Linux system line separator: "\n"
 * Windows system line separator: "\r\n"
 * </pre>
 *
 * @author Kimi Liu
 * @see #MAC
 * @see #LINUX
 * @see #WINDOWS
 * @since Java 17+
 */
public enum LineSeparator {

    /**
     * Mac system line separator: "\r"
     */
    MAC("\r"),
    /**
     * Linux system line separator: "\n"
     */
    LINUX("\n"),
    /**
     * Windows system line separator: "\r\n"
     */
    WINDOWS("\r\n");

    /**
     * The string value of the line separator.
     */
    private final String value;

    /**
     * Constructs a {@code LineSeparator} enum constant.
     *
     * @param lineSeparator The string value of the line separator.
     */
    LineSeparator(final String lineSeparator) {
        this.value = lineSeparator;
    }

    /**
     * Returns the string value of the line separator.
     *
     * @return The string value of the line separator.
     */
    public String getValue() {
        return this.value;
    }

}
