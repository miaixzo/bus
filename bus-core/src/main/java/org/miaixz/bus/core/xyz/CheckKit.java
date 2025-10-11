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
