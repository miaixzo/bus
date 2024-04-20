/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.socket.plugins;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.channel.AsynchronousSocketChannelProxy;
import org.miaixz.bus.socket.channel.UnsupportedAsynchronousSocketChannel;

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
 * 传输层码流监控插件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamMonitorPlugin<T> extends AbstractPlugin<T> {

    public static final BiConsumer<AsynchronousSocketChannel, byte[]> BLUE_HEX_INPUT_STREAM = (channel, bytes) -> {
        try {
            Logger.info(ConsoleColors.BLUE + Fields.NORM_DATETIME_MS_FORMAT.format(new Date()) + " [ " + channel.getRemoteAddress() + " --> " + channel.getLocalAddress() + " ] [ read: " + bytes.length + " bytes ]" + StringKit.byteArrayToHex(bytes) + ConsoleColors.RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };
    public static final BiConsumer<AsynchronousSocketChannel, byte[]> RED_HEX_OUTPUT_STREAM = (channel, bytes) -> {
        try {
            Logger.info(ConsoleColors.RED + Fields.NORM_DATETIME_MS_FORMAT.format(new Date()) + " [ " + channel.getLocalAddress() + " --> " + channel.getRemoteAddress() + " ] [ write: " + bytes.length + " bytes ]" + StringKit.byteArrayToHex(bytes) + ConsoleColors.RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public static final BiConsumer<AsynchronousSocketChannel, byte[]> BLUE_TEXT_INPUT_STREAM = (channel, bytes) -> {
        try {
            Logger.info(ConsoleColors.BLUE + Fields.NORM_DATETIME_MS_FORMAT.format(new Date()) + " [ " + channel.getRemoteAddress() + " --> " + channel.getLocalAddress() + " ] [ read: " + bytes.length + " bytes ]\r\n" + new String(bytes) + ConsoleColors.RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };
    public static final BiConsumer<AsynchronousSocketChannel, byte[]> RED_TEXT_OUTPUT_STREAM = (channel, bytes) -> {
        try {
            Logger.info(ConsoleColors.RED + Fields.NORM_DATETIME_MS_FORMAT.format(new Date()) + " [ " + channel.getLocalAddress() + " --> " + channel.getRemoteAddress() + " ] [ write: " + bytes.length + " bytes ]\r\n" + new String(bytes) + ConsoleColors.RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };
    private final BiConsumer<AsynchronousSocketChannel, byte[]> inputStreamConsumer;
    private final BiConsumer<AsynchronousSocketChannel, byte[]> outputStreamConsumer;

    public StreamMonitorPlugin() {
        this(BLUE_HEX_INPUT_STREAM, RED_HEX_OUTPUT_STREAM);
    }

    public StreamMonitorPlugin(BiConsumer<AsynchronousSocketChannel, byte[]> inputStreamConsumer, BiConsumer<AsynchronousSocketChannel, byte[]> outputStreamConsumer) {
        this.inputStreamConsumer = Objects.requireNonNull(inputStreamConsumer);
        this.outputStreamConsumer = Objects.requireNonNull(outputStreamConsumer);
    }

    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        return new StreamMonitorAsynchronousSocketChannel(channel);
    }

    static class MonitorCompletionHandler<A> implements CompletionHandler<Integer, A> {
        CompletionHandler<Integer, A> handler;
        BiConsumer<AsynchronousSocketChannel, byte[]> consumer;
        ByteBuffer buffer;
        AsynchronousSocketChannel channel;

        public MonitorCompletionHandler(AsynchronousSocketChannel channel, CompletionHandler<Integer, A> handler, BiConsumer<AsynchronousSocketChannel, byte[]> consumer, ByteBuffer buffer) {
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

    static class ConsoleColors {
        /**
         * 重置颜色
         */
        public static final String RESET = "\033[0m";
        /**
         * 蓝色
         */
        public static final String BLUE = "\033[34m";

        /**
         * 红色
         */
        public static final String RED = "\033[31m";

    }

    class StreamMonitorAsynchronousSocketChannel extends AsynchronousSocketChannelProxy {

        public StreamMonitorAsynchronousSocketChannel(AsynchronousSocketChannel asynchronousSocketChannel) {
            super(asynchronousSocketChannel);
        }

        @Override
        public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
            super.read(dst, timeout, unit, attachment, new MonitorCompletionHandler<>(this, handler, inputStreamConsumer, dst));
        }

        @Override
        public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
            super.write(src, timeout, unit, attachment, new MonitorCompletionHandler<>(this, handler, outputStreamConsumer, src));
        }
    }

}