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
package org.miaixz.bus.image.nimble.opencv;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.miaixz.bus.core.Version;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeJ2kImageReaderSpi extends ImageReaderSpi {

    static final String[] SUFFIXES = { "jp2", "jp2k", "j2k", "j2c" };
    static final String[] NAMES = { "jpeg2000-cv", "jpeg2000", "JP2KSimpleBox", "jpeg 2000", "JPEG 2000", "JPEG2000" };
    static final String[] MIMES = { "image/jp2", "image/jp2k", "image/j2k", "image/j2c" };

    public NativeJ2kImageReaderSpi() {
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
        return "Natively-accelerated JPEG2000 Image Reader (OpenJPEG based)";
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
            int marker = (iis.read() << 8) | iis.read();

            if (marker == 0xFF4F) {
                return true;
            }

            iis.reset();
            iis.mark();
            byte[] b = new byte[12];
            iis.readFully(b);

            // Verify the signature box
            // The length of the signature box is 12
            if (b[0] != 0 || b[1] != 0 || b[2] != 0 || b[3] != 12) {
                return false;
            }

            // The signature box type is "jP "
            if ((b[4] & 0xff) != 0x6A || (b[5] & 0xFF) != 0x50 || (b[6] & 0xFF) != 0x20 || (b[7] & 0xFF) != 0x20) {
                return false;
            }

            // The signature content is 0x0D0A870A
            return (b[8] & 0xFF) != 0x0D || (b[9] & 0xFF) != 0x0A || (b[10] & 0xFF) != 0x87 || (b[11] & 0xFF) != 0x0A;
        } finally {
            iis.reset();
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new NativeImageReader(this, true);
    }

}
