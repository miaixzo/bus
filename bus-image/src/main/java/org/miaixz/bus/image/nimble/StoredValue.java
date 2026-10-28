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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opencv.core.CvType;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.opencv.ImageCV;

/**
 * Interprets stored integer pixel samples and supplies shared lookup ramps for image rendering. Signed and unsigned
 * sample interpretation does not allocate native images; lookup ramps are created only when requested.
 *
 * @author Kimi Liu
 */
public abstract class StoredValue {

    /**
     * Constructs a new {@code StoredValue} instance.
     */
    public StoredValue() {
        // No initialization required.
    }

    /**
     * Selects signed or unsigned interpretation from the dataset's pixel representation and stored bit depth.
     *
     * @param attrs the dataset supplying Bits Stored, Bits Allocated and Pixel Representation
     * @return the interpreter for the stored pixel values
     * @throws NullPointerException if the dataset is null
     */
    public static StoredValue valueOf(Attributes attrs) {
        int bitsStored = attrs.getInt(Tag.BitsStored, 0);
        if (bitsStored == 0)
            bitsStored = attrs.getInt(Tag.BitsAllocated, 8);
        return attrs.getInt(Tag.PixelRepresentation, 0) != 0 ? new Signed(bitsStored) : new Unsigned(bitsStored);
    }

    /**
     * Interprets the stored bits of a pixel as a signed or unsigned integer sample.
     *
     * @param pixel the pixel bit pattern
     * @return the interpreted sample value
     */
    public abstract int valueOf(int pixel);

    /**
     * Returns the smallest sample representable by this stored bit depth.
     *
     * @return the minimum stored sample value
     */
    public abstract int minValue();

    /**
     * Returns the largest sample representable by this stored bit depth.
     *
     * @return the maximum stored sample value
     */
    public abstract int maxValue();

    /**
     * Returns a shared one-row ramp for a single-channel 8-bit or 16-bit integer OpenCV type. Values follow unsigned
     * bit-pattern order, so signed ramps contain nonnegative values followed by negative values. Unsupported types
     * return null without initializing the cache or allocating a native image.
     *
     * @param cvType the complete OpenCV pixel type, including its channel count
     * @return the cached ramp, or null for unsupported pixel types; callers must not modify or release the ramp
     */
    static ImageCV ramp(int cvType) {
        if (CvType.channels(cvType) != 1 || CvType.depth(cvType) > CvType.CV_16S) {
            return null;
        }
        return RampCache.RAMPS.computeIfAbsent(cvType, StoredValue::createRamp);
    }

    /**
     * Allocates a ramp containing every bit pattern of a supported integer type in native lookup-table index order.
     *
     * @param cvType the single-channel 8-bit or 16-bit integer OpenCV type
     * @return the new native image owned exclusively by the shared cache
     */
    private static ImageCV createRamp(int cvType) {
        boolean sixteenBits = CvType.depth(cvType) >= CvType.CV_16U;
        int size = sixteenBits ? 1 << 16 : 1 << 8;
        var ramp = new ImageCV(1, size, cvType);
        if (sixteenBits) {
            short[] values = new short[size];
            for (int i = 0; i < size; i++) {
                values[i] = (short) i;
            }
            ramp.put(0, 0, values);
        } else {
            byte[] values = new byte[size];
            for (int i = 0; i < size; i++) {
                values[i] = (byte) i;
            }
            ramp.put(0, 0, values);
        }
        return ramp;
    }

    /**
     * Lazily owns shared native ramps without initializing a cache during ordinary pixel interpretation.
     *
     * @author Kimi Liu
     */
    private static final class RampCache {

        /**
         * Shared ramps indexed by complete OpenCV type; cached images must never be modified or released by callers.
         */
        private static final Map<Integer, ImageCV> RAMPS = new ConcurrentHashMap<>();

        /**
         * Prevents construction of the cache holder.
         */
        private RampCache() {
        }

    }

    /**
     * Represents the Unsigned type.
     *
     * @author Kimi Liu
     */
    public static class Unsigned extends StoredValue {

        /**
         * The mask value.
         */
        private final int mask;

        /**
         * Creates a new instance.
         *
         * @param bitsStored the bits stored.
         */
        public Unsigned(int bitsStored) {
            this.mask = (1 << bitsStored) - 1;
        }

        /**
         * Executes the value of operation.
         *
         * @param pixel the pixel.
         * @return the operation result.
         */
        @Override
        public int valueOf(int pixel) {
            return pixel & mask;
        }

        /**
         * Executes the min value operation.
         *
         * @return the operation result.
         */
        @Override
        public int minValue() {
            return 0;
        }

        /**
         * Executes the max value operation.
         *
         * @return the operation result.
         */
        @Override
        public int maxValue() {
            return mask;
        }

    }

    /**
     * Represents the Signed type.
     *
     * @author Kimi Liu
     */
    public static class Signed extends StoredValue {

        /**
         * The bits stored value.
         */
        private final int bitsStored;

        /**
         * The shift value.
         */
        private final int shift;

        /**
         * Creates a new instance.
         *
         * @param bitsStored the bits stored.
         */
        public Signed(int bitsStored) {
            this.bitsStored = bitsStored;
            this.shift = 32 - bitsStored;
        }

        /**
         * Executes the value of operation.
         *
         * @param pixel the pixel.
         * @return the operation result.
         */
        @Override
        public int valueOf(int pixel) {
            return pixel << shift >> shift;
        }

        /**
         * Executes the min value operation.
         *
         * @return the operation result.
         */
        @Override
        public int minValue() {
            return -(1 << (bitsStored - 1));
        }

        /**
         * Executes the max value operation.
         *
         * @return the operation result.
         */
        @Override
        public int maxValue() {
            return (1 << (bitsStored - 1)) - 1;
        }

    }

}
