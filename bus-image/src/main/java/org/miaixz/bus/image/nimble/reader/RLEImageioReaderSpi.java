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
package org.miaixz.bus.image.nimble.reader;

import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.galaxy.data.Implementation;

/**
 * Represents the RLEImageioReaderSpi type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RLEImageioReaderSpi extends ImageReaderSpi {

    /**
     * The vendor name value.
     */
    private static final String vendorName = "org.miaixz";

    /**
     * The version value.
     */
    private static final String version = Implementation.getVersionName();

    /**
     * The format names value.
     */
    private static final String[] formatNames = { "rle", "RLE" };

    /**
     * The input types value.
     */
    private static final Class<?>[] inputTypes = { ImageInputStream.class };

    /**
     * The entensions value.
     */
    private static final String[] entensions = { Normal.EMPTY };

    /**
     * The mime type value.
     */
    private static final String[] mimeType = { Normal.EMPTY };

    /**
     * Creates a new instance.
     */
    public RLEImageioReaderSpi() {
        super(vendorName, version, formatNames, entensions, // suffixes
                mimeType, // MIMETypes
                RLEImageioReader.class.getName(), inputTypes, null, // writerSpiNames
                false, // supportsStandardStreamMetadataFormat
                null, // nativeStreamMetadataFormatName
                null, // nativeStreamMetadataFormatClassName
                null, // extraStreamMetadataFormatNames
                null, // extraStreamMetadataFormatClassNames
                false, // supportsStandardImageMetadataFormat
                null, // nativeImageMetadataFormatName
                null, // nativeImageMetadataFormatClassName
                null, // extraImageMetadataFormatNames
                null); // extraImageMetadataFormatClassNames
    }

    /**
     * Gets the description.
     *
     * @param locale the locale.
     * @return the description.
     */
    @Override
    public String getDescription(Locale locale) {
        return "RLE Image Reader";
    }

    /**
     * Determines whether decode input.
     *
     * @param source the source.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean canDecodeInput(Object source) {
        return false;
    }

    /**
     * Creates the reader instance.
     *
     * @param extension the extension.
     * @return the operation result.
     */
    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new RLEImageioReader(this);
    }

}
