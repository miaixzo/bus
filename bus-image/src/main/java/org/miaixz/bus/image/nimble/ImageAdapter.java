/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.lut.*;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageAdapter {

    private static final Map<LutParameters, LookupTableCV> LUT_Cache = new ConcurrentHashMap();

    private final ImageDescriptor desc;
    private final MinMaxLocResult minMax;
    private final int frameIndex;

    private int bitsStored;
    private List<PresetWindowLevel> windowingPresetCollection = null;

    public ImageAdapter(PlanarImage image, ImageDescriptor desc, int frameIndex) {
        int depth = CvType.depth(Objects.requireNonNull(image).type());
        this.desc = Objects.requireNonNull(desc);
        this.bitsStored = depth > CvType.CV_16S ? (int) image.elemSize1() * 8 : desc.getBitsStored();
        this.frameIndex = frameIndex;
        MinMaxLocResult minMax = desc.getMinMaxPixelValue(frameIndex);
        if (minMax == null) {
            minMax = findMinMaxValues(image, frameIndex);
            desc.setMinMaxPixelValue(frameIndex, minMax);
        }
        this.minMax = minMax;
        /*
         * Lazily compute image pixel transformation here since inner class Load is called from a separate and dedicated
         * worker Thread. Also, it will be computed only once
         *
         * Considering that the default pixel padding option is true and Inverse LUT action is false
         */
        getModalityLookup(null, false);
    }

    public static MinMaxLocResult getMinMaxValues(PlanarImage image, ImageDescriptor desc, int frameIndex) {
        MinMaxLocResult val = desc.getMinMaxPixelValue(frameIndex);
        if (val != null) {
            return val;
        }
        boolean monochrome = desc.getPhotometricInterpretation().isMonochrome();
        if (monochrome) {
            Integer paddingValue = desc.getPixelPaddingValue();
            if (paddingValue != null) {
                Integer paddingLimit = desc.getPixelPaddingRangeLimit();
                Integer paddingValueMin = (paddingLimit == null) ? paddingValue : Math.min(paddingValue, paddingLimit);
                Integer paddingValueMax = (paddingLimit == null) ? paddingValue : Math.max(paddingValue, paddingLimit);
                val = findMinMaxValues(image, paddingValueMin, paddingValueMax);
            }
        }

        // When not monochrome and no padding value, use the default min and max values
        if (val == null) {
            val = ImageProcessor.findRawMinMaxValues(image, !monochrome);
        }
        return val;
    }

    private MinMaxLocResult findMinMaxValues(PlanarImage image, int frameIndex) {
        /*
         * This function can be called several times from the inner class Load. min and max will be computed only once.
         */

        MinMaxLocResult val = getMinMaxValues(image, desc, frameIndex);
        // Cannot trust SmallestImagePixelValue and LargestImagePixelValue values! So search min and max
        // values
        int bitsAllocated = desc.getBitsAllocated();
        if (bitsStored < bitsAllocated) {
            boolean isSigned = desc.isSigned();
            int minInValue = isSigned ? -(1 << (bitsStored - 1)) : 0;
            int maxInValue = isSigned ? (1 << (bitsStored - 1)) - 1 : (1 << bitsStored) - 1;
            if (val.minVal < minInValue || val.maxVal > maxInValue) {
                /*
                 * When the image contains values outside the bits stored values, the bits stored is replaced by the
                 * bits allocated for having a LUT which handles all the values.
                 *
                 * Overlays in pixel data should be masked before finding min and max.
                 */
                setBitsStored(bitsAllocated);
            }
        }
        return val;
    }

    /**
     * Computes Min/Max values from Image excluding range of values provided
     *
     * @param paddingValueMin padding value to exclude from min value
     * @param paddingValueMax padding value to exclude from max value
     */
    private static MinMaxLocResult findMinMaxValues(PlanarImage image, Integer paddingValueMin,
            Integer paddingValueMax) {
        MinMaxLocResult val;
        if (CvType.depth(image.type()) <= CvType.CV_8S) {
            val = new MinMaxLocResult();
            val.minVal = 0.0;
            val.maxVal = 255.0;
        } else {
            val = ImageProcessor.findMinMaxValues(image.toMat(), paddingValueMin, paddingValueMax);
            // Handle a special case when min and max are equal, ex. Black image
            // + 1 to max enables to display the correct value
            if (val != null && val.minVal == val.maxVal) {
                val.maxVal += 1.0;
            }
        }
        return val;
    }

    public int getBitsStored() {
        return bitsStored;
    }

    public void setBitsStored(int bitsStored) {
        this.bitsStored = bitsStored;
    }

    public MinMaxLocResult getMinMax() {
        return minMax;
    }

    public ImageDescriptor getImageDescriptor() {
        return desc;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public int getMinAllocatedValue(WlPresentation wl) {
        boolean signed = isModalityLutOutSigned(wl);
        int bitsAllocated = desc.getBitsAllocated();
        int maxValue = signed ? (1 << (bitsAllocated - 1)) - 1 : ((1 << bitsAllocated) - 1);
        return signed ? -(maxValue + 1) : 0;
    }

    public int getMaxAllocatedValue(WlPresentation wl) {
        boolean signed = isModalityLutOutSigned(wl);
        int bitsAllocated = desc.getBitsAllocated();
        return signed ? (1 << (bitsAllocated - 1)) - 1 : ((1 << bitsAllocated) - 1);
    }

    /**
     * In the case where Rescale Slope and Rescale Intercept are used for modality pixel transformation, the output
     * ranges may be signed even if Pixel Representation is unsigned.
     *
     * @param wl WlPresentation
     * @return true if the output of the modality pixel transformation can be signed
     */
    public boolean isModalityLutOutSigned(WlPresentation wl) {
        boolean signed = desc.isSigned();
        return getMinValue(wl) < 0 || signed;
    }

    /**
     * @return return the min value after modality pixel transformation and after pixel padding operation if padding
     *         exists.
     */
    public double getMinValue(WlPresentation wl) {
        return minMaxValue(true, wl);
    }

    /**
     * @return return the max value after modality pixel transformation and after pixel padding operation if padding
     *         exists.
     */
    public double getMaxValue(WlPresentation wl) {
        return minMaxValue(false, wl);
    }

    private double minMaxValue(boolean minVal, WlPresentation wl) {
        Number min = pixelToRealValue(minMax.minVal, wl);
        Number max = pixelToRealValue(minMax.maxVal, wl);
        if (min == null || max == null) {
            return 0;
        }
        // Computes min and max as slope can be negative
        if (minVal) {
            return Math.min(min.doubleValue(), max.doubleValue());
        }
        return Math.max(min.doubleValue(), max.doubleValue());
    }

    public double getRescaleIntercept(PresentationLutObject dcm) {
        if (dcm != null) {
            OptionalDouble prIntercept = dcm.getModalityLutModule().getRescaleIntercept();
            if (prIntercept.isPresent()) {
                return prIntercept.getAsDouble();
            }
        }
        return desc.getModalityLUT().getRescaleIntercept().orElse(0.0);
    }

    public double getRescaleSlope(PresentationLutObject dcm) {
        if (dcm != null) {
            OptionalDouble prSlope = dcm.getModalityLutModule().getRescaleSlope();
            if (prSlope.isPresent()) {
                return prSlope.getAsDouble();
            }
        }
        return desc.getModalityLUT().getRescaleSlope().orElse(1.0);
    }

    public double getFullDynamicWidth(WlPresentation wl) {
        return getMaxValue(wl) - getMinValue(wl);
    }

    public double getFullDynamicCenter(WlPresentation wl) {
        double minValue = getMinValue(wl);
        double maxValue = getMaxValue(wl);
        return minValue + (maxValue - minValue) / 2.f;
    }

    /**
     * @return default as first element of preset List Note : null should never be returned since auto is at least one
     *         preset
     */
    public PresetWindowLevel getDefaultPreset(WlPresentation wlp) {
        List<PresetWindowLevel> presetList = getPresetList(wlp);
        return (presetList != null && !presetList.isEmpty()) ? presetList.get(0) : null;
    }

    public synchronized List<PresetWindowLevel> getPresetList(WlPresentation wl) {
        return getPresetList(wl, false);
    }

    public synchronized List<PresetWindowLevel> getPresetList(WlPresentation wl, boolean reload) {
        if (minMax != null && (windowingPresetCollection == null || reload)) {
            windowingPresetCollection = PresetWindowLevel.getPresetCollection(this, "[DICOM]", wl);
        }
        return windowingPresetCollection;
    }

    public int getPresetCollectionSize() {
        if (windowingPresetCollection == null) {
            return 0;
        }
        return windowingPresetCollection.size();
    }

    public LutShape getDefaultShape(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = getDefaultPreset(wlp);
        return (defaultPreset != null) ? defaultPreset.getLutShape() : LutShape.LINEAR;
    }

    public double getDefaultWindow(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = getDefaultPreset(wlp);
        return (defaultPreset != null) ? defaultPreset.getWindow()
                : minMax == null ? 0.0 : minMax.maxVal - minMax.minVal;
    }

    public double getDefaultLevel(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = getDefaultPreset(wlp);
        if (defaultPreset != null) {
            return defaultPreset.getLevel();
        }
        if (minMax != null) {
            return minMax.minVal + (minMax.maxVal - minMax.minVal) / 2.0;
        }
        return 0.0f;
    }

    public Number pixelToRealValue(Number pixelValue, WlPresentation wlp) {
        if (pixelValue != null) {
            LookupTableCV lookup = getModalityLookup(wlp, false);
            if (lookup != null) {
                int val = pixelValue.intValue();
                if (val >= lookup.getOffset() && val < lookup.getOffset() + lookup.getNumEntries()) {
                    return lookup.lookup(0, val);
                }
            }
        }
        return pixelValue;
    }

    /**
     * DICOM PS 3.3 $C.11.1 Modality LUT Module
     */
    public LookupTableCV getModalityLookup(WlPresentation wlp, boolean inverseLUTAction) {
        Integer paddingValue = desc.getPixelPaddingValue();
        boolean pixelPadding = wlp == null || wlp.isPixelPadding();
        PresentationLutObject pr = wlp != null && wlp.getPresentationState() instanceof PresentationLutObject
                ? (PresentationLutObject) wlp.getPresentationState()
                : null;
        LookupTableCV prModLut = (pr != null ? pr.getModalityLutModule().getLut().orElse(null) : null);
        final LookupTableCV mLUTSeq = prModLut == null ? desc.getModalityLUT().getLut().orElse(null) : prModLut;
        if (mLUTSeq != null) {
            if (!pixelPadding || paddingValue == null) {
                if (minMax.minVal >= mLUTSeq.getOffset()
                        && minMax.maxVal < mLUTSeq.getOffset() + mLUTSeq.getNumEntries()) {
                    return mLUTSeq;
                } else if (prModLut == null) {
                    Logger.warn(
                            "Pixel values doesn't match to Modality LUT sequence table. So the Modality LUT is not applied.");
                }
            } else {
                Logger.warn("Cannot apply Modality LUT sequence and Pixel Padding");
            }
        }

        boolean inverseLut = isPhotometricInterpretationInverse(pr);
        if (pixelPadding) {
            inverseLut ^= inverseLUTAction;
        }
        LutParameters lutParams = getLutParameters(pixelPadding, mLUTSeq, inverseLut, pr);
        // Not required to have a modality lookup table
        if (lutParams == null) {
            return null;
        }
        LookupTableCV modalityLookup = LUT_Cache.get(lutParams);

        if (modalityLookup != null) {
            return modalityLookup;
        }

        if (mLUTSeq != null) {
            if (mLUTSeq.getNumBands() == 1) {
                if (mLUTSeq.getDataType() == DataBuffer.TYPE_BYTE) {
                    byte[] data = mLUTSeq.getByteData(0);
                    if (data != null) {
                        modalityLookup = new LookupTableCV(data, mLUTSeq.getOffset(0));
                    }
                } else {
                    short[] data = mLUTSeq.getShortData(0);
                    if (data != null) {
                        modalityLookup = new LookupTableCV(data, mLUTSeq.getOffset(0),
                                mLUTSeq.getData() instanceof DataBufferUShort);
                    }
                }
            }
            if (modalityLookup == null) {
                modalityLookup = mLUTSeq;
            }
        } else {
            modalityLookup = RGBImageVoiLut.createRescaleRampLut(lutParams);
        }

        if (desc.getPhotometricInterpretation().isMonochrome()) {
            RGBImageVoiLut.applyPixelPaddingToModalityLUT(modalityLookup, lutParams);
        }
        LUT_Cache.put(lutParams, modalityLookup);
        return modalityLookup;
    }

    public boolean isPhotometricInterpretationInverse(PresentationStateLut pr) {
        Optional<String> prLUTShape = pr == null ? Optional.empty() : pr.getPrLutShapeMode();
        Photometric p = desc.getPhotometricInterpretation();
        return prLUTShape.map("INVERSE"::equals).orElseGet(() -> p == Photometric.MONOCHROME1);
    }

    public LutParameters getLutParameters(boolean pixelPadding, LookupTableCV mLUTSeq, boolean inversePaddingMLUT,
            PresentationLutObject pr) {
        Integer paddingValue = desc.getPixelPaddingValue();

        boolean isSigned = desc.isSigned();
        double intercept = getRescaleIntercept(pr);
        double slope = getRescaleSlope(pr);

        // No need to have a modality lookup table
        if (bitsStored > 16
                || (MathKit.isEqual(slope, 1.0) && MathKit.isEqualToZero(intercept) && paddingValue == null)) {
            return null;
        }

        Integer paddingLimit = desc.getPixelPaddingRangeLimit();
        boolean outputSigned = false;
        int bitsOutputLut;
        if (mLUTSeq == null) {
            double minValue = minMax.minVal * slope + intercept;
            double maxValue = minMax.maxVal * slope + intercept;
            bitsOutputLut = Integer.SIZE - Integer.numberOfLeadingZeros((int) Math.round(maxValue - minValue));
            outputSigned = minValue < 0 || isSigned;
            if (outputSigned && bitsOutputLut <= 8) {
                // Allows to handle negative values with 8-bit image
                bitsOutputLut = 9;
            }
        } else {
            bitsOutputLut = mLUTSeq.getDataType() == DataBuffer.TYPE_BYTE ? 8 : 16;
        }
        return new LutParameters(intercept, slope, pixelPadding, paddingValue, paddingLimit, bitsStored, isSigned,
                outputSigned, bitsOutputLut, inversePaddingMLUT);
    }

    /**
     * @return 8 bits unsigned Lookup Table
     */
    public LookupTableCV getVOILookup(WlParams wl) {
        if (wl == null || wl.getLutShape() == null) {
            return null;
        }

        int minValue;
        int maxValue;
        /*
         * When pixel padding is activated, VOI LUT must extend to the min bit stored value when MONOCHROME2 and to the
         * max bit stored value when MONOCHROME1. See C.7.5.1.1.2
         */
        if (wl.isFillOutsideLutRange()
                || (desc.getPixelPaddingValue() != null && desc.getPhotometricInterpretation().isMonochrome())) {
            minValue = getMinAllocatedValue(wl);
            maxValue = getMaxAllocatedValue(wl);
        } else {
            minValue = (int) wl.getLevelMin();
            maxValue = (int) wl.getLevelMax();
        }

        return RGBImageVoiLut.createVoiLut(wl.getLutShape(), wl.getWindow(), wl.getLevel(), minValue, maxValue, 8,
                false, isPhotometricInterpretationInverse(wl.getPresentationState()));
    }

}
