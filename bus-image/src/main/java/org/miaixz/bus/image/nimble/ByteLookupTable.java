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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteLookupTable extends LookupTable {

    private final byte[] lut;

    ByteLookupTable(StoredValue inBits, int outBits, int offset, byte[] lut) {
        super(inBits, outBits, offset);
        this.lut = lut;
    }

    ByteLookupTable(StoredValue inBits, int outBits, int offset, int size, boolean flip) {
        this(inBits, outBits, offset, new byte[size]);
        int maxOut = (1 << outBits) - 1;
        int maxIndex = size - 1;
        int midIndex = maxIndex / 2;
        if (flip)
            for (int i = 0; i < size; i++)
                lut[maxIndex - i] = (byte) ((i * maxOut + midIndex) / maxIndex);
        else
            for (int i = 0; i < size; i++)
                lut[i] = (byte) ((i * maxOut + midIndex) / maxIndex);
    }

    @Override
    public int length() {
        return lut.length;
    }

    @Override
    public void lookup(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++])];
    }

    private int index(int pixel) {
        int index = inBits.valueOf(pixel) - offset;
        return Math.min(Math.max(0, index), lut.length - 1);
    }

    @Override
    public void lookup(short[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = lut[index(src[i++])];
    }

    @Override
    public void lookup(byte[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (short) (lut[index(src[i++])] & 0xff);
    }

    @Override
    public void lookup(short[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (short) (lut[index(src[i++])] & 0xff);
    }

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

    @Override
    public void inverse() {
        byte[] lut = this.lut;
        int maxOut = (1 << outBits) - 1;
        for (int i = 0; i < lut.length; i++)
            lut[i] = (byte) (maxOut - lut[i]);
    }

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
