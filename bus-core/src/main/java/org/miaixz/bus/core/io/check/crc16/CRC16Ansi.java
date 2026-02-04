/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io.check.crc16;

import java.io.Serial;

/**
 * Implements the CRC16-ANSI (Cyclic Redundancy Check) algorithm.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CRC16Ansi extends CRC16Checksum {

    /**
     * Constructs a new CRC16Ansi. Utility class constructor for static access.
     */
    private CRC16Ansi() {
    }

    @Serial
    private static final long serialVersionUID = 2852278118286L;

    /**
     * The polynomial used in the CRC16-ANSI calculation.
     */
    private static final int WC_POLY = 0xa001;

    /**
     * Resets the CRC16-ANSI calculation to its initial state (0xFFFF).
     */
    @Override
    public void reset() {
        this.wCRCin = 0xffff;
    }

    /**
     * Updates the CRC16-ANSI checksum with the specified byte.
     *
     * @param b The byte to update the checksum with.
     */
    @Override
    public void update(final int b) {
        int hi = wCRCin >> 8;
        hi ^= b;
        wCRCin = hi;

        for (int i = 0; i < 8; i++) {
            final int flag = wCRCin & 0x0001;
            wCRCin = wCRCin >> 1;
            if (flag == 1) {
                wCRCin ^= WC_POLY;
            }
        }
    }

}
