/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.check.crc16;

import java.io.Serial;

/**
 * Implements the CRC16-CCITT-FALSE (Cyclic Redundancy Check) algorithm. This CRC uses the polynomial x16+x12+x5+1
 * (0x1021), an initial value of 0xFFFF, processes data with the low byte last, high byte first, and the final result is
 * XORed with 0x0000.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CRC16CCITTFalse extends CRC16Checksum {

    /**
     * Constructs a new CRC16CCITTFalse. Utility class constructor for static access.
     */
    private CRC16CCITTFalse() {
    }

    @Serial
    private static final long serialVersionUID = 2852278527996L;

    /**
     * The polynomial used in the CRC16-CCITT-FALSE calculation (0x1021).
     */
    private static final int WC_POLY = 0x1021;

    /**
     * Resets the CRC16-CCITT-FALSE calculation to its initial state (0xFFFF).
     */
    @Override
    public void reset() {
        this.wCRCin = 0xffff;
    }

    /**
     * Updates the CRC16-CCITT-FALSE checksum with the specified array of bytes. The internal CRC value is masked with
     * 0xffff after the superclass update.
     *
     * @param b   The byte array to update the checksum with.
     * @param off The start offset in the data.
     * @param len The number of bytes to use for the update.
     */
    @Override
    public void update(final byte[] b, final int off, final int len) {
        super.update(b, off, len);
        wCRCin &= 0xffff;
    }

    /**
     * Updates the CRC16-CCITT-FALSE checksum with the specified byte.
     *
     * @param b The byte to update the checksum with.
     */
    @Override
    public void update(final int b) {
        for (int i = 0; i < 8; i++) {
            final boolean bit = ((b >> (7 - i) & 1) == 1);
            final boolean c15 = ((wCRCin >> 15 & 1) == 1);
            wCRCin <<= 1;
            if (c15 ^ bit)
                wCRCin ^= WC_POLY;
        }
    }

}
