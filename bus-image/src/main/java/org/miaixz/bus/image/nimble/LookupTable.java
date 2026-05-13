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
 * Represents the LookupTable type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class LookupTable {

    /**
     * The in bits value.
     */
    protected StoredValue inBits;

    /**
     * The out bits value.
     */
    protected int outBits;

    /**
     * The offset value.
     */
    protected int offset;

    /**
     * Creates a new instance.
     *
     * @param inBits  the in bits.
     * @param outBits the out bits.
     * @param offset  the offset.
     */
    public LookupTable(StoredValue inBits, int outBits, int offset) {
        this.inBits = inBits;
        this.outBits = outBits;
        this.offset = offset;
    }

    /**
     * Executes the length operation.
     *
     * @return the operation result.
     */
    public abstract int length();

    /**
     * Executes the lookup operation.
     *
     * @param srcRaster  the src raster.
     * @param destRaster the dest raster.
     */
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

    /**
     * Executes the lookup operation.
     *
     * @param sm     the sm.
     * @param src    the src.
     * @param destsm the destsm.
     * @param dest   the dest.
     */
    private void lookup(ComponentSampleModel sm, byte[] src, ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    /**
     * Executes the lookup operation.
     *
     * @param sm     the sm.
     * @param src    the src.
     * @param destsm the destsm.
     * @param dest   the dest.
     */
    private void lookup(ComponentSampleModel sm, short[] src, ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    /**
     * Executes the lookup operation.
     *
     * @param sm     the sm.
     * @param src    the src.
     * @param destsm the destsm.
     * @param dest   the dest.
     */
    private void lookup(ComponentSampleModel sm, byte[] src, ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    /**
     * Executes the lookup operation.
     *
     * @param sm     the sm.
     * @param src    the src.
     * @param destsm the destsm.
     * @param dest   the dest.
     */
    private void lookup(ComponentSampleModel sm, short[] src, ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    /**
     * Executes the lookup operation.
     *
     * @param src     the src.
     * @param srcPost the src post.
     * @param dest    the dest.
     * @param destPos the dest pos.
     * @param length  the length.
     */
    public abstract void lookup(byte[] src, int srcPost, byte[] dest, int destPos, int length);

    /**
     * Executes the lookup operation.
     *
     * @param src     the src.
     * @param srcPost the src post.
     * @param dest    the dest.
     * @param destPos the dest pos.
     * @param length  the length.
     */
    public abstract void lookup(short[] src, int srcPost, byte[] dest, int destPos, int length);

    /**
     * Executes the lookup operation.
     *
     * @param src     the src.
     * @param srcPost the src post.
     * @param dest    the dest.
     * @param destPos the dest pos.
     * @param length  the length.
     */
    public abstract void lookup(byte[] src, int srcPost, short[] dest, int destPos, int length);

    /**
     * Executes the lookup operation.
     *
     * @param src     the src.
     * @param srcPost the src post.
     * @param dest    the dest.
     * @param destPos the dest pos.
     * @param length  the length.
     */
    public abstract void lookup(short[] src, int srcPost, short[] dest, int destPos, int length);

    /**
     * Executes the adjust out bits operation.
     *
     * @param outBits the out bits.
     * @return the operation result.
     */
    public abstract LookupTable adjustOutBits(int outBits);

    /**
     * Executes the inverse operation.
     */
    public abstract void inverse();

    /**
     * Executes the combine operation.
     *
     * @param lut the lut.
     * @return the operation result.
     */
    public abstract LookupTable combine(LookupTable lut);

}
