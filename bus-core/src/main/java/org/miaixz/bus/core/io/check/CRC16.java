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

import org.miaixz.bus.core.io.check.crc16.CRC16Checksum;
import org.miaixz.bus.core.io.check.crc16.CRC16IBM;

/**
 * Implements the CRC16 (Cyclic Redundancy Check) algorithm, using the IBM algorithm by default.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CRC16 implements Checksum, Serializable {

    /**
     * The serial version UID for serialization.
     */
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
