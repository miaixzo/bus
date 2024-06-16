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
package org.miaixz.bus.image.nimble.opencv;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeJLSImageReaderSpi extends ImageReaderSpi {

    public static final String[] NAMES = {"jpeg-ls-cv", "jpeg-ls", "JPEG-LS"};
    public static final String[] SUFFIXES = {"jls"};
    public static final String[] MIMES = {"image/jpeg-ls"};

    public NativeJLSImageReaderSpi() {
        super("Bus Team", "1.5", NAMES, SUFFIXES, MIMES, NativeImageReader.class.getName(),
                new Class[]{ImageInputStream.class}, new String[]{NativeJLSImageWriterSpi.class.getName()}, false, // supportsStandardStreamMetadataFormat
                null, // nativeStreamMetadataFormatName
                null, // nativeStreamMetadataFormatClassName
                null, // extraStreamMetadataFormatNames
                null, // extraStreamMetadataFormatClassNames
                false, // supportsStandardImageMetadataFormat
                null, null, null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG-LS Image Reader (CharLS based)";
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
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

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new NativeImageReader(this, false);
    }

}
