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
package org.miaixz.bus.image.metric.hl7.net;

import java.io.Serializable;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.image.metric.hl7.HL7Segment;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class UnparsedHL7Message implements Serializable {

    private static final AtomicInteger prevSerialNo = new AtomicInteger();
    private final int serialNo;
    private final byte[] data;
    private transient volatile byte[] unescapeXdddd;
    private transient volatile HL7Segment msh;
    private transient volatile int mshLength;

    public UnparsedHL7Message(byte[] data) {
        this.serialNo = prevSerialNo.incrementAndGet();
        this.data = data;
    }

    private static byte[] unescapeXdddd(byte[] data) {
        int[] pos = findEscapeXdddd(data);
        return pos.length == 0 ? data : replaceXdddd(data, pos);
    }

    private static byte[] replaceXdddd(byte[] src, int[] pos) {
        byte[] dest = new byte[src.length - calcLengthDecrement(pos)];
        int srcPos = 0;
        int destPos = 0;
        int i = 0;
        do {
            int length = pos[i] - srcPos - 2;
            System.arraycopy(src, srcPos, dest, destPos, length);
            srcPos += length;
            length = replaceXdddd(src, pos[i], pos[++i], dest, destPos += length);
            srcPos += 3 + length;
            destPos += length / 2;
        } while (++i < pos.length);
        System.arraycopy(src, srcPos, dest, destPos, src.length - srcPos);
        return dest;
    }

    private static int replaceXdddd(byte[] src, int beginIndex, int endIndex, byte[] dest, int destPos) {
        for (int i = beginIndex; i < endIndex;) {
            dest[destPos++] = (byte) parseHex(src[i++], src[i++]);
        }
        return endIndex - beginIndex;
    }

    private static int calcLengthDecrement(int[] pos) {
        int i = pos.length;
        int l = 0;
        do {
            l += pos[--i];
            l -= pos[--i];
        } while (i > 0);
        return (l + pos.length * 3) / 2;
    }

    private static int[] findEscapeXdddd(byte[] data) {
        int[] pos = {};
        int x = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x58) { // == X
                if (i > 0 && data[i - 1] == data[6]) {
                    x = i + 1;
                }
            } else if (x > 0 && data[i] == data[6]) {
                if (validHexAndNoSeparator(data, x, i)) {
                    pos = Arrays.copyOf(pos, pos.length + 2);
                    pos[pos.length - 2] = x;
                    pos[pos.length - 1] = i;
                }
                x = 0;
            }
        }
        return pos;
    }

    private static boolean validHexAndNoSeparator(byte[] data, int beginIndex, int endIndex) {
        if (((endIndex - beginIndex) & 1) != 0)
            return false;
        int d;
        for (int i = beginIndex; i < endIndex;) {
            if ((d = parseHex(data[i++], data[i++])) < 0 || d == data[3] // field separator
                    || d == data[4] // component separator
                    || d == data[5] // repetition separator
                    || d == data[6] // escape character
                    || d == data[7] // subcomponent separator
            ) {
                return false;
            }
        }
        return true;
    }

    private static int parseHex(int ch1, int ch2) {
        return (parseHex(ch1) << 4) | parseHex(ch2);
    }

    private static int parseHex(int ch) {
        int d = ch - 0x30;
        if (d > 9) {
            d = ch - 0x41;
            if (d > 5) {
                d = ch - 0x61;
                if (d > 5)
                    return -1;
            }
            if (d >= 0)
                d += 10;
        }
        return d;
    }

    public HL7Segment msh() {
        init();
        return msh;
    }

    public int getSerialNo() {
        return serialNo;
    }

    private void init() {
        if (msh == null) {
            ParsePosition pos = new ParsePosition(0);
            msh = HL7Segment.parseMSH(data, data.length, pos);
            mshLength = pos.getIndex();
        }
    }

    public byte[] data() {
        return data;
    }

    @Override
    public String toString() {
        if (mshLength == 0) {
            int mshlen = 0;
            while (mshlen < data.length && data[mshlen] != '\r')
                mshlen++;
            mshLength = mshlen;
        }
        return new String(data, 0, mshLength);
    }

    /**
     * Return HL7 message with unescaped hexdata from \Xdddd\ escape sequences. Does not unescape \Xdddd\ escape
     * sequences which contains a field separator, component separator, subcomponent separator, repetition separator or
     * escape character.
     *
     * @return HL7 message with unescaped hexdata from \Xdddd\ escape sequences
     */
    public byte[] unescapeXdddd() {
        if (unescapeXdddd == null)
            unescapeXdddd = unescapeXdddd(data);
        return unescapeXdddd;
    }

}
