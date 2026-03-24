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

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import org.miaixz.bus.core.lang.Normal;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class NativeJPEGImageWriterSpi extends ImageWriterSpi {

    public NativeJPEGImageWriterSpi() {
        this(NativeJPEGImageWriter.class);
    }

    public NativeJPEGImageWriterSpi(Class<? extends NativeJPEGImageWriter> writer) {
        super("Miaixz Team", "1.0", NativeJPEGImageReaderSpi.NAMES, NativeJPEGImageReaderSpi.SUFFIXES,
                NativeJPEGImageReaderSpi.MIMES, writer.getName(), new Class[] { ImageOutputStream.class },
                new String[] { NativeJPEGImageReaderSpi.class.getName() }, false, null, null, null, null, false, null,
                null, null, null);
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
        if (colorModel != null) {
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
