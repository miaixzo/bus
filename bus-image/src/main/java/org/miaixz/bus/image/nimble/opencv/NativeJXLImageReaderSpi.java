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
 * ImageIO reader SPI for native JPEG XL decoding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeJXLImageReaderSpi extends ImageReaderSpi {

    /**
     * The names value.
     */
    static final String[] NAMES = { "jpeg-xl-cv", "jpeg-xl", "JPEG-XL" };

    /**
     * The suffixes value.
     */
    static final String[] SUFFIXES = { "jxl" };

    /**
     * The mimes value.
     */
    static final String[] MIMES = { "image/jxl" };

    /**
     * Creates a new instance.
     */
    public NativeJXLImageReaderSpi() {
        super("Miaixz Team", Version._VERSION, NAMES, SUFFIXES, MIMES, NativeImageReader.class.getName(),
                new Class[] { ImageInputStream.class }, new String[] { NativeJXLImageWriterSpi.class.getName() }, false,
                null, null, null, null, false, null, null, null, null);
    }

    /**
     * Gets the description.
     *
     * @param locale the locale.
     * @return the description.
     */
    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG XL Image Reader";
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
        if (!StreamSegment.supportsInputStream(source))
            return false;

        ImageInputStream iis = (ImageInputStream) source;
        iis.mark();
        try {
            int byte1 = iis.read();
            int byte2 = iis.read();
            if (byte1 == 0xff && byte2 == 0x0a)
                return true;

            int byte3 = iis.read();
            int byte4 = iis.read();
            int byte5 = iis.read();
            int byte6 = iis.read();
            int byte7 = iis.read();
            int byte8 = iis.read();
            int byte9 = iis.read();
            int byte10 = iis.read();
            int byte11 = iis.read();
            int byte12 = iis.read();
            return byte1 == 0x00 && byte2 == 0x00 && byte3 == 0x00 && byte4 == 0x0c && byte5 == 0x4a && byte6 == 0x58
                    && byte7 == 0x4c && byte8 == 0x20 && byte9 == 0x0d && byte10 == 0x0a && byte11 == 0x87
                    && byte12 == 0x0a;
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
