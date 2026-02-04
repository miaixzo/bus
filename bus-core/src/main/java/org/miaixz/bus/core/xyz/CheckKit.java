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
package org.miaixz.bus.core.xyz;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.miaixz.bus.core.io.stream.EmptyOutputStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Checksum utility.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CheckKit {

    /**
     * Constructs a new CheckKit. Utility class constructor for static access.
     */
    private CheckKit() {
    }

    /**
     * Calculates the CRC32 checksum for a file.
     *
     * @param file The file, cannot be a directory.
     * @return The CRC32 value.
     * @throws InternalException for IO errors.
     */
    public static long checksumCRC32(final File file) throws InternalException {
        return checksum(file, new CRC32()).getValue();
    }

    /**
     * Calculates the CRC32 checksum for an InputStream. The stream is closed after calculation.
     *
     * @param in The InputStream.
     * @return The CRC32 value.
     * @throws InternalException for IO errors.
     */
    public static long checksumCRC32(final InputStream in) throws InternalException {
        return checksum(in, new CRC32()).getValue();
    }

    /**
     * Calculates the checksum for a file using a given Checksum algorithm.
     *
     * @param file     The file, cannot be a directory.
     * @param checksum The {@link Checksum} algorithm to use.
     * @return The {@link Checksum} object with the updated checksum.
     * @throws InternalException for IO errors.
     */
    public static Checksum checksum(final File file, final Checksum checksum) throws InternalException {
        Assert.notNull(file, "File is null !");
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Checksums can't be computed on directories");
        }
        try {
            return checksum(Files.newInputStream(file.toPath()), checksum);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Calculates the checksum for an InputStream using a given Checksum algorithm. The stream is closed after
     * calculation.
     *
     * @param in       The InputStream.
     * @param checksum The {@link Checksum} algorithm to use.
     * @return The {@link Checksum} object with the updated checksum.
     * @throws InternalException for IO errors.
     */
    public static Checksum checksum(InputStream in, Checksum checksum) throws InternalException {
        Assert.notNull(in, "InputStream is null !");
        if (null == checksum) {
            checksum = new CRC32();
        }
        try {
            in = new CheckedInputStream(in, checksum);
            IoKit.copy(in, EmptyOutputStream.INSTANCE);
        } finally {
            IoKit.closeQuietly(in);
        }
        return checksum;
    }

    /**
     * Calculates the checksum value for an InputStream using a given Checksum algorithm. The stream is closed after
     * calculation.
     *
     * @param in       The InputStream.
     * @param checksum The {@link Checksum} algorithm to use.
     * @return The checksum value.
     * @throws InternalException for IO errors.
     */
    public static long checksumValue(final InputStream in, final Checksum checksum) {
        return checksum(in, checksum).getValue();
    }

}
