/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.office.excel;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

/**
 * 样式集合,此样式集合汇集了整个工作簿的样式,用于减少样式的创建和冗余
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StyleSet implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 标题样式
     */
    protected final CellStyle headCellStyle;
    /**
     * 默认样式
     */
    protected final CellStyle cellStyle;
    /**
     * 默认数字样式
     */
    protected final CellStyle cellStyleForNumber;
    /**
     * 默认日期样式
     */
    protected final CellStyle cellStyleForDate;
    /**
     * 默认链接样式
     */
    protected final CellStyle cellStyleForHyperlink;
    /**
     * 工作簿引用
     */
    private final Workbook workbook;

    /**
     * 构造
     *
     * @param workbook 工作簿
     */
    public StyleSet(Workbook workbook) {
        this.workbook = workbook;
        this.headCellStyle = StyleKit.createHeadCellStyle(workbook);
        this.cellStyle = StyleKit.createDefaultCellStyle(workbook);

        // 默认数字格式
        cellStyleForNumber = StyleKit.cloneCellStyle(workbook, this.cellStyle);
        // 2表示：0.00
        cellStyleForNumber.setDataFormat((short) 2);

        // 默认日期格式
        this.cellStyleForDate = StyleKit.cloneCellStyle(workbook, this.cellStyle);
        // 22表示：m/d/yy h:mm
        this.cellStyleForDate.setDataFormat((short) 22);


        // 默认链接样式
        this.cellStyleForHyperlink = StyleKit.cloneCellStyle(workbook, this.cellStyle);
        final Font font = this.workbook.createFont();
        font.setUnderline((byte) 1);
        font.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
        this.cellStyleForHyperlink.setFont(font);
    }

    /**
     * 获取头部样式，获取后可以定义整体头部样式
     *
     * @return 头部样式
     */
    public CellStyle getHeadCellStyle() {
        return this.headCellStyle;
    }

    /**
     * 获取常规单元格样式，获取后可以定义整体头部样式
     *
     * @return 常规单元格样式
     */
    public CellStyle getCellStyle() {
        return this.cellStyle;
    }

    /**
     * 获取数字（带小数点）单元格样式，获取后可以定义整体数字样式
     *
     * @return 数字（带小数点）单元格样式
     */
    public CellStyle getCellStyleForNumber() {
        return this.cellStyleForNumber;
    }

    /**
     * 获取日期单元格样式，获取后可以定义整体日期样式
     *
     * @return 日期单元格样式
     */
    public CellStyle getCellStyleForDate() {
        return this.cellStyleForDate;
    }

    /**
     * 获取链接单元格样式，获取后可以定义整体链接样式
     *
     * @return 链接单元格样式
     */
    public CellStyle getCellStyleForHyperlink() {
        return this.cellStyleForHyperlink;
    }

    /**
     * 定义所有单元格的边框类型
     *
     * @param borderSize 边框粗细{@link BorderStyle}枚举
     * @param colorIndex 颜色的short值
     * @return this
     */
    public StyleSet setBorder(BorderStyle borderSize, IndexedColors colorIndex) {
        StyleKit.setBorder(this.headCellStyle, borderSize, colorIndex);
        StyleKit.setBorder(this.cellStyle, borderSize, colorIndex);
        StyleKit.setBorder(this.cellStyleForNumber, borderSize, colorIndex);
        StyleKit.setBorder(this.cellStyleForDate, borderSize, colorIndex);
        StyleKit.setBorder(this.cellStyleForHyperlink, borderSize, colorIndex);
        return this;
    }

    /**
     * 设置cell文本对齐样式
     *
     * @param halign 横向位置
     * @param valign 纵向位置
     * @return this
     */
    public StyleSet setAlign(HorizontalAlignment halign, VerticalAlignment valign) {
        StyleKit.setAlign(this.headCellStyle, halign, valign);
        StyleKit.setAlign(this.cellStyle, halign, valign);
        StyleKit.setAlign(this.cellStyleForNumber, halign, valign);
        StyleKit.setAlign(this.cellStyleForDate, halign, valign);
        StyleKit.setAlign(this.cellStyleForHyperlink, halign, valign);
        return this;
    }

    /**
     * 设置单元格背景样式
     *
     * @param backgroundColor 背景色
     * @param withHeadCell    是否也定义头部样式
     * @return this
     */
    public StyleSet setBackgroundColor(IndexedColors backgroundColor, boolean withHeadCell) {
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
     * 设置全局字体
     *
     * @param color      字体颜色
     * @param fontSize   字体大小，-1表示默认大小
     * @param fontName   字体名，null表示默认字体
     * @param ignoreHead 是否跳过头部样式
     * @return this
     */
    public StyleSet setFont(short color, short fontSize, String fontName, boolean ignoreHead) {
        final Font font = StyleKit.createFont(this.workbook, color, fontSize, fontName);
        return setFont(font, ignoreHead);
    }

    /**
     * 设置全局字体
     *
     * @param font       字体，可以通过{@link StyleKit#createFont(Workbook, short, short, String)}创建
     * @param ignoreHead 是否跳过头部样式
     * @return this
     */
    public StyleSet setFont(Font font, boolean ignoreHead) {
        if (false == ignoreHead) {
            this.headCellStyle.setFont(font);
        }
        this.cellStyle.setFont(font);
        this.cellStyleForNumber.setFont(font);
        this.cellStyleForDate.setFont(font);
        this.cellStyleForHyperlink.setFont(font);
        return this;
    }

    /**
     * 设置单元格文本自动换行
     *
     * @return this
     */
    public StyleSet setWrapText() {
        this.cellStyle.setWrapText(true);
        this.cellStyleForNumber.setWrapText(true);
        this.cellStyleForDate.setWrapText(true);
        this.cellStyleForHyperlink.setWrapText(true);
        return this;
    }

    /**
     * 获取值对应的公共单元格样式
     *
     * @param value    值
     * @param isHeader 是否为标题单元格
     * @return 值对应单元格样式
     */
    public CellStyle getStyleByValueType(Object value, boolean isHeader) {
        CellStyle style = null;

        if (isHeader && null != this.headCellStyle) {
            style = headCellStyle;
        } else if (null != cellStyle) {
            style = cellStyle;
        }

        if (value instanceof Date
                || value instanceof TemporalAccessor
                || value instanceof Calendar) {
            // 日期单独定义格式
            if (null != this.cellStyleForDate) {
                style = this.cellStyleForDate;
            }
        } else if (value instanceof Number) {
            // 数字单独定义格式
            if ((value instanceof Double || value instanceof Float || value instanceof BigDecimal) &&
                    null != this.cellStyleForNumber) {
                style = this.cellStyleForNumber;
            }
        } else if (value instanceof Hyperlink) {
            // 自定义超链接样式
            if (null != this.cellStyleForHyperlink) {
                style = this.cellStyleForHyperlink;
            }
        }

        return style;
    }

}
