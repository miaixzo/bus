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
package org.miaixz.bus.image.nimble.codec.mp4;

import java.nio.ByteBuffer;

/**
 * @author Kimi Liu
 * @since Java 21+
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
