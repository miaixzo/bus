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
package org.miaixz.bus.core.io.sink;

import java.io.IOException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.WritableByteChannel;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A {@link Sink} implementation that writes {@link Buffer} segments to a {@link WritableByteChannel}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ChannelSink implements Sink {

    /**
     * Number of segment views used for gathering writes.
     */
    private static final int GATHERING_VIEW_COUNT = 16;

    /**
     * Target writable channel.
     */
    private final WritableByteChannel channel;

    /**
     * Timeout applied before each write attempt.
     */
    private final Timeout timeout;

    /**
     * Reusable gathering write buffers.
     */
    private final java.nio.ByteBuffer[] buffers = new java.nio.ByteBuffer[GATHERING_VIEW_COUNT];

    /**
     * Creates a channel backed sink.
     *
     * @param channel the target channel
     * @param timeout the timeout information
     */
    public ChannelSink(WritableByteChannel channel, Timeout timeout) {
        if (channel == null) {
            throw new IllegalArgumentException("channel == null");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("timeout == null");
        }
        this.channel = channel;
        this.timeout = timeout;
    }

    /**
     * Writes bytes from {@code source} to the channel.
     *
     * @param source    the source buffer
     * @param byteCount the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        IoKit.checkOffsetAndCount(source.size, 0, byteCount);
        if (channel instanceof GatheringByteChannel gatheringChannel) {
            writeGathering(gatheringChannel, source, byteCount);
        } else {
            writeSingle(source, byteCount);
        }
    }

    /**
     * Flushes this sink. Channels have no generic flush operation.
     */
    @Override
    public void flush() {
        // No generic channel flush operation exists.
    }

    /**
     * Closes the channel.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        channel.close();
    }

    /**
     * Returns the timeout for this sink.
     *
     * @return the timeout
     */
    @Override
    public Timeout timeout() {
        return timeout;
    }

    /**
     * Returns a readable description of this sink.
     *
     * @return the sink description
     */
    @Override
    public String toString() {
        return "sink(" + channel + Symbol.PARENTHESE_RIGHT;
    }

    /**
     * Writes bytes with channel gathering support.
     *
     * @param gatheringChannel the gathering channel
     * @param source           the source buffer
     * @param byteCount        the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    private void writeGathering(GatheringByteChannel gatheringChannel, Buffer source, long byteCount)
            throws IOException {
        while (byteCount > 0) {
            timeout.throwIfReached();
            int count = source.nioBuffers(buffers, 0, buffers.length, byteCount);
            long written = gatheringChannel.write(buffers, 0, count);
            clearBuffers(count);
            if (written <= 0) {
                throw new IOException("channel write returned " + written);
            }
            source.skip(written);
            byteCount -= written;
        }
    }

    /**
     * Writes bytes with a single NIO view.
     *
     * @param source    the source buffer
     * @param byteCount the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    private void writeSingle(Buffer source, long byteCount) throws IOException {
        while (byteCount > 0) {
            timeout.throwIfReached();
            java.nio.ByteBuffer buffer = source.nioBuffer((int) Math.min(byteCount, Integer.MAX_VALUE));
            int written = channel.write(buffer);
            if (written <= 0) {
                throw new IOException("channel write returned " + written);
            }
            source.skip(written);
            byteCount -= written;
        }
    }

    /**
     * Clears retained NIO views to avoid keeping old segments reachable.
     *
     * @param count the number of entries to clear
     */
    private void clearBuffers(int count) {
        for (int i = 0; i < count; i++) {
            buffers[i] = null;
        }
    }

}
