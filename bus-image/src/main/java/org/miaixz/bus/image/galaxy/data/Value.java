/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.galaxy.data;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Value {

    Value NULL = new Value() {

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int getEncodedLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
            return vr == VR.SQ && encOpts.undefEmptySequenceLength ? -1 : 0;
        }

        @Override
        public void writeTo(ImageOutputStream dos, VR vr) {
        }

        @Override
        public int calcLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
            return vr == VR.SQ && encOpts.undefEmptySequenceLength ? 8 : 0;
        }

        @Override
        public String toString() {
            return Normal.EMPTY;
        }

        @Override
        public byte[] toBytes(VR vr, boolean bigEndian) {
            return new byte[]{};
        }
    };

    boolean isEmpty();

    byte[] toBytes(VR vr, boolean bigEndian) throws IOException;

    void writeTo(ImageOutputStream out, VR vr) throws IOException;

    int calcLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr);

    int getEncodedLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr);

}
