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
package org.miaixz.bus.image.nimble.codec.jpeg;

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
        sb.append("SOF").append(getMarker() - 0xC0).append("[Lf=").append(getHeaderLength()).append(", P=")
                .append(getPrecision()).append(", Y=").append(getY()).append(", X=").append(getX()).append(", Nf=")
                .append(numComponents);
        for (int i = 0; i < numComponents; i++) {
            sb.append(", C").append(i + 1).append('=').append(getComponentID(i)).append(", H").append(i + 1).append('=')
                    .append(getXSubsampling(i)).append(", V").append(i + 1).append('=').append(getYSubsampling(i))
                    .append(", Tq").append(i + 1).append('=').append(getQTableSelector(i));
        }
        sb.append(']');
        return sb.toString();
    }

}
