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
package org.miaixz.bus.office.excel.style;

import java.io.Serial;
import java.io.Serializable;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Encapsulates cell border style and color. Borders are defined in the order of "top, right, bottom, left", consistent
 * with CSS.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CellBorderStyle implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852286028705L;

    /**
     * The top border style.
     */
    private BorderStyle topStyle;
    /**
     * The top border color.
     */
    private Short topColor;
    /**
     * The right border style.
     */
    private BorderStyle rightStyle;
    /**
     * The right border color.
     */
    private Short rightColor;
    /**
     * The bottom border style.
     */
    private BorderStyle bottomStyle;
    /**
     * The bottom border color.
     */
    private Short bottomColor;
    /**
     * The left border style.
     */
    private BorderStyle leftStyle;
    /**
     * The left border color.
     */
    private Short leftColor;

    /**
     * Creates a {@code CellBorderStyle} object based on an existing {@link CellStyle}.
     *
     * @param cellStyle The {@link CellStyle} to extract border information from.
     * @return A new {@code CellBorderStyle} instance.
     */
    public static CellBorderStyle of(final CellStyle cellStyle) {
        return new CellBorderStyle().setTopStyle(cellStyle.getBorderTop()).setTopColor(cellStyle.getTopBorderColor())
                .setRightStyle(cellStyle.getBorderRight()).setRightColor(cellStyle.getRightBorderColor())
                .setBottomStyle(cellStyle.getBorderBottom()).setBottomColor(cellStyle.getBottomBorderColor())
                .setLeftStyle(cellStyle.getBorderLeft()).setLeftColor(cellStyle.getLeftBorderColor());
    }

    /**
     * Creates a {@code CellBorderStyle} object where all four borders have the same style and color.
     *
     * @param borderStyle The {@link BorderStyle} for all four borders.
     * @param colorIndex  The {@link IndexedColors} for all four borders.
     * @return A new {@code CellBorderStyle} instance.
     */
    public static CellBorderStyle of(final BorderStyle borderStyle, final IndexedColors colorIndex) {
        return new CellBorderStyle().setTopStyle(borderStyle).setTopColor(colorIndex.getIndex())
                .setRightStyle(borderStyle).setRightColor(colorIndex.getIndex()).setBottomStyle(borderStyle)
                .setBottomColor(colorIndex.getIndex()).setLeftStyle(borderStyle).setLeftColor(colorIndex.getIndex());
    }

    /**
     * Gets the style of the top border.
     *
     * @return The {@link BorderStyle} of the top border.
     */
    public BorderStyle getTopStyle() {
        return topStyle;
    }

    /**
     * Sets the style of the top border.
     *
     * @param topStyle The {@link BorderStyle} for the top border.
     * @return This {@code CellBorderStyle} instance, for chaining.
     */
    public CellBorderStyle setTopStyle(final BorderStyle topStyle) {
        this.topStyle = topStyle;
        return this;
    }

    /**
     * Gets the color of the top border.
     *
     * @return The color index of the top border.
     */
    public Short getTopColor() {
        return topColor;
    }

    /**
     * Sets the color of the top border.
     *
     * @param topColor The color index for the top border.
     * @return This {@code CellBorderStyle} instance, for chaining.
     */
    public CellBorderStyle setTopColor(final Short topColor) {
        this.topColor = topColor;
        return this;
    }

    /**
     * Gets the style of the right border.
     *
     * @return The {@link BorderStyle} of the right border.
     */
    public BorderStyle getRightStyle() {
        return rightStyle;
    }

    /**
     * Sets the style of the right border.
     *
     * @param rightStyle The {@link BorderStyle} for the right border.
     * @return This {@code CellBorderStyle} instance, for chaining.
     */
    public CellBorderStyle setRightStyle(final BorderStyle rightStyle) {
        this.rightStyle = rightStyle;
        return this;
    }

    /**
     * Gets the color of the right border.
     *
     * @return The color index of the right border.
     */
    public Short getRightColor() {
        return rightColor;
    }

    /**
     * Sets the color of the right border.
     *
     * @param rightColor The color index for the right border.
     * @return This {@code CellBorderStyle} instance, for chaining.
     */
    public CellBorderStyle setRightColor(final Short rightColor) {
        this.rightColor = rightColor;
        return this;
    }

    /**
     * Gets the style of the bottom border.
     *
     * @return The {@link BorderStyle} of the bottom border.
     */
    public BorderStyle getBottomStyle() {
        return bottomStyle;
    }

    /**
     * Sets the style of the bottom border.
     *
     * @param bottomStyle The {@link BorderStyle} for the bottom border.
     * @return This {@code CellBorderStyle} instance, for chaining.
     */
    public CellBorderStyle setBottomStyle(final BorderStyle bottomStyle) {
        this.bottomStyle = bottomStyle;
        return this;
    }

    /**
     * Gets the color of the bottom border.
     *
     * @return The color index of the bottom border.
     */
    public Short getBottomColor() {
        return bottomColor;
    }

    /**
     * Sets the color of the bottom border.
     *
     * @param bottomColor The color index for the bottom border.
     * @return This {@code CellBorderStyle} instance, for chaining.
     */
    public CellBorderStyle setBottomColor(final Short bottomColor) {
        this.bottomColor = bottomColor;
        return this;
    }

    /**
     * Gets the style of the left border.
     *
     * @return The {@link BorderStyle} of the left border.
     */
    public BorderStyle getLeftStyle() {
        return leftStyle;
    }

    /**
     * Sets the style of the left border.
     *
     * @param leftStyle The {@link BorderStyle} for the left border.
     * @return This {@code CellBorderStyle} instance, for chaining.
     */
    public CellBorderStyle setLeftStyle(final BorderStyle leftStyle) {
        this.leftStyle = leftStyle;
        return this;
    }

    /**
     * Gets the color of the left border.
     *
     * @return The color index of the left border.
     */
    public Short getLeftColor() {
        return leftColor;
    }

    /**
     * Sets the color of the left border.
     *
     * @param leftColor The color index for the left border.
     * @return This {@code CellBorderStyle} instance, for chaining.
     */
    public CellBorderStyle setLeftColor(final Short leftColor) {
        this.leftColor = leftColor;
        return this;
    }

    /**
     * Applies the border style and color settings to the given {@link CellStyle}.
     *
     * @param cellStyle The {@link CellStyle} to apply the border settings to.
     * @return The modified {@link CellStyle}.
     */
    public CellStyle setTo(final CellStyle cellStyle) {
        ObjectKit.accept(this.topStyle, cellStyle::setBorderTop);
        ObjectKit.accept(this.topColor, cellStyle::setTopBorderColor);

        ObjectKit.accept(this.rightStyle, cellStyle::setBorderRight);
        ObjectKit.accept(this.rightColor, cellStyle::setRightBorderColor);

        ObjectKit.accept(this.bottomStyle, cellStyle::setBorderBottom);
        ObjectKit.accept(this.bottomColor, cellStyle::setBottomBorderColor);

        ObjectKit.accept(this.leftStyle, cellStyle::setBorderLeft);
        ObjectKit.accept(this.leftColor, cellStyle::setLeftBorderColor);

        return cellStyle;
    }

}
