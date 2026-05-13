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

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Represents the StoredValue type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class StoredValue {

    /**
     * Executes the value of operation.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
    public static StoredValue valueOf(Attributes attrs) {
        int bitsStored = attrs.getInt(Tag.BitsStored, 0);
        if (bitsStored == 0)
            bitsStored = attrs.getInt(Tag.BitsAllocated, 8);
        return attrs.getInt(Tag.PixelRepresentation, 0) != 0 ? new Signed(bitsStored) : new Unsigned(bitsStored);
    }

    /**
     * Executes the value of operation.
     *
     * @param pixel the pixel.
     * @return the operation result.
     */
    public abstract int valueOf(int pixel);

    /**
     * Executes the min value operation.
     *
     * @return the operation result.
     */
    public abstract int minValue();

    /**
     * Executes the max value operation.
     *
     * @return the operation result.
     */
    public abstract int maxValue();

    /**
     * Represents the Unsigned type.
     *
     * @author Kimi Liu
     * @since Java 21+
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
     * @since Java 21+
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
