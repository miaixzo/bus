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
package org.miaixz.bus.office.excel;

import java.io.File;
import java.util.List;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.miaixz.bus.core.center.map.multi.ListValueMap;
import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;

/**
 * Excel支持的图片类型枚举
 *
 * 
 * @see Workbook#PICTURE_TYPE_EMF
 * @see Workbook#PICTURE_TYPE_WMF
 * @see Workbook#PICTURE_TYPE_PICT
 * @see Workbook#PICTURE_TYPE_JPEG
 * @see Workbook#PICTURE_TYPE_PNG
 * @see Workbook#PICTURE_TYPE_DIB
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ExcelImgType {
    /**
     * Extended windows meta file
     */
    EMF(Workbook.PICTURE_TYPE_EMF),

    /**
     * Windows Meta File
     */
    WMF(Workbook.PICTURE_TYPE_WMF),

    /**
     * Mac PICT format
     */
    PICT(Workbook.PICTURE_TYPE_PICT),

    /**
     * JPEG format
     */
    JPEG(Workbook.PICTURE_TYPE_JPEG),

    /**
     * PNG format
     */
    PNG(Workbook.PICTURE_TYPE_PNG),

    /**
     * Device independent bitmap
     */
    DIB(Workbook.PICTURE_TYPE_DIB);

    private final int value;

    /**
     * 构造
     *
     * @param value 类型编码
     */
    ExcelImgType(final int value) {
        this.value = value;
    }

    /**
     * 获取图片类型
     *
     * @param imgFile 图片文件
     * @return 图片类型，默认PNG
     */
    public static ExcelImgType getType(final File imgFile) {
        final String type = FileType.getType(imgFile);
        if (StringKit.equalsAnyIgnoreCase(type, "jpg", "jpeg")) {
            return JPEG;
        } else if (StringKit.equalsAnyIgnoreCase(type, "emf")) {
            return EMF;
        } else if (StringKit.equalsAnyIgnoreCase(type, "wmf")) {
            return WMF;
        } else if (StringKit.equalsAnyIgnoreCase(type, "pict")) {
            return PICT;
        } else if (StringKit.equalsAnyIgnoreCase(type, "dib")) {
            return DIB;
        }

        // 默认格式
        return PNG;
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
            throw new IllegalArgumentException(
                    StringKit.format("Workbook type [{}] is not supported!", workbook.getClass()));
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
                    picMap.putValue(StringKit.format("{}_{}", anchor.getRow1(), anchor.getCol1()),
                            pictures.get(pictureIndex));
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
                        sheetIndexPicMap.putValue(StringKit.format("{}_{}", ctMarker.getRow(), ctMarker.getCol()),
                                pic.getPictureData());
                    }
                }
            }
        }
        return sheetIndexPicMap;
    }

    /**
     * 获取类型编码
     *
     * @return 编码
     */
    public int getValue() {
        return this.value;
    }

}
