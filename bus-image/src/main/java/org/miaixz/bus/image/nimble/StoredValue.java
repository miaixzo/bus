/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class StoredValue {

    public static StoredValue valueOf(Attributes attrs) {
        int bitsStored = attrs.getInt(Tag.BitsStored, 0);
        if (bitsStored == 0)
            bitsStored = attrs.getInt(Tag.BitsAllocated, 8);
        return attrs.getInt(Tag.PixelRepresentation, 0) != 0 ? new Signed(bitsStored) : new Unsigned(bitsStored);
    }

    public abstract int valueOf(int pixel);

    public abstract int minValue();

    public abstract int maxValue();

    public static class Unsigned extends StoredValue {

        private final int mask;

        public Unsigned(int bitsStored) {
            this.mask = (1 << bitsStored) - 1;
        }

        @Override
        public int valueOf(int pixel) {
            return pixel & mask;
        }

        @Override
        public int minValue() {
            return 0;
        }

        @Override
        public int maxValue() {
            return mask;
        }
    }

    public static class Signed extends StoredValue {

        private final int bitsStored;
        private final int shift;

        public Signed(int bitsStored) {
            this.bitsStored = bitsStored;
            this.shift = 32 - bitsStored;
        }

        @Override
        public int valueOf(int pixel) {
            return pixel << shift >> shift;
        }

        @Override
        public int minValue() {
            return -(1 << (bitsStored - 1));
        }

        @Override
        public int maxValue() {
            return (1 << (bitsStored - 1)) - 1;
        }
    }

}
