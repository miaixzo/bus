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
package org.miaixz.bus.office.excel.writer;

import java.awt.Color;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.miaixz.bus.office.excel.SimpleAnchor;
import org.miaixz.bus.office.excel.shape.ExcelPictureType;
import org.miaixz.bus.office.excel.style.ShapeConfig;

/**
 * Excel绘制工具类 用于辅助写出指定的图形
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelDrawing {

    /**
     * 写出图片，本方法只是将数据写入Workbook中的Sheet，并不写出到文件 添加图片到当前sheet中
     *
     * @param sheet        {@link Sheet}
     * @param pictureData  数据bytes
     * @param imgType      图片类型，对应poi中Workbook类中的图片类型2-7变量
     * @param clientAnchor 图片的位置和大小信息
     * 
     */
    public static void drawingPicture(final Sheet sheet, final byte[] pictureData, final ExcelPictureType imgType,
            final SimpleAnchor clientAnchor) {
        final Drawing<?> patriarch = sheet.createDrawingPatriarch();
        final Workbook workbook = sheet.getWorkbook();
        final ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        clientAnchor.copyTo(anchor);

        patriarch.createPicture(anchor, workbook.addPicture(pictureData, imgType.getValue()));
    }

    /**
     * 绘制简单形状
     *
     * @param sheet        {@link Sheet}
     * @param clientAnchor 绘制区域信息
     * @param shapeConfig  形状配置，包括形状类型、线条样式、线条宽度、线条颜色、填充颜色等
     */
    public static void drawingSimpleShape(final Sheet sheet, final SimpleAnchor clientAnchor, ShapeConfig shapeConfig) {
        final Drawing<?> patriarch = sheet.createDrawingPatriarch();
        final ClientAnchor anchor = sheet.getWorkbook().getCreationHelper().createClientAnchor();
        clientAnchor.copyTo(anchor);

        if (null == shapeConfig) {
            shapeConfig = ShapeConfig.of();
        }
        final Color lineColor = shapeConfig.getLineColor();
        if (patriarch instanceof HSSFPatriarch) {
            final HSSFSimpleShape simpleShape = ((HSSFPatriarch) patriarch)
                    .createSimpleShape((HSSFClientAnchor) anchor);
            simpleShape.setShapeType(shapeConfig.getShapeType().ooxmlId);
            simpleShape.setLineStyle(shapeConfig.getLineStyle().getValue());
            simpleShape.setLineWidth(shapeConfig.getLineWidth());
            if (null != lineColor) {
                simpleShape.setLineStyleColor(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue());
            }
        } else if (patriarch instanceof XSSFDrawing) {
            final XSSFSimpleShape simpleShape = ((XSSFDrawing) patriarch).createSimpleShape((XSSFClientAnchor) anchor);
            simpleShape.setShapeType(shapeConfig.getShapeType().ooxmlId);
            simpleShape.setLineStyle(shapeConfig.getLineStyle().getValue());
            simpleShape.setLineWidth(shapeConfig.getLineWidth());
            if (null != lineColor) {
                simpleShape.setLineStyleColor(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue());
            }
        } else {
            throw new UnsupportedOperationException("Unsupported patriarch type: " + patriarch.getClass().getName());
        }
    }

    /**
     * 添加批注
     *
     * @param cell         {@link Cell}
     * @param clientAnchor 绘制区域信息
     * @param content      内容
     */
    public static void drawingCellComment(final Cell cell, final SimpleAnchor clientAnchor, final String content) {
        final Sheet sheet = cell.getSheet();
        final Drawing<?> patriarch = sheet.createDrawingPatriarch();
        final Workbook workbook = sheet.getWorkbook();
        final ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        clientAnchor.copyTo(anchor);

        final RichTextString richTextString = workbook.getCreationHelper().createRichTextString(content);
        final Comment cellComment = patriarch.createCellComment(anchor);
        cellComment.setString(richTextString);

        cell.setCellComment(cellComment);
    }

}
