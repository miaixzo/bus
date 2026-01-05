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
package org.miaixz.bus.office.excel.style;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.miaixz.bus.office.excel.xyz.StyleKit;

/**
 * Default style set, defining default styles for headers, numbers, dates, etc.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultStyleSet implements StyleSet, Serializable {

    @Serial
    private static final long serialVersionUID = 2852286072270L;

    /**
     * Workbook reference.
     */
    private final Workbook workbook;
    /**
     * Header cell style.
     */
    private final CellStyle headCellStyle;
    /**
     * Default cell style.
     */
    private final CellStyle cellStyle;
    /**
     * Default numeric cell style.
     */
    private final CellStyle cellStyleForNumber;
    /**
     * Default date cell style.
     */
    private final CellStyle cellStyleForDate;
    /**
     * Default hyperlink cell style.
     */
    private final CellStyle cellStyleForHyperlink;

    /**
     * Constructs a new {@code DefaultStyleSet}.
     *
     * @param workbook The workbook to which these styles apply.
     */
    public DefaultStyleSet(final Workbook workbook) {
        this.workbook = workbook;
        this.headCellStyle = StyleKit.createHeadCellStyle(workbook);
        this.cellStyle = StyleKit.createDefaultCellStyle(workbook);

        // Default numeric format.
        cellStyleForNumber = StyleKit.cloneCellStyle(workbook, this.cellStyle);
        // 0 means: General
        cellStyleForNumber.setDataFormat((short) 0);

        // Default date format.
        this.cellStyleForDate = StyleKit.cloneCellStyle(workbook, this.cellStyle);
        // 22 means: m/d/yy h:mm
        this.cellStyleForDate.setDataFormat((short) 22);

        // Default hyperlink style.
        this.cellStyleForHyperlink = StyleKit.cloneCellStyle(workbook, this.cellStyle);
        final Font font = workbook.createFont();
        font.setUnderline((byte) 1);
        font.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
        this.cellStyleForHyperlink.setFont(font);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param reference the cell reference
     * @param cellValue the cell value
     * @param isHeader  whether this is a header cell
     * @return the appropriate cell style for the given cell
     */
    @Override
    public CellStyle getStyleFor(final CellReference reference, final Object cellValue, final boolean isHeader) {
        CellStyle style = null;

        if (isHeader && null != this.headCellStyle) {
            style = headCellStyle;
        } else if (null != cellStyle) {
            style = cellStyle;
        }

        if (cellValue instanceof Date || cellValue instanceof TemporalAccessor || cellValue instanceof Calendar) {
            // Date values have a specific format.
            if (null != this.cellStyleForDate) {
                style = this.cellStyleForDate;
            }
        } else if (cellValue instanceof Number) {
            // Numeric values have a specific format.
            if ((cellValue instanceof Double || cellValue instanceof Float || cellValue instanceof BigDecimal)
                    && null != this.cellStyleForNumber) {
                style = this.cellStyleForNumber;
            }
        } else if (cellValue instanceof Hyperlink) {
            // Custom hyperlink style.
            if (null != this.cellStyleForHyperlink) {
                style = this.cellStyleForHyperlink;
            }
        }

        return style;
    }

    /**
     * Gets the header style. After obtaining, the overall header style can be defined.
     *
     * @return The header {@link CellStyle}.
     */
    public CellStyle getHeadCellStyle() {
        return this.headCellStyle;
    }

    /**
     * Gets the general cell style. After obtaining, the overall general cell style can be defined.
     *
     * @return The general cell {@link CellStyle}.
     */
    public CellStyle getCellStyle() {
        return this.cellStyle;
    }

    /**
     * Gets the numeric cell style (with decimal points). After obtaining, the overall numeric style can be defined.
     *
     * @return The numeric (with decimal points) cell {@link CellStyle}.
     */
    public CellStyle getCellStyleForNumber() {
        return this.cellStyleForNumber;
    }

    /**
     * Gets the date cell style. After obtaining, the overall date style can be defined.
     *
     * @return The date cell {@link CellStyle}.
     */
    public CellStyle getCellStyleForDate() {
        return this.cellStyleForDate;
    }

    /**
     * Gets the hyperlink cell style. After obtaining, the overall hyperlink style can be defined.
     *
     * @return The hyperlink cell {@link CellStyle}.
     */
    public CellStyle getCellStyleForHyperlink() {
        return this.cellStyleForHyperlink;
    }

    /**
     * Defines the border type for all cells.
     *
     * @param borderSize The border thickness, represented by {@link BorderStyle} enum.
     * @param colorIndex The short value of the color, see {@link IndexedColors} enum.
     * @return This {@code DefaultStyleSet} instance, for chaining.
     */
    public DefaultStyleSet setBorder(final BorderStyle borderSize, final IndexedColors colorIndex) {
        StyleKit.setBorder(this.headCellStyle, borderSize, colorIndex);
        StyleKit.setBorder(this.cellStyle, borderSize, colorIndex);
        StyleKit.setBorder(this.cellStyleForNumber, borderSize, colorIndex);
        StyleKit.setBorder(this.cellStyleForDate, borderSize, colorIndex);
        StyleKit.setBorder(this.cellStyleForHyperlink, borderSize, colorIndex);
        return this;
    }

    /**
     * Sets the text alignment for cells.
     *
     * @param halign The horizontal alignment.
     * @param valign The vertical alignment.
     * @return This {@code DefaultStyleSet} instance, for chaining.
     */
    public DefaultStyleSet setAlign(final HorizontalAlignment halign, final VerticalAlignment valign) {
        StyleKit.setAlign(this.headCellStyle, halign, valign);
        StyleKit.setAlign(this.cellStyle, halign, valign);
        StyleKit.setAlign(this.cellStyleForNumber, halign, valign);
        StyleKit.setAlign(this.cellStyleForDate, halign, valign);
        StyleKit.setAlign(this.cellStyleForHyperlink, halign, valign);
        return this;
    }

    /**
     * Sets the background style for cells.
     *
     * @param backgroundColor The background color.
     * @param withHeadCell    {@code true} to also define the header cell style, {@code false} otherwise.
     * @return This {@code DefaultStyleSet} instance, for chaining.
     */
    public DefaultStyleSet setBackgroundColor(final IndexedColors backgroundColor, final boolean withHeadCell) {
        if (withHeadCell) {
            StyleKit.setColor(this.headCellStyle, backgroundColor, FillPatternType.SOLID_FOREGROUND);
        }
        StyleKit.setColor(this.cellStyle, backgroundColor, FillPatternType.SOLID_FOREGROUND);
        StyleKit.setColor(this.cellStyleForNumber, backgroundColor, FillPatternType.SOLID_FOREGROUND);
        StyleKit.setColor(this.cellStyleForDate, backgroundColor, FillPatternType.SOLID_FOREGROUND);
        StyleKit.setColor(this.cellStyleForHyperlink, backgroundColor, FillPatternType.SOLID_FOREGROUND);
        return this;
    }

    /**
     * Sets the global font for cells.
     *
     * @param color      The font color.
     * @param fontSize   The font size. -1 indicates default size.
     * @param fontName   The font name. {@code null} indicates default font.
     * @param ignoreHead {@code true} to skip header style, {@code false} otherwise.
     * @return This {@code DefaultStyleSet} instance, for chaining.
     */
    public DefaultStyleSet setFont(
            final short color,
            final short fontSize,
            final String fontName,
            final boolean ignoreHead) {
        final Font font = StyleKit.createFont(this.workbook, color, fontSize, fontName);
        return setFont(font, ignoreHead);
    }

    /**
     * Sets the global font for cells.
     *
     * @param font       The font, which can be created using
     *                   {@link StyleKit#createFont(Workbook, short, short, String)}.
     * @param ignoreHead {@code true} to skip header style, {@code false} otherwise.
     * @return This {@code DefaultStyleSet} instance, for chaining.
     */
    public DefaultStyleSet setFont(final Font font, final boolean ignoreHead) {
        if (!ignoreHead) {
            this.headCellStyle.setFont(font);
        }
        this.cellStyle.setFont(font);
        this.cellStyleForNumber.setFont(font);
        this.cellStyleForDate.setFont(font);
        this.cellStyleForHyperlink.setFont(font);
        return this;
    }

    /**
     * Sets text wrapping for cells.
     *
     * @return This {@code DefaultStyleSet} instance, for chaining.
     */
    public DefaultStyleSet setWrapText() {
        this.cellStyle.setWrapText(true);
        this.cellStyleForNumber.setWrapText(true);
        this.cellStyleForDate.setWrapText(true);
        this.cellStyleForHyperlink.setWrapText(true);
        return this;
    }

}
