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
package org.miaixz.bus.office.excel.cell.setters;

import java.util.regex.Pattern;

import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Cell value setter for escaping strings. It uses the {@code _x005F} prefix to escape {@code _xXXXX_} patterns,
 * preventing decoding issues. For example, if a user inputs '_x5116_', it could be garbled; this setter escapes it to
 * '_x005F_x5116_'.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EscapeStrCellSetter extends CharSequenceCellSetter {

    /**
     * Pattern for matching {@code _xXXXX_} sequences.
     */
    private static final Pattern utfPtrn = Pattern.compile("_x[0-9A-Fa-f]{4}_");

    /**
     * Constructor.
     *
     * @param value The value.
     */
    public EscapeStrCellSetter(final CharSequence value) {
        super(escape(StringKit.toStringOrNull(value)));
    }

    /**
     * Escapes {@code _xXXXX_} patterns with the {@code _x005F} prefix to prevent decoding issues.
     *
     * @param value The string to be escaped.
     * @return The escaped string.
     */
    private static String escape(final String value) {
        if (value == null || !value.contains("_x")) {
            return value;
        }

        // Escape `_xXXXX_` with the `_x005F` prefix to avoid decoding issues.
        return PatternKit.replaceAll(value, utfPtrn, "_x005F$0");
    }

}
