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
package org.miaixz.bus.core.io.unit;

import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Enumeration representing common data units and their corresponding sizes. This class is inspired by Spring
 * Framework's DataUnit.
 *
 * <pre>
 *     BYTES      1B      2^0     1
 *     KILOBYTES  1KB     2^10    1,024
 *     MEGABYTES  1MB     2^20    1,048,576
 *     GIGABYTES  1GB     2^30    1,073,741,824
 *     TERABYTES  1TB     2^40    1,099,511,627,776
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum DataUnit {

    /**
     * Bytes, represented by the suffix: {@code B}.
     */
    BYTES("B", DataSize.ofBytes(1)),

    /**
     * Kilobytes, represented by the suffix: {@code KB}.
     */
    KILOBYTES("KB", DataSize.ofKilobytes(1)),

    /**
     * Megabytes, represented by the suffix: {@code MB}.
     */
    MEGABYTES("MB", DataSize.ofMegabytes(1)),

    /**
     * Gigabytes, represented by the suffix: {@code GB}.
     */
    GIGABYTES("GB", DataSize.ofGigabytes(1)),

    /**
     * Terabytes, represented by the suffix: {@code TB}.
     */
    TERABYTES("TB", DataSize.ofTerabytes(1));

    /**
     * The suffix string representing this data unit (e.g., "B", "KB", "MB").
     */
    private final String suffix;

    /**
     * The {@link DataSize} object representing one unit of this type (e.g., 1 byte, 1 kilobyte).
     */
    private final DataSize size;

    /**
     * Constructs a {@code DataUnit} enum constant.
     *
     * @param suffix The string suffix for this unit (e.g., "B", "KB").
     * @param size   The {@link DataSize} representing one unit of this type.
     */
    DataUnit(final String suffix, final DataSize size) {
        this.suffix = suffix;
        this.size = size;
    }

    /**
     * Returns the {@code DataUnit} corresponding to the given suffix. This method supports common suffixes like "KB",
     * "MB", "GB", "TB", and also handles variations like "KiB", "MiB", "GiB" by converting them to their standard
     * forms. It also supports case-insensitive matching and partial matches (e.g., "M" for "MB").
     *
     * @param suffix The unit suffix string (e.g., "KB", "GB", "GiB").
     * @return The matching {@link DataUnit}.
     * @throws IllegalArgumentException If the suffix is not recognized.
     */
    public static DataUnit fromSuffix(String suffix) {
        // Handle KiB, MiB, GiB variations by converting to KB, MB, GB
        if (StringKit.length(suffix) == 3 && CharKit.equals(suffix.charAt(1), 'i', true)) {
            suffix = new String(new char[] { suffix.charAt(0), suffix.charAt(2) });
        }

        for (final DataUnit candidate : values()) {
            // Supports variations like 3MB, 3M, 3m
            if (StringKit.startWithIgnoreCase(candidate.suffix, suffix)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Unknown data unit suffix '" + suffix + "'");
    }

    /**
     * Returns the suffix string for this data unit.
     *
     * @return The unit suffix (e.g., "B", "KB").
     */
    public String getSuffix() {
        return this.suffix;
    }

    /**
     * Returns the {@link DataSize} object representing one unit of this type.
     *
     * @return The {@link DataSize} equivalent of one unit.
     */
    DataSize getSize() {
        return this.size;
    }

}
