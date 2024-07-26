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
package org.miaixz.bus.image.nimble.codec.jpeg;

import org.miaixz.bus.core.xyz.ByteKit;

/**
 * @author Kimi Liu
 * @since Java 17+
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
