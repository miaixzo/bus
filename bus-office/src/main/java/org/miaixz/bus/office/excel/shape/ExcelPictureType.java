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
