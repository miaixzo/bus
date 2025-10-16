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
package org.miaixz.bus.core.io.copier;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Encapsulates data copying between {@link FileChannel} instances.
 *
 * <pre>{@code
 * The implementation of FileChannel#transferTo or FileChannel#transferFrom is platform-specific,
 * and compatibility with lower-version platforms needs to be ensured.
 * For example, on Android platforms below version 7, when decompressing files using ZipInputStream,
 * if FileChannel#transferFrom is used to transfer to a file, its return value may be less than totalBytes,
 * which will lead to file content loss if not handled.
 *
 * // Incorrect usage: dstChannel.transferFrom returns a value less than zipEntry.getSize(),
 * // resulting in missing file content after decompression.
 * try (InputStream srcStream = zipFile.getInputStream(zipEntry);
 *     ReadableByteChannel srcChannel = Channels.newChannel(srcStream);
 *     FileOutputStream fos = new FileOutputStream(saveFile);
 *     FileChannel dstChannel = fos.getChannel()) {
 *     dstChannel.transferFrom(srcChannel, 0, zipEntry.getSize());
 *  }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileChannelCopier extends IoCopier<FileChannel, FileChannel> {

    /**
     * Constructs a {@code FileChannelCopier} with the specified total count of bytes to copy.
     *
     * @param count The total number of bytes to copy. A value of -1 indicates no limit.
     */
    public FileChannelCopier(final long count) {
        super(-1, count, null);
    }

    /**
     * Creates a {@link FileChannel} copier with no limit on the number of bytes to copy.
     *
     * @return A new {@code FileChannelCopier} instance.
     */
    public static FileChannelCopier of() {
        return of(-1);
    }

    /**
     * Creates a {@link FileChannel} copier with the specified total count of bytes to copy.
     *
     * @param count The total number of bytes to copy. A value of -1 indicates no limit.
     * @return A new {@code FileChannelCopier} instance.
     */
    public static FileChannelCopier of(final long count) {
        return new FileChannelCopier(count);
    }

    /**
     * Copies data from a {@link FileInputStream} to a {@link FileOutputStream} using NIO {@link FileChannel}.
     *
     * @param in  The input {@link FileInputStream}.
     * @param out The output {@link FileOutputStream}.
     * @return The number of bytes copied.
     * @throws InternalException if an I/O error occurs during the copy operation.
     */
    public long copy(final FileInputStream in, final FileOutputStream out) throws InternalException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = in.getChannel();
            outChannel = out.getChannel();
            return copy(inChannel, outChannel);
        } finally {
            IoKit.closeQuietly(outChannel);
            IoKit.closeQuietly(inChannel);
        }
    }

    /**
     * Copies data from the source {@link FileChannel} to the target {@link FileChannel}.
     *
     * @param source The source {@link FileChannel}.
     * @param target The target {@link FileChannel}.
     * @return The total number of bytes copied.
     * @throws InternalException if an {@link IOException} occurs during the copy operation.
     */
    @Override
    public long copy(final FileChannel source, final FileChannel target) {
        try {
            return doCopySafely(source, target);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Implements file copying safely, addressing platform-specific behaviors of {@code FileChannel#transferTo} or
     * {@code FileChannel#transferFrom}.
     *
     * <pre>
     * The implementation of FileChannel#transferTo or FileChannel#transferFrom is platform-specific,
     * and compatibility with lower-version platforms needs to be ensured.
     * For example, on Android platforms below version 7, when decompressing files using ZipInputStream,
     * if FileChannel#transferFrom is used to transfer to a file, its return value may be less than totalBytes,
     * which will lead to file content loss if not handled.
     *
     * // Incorrect usage: dstChannel.transferFrom returns a value less than zipEntry.getSize(),
     * // resulting in missing file content after decompression.
     * try (InputStream srcStream = zipFile.getInputStream(zipEntry);
     * 		ReadableByteChannel srcChannel = Channels.newChannel(srcStream);
     * 		FileOutputStream fos = new FileOutputStream(saveFile);
     * 		FileChannel dstChannel = fos.getChannel()) {
     * 		dstChannel.transferFrom(srcChannel, 0, zipEntry.getSize());
     *  }
     * </pre>
     *
     * @param inChannel  The input {@link FileChannel}.
     * @param outChannel The output {@link FileChannel}.
     * @return The number of bytes transferred from the input channel.
     * @throws IOException If an I/O error occurs.
     * @see <a href=
     *      "http://androidxref.com/6.0.1_r10/xref/libcore/luni/src/main/java/java/nio/FileChannelImpl.java">FileChannelImpl.java
     *      (Android 6.0.1)</a>
     * @see <a href=
     *      "http://androidxref.com/7.0.0_r1/xref/libcore/ojluni/src/main/java/sun/nio/ch/FileChannelImpl.java">FileChannelImpl.java
     *      (Android 7.0.0)</a>
     * @see <a href=
     *      "http://androidxref.com/7.0.0_r1/xref/libcore/ojluni/src/main/native/FileChannelImpl.c">FileChannelImpl.c
     *      (Android 7.0.0)</a>
     */
    private long doCopySafely(final FileChannel inChannel, final FileChannel outChannel) throws IOException {
        long totalBytes = inChannel.size();
        if (this.count > 0 && this.count < totalBytes) {
            // Limit the total number of bytes to copy
            totalBytes = count;
        }
        for (long pos = 0, remaining = totalBytes; remaining > 0;) { // Ensure no file content is lost
            final long writeBytes = inChannel.transferTo(pos, remaining, outChannel); // Actual bytes transferred
            pos += writeBytes;
            remaining -= writeBytes;
        }
        return totalBytes;
    }

}
