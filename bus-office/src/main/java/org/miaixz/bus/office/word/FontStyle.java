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
package org.miaixz.bus.office.word;

import java.awt.*;

import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 * Represents a font style configuration for Word documents.
 *
 * @param font  The font information (family, style, size).
 * @param color The font color.
 * @author Kimi Liu
 * @since Java 21+
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
