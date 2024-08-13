/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageFileDetector extends FileTypeDetector {

    private static boolean isPart10(byte[] b134, int rlen) {
        return rlen == 134 && b134[128] == 'D' && b134[129] == 'I' && b134[130] == 'C' && b134[131] == 'M'
                && b134[132] == 2 && b134[133] == 0;
    }

    private static boolean isIVR_LE(byte[] b134, int rlen) {
        int tag = ByteKit.bytesToTagLE(b134, 0);
        int vlen = ByteKit.bytesToIntLE(b134, 4);
        return Tag.isGroupLength(tag) ? vlen == 4
                : (ElementDictionary.getStandardElementDictionary().vrOf(tag) != VR.UN && (16 + vlen) <= rlen);
    }

    private static boolean isEVR(byte[] b134, int rlen) {
        int tagLE = ByteKit.bytesToTagLE(b134, 0);
        int tagBE = ByteKit.bytesToTagBE(b134, 0);
        VR vr = VR.valueOf(ByteKit.bytesToVR(b134, 4));
        return vr != null && vr == ElementDictionary.getStandardElementDictionary()
                .vrOf(tagLE >= 0 && tagLE < tagBE ? tagLE : tagBE);
    }

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
