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

import java.util.Locale;

import javax.imageio.ImageWriteParam;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JPEGImageWriteParam extends ImageWriteParam {

    private static final String[] COMPRESSION_TYPES = { "BASELINE", // JPEG Baseline: Imgcodecs.JPEG_baseline (0)
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
