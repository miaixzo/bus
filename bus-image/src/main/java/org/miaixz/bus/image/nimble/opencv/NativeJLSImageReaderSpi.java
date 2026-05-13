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
 * Represents the NativeJLSImageReaderSpi type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeJLSImageReaderSpi extends ImageReaderSpi {

    /**
     * The names value.
     */
    static final String[] NAMES = { "jpeg-ls-cv", "jpeg-ls", "JPEG-LS" };

    /**
     * The suffixes value.
     */
    static final String[] SUFFIXES = { "jls" };

    /**
     * The mimes value.
     */
    static final String[] MIMES = { "image/jpeg-ls" };

    /**
     * Creates a new instance.
     */
    public NativeJLSImageReaderSpi() {
        super("Miaixz Team", Version._VERSION, NAMES, SUFFIXES, MIMES, NativeImageReader.class.getName(),
                new Class[] { ImageInputStream.class }, new String[] { NativeJLSImageWriterSpi.class.getName() }, false, // supportsStandardStreamMetadataFormat
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
        return "Natively-accelerated JPEG-LS Image Reader (CharLS based)";
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
        int byte1 = iis.read();
        int byte2 = iis.read();
        int byte3 = iis.read();
        int byte4 = iis.read();
        iis.reset();
        // Magic numbers for JPEG (general jpeg marker): 0xFFD8
        // Start of Frame, also known as SOF55, indicates a JPEG-LS file
        return (byte1 == 0xFF) && (byte2 == 0xD8) && (byte3 == 0xFF) && (byte4 == 0xF7);
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
