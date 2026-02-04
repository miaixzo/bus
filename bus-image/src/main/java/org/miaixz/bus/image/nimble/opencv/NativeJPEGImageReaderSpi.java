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
package org.miaixz.bus.image.nimble.opencv;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.miaixz.bus.core.Version;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeJPEGImageReaderSpi extends ImageReaderSpi {

    static final String[] NAMES = { "jpeg-cv" };
    static final String[] SUFFIXES = {};
    static final String[] MIMES = {};

    public NativeJPEGImageReaderSpi() {
        super("Miaixz Team", Version._VERSION, NAMES, SUFFIXES, MIMES, NativeImageReader.class.getName(),
                new Class[] { ImageInputStream.class }, new String[] {}, false, // supportsStandardStreamMetadataFormat
                null, // nativeStreamMetadataFormatName
                null, // nativeStreamMetadataFormatClassName
                null, // extraStreamMetadataFormatNames
                null, // extraStreamMetadataFormatClassNames
                false, // supportsStandardImageMetadataFormat
                null, null, null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG Image Reader (8/12/16 bits, IJG 6b based)";
    }

    @Override
    public String[] getFileSuffixes() {
        return SUFFIXES;
    }

    @Override
    public String[] getMIMETypes() {
        return MIMES;
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        // NativeImageReader.read() eventually instantiates a StreamSegment,
        // which does not support all ImageInputStreams
        if (!StreamSegment.supportsInputStream(source)) {
            return false;
        }
        ImageInputStream iis = (ImageInputStream) source;

        iis.mark();
        try {
            int byte1 = iis.read();
            int byte2 = iis.read();
            // Magic numbers for JPEG (general jpeg marker)
            if ((byte1 != 0xFF) || (byte2 != 0xD8)) {
                return false;
            }
            do {
                byte1 = iis.read();
                byte2 = iis.read();
                // Something wrong, but try to read it anyway
                if (byte1 != 0xFF) {
                    break;
                }
                // Start of scan
                if (byte2 == 0xDA) {
                    break;
                }
                // Start of Frame, also known as SOF55, indicates a JPEG-LS file. Not supported in this reader
                if (byte2 == 0xF7) {
                    return false;
                }
                // 0xffc0: // SOF_0: JPEG baseline
                // 0xffc1: // SOF_1: JPEG extended sequential DCT
                // 0xffc2: // SOF_2: JPEG progressive DCT
                // 0xffc3: // SOF_3: JPEG lossless sequential
                if ((byte2 >= 0xC0) && (byte2 <= 0xC3)) {
                    return true;
                }
                // 0xffc5: // SOF_5: differential (hierarchical) extended sequential, Huffman
                // 0xffc6: // SOF_6: differential (hierarchical) progressive, Huffman
                // 0xffc7: // SOF_7: differential (hierarchical) lossless, Huffman
                if ((byte2 >= 0xC5) && (byte2 <= 0xC7)) {
                    return true;
                }
                // 0xffc9: // SOF_9: extended sequential, arithmetic
                // 0xffca: // SOF_10: progressive, arithmetic
                // 0xffcb: // SOF_11: lossless, arithmetic
                if ((byte2 >= 0xC9) && (byte2 <= 0xCB)) {
                    return true;
                }
                // 0xffcd: // SOF_13: differential (hierarchical) extended sequential, arithmetic
                // 0xffce: // SOF_14: differential (hierarchical) progressive, arithmetic
                // 0xffcf: // SOF_15: differential (hierarchical) lossless, arithmetic
                if ((byte2 >= 0xCD) && (byte2 <= 0xCF)) {
                    return true;
                }
                int length = iis.read() << 8;
                length += iis.read();
                length -= 2;
                while (length > 0) {
                    length -= iis.skipBytes(length);
                }
            } while (true);
            return true;
        } finally {
            iis.reset();
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new NativeImageReader(this, false);
    }

}
