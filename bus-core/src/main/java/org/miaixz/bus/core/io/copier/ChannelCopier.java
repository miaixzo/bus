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
package org.miaixz.bus.core.io.copier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.miaixz.bus.core.io.StreamProgress;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Copies data from a {@link ReadableByteChannel} to a {@link WritableByteChannel}. This class extends {@link IoCopier}
 * to provide channel-specific copying functionality.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChannelCopier extends IoCopier<ReadableByteChannel, WritableByteChannel> {

    /**
     * Constructs a {@code ChannelCopier} with a default buffer size of 8192 bytes.
     */
    public ChannelCopier() {
        this(Normal._8192);
    }

    /**
     * Constructs a {@code ChannelCopier} with the specified buffer size.
     *
     * @param bufferSize The size of the buffer to use for copying.
     */
    public ChannelCopier(final int bufferSize) {
        this(bufferSize, -1);
    }

    /**
     * Constructs a {@code ChannelCopier} with the specified buffer size and total count of bytes to copy.
     *
     * @param bufferSize The size of the buffer to use for copying.
     * @param count      The total number of bytes to copy. If -1, copy until the end of the source.
     */
    public ChannelCopier(final int bufferSize, final long count) {
        this(bufferSize, count, null);
    }

    /**
     * Constructs a {@code ChannelCopier} with the specified buffer size, total count of bytes to copy, and a progress
     * listener.
     *
     * @param bufferSize The size of the buffer to use for copying.
     * @param count      The total number of bytes to copy. If -1, copy until the end of the source.
     * @param progress   The progress listener to report copy progress.
     */
    public ChannelCopier(final int bufferSize, final long count, final StreamProgress progress) {
        super(bufferSize, count, progress);
    }

    /**
     * Copies data from the source {@link ReadableByteChannel} to the target {@link WritableByteChannel}.
     *
     * @param source The source {@link ReadableByteChannel}.
     * @param target The target {@link WritableByteChannel}.
     * @return The total number of bytes copied.
     * @throws InternalException if an {@link IOException} occurs during the copy operation.
     */
    @Override
    public long copy(final ReadableByteChannel source, final WritableByteChannel target) {
        final StreamProgress progress = this.progress;
        if (null != progress) {
            progress.start();
        }
        final long size;
        try {
            size = doCopy(source, target, ByteBuffer.allocate(bufferSize(this.count)), progress);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null != progress) {
            progress.finish();
        }
        return size;
    }

    /**
     * Performs the actual copy operation. If a maximum length is specified, it reads up to that length; otherwise, it
     * reads until the end of the source (when -1 is encountered).
     *
     * @param source   The {@link ReadableByteChannel} to read from.
     * @param target   The {@link WritableByteChannel} to write to.
     * @param buffer   The {@link ByteBuffer} used for copying.
     * @param progress The progress listener to report copy progress, can be {@code null}.
     * @return The total number of bytes copied.
     * @throws IOException If an I/O error occurs during the copy.
     */
    private long doCopy(
            final ReadableByteChannel source,
            final WritableByteChannel target,
            final ByteBuffer buffer,
            final StreamProgress progress) throws IOException {
        long numToRead = this.count > 0 ? this.count : Long.MAX_VALUE;
        long total = 0;

        int read;
        while (numToRead > 0) {
            read = source.read(buffer);
            if (read < 0) {
                // Reached end of stream prematurely
                break;
            }
            buffer.flip();// Switch from writing to reading mode
            target.write(buffer);
            buffer.clear();

            numToRead -= read;
            total += read;
            if (null != progress) {
                // If total length is unknown, -1 indicates unknown
                progress.progress(this.count < Long.MAX_VALUE ? this.count : -1, total);
            }
        }

        return total;
    }

}
