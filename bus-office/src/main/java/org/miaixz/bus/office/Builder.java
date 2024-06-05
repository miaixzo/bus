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
package org.miaixz.bus.office;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.miaixz.bus.core.center.map.multi.ListValueMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 为office提供辅助功能
 * Excel中日期判断、读取、处理等补充工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * 没有引入POI的错误消息
     */
    public static final String NO_POI_ERROR_MSG = "You need to add dependency of 'poi-ooxml' to your project, and version >= 4.1.2";

    /**
     * 某些特殊的自定义日期格式
     */
    private static final int[] CUSTOM_FORMATS = new int[]{28, 30, 31, 32, 33, 55, 56, 57, 58};

    /**
     * 是否日期格式
     *
     * @param cell 单元格
     * @return 是否日期格式
     */
    public static boolean isDateFormat(final Cell cell) {
        return isDateFormat(cell, null);
    }

    /**
     * 判断是否日期格式
     *
     * @param cell        单元格
     * @param cfEvaluator {@link ConditionalFormattingEvaluator}
     * @return 是否日期格式
     */
    public static boolean isDateFormat(final Cell cell, final ConditionalFormattingEvaluator cfEvaluator) {
        final ExcelNumberFormat nf = ExcelNumberFormat.from(cell, cfEvaluator);
        return isDateFormat(nf);
    }

    /**
     * 判断是否日期格式
     *
     * @param numFmt {@link ExcelNumberFormat}
     * @return 是否日期格式
     */
    public static boolean isDateFormat(final ExcelNumberFormat numFmt) {
        return isDateFormat(numFmt.getIdx(), numFmt.getFormat());
    }

    /**
     * 判断日期格式
     *
     * @param formatIndex  格式索引，一般用于内建格式
     * @param formatString 格式字符串
     * @return 是否为日期格式
     */
    public static boolean isDateFormat(final int formatIndex, final String formatString) {
        if (ArrayKit.contains(CUSTOM_FORMATS, formatIndex)) {
            return true;
        }

        // 自定义格式判断
        if (StringKit.isNotEmpty(formatString) &&
                StringKit.containsAny(formatString, "周", "星期", "aa")) {
            // aa  -> 周一
            // aaa -> 星期一
            return true;
        }

        return org.apache.poi.ss.usermodel.DateUtil.isADateFormat(formatIndex, formatString);
    }

    /**
     * 是否为XLS格式的Excel文件（HSSF）
     * XLS文件主要用于Excel 97~2003创建
     * 此方法会自动调用{@link InputStream#reset()}方法
     *
     * @param in excel输入流
     * @return 是否为XLS格式的Excel文件（HSSF）
     */
    public static boolean isXls(final InputStream in) {
        return FileMagic.OLE2 == getFileMagic(in);
    }

    /**
     * 是否为XLSX格式的Excel文件（XSSF）
     * XLSX文件主要用于Excel 2007+创建
     * 此方法会自动调用{@link InputStream#reset()}方法
     *
     * @param in excel输入流
     * @return 是否为XLSX格式的Excel文件（XSSF）
     */
    public static boolean isXlsx(final InputStream in) {
        return FileMagic.OOXML == getFileMagic(in);
    }

    /**
     * 是否为XLSX格式的Excel文件（XSSF）
     * XLSX文件主要用于Excel 2007+创建
     *
     * @param file excel文件
     * @return 是否为XLSX格式的Excel文件（XSSF）
     */
    public static boolean isXlsx(final File file) {
        try {
            return FileMagic.valueOf(file) == FileMagic.OOXML;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * {@link java.io.PushbackInputStream}
     * PushbackInputStream的markSupported()为false，并不支持mark和reset
     * 如果强转成PushbackInputStream在调用FileMagic.valueOf(inputStream)时会报错
     * {@link FileMagic}
     * 报错内容：getFileMagic() only operates on streams which support mark(int)
     * 此处修改成 final InputStream in = FileMagic.prepareToCheckMagic(in)
     *
     * @param in {@link InputStream}
     */
    private static FileMagic getFileMagic(InputStream in) {
        final FileMagic magic;
        in = FileMagic.prepareToCheckMagic(in);
        try {
            magic = FileMagic.valueOf(in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return magic;
    }

    /**
     * 获取工作簿指定sheet中图片列表
     *
     * @param workbook   工作簿{@link Workbook}
     * @param sheetIndex sheet的索引
     * @return 图片映射，键格式：行_列，值：{@link PictureData}
     */
    public static ListValueMap<String, PictureData> getPicMap(final Workbook workbook, int sheetIndex) {
        Assert.notNull(workbook, "Workbook must be not null !");
        if (sheetIndex < 0) {
            sheetIndex = 0;
        }

        if (workbook instanceof HSSFWorkbook) {
            return getPicMapXls((HSSFWorkbook) workbook, sheetIndex);
        } else if (workbook instanceof XSSFWorkbook) {
            return getPicMapXlsx((XSSFWorkbook) workbook, sheetIndex);
        } else {
            throw new IllegalArgumentException(StringKit.format("Workbook type [{}] is not supported!", workbook.getClass()));
        }
    }

    /**
     * 获取XLS工作簿指定sheet中图片列表
     *
     * @param workbook   工作簿{@link Workbook}
     * @param sheetIndex sheet的索引
     * @return 图片映射，键格式：行_列，值：{@link PictureData}
     */
    private static ListValueMap<String, PictureData> getPicMapXls(final HSSFWorkbook workbook, final int sheetIndex) {
        final ListValueMap<String, PictureData> picMap = new ListValueMap<>();
        final List<HSSFPictureData> pictures = workbook.getAllPictures();
        if (CollKit.isNotEmpty(pictures)) {
            final HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
            HSSFClientAnchor anchor;
            int pictureIndex;
            for (final HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
                if (shape instanceof HSSFPicture) {
                    pictureIndex = ((HSSFPicture) shape).getPictureIndex() - 1;
                    anchor = (HSSFClientAnchor) shape.getAnchor();
                    picMap.putValue(StringKit.format("{}_{}", anchor.getRow1(), anchor.getCol1()), pictures.get(pictureIndex));
                }
            }
        }
        return picMap;
    }

    /**
     * 获取XLSX工作簿指定sheet中图片列表
     *
     * @param workbook   工作簿{@link Workbook}
     * @param sheetIndex sheet的索引
     * @return 图片映射，键格式：行_列，值：{@link PictureData}
     */
    private static ListValueMap<String, PictureData> getPicMapXlsx(final XSSFWorkbook workbook, final int sheetIndex) {
        final ListValueMap<String, PictureData> sheetIndexPicMap = new ListValueMap<>();
        final XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        XSSFDrawing drawing;
        for (final POIXMLDocumentPart dr : sheet.getRelations()) {
            if (dr instanceof XSSFDrawing) {
                drawing = (XSSFDrawing) dr;
                final List<XSSFShape> shapes = drawing.getShapes();
                XSSFPicture pic;
                CTMarker ctMarker;
                for (final XSSFShape shape : shapes) {
                    if (shape instanceof XSSFPicture) {
                        pic = (XSSFPicture) shape;
                        ctMarker = pic.getPreferredSize().getFrom();
                        sheetIndexPicMap.putValue(StringKit.format("{}_{}", ctMarker.getRow(), ctMarker.getCol()), pic.getPictureData());
                    }
                    // 其他类似于图表等忽略
                }
            }
        }
        return sheetIndexPicMap;
    }

}
