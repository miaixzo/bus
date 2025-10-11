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
 * @since Java 17+
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
