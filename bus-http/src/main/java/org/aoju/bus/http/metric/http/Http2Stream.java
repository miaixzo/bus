/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.io.*;
import org.aoju.bus.http.Builder;
import org.aoju.bus.http.Headers;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 逻辑双向流.
 *
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
public final class Http2Stream {

    final int id;
    /**
     * 异常终止此流。这将阻塞，直到{@code RST_STREAM}帧被传输
     */
    final Http2Connection connection;
    /**
     * 返回可用于向对等方写入数据的接收器
     */
    final FramingSink sink;
    final StreamTimeout readTimeout = new StreamTimeout();
    final StreamTimeout writeTimeout = new StreamTimeout();
    /**
     * 接收到的头信息尚未被{@linkplain #takeHeaders taken}或{@linkplain FramingSource#read read}.
     */
    private final Deque<Headers> headersQueue = new ArrayDeque<>();
    /**
     * 对等节点读取数据的源
     */
    private final FramingSource source;
    /**
     * 应用程序消耗的总字节数(使用{@link FramingSource#read})，但尚未通过在此流上发送{@code WINDOW_UPDATE}确认.
     */
    long unacknowledgedBytesRead = 0;
    /**
     * 在接收窗口更新之前可以写入流的字节数。即使这是正的，写操作也会阻塞，
     * 直到{@code connection.bytesLeftInWriteWindow}中有可用字节为止
     */
    long bytesLeftInWriteWindow;
    /**
     * 这条小溪非正常关闭的原因。如果有多个原因导致异常关闭这个流(例如两个对等点几乎同时关闭它)，
     * 那么这就是这个对等点知道的第一个原因.
     */
    ErrorCode errorCode = null;
    private HttpHeaders.Listener headersListener;
    /**
     * 如果已发送或接收响应头，则为
     */
    private boolean hasResponseHeaders;

    Http2Stream(int id, Http2Connection connection, boolean outFinished, boolean inFinished,
                Headers headers) {
        if (connection == null) throw new NullPointerException("connection == null");

        this.id = id;
        this.connection = connection;
        this.bytesLeftInWriteWindow =
                connection.peerSettings.getInitialWindowSize();
        this.source = new FramingSource(connection.settings.getInitialWindowSize());
        this.sink = new FramingSink();
        this.source.finished = inFinished;
        this.sink.finished = outFinished;
        if (headers != null) {
            headersQueue.add(headers);
        }

        if (isLocallyInitiated() && headers != null) {
            throw new IllegalStateException("locally-initiated streams shouldn't have headers yet");
        } else if (!isLocallyInitiated() && headers == null) {
            throw new IllegalStateException("remotely-initiated streams should have headers");
        }
    }

    public int getId() {
        return id;
    }

    public synchronized boolean isOpen() {
        if (errorCode != null) {
            return false;
        }
        if ((source.finished || source.closed)
                && (sink.finished || sink.closed)
                && hasResponseHeaders) {
            return false;
        }
        return true;
    }

    public boolean isLocallyInitiated() {
        boolean streamIsClient = ((id & 1) == 1);
        return connection.client == streamIsClient;
    }

    public Http2Connection getConnection() {
        return connection;
    }

    public synchronized Headers takeHeaders() throws IOException {
        readTimeout.enter();
        try {
            while (headersQueue.isEmpty() && errorCode == null) {
                waitForIo();
            }
        } finally {
            readTimeout.exitAndThrowIfTimedOut();
        }
        if (!headersQueue.isEmpty()) {
            return headersQueue.removeFirst();
        }
        throw new StreamException(errorCode);
    }

    public synchronized ErrorCode getErrorCode() {
        return errorCode;
    }

    public void writeHeaders(List<HttpHeaders> responseHeaders, boolean out) throws IOException {
        assert (!Thread.holdsLock(Http2Stream.this));
        if (responseHeaders == null) {
            throw new NullPointerException("headers == null");
        }
        boolean outFinished = false;
        boolean flushHeaders = false;
        synchronized (this) {
            this.hasResponseHeaders = true;
            if (!out) {
                this.sink.finished = true;
                flushHeaders = true;
                outFinished = true;
            }
        }

        if (!flushHeaders) {
            synchronized (connection) {
                flushHeaders = connection.bytesLeftInWriteWindow == 0L;
            }
        }

        connection.writeSynReply(id, outFinished, responseHeaders);

        if (flushHeaders) {
            connection.flush();
        }
    }

