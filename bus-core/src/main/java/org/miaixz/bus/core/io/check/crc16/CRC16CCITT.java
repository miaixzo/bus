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
package org.miaixz.bus.core.io.check.crc16;

import java.io.Serial;

/**
 * Implements the CRC16-CCITT (Cyclic Redundancy Check) algorithm. This CRC uses the polynomial x16+x12+x5+1 (0x1021),
 * an initial value of 0x0000, processes data with the low byte first, high byte last, and the final result is XORed
 * with 0x0000. Note: 0x8408 is the bit-reversed representation of 0x1021.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CRC16CCITT extends CRC16Checksum {

    /**
     * Constructs a new CRC16CCITT. Utility class constructor for static access.
     */
    private CRC16CCITT() {
    }

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852278296375L;

    /**
     * The polynomial used in the CRC16-CCITT calculation. It is the bit-reversed form of 0x1021.
     */
    private static final int WC_POLY = 0x8408;

    /**
     * Updates the CRC16-CCITT checksum with the specified byte.
     *
     * @param b The byte to update the checksum with.
     */
    @Override
    public void update(final int b) {
        wCRCin ^= (b & 0x00ff);
        for (int j = 0; j < 8; j++) {
            if ((wCRCin & 0x0001) != 0) {
                wCRCin >>= 1;
                wCRCin ^= WC_POLY;
            } else {
                wCRCin >>= 1;
            }
        }
    }

}
