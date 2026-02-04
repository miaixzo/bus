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
package org.miaixz.bus.office.word;

import java.io.File;
import java.io.IOException;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;

/**
 * Utility class for Word Document (DOCX) operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DocxKit {

    /**
     * Creates an {@link XWPFDocument} instance. If the file already exists, it will be opened; otherwise, a new
     * document will be created.
     *
     * @param file The DOCX file.
     * @return A new or existing {@link XWPFDocument}.
     * @throws InternalException if an {@link InvalidFormatException} or {@link IOException} occurs.
     */
    public static XWPFDocument create(final File file) {
        try {
            return FileKit.exists(file) ? new XWPFDocument(OPCPackage.open(file)) : new XWPFDocument();
        } catch (final InvalidFormatException | IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the {@link PictureType} enum for a given file name.
     *
     * @param fileName The name of the file.
     * @return The corresponding {@link PictureType} enum. Defaults to {@link PictureType#JPEG} if the type is unknown.
     */
    public static PictureType getType(final String fileName) {
        String extName = FileName.extName(fileName).toUpperCase();
        if ("JPG".equals(extName)) {
            extName = "JPEG";
        }

        PictureType picType;
        try {
            picType = PictureType.valueOf(extName);
        } catch (final IllegalArgumentException e) {
            // Default value
            picType = PictureType.JPEG;
        }
        return picType;
    }

    /**
     * Creates a Word 07 format writer.
     *
     * @return A new {@link Word07Writer} instance.
     */
    public static Word07Writer getWriter() {
        return new Word07Writer();
    }

    /**
     * Creates a Word 07 format writer for a specific destination file.
     *
     * @param destFile The destination file for the Word document.
     * @return A new {@link Word07Writer} instance.
     */
    public static Word07Writer getWriter(final File destFile) {
        return new Word07Writer(destFile);
    }

}