    public Timeout readTimeout() {
        return readTimeout;
    }

    public Timeout writeTimeout() {
        return writeTimeout;
    }

    public Source getSource() {
        return source;
    }

    public Sink getSink() {
        synchronized (this) {
            if (!hasResponseHeaders && !isLocallyInitiated()) {
                throw new IllegalStateException("reply before requesting the sink");
            }
        }
        return sink;
    }

    public void close(ErrorCode rstStatusCode) throws IOException {
        if (!closeInternal(rstStatusCode)) {
            return; // Already closed.
        }
        connection.writeSynReset(id, rstStatusCode);
    }

    public void closeLater(ErrorCode errorCode) {
        if (!closeInternal(errorCode)) {
            return; // Already closed.
        }
        connection.writeSynResetLater(id, errorCode);
    }

    private boolean closeInternal(ErrorCode errorCode) {
        assert (!Thread.holdsLock(this));
        synchronized (this) {
            if (this.errorCode != null) {
                return false;
            }
            if (source.finished && sink.finished) {
                return false;
            }
            this.errorCode = errorCode;
            notifyAll();
        }
        connection.removeStream(id);
        return true;
    }

    void receiveHeaders(List<HttpHeaders> headers) {
        assert (!Thread.holdsLock(Http2Stream.this));
        boolean open;
        synchronized (this) {
            hasResponseHeaders = true;
            headersQueue.add(Builder.toHeaders(headers));
            open = isOpen();
            notifyAll();
        }
        if (!open) {
            connection.removeStream(id);
        }
    }

    void receiveData(BufferSource in, int length) throws IOException {
        assert (!Thread.holdsLock(Http2Stream.this));
        this.source.receive(in, length);
    }

    void receiveFin() {
        assert (!Thread.holdsLock(Http2Stream.this));
        boolean open;
        synchronized (this) {
            this.source.finished = true;
            open = isOpen();
            notifyAll();
        }
        if (!open) {
            connection.removeStream(id);
        }
    }

    synchronized void receiveRstStream(ErrorCode errorCode) {
        if (this.errorCode == null) {
            this.errorCode = errorCode;
            notifyAll();
        }
    }

    public synchronized void setHeadersListener(HttpHeaders.Listener headersListener) {
        this.headersListener = headersListener;
        if (!headersQueue.isEmpty() && headersListener != null) {
            notifyAll(); // We now have somewhere to deliver headers!
        }
    }

    void cancelStreamIfNecessary() throws IOException {
        assert (!Thread.holdsLock(Http2Stream.this));
        boolean open;
        boolean cancel;
        synchronized (this) {
            cancel = !source.finished && source.closed && (sink.finished || sink.closed);
            open = isOpen();
        }
        if (cancel) {
            Http2Stream.this.close(ErrorCode.CANCEL);
        } else if (!open) {
            connection.removeStream(id);
        }
    }

    void addBytesToWriteWindow(long delta) {
        bytesLeftInWriteWindow += delta;
        if (delta > 0) Http2Stream.this.notifyAll();
    }

    void checkOutNotClosed() throws IOException {
        if (sink.closed) {
            throw new IOException("stream closed");
        } else if (sink.finished) {
            throw new IOException("stream finished");
        } else if (errorCode != null) {
            throw new StreamException(errorCode);
        }
    }

