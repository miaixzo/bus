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
package org.miaixz.bus.core.io.check;

import java.io.Serial;
import java.io.Serializable;
import java.util.zip.Checksum;

import org.miaixz.bus.core.io.check.crc16.CRC16Checksum;
import org.miaixz.bus.core.io.check.crc16.CRC16IBM;

/**
 * Implements the CRC16 (Cyclic Redundancy Check) algorithm, using the IBM algorithm by default.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CRC16 implements Checksum, Serializable {

    @Serial
    private static final long serialVersionUID = 2852277865069L;

    /**
     * The underlying CRC16Checksum implementation.
     */
    private final CRC16Checksum crc16;

    /**
     * Constructs a new CRC16 instance using the default IBM CRC16 algorithm.
     */
    public CRC16() {
        this(new CRC16IBM());
    }

    /**
     * Constructs a new CRC16 instance with a custom {@link CRC16Checksum} implementation.
     *
     * @param crc16Checksum The custom {@link CRC16Checksum} implementation to use.
     */
    public CRC16(final CRC16Checksum crc16Checksum) {
        this.crc16 = crc16Checksum;
    }

    /**
     * Retrieves the CRC16 value as a hexadecimal string.
     *
     * @return The CRC16 value in hexadecimal format.
     */
    public String getHexValue() {
        return this.crc16.getHexValue();
    }

    /**
     * Retrieves the CRC16 value as a hexadecimal string, with optional zero-padding.
     *
     * @param isPadding If {@code true}, the hexadecimal string will be padded with leading zeros to ensure a length of
     *                  4 characters.
     * @return The CRC16 value in hexadecimal format.
     */
    public String getHexValue(final boolean isPadding) {
        return crc16.getHexValue(isPadding);
    }

    /**
     * Returns the current CRC16 value.
     *
     * @return The current CRC16 value.
     */
    @Override
    public long getValue() {
        return crc16.getValue();
    }

    /**
     * Resets the CRC16 calculation to its initial state.
     */
    @Override
    public void reset() {
        crc16.reset();
    }

    /**
     * Updates the CRC16 with the specified array of bytes.
     *
     * @param b   The byte array to update the CRC16 with.
     * @param off The start offset in the data.
     * @param len The number of bytes to use for the update.
     */
    @Override
    public void update(final byte[] b, final int off, final int len) {
        crc16.update(b, off, len);
    }

    /**
     * Updates the CRC16 with the specified byte.
     *
     * @param b The byte to update the CRC16 with.
     */
    @Override
    public void update(final int b) {
        crc16.update(b);
    }

}
