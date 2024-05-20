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
package org.miaixz.bus.image.nimble.codec.jpeg;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ByteKit;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SOFSegment {

    private final byte[] data;
    private final int offset;
    private final int numComponents;

    public SOFSegment(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.numComponents = data[offset + 8] & 255;
        getQTableSelector(numComponents - 1);
    }

    public int offset() {
        return offset;
    }

    public int getMarker() {
        return data[offset] & 255;
    }

    public int getHeaderLength() {
        return ByteKit.bytesToUShortBE(data, offset + 1);
    }

    public int getPrecision() {
        return data[offset + 3] & 255;
    }

    public int getY() {
        return ByteKit.bytesToUShortBE(data, offset + 4);
    }

    public int getX() {
        return ByteKit.bytesToUShortBE(data, offset + 6);
    }

    public int getNumComponents() {
        return numComponents;
    }

    public int getComponentID(int index) {
        return data[offset + 9 + index * 3] & 255;
    }

    public int getXSubsampling(int index) {
        return (data[offset + 10 + index * 3] >> 4) & 15;
    }

    public int getYSubsampling(int index) {
        return (data[offset + 10 + index * 3]) & 15;
    }

    public int getQTableSelector(int index) {
        return data[offset + 11 + index * 3] & 255;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SOF").append(getMarker() - 0xC0)
                .append("[Lf=").append(getHeaderLength())
                .append(", P=").append(getPrecision())
                .append(", Y=").append(getY())
                .append(", X=").append(getX())
                .append(", Nf=").append(numComponents);
        for (int i = 0; i < numComponents; i++) {
            sb.append(", C").append(i + 1).append(Symbol.C_EQUAL).append(getComponentID(i))
                    .append(", H").append(i + 1).append(Symbol.C_EQUAL).append(getXSubsampling(i))
                    .append(", V").append(i + 1).append(Symbol.C_EQUAL).append(getYSubsampling(i))
                    .append(", Tq").append(i + 1).append(Symbol.C_EQUAL).append(getQTableSelector(i));
        }
        sb.append(']');
        return sb.toString();
    }
}
