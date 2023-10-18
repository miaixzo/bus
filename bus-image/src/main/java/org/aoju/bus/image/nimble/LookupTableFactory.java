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
package org.aoju.bus.image.nimble;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.ByteKit;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.VR;

import java.awt.image.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class LookupTableFactory {

    private final StoredValue storedValue;
    private float rescaleSlope = 1;
    private float rescaleIntercept = 0;
    private LookupTable modalityLUT;
    private float windowCenter;
    private float windowWidth;
    private LookupTable voiLUT;
    private LookupTable presentationLUT;
    private boolean inverse;

    public LookupTableFactory(StoredValue storedValue) {
        this.storedValue = storedValue;
    }

    static byte[] halfLength(byte[] data, int hilo) {
        byte[] bs = new byte[data.length >> 1];
        for (int i = 0; i < bs.length; i++)
            bs[i] = data[(i << 1) | hilo];

        return bs;
    }

    private static int log2(int value) {
        int i = 0;
        while ((value >>> i) != 0)
            ++i;
        return i - 1;
    }

    public void setModalityLUT(Attributes attrs) {
        rescaleIntercept = attrs.getFloat(Tag.RescaleIntercept, 0);
        rescaleSlope = attrs.getFloat(Tag.RescaleSlope, 1);
        modalityLUT = createLUT(storedValue,
                attrs.getNestedDataset(Tag.ModalityLUTSequence));
    }

    public void setPresentationLUT(Attributes attrs) {
        Attributes pLUT = attrs.getNestedDataset(Tag.PresentationLUTSequence);
        if (null != pLUT) {
            int[] desc = pLUT.getInts(Tag.LUTDescriptor);
            if (null != desc && desc.length == 3) {
                int len = desc[0] == 0 ? 0x10000 : desc[0];
                presentationLUT = createLUT(new StoredValue.Unsigned(log2(len)),
                        resetOffset(desc),
                        pLUT.getSafeBytes(Tag.LUTData), pLUT.bigEndian());
            }
        } else {
            String pShape = attrs.getString(Tag.PresentationLUTShape);
            inverse = (null != pShape
                    ? "INVERSE".equals(pShape)
                    : "MONOCHROME1".equals(
                    attrs.getString(Tag.PhotometricInterpretation)));
        }
    }

    private int[] resetOffset(int[] desc) {
        if (desc[1] == 0)
            return desc;

        int[] copy = desc.clone();
        copy[1] = 0;
        return copy;
    }

    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public void setVOI(Attributes img, int windowIndex, int voiLUTIndex,
                       boolean preferWindow) {
        if (null == img)
            return;

        Attributes vLUT = img.getNestedDataset(Tag.VOILUTSequence, voiLUTIndex);
        if (preferWindow || null == vLUT) {
            float[] wcs = img.getFloats(Tag.WindowCenter);
            float[] wws = img.getFloats(Tag.WindowWidth);
            if (null != wcs && wcs.length != 0
                    && null != wws && wws.length != 0) {
                int index = windowIndex < Math.min(wcs.length, wws.length)
                        ? windowIndex
                        : 0;
                windowCenter = wcs[index];
                windowWidth = wws[index];
                return;
            }
        }
        if (null != vLUT) {
            adjustVOILUTDescriptor(vLUT);
            voiLUT = createLUT(null != modalityLUT
                            ? new StoredValue.Unsigned(modalityLUT.outBits)
                            : storedValue,
                    vLUT);
        }
    }

    private void adjustVOILUTDescriptor(Attributes vLUT) {
        int[] desc = vLUT.getInts(Tag.LUTDescriptor);
        byte[] data;
        if (null != desc && desc.length == 3 && desc[2] == Normal._16
                && null != (data = vLUT.getSafeBytes(Tag.LUTData))) {
            int hiByte = 0;
            for (int i = vLUT.bigEndian() ? 0 : 1; i < data.length; i++, i++)
                hiByte |= data[i];
            if ((hiByte & 0x80) == 0) {
                desc[2] = 40 - Integer.numberOfLeadingZeros(hiByte & 0xFF);
                vLUT.setInt(Tag.LUTDescriptor, VR.SS, desc);
            }
        }
    }

    private LookupTable createLUT(StoredValue inBits, Attributes attrs) {
        if (null == attrs)
            return null;

        return createLUT(inBits, attrs.getInts(Tag.LUTDescriptor),
                attrs.getSafeBytes(Tag.LUTData), attrs.bigEndian());
    }

    private LookupTable createLUT(StoredValue inBits, int[] desc, byte[] data,
                                  boolean bigEndian) {

        if (null == desc)
            return null;

        if (desc.length != 3)
            return null;

        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int offset = (short) desc[1];
        int outBits = desc[2];
        if (null == data)
            return null;

        if (data.length == len << 1) {
            if (outBits > 8) {
                if (outBits > Normal._16)
                    return null;

                short[] ss = new short[len];
                if (bigEndian)
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteKit.bytesToShortBE(data, i << 1);
                else
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteKit.bytesToShortLE(data, i << 1);

                return new ShortLookupTable(inBits, outBits, offset, ss);
            }
            // padded high bits -> use low bits
            data = halfLength(data, bigEndian ? 1 : 0);
        }
        if (data.length != len)
            return null;

        if (outBits > 8)
            return null;

        return new ByteLookupTable(inBits, outBits, offset, data);
    }

    public LookupTable createLUT(int outBits) {
        LookupTable lut = combineModalityVOILUT(null != presentationLUT
                ? log2(presentationLUT.length())
                : outBits);
        if (null != presentationLUT) {
            lut = lut.combine(presentationLUT.adjustOutBits(outBits));
        } else if (inverse)
            lut.inverse();
        return lut;
    }

    private LookupTable combineModalityVOILUT(int outBits) {
        float m = rescaleSlope;
        float b = rescaleIntercept;
        LookupTable modalityLUT = this.modalityLUT;
        LookupTable lut = this.voiLUT;
        if (null == lut) {
            float c = windowCenter;
            float w = windowWidth;

            if (w == 0 && null != modalityLUT)
                return modalityLUT.adjustOutBits(outBits);

            int size, offset;
            StoredValue inBits = null != modalityLUT
                    ? new StoredValue.Unsigned(modalityLUT.outBits)
                    : storedValue;
            if (w != 0) {
                size = Math.max(2, Math.abs(Math.round(w / m)));
                offset = Math.round((c - b) / m) - size / 2;
            } else {
                offset = inBits.minValue();
                size = inBits.maxValue() - inBits.minValue() + 1;
            }
            lut = outBits > 8
                    ? new ShortLookupTable(inBits, outBits, offset, size, m < 0)
                    : new ByteLookupTable(inBits, outBits, offset, size, m < 0);
        } else {
            //TODO consider m+b
            lut = lut.adjustOutBits(outBits);
        }
        return null != modalityLUT ? modalityLUT.combine(lut) : lut;
    }

    public boolean autoWindowing(Attributes img, Raster raster) {
        if (null != modalityLUT || null != voiLUT || windowWidth != 0)
            return false;

        int min = img.getInt(Tag.SmallestImagePixelValue, 0);
        int max = img.getInt(Tag.LargestImagePixelValue, 0);
        if (max == 0) {
            int[] min_max;
            ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
            DataBuffer dataBuffer = raster.getDataBuffer();
            switch (dataBuffer.getDataType()) {
                case DataBuffer.TYPE_BYTE:
                    min_max = calcMinMax(storedValue, sm,
                            ((DataBufferByte) dataBuffer).getData());
                    break;
                case DataBuffer.TYPE_USHORT:
                    min_max = calcMinMax(storedValue, sm,
                            ((DataBufferUShort) dataBuffer).getData());
                    break;
                case DataBuffer.TYPE_SHORT:
                    min_max = calcMinMax(storedValue, sm,
                            ((DataBufferShort) dataBuffer).getData());
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "DataBuffer: " + dataBuffer.getClass() + " not supported");
            }
            min = min_max[0];
            max = min_max[1];
        }
        windowCenter = (min + max + 1) / 2 * rescaleSlope + rescaleIntercept;
        windowWidth = Math.abs((max + 1 - min) * rescaleSlope);
        return true;
    }

    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm,
                             byte[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++)
            for (int i = y * stride, end = i + w; i < end; ) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min) min = val;
                if (val > max) max = val;
            }
        return new int[]{min, max};
    }

    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm,
                             short[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++)
            for (int i = y * stride, end = i + w; i < end; ) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min) min = val;
                if (val > max) max = val;
            }
        return new int[]{min, max};
    }

}
