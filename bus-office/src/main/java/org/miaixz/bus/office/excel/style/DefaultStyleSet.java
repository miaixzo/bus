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
 * 默认样式集合，定义了标题、数字、日期等默认样式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultStyleSet implements StyleSet, Serializable {

    @Serial
    private static final long serialVersionUID = 2852270697076L;

    /**
     * 工作簿引用
     */
    private final Workbook workbook;
    /**
     * 标题样式
     */
    private final CellStyle headCellStyle;
    /**
     * 默认样式
     */
    private final CellStyle cellStyle;
    /**
     * 默认数字样式
     */
    private final CellStyle cellStyleForNumber;
    /**
     * 默认日期样式
     */
    private final CellStyle cellStyleForDate;
    /**
     * 默认链接样式
     */
    private final CellStyle cellStyleForHyperlink;

    /**
     * 构造
     *
     * @param workbook 工作簿
     */
    public DefaultStyleSet(final Workbook workbook) {
        this.workbook = workbook;
        this.headCellStyle = StyleKit.createHeadCellStyle(workbook);
        this.cellStyle = StyleKit.createDefaultCellStyle(workbook);

        // 默认数字格式
        cellStyleForNumber = StyleKit.cloneCellStyle(workbook, this.cellStyle);
        // 0表示：General
        cellStyleForNumber.setDataFormat((short) 0);

        // 默认日期格式
        this.cellStyleForDate = StyleKit.cloneCellStyle(workbook, this.cellStyle);
        // 22表示：m/d/yy h:mm
        this.cellStyleForDate.setDataFormat((short) 22);

        // 默认链接样式
        this.cellStyleForHyperlink = StyleKit.cloneCellStyle(workbook, this.cellStyle);
        final Font font = workbook.createFont();
        font.setUnderline((byte) 1);
        font.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
        this.cellStyleForHyperlink.setFont(font);
    }

    @Override
    public CellStyle getStyleFor(final CellReference reference, final Object cellValue, final boolean isHeader) {
        CellStyle style = null;

        if (isHeader && null != this.headCellStyle) {
            style = headCellStyle;
        } else if (null != cellStyle) {
            style = cellStyle;
        }

        if (cellValue instanceof Date || cellValue instanceof TemporalAccessor || cellValue instanceof Calendar) {
            // 日期单独定义格式
            if (null != this.cellStyleForDate) {
                style = this.cellStyleForDate;
            }
        } else if (cellValue instanceof Number) {
            // 数字单独定义格式
            if ((cellValue instanceof Double || cellValue instanceof Float || cellValue instanceof BigDecimal)
                    && null != this.cellStyleForNumber) {
                style = this.cellStyleForNumber;
            }
        } else if (cellValue instanceof Hyperlink) {
            // 自定义超链接样式
            if (null != this.cellStyleForHyperlink) {
                style = this.cellStyleForHyperlink;
            }
        }

        return style;
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
    public DefaultStyleSet setBorder(final BorderStyle borderSize, final IndexedColors colorIndex) {
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
    public DefaultStyleSet setAlign(final HorizontalAlignment halign, final VerticalAlignment valign) {
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
     * 设置全局字体
     *
     * @param color      字体颜色
     * @param fontSize   字体大小，-1表示默认大小
     * @param fontName   字体名，null表示默认字体
     * @param ignoreHead 是否跳过头部样式
     * @return this
     */
    public DefaultStyleSet setFont(final short color, final short fontSize, final String fontName,
            final boolean ignoreHead) {
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
     * 设置单元格文本自动换行
     *
     * @return this
     */
    public DefaultStyleSet setWrapText() {
        this.cellStyle.setWrapText(true);
        this.cellStyleForNumber.setWrapText(true);
        this.cellStyleForDate.setWrapText(true);
        this.cellStyleForHyperlink.setWrapText(true);
        return this;
    }

}
