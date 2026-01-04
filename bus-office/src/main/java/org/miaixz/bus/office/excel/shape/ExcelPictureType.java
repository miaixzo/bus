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

import org.apache.poi.ss.usermodel.Workbook;
import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Enumeration of picture types supported by Excel.
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
public enum ExcelPictureType {

    /**
     * Extended Windows Metafile format.
     */
    EMF(Workbook.PICTURE_TYPE_EMF),

    /**
     * Windows Metafile format.
     */
    WMF(Workbook.PICTURE_TYPE_WMF),

    /**
     * Macintosh PICT format.
     */
    PICT(Workbook.PICTURE_TYPE_PICT),

    /**
     * JPEG format.
     */
    JPEG(Workbook.PICTURE_TYPE_JPEG),

    /**
     * PNG format.
     */
    PNG(Workbook.PICTURE_TYPE_PNG),

    /**
     * Device Independent Bitmap format.
     */
    DIB(Workbook.PICTURE_TYPE_DIB);

    /**
     * The integer code representing the picture type.
     */
    private final int value;

    /**
     * Constructs an {@code ExcelPictureType} enum with the specified type code.
     *
     * @param value The integer code representing the picture type.
     */
    ExcelPictureType(final int value) {
        this.value = value;
    }

    /**
     * Gets the {@code ExcelPictureType} based on the image file's extension.
     *
     * @param imgFile The image file.
     * @return The corresponding {@code ExcelPictureType}, defaults to {@link #PNG} if the type is unknown.
     */
    public static ExcelPictureType getType(final File imgFile) {
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

        // Default format.
        return PNG;
    }

    /**
     * Gets the integer code representing this picture type.
     *
     * @return The type code.
     */
    public int getValue() {
        return this.value;
    }

}
