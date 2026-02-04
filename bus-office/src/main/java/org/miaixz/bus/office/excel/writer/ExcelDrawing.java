/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
 * Excel drawing utility class, used to assist in writing specified graphics.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelDrawing {

    /**
     * Writes a picture to the current sheet in the workbook. This method only writes data to the Workbook's Sheet and
     * does not write to a file.
     *
     * @param sheet        The {@link Sheet} to add the picture to.
     * @param pictureData  The picture data as a byte array.
     * @param imgType      The picture type, corresponding to the picture type constants in the POI {@link Workbook}
     *                     class (e.g., {@code PICTURE_TYPE_PNG}).
     * @param clientAnchor The position and size information of the picture.
     */
    public static void drawingPicture(
            final Sheet sheet,
            final byte[] pictureData,
            final ExcelPictureType imgType,
            final SimpleAnchor clientAnchor) {
        final Drawing<?> patriarch = sheet.createDrawingPatriarch();
        final Workbook workbook = sheet.getWorkbook();
        final ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        clientAnchor.copyTo(anchor);

        patriarch.createPicture(anchor, workbook.addPicture(pictureData, imgType.getValue()));
    }

    /**
     * Draws a simple shape on the Excel sheet.
     *
     * @param sheet        The {@link Sheet} to draw on.
     * @param clientAnchor The drawing area information.
     * @param shapeConfig  The shape configuration, including shape type, line style, line width, line color, fill
     *                     color, etc.
     * @throws UnsupportedOperationException if the patriarch type is not supported (e.g., not HSSFPatriarch or
     *                                       XSSFDrawing).
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
     * Adds a cell comment to the specified cell.
     *
     * @param cell         The {@link Cell} to add the comment to.
     * @param clientAnchor The drawing area information for the comment.
     * @param content      The content of the comment.
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
