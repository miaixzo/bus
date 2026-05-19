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

/**
 * Integer dimensions of a 3D voxel volume.
 *
 * @param x the x.
 * @param y the y.
 * @param z the z.
 * @author Kimi Liu
 * @since Java 21+
 */
public record VolumeSize(int x, int y, int z) {

    /**
     * Creates a new instance.
     *
     * @param x the x.
     * @param y the y.
     * @param z the z.
     */
    public VolumeSize {
        requirePositive("x", x);
        requirePositive("y", y);
        requirePositive("z", z);
    }

    /**
     * Executes the voxel operation.
     *
     * @param channels number of samples per voxel
     * @return total number of stored primitive values
     */
    public long elementCount(int channels) {
        requirePositive("channels", channels);
        return Math.multiplyExact(Math.multiplyExact(Math.multiplyExact((long) x, y), z), channels);
    }

    /**
     * Executes the expansion operation.
     *
     * @return number of voxels without channel expansion
     */
    public long voxelCount() {
        return Math.multiplyExact(Math.multiplyExact((long) x, y), z);
    }

    /**
     * Executes the require positive operation.
     *
     * @param name  the name.
     * @param value the value.
     */
    private static void requirePositive(String name, int value) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be > 0: " + value);
        }
    }

}
