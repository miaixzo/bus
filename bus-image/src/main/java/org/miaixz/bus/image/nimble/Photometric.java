/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble;

import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;

import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Photometric {

    MONOCHROME1(true, true, false, false) {

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createMonochromeColorModel(bits, dataType);
        }
    },
    MONOCHROME2(true, false, false, false) {

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createMonochromeColorModel(bits, dataType);
        }
    },
    PALETTE_COLOR(false, false, false, false) {

        @Override
        public String toString() {
            return "PALETTE COLOR";
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createPaletteColorModel(bits, dataType, cspace, ds);
        }
    },
    RGB(false, false, false, false) {

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createRGBColorModel(bits, dataType, cspace);
        }

        @Override
        public Photometric compress(String tsuid) {
            switch (UID.from(tsuid)) {
                case UID.JPEGBaseline8Bit:
                case UID.JPEGExtended12Bit:
                    return YBR_FULL_422;

                case UID.JPEGSpectralSelectionNonHierarchical68:
                case UID.JPEGFullProgressionNonHierarchical1012:
                    return YBR_FULL;

                case UID.JPEG2000Lossless:
                case UID.JPEG2000MCLossless:
                case UID.HTJ2KLossless:
                case UID.HTJ2KLosslessRPCL:
                    return YBR_RCT;

                case UID.JPEG2000:
                case UID.JPEG2000MC:
                case UID.HTJ2K:
                    return YBR_ICT;
            }
            return this;
        }
    },
    YBR_FULL(false, false, true, false) {

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createYBRFullColorModel(bits, dataType, new YBRColorSpace(cspace, YBR.FULL));
        }
    },
    YBR_FULL_422(false, false, true, true) {

        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_422.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createYBRColorModel(
                    bits,
                    dataType,
                    new YBRColorSpace(cspace, YBR.PARTIAL),
                    ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
            return new SampledSampleModel(w, h, ColorSubsampling.YBR_XXX_422);
        }
    },
    YBR_ICT(false, false, true, false) {

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            throw new UnsupportedOperationException();
        }

    },
    YBR_PARTIAL_420(false, false, true, true) {

        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_420.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createYBRColorModel(
                    bits,
                    dataType,
                    new YBRColorSpace(cspace, YBR.PARTIAL),
                    ColorSubsampling.YBR_XXX_420);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
            return new SampledSampleModel(w, h, ColorSubsampling.YBR_XXX_420);
        }
    },
    YBR_PARTIAL_422(false, false, true, true) {

        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_422.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createYBRColorModel(
                    bits,
                    dataType,
                    new YBRColorSpace(cspace, YBR.PARTIAL),
                    ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
            return new SampledSampleModel(w, h, ColorSubsampling.YBR_XXX_422);
        }
    },
    YBR_RCT(false, false, true, false) {

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            throw new UnsupportedOperationException();
        }
    };

    private final boolean monochrome;
    private final boolean inverse;
    private final boolean ybr;
    private final boolean subSampled;

    Photometric(boolean monochrome, boolean inverse, boolean ybr, boolean subSampled) {
        this.monochrome = monochrome;
        this.inverse = inverse;
        this.ybr = ybr;
        this.subSampled = subSampled;
    }

    public static Photometric fromString(String s) {
        return s.equals("PALETTE COLOR") ? PALETTE_COLOR : valueOf(s);
    }

    public int frameLength(int w, int h, int samples, int bitsAllocated) {
        return w * h * samples * bitsAllocated / 8;
    }

    public boolean isMonochrome() {
        return monochrome;
    }

    public boolean isYBR() {
        return ybr;
    }

    public Photometric compress(String tsuid) {
        return this;
    }

    public boolean isInverse() {
        return inverse;
    }

    public boolean isSubSampled() {
        return subSampled;
    }

    public abstract ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds);

    public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
        int[] indicies = new int[samples];
        for (int i = 1; i < samples; i++)
            indicies[i] = i;
        return banded && samples > 1 ? new BandedSampleModel(dataType, w, h, w, indicies, new int[samples])
                : new PixelInterleavedSampleModel(dataType, w, h, samples, w * samples, indicies);
    }

}
