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
package org.miaixz.bus.image.nimble;

import org.miaixz.bus.image.galaxy.data.Implementation;
import org.miaixz.bus.image.nimble.stream.BytesWithImageDescriptor;
import org.miaixz.bus.image.nimble.stream.ImageFileInputStream;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageReaderSpi extends javax.imageio.spi.ImageReaderSpi {

    private static final String[] dicomFormatNames = {"dicom", "DICOM"};
    private static final String[] dicomExt = {"dcm", "dic", "dicm", "dicom"};
    private static final String[] dicomMimeType = {"application/dicom"};
    private static final Class<?>[] dicomInputTypes = {
            ImageFileInputStream.class, BytesWithImageDescriptor.class
    };

    public ImageReaderSpi() {
        super(
                "image",
                Implementation.getVersionName(),
                dicomFormatNames,
                dicomExt,
                dicomMimeType,
                ImageReader.class.getName(),
                dicomInputTypes,
                null, // writerSpiNames
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
            return ((tag >= 0x00080000 && tag <= 0x00080016)
                    || (iis.skipBytes(124) == 124
                    && iis.read() == 'D'
                    && iis.read() == 'I'
                    && iis.read() == 'C'
                    && iis.read() == 'M'));
        } finally {
            iis.reset();
        }
    }

    @Override
    public javax.imageio.ImageReader createReaderInstance(Object extension) {
        return new ImageReader(this);
    }

}
