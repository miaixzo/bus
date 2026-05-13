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
package org.miaixz.bus.image.nimble.mpr;

import java.util.Arrays;

/**
 * Short-backed MPR volume.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class VolumeShort extends Volume<Short> {

    /**
     * The data value.
     */
    private final short[] data;

    /**
     * Creates a new instance.
     *
     * @param sizeX    the size x.
     * @param sizeY    the size y.
     * @param sizeZ    the size z.
     * @param signed   the signed.
     * @param channels the channels.
     */
    public VolumeShort(int sizeX, int sizeY, int sizeZ, boolean signed, int channels) {
        this(new VolumeSize(sizeX, sizeY, sizeZ), signed, channels);
    }

    /**
     * Creates a new instance.
     *
     * @param size     the size.
     * @param signed   the signed.
     * @param channels the channels.
     */
    public VolumeShort(VolumeSize size, boolean signed, int channels) {
        super(size, signed, channels);
        this.data = new short[(int) elementCount()];
    }

    /**
     * Gets the linear value.
     *
     * @param index the index.
     * @return the linear value.
     */
    @Override
    public Short getLinearValue(long index) {
        return data[checkedArrayIndex(index)];
    }

    /**
     * Sets the linear value.
     *
     * @param index the index.
     * @param value the value.
     */
    @Override
    public void setLinearValue(long index, Short value) {
        data[checkedArrayIndex(index)] = value;
    }

    /**
     * Executes the fill operation.
     *
     * @param value the value.
     */
    @Override
    public void fill(Short value) {
        Arrays.fill(data, value);
    }

    /**
     * Executes the native minimum operation.
     *
     * @return the operation result.
     */
    @Override
    public Number nativeMinimum() {
        return signed ? Short.MIN_VALUE : 0;
    }

    /**
     * Executes the native maximum operation.
     *
     * @return the operation result.
     */
    @Override
    public Number nativeMaximum() {
        return signed ? Short.MAX_VALUE : 0xFFFF;
    }

    /**
     * Gets the unsigned value.
     *
     * @param x       the x.
     * @param y       the y.
     * @param z       the z.
     * @param channel the channel.
     * @return the unsigned value.
     */
    public int getUnsignedValue(int x, int y, int z, int channel) {
        return Short.toUnsignedInt(getValue(x, y, z, channel));
    }

    /**
     * Executes the copy data operation.
     *
     * @return the operation result.
     */
    public short[] copyData() {
        return data.clone();
    }

}
