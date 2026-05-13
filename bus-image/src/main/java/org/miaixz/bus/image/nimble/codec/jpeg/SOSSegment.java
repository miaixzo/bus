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
 * Represents the SOSSegment type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SOSSegment {

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
    public SOSSegment(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.numComponents = data[offset + 3] & 255;
        getAl();
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
        return data[offset + 4 + index * 2] & 255;
    }

    /**
     * Gets the ta.
     *
     * @param index the index.
     * @return the ta.
     */
    public int getTa(int index) {
        return (data[offset + 5 + index * 2] >> 4) & 15;
    }

    /**
     * Gets the td.
     *
     * @param index the index.
     * @return the td.
     */
    public int getTd(int index) {
        return (data[offset + 5 + index * 2]) & 15;
    }

    /**
     * Gets the ss.
     *
     * @return the ss.
     */
    public int getSs() {
        return data[offset + 4 + numComponents * 2] & 255;
    }

    /**
     * Gets the se.
     *
     * @return the se.
     */
    public int getSe() {
        return data[offset + 5 + numComponents * 2] & 255;
    }

    /**
     * Gets the ah.
     *
     * @return the ah.
     */
    public int getAh() {
        return (data[offset + 6 + numComponents * 2] >> 4) & 15;
    }

    /**
     * Gets the al.
     *
     * @return the al.
     */
    public int getAl() {
        return (data[offset + 6 + numComponents * 2]) & 15;
    }

    /**
     * Gets the near.
     *
     * @return the near.
     */
    public int getNear() {
        return getSs();
    }

    /**
     * Gets the ilv.
     *
     * @return the ilv.
     */
    public int getILV() {
        return getSe();
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SOS=[Ls=").append(getHeaderLength()).append(", Ns=").append(numComponents);
        for (int i = 0; i < numComponents; i++) {
            sb.append(", C").append(i + 1).append('=').append(getComponentID(i)).append(", Td").append(i + 1)
                    .append('=').append(getTd(i)).append(", Ta").append(i + 1).append('=').append(getTa(i));
        }
        sb.append(", Ss=").append(getSs()).append(", Se=").append(getSe()).append(", Ah=").append(getAh())
                .append(", Al=").append(getAl()).append(']');
        return sb.toString();
    }

}
