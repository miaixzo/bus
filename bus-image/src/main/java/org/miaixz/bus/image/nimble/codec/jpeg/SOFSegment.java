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
package org.miaixz.bus.image.nimble.codec.jpeg;

import org.miaixz.bus.core.xyz.ByteKit;

/**
 * Represents the SOFSegment type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SOFSegment {

    /**
     * The data value.
     */
    private final byte[] data;

    /**
     * The offset value.
     */
    private final int offset;

    /**
     * The num components value.
     */
    private final int numComponents;

    /**
     * Creates a new instance.
     *
     * @param data   the data.
     * @param offset the offset.
     */
    public SOFSegment(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.numComponents = data[offset + 8] & 255;
        getQTableSelector(numComponents - 1);
    }

    /**
     * Executes the offset operation.
     *
     * @return the operation result.
     */
    public int offset() {
        return offset;
    }

    /**
     * Gets the marker.
     *
     * @return the marker.
     */
    public int getMarker() {
        return data[offset] & 255;
    }

    /**
     * Gets the header length.
     *
     * @return the header length.
     */
    public int getHeaderLength() {
        return ByteKit.bytesToUShortBE(data, offset + 1);
    }

    /**
     * Gets the precision.
     *
     * @return the precision.
     */
    public int getPrecision() {
        return data[offset + 3] & 255;
    }

    /**
     * Gets the y.
     *
     * @return the y.
     */
    public int getY() {
        return ByteKit.bytesToUShortBE(data, offset + 4);
    }

    /**
     * Gets the x.
     *
     * @return the x.
     */
    public int getX() {
        return ByteKit.bytesToUShortBE(data, offset + 6);
    }

    /**
     * Gets the num components.
     *
     * @return the num components.
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * Gets the component id.
     *
     * @param index the index.
     * @return the component id.
     */
    public int getComponentID(int index) {
        return data[offset + 9 + index * 3] & 255;
    }

    /**
     * Gets the x subsampling.
     *
     * @param index the index.
     * @return the x subsampling.
     */
    public int getXSubsampling(int index) {
        return (data[offset + 10 + index * 3] >> 4) & 15;
    }

    /**
     * Gets the y subsampling.
     *
     * @param index the index.
     * @return the y subsampling.
     */
    public int getYSubsampling(int index) {
        return (data[offset + 10 + index * 3]) & 15;
    }

    /**
     * Gets the q table selector.
     *
     * @param index the index.
     * @return the q table selector.
     */
    public int getQTableSelector(int index) {
        return data[offset + 11 + index * 3] & 255;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
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
