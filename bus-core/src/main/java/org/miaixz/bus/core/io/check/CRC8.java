/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.check;

import java.io.Serial;
import java.io.Serializable;
import java.util.zip.Checksum;

/**
 * CRC8 循环冗余校验码（Cyclic Redundancy Check）实现 代码来自：<a href="https://github.com/BBSc0der">https://github.com/BBSc0der</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CRC8 implements Checksum, Serializable {

    @Serial
    private static final long serialVersionUID = 2852285077312L;

    private final short init;
    private final short[] crcTable = new short[256];
    private short value;

    /**
     * 构造
     *
     * @param polynomial Polynomial, typically one of the POLYNOMIAL_* constants.
     * @param init       Initial value, typically either 0xff or zero.
     */
    public CRC8(final int polynomial, final short init) {
        this.value = this.init = init;
        for (int dividend = 0; dividend < 256; dividend++) {
            int remainder = dividend;// << 8;
            for (int bit = 0; bit < 8; ++bit) {
                if ((remainder & 0x01) != 0) {
                    remainder = (remainder >>> 1) ^ polynomial;
                } else {
                    remainder >>>= 1;
                }
            }
            crcTable[dividend] = (short) remainder;
        }
    }

    @Override
    public void update(final byte[] buffer, final int offset, final int len) {
        for (int i = 0; i < len; i++) {
            final int data = buffer[offset + i] ^ value;
            value = (short) (crcTable[data & 0xff] ^ (value << 8));
        }
    }

    /**
     * Updates the current check with the specified array of bytes. Equivalent to calling
     * {@code update(buffer, 0, buffer.length)}.
     *
     * @param buffer the byte array to update the check with
     */
    public void update(final byte[] buffer) {
        update(buffer, 0, buffer.length);
    }

    @Override
    public void update(final int b) {
        update(new byte[] { (byte) b }, 0, 1);
    }

    @Override
    public long getValue() {
        return value & 0xff;
    }

    @Override
    public void reset() {
        value = init;
    }

}
