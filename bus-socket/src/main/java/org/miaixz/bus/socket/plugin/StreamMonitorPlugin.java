/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.plugin;

import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.metric.channel.AsynchronousSocketChannelProxy;
import org.miaixz.bus.socket.metric.channel.UnsupportedAsynchronousSocketChannel;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * A plugin for monitoring the byte stream at the transport layer.
 * <p>
 * This plugin intercepts read and write operations on {@link AsynchronousSocketChannel}s and logs the transmitted data,
 * providing visibility into the raw communication stream.
 * </p>
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamMonitorPlugin<T> extends AbstractPlugin<T> {

    /**
     * A {@link BiConsumer} that logs incoming byte streams in blue hexadecimal format. It displays the timestamp,
     * remote and local addresses, and the byte content.
     */
    public static final BiConsumer<AsynchronousSocketChannel, byte[]> BLUE_HEX_INPUT_STREAM = (channel, bytes) -> {
        try {
            Logger.info(
                    ConsoleColors.BLUE + Formatter.NORM_DATETIME_MS_FORMAT.format(new Date()) + " [ "
                            + channel.getRemoteAddress() + " --> " + channel.getLocalAddress() + " ] [ read: "
                            + bytes.length + " bytes ]" + ByteKit.byteArrayToHexString(bytes) + ConsoleColors.RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };
    /**
     * A {@link BiConsumer} that logs outgoing byte streams in red hexadecimal format. It displays the timestamp, local
     * and remote addresses, and the byte content.
     */
    public static final BiConsumer<AsynchronousSocketChannel, byte[]> RED_HEX_OUTPUT_STREAM = (channel, bytes) -> {
        try {
            Logger.info(
                    ConsoleColors.RED + Formatter.NORM_DATETIME_MS_FORMAT.format(new Date()) + " [ "
                            + channel.getLocalAddress() + " --> " + channel.getRemoteAddress() + " ] [ write: "
                            + bytes.length + " bytes ]" + ByteKit.byteArrayToHexString(bytes) + ConsoleColors.RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    /**
     * A {@link BiConsumer} that logs incoming byte streams in blue text format. It displays the timestamp, remote and
     * local addresses, and the byte content as a string.
     */
    public static final BiConsumer<AsynchronousSocketChannel, byte[]> BLUE_TEXT_INPUT_STREAM = (channel, bytes) -> {
        try {
            Logger.info(
                    ConsoleColors.BLUE + Formatter.NORM_DATETIME_MS_FORMAT.format(new Date()) + " [ "
                            + channel.getRemoteAddress() + " --> " + channel.getLocalAddress() + " ] [ read: "
                            + bytes.length + " bytes ]\r\n" + new String(bytes) + ConsoleColors.RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };
    /**
     * A {@link BiConsumer} that logs outgoing byte streams in red text format. It displays the timestamp, local and
     * remote addresses, and the byte content as a string.
     */
    public static final BiConsumer<AsynchronousSocketChannel, byte[]> RED_TEXT_OUTPUT_STREAM = (channel, bytes) -> {
        try {
            Logger.info(
                    ConsoleColors.RED + Formatter.NORM_DATETIME_MS_FORMAT.format(new Date()) + " [ "
                            + channel.getLocalAddress() + " --> " + channel.getRemoteAddress() + " ] [ write: "
                            + bytes.length + " bytes ]\r\n" + new String(bytes) + ConsoleColors.RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };
    /**
     * The consumer for processing and logging incoming byte streams.
     */
    private final BiConsumer<AsynchronousSocketChannel, byte[]> inputStreamConsumer;
    /**
     * The consumer for processing and logging outgoing byte streams.
     */
    private final BiConsumer<AsynchronousSocketChannel, byte[]> outputStreamConsumer;

    /**
     * Constructs a {@code StreamMonitorPlugin} with default hexadecimal stream consumers.
     */
    public StreamMonitorPlugin() {
        this(BLUE_HEX_INPUT_STREAM, RED_HEX_OUTPUT_STREAM);
    }

    /**
     * Constructs a {@code StreamMonitorPlugin} with custom input and output stream consumers.
     *
     * @param inputStreamConsumer  the consumer for incoming byte streams
     * @param outputStreamConsumer the consumer for outgoing byte streams
     */
    public StreamMonitorPlugin(BiConsumer<AsynchronousSocketChannel, byte[]> inputStreamConsumer,
            BiConsumer<AsynchronousSocketChannel, byte[]> outputStreamConsumer) {
        this.inputStreamConsumer = Objects.requireNonNull(inputStreamConsumer);
        this.outputStreamConsumer = Objects.requireNonNull(outputStreamConsumer);
    }

    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return new StreamMonitorAsynchronousSocketChannel(channel);
    }

    /**
     * A {@link CompletionHandler} wrapper that logs the read/written data before delegating to the original handler.
     *
     * @param <A> the type of the attachment object
     */
    static class MonitorCompletionHandler<A> implements CompletionHandler<Integer, A> {

        /**
         * The original completion handler to delegate to.
         */
        CompletionHandler<Integer, A> handler;
        /**
         * The consumer to log the byte stream.
         */
        BiConsumer<AsynchronousSocketChannel, byte[]> consumer;
        /**
         * The buffer containing the data.
         */
        ByteBuffer buffer;
        /**
         * The asynchronous socket channel being monitored.
         */
        AsynchronousSocketChannel channel;

        /**
         * Constructs a {@code MonitorCompletionHandler}.
         *
         * @param channel  the asynchronous socket channel
         * @param handler  the original completion handler
         * @param consumer the consumer for logging the stream
         * @param buffer   the ByteBuffer involved in the operation
         */
        public MonitorCompletionHandler(AsynchronousSocketChannel channel, CompletionHandler<Integer, A> handler,
                BiConsumer<AsynchronousSocketChannel, byte[]> consumer, ByteBuffer buffer) {
            this.channel = new UnsupportedAsynchronousSocketChannel(channel) {

                @Override
                public SocketAddress getRemoteAddress() throws IOException {
                    return channel.getRemoteAddress();
                }

                @Override
                public SocketAddress getLocalAddress() throws IOException {
                    return channel.getLocalAddress();
                }
            };
            this.handler = handler;
            this.consumer = consumer;
            this.buffer = buffer;
        }

        @Override
        public void completed(Integer result, A attachment) {
            if (result > 0) {
                byte[] bytes = new byte[result];
                buffer.position(buffer.position() - result);
                buffer.get(bytes);
                consumer.accept(channel, bytes);
            }
            handler.completed(result, attachment);
        }

        @Override
        public void failed(Throwable exc, A attachment) {
            handler.failed(exc, attachment);
        }
    }

    /**
     * Utility class for console color codes.
     */
    static class ConsoleColors {

        /**
         * ANSI escape code to reset console colors.
         */
        public static final String RESET = "\033[0m";
        /**
         * ANSI escape code for blue text.
         */
        public static final String BLUE = "\033[34m";

        /**
         * ANSI escape code for red text.
         */
        public static final String RED = "\033[31m";

    }

    /**
     * An internal {@link AsynchronousSocketChannelProxy} that intercepts read and write operations to log the byte
     * streams using the configured consumers.
     */
    class StreamMonitorAsynchronousSocketChannel extends AsynchronousSocketChannelProxy {

        /**
         * Constructs a {@code StreamMonitorAsynchronousSocketChannel}.
         *
         * @param asynchronousSocketChannel the underlying {@link AsynchronousSocketChannel} to proxy
         */
        public StreamMonitorAsynchronousSocketChannel(AsynchronousSocketChannel asynchronousSocketChannel) {
            super(asynchronousSocketChannel);
        }

        @Override
        public <A> void read(
                ByteBuffer dst,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            super.read(
                    dst,
                    timeout,
                    unit,
                    attachment,
                    new MonitorCompletionHandler<>(this, handler, inputStreamConsumer, dst));
        }

        @Override
        public <A> void write(
                ByteBuffer src,
                long timeout,
                TimeUnit unit,
                A attachment,
                CompletionHandler<Integer, ? super A> handler) {
            super.write(
                    src,
                    timeout,
                    unit,
                    attachment,
                    new MonitorCompletionHandler<>(this, handler, outputStreamConsumer, src));
        }
    }

}
