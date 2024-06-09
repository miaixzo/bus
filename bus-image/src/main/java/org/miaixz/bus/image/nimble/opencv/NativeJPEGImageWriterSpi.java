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

import org.miaixz.bus.core.lang.Normal;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.util.Locale;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class NativeJPEGImageWriterSpi extends ImageWriterSpi {

    public NativeJPEGImageWriterSpi() {
        this(NativeJPEGImageWriter.class);
    }

    public NativeJPEGImageWriterSpi(Class<? extends NativeJPEGImageWriter> writer) {
        super("Bus Team", "1.5", NativeJPEGImageReaderSpi.NAMES, NativeJPEGImageReaderSpi.SUFFIXES,
                NativeJPEGImageReaderSpi.MIMES, writer.getName(), new Class[]{ImageOutputStream.class},
                new String[]{NativeJPEGImageReaderSpi.class.getName()}, false, null, null, null, null, false, null, null,
                null, null);
    }

    public static boolean checkCommonJpgRequirement(ImageTypeSpecifier type) {
        ColorModel colorModel = type.getColorModel();

        if (colorModel instanceof IndexColorModel) {
            // No need to check further: writer converts to 8-8-8 RGB.
            return true;
        }

        SampleModel sampleModel = type.getSampleModel();

        // Ensure all channels have the same bit depth
        int bitDepth;
        if (null != colorModel) {
            int[] componentSize = colorModel.getComponentSize();
            bitDepth = componentSize[0];
            for (int i = 1; i < componentSize.length; i++) {
                if (componentSize[i] != bitDepth) {
                    return false;
                }
            }
        } else {
            int[] sampleSize = sampleModel.getSampleSize();
            bitDepth = sampleSize[0];
            for (int i = 1; i < sampleSize.length; i++) {
                if (sampleSize[i] != bitDepth) {
                    return false;
                }
            }
        }

        // Ensure bitDepth is no more than 16
        if (bitDepth > Normal._16) {
            return false;
        }

        // Check number of bands.
        int numBands = sampleModel.getNumBands();
        return numBands == 1 || numBands == 3 || numBands == 4;
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        return checkCommonJpgRequirement(type);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG Image Writer (8/12/16 bits, IJG 6b based)";
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) {
        return new NativeJPEGImageWriter(this);
    }

}
