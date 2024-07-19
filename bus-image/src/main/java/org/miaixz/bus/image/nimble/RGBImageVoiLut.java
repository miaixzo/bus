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

import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.lut.LutParameters;
import org.miaixz.bus.image.nimble.opencv.lut.LutShape;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;

import java.awt.image.DataBuffer;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RGBImageVoiLut {

    public static PlanarImage bgr2rgb(PlanarImage img) {
        if (img != null && img.channels() > 1) {
            ImageCV dstImg = new ImageCV();
            Imgproc.cvtColor(img.toMat(), dstImg, Imgproc.COLOR_BGR2RGB);
            return dstImg;
        }
        return img;
    }

    public static String getModality(Attributes dcm) {
        String modality = dcm.getString(Tag.Modality);
        if (modality == null) {
            Attributes parent = dcm.getParent();
            while (parent != null) {
                modality = parent.getString(Tag.Modality);
                if (modality != null) {
                    break;
                }
                parent = parent.getParent();
            }
        }
        return modality;
    }

    public static PlanarImage getRGBImageFromPaletteColorModel(PlanarImage source, Attributes ds) {
        // Convert images with PaletteColorModel to RGB model
        if (ds != null) {
            int[] rDesc = lutDescriptor(ds, Tag.RedPaletteColorLookupTableDescriptor);
            int[] gDesc = lutDescriptor(ds, Tag.GreenPaletteColorLookupTableDescriptor);
            int[] bDesc = lutDescriptor(ds, Tag.BluePaletteColorLookupTableDescriptor);
            byte[] r =
                    lutData(
                            ds,
                            rDesc,
                            Tag.RedPaletteColorLookupTableData,
                            Tag.SegmentedRedPaletteColorLookupTableData);
            byte[] g =
                    lutData(
                            ds,
                            gDesc,
                            Tag.GreenPaletteColorLookupTableData,
                            Tag.SegmentedGreenPaletteColorLookupTableData);
            byte[] b =
                    lutData(
                            ds,
                            bDesc,
                            Tag.BluePaletteColorLookupTableData,
                            Tag.SegmentedBluePaletteColorLookupTableData);

            if (source.depth() <= CvType.CV_8S && rDesc[1] == 0 && gDesc[1] == 0 && bDesc[1] == 0) {
                // Replace the original image with the RGB image.
                return ImageProcessor.applyLUT(source.toMat(), new byte[][]{b, g, r});
            } else {
                LookupTableCV lookup =
                        new LookupTableCV(
                                new byte[][]{b, g, r}, new int[]{bDesc[1], gDesc[1], rDesc[1]}, true);
                return lookup.lookup(source.toMat());
            }
        }
        return source;
    }

    /**
     * @param dicomLutObject defines LUT data dicom structure
     * @return LookupTableJAI object if Data Element and Descriptors are consistent
     */
    public static Optional<LookupTableCV> createLut(Attributes dicomLutObject) {
        if (dicomLutObject == null || dicomLutObject.isEmpty()) {
            return Optional.empty();
        }

        LookupTableCV lookupTable = null;

        // Three values of the LUT Descriptor describe the format of the LUT Data in the corresponding
        // Data Element
        int[] descriptor =
                Builder.getIntArrayFromDicomElement(dicomLutObject, Tag.LUTDescriptor, null);

        if (descriptor == null) {
            Logger.debug("Missing LUT Descriptor");
        } else if (descriptor.length != 3) {
            Logger.debug("Illegal number of LUT Descriptor values \"{}\"", descriptor.length);
        } else {
            // First value is the number of entries in the lookup table.
            // When this value is 0 the number of table entries is equal to 65536 <=> 0x10000.
            int numEntries = (descriptor[0] <= 0) ? descriptor[0] + 0x10000 : descriptor[0];

            // Second value is mapped to the first entry in the LUT.
            int offset =
                    (short) descriptor[1]; // necessary to cast in order to get negative value when present

            // Third value specifies the number of bits for each entry in the LUT Data.
            int numBits = descriptor[2];

            int dataLength = 0; // number of entry values in the LUT Data.

            // LUT Data contains the LUT entry values, assuming data is always unsigned data
            byte[] bData = null;
            try {
                bData = dicomLutObject.getBytes(Tag.LUTData);
            } catch (IOException e) {
                Logger.error("Cannot get byte[] of {}", Tag.toString(Tag.LUTData), e);
            }

            if (bData == null || bData.length == 0) {
                return Optional.empty();
            }

            if (numBits <= 8) { // LUT Data should be stored in 8 bits allocated format
                if (numEntries <= 256 && (bData.length == (numEntries << 1))) {
                    // Some implementations have encoded 8 bit entries with 16 bits allocated, padding the
                    // high bits

                    byte[] bDataNew = new byte[numEntries];
                    int byteShift = (dicomLutObject.bigEndian() ? 1 : 0);
                    for (int i = 0; i < bDataNew.length; i++) {
                        bDataNew[i] = bData[(i << 1) | byteShift];
                    }

                    dataLength = bDataNew.length;
                    lookupTable = new LookupTableCV(bDataNew, offset);

                } else {
                    dataLength = bData.length;
                    lookupTable = new LookupTableCV(bData, offset); // LUT entry value range should be [0,255]
                }
            } else if (numBits <= 16) { // LUT Data should be stored in 16 bits allocated format
                // LUT Data contains the LUT entry values, assuming data is always unsigned data
                short[] sData = new short[numEntries];
                ByteKit.bytesToShort(bData, sData, 0, sData.length, dicomLutObject.bigEndian());

                if (numEntries <= 256) {
                    // Some implementations have encoded 8 bit entries with 16 bits allocated, padding the
                    // high bits
                    int maxIn = (1 << numBits) - 1;
                    int maxOut = numEntries - 1;

                    byte[] bDataNew = new byte[numEntries];
                    for (int i = 0; i < numEntries; i++) {
                        bDataNew[i] = (byte) ((sData[i] & 0xffff) * maxOut / maxIn);
                    }
                    dataLength = bDataNew.length;
                    lookupTable = new LookupTableCV(bDataNew, offset);
                } else {
                    // LUT Data contains the LUT entry values, assuming data is always unsigned data
                    dataLength = sData.length;
                    lookupTable = new LookupTableCV(sData, offset, true);
                }
            } else {
                Logger.debug("Illegal number of bits for each entry in the LUT Data");
            }

            if (lookupTable != null) {
                if (dataLength != numEntries) {
                    Logger.debug(
                            "LUT Data length \"{}\" mismatch number of entries \"{}\" in LUT Descriptor ",
                            dataLength,
                            numEntries);
                }
                if (dataLength > (1 << numBits)) {
                    Logger.debug(
                            "Illegal LUT Data length \"{}\" with respect to the number of bits in LUT descriptor \"{}\"",
                            dataLength,
                            numBits);
                }
            }
        }
        return Optional.ofNullable(lookupTable);
    }

    /**
     * Minimum output is given for input value below (level - window/2)
     * Maximum output is given for input value above (level + window/2)
     * These Minimum and Maximum values depends on bitsStored and signed given attributes. ie :
     * - when bitsStored=8 bits unsigned => minOutValue=0 and maxOutValue=255
     * - when bitsStored=8 bits signed => minOutValue=-128 and maxOutValue=127
     * - when bitsStored=16 bits unsigned => minOutValue= 0 and maxOutValue= 65535
     * - when bitsStored=16 bits signed => minOutValue= -32768 and maxOutValue= 32767
     *
     * @param lutShape
     * @param window
     * @param level
     * @param minValue
     * @param maxValue
     * @param bitsStored
     * @param isSigned
     * @param inverse
     * @return a LookupTableJAI for data between minValue and maxValue according to all given
     * parameters
     */
    public static LookupTableCV createVoiLut(
            LutShape lutShape,
            double window,
            double level,
            int minValue,
            int maxValue,
            int bitsStored,
            boolean isSigned,
            boolean inverse) {

        if (lutShape == null) {
            return null;
        }

        int bStored = bitsStored > 16 ? 16 : (bitsStored < 1) ? 1 : bitsStored;
        double win = window < 1.0 ? 1.0 : window;

        int bitsAllocated = (bStored <= 8) ? 8 : 16;
        int outRangeSize = (1 << bitsAllocated) - 1;
        int maxOutValue = isSigned ? (1 << (bitsAllocated - 1)) - 1 : outRangeSize;
        int minOutValue = isSigned ? -(maxOutValue + 1) : 0;

        int minInValue = Math.min(maxValue, minValue);
        int maxInValue = Math.max(maxValue, minValue);

        int numEntries = maxInValue - minInValue + 1;
        Object outLut = bStored <= 8 ? new byte[numEntries] : new short[numEntries];

        if (lutShape.getFunctionType() != null) {
            switch (lutShape.getFunctionType()) {
                case LINEAR:
                    setWindowLevelLinearLut(
                            win, level, minInValue, outLut, minOutValue, maxOutValue, inverse);
                    break;
                case SIGMOID:
                    setWindowLevelSigmoidLut(
                            win, level, minInValue, outLut, minOutValue, maxOutValue, inverse);
                    break;
                case SIGMOID_NORM:
                    setWindowLevelSigmoidLut(
                            win, level, minInValue, outLut, minOutValue, maxOutValue, inverse, true);
                    break;
                case LOG:
                    setWindowLevelLogarithmicLut(
                            win, level, minInValue, outLut, minOutValue, maxOutValue, inverse);
                    break;
                case LOG_INV:
                    setWindowLevelExponentialLut(
                            win, level, minInValue, outLut, minOutValue, maxOutValue, inverse);
                    break;
                default:
                    return null;
            }
        } else {
            setWindowLevelSequenceLut(
                    win,
                    level,
                    lutShape.getLookup(),
                    minInValue,
                    maxInValue,
                    outLut,
                    minOutValue,
                    maxOutValue,
                    inverse);
        }

        return (outLut instanceof byte[])
                ? new LookupTableCV((byte[]) outLut, minInValue)
                : //
                new LookupTableCV((short[]) outLut, minInValue, isSigned);
    }

    /**
     * @return LookupTable with full range of possible input entries according to bitStored.<br>
     * Note that isSigned is relevant for both input and output values
     */
    public static LookupTableCV createRescaleRampLut(LutParameters params) {
        return createRescaleRampLut(
                params.getIntercept(),
                params.getSlope(),
                params.getBitsStored(),
                params.isSigned(),
                params.isOutputSigned(),
                params.getBitsOutput());
    }

    public static LookupTableCV createRescaleRampLut(
            double intercept,
            double slope,
            int bitsStored,
            boolean isSigned,
            boolean outputSigned,
            int bitsOutput) {

        return createRescaleRampLut(
                intercept,
                slope,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                bitsStored,
                isSigned,
                false,
                outputSigned,
                bitsOutput);
    }

    public static LookupTableCV createRescaleRampLut(
            double intercept,
            double slope,
            int minValue,
            int maxValue,
            int bitsStored,
            boolean isSigned,
            boolean inverse,
            boolean outputSigned,
            int bitsOutput) {

        int stored = (bitsStored > 16) ? 16 : ((bitsStored < 1) ? 1 : bitsStored);

        int bitsOutLut = bitsOutput <= 8 ? 8 : 16;
        int outRangeSize = (1 << bitsOutLut) - 1;
        int maxOutValue = outputSigned ? (1 << (bitsOutLut - 1)) - 1 : outRangeSize;
        int minOutValue = outputSigned ? -(maxOutValue + 1) : 0;

        int minInValue = isSigned ? -(1 << (stored - 1)) : 0;
        int maxInValue = isSigned ? (1 << (stored - 1)) - 1 : (1 << stored) - 1;

        minInValue = Math.max(minInValue, maxValue < minValue ? maxValue : minValue);
        maxInValue = Math.min(maxInValue, maxValue < minValue ? minValue : maxValue);

        int numEntries = maxInValue - minInValue + 1;
        Object outLut = (bitsOutLut == 8) ? new byte[numEntries] : new short[numEntries];

        for (int i = 0; i < numEntries; i++) {
            int value = (int) Math.round((i + minInValue) * slope + intercept);

            value = (value >= maxOutValue) ? maxOutValue : ((value <= minOutValue) ? minOutValue : value);
            value = inverse ? (maxOutValue + minOutValue - value) : value;

            if (outLut instanceof byte[]) {
                Array.set(outLut, i, (byte) value);
            } else if (outLut instanceof short[]) {
                Array.set(outLut, i, (short) value);
            }
        }

        return (outLut instanceof byte[])
                ? new LookupTableCV((byte[]) outLut, minInValue)
                : //
                new LookupTableCV((short[]) outLut, minInValue, !outputSigned);
    }

    /**
     * Apply the pixel padding to the modality LUT
     */
    public static void applyPixelPaddingToModalityLUT(
            LookupTableCV modalityLookup, LutParameters lutparams) {
        if (modalityLookup != null
                && lutparams.isApplyPadding()
                && lutparams.getPaddingMinValue() != null
                && modalityLookup.getDataType() <= DataBuffer.TYPE_SHORT) {

            int paddingValue = lutparams.getPaddingMinValue();
            Integer paddingLimit = lutparams.getPaddingMaxValue();
            int paddingValueMin =
                    (paddingLimit == null) ? paddingValue : Math.min(paddingValue, paddingLimit);
            int paddingValueMax =
                    (paddingLimit == null) ? paddingValue : Math.max(paddingValue, paddingLimit);

            int numPaddingValues = paddingValueMax - paddingValueMin + 1;
            int paddingValuesStartIndex = paddingValueMin - modalityLookup.getOffset();

            if (paddingValuesStartIndex >= modalityLookup.getNumEntries()) {
                return;
            }

            if (paddingValuesStartIndex < 0) {
                numPaddingValues += paddingValuesStartIndex;
                if (numPaddingValues < 1) {
                    // No padding value in the LUT range
                    return;
                }
                paddingValuesStartIndex = 0;
            }

            Object inLut;
            // if FALSE DataBuffer Type is supposed to be either TYPE_SHORT or TYPE_USHORT
            final boolean isDataTypeByte = modalityLookup.getDataType() == DataBuffer.TYPE_BYTE;
            if (isDataTypeByte) {
                inLut = modalityLookup.getByteData(0);
            } else {
                inLut = modalityLookup.getShortData(0);
            }

            Object outLut = inLut;
            if (isDataTypeByte) {
                byte fillVal = lutparams.isInversePaddingMLUT() ? (byte) 255 : (byte) 0;
                byte[] data = (byte[]) outLut;
                Arrays.fill(
                        data, paddingValuesStartIndex, paddingValuesStartIndex + numPaddingValues, fillVal);
            } else {
                short[] data = (short[]) outLut;
                short fillVal = lutparams.isInversePaddingMLUT() ? data[data.length - 1] : data[0];
                Arrays.fill(
                        data, paddingValuesStartIndex, paddingValuesStartIndex + numPaddingValues, fillVal);
            }
        }
    }

    private static void setWindowLevelLinearLutLegacy(
            double window,
            double level,
            int minInValue,
            Object outLut,
            int minOutValue,
            int maxOutValue,
            boolean inverse) {

        /** Pseudo code defined in Dicom Standard 2011 - PS 3.3 § C.11.2 VOI LUT Module */
        double lowLevel = (level - 0.5) - (window - 1.0) / 2.0;
        double highLevel = (level - 0.5) + (window - 1.0) / 2.0;

        for (int i = 0; i < Array.getLength(outLut); i++) {
            int value;

            if ((i + minInValue) <= lowLevel) {
                value = minOutValue;
            } else if ((i + minInValue) > highLevel) {
                value = maxOutValue;
            } else {
                value =
                        (int)
                                ((((i + minInValue) - (level - 0.5)) / (window - 1.0) + 0.5)
                                        * (maxOutValue - minOutValue)
                                        + minOutValue);
            }

            value = (value >= maxOutValue) ? maxOutValue : ((value <= minOutValue) ? minOutValue : value);
            value = inverse ? (maxOutValue + minOutValue - value) : value;

            if (outLut instanceof byte[]) {
                Array.set(outLut, i, (byte) value);
            } else if (outLut instanceof short[]) {
                Array.set(outLut, i, (short) value);
            }
        }
    }

    private static void setWindowLevelLinearLut(
            double window,
            double level,
            int minInValue,
            Object outLut,
            int minOutValue,
            int maxOutValue,
            boolean inverse) {

        double slope = (maxOutValue - minOutValue) / window;
        double intercept = maxOutValue - slope * (level + (window / 2.0));

        for (int i = 0; i < Array.getLength(outLut); i++) {
            int value = (int) ((i + minInValue) * slope + intercept);

            value = (value >= maxOutValue) ? maxOutValue : ((value <= minOutValue) ? minOutValue : value);
            value = inverse ? (maxOutValue + minOutValue - value) : value;

            if (outLut instanceof byte[]) {
                Array.set(outLut, i, (byte) value);
            } else if (outLut instanceof short[]) {
                Array.set(outLut, i, (short) value);
            }
        }
    }

    private static void setWindowLevelSigmoidLut(
            double width,
            double center,
            int minInValue,
            Object outLut,
            int minOutValue,
            int maxOutValue,
            boolean inverse) {

        setWindowLevelSigmoidLut(
                width, center, minInValue, outLut, minOutValue, maxOutValue, inverse, false);
    }

    private static void setWindowLevelSigmoidLut(
            double width,
            double center,
            int minInValue,
            Object outLut,
            int minOutValue,
            int maxOutValue,
            boolean inverse,
            boolean normalize) {

        double nFactor = -20d; // factor defined by default in Dicom standard ( -20*2/10 = -4 )
        double outRange = maxOutValue - (double) minOutValue;

        double minValue = 0;
        double outRescaleRatio = 1;

        if (normalize) {
            double lowLevel = center - width / 2d;
            double highLevel = center + width / 2d;

            minValue =
                    minOutValue
                            + outRange / (1d + Math.exp((2d * nFactor / 10d) * (lowLevel - center) / width));
            double maxValue =
                    minOutValue
                            + outRange / (1d + Math.exp((2d * nFactor / 10d) * (highLevel - center) / width));
            outRescaleRatio = (maxOutValue - minOutValue) / Math.abs(maxValue - minValue);
        }

        for (int i = 0; i < Array.getLength(outLut); i++) {
            double value =
                    outRange / (1d + Math.exp((2d * nFactor / 10d) * (i + minInValue - center) / width));

            if (normalize) {
                value = (value - minValue) * outRescaleRatio;
            }

            value = (int) Math.round(value + minOutValue);
            value =
                    (int)
                            ((value > maxOutValue) ? maxOutValue : ((value < minOutValue) ? minOutValue : value));
            value = (int) (inverse ? (maxOutValue + minOutValue - value) : value);

            if (outLut instanceof byte[]) {
                Array.set(outLut, i, (byte) value);
            } else if (outLut instanceof short[]) {
                Array.set(outLut, i, (short) value);
            }
        }
    }

    private static void setWindowLevelExponentialLut(
            double width,
            double center,
            int minInValue,
            Object outLut,
            int minOutValue,
            int maxOutValue,
            boolean inverse) {

        setWindowLevelExponentialLut(
                width, center, minInValue, outLut, minOutValue, maxOutValue, inverse, true);
    }

    private static void setWindowLevelExponentialLut(
            double width,
            double center,
            int minInValue,
            Object outLut,
            int minOutValue,
            int maxOutValue,
            boolean inverse,
            boolean normalize) {

        double nFactor = 20d;
        double outRange = maxOutValue - (double) minOutValue;

        double minValue = 0;
        double outRescaleRatio = 1;

        if (normalize) {
            double lowLevel = center - width / 2d;
            double highLevel = center + width / 2d;

            minValue = minOutValue + outRange * Math.exp((nFactor / 10d) * (lowLevel - center) / width);
            double maxValue =
                    minOutValue + outRange * Math.exp((nFactor / 10d) * (highLevel - center) / width);
            outRescaleRatio = (maxOutValue - minOutValue) / Math.abs(maxValue - minValue);
        }

        for (int i = 0; i < Array.getLength(outLut); i++) {
            double value = outRange * Math.exp((nFactor / 10d) * (i + minInValue - center) / width);

            if (normalize) {
                value = (value - minValue) * outRescaleRatio;
            }

            value = (int) Math.round(value + minOutValue);
            value =
                    (int)
                            ((value > maxOutValue) ? maxOutValue : ((value < minOutValue) ? minOutValue : value));
            value = (int) (inverse ? (maxOutValue + minOutValue - value) : value);

            if (outLut instanceof byte[]) {
                Array.set(outLut, i, (byte) value);
            } else if (outLut instanceof short[]) {
                Array.set(outLut, i, (short) value);
            }
        }
    }

    private static void setWindowLevelLogarithmicLut(
            double width,
            double center,
            int minInValue,
            Object outLut,
            int minOutValue,
            int maxOutValue,
            boolean inverse) {

        setWindowLevelLogarithmicLut(
                width, center, minInValue, outLut, minOutValue, maxOutValue, inverse, true);
    }

    private static void setWindowLevelLogarithmicLut(
            double width,
            double center,
            int minInValue,
            Object outLut,
            int minOutValue,
            int maxOutValue,
            boolean inverse,
            boolean normalize) {

        double nFactor = 20d;
        double outRange = maxOutValue - (double) minOutValue;

        double minValue = 0;
        double outRescaleRatio = 1;

        if (normalize) {
            double lowLevel = center - width / 2d;
            double highLevel = center + width / 2d;

            minValue =
                    minOutValue + outRange * Math.log((nFactor / 10d) * (1 + (lowLevel - center) / width));
            double maxValue =
                    minOutValue + outRange * Math.log((nFactor / 10d) * (1 + (highLevel - center) / width));

            outRescaleRatio = (maxOutValue - minOutValue) / Math.abs(maxValue - minValue);
        }

        for (int i = 0; i < Array.getLength(outLut); i++) {
            double value = outRange * Math.log((nFactor / 10d) * (1 + (i + minInValue - center) / width));

            if (normalize) {
                value = (value - minValue) * outRescaleRatio;
            }

            value = (int) Math.round(value + minOutValue);
            value =
                    (int)
                            ((value > maxOutValue) ? maxOutValue : ((value < minOutValue) ? minOutValue : value));
            value = (int) (inverse ? (maxOutValue + minOutValue - value) : value);

            if (outLut instanceof byte[]) {
                Array.set(outLut, i, (byte) value);
            } else if (outLut instanceof short[]) {
                Array.set(outLut, i, (short) value);
            }
        }
    }

    private static Object getLutDataArray(LookupTableCV lookup) {
        Object lutDataArray = null;
        if (lookup != null) {
            if (lookup.getDataType() == DataBuffer.TYPE_BYTE) {
                lutDataArray = lookup.getByteData(0);
            } else if (lookup.getDataType() <= DataBuffer.TYPE_SHORT) {
                lutDataArray = lookup.getShortData(0);
            }
        }
        return lutDataArray;
    }

    /**
     * @param width
     * @param center
     * @param lookupSequence
     * @param minInValue
     * @param maxInValue
     * @param outLut
     * @param minOutValue
     * @param maxOutValue
     * @param inverse
     * @return a normalized LookupTableJAI based upon given lutSequence <br>
     */
    private static void setWindowLevelSequenceLut(
            double width,
            double center,
            LookupTableCV lookupSequence,
            int minInValue,
            int maxInValue,
            Object outLut,
            int minOutValue,
            int maxOutValue,
            boolean inverse) {

        final Object inLutDataArray = getLutDataArray(lookupSequence);

        if (inLutDataArray == null) {
            return;
        }

        // Use this mask to get positive value assuming inLutData value is always unsigned
        final int lutDataValueMask =
                inLutDataArray instanceof byte[]
                        ? 0x000000FF
                        : (inLutDataArray instanceof short[] ? 0x0000FFFF : 0xFFFFFFFF);

        double lowLevel = center - width / 2.0;
        double highLevel = center + width / 2.0;

        int maxInLutIndex = Array.getLength(inLutDataArray) - 1;
        int minLookupValue = Integer.MAX_VALUE;
        int maxLookupValue = Integer.MIN_VALUE;
        for (int i = 0; i < Array.getLength(inLutDataArray); i++) {
            int val = lutDataValueMask & Array.getInt(inLutDataArray, i);
            if (val < minLookupValue) {
                minLookupValue = val;
            }
            if (val > maxLookupValue) {
                maxLookupValue = val;
            }
        }
        int lookupValueRange = Math.abs(maxLookupValue - minLookupValue);

        double widthRescaleRatio = maxInLutIndex / width;
        double outRescaleRatio = (maxOutValue - minOutValue) / (double) lookupValueRange;

        for (int i = 0; i < Array.getLength(outLut); i++) {
            int value;
            double inValueRescaled;

            if ((i + minInValue) <= lowLevel) {
                inValueRescaled = 0;
            } else if ((i + minInValue) > highLevel) {
                inValueRescaled = maxInLutIndex;
            } else {
                inValueRescaled = (i + minInValue - lowLevel) * widthRescaleRatio;
            }

            int inValueRoundDown = Math.max(0, (int) Math.floor(inValueRescaled));
            int inValueRoundUp = Math.min(maxInLutIndex, (int) Math.ceil(inValueRescaled));

            int valueDown = lutDataValueMask & Array.getInt(inLutDataArray, inValueRoundDown);
            int valueUp = lutDataValueMask & Array.getInt(inLutDataArray, inValueRoundUp);

            // Linear Interpolation of the output value with respect to the rescaled ratio
            value =
                    (int)
                            ((inValueRoundUp == inValueRoundDown)
                                    ? valueDown
                                    : Math.round(
                                    valueDown
                                            + (inValueRescaled - inValueRoundDown)
                                            * (valueUp - valueDown)
                                            / (inValueRoundUp - inValueRoundDown)));

            value = (int) Math.round(value * outRescaleRatio);

            value = (value >= maxOutValue) ? maxOutValue : ((value <= minOutValue) ? minOutValue : value);
            value = inverse ? (maxOutValue + minOutValue - value) : value;

            if (outLut instanceof byte[]) {
                Array.set(outLut, i, (byte) value);
            } else if (outLut instanceof short[]) {
                Array.set(outLut, i, (short) value);
            }
        }
    }

    public static double pixel2rescale(LookupTableCV lookup, double pixelValue) {
        if (lookup != null) {
            if (pixelValue >= lookup.getOffset()
                    && pixelValue <= lookup.getOffset() + lookup.getNumEntries() - 1) {
                return lookup.lookup(0, (int) pixelValue);
            }
        }
        return pixelValue;
    }

    public static double pixel2rescale(Attributes dcm, double pixelValue) {
        if (dcm != null) {
            // value = pixelValue * rescale slope + intercept value
            Double slope = Builder.getDoubleFromDicomElement(dcm, Tag.RescaleSlope, null);
            Double intercept = Builder.getDoubleFromDicomElement(dcm, Tag.RescaleIntercept, null);
            if (slope != null || intercept != null) {
                return pixelValue * (slope == null ? 1.0 : slope) + (intercept == null ? 0.0 : intercept);
            }
        }
        return pixelValue;
    }

    public static Optional<byte[]> getByteData(Attributes dicom, int tag) {
        return getByteData(dicom, null, tag);
    }

    public static Optional<byte[]> getByteData(Attributes dicom, String privateCreator, int tag) {
        if (dicom == null) {
            return Optional.empty();
        }
        if (!dicom.containsValue(privateCreator, tag)) {
            return Optional.empty();
        }
        byte[] bData = null;
        try {
            bData = dicom.getBytes(tag);
        } catch (IOException e) {
            Logger.error("Getting byte data from {}", Tag.toString(tag), e);
        }
        return Optional.ofNullable(bData);
    }

    public static int[] lutDescriptor(Attributes ds, int descTag) {
        int[] desc = ds.getInts(descTag);
        if (desc == null) {
            throw new IllegalArgumentException("Missing LUT Descriptor!");
        }
        if (desc.length != 3) {
            throw new IllegalArgumentException("Illegal number of LUT Descriptor values: " + desc.length);
        }
        if (desc[0] < 0) {
            throw new IllegalArgumentException("Illegal LUT Descriptor: len=" + desc[0]);
        }
        int bits = desc[2];
        if (bits != 8 && bits != 16) {
            throw new IllegalArgumentException("Illegal LUT Descriptor: bits=" + bits);
        }
        return desc;
    }

    public static byte[] lutData(Attributes ds, int[] desc, int dataTag, int segmTag) {
        int len = desc[0] <= 0 ? desc[0] + 0x10000 : desc[0];
        int bits = desc[2];
        Optional<byte[]> odata = getByteData(ds, dataTag);
        byte[] data;
        if (odata.isEmpty()) {
            int[] lut = ds.getInts(segmTag);
            if (lut == null) {
                throw new IllegalArgumentException("Missing LUT Data!");
            }
            if (bits == 8) {
                throw new IllegalArgumentException("Segmented LUT Data with LUT Descriptor: bits=8");
            }
            data = new byte[len];
            new InflateSegmentedLut(lut, 0, data, 0).inflate(-1, 0);
        } else if (bits == 16 || odata.get().length != len) {
            data = odata.get();
            if (data.length != len << 1) {
                throw new IllegalArgumentException(
                        "Number of actual LUT entries: "
                                + data.length
                                + " mismatch specified value: "
                                + len
                                + " in LUT Descriptor");
            }

            int hilo = ds.bigEndian() ? 0 : 1;
            if (bits == 8) {
                hilo = 1 - hilo; // padded high bits -> use low bits
            }
            data = halfLength(data, hilo);
        } else {
            data = odata.get();
        }
        return data;
    }

    private static byte[] halfLength(byte[] data, int hilo) {
        byte[] bs = new byte[data.length >> 1];
        for (int i = 0; i < bs.length; i++) bs[i] = data[(i << 1) | hilo];

        return bs;
    }

    private static class InflateSegmentedLut {
        final int[] segm;
        final byte[] data;
        int readPos;
        int writePos;

        private InflateSegmentedLut(int[] segm, int readPos, byte[] data, int writePos) {
            this.segm = segm;
            this.data = data;
            this.readPos = readPos;
            this.writePos = writePos;
        }

        private int inflate(int segs, int y0) {
            while (segs < 0 ? (readPos < segm.length) : segs-- > 0) {
                int segPos = readPos;
                int op = read();
                int n = read();
                switch (op) {
                    case 0:
                        y0 = discreteSegment(n);
                        break;
                    case 1:
                        if (writePos == 0)
                            throw new IllegalArgumentException("Linear segment cannot be the first segment");
                        y0 = linearSegment(n, y0, read());
                        break;
                    case 2:
                        if (segs >= 0)
                            throw new IllegalArgumentException("nested indirect segment at index " + segPos);
                        y0 = indirectSegment(n, y0);
                        break;
                    default:
                        throw new IllegalArgumentException("illegal op code " + op + " at index" + segPos);
                }
            }
            return y0;
        }

        private int read() {
            if (readPos >= segm.length) {
                throw new IllegalArgumentException("Running out of data inflating segmented LUT");
            }
            return segm[readPos++] & 0xffff;
        }

        private void write(int y) {
            if (writePos >= data.length) {
                throw new IllegalArgumentException(
                        "Number of entries in inflated segmented LUT exceeds specified value: "
                                + data.length
                                + " in LUT Descriptor");
            }
            data[writePos++] = (byte) (y >> 8);
        }

        private int discreteSegment(int n) {
            while (n-- > 0) write(read());
            return segm[readPos - 1] & 0xffff;
        }

        private int linearSegment(int n, int y0, int y1) {
            int dy = y1 - y0;
            for (int j = 1; j <= n; j++) write(y0 + dy * j / n);
            return y1;
        }

        private int indirectSegment(int n, int y0) {
            int readPos = read() | (read() << 16);
            return new InflateSegmentedLut(segm, readPos, data, writePos).inflate(n, y0);
        }
    }

}
