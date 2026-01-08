/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * A {@link FileTypeDetector} implementation for detecting DICOM image files. It checks for DICOM Part 10 header,
 * Implicit VR Little Endian, and Explicit VR transfer syntaxes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageFileDetector extends FileTypeDetector {

    /**
     * Checks if the given byte array indicates a DICOM Part 10 file format. A DICOM Part 10 file typically has "DICM"
     * at offset 128.
     *
     * @param b134 The byte array containing the first 134 bytes of the file.
     * @param rlen The number of bytes read into the array.
     * @return {@code true} if it's a DICOM Part 10 file, {@code false} otherwise.
     */
    private static boolean isPart10(byte[] b134, int rlen) {
        return rlen == 134 && b134[128] == 'D' && b134[129] == 'I' && b134[130] == 'C' && b134[131] == 'M'
                && b134[132] == 2 && b134[133] == 0;
    }

    /**
     * Checks if the given byte array indicates an Implicit VR Little Endian transfer syntax. This is determined by
     * checking the tag and value length of the first element.
     *
     * @param b134 The byte array containing the first 134 bytes of the file.
     * @param rlen The number of bytes read into the array.
     * @return {@code true} if it's Implicit VR Little Endian, {@code false} otherwise.
     */
    private static boolean isIVR_LE(byte[] b134, int rlen) {
        int tag = ByteKit.bytesToTagLE(b134, 0);
        int vlen = ByteKit.bytesToIntLE(b134, 4);
        return Tag.isGroupLength(tag) ? vlen == 4
                : (ElementDictionary.getStandardElementDictionary().vrOf(tag) != VR.UN && (16 + vlen) <= rlen);
    }

    /**
     * Checks if the given byte array indicates an Explicit VR transfer syntax. This is determined by attempting to
     * parse the VR from the byte array and comparing it with the expected VR for the tag.
     *
     * @param b134 The byte array containing the first 134 bytes of the file.
     * @param rlen The number of bytes read into the array.
     * @return {@code true} if it's Explicit VR, {@code false} otherwise.
     */
    private static boolean isEVR(byte[] b134, int rlen) {
        int tagLE = ByteKit.bytesToTagLE(b134, 0);
        int tagBE = ByteKit.bytesToTagBE(b134, 0);
        VR vr = VR.valueOf(ByteKit.bytesToVR(b134, 4));
        return vr != null && vr == ElementDictionary.getStandardElementDictionary()
                .vrOf(tagLE >= 0 && tagLE < tagBE ? tagLE : tagBE);
    }

    /**
     * Probes the content type of the given file path to determine if it is a DICOM file. It reads the first 134 bytes
     * and checks for DICOM Part 10 header, Implicit VR Little Endian, or Explicit VR characteristics.
     *
     * @param path The path to the file.
     * @return The MIME type {@code MediaType.APPLICATION_DICOM} if the file is detected as DICOM, otherwise
     *         {@code null}.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public String probeContentType(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            byte[] b134 = new byte[134];
            int rlen = StreamKit.readAvailable(in, b134, 0, 134);
            return rlen >= 8 && (isPart10(b134, rlen) || isIVR_LE(b134, rlen) || isEVR(b134, rlen))
                    ? MediaType.APPLICATION_DICOM
                    : null;
        }
    }

}
