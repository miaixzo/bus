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

import java.awt.image.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class LookupTable {

    protected StoredValue inBits;
    protected int outBits;
    protected int offset;

    public LookupTable(StoredValue inBits, int outBits, int offset) {
        this.inBits = inBits;
        this.outBits = outBits;
        this.offset = offset;
    }

    public abstract int length();

    public void lookup(Raster srcRaster, Raster destRaster) {
        ComponentSampleModel sm = (ComponentSampleModel) srcRaster.getSampleModel();
        ComponentSampleModel destsm = (ComponentSampleModel) destRaster.getSampleModel();
        DataBuffer src = srcRaster.getDataBuffer();
        DataBuffer dest = destRaster.getDataBuffer();
        switch (src.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            switch (dest.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookup(sm, ((DataBufferByte) src).getData(), destsm, ((DataBufferByte) dest).getData());
                return;
            case DataBuffer.TYPE_USHORT:
                lookup(sm, ((DataBufferByte) src).getData(), destsm, ((DataBufferUShort) dest).getData());
                return;
            }
            break;
        case DataBuffer.TYPE_USHORT:
            switch (dest.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookup(sm, ((DataBufferUShort) src).getData(), destsm, ((DataBufferByte) dest).getData());
                return;
            case DataBuffer.TYPE_USHORT:
                lookup(sm, ((DataBufferUShort) src).getData(), destsm, ((DataBufferUShort) dest).getData());
                return;
            }
            break;
        case DataBuffer.TYPE_SHORT:
            switch (dest.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookup(sm, ((DataBufferShort) src).getData(), destsm, ((DataBufferByte) dest).getData());
                return;
            case DataBuffer.TYPE_USHORT:
                lookup(sm, ((DataBufferShort) src).getData(), destsm, ((DataBufferUShort) dest).getData());
                return;
            }
            break;
        }
        throw new UnsupportedOperationException(
                "Lookup " + src.getClass() + " -> " + dest.getClass() + " not supported");
    }

    private void lookup(ComponentSampleModel sm, byte[] src, ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    private void lookup(ComponentSampleModel sm, short[] src, ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    private void lookup(ComponentSampleModel sm, byte[] src, ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    private void lookup(ComponentSampleModel sm, short[] src, ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    public abstract void lookup(byte[] src, int srcPost, byte[] dest, int destPos, int length);

    public abstract void lookup(short[] src, int srcPost, byte[] dest, int destPos, int length);

    public abstract void lookup(byte[] src, int srcPost, short[] dest, int destPos, int length);

    public abstract void lookup(short[] src, int srcPost, short[] dest, int destPos, int length);

    public abstract LookupTable adjustOutBits(int outBits);

    public abstract void inverse();

    public abstract LookupTable combine(LookupTable lut);

}
