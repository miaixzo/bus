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
 * @author Kimi Liu
 * @since Java 21+
 */
public class PptStyle {

    /**
     * Font object containing font name, style, and size.
     */
    private final Font font;
    /**
     * Font color; if {@code null}, no color is applied.
     */
    private final Color color;
    /**
     * Paragraph text alignment; if {@code null}, no alignment is applied.
     */
    private final TextParagraph.TextAlign align;

    /**
     * Constructs a new {@code PptStyle} with font, color, and alignment.
     *
     * @param font  the font object
     * @param color the font color, may be {@code null}
     * @param align the text alignment, may be {@code null}
     */
    public PptStyle(final Font font, final Color color, final TextParagraph.TextAlign align) {
        this.font = font;
        this.color = color;
        this.align = align;
    }

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
     * Returns the font object.
     *
     * @return the {@link Font}
     */
    public Font font() {
        return font;
    }

    /**
     * Returns the font color.
     *
     * @return the {@link Color}, or {@code null} if not set
     */
    public Color color() {
        return color;
    }

    /**
     * Returns the text alignment.
     *
     * @return the {@link TextParagraph.TextAlign}, or {@code null} if not set
     */
    public TextParagraph.TextAlign align() {
        return align;
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
