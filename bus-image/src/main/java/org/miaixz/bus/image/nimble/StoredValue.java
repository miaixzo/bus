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

import org.miaixz.bus.core.lang.Normal;
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
        return attrs.getInt(Tag.PixelRepresentation, 0) != 0
                ? new Signed(bitsStored)
                : new Unsigned(bitsStored);
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
            this.shift = Normal._32 - bitsStored;
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
