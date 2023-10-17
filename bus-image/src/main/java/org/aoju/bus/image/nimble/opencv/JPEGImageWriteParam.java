/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.nimble.opencv;

import javax.imageio.ImageWriteParam;
import java.util.Locale;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JPEGImageWriteParam extends ImageWriteParam {

    private static final String[] COMPRESSION_TYPES = {
            "BASELINE", // JPEG Baseline: Imgcodecs.JPEG_baseline (0)
            "EXTENDED", // JPEG Extended sequential: Imgcodecs.JPEG_sequential (1)
            "SPECTRAL", // JPEG Spectral Selection: Imgcodecs.JPEG_spectralSelection (2) (Retired from DICOM)
            "PROGRESSIVE", // JPEG Full Progression: Imgcodecs.JPEG_progressive (3) (Retired from DICOM)
            "LOSSLESS-1", // JPEG Lossless, Selection Value 1: Imgcodecs.JPEG_lossless (4), prediction (1)
            "LOSSLESS-2", // JPEG Lossless, Selection Value 2: Imgcodecs.JPEG_lossless (4), prediction (2)
            "LOSSLESS-3", // JPEG Lossless, Selection Value 3: Imgcodecs.JPEG_lossless (4), prediction (3)
            "LOSSLESS-4", // JPEG Lossless, Selection Value 4: Imgcodecs.JPEG_lossless (4), prediction (4)
            "LOSSLESS-5", // JPEG Lossless, Selection Value 5: Imgcodecs.JPEG_lossless (4), prediction (5)
            "LOSSLESS-6", // JPEG Lossless, Selection Value 6: Imgcodecs.JPEG_lossless (4), prediction (6)
            "LOSSLESS-7", // JPEG Lossless, Selection Value 7: Imgcodecs.JPEG_lossless (4), prediction (7)
    };

    /**
     * JPEG lossless point transform (0..15, default: 0)
     */
    private int pointTransform;

    public JPEGImageWriteParam(Locale locale) {
        super(locale);
        super.canWriteCompressed = true;
        super.compressionMode = MODE_EXPLICIT;
        super.compressionType = "BASELINE";
        super.compressionTypes = COMPRESSION_TYPES;
        super.compressionQuality = 0.75F;
        this.pointTransform = 0;
    }

    public int getMode() {
        switch (compressionType.charAt(0)) {
            case 'B':
                return 0;
            case 'E':
                return 1;
            case 'S':
                return 2;
            case 'P':
                return 3;
        }
        return 4;
    }

    public int getPrediction() {
        return isCompressionLossless() ? (compressionType.charAt(9) - '0') : 0;
    }

    public int getPointTransform() {
        return pointTransform;
    }

    public void setPointTransform(int pointTransform) {
        this.pointTransform = pointTransform;
    }

    @Override
    public boolean isCompressionLossless() {
        return compressionType.charAt(0) == 'L';
    }

}
