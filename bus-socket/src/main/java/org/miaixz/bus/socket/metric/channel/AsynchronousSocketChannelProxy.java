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
 * A proxy class for {@link AsynchronousSocketChannel}.
 * <p>
 * This class delegates all calls to an underlying {@link AsynchronousSocketChannel} instance. It can be used to wrap
 * and potentially extend the functionality of an existing asynchronous socket channel.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AsynchronousSocketChannelProxy extends AsynchronousSocketChannel {

    /**
     * The underlying {@link AsynchronousSocketChannel} to which all operations are delegated.
     */
    protected final AsynchronousSocketChannel asynchronousSocketChannel;

    /**
     * Constructs a new {@code AsynchronousSocketChannelProxy}.
     *
     * @param asynchronousSocketChannel the underlying asynchronous socket channel to proxy
     */
    public AsynchronousSocketChannelProxy(AsynchronousSocketChannel asynchronousSocketChannel) {
        super(asynchronousSocketChannel.provider());
        this.asynchronousSocketChannel = asynchronousSocketChannel;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param local the local address to bind to
     * @return this channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
        return asynchronousSocketChannel.bind(local);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <T>   the type of the socket option value
     * @param name  the socket option
     * @param value the value of the socket option
     * @return this channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        return asynchronousSocketChannel.setOption(name, value);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <T>  the type of the socket option value
     * @param name the socket option
     * @return the value of the socket option
     * @throws IOException if an I/O error occurs
     */
    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return asynchronousSocketChannel.getOption(name);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return a set of the socket options supported by this channel
     */
    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return asynchronousSocketChannel.supportedOptions();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return this channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public AsynchronousSocketChannel shutdownInput() throws IOException {
        return asynchronousSocketChannel.shutdownInput();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return this channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public AsynchronousSocketChannel shutdownOutput() throws IOException {
        return asynchronousSocketChannel.shutdownOutput();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the remote address, or {@code null} if the channel is not connected
     * @throws IOException if an I/O error occurs
     */
    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return asynchronousSocketChannel.getRemoteAddress();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <A>        the type of the attachment
     * @param remote     the remote address to which this channel is to be connected
     * @param attachment the object to attach to the I/O operation
     * @param handler    the handler for consuming the result
     */
    @Override
    public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        asynchronousSocketChannel.connect(remote, attachment, handler);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param remote the remote address to which this channel is to be connected
     * @return a {@link Future} representing the pending result
     */
    @Override
    public Future<Void> connect(SocketAddress remote) {
        return asynchronousSocketChannel.connect(remote);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <A>        the type of the attachment
     * @param dst        the buffer into which bytes are to be transferred
     * @param timeout    the maximum time for the I/O operation to complete
     * @param unit       the time unit of the timeout argument
     * @param attachment the object to attach to the I/O operation
     * @param handler    the handler for consuming the result
     */
    @Override
    public <A> void read(
            ByteBuffer dst,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        asynchronousSocketChannel.read(dst, timeout, unit, attachment, handler);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param dst the buffer into which bytes are to be transferred
     * @return a {@link Future} representing the pending result
     */
    @Override
    public Future<Integer> read(ByteBuffer dst) {
        return asynchronousSocketChannel.read(dst);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <A>        the type of the attachment
     * @param dsts       the buffers into which bytes are to be transferred
     * @param offset     the offset within the buffer array of the first buffer
     * @param length     the maximum number of buffers to be accessed
     * @param timeout    the maximum time for the I/O operation to complete
     * @param unit       the time unit of the timeout argument
     * @param attachment the object to attach to the I/O operation
     * @param handler    the handler for consuming the result
     */
    @Override
    public <A> void read(
            ByteBuffer[] dsts,
            int offset,
            int length,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Long, ? super A> handler) {
        asynchronousSocketChannel.read(dsts, offset, length, timeout, unit, attachment, handler);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <A>        the type of the attachment
     * @param src        the buffer from which bytes are to be retrieved
     * @param timeout    the maximum time for the I/O operation to complete
     * @param unit       the time unit of the timeout argument
     * @param attachment the object to attach to the I/O operation
     * @param handler    the handler for consuming the result
     */
    @Override
    public <A> void write(
            ByteBuffer src,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        asynchronousSocketChannel.write(src, timeout, unit, attachment, handler);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param src the buffer from which bytes are to be retrieved
     * @return a {@link Future} representing the pending result
     */
    @Override
    public Future<Integer> write(ByteBuffer src) {
        return asynchronousSocketChannel.write(src);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <A>        the type of the attachment
     * @param srcs       the buffers from which bytes are to be retrieved
     * @param offset     the offset within the buffer array of the first buffer
     * @param length     the maximum number of buffers to be accessed
     * @param timeout    the maximum time for the I/O operation to complete
     * @param unit       the time unit of the timeout argument
     * @param attachment the object to attach to the I/O operation
     * @param handler    the handler for consuming the result
     */
    @Override
    public <A> void write(
            ByteBuffer[] srcs,
            int offset,
            int length,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Long, ? super A> handler) {
        asynchronousSocketChannel.write(srcs, offset, length, timeout, unit, attachment, handler);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the local address, or {@code null} if the channel is not bound
     * @throws IOException if an I/O error occurs
     */
    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return asynchronousSocketChannel.getLocalAddress();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return {@code true} if, and only if, this channel is open
     */
    @Override
    public boolean isOpen() {
        return asynchronousSocketChannel.isOpen();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        asynchronousSocketChannel.close();
    }

}
