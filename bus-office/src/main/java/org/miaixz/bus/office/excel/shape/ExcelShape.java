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
package org.miaixz.bus.office.excel.shape;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.StreamKit;

/**
 * Utility class for Excel pictures (shapes).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ExcelShape {

    /**
     * Writes picture data to a file.
     *
     * @param pic  The {@link Picture} object containing the image data.
     * @param file The target file to write the image to.
     */
    public static void writePicTo(final Picture pic, final File file) {
        writePicTo(pic.getPictureData(), file);
    }

    /**
     * Writes picture data to a file.
     *
     * @param pic  The {@link PictureData} object containing the image data.
     * @param file The target file to write the image to.
     */
    public static void writePicTo(final PictureData pic, final File file) {
        FileKit.writeBytes(pic.getData(), file);
    }

    /**
     * Gets a list of all pictures in the workbook.
     *
     * @param workbook The workbook {@link Workbook}.
     * @return A list of {@link PictureData} objects.
     */
    public static List<? extends PictureData> getAllPictures(final Workbook workbook) {
        return workbook.getAllPictures();
    }

    /**
     * Gets a list of pictures drawn in a specific sheet of the workbook.
     *
     * @param workbook   The workbook {@link Workbook}.
     * @param sheetIndex The 0-based index of the sheet.
     * @return A list of {@link Picture} objects.
     * @throws NullPointerException if {@code workbook} is {@code null}.
     */
    public static List<Picture> getShapePics(final Workbook workbook, int sheetIndex) {
        Assert.notNull(workbook, "Workbook must be not null !");
        if (sheetIndex < 0) {
            sheetIndex = 0;
        }

        return getShapePics(workbook.getSheetAt(sheetIndex));
    }

    /**
     * Gets a list of pictures drawn in a specific sheet. The result includes {@link Picture#getClientAnchor()} for
     * position information and {@link Picture#getPictureData()} for image data.
     *
     * @param sheet The worksheet {@link Sheet}.
     * @return A list of {@link Picture} objects.
     * @throws NullPointerException if {@code sheet} is {@code null}.
     */
    public static List<Picture> getShapePics(final Sheet sheet) {
        Assert.notNull(sheet, "Sheet must be not null !");

        final Drawing<?> drawing = sheet.getDrawingPatriarch();
        if (null == drawing) {
            return ListKit.empty();
        }

        return StreamKit.of(drawing).filter(shape -> shape instanceof Picture).map(shape -> (Picture) shape)
                .collect(Collectors.toList());
    }

}
