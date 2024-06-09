/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Excel样式工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Styles {

    /**
     * 克隆新的{@link CellStyle}
     *
     * @param cell      单元格
     * @param cellStyle 被复制的样式
     * @return {@link CellStyle}
     */
    public static CellStyle cloneCellStyle(final Cell cell, final CellStyle cellStyle) {
        return cloneCellStyle(cell.getSheet().getWorkbook(), cellStyle);
    }

    /**
     * 克隆新的{@link CellStyle}
     *
     * @param workbook  工作簿
     * @param cellStyle 被复制的样式
     * @return {@link CellStyle}
     */
    public static CellStyle cloneCellStyle(final Workbook workbook, final CellStyle cellStyle) {
        final CellStyle newCellStyle = createCellStyle(workbook);
        newCellStyle.cloneStyleFrom(cellStyle);
        return newCellStyle;
    }

    /**
     * 设置cell文本对齐样式
     *
     * @param cellStyle {@link CellStyle}
     * @param halign    横向位置
     * @param valign    纵向位置
     * @return {@link CellStyle}
     */
    public static CellStyle setAlign(final CellStyle cellStyle, final HorizontalAlignment halign, final VerticalAlignment valign) {
        cellStyle.setAlignment(halign);
        cellStyle.setVerticalAlignment(valign);
        return cellStyle;
    }

    /**
     * 设置cell的四个边框粗细和颜色
     *
     * @param cellStyle  {@link CellStyle}
     * @param borderSize 边框粗细{@link BorderStyle}枚举
     * @param colorIndex 预定义颜色的short值，见{@link IndexedColors}枚举
     * @return {@link CellStyle}
     */
    public static CellStyle setBorder(final CellStyle cellStyle, final BorderStyle borderSize, final IndexedColors colorIndex) {
        return setBorder(cellStyle, CellBorderStyle.of(borderSize, colorIndex));
    }

    /**
     * 设置cell的四个边框粗细和颜色
     *
     * @param cellStyle       {@link CellStyle}
     * @param cellBorderStyle {@link CellBorderStyle}单元格边框样式和颜色
     *                        }
     * @return {@link CellStyle}
     */
    public static CellStyle setBorder(final CellStyle cellStyle, final CellBorderStyle cellBorderStyle) {
        return cellBorderStyle.setTo(cellStyle);
    }

    /**
     * 给cell设置颜色
     *
     * @param cellStyle   {@link CellStyle}
     * @param color       预定义的背景颜色，见{@link IndexedColors}枚举
     * @param fillPattern 填充方式 {@link FillPatternType}枚举
     * @return {@link CellStyle}
     */
    public static CellStyle setColor(final CellStyle cellStyle, final IndexedColors color, final FillPatternType fillPattern) {
        return setColor(cellStyle, color.index, fillPattern);
    }

    /**
     * 给cell设置颜色（即单元格背景色）
     *
     * @param cellStyle   {@link CellStyle}
     * @param color       预定义的背景颜色，见{@link IndexedColors}枚举
     * @param fillPattern 填充方式 {@link FillPatternType}枚举
     * @return {@link CellStyle}
     */
    public static CellStyle setColor(final CellStyle cellStyle, final short color, final FillPatternType fillPattern) {
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(ObjectKit.defaultIfNull(fillPattern, FillPatternType.SOLID_FOREGROUND));
        return cellStyle;
    }

    /**
     * 给cell设置颜色（即单元格背景色）
     *
     * @param cellStyle   {@link CellStyle}
     * @param color       背景颜色
     * @param fillPattern 填充方式 {@link FillPatternType}枚举
     * @return {@link CellStyle}
     */
    public static CellStyle setColor(final XSSFCellStyle cellStyle, final XSSFColor color, final FillPatternType fillPattern) {
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(ObjectKit.defaultIfNull(fillPattern, FillPatternType.SOLID_FOREGROUND));
        return cellStyle;
    }

    /**
     * 创建字体
     *
     * @param workbook {@link Workbook}
     * @param color    字体颜色
     * @param fontSize 字体大小
     * @param fontName 字体名称，可以为null使用默认字体
     * @return {@link Font}
     */
    public static Font createFont(final Workbook workbook, final short color, final short fontSize, final String fontName) {
        final Font font = workbook.createFont();
        return setFontStyle(font, color, fontSize, fontName);
    }

    /**
     * 设置字体样式
     *
     * @param font     字体{@link Font}
     * @param color    字体颜色
     * @param fontSize 字体大小
     * @param fontName 字体名称，可以为null使用默认字体
     * @return {@link Font}
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
     * 创建单元格样式
     *
     * @param workbook {@link Workbook} 工作簿
     * @return {@link CellStyle}
     * @see Workbook#createCellStyle()
     */
    public static CellStyle createCellStyle(final Workbook workbook) {
        if (null == workbook) {
            return null;
        }
        return workbook.createCellStyle();
    }

    /**
     * 创建默认普通单元格样式
     *
     * <pre>
     * 1. 文字上下左右居中
     * 2. 细边框，黑色
     * </pre>
     *
     * @param workbook {@link Workbook} 工作簿
     * @return {@link CellStyle}
     */
    public static CellStyle createDefaultCellStyle(final Workbook workbook) {
        final CellStyle cellStyle = createCellStyle(workbook);
        setAlign(cellStyle, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        setBorder(cellStyle, BorderStyle.THIN, IndexedColors.BLACK);
        return cellStyle;
    }

    /**
     * 创建默认头部样式
     *
     * @param workbook {@link Workbook} 工作簿
     * @return {@link CellStyle}
     */
    public static CellStyle createHeadCellStyle(final Workbook workbook) {
        final CellStyle cellStyle = createCellStyle(workbook);
        setAlign(cellStyle, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        setBorder(cellStyle, BorderStyle.THIN, IndexedColors.BLACK);
        setColor(cellStyle, IndexedColors.GREY_25_PERCENT, FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    /**
     * 给定样式是否为null（无样式）或默认样式，默认样式为{@code workbook.getCellStyleAt(0)}
     *
     * @param workbook 工作簿
     * @param style    被检查的样式
     * @return 是否为null（无样式）或默认样式
     */
    public static boolean isNullOrDefaultStyle(final Workbook workbook, final CellStyle style) {
        return (null == style) || style.equals(workbook.getCellStyleAt(0));
    }

    /**
     * 创建数据格式并获取格式
     *
     * @param workbook {@link Workbook}
     * @param format   数据格式
     * @return 数据格式
     */
    public static Short getFormat(final Workbook workbook, final String format) {
        final DataFormat dataFormat = workbook.createDataFormat();
        return dataFormat.getFormat(format);
    }

}
