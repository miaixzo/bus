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
package org.miaixz.bus.office.excel.xyz;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.style.CellBorderStyle;

/**
 * Utility class for Excel cell styles.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StyleKit {

    /**
     * Creates a new cell style for the workbook.
     *
     * @param workbook The {@link Workbook} to create the style for.
     * @return A new {@link CellStyle}.
     * @see Workbook#createCellStyle()
     */
    public static CellStyle createCellStyle(final Workbook workbook) {
        if (null == workbook) {
            return null;
        }
        return workbook.createCellStyle();
    }

    /**
     * Creates a default normal cell style.
     *
     * <pre>
     * 1. Text is centered horizontally and vertically.
     * 2. Thin border, black color.
     * </pre>
     *
     * @param workbook The {@link Workbook} to create the style for.
     * @return A new {@link CellStyle}.
     */
    public static CellStyle createDefaultCellStyle(final Workbook workbook) {
        final CellStyle cellStyle = createCellStyle(workbook);
        setAlign(cellStyle, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        setBorder(cellStyle, BorderStyle.THIN, IndexedColors.BLACK);
        return cellStyle;
    }

    /**
     * Creates a default header style.
     *
     * @param workbook The {@link Workbook} to create the style for.
     * @return A new {@link CellStyle}.
     */
    public static CellStyle createHeadCellStyle(final Workbook workbook) {
        final CellStyle cellStyle = createCellStyle(workbook);
        setAlign(cellStyle, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        setBorder(cellStyle, BorderStyle.THIN, IndexedColors.BLACK);
        setColor(cellStyle, IndexedColors.GREY_25_PERCENT, FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    /**
     * Checks if the given style is {@code null} (no style) or the default style. The default style is considered to be
     * {@code workbook.getCellStyleAt(0)}.
     *
     * @param workbook The workbook.
     * @param style    The style to check.
     * @return {@code true} if the style is {@code null} or the default style, {@code false} otherwise.
     */
    public static boolean isNullOrDefaultStyle(final Workbook workbook, final CellStyle style) {
        return (null == style) || style.equals(workbook.getCellStyleAt(0));
    }

    /**
     * Clones an existing {@link CellStyle} to a new one for the given cell.
     *
     * @param cell      The cell for which to clone the style.
     * @param cellStyle The {@link CellStyle} to be cloned.
     * @return The new cloned {@link CellStyle}.
     */
    public static CellStyle cloneCellStyle(final Cell cell, final CellStyle cellStyle) {
        return cloneCellStyle(cell.getSheet().getWorkbook(), cellStyle);
    }

    /**
     * Clones an existing {@link CellStyle} to a new one for the given workbook.
     *
     * @param workbook  The workbook to create the new style in.
     * @param cellStyle The {@link CellStyle} to be cloned.
     * @return The new cloned {@link CellStyle}.
     */
    public static CellStyle cloneCellStyle(final Workbook workbook, final CellStyle cellStyle) {
        final CellStyle newCellStyle = createCellStyle(workbook);
        newCellStyle.cloneStyleFrom(cellStyle);
        return newCellStyle;
    }

    /**
     * Sets the text alignment for a cell style.
     *
     * @param cellStyle The {@link CellStyle} to modify.
     * @param halign    The horizontal alignment.
     * @param valign    The vertical alignment.
     * @return The modified {@link CellStyle}.
     */
    public static CellStyle setAlign(
            final CellStyle cellStyle,
            final HorizontalAlignment halign,
            final VerticalAlignment valign) {
        cellStyle.setAlignment(halign);
        cellStyle.setVerticalAlignment(valign);
        return cellStyle;
    }

    /**
     * Sets the border style and color for all four borders of a cell.
     *
     * @param cellStyle  The {@link CellStyle} to modify.
     * @param borderSize The border thickness, represented by {@link BorderStyle} enum.
     * @param colorIndex The short value of the predefined color, see {@link IndexedColors} enum.
     * @return The modified {@link CellStyle}.
     */
    public static CellStyle setBorder(
            final CellStyle cellStyle,
            final BorderStyle borderSize,
            final IndexedColors colorIndex) {
        return setBorder(cellStyle, CellBorderStyle.of(borderSize, colorIndex));
    }

    /**
     * Sets the border style and color for all four borders of a cell.
     *
     * @param cellStyle       The {@link CellStyle} to modify.
     * @param cellBorderStyle The {@link CellBorderStyle} object containing border style and color information.
     * @return The modified {@link CellStyle}.
     */
    public static CellStyle setBorder(final CellStyle cellStyle, final CellBorderStyle cellBorderStyle) {
        return cellBorderStyle.setTo(cellStyle);
    }

    /**
     * Sets the border style for a specified range of cells based on a {@link CellStyle}.
     *
     * @param sheet            The {@link Sheet}.
     * @param cellRangeAddress The range of cells for which to set the border style.
     * @param cellBorderStyle  The border style, including border style and color.
     */
    public static void setBorderStyle(
            final Sheet sheet,
            final CellRangeAddress cellRangeAddress,
            final CellBorderStyle cellBorderStyle) {
        if (null != cellBorderStyle) {
            RegionUtil.setBorderTop(cellBorderStyle.getTopStyle(), cellRangeAddress, sheet);
            RegionUtil.setBorderRight(cellBorderStyle.getRightStyle(), cellRangeAddress, sheet);
            RegionUtil.setBorderBottom(cellBorderStyle.getBottomStyle(), cellRangeAddress, sheet);
            RegionUtil.setBorderLeft(cellBorderStyle.getLeftStyle(), cellRangeAddress, sheet);

            RegionUtil.setTopBorderColor(cellBorderStyle.getTopColor(), cellRangeAddress, sheet);
            RegionUtil.setRightBorderColor(cellBorderStyle.getRightColor(), cellRangeAddress, sheet);
            RegionUtil.setLeftBorderColor(cellBorderStyle.getLeftColor(), cellRangeAddress, sheet);
            RegionUtil.setBottomBorderColor(cellBorderStyle.getBottomColor(), cellRangeAddress, sheet);
        }
    }

    /**
     * Sets the border style for a specified range of cells based on a {@link CellStyle}.
     *
     * @param sheet            The {@link Sheet}.
     * @param cellRangeAddress The {@link CellRangeAddress}.
     * @param cellStyle        The {@link CellStyle}.
     */
    public static void setBorderStyle(
            final Sheet sheet,
            final CellRangeAddress cellRangeAddress,
            final CellStyle cellStyle) {
        if (null != cellStyle) {
            final CellBorderStyle cellBorderStyle = CellBorderStyle.of(cellStyle);
            setBorderStyle(sheet, cellRangeAddress, cellBorderStyle);
        }
    }

    /**
     * Sets the color for a cell (i.e., cell background color).
     *
     * @param cellStyle   The {@link CellStyle} to modify.
     * @param color       The predefined background color, see {@link IndexedColors} enum.
     * @param fillPattern The fill pattern, see {@link FillPatternType} enum.
     * @return The modified {@link CellStyle}.
     */
    public static CellStyle setColor(
            final CellStyle cellStyle,
            final IndexedColors color,
            final FillPatternType fillPattern) {
        return setColor(cellStyle, color.index, fillPattern);
    }

    /**
     * Sets the color for a cell (i.e., cell background color).
     *
     * @param cellStyle   The {@link CellStyle} to modify.
     * @param color       The predefined background color, see {@link IndexedColors} enum.
     * @param fillPattern The fill pattern, see {@link FillPatternType} enum.
     * @return The modified {@link CellStyle}.
     */
    public static CellStyle setColor(final CellStyle cellStyle, final short color, final FillPatternType fillPattern) {
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(ObjectKit.defaultIfNull(fillPattern, FillPatternType.SOLID_FOREGROUND));
        return cellStyle;
    }

    /**
     * Sets the color for a cell (i.e., cell background color).
     *
     * @param cellStyle   The {@link XSSFCellStyle} to modify.
     * @param color       The background color.
     * @param fillPattern The fill pattern, see {@link FillPatternType} enum.
     * @return The modified {@link XSSFCellStyle}.
     */
    public static CellStyle setColor(
            final XSSFCellStyle cellStyle,
            final XSSFColor color,
            final FillPatternType fillPattern) {
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(ObjectKit.defaultIfNull(fillPattern, FillPatternType.SOLID_FOREGROUND));
        return cellStyle;
    }

    /**
     * Creates a new font for the workbook.
     *
     * @param workbook The {@link Workbook}.
     * @param color    The font color.
     * @param fontSize The font size.
     * @param fontName The font name. May be {@code null} to use the default font.
     * @return A new {@link Font}.
     */
    public static Font createFont(
            final Workbook workbook,
            final short color,
            final short fontSize,
            final String fontName) {
        final Font font = workbook.createFont();
        return setFontStyle(font, color, fontSize, fontName);
    }

    /**
     * Sets the font style.
     *
     * @param font     The font {@link Font} to modify.
     * @param color    The font color.
     * @param fontSize The font size.
     * @param fontName The font name. May be {@code null} to use the default font.
     * @return The modified {@link Font}.
     */
    public static Font setFontStyle(final Font font, final short color, final short fontSize, final String fontName) {
        if (color > 0) {
            font.setColor(color);
        }
        if (fontSize > 0) {
            font.setFontHeightInPoints(fontSize);
        }
        if (StringKit.isNotBlank(fontName)) {
            font.setFontName(fontName);
        }
        return font;
    }

    /**
     * Creates a data format and retrieves the format index.
     *
     * @param workbook The {@link Workbook}.
     * @param format   The data format string.
     * @return The format index.
     */
    public static Short getFormat(final Workbook workbook, final String format) {
        final DataFormat dataFormat = workbook.createDataFormat();
        return dataFormat.getFormat(format);
    }

}
