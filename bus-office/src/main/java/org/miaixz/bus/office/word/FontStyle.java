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
package org.miaixz.bus.office.word;

import java.awt.*;

import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 * Represents a font style configuration for Word documents.
 *
 * @param font  The font information (family, style, size).
 * @param color The font color.
 * @author Kimi Liu
 * @since Java 17+
 */
public record FontStyle(Font font, Color color) {

    /**
     * Constructs a new {@code FontStyle} with no color (default).
     *
     * @param name  The font name (family).
     * @param style The font style. See {@link Font#PLAIN}, {@link Font#BOLD}, {@link Font#ITALIC}.
     * @param size  The font size.
     */
    public FontStyle(final String name, final int style, final int size) {
        this(new Font(name, style, size), null);
    }

    /**
     * Constructs a new {@code FontStyle} with a specified color.
     *
     * @param name  The font name (family).
     * @param style The font style. See {@link Font#PLAIN}, {@link Font#BOLD}, {@link Font#ITALIC}.
     * @param size  The font size.
     * @param color The font color.
     */
    public FontStyle(final String name, final int style, final int size, final Color color) {
        this(new Font(name, style, size), color);
    }

    /**
     * Applies the font style to the specified paragraph run.
     *
     * @param run The paragraph run object ({@link XWPFRun}) to modify.
     */
    public void fill(final XWPFRun run) {
        run.setFontFamily(font.getFamily());
        run.setFontSize(font.getSize());
        run.setBold(font.isBold());
        run.setItalic(font.isItalic());
        if (null != color) {
            // Convert Color to hex string "RRGGBB"
            run.setColor(String.format("%06X", color.getRGB() & 0xFFFFFF));
        }
    }

}
