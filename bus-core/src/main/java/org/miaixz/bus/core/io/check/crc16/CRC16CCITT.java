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
 * Implements the CRC16-CCITT (Cyclic Redundancy Check) algorithm. This CRC uses the polynomial x16+x12+x5+1 (0x1021),
 * an initial value of 0x0000, processes data with the low byte first, high byte last, and the final result is XORed
 * with 0x0000. Note: 0x8408 is the bit-reversed representation of 0x1021.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CRC16CCITT extends CRC16Checksum {

    /**
     * Constructs a new CRC16CCITT. Utility class constructor for static access.
     */
    private CRC16CCITT() {
    }

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
