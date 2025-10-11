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
import java.io.Serializable;
import java.util.zip.Checksum;

import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Abstract base class for CRC16 checksum implementations. This class provides a common structure for various CRC16
 * algorithms. Subclasses should override the {@link #update(int)} and {@link #reset()} methods to implement specific
 * CRC16 algorithms.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class CRC16Checksum implements Checksum, Serializable {

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852278688761L;

    /**
     * The current CRC16 checksum value.
     */
    protected int wCRCin;

    /**
     * Constructs a new CRC16Checksum and resets its internal state to the initial value.
     */
    public CRC16Checksum() {
        reset();
    }

    /**
     * Returns the current CRC16 checksum value.
     *
     * @return The current CRC16 checksum value.
     */
    @Override
    public long getValue() {
        return wCRCin;
    }

    /**
     * Retrieves the CRC16 value as a hexadecimal string.
     *
     * @return The CRC16 value in hexadecimal format.
     */
    public String getHexValue() {
        return getHexValue(false);
    }

    /**
     * Retrieves the CRC16 value as a hexadecimal string, with optional zero-padding.
     *
     * @param isPadding If {@code true}, the hexadecimal string will be padded with leading zeros to ensure a length of
     *                  4 characters.
     * @return The CRC16 value in hexadecimal format.
     */
    public String getHexValue(final boolean isPadding) {
        String hex = HexKit.toHex(getValue());
        if (isPadding) {
            hex = StringKit.padPre(hex, 4, '0');
        }

        return hex;
    }

    /**
     * Resets the CRC16 checksum to its initial value (0x0000).
     */
    @Override
    public void reset() {
        wCRCin = 0x0000;
    }

    /**
     * Updates the CRC16 checksum with the specified array of bytes. This method is equivalent to calling
     * {@link #update(byte[], int, int)} with an offset of 0 and the full length of the array.
     *
     * @param b The byte array to update the checksum with.
     */
    public void update(final byte[] b) {
        update(b, 0, b.length);
    }

    /**
     * Updates the CRC16 checksum with the specified array of bytes.
     *
     * @param b   The byte array to update the checksum with.
     * @param off The start offset in the data.
     * @param len The number of bytes to use for the update.
     */
    @Override
    public void update(final byte[] b, final int off, final int len) {
        for (int i = off; i < off + len; i++)
            update(b[i]);
    }

}
