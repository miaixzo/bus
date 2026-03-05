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
package org.miaixz.bus.core.io.check.crc16;

import java.io.Serial;

/**
 * Implements the CRC-CCITT (XModem) Cyclic Redundancy Check algorithm. This CRC uses the polynomial x16+x12+x5+1
 * (0x1021), an initial value of 0x0000, processes data with the low byte last, high byte first, and the final result is
 * XORed with 0x0000.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CRC16XModem extends CRC16Checksum {

    /**
     * Constructs a new CRC16XModem. Utility class constructor for static access.
     */
    private CRC16XModem() {
    }

    @Serial
    private static final long serialVersionUID = 2852285115000L;

    /**
     * The polynomial used in the CRC16-XModem calculation (0x1021). This corresponds to bits at positions 0, 5, and 12.
     */
    private static final int WC_POLY = 0x1021;

    /**
     * Updates the CRC16-XModem checksum with the specified array of bytes. The internal CRC value is masked with 0xffff
     * after the superclass update.
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
     * Updates the CRC16-XModem checksum with the specified byte.
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
