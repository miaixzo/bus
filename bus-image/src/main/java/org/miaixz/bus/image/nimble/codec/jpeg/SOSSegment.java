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
 * @author Kimi Liu
 * @since Java 21+
 */
public class SOSSegment {

    private final byte[] data;
    private final int offset;
    private final int numComponents;

    public SOSSegment(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.numComponents = data[offset + 3] & 255;
        getAl();
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

    public int getNumComponents() {
        return numComponents;
    }

    public int getComponentID(int index) {
        return data[offset + 4 + index * 2] & 255;
    }

    public int getTa(int index) {
        return (data[offset + 5 + index * 2] >> 4) & 15;
    }

    public int getTd(int index) {
        return (data[offset + 5 + index * 2]) & 15;
    }

    public int getSs() {
        return data[offset + 4 + numComponents * 2] & 255;
    }

    public int getSe() {
        return data[offset + 5 + numComponents * 2] & 255;
    }

    public int getAh() {
        return (data[offset + 6 + numComponents * 2] >> 4) & 15;
    }

    public int getAl() {
        return (data[offset + 6 + numComponents * 2]) & 15;
    }

    public int getNear() {
        return getSs();
    }

    public int getILV() {
        return getSe();
    }

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
