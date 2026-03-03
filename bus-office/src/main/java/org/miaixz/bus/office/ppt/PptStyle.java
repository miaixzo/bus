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
package org.miaixz.bus.office.ppt;

import java.awt.*;

import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * Represents a font and paragraph style configuration for PowerPoint documents. Similar to
 * {@link org.miaixz.bus.office.word.FontStyle} but adapted for XSLF (PowerPoint) text runs.
 *
 * @param font  The font information (family, style, size).
 * @param color The font color.
 * @param align The text alignment.
 * @author Kimi Liu
 * @since Java 17+
 */
public record PptStyle(Font font, Color color, TextParagraph.TextAlign align) {

    /**
     * Constructs a new {@code PptStyle} with font only.
     *
     * @param name  The font name (family).
     * @param style The font style. See {@link Font#PLAIN}, {@link Font#BOLD}, {@link Font#ITALIC}.
     * @param size  The font size.
     */
    public PptStyle(final String name, final int style, final int size) {
        this(new Font(name, style, size), null, null);
    }

    /**
     * Constructs a new {@code PptStyle} with font and color.
     *
     * @param name  The font name (family).
     * @param style The font style. See {@link Font#PLAIN}, {@link Font#BOLD}, {@link Font#ITALIC}.
     * @param size  The font size.
     * @param color The font color.
     */
    public PptStyle(final String name, final int style, final int size, final Color color) {
        this(new Font(name, style, size), color, null);
    }

    /**
     * Constructs a new {@code PptStyle} with font, color, and alignment.
     *
     * @param name  The font name (family).
     * @param style The font style. See {@link Font#PLAIN}, {@link Font#BOLD}, {@link Font#ITALIC}.
     * @param size  The font size.
     * @param color The font color.
     * @param align The text alignment.
     */
    public PptStyle(final String name, final int style, final int size, final Color color,
            final TextParagraph.TextAlign align) {
        this(new Font(name, style, size), color, align);
    }

    /**
     * Applies the font style to the specified text run.
     *
     * @param run The text run ({@link XSLFTextRun}) to modify.
     */
    public void fill(final XSLFTextRun run) {
        run.setFontFamily(font.getFamily());
        run.setFontSize((double) font.getSize());
        run.setBold(font.isBold());
        run.setItalic(font.isItalic());
        if (null != color) {
            run.setFontColor(color);
        }
    }

    /**
     * Applies the alignment to the specified paragraph.
     *
     * @param paragraph The paragraph ({@link XSLFTextParagraph}) to modify.
     */
    public void fill(final XSLFTextParagraph paragraph) {
        if (null != align) {
            paragraph.setTextAlign(align);
        }
    }

}
