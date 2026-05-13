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
package org.miaixz.bus.image.metric.hl7.net;

import java.io.Serializable;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.image.metric.hl7.HL7Segment;

/**
 * Represents the UnparsedHL7Message type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class UnparsedHL7Message implements Serializable {

    /**
     * The prev serial no value.
     */
    private static final AtomicInteger prevSerialNo = new AtomicInteger();

    /**
     * The serial no value.
     */
    private final int serialNo;

    /**
     * The data value.
     */
    private final byte[] data;

    /**
     * The unescape xdddd value.
     */
    private transient volatile byte[] unescapeXdddd;

    /**
     * The msh value.
     */
    private transient volatile HL7Segment msh;

    /**
     * The msh length value.
     */
    private transient volatile int mshLength;

    /**
     * Creates a new instance.
     *
     * @param data the data.
     */
    public UnparsedHL7Message(byte[] data) {
        this.serialNo = prevSerialNo.incrementAndGet();
        this.data = data;
    }

    /**
     * Executes the unescape xdddd operation.
     *
     * @param data the data.
     * @return the operation result.
     */
    private static byte[] unescapeXdddd(byte[] data) {
        int[] pos = findEscapeXdddd(data);
        return pos.length == 0 ? data : replaceXdddd(data, pos);
    }

    /**
     * Executes the replace xdddd operation.
     *
     * @param src the src.
     * @param pos the pos.
     * @return the operation result.
     */
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

    /**
     * Executes the replace xdddd operation.
     *
     * @param src        the src.
     * @param beginIndex the begin index.
     * @param endIndex   the end index.
     * @param dest       the dest.
     * @param destPos    the dest pos.
     * @return the operation result.
     */
    private static int replaceXdddd(byte[] src, int beginIndex, int endIndex, byte[] dest, int destPos) {
        for (int i = beginIndex; i < endIndex;) {
            dest[destPos++] = (byte) parseHex(src[i++], src[i++]);
        }
        return endIndex - beginIndex;
    }

    /**
     * Executes the calc length decrement operation.
     *
     * @param pos the pos.
     * @return the operation result.
     */
    private static int calcLengthDecrement(int[] pos) {
        int i = pos.length;
        int l = 0;
        do {
            l += pos[--i];
            l -= pos[--i];
        } while (i > 0);
        return (l + pos.length * 3) / 2;
    }

    /**
     * Finds the escape xdddd.
     *
     * @param data the data.
     * @return the operation result.
     */
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

    /**
     * Executes the valid hex and no separator operation.
     *
     * @param data       the data.
     * @param beginIndex the begin index.
     * @param endIndex   the end index.
     * @return true if the condition is met; otherwise false.
     */
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

    /**
     * Parses the hex.
     *
     * @param ch1 the ch1.
     * @param ch2 the ch2.
     * @return the operation result.
     */
    private static int parseHex(int ch1, int ch2) {
        return (parseHex(ch1) << 4) | parseHex(ch2);
    }

    /**
     * Parses the hex.
     *
     * @param ch the ch.
     * @return the operation result.
     */
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

    /**
     * Executes the msh operation.
     *
     * @return the operation result.
     */
    public HL7Segment msh() {
        init();
        return msh;
    }

    /**
     * Gets the serial no.
     *
     * @return the serial no.
     */
    public int getSerialNo() {
        return serialNo;
    }

    /**
     * Executes the init operation.
     */
    private void init() {
        if (msh == null) {
            ParsePosition pos = new ParsePosition(0);
            msh = HL7Segment.parseMSH(data, data.length, pos);
            mshLength = pos.getIndex();
        }
    }

    /**
     * Executes the data operation.
     *
     * @return the operation result.
     */
    public byte[] data() {
        return data;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
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
