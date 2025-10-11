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
package org.miaixz.bus.socket.metric.channel;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * An {@link AsynchronousSocketChannel} implementation that throws {@link UnsupportedOperationException} for all its
 * methods. This class is intended to be used as a placeholder or a base for channels that do not support certain
 * asynchronous operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UnsupportedAsynchronousSocketChannel extends AsynchronousSocketChannel {

    /**
     * Constructs an {@code UnsupportedAsynchronousSocketChannel}.
     *
     * @param asynchronousSocketChannel an existing {@link AsynchronousSocketChannel} whose provider will be used.
     */
    public UnsupportedAsynchronousSocketChannel(AsynchronousSocketChannel asynchronousSocketChannel) {
        super(asynchronousSocketChannel.provider());
    }

    @Override
    public AsynchronousSocketChannel bind(SocketAddress local) {
        throw new UnsupportedOperationException("bind operation is not supported");
    }

    @Override
    public <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) {
        throw new UnsupportedOperationException("setOption operation is not supported");
    }

    @Override
    public <T> T getOption(SocketOption<T> name) {
        throw new UnsupportedOperationException("getOption operation is not supported");
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        throw new UnsupportedOperationException("supportedOptions operation is not supported");
    }

    @Override
    public AsynchronousSocketChannel shutdownInput() {
        throw new UnsupportedOperationException("shutdownInput operation is not supported");
    }

    @Override
    public AsynchronousSocketChannel shutdownOutput() {
        throw new UnsupportedOperationException("shutdownOutput operation is not supported");
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        throw new UnsupportedOperationException("getRemoteAddress operation is not supported");
    }

    @Override
    public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        throw new UnsupportedOperationException("connect operation is not supported");
    }

    @Override
    public Future<Void> connect(SocketAddress remote) {
        throw new UnsupportedOperationException("connect operation is not supported");
    }

    @Override
    public <A> void read(
            ByteBuffer dst,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        throw new UnsupportedOperationException("read operation is not supported");
    }

    @Override
    public Future<Integer> read(ByteBuffer dst) {
        throw new UnsupportedOperationException("read operation is not supported");
    }

    @Override
    public <A> void read(
            ByteBuffer[] dsts,
            int offset,
            int length,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException("scattering read operation is not supported");
    }

    @Override
    public <A> void write(
            ByteBuffer src,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        throw new UnsupportedOperationException("write operation is not supported");
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        throw new UnsupportedOperationException("write operation is not supported");
    }

    @Override
    public <A> void write(
            ByteBuffer[] srcs,
            int offset,
            int length,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException("gathering write operation is not supported");
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        throw new UnsupportedOperationException("getLocalAddress operation is not supported");
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("isOpen operation is not supported");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("close operation is not supported");
    }

}
