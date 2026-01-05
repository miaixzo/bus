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

/**
 * A lookup table implementation for image processing that uses a {@code short} array for pixel value transformations.
 * <p>
 * This class maps input pixel values to output pixel values and is suitable for operations like brightness/contrast
 * adjustment, color mapping, and other pixel-wise transformations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ShortLookupTable extends LookupTable {

    /**
     * The lookup table array that stores the mapped pixel values.
     */
    private final short[] lut;

    /**
     * Constructs a ShortLookupTable with a pre-defined LUT array.
     *
     * @param inBits  The bit depth of the input values.
     * @param outBits The bit depth of the output values.
     * @param offset  An offset to apply to input values before lookup.
     * @param lut     The short array representing the lookup table.
     */
    ShortLookupTable(StoredValue inBits, int outBits, int offset, short[] lut) {
        super(inBits, outBits, offset);
        this.lut = lut;
    }

    /**
     * Constructs a linear ShortLookupTable.
     *
     * @param inBits  The bit depth of the input values.
     * @param outBits The bit depth of the output values.
     * @param minOut  The minimum output value.
     * @param maxOut  The maximum output value.
     * @param offset  An offset to apply to input values.
     * @param size    The size of the lookup table to generate.
     * @param flip    If {@code true}, the mapping is inverted (high input maps to low output).
     */
    ShortLookupTable(StoredValue inBits, int outBits, int minOut, int maxOut, int offset, int size, boolean flip) {
        this(inBits, outBits, offset, new short[minOut == maxOut ? 1 : size]);
        if (lut.length == 1) {
            lut[0] = (short) minOut;
        } else {
            int outRange = maxOut - minOut;
            int maxIndex = size - 1;
            int midIndex = maxIndex / 2;
            for (int i = 0; i < size; i++)
                lut[flip ? maxIndex - i : i] = (short) ((i * outRange + midIndex) / maxIndex + minOut);
        }
    }

    /**
     * Gets the length of the lookup table.
     *
     * @return the length of the lookup table
     */
    @Override
    public int length() {
        return lut.length;
    }

    /**
     * Calculates the lookup table index for a given pixel value, applying the offset and clamping the result.
     *
     * @param pixel The input pixel value.
     * @return The calculated index within the bounds of the LUT array.
     */
    private int index(int pixel) {
        int index = inBits.valueOf(pixel) - offset;
        return Math.min(Math.max(0, index), lut.length - 1);
    }

    /**
     * Performs lookup transformation from byte array to byte array.
     *
     * @param src     the source array
     * @param srcPos  the starting position in the source array
     * @param dest    the destination array
     * @param destPos the starting position in the destination array
     * @param length  the number of elements to transform
     */
    @Override
    public void lookup(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (byte) lut[index(src[i++] & 0xff)];
    }

    /**
     * Performs lookup transformation from short array to byte array.
     *
     * @param src     the source array
     * @param srcPos  the starting position in the source array
     * @param dest    the destination array
     * @param destPos the starting position in the destination array
     * @param length  the number of elements to transform
     */
    @Override
    public void lookup(short[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (byte) lut[index(src[i++] & 0xffff)];
    }

    /**
     * Performs lookup transformation from byte array to short array.
     *
     * @param src     the source array
     * @param srcPos  the starting position in the source array
     * @param dest    the destination array
     * @param destPos the starting position in the destination array
     * @param length  the number of elements to transform
     */
    @Override
    public void lookup(byte[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++] & 0xff)];
    }

    /**
     * Performs lookup transformation from short array to short array.
     *
     * @param src     the source array
     * @param srcPos  the starting position in the source array
     * @param dest    the destination array
     * @param destPos the starting position in the destination array
     * @param length  the number of elements to transform
     */
    @Override
    public void lookup(short[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++] & 0xffff)];
    }

    /**
     * Adjusts the output bit depth of this lookup table.
     *
     * @param outBits the new output bit depth
     * @return this lookup table for chaining
     */
    @Override
    public LookupTable adjustOutBits(int outBits) {
        int diff = outBits - this.outBits;
        if (diff != 0) {
            short[] lut = this.lut;
            if (diff < 0) {
                diff = -diff;
                for (int i = 0; i < lut.length; i++)
                    lut[i] = (short) ((lut[i] & 0xffff) >> diff);
            } else {
                for (int i = 0; i < lut.length; i++)
                    lut[i] <<= diff;
            }
            this.outBits = outBits;
        }
        return this;
    }

    /**
     * Inverts this lookup table.
     */
    @Override
    public void inverse() {
        short[] lut = this.lut;
        int maxOut = (1 << outBits) - 1;
        for (int i = 0; i < lut.length; i++)
            lut[i] = (short) (maxOut - lut[i]);
    }

    /**
     * Combines this lookup table with another.
     *
     * @param other the other lookup table to combine with
     * @return this lookup table for chaining
     */
    @Override
    public LookupTable combine(LookupTable other) {
        short[] lut = this.lut;
        other.lookup(lut, 0, lut, 0, lut.length);
        this.outBits = other.outBits;
        return this;
    }

}
