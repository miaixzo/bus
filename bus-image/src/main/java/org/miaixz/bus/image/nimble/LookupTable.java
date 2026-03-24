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

import java.awt.image.*;

/**
 * @author Kimi Liu
 * @since Java 21+
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
