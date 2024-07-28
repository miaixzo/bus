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
package org.miaixz.bus.image.nimble.codec.mp4;

import java.nio.ByteBuffer;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MP4FileType {

    private final int[] brands;

    public MP4FileType(int majorBrand, int minorVersion, int... compatibleBrands) {
        this.brands = new int[2 + compatibleBrands.length];
        brands[0] = majorBrand;
        brands[1] = minorVersion;
        System.arraycopy(compatibleBrands, 0, brands, 2, compatibleBrands.length);
    }

    private static void append4CC(StringBuilder sb, int brand) {
        sb.append((char) ((brand >>> 24) & 0xFF));
        sb.append((char) ((brand >>> 16) & 0xFF));
        sb.append((char) ((brand >>> 8) & 0xFF));
        sb.append((char) ((brand >>> 0) & 0xFF));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append4CC(sb.append("ftyp["), brands[0]);
        sb.append('.').append(brands[1]);
        for (int i = 2; i < brands.length; i++) {
            append4CC(sb.append(", "), brands[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    public byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(size());
        bb.putInt(bb.remaining());
        bb.putInt(0x66747970);
        for (int brand : brands) {
            bb.putInt(brand);
        }
        return bb.array();
    }

    public int size() {
        return (2 + brands.length) * 4;
    }

    public int majorBrand() {
        return brands[0];
    }

    public int minorVersion() {
        return brands[1];
    }

    public int[] compatibleBrands() {
        int[] compatibleBrands = new int[brands.length - 2];
        System.arraycopy(brands, 2, brands, 0, compatibleBrands.length);
        return compatibleBrands;
    }

}