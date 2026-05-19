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

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.image.nimble.opencv.ImageAnalyzer;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.lut.*;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

/**
 * DICOM image adapter for handling DICOM image attributes and transformations.
 * <p>
 * This class handles modality LUTs, VOI LUTs, and window/level adjustments for DICOM images. It also computes image
 * min/max values, applies pixel padding rules, and manages preset window/level collections.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageAdapter {

    /**
     * LUT cache map.
     */
    private static final Map<LutParameters, LookupTableCV> LUT_Cache = new ConcurrentHashMap();

    /**
     * Image descriptor.
     */
    private final ImageDescriptor desc;

    /**
     * Minimum and maximum value result.
     */
    private final MinMaxLocResult minMax;

    /**
     * Frame index.
     */
    private final int frameIndex;

    /**
     * Stored bit count.
     */
    private int bitsStored;

    /**
     * Window and level preset collection.
     */
    private List<PresetWindowLevel> windowingPresetCollection = null;

    /**
     * Creates a new instance.
     *
     * @param image      Planar image.
     * @param desc       Image descriptor.
     * @param frameIndex Frame index.
     */
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
        // Deferred calculation of image pixel transformation, because inner class Load is called from a separate
        // dedicated worker thread. Additionally, it will only be calculated once.
        // Considering the default pixel padding option is true, and inverse LUT operation is false
        getModalityLookup(null, false);
    }

    /**
     * Gets the image minimum and maximum values.
     *
     * @param image      Planar image.
     * @param desc       Image descriptor.
     * @param frameIndex Frame index.
     * @return Minimum and maximum value result.
     */
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
        // When not monochrome and no padding value, use default minimum and maximum values
        if (val == null) {
            val = ImageAnalyzer.findRawMinMaxValues(image, !monochrome);
        }
        return val;
    }

    /**
     * Finds the image minimum and maximum values.
     *
     * @param image      Planar image.
     * @param frameIndex Frame index.
     * @return Minimum and maximum value result.
     */
    private MinMaxLocResult findMinMaxValues(PlanarImage image, int frameIndex) {
        // This function can be called multiple times from inner class Load. min and max will only be calculated once.
        MinMaxLocResult val = getMinMaxValues(image, desc, frameIndex);
        // Cannot trust SmallestImagePixelValue and LargestImagePixelValue values! So need to search for minimum and
        // maximum values
        int bitsAllocated = desc.getBitsAllocated();
        if (bitsStored < bitsAllocated) {
            boolean isSigned = desc.isSigned();
            int minInValue = isSigned ? -(1 << (bitsStored - 1)) : 0;
            int maxInValue = isSigned ? (1 << (bitsStored - 1)) - 1 : (1 << bitsStored) - 1;
            if (val.minVal < minInValue || val.maxVal > maxInValue) {
                // When image contains values outside the stored bits, bits stored will be replaced by bits allocated,
                // to have a LUT that can handle all values.
                // Before finding minimum and maximum values, overlays in pixel data should be masked.
                setBitsStored(bitsAllocated);
            }
        }
        return val;
    }

    /**
     * Computes the image minimum and maximum values while excluding the supplied padding range.
     *
     * @param paddingValueMin Padding value excluded from the minimum value search.
     * @param paddingValueMax Padding value excluded from the maximum value search.
     * @return Minimum and maximum value result.
     */
    private static MinMaxLocResult findMinMaxValues(
            PlanarImage image,
            Integer paddingValueMin,
            Integer paddingValueMax) {
        MinMaxLocResult val;
        if (CvType.depth(image.type()) <= CvType.CV_8S) {
            val = new MinMaxLocResult();
            val.minVal = 0.0;
            val.maxVal = 255.0;
        } else {
            val = ImageAnalyzer.findMinMaxValues(image.toMat(), paddingValueMin, paddingValueMax);
            // Handle case where minimum and maximum are equal, e.g., black image
            // Maximum value+1 to display correct value
            if (val != null && val.minVal == val.maxVal) {
                val.maxVal += 1.0;
            }
        }
        return val;
    }

    /**
     * Gets the stored bit count.
     *
     * @return Stored bit count.
     */
    public int getBitsStored() {
        return bitsStored;
    }

    /**
     * Sets the stored bit count.
     *
     * @param bitsStored Stored bit count.
     */
    public void setBitsStored(int bitsStored) {
        this.bitsStored = bitsStored;
    }

    /**
     * Gets the minimum and maximum value result.
     *
     * @return Minimum and maximum value result.
     */
    public MinMaxLocResult getMinMax() {
        return minMax;
    }

    /**
     * Gets the image descriptor.
     *
     * @return Image descriptor.
     */
    public ImageDescriptor getImageDescriptor() {
        return desc;
    }

    /**
     * Gets the frame index.
     *
     * @return Frame index.
     */
    public int getFrameIndex() {
        return frameIndex;
    }

    /**
     * Gets the minimum allocated value.
     *
     * @param wl Window and level presentation.
     * @return Minimum allocated value.
     */
    public int getMinAllocatedValue(WlPresentation wl) {
        boolean signed = isModalityLutOutSigned(wl);
        int bitsAllocated = desc.getBitsAllocated();
        int maxValue = signed ? (1 << (bitsAllocated - 1)) - 1 : ((1 << bitsAllocated) - 1);
        return signed ? -(maxValue + 1) : 0;
    }

    /**
     * Gets the maximum allocated value.
     *
     * @param wl Window and level presentation.
     * @return Maximum allocated value.
     */
    public int getMaxAllocatedValue(WlPresentation wl) {
        boolean signed = isModalityLutOutSigned(wl);
        int bitsAllocated = desc.getBitsAllocated();
        return signed ? (1 << (bitsAllocated - 1)) - 1 : ((1 << bitsAllocated) - 1);
    }

    /**
     * When modality pixel transformation uses rescale slope and intercept, the output range can be signed even when the
     * pixel representation is unsigned.
     *
     * @param wl Window and level presentation.
     * @return true if the modality pixel transformation output can be signed.
     */
    public boolean isModalityLutOutSigned(WlPresentation wl) {
        boolean signed = desc.isSigned();
        return getMinValue(wl) < 0 || signed;
    }

    /**
     * Returns the minimum value after modality pixel transformation and pixel padding when present.
     *
     * @param wl Window and level presentation.
     * @return The minimum value after modality pixel transformation and pixel padding when present.
     */
    public double getMinValue(WlPresentation wl) {
        return minMaxValue(true, wl);
    }

    /**
     * Returns the maximum value after modality pixel transformation and pixel padding when present.
     *
     * @param wl Window and level presentation.
     * @return The maximum value after modality pixel transformation and pixel padding when present.
     */
    public double getMaxValue(WlPresentation wl) {
        return minMaxValue(false, wl);
    }

    /**
     * Computes the minimum or maximum value.
     *
     * @param minVal Whether to compute the minimum value.
     * @param wl     Window and level presentation.
     * @return Minimum or maximum value.
     */
    private double minMaxValue(boolean minVal, WlPresentation wl) {
        Number min = pixelToRealValue(minMax.minVal, wl);
        Number max = pixelToRealValue(minMax.maxVal, wl);
        if (min == null || max == null) {
            return 0;
        }
        // Calculate minimum and maximum values, because slope may be negative
        if (minVal) {
            return Math.min(min.doubleValue(), max.doubleValue());
        }
        return Math.max(min.doubleValue(), max.doubleValue());
    }

    /**
     * Gets the rescale intercept.
     *
     * @param dcm DICOM object.
     * @return Rescale intercept.
     */
    public double getRescaleIntercept(PresentationLutObject dcm) {
        if (dcm != null) {
            OptionalDouble prIntercept = dcm.getModalityLutModule().getRescaleIntercept();
            if (prIntercept.isPresent()) {
                return prIntercept.getAsDouble();
            }
        }
        return desc.getModalityLutForFrame(frameIndex).getRescaleIntercept().orElse(0.0);
    }

    /**
     * Gets the rescale slope.
     *
     * @param dcm DICOM object.
     * @return Rescale slope.
     */
    public double getRescaleSlope(PresentationLutObject dcm) {
        if (dcm != null) {
            OptionalDouble prSlope = dcm.getModalityLutModule().getRescaleSlope();
            if (prSlope.isPresent()) {
                return prSlope.getAsDouble();
            }
        }
        return desc.getModalityLutForFrame(frameIndex).getRescaleSlope().orElse(1.0);
    }

    /**
     * Gets the full dynamic range width.
     *
     * @param wl Window and level presentation.
     * @return Full dynamic range width.
     */
    public double getFullDynamicWidth(WlPresentation wl) {
        return getMaxValue(wl) - getMinValue(wl);
    }

    /**
     * Gets the full dynamic range center.
     *
     * @param wl Window and level presentation.
     * @return Full dynamic range center.
     */
    public double getFullDynamicCenter(WlPresentation wl) {
        double minValue = getMinValue(wl);
        double maxValue = getMaxValue(wl);
        return minValue + (maxValue - minValue) / 2.f;
    }

    /**
     * Returns the default preset as the first preset list element.
     *
     * @param wlp Window and level presentation.
     * @return The default preset as the first preset list element. This should never be null because at least one auto
     *         preset exists.
     */
    public PresetWindowLevel getDefaultPreset(WlPresentation wlp) {
        List<PresetWindowLevel> presetList = getPresetList(wlp);
        return (presetList != null && !presetList.isEmpty()) ? presetList.get(0) : null;
    }

    /**
     * Gets the preset list.
     *
     * @param wl Window and level presentation.
     * @return Preset list.
     */
    public synchronized List<PresetWindowLevel> getPresetList(WlPresentation wl) {
        return getPresetList(wl, false);
    }

    /**
     * Gets the preset list.
     *
     * @param wl     Window and level presentation.
     * @param reload Whether to reload the preset list.
     * @return Preset list.
     */
    public synchronized List<PresetWindowLevel> getPresetList(WlPresentation wl, boolean reload) {
        if (minMax != null && (windowingPresetCollection == null || reload)) {
            windowingPresetCollection = PresetWindowLevel.getPresetCollection(this, "[DICOM]", wl);
        }
        return windowingPresetCollection;
    }

    /**
     * Gets the preset collection size.
     *
     * @return Preset collection size.
     */
    public int getPresetCollectionSize() {
        if (windowingPresetCollection == null) {
            return 0;
        }
        return windowingPresetCollection.size();
    }

    /**
     * Gets the default LUT shape.
     *
     * @param wlp Window and level presentation.
     * @return Default LUT shape.
     */
    public LutShape getDefaultShape(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = getDefaultPreset(wlp);
        return (defaultPreset != null) ? defaultPreset.getLutShape() : LutShape.LINEAR;
    }

    /**
     * Gets the default window width.
     *
     * @param wlp Window and level presentation.
     * @return Default window width.
     */
    public double getDefaultWindow(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = getDefaultPreset(wlp);
        return (defaultPreset != null) ? defaultPreset.getWindow()
                : minMax == null ? 0.0 : minMax.maxVal - minMax.minVal;
    }

    /**
     * Gets the default window level.
     *
     * @param wlp Window and level presentation.
     * @return Default window level.
     */
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

    /**
     * Converts a pixel value to a real value.
     *
     * @param pixelValue Pixel value.
     * @param wlp        Window and level presentation.
     * @return Real value.
     */
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
     * DICOM PS 3.3 C.11.1 modality LUT module.
     *
     * @param wlp              Window and level presentation.
     * @param inverseLUTAction Whether to invert the LUT action.
     * @return Modality LUT.
     */
    public LookupTableCV getModalityLookup(WlPresentation wlp, boolean inverseLUTAction) {
        Integer paddingValue = desc.getPixelPaddingValue();
        boolean pixelPadding = wlp == null || wlp.isPixelPadding();
        PresentationLutObject pr = wlp != null && wlp.getPresentationState() instanceof PresentationLutObject
                ? (PresentationLutObject) wlp.getPresentationState()
                : null;
        LookupTableCV prModLut = (pr != null ? pr.getModalityLutModule().getLut().orElse(null) : null);
        final LookupTableCV mLUTSeq = prModLut == null ? desc.getModalityLutForFrame(frameIndex).getLut().orElse(null)
                : prModLut;
        if (mLUTSeq != null) {
            if (!pixelPadding || paddingValue == null) {
                if (minMax.minVal >= mLUTSeq.getOffset()
                        && minMax.maxVal < mLUTSeq.getOffset() + mLUTSeq.getNumEntries()) {
                    return mLUTSeq;
                } else if (prModLut == null) {
                    Logger.warn(
                            false,
                            "Image",
                            "Pixel values doesn't match to Modality LUT sequence table. So the Modality LUT is not applied.");
                }
            } else {
                Logger.warn(false, "Image", "Cannot apply Modality LUT sequence and Pixel Padding");
            }
        }
        boolean inverseLut = isPhotometricInterpretationInverse(pr);
        if (pixelPadding) {
            inverseLut ^= inverseLUTAction;
        }
        LutParameters lutParams = getLutParameters(pixelPadding, mLUTSeq, inverseLut, pr);
        // No modality lookup table needed
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

    /**
     * Determines whether the photometric interpretation must be inverted.
     *
     * @param pr Presentation state LUT.
     * @return true if inversion is required; otherwise false.
     */
    public boolean isPhotometricInterpretationInverse(PresentationStateLut pr) {
        Optional<String> prLUTShape = pr == null ? Optional.empty() : pr.getPrLutShapeMode();
        Photometric p = desc.getPhotometricInterpretation();
        return prLUTShape.map("INVERSE"::equals).orElseGet(() -> p == Photometric.MONOCHROME1);
    }

    /**
     * Gets the LUT parameters.
     *
     * @param pixelPadding       Whether pixel padding is enabled.
     * @param mLUTSeq            Modality LUT sequence.
     * @param inversePaddingMLUT Whether to invert the padded modality LUT.
     * @param pr                 Presentation LUT object.
     * @return LUT parameters.
     */
    public LutParameters getLutParameters(
            boolean pixelPadding,
            LookupTableCV mLUTSeq,
            boolean inversePaddingMLUT,
            PresentationLutObject pr) {
        Integer paddingValue = desc.getPixelPaddingValue();
        boolean isSigned = desc.isSigned();
        double intercept = getRescaleIntercept(pr);
        double slope = getRescaleSlope(pr);
        // No modality lookup table needed
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
                // Allow 8-bit images to handle negative values
                bitsOutputLut = 9;
            }
        } else {
            bitsOutputLut = mLUTSeq.getDataType() == DataBuffer.TYPE_BYTE ? 8 : 16;
        }
        return new LutParameters(intercept, slope, pixelPadding, paddingValue, paddingLimit, bitsStored, isSigned,
                outputSigned, bitsOutputLut, inversePaddingMLUT);
    }

    /**
     * Returns an 8-bit unsigned VOI lookup table.
     *
     * @param wl Window and level parameters.
     * @return 8-bit unsigned lookup table.
     */
    public LookupTableCV getVOILookup(WlParams wl) {
        if (wl == null || wl.getLutShape() == null) {
            return null;
        }
        int minValue;
        int maxValue;
        // When pixel padding is activated, VOI LUT must be extended to minimum stored bit value (MONOCHROME2) and
        // maximum stored bit value (MONOCHROME1). See C.7.5.1.1.2
        if (wl.isFillOutsideLutRange()
                || (desc.getPixelPaddingValue() != null && desc.getPhotometricInterpretation().isMonochrome())) {
            minValue = getMinAllocatedValue(wl);
            maxValue = getMaxAllocatedValue(wl);
        } else {
            minValue = (int) wl.getLevelMin();
            maxValue = (int) wl.getLevelMax();
        }
        return RGBImageVoiLut.createVoiLut(
                wl.getLutShape(),
                wl.getWindow(),
                wl.getLevel(),
                minValue,
                maxValue,
                8,
                false,
                isPhotometricInterpretationInverse(wl.getPresentationState()));
    }

}
