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
