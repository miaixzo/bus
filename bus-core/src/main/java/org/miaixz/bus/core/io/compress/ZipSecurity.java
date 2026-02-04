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