    void waitForIo() throws InterruptedIOException {
        try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();
        }
    }

    /**
     * 读取流的传入数据帧的源。虽然这个类使用同步来安全地接收传入的数据帧，但它并不打算供多个读取器使用.
     */
    private final class FramingSource implements Source {
        /**
         * 缓冲区接收来自网络的数据。仅由读线程访问.
         */
        private final Buffer receiveBuffer = new Buffer();

        /**
         * 具有可读数据的缓冲区。有Http2Stream.this
         */
        private final Buffer readBuffer = new Buffer();

        /**
         * 在报告流控制错误之前要缓冲的最大字节数。
         */
        private final long maxByteCount;

        /**
         * 如果调用者已关闭此流，则为真.
         */
        boolean closed;

        /**
         * 如果任何一方干净地关闭了这条河，那就是正确的。
         * 除了已经在缓冲区中的字节外，我们不会收到更多的字节.
         */
        boolean finished;

        FramingSource(long maxByteCount) {
            this.maxByteCount = maxByteCount;
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);

            while (true) {
                Headers headersToDeliver = null;
                HttpHeaders.Listener headersListenerToNotify = null;
                long readBytesDelivered = -1;
                ErrorCode errorCodeToDeliver = null;

                synchronized (Http2Stream.this) {
                    readTimeout.enter();
                    try {
                        if (errorCode != null) {
                            errorCodeToDeliver = errorCode;
                        }

                        if (closed) {
                            throw new IOException("stream closed");

                        } else if (!headersQueue.isEmpty() && headersListener != null) {
                            headersToDeliver = headersQueue.removeFirst();
                            headersListenerToNotify = headersListener;

                        } else if (readBuffer.size() > 0) {
                            readBytesDelivered = readBuffer.read(sink, Math.min(byteCount, readBuffer.size()));
                            unacknowledgedBytesRead += readBytesDelivered;

                            if (errorCodeToDeliver == null
                                    && unacknowledgedBytesRead
                                    >= connection.settings.getInitialWindowSize() / 2) {
                                connection.writeWindowUpdateLater(id, unacknowledgedBytesRead);
                                unacknowledgedBytesRead = 0;
                            }
                        } else if (!finished && errorCodeToDeliver == null) {
                            waitForIo();
                            continue;
                        }
                    } finally {
                        readTimeout.exitAndThrowIfTimedOut();
                    }
                }

                if (headersToDeliver != null && headersListenerToNotify != null) {
                    headersListenerToNotify.onHeaders(headersToDeliver);
                    continue;
                }

                if (readBytesDelivered != -1) {
                    updateConnectionFlowControl(readBytesDelivered);
                    return readBytesDelivered;
                }

                if (errorCodeToDeliver != null) {
                    throw new StreamException(errorCodeToDeliver);
                }

                return -1;
            }
        }

        private void updateConnectionFlowControl(long read) {
            assert (!Thread.holdsLock(Http2Stream.this));
            connection.updateConnectionFlowControl(read);
        }

        void receive(BufferSource in, long byteCount) throws IOException {
            assert (!Thread.holdsLock(Http2Stream.this));

            while (byteCount > 0) {
                boolean finished;
                boolean flowControlError;
                synchronized (Http2Stream.this) {
                    finished = this.finished;
                    flowControlError = byteCount + readBuffer.size() > maxByteCount;
                }

                // 如果对方发送的数据超出了我们的处理能力，则丢弃它并关闭连接.
                if (flowControlError) {
                    in.skip(byteCount);
                    closeLater(ErrorCode.FLOW_CONTROL_ERROR);
                    return;
                }

                // 在流完成后丢弃接收到的数据。这可能是一场良性竞争.
                if (finished) {
                    in.skip(byteCount);
                    return;
                }

                // 填充接收缓冲区而不持有任何锁.
                long read = in.read(receiveBuffer, byteCount);
                if (read == -1) throw new EOFException();
                byteCount -= read;

                long bytesDiscarded = 0L;
                synchronized (Http2Stream.this) {
                    if (closed) {
                        bytesDiscarded = receiveBuffer.size();
                        receiveBuffer.clear();
                    } else {
                        boolean wasEmpty = readBuffer.size() == 0;
                        readBuffer.writeAll(receiveBuffer);
                        if (wasEmpty) {
                            Http2Stream.this.notifyAll();
                        }
                    }
                }
                if (bytesDiscarded > 0L) {
                    updateConnectionFlowControl(bytesDiscarded);
                }
            }
        }

        @Override
        public Timeout timeout() {
            return readTimeout;
        }

        @Override
        public void close() throws IOException {
            long bytesDiscarded;
            List<Headers> headersToDeliver = null;
            HttpHeaders.Listener headersListenerToNotify = null;
            synchronized (Http2Stream.this) {
                closed = true;
                bytesDiscarded = readBuffer.size();
                readBuffer.clear();
                if (!headersQueue.isEmpty() && headersListener != null) {
                    headersToDeliver = new ArrayList<>(headersQueue);
                    headersQueue.clear();
                    headersListenerToNotify = headersListener;
                }
                Http2Stream.this.notifyAll();
            }
            if (bytesDiscarded > 0) {
                updateConnectionFlowControl(bytesDiscarded);
            }
            cancelStreamIfNecessary();
            if (headersListenerToNotify != null) {
                for (Headers headers : headersToDeliver) {
                    headersListenerToNotify.onHeaders(headers);
                }
            }
        }
    }

    /**
     * 一种将流出的数据帧写入流的接收器。这个类不是线程安全的.
     */
    final class FramingSink implements Sink {
        private static final long EMIT_BUFFER_SIZE = 16384;

        /**
         * 输出数据的缓冲区。此批处理将小的写操作作为大的写帧写入到传出连接中。批量处理节省了(小的)帧开销.
         */
        private final Buffer sendBuffer = new Buffer();

        boolean closed;

        /**
         * 如果任何一方干净地关闭了这条河，那就是正确的。我们将不再发送字节.
         */
        boolean finished;

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            assert (!Thread.holdsLock(Http2Stream.this));
            sendBuffer.write(source, byteCount);
            while (sendBuffer.size() >= EMIT_BUFFER_SIZE) {
                emitFrame(false);
            }
        }

        private void emitFrame(boolean outFinished) throws IOException {
            long toWrite;
            synchronized (Http2Stream.this) {
                writeTimeout.enter();
                try {
                    while (bytesLeftInWriteWindow <= 0 && !finished && !closed && errorCode == null) {
                        waitForIo();
                    }
                } finally {
                    writeTimeout.exitAndThrowIfTimedOut();
                }

                checkOutNotClosed();
                toWrite = Math.min(bytesLeftInWriteWindow, sendBuffer.size());
                bytesLeftInWriteWindow -= toWrite;
            }

            writeTimeout.enter();
            try {
                connection.writeData(id, outFinished && toWrite == sendBuffer.size(), sendBuffer, toWrite);
            } finally {
                writeTimeout.exitAndThrowIfTimedOut();
            }
        }

        @Override
        public void flush() throws IOException {
            assert (!Thread.holdsLock(Http2Stream.this));
            synchronized (Http2Stream.this) {
                checkOutNotClosed();
            }
            while (sendBuffer.size() > 0) {
                emitFrame(false);
                connection.flush();
            }
        }

        @Override
        public Timeout timeout() {
            return writeTimeout;
        }

        @Override
        public void close() throws IOException {
            assert (!Thread.holdsLock(Http2Stream.this));
            synchronized (Http2Stream.this) {
                if (closed) return;
            }
            if (!sink.finished) {
                if (sendBuffer.size() > 0) {
                    while (sendBuffer.size() > 0) {
                        emitFrame(true);
                    }
                } else {
                    connection.writeData(id, true, null, 0);
                }
            }
            synchronized (Http2Stream.this) {
                closed = true;
            }
            connection.flush();
            cancelStreamIfNecessary();
        }
    }

    /**
     * 如果超时到达，Okio超时监视器将调用{@link #timedOut}。
     * 在这种情况下，我们关闭(异步)流，它将通知正在等待的线程.
     */
    class StreamTimeout extends AsyncTimeout {
        @Override
        protected void timedOut() {
            closeLater(ErrorCode.CANCEL);
        }

        @Override
        protected IOException newTimeoutException(IOException cause) {
            SocketTimeoutException socketTimeoutException = new SocketTimeoutException("timeout");
            if (cause != null) {
                socketTimeoutException.initCause(cause);
            }
            return socketTimeoutException;
        }

        public void exitAndThrowIfTimedOut() throws IOException {
            if (exit()) throw newTimeoutException(null);
        }
    }

}
