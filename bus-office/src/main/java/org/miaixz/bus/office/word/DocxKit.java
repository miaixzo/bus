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
