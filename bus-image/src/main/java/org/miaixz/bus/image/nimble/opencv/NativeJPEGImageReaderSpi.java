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
 * Represents the NativeJPEGImageReaderSpi type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeJPEGImageReaderSpi extends ImageReaderSpi {

    /**
     * The names value.
     */
    static final String[] NAMES = { "jpeg-cv" };

    /**
     * The suffixes value.
     */
    static final String[] SUFFIXES = {};

    /**
     * The mimes value.
     */
    static final String[] MIMES = {};

    /**
     * Creates a new instance.
     */
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

    /**
     * Gets the description.
     *
     * @param locale the locale.
     * @return the description.
     */
    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG Image Reader (8/12/16 bits, IJG 6b based)";
    }

    /**
     * Gets the file suffixes.
     *
     * @return the file suffixes.
     */
    @Override
    public String[] getFileSuffixes() {
        return SUFFIXES;
    }

    /**
     * Gets the mime types.
     *
     * @return the mime types.
     */
    @Override
    public String[] getMIMETypes() {
        return MIMES;
    }

    /**
     * Determines whether decode input.
     *
     * @param source the source.
     * @return true if the condition is met; otherwise false.
     * @throws IOException if the operation cannot be completed.
     */
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
                // SOF55 (0xF7) marks a JPEG-LS file, which is not supported by this reader.
                if (byte2 == 0xF7) {
                    return false;
                }
                // Any JPEG Start-Of-Frame marker means this reader can decode the stream.
                if (NativeImageReader.isSOFMarker(byte2)) {
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

    /**
     * Creates the reader instance.
     *
     * @param extension the extension.
     * @return the operation result.
     */
    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new NativeImageReader(this, false);
    }

}
