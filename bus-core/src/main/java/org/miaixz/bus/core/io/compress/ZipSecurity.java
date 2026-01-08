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
package org.miaixz.bus.core.io.compress;

import java.util.zip.ZipEntry;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Zip security related classes, such as checking for Zip bomb vulnerabilities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipSecurity {

    /**
     * Checks for Zip bomb vulnerabilities.
     *
     * @param entry       The {@link ZipEntry} to check.
     * @param maxSizeDiff The maximum size difference multiplier for the ZipBomb check. A value of -1 indicates no
     *                    ZipBomb check.
     * @return The checked {@link ZipEntry}.
     * @throws ValidateException if a Zip bomb attack is detected.
     */
    public static ZipEntry checkZipBomb(final ZipEntry entry, final int maxSizeDiff) {
        if (null == entry) {
            return null;
        }
        if (maxSizeDiff < 0 || entry.isDirectory()) {
            // Directories are not checked.
            return entry;
        }

        final long compressedSize = entry.getCompressedSize();
        final long uncompressedSize = entry.getSize();
        // Console.logger(entry.getName(), compressedSize, uncompressedSize);
        if (compressedSize < 0 || uncompressedSize < 0 ||
        // Default compression ratio is 100 times. If the compression ratio exceeds this threshold, it is considered a
        // Zip bomb.
                compressedSize * maxSizeDiff < uncompressedSize) {
            throw new ValidateException(
                    "Zip bomb attack detected, invalid sizes: compressed {}, uncompressed {}, name {}", compressedSize,
                    uncompressedSize, entry.getName());
        }
        return entry;
    }

}
