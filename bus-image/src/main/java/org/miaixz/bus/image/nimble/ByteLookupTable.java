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

/**
 * Provides DICOM processing details.
 * <p>
 * Provides DICOM processing details.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ByteLookupTable extends LookupTable {

    /**
     * Provides DICOM processing details.
     */
    private final byte[] lut;

    /**
     * Creates a new instance.
     *
     * @param inBits  the in bits.
     * @param outBits the out bits.
     * @param offset  the offset.
     * @param lut     the lut.
     */
    ByteLookupTable(StoredValue inBits, int outBits, int offset, byte[] lut) {
        super(inBits, outBits, offset);
        this.lut = lut;
    }

    /**
     * Creates a new instance.
     *
     * @param inBits  the in bits.
     * @param outBits the out bits.
     * @param minOut  the min out.
     * @param maxOut  the max out.
     * @param offset  the offset.
     * @param size    the size.
     * @param flip    the flip.
     */
    ByteLookupTable(StoredValue inBits, int outBits, int minOut, int maxOut, int offset, int size, boolean flip) {
        this(inBits, outBits, offset, new byte[minOut == maxOut ? 1 : size]);
        if (lut.length == 1) {
            lut[0] = (byte) minOut;
        } else {
            int maxIndex = size - 1;
            int midIndex = maxIndex / 2;
            int outRange = maxOut - minOut;
            for (int i = 0; i < size; i++)
                lut[flip ? maxIndex - i : i] = (byte) ((i * outRange + midIndex) / maxIndex + minOut);
        }
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    @Override
    public int length() {
        return lut.length;
    }

    /**
     * Provides DICOM processing details.
     *
     * @param src     the src.
     * @param srcPos  the src pos.
     * @param dest    the dest.
     * @param destPos the dest pos.
     * @param length  the length.
     */
    @Override
    public void lookup(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++])];
    }

    /**
     * Provides DICOM processing details.
     *
     * @param pixel the pixel.
     * @return the result.
     */
    private int index(int pixel) {
        int index = inBits.valueOf(pixel) - offset;
        return Math.min(Math.max(0, index), lut.length - 1);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param src     the src.
     * @param srcPos  the src pos.
     * @param dest    the dest.
     * @param destPos the dest pos.
     * @param length  the length.
     */
    @Override
    public void lookup(short[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++])];
    }

    /**
     * Provides DICOM processing details.
     *
     * @param src     the src.
     * @param srcPos  the src pos.
     * @param dest    the dest.
     * @param destPos the dest pos.
     * @param length  the length.
     */
    @Override
    public void lookup(byte[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (short) (lut[index(src[i++])] & 0xff);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param src     the src.
     * @param srcPos  the src pos.
     * @param dest    the dest.
     * @param destPos the dest pos.
     * @param length  the length.
     */
    @Override
    public void lookup(short[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (short) (lut[index(src[i++])] & 0xff);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param outBits the out bits.
     * @return the result.
     */
    @Override
    public LookupTable adjustOutBits(int outBits) {
        int diff = outBits - this.outBits;
        if (diff != 0) {
            byte[] lut = this.lut;
            if (outBits > 8) {
                short[] ss = new short[lut.length];
                for (int i = 0; i < lut.length; i++)
                    ss[i] = (short) ((lut[i] & 0xff) << diff);
                return new ShortLookupTable(inBits, outBits, offset, ss);
            }
            if (diff < 0) {
                diff = -diff;
                for (int i = 0; i < lut.length; i++)
                    lut[i] = (byte) ((lut[i] & 0xff) >> diff);
            } else
                for (int i = 0; i < lut.length; i++)
                    lut[i] <<= diff;
            this.outBits = outBits;
        }
        return this;
    }

    /**
     * Provides DICOM processing details.
     */
    @Override
    public void inverse() {
        byte[] lut = this.lut;
        int maxOut = (1 << outBits) - 1;
        for (int i = 0; i < lut.length; i++)
            lut[i] = (byte) (maxOut - lut[i]);
    }

    /**
     * Provides DICOM processing details.
     *
     * @param other the other.
     * @return the result.
     */
    @Override
    public LookupTable combine(LookupTable other) {
        byte[] lut = this.lut;
        if (other.outBits > 8) {
            short[] ss = new short[lut.length];
            other.lookup(lut, 0, ss, 0, lut.length);
            return new ShortLookupTable(inBits, other.outBits, offset, ss);
        }
        other.lookup(lut, 0, lut, 0, lut.length);
        this.outBits = other.outBits;
        return this;
    }

}
