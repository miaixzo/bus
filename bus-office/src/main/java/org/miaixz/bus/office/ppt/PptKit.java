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
package org.miaixz.bus.office.ppt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;

/**
 * Utility class for PowerPoint (PPTX) operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PptKit {

    /**
     * Creates an {@link XMLSlideShow} instance. If the file already exists, it will be opened; otherwise, a new
     * presentation will be created.
     *
     * @param file The PPTX file.
     * @return A new or existing {@link XMLSlideShow}.
     * @throws InternalException if an {@link InvalidFormatException} or {@link IOException} occurs.
     */
    public static XMLSlideShow create(final File file) {
        try {
            return FileKit.exists(file) ? new XMLSlideShow(OPCPackage.open(file)) : new XMLSlideShow();
        } catch (final InvalidFormatException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates an {@link XMLSlideShow} from an input stream.
     *
     * @param in The input stream of the PPTX file.
     * @return A new {@link XMLSlideShow}.
     * @throws InternalException if an {@link IOException} occurs.
     */
    public static XMLSlideShow create(final InputStream in) {
        try {
            return new XMLSlideShow(in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the {@link PictureData.PictureType} enum for a given file name.
     *
     * @param fileName The name of the file.
     * @return The corresponding {@link PictureData.PictureType}. Defaults to {@link PictureData.PictureType#JPEG} if
     *         the type is unknown.
     */
    public static PictureData.PictureType toPictureType(final String fileName) {
        String extName = FileName.extName(fileName).toUpperCase();
        if ("JPG".equals(extName)) {
            extName = "JPEG";
        }

        try {
            return PictureData.PictureType.valueOf(extName);
        } catch (final IllegalArgumentException e) {
            return PictureData.PictureType.JPEG;
        }
    }

    /**
     * Creates a PowerPoint 07 format writer.
     *
     * @return A new {@link PptWriter} instance.
     */
    public static PptWriter getWriter() {
        return new PptWriter();
    }

    /**
     * Creates a PowerPoint 07 format writer for a specific destination file.
     *
     * @param destFile The destination file for the PowerPoint presentation.
     * @return A new {@link PptWriter} instance.
     */
    public static PptWriter getWriter(final File destFile) {
        return new PptWriter(destFile);
    }

    /**
     * Creates a PowerPoint reader from a file.
     *
     * @param file The PPTX file to read.
     * @return A new {@link PptReader} instance.
     */
    public static PptReader getReader(final File file) {
        return new PptReader(file);
    }

    /**
     * Creates a PowerPoint reader from an input stream.
     *
     * @param in The input stream of the PPTX file.
     * @return A new {@link PptReader} instance.
     */
    public static PptReader getReader(final InputStream in) {
        return new PptReader(in);
    }

}
