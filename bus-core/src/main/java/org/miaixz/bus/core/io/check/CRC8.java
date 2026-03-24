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
package org.miaixz.bus.core.io.check;

import java.io.Serial;
import java.io.Serializable;
import java.util.zip.Checksum;

/**
 * Implements the CRC8 (Cyclic Redundancy Check) algorithm. This code is adapted from
 * <a href="https://github.com/BBSc0der">https://github.com/BBSc0der</a>.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CRC8 implements Checksum, Serializable {

    @Serial
    private static final long serialVersionUID = 2852278059978L;

    /**
     * The initial value for the CRC calculation.
     */
    private final short init;
    /**
     * The CRC lookup table.
     */
    private final short[] crcTable = new short[256];
    /**
     * The current CRC value.
     */
    private short value;

    /**
     * Constructs a new CRC8 instance with the specified polynomial and initial value.
     *
     * @param polynomial The polynomial to use for CRC calculation, typically one of the POLYNOMIAL_* constants.
     * @param init       The initial value for the CRC, typically either 0xff or zero.
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

    /**
     * Updates the current CRC with the specified array of bytes.
     *
     * @param buffer The byte array to update the CRC with.
     * @param offset The start offset in the data.
     * @param len    The number of bytes to use for the update.
     */
    @Override
    public void update(final byte[] buffer, final int offset, final int len) {
        for (int i = 0; i < len; i++) {
            final int data = buffer[offset + i] ^ value;
            value = (short) (crcTable[data & 0xff] ^ (value << 8));
        }
    }

    /**
     * Updates the current CRC with the specified array of bytes. Equivalent to calling
     * {@code update(buffer, 0, buffer.length)}.
     *
     * @param buffer The byte array to update the CRC with.
     */
    public void update(final byte[] buffer) {
        update(buffer, 0, buffer.length);
    }

    /**
     * Updates the current CRC with the specified byte.
     *
     * @param b The byte to update the CRC with.
     */
    @Override
    public void update(final int b) {
        update(new byte[] { (byte) b }, 0, 1);
    }

    /**
     * Returns the current CRC value.
     *
     * @return The current CRC value.
     */
    @Override
    public long getValue() {
        return value & 0xff;
    }

    /**
     * Resets the CRC to its initial value.
     */
    @Override
    public void reset() {
        value = init;
    }

}
