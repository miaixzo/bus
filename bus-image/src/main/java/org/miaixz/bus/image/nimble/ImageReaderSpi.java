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
package org.miaixz.bus.image.nimble;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.stream.ImageInputStream;

import org.miaixz.bus.image.galaxy.data.Implementation;
import org.miaixz.bus.image.nimble.stream.BytesWithImageDescriptor;
import org.miaixz.bus.image.nimble.stream.ImageFileInputStream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageReaderSpi extends javax.imageio.spi.ImageReaderSpi {

    private static final String[] dicomFormatNames = { "dicom", "DICOM" };
    private static final String[] dicomExt = { "dcm", "dic", "dicm", "dicom" };
    private static final String[] dicomMimeType = { "application/dicom" };
    private static final Class<?>[] dicomInputTypes = { ImageFileInputStream.class, BytesWithImageDescriptor.class };

    public ImageReaderSpi() {
        super("image", Implementation.getVersionName(), dicomFormatNames, dicomExt, dicomMimeType,
                ImageReader.class.getName(), dicomInputTypes, null, // writerSpiNames
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

    @Override
    public String getDescription(Locale locale) {
        return "DICOM Image Reader";
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        ImageInputStream iis = (ImageInputStream) source;
        iis.mark();
        try {
            int tag = iis.read() | (iis.read() << 8) | (iis.read() << 16) | (iis.read() << 24);
            return ((tag >= 0x00080000 && tag <= 0x00080016) || (iis.skipBytes(124) == 124 && iis.read() == 'D'
                    && iis.read() == 'I' && iis.read() == 'C' && iis.read() == 'M'));
        } finally {
            iis.reset();
        }
    }

    @Override
    public javax.imageio.ImageReader createReaderInstance(Object extension) {
        return new ImageReader(this);
    }

}
