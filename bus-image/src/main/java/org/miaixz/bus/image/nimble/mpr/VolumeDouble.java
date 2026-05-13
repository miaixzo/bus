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
 * Double-backed MPR volume.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class VolumeDouble extends Volume<Double> {

    /**
     * The data value.
     */
    private final double[] data;

    /**
     * Creates a new instance.
     *
     * @param sizeX    the size x.
     * @param sizeY    the size y.
     * @param sizeZ    the size z.
     * @param channels the channels.
     */
    public VolumeDouble(int sizeX, int sizeY, int sizeZ, int channels) {
        this(new VolumeSize(sizeX, sizeY, sizeZ), channels);
    }

    /**
     * Creates a new instance.
     *
     * @param size     the size.
     * @param channels the channels.
     */
    public VolumeDouble(VolumeSize size, int channels) {
        super(size, true, channels);
        checkSingleChannel();
        this.data = new double[(int) elementCount()];
    }

    /**
     * Gets the linear value.
     *
     * @param index the index.
     * @return the linear value.
     */
    @Override
    public Double getLinearValue(long index) {
        return data[checkedArrayIndex(index)];
    }

    /**
     * Sets the linear value.
     *
     * @param index the index.
     * @param value the value.
     */
    @Override
    public void setLinearValue(long index, Double value) {
        data[checkedArrayIndex(index)] = value;
    }

    /**
     * Executes the fill operation.
     *
     * @param value the value.
     */
    @Override
    public void fill(Double value) {
        Arrays.fill(data, value);
    }

    /**
     * Executes the native minimum operation.
     *
     * @return the operation result.
     */
    @Override
    public Number nativeMinimum() {
        return -Double.MAX_VALUE;
    }

    /**
     * Executes the native maximum operation.
     *
     * @return the operation result.
     */
    @Override
    public Number nativeMaximum() {
        return Double.MAX_VALUE;
    }

    /**
     * Executes the copy data operation.
     *
     * @return the operation result.
     */
    public double[] copyData() {
        return data.clone();
    }

}
