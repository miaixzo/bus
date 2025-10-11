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

import java.awt.image.*;

import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * A factory class for creating and managing Lookup Tables (LUTs) for DICOM image rendering.
 * <p>
 * This class is responsible for processing Modality LUTs, VOI (Value of Interest) LUTs, and Presentation LUTs to
 * correctly transform stored pixel values into display values. It supports automatic and manual window/level
 * adjustments, as well as special handling for various DICOM image types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LookupTableFactory {

    /**
     * Array of SOP Class UIDs for X-Ray Angiography and Radiofluoroscopy images, which have special LUT handling.
     */
    private static final String[] XA_XRF_CUIDS = { UID.XRayAngiographicImageStorage.uid,
            UID.XRayRadiofluoroscopicImageStorage.uid, UID.XRayAngiographicBiPlaneImageStorage.uid };
    /**
     * Keywords for Pixel Intensity Relationship that indicate special LUT processing.
     */
    private static final String[] LOG_DISP = { "LOG", "DISP" };
    /**
     * Describes the properties of the stored pixel values (bits stored, signed/unsigned).
     */
    private final StoredValue storedValue;
    /**
     * The Rescale Slope value from the DICOM attributes.
     */
    private float rescaleSlope = 1;
    /**
     * The Rescale Intercept value from the DICOM attributes.
     */
    private float rescaleIntercept = 0;
    /**
     * The Modality LUT.
     */
    private LookupTable modalityLUT;
    /**
     * The Window Center value for VOI transformation.
     */
    private float windowCenter;
    /**
     * The Window Width value for VOI transformation.
     */
    private float windowWidth;
    /**
     * The VOI (Value of Interest) LUT.
     */
    private LookupTable voiLUT;
    /**
     * The Presentation LUT.
     */
    private LookupTable presentationLUT;
    /**
     * A flag indicating if the final output should be inverted.
     */
    private boolean inverse;

    /**
     * Constructs a LookupTableFactory for a given pixel data storage format.
     *
     * @param storedValue An object describing the properties of the stored pixel values.
     */
    public LookupTableFactory(StoredValue storedValue) {
        this.storedValue = storedValue;
    }

    /**
     * Determines whether a Modality LUT should be applied based on the SOP Class and Pixel Intensity Relationship.
     *
     * @param attrs The DICOM attributes of the image.
     * @return {@code true} if the Modality LUT should be applied, {@code false} otherwise.
     */
    public static boolean applyModalityLUT(Attributes attrs) {
        return !(Builder.contains(XA_XRF_CUIDS, attrs.getString(Tag.SOPClassUID))
                && Builder.contains(LOG_DISP, attrs.getString(Tag.PixelIntensityRelationship)));
    }

    /**
     * Extracts either the high or low bytes from a 16-bit-per-entry LUT data array.
     *
     * @param data The source byte array containing interleaved 16-bit data.
     * @param hilo 0 for the low byte, 1 for the high byte.
     * @return A new byte array containing only the specified bytes.
     */
    static byte[] halfLength(byte[] data, int hilo) {
        byte[] bs = new byte[data.length >> 1];
        for (int i = 0; i < bs.length; i++)
            bs[i] = data[(i << 1) | hilo];
        return bs;
    }

    /**
     * Calculates the integer base-2 logarithm of a value, which is equivalent to finding the position of the most
     * significant bit.
     *
     * @param value The input value.
     * @return The floor of log2(value).
     */
    private static int log2(int value) {
        return 31 - Integer.numberOfLeadingZeros(value);
    }

    /**
     * Configures the Modality LUT based on the provided DICOM attributes.
     *
     * @param attrs The DICOM attributes.
     */
    public void setModalityLUT(Attributes attrs) {
        rescaleIntercept = attrs.getFloat(Tag.RescaleIntercept, 0);
        rescaleSlope = attrs.getFloat(Tag.RescaleSlope, 1);
        modalityLUT = createLUT(storedValue, attrs.getNestedDataset(Tag.ModalityLUTSequence));
    }

    /**
     * Configures the Presentation LUT based on the provided DICOM attributes.
     *
     * @param attrs The DICOM attributes.
     */
    public void setPresentationLUT(Attributes attrs) {
        setPresentationLUT(attrs, false);
    }

    /**
     * Configures the Presentation LUT based on the provided DICOM attributes, with an option to ignore the Presentation
     * LUT Shape.
     *
     * @param attrs                      The DICOM attributes.
     * @param ignorePresentationLUTShape If {@code true}, the Presentation LUT Shape is ignored and inversion is
     *                                   determined by Photometric Interpretation.
     */
    public void setPresentationLUT(Attributes attrs, boolean ignorePresentationLUTShape) {
        Attributes pLUT = attrs.getNestedDataset(Tag.PresentationLUTSequence);
        if (pLUT != null) {
            int[] desc = pLUT.getInts(Tag.LUTDescriptor);
            if (desc != null && desc.length == 3) {
                int len = desc[0] == 0 ? 0x10000 : desc[0];
                presentationLUT = createLUT(
                        new StoredValue.Unsigned(log2(len)),
                        resetOffset(desc),
                        pLUT.getSafeBytes(Tag.LUTData),
                        pLUT.bigEndian());
            }
        } else {
            String pShape;
            inverse = (ignorePresentationLUTShape || (pShape = attrs.getString(Tag.PresentationLUTShape)) == null
                    ? "MONOCHROME1".equals(attrs.getString(Tag.PhotometricInterpretation))
                    : "INVERSE".equals(pShape));
        }
    }

    /**
     * Creates a copy of a LUT descriptor array with the offset reset to zero.
     *
     * @param desc The original LUT descriptor array.
     * @return A new LUT descriptor array with the offset at index 1 set to 0.
     */
    private int[] resetOffset(int[] desc) {
        if (desc[1] == 0)
            return desc;
        int[] copy = desc.clone();
        copy[1] = 0;
        return copy;
    }

    /**
     * Sets the Window Center for VOI transformation.
     *
     * @param windowCenter The Window Center value.
     */
    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    /**
     * Sets the Window Width for VOI transformation.
     *
     * @param windowWidth The Window Width value.
     */
    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    /**
     * Configures the VOI (Value of Interest) transformation based on DICOM attributes. It selects either a Window/Level
     * pair or a VOI LUT sequence item.
     *
     * @param img          The DICOM attributes.
     * @param windowIndex  The index of the Window Center/Width pair to use if multiple are present.
     * @param voiLUTIndex  The index of the VOI LUT sequence item to use.
     * @param preferWindow If {@code true}, Window/Level is preferred even if a VOI LUT is present.
     */
    public void setVOI(Attributes img, int windowIndex, int voiLUTIndex, boolean preferWindow) {
        if (img == null)
            return;
        Attributes vLUT = img.getNestedDataset(Tag.VOILUTSequence, voiLUTIndex);
        if (preferWindow || vLUT == null) {
            float[] wcs = img.getFloats(Tag.WindowCenter);
            float[] wws = img.getFloats(Tag.WindowWidth);
            if (wcs != null && wcs.length != 0 && wws != null && wws.length != 0) {
                int index = windowIndex < Math.min(wcs.length, wws.length) ? windowIndex : 0;
                windowCenter = wcs[index];
                windowWidth = wws[index];
                return;
            }
        }
        if (vLUT != null) {
            adjustVOILUTDescriptor(vLUT);
            voiLUT = createLUT(modalityLUT != null ? new StoredValue.Unsigned(modalityLUT.outBits) : storedValue, vLUT);
        }
    }

    /**
     * Adjusts the VOI LUT descriptor if it appears to be 16-bit but only uses the lower 8 bits. This is a workaround
     * for certain types of data.
     *
     * @param vLUT The VOI LUT attributes.
     */
    private void adjustVOILUTDescriptor(Attributes vLUT) {
        int[] desc = vLUT.getInts(Tag.LUTDescriptor);
        byte[] data;
        if (desc != null && desc.length == 3 && desc[2] == 16 && (data = vLUT.getSafeBytes(Tag.LUTData)) != null) {
            int hiByte = 0;
            for (int i = vLUT.bigEndian() ? 0 : 1; i < data.length; i += 2)
                hiByte |= data[i];
            if ((hiByte & 0x80) == 0) {
                desc[2] = 8 + (31 - Integer.numberOfLeadingZeros(hiByte & 0xFF));
                vLUT.setInt(Tag.LUTDescriptor, VR.US, desc);
            }
        }
    }

    /**
     * Creates a {@link LookupTable} from a DICOM sequence item.
     *
     * @param inBits The bit depth of the input data for the LUT.
     * @param attrs  The attributes of the LUT sequence item.
     * @return A new {@link LookupTable}, or {@code null} if the attributes are invalid.
     */
    private LookupTable createLUT(StoredValue inBits, Attributes attrs) {
        if (attrs == null)
            return null;
        return createLUT(inBits, attrs.getInts(Tag.LUTDescriptor), attrs.getSafeBytes(Tag.LUTData), attrs.bigEndian());
    }

    /**
     * Creates a {@link LookupTable} from its core components.
     *
     * @param inBits    The bit depth of the input data.
     * @param desc      The LUT Descriptor attribute (entry count, first value mapped, bits per entry).
     * @param data      The LUT Data as a byte array.
     * @param bigEndian {@code true} if the LUT data is big-endian.
     * @return A new {@link LookupTable}, or {@code null} if the components are invalid.
     */
    private LookupTable createLUT(StoredValue inBits, int[] desc, byte[] data, boolean bigEndian) {
        if (desc == null || desc.length != 3 || data == null) {
            return null;
        }

        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int offset = (short) desc[1];
        int outBits = desc[2];

        if (data.length == len << 1) { // 16-bit LUT data
            if (outBits > 16) {
                return null;
            }
            if (outBits > 8) {
                short[] ss = new short[len];
                if (bigEndian)
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteKit.bytesToShortBE(data, i << 1);
                else
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteKit.bytesToShortLE(data, i << 1);
                return new ShortLookupTable(inBits, outBits, offset, ss);
            }
            // If outBits <= 8 but data is 16-bit, assume the useful data is in the low byte.
            data = halfLength(data, bigEndian ? 1 : 0);
        }

        if (data.length != len || outBits > 8) {
            return null;
        }
        return new ByteLookupTable(inBits, outBits, offset, data);
    }

    /**
     * Creates the final, combined lookup table for rendering. This method combines the Modality LUT, VOI LUT (or
     * window/level), and Presentation LUT.
     *
     * @param outBits The desired bit depth for the final output.
     * @return The final, combined {@link LookupTable}.
     */
    public LookupTable createLUT(int outBits) {
        LookupTable lut = combineModalityVOILUT(presentationLUT != null ? log2(presentationLUT.length()) : outBits);
        if (presentationLUT != null) {
            lut = lut.combine(presentationLUT.adjustOutBits(outBits));
        } else if (inverse) {
            lut.inverse();
        }
        return lut;
    }

    /**
     * Combines the Modality and VOI LUTs into a single lookup table. If a VOI LUT is not explicitly defined, a linear
     * LUT is generated from the window/level parameters.
     *
     * @param outBits The desired output bit depth for the combined LUT.
     * @return The combined Modality and VOI {@link LookupTable}.
     */
    private LookupTable combineModalityVOILUT(int outBits) {
        LookupTable lut = this.voiLUT;
        if (lut == null) {
            if (windowWidth == 0 && modalityLUT != null) {
                return modalityLUT.adjustOutBits(outBits);
            }
            StoredValue inBits = modalityLUT != null ? new StoredValue.Unsigned(modalityLUT.outBits) : storedValue;
            int offset = Math.round((windowCenter - windowWidth / 2 - rescaleIntercept) / rescaleSlope);
            int size = Math.max(2, Math.round(windowWidth / Math.abs(rescaleSlope)));
            lut = outBits > 8
                    ? new ShortLookupTable(inBits, outBits, 0, (1 << outBits) - 1, offset, size, rescaleSlope < 0)
                    : new ByteLookupTable(inBits, outBits, 0, (1 << outBits) - 1, offset, size, rescaleSlope < 0);
        } else {
            lut = lut.adjustOutBits(outBits);
        }
        return modalityLUT != null ? modalityLUT.combine(lut) : lut;
    }

    /**
     * Automatically calculates and sets the window center and width based on the image's pixel data range.
     *
     * @param img    The DICOM attributes.
     * @param raster The image raster data.
     * @return {@code true} if auto-windowing was successful, {@code false} otherwise.
     */
    public boolean autoWindowing(Attributes img, Raster raster) {
        return autoWindowing(img, raster, false);
    }

    /**
     * Automatically calculates and sets the window center and width, with an option to store the values back into the
     * DICOM attributes.
     *
     * @param img           The DICOM attributes.
     * @param raster        The image raster data.
     * @param addAutoWindow If {@code true}, the calculated window center and width are added to the attributes.
     * @return {@code true} if auto-windowing was successful, {@code false} otherwise.
     */
    public boolean autoWindowing(Attributes img, Raster raster, boolean addAutoWindow) {
        if (modalityLUT != null || voiLUT != null || windowWidth != 0)
            return false;
        int[] min_max = calcMinMax(raster);
        if (min_max[0] == min_max[1])
            return false;
        windowCenter = (min_max[0] + min_max[1] + 1) / 2f * rescaleSlope + rescaleIntercept;
        windowWidth = Math.abs((min_max[1] + 1 - min_max[0]) * rescaleSlope);
        if (addAutoWindow) {
            img.setFloat(Tag.WindowCenter, VR.DS, windowCenter);
            img.setFloat(Tag.WindowWidth, VR.DS, windowWidth);
        }
        return true;
    }

    /**
     * Calculates the minimum and maximum pixel values from a raster.
     *
     * @param raster The image raster.
     * @return An array containing the minimum and maximum values.
     */
    private int[] calcMinMax(Raster raster) {
        ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
        DataBuffer dataBuffer = raster.getDataBuffer();
        return switch (dataBuffer.getDataType()) {
            case DataBuffer.TYPE_BYTE -> calcMinMax(storedValue, sm, ((DataBufferByte) dataBuffer).getData());
            case DataBuffer.TYPE_USHORT -> calcMinMax(storedValue, sm, ((DataBufferUShort) dataBuffer).getData());
            case DataBuffer.TYPE_SHORT -> calcMinMax(storedValue, sm, ((DataBufferShort) dataBuffer).getData());
            default -> throw new UnsupportedOperationException(
                    "DataBuffer: " + dataBuffer.getClass() + " not supported");
        };
    }

    /**
     * Calculates the min/max values for a byte array of pixel data.
     *
     * @param storedValue The stored value properties.
     * @param sm          The sample model.
     * @param data        The pixel data.
     * @return An array with {min, max}.
     */
    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm, byte[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++) {
            for (int i = y * stride, end = i + w; i < end;) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min)
                    min = val;
                if (val > max)
                    max = val;
            }
        }
        return new int[] { min, max };
    }

    /**
     * Calculates the min/max values for a short array of pixel data.
     *
     * @param storedValue The stored value properties.
     * @param sm          The sample model.
     * @param data        The pixel data.
     * @return An array with {min, max}.
     */
    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm, short[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++) {
            for (int i = y * stride, end = i + w; i < end;) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min)
                    min = val;
                if (val > max)
                    max = val;
            }
        }
        return new int[] { min, max };
    }

}
