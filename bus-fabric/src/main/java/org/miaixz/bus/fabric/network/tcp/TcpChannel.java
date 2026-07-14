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
package org.miaixz.bus.fabric.network.tcp;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.network.aio.AioChannel;

/**
 * TCP channel wrapper over an AIO channel.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class TcpChannel {

    /**
     * Wrapped AIO channel.
     */
    private final AioChannel channel;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Creates a TCP channel.
     *
     * @param channel AIO channel
     */
    private TcpChannel(final AioChannel channel) {
        this.channel = Assert.notNull(channel, () -> new ValidateException("AIO channel must not be null"));
        this.state = new AtomicReference<>(Status.OPENED);
    }

    /**
     * Wraps an AIO channel.
     *
     * @param channel AIO channel
     * @return TCP channel
     */
    static TcpChannel wrap(final AioChannel channel) {
        return new TcpChannel(channel);
    }

    /**
     * Reads bytes.
     *
     * @param target target buffer
     * @return read future
     */
    CompletableFuture<Integer> read(final ByteBuffer target) {
        return channel.read(target);
    }

    /**
     * Writes bytes.
     *
     * @param source source buffer
     * @return write future
     */
    CompletableFuture<Integer> write(final ByteBuffer source) {
        return channel.write(source);
    }

    /**
     * Returns whether this channel is opened.
     *
     * @return true when opened
     */
    boolean opened() {
        return state.get() == Status.OPENED;
    }

    /**
     * Closes this channel.
     */
    void close() {
        if (state.getAndSet(Status.CLOSED) != Status.CLOSED) {
            channel.close();
        }
    }

    /**
     * Returns lifecycle state.
     *
     * @return state
     */
    Status state() {
        return state.get();
    }

}
