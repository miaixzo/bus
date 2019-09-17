/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.internal.http.second;

import org.aoju.bus.core.io.*;
import org.aoju.bus.http.Headers;
import org.aoju.bus.http.internal.Internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A logical bidirectional stream.
 *
 * @author Kimi Liu
 * @version 3.5.0
 * @since JDK 1.8
 */
public final class Http2Stream {

    // Internal state is guarded by this. No long-running or potentially
    // blocking operations are performed while the lock is held.

    final int id;
    final Http2Connection connection;
    final FramingSink sink;
    final StreamTimeout readTimeout = new StreamTimeout();
    final StreamTimeout writeTimeout = new StreamTimeout();
    /**
     * Received headers yet to be {@linkplain #takeHeaders taken}, or {@linkplain FramingSource#read
     * read}.
     */
    private final Deque<Headers> headersQueue = new ArrayDeque<>();
    private final FramingSource source;
    /**
     * The total number of bytes consumed by the application (with {@link FramingSource#read}), but
     * not yet acknowledged by sending a {@code WINDOW_UPDATE} frame on this stream.
     */
    // Visible for testing
    long unacknowledgedBytesRead = 0;
    /**
     * Count of bytes that can be written on the stream before receiving a window update. Even if this
     * is positive, writes will block until there available bytes in {@code
     * connection.bytesLeftInWriteWindow}.
     */
    // guarded by this
    long bytesLeftInWriteWindow;
    /**
     * The reason why this stream was abnormally closed. If there are multiple reasons to abnormally
     * close this stream (such as both peers closing it near-simultaneously) then this is the first
     * reason known to this peer.
     */
    ErrorCode errorCode = null;
    private Header.Listener headersListener;
    /**
     * True if response headers have been sent or received.
     */
    private boolean hasResponseHeaders;

    Http2Stream(int id, Http2Connection connection, boolean outFinished, boolean inFinished,
                Headers headers) {
        if (connection == null) throw new NullPointerException("connection == null");

        this.id = id;
        this.connection = connection;
        this.bytesLeftInWriteWindow =
                connection.peerSettings.getInitialWindowSize();
        this.source = new FramingSource(connection.httpSettings.getInitialWindowSize());
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
        throw new StreamResetException(errorCode);
    }

    public synchronized ErrorCode getErrorCode() {
        return errorCode;
    }

    public void writeHeaders(List<Header> responseHeaders, boolean out) throws IOException {
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
            return;
        }
        connection.writeSynReset(id, rstStatusCode);
    }

    public void closeLater(ErrorCode errorCode) {
        if (!closeInternal(errorCode)) {
            return;
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

    void receiveHeaders(List<Header> headers) {
        assert (!Thread.holdsLock(Http2Stream.this));
        boolean open;
        synchronized (this) {
            hasResponseHeaders = true;
            headersQueue.add(Internal.toHeaders(headers));
            open = isOpen();
            notifyAll();
        }
        if (!open) {
            connection.removeStream(id);
        }
    }

    void receiveData(BufferedSource in, int length) throws IOException {
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

    public synchronized void setHeadersListener(Header.Listener headersListener) {
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
            throw new StreamResetException(errorCode);
        }
    }

    void waitForIo() throws InterruptedIOException {
        try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Retain interrupted status.
            throw new InterruptedIOException();
        }
    }

    private final class FramingSource implements Source {
        /**
         * Buffer to receive data from the network into. Only accessed by the reader thread.
         */
        private final Buffer receiveBuffer = new Buffer();

        /**
         * Buffer with readable data. Guarded by Http2Stream.this.
         */
        private final Buffer readBuffer = new Buffer();

        /**
         * Maximum number of bytes to buffer before reporting a flow control error.
         */
        private final long maxByteCount;

        /**
         * True if the caller has closed this stream.
         */
        boolean closed;

        /**
         * True if either side has cleanly shut down this stream. We will receive no more bytes beyond
         * those already in the buffer.
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
                Header.Listener headersListenerToNotify = null;
                long readBytesDelivered = -1;
                ErrorCode errorCodeToDeliver = null;

                synchronized (Http2Stream.this) {
                    readTimeout.enter();
                    try {
                        if (errorCode != null) {
                            // Prepare to deliver an error.
                            errorCodeToDeliver = errorCode;
                        }

                        if (closed) {
                            throw new IOException("stream closed");

                        } else if (!headersQueue.isEmpty() && headersListener != null) {
                            // Prepare to deliver headers.
                            headersToDeliver = headersQueue.removeFirst();
                            headersListenerToNotify = headersListener;

                        } else if (readBuffer.size() > 0) {
                            // Prepare to read bytes. Start by moving them to the caller's buffer.
                            readBytesDelivered = readBuffer.read(sink, Math.min(byteCount, readBuffer.size()));
                            unacknowledgedBytesRead += readBytesDelivered;

                            if (errorCodeToDeliver == null
                                    && unacknowledgedBytesRead
                                    >= connection.httpSettings.getInitialWindowSize() / 2) {
                                // Flow control: notify the peer that we're ready for more data! Only send a
                                // WINDOW_UPDATE if the stream isn't in error.
                                connection.writeWindowUpdateLater(id, unacknowledgedBytesRead);
                                unacknowledgedBytesRead = 0;
                            }
                        } else if (!finished && errorCodeToDeliver == null) {
                            // Nothing to do. Wait until that changes then try again.
                            waitForIo();
                            continue;
                        }
                    } finally {
                        readTimeout.exitAndThrowIfTimedOut();
                    }
                }

                // 2. Do it outside of the synchronized block and timeout.

                if (headersToDeliver != null && headersListenerToNotify != null) {
                    headersListenerToNotify.onHeaders(headersToDeliver);
                    continue;
                }

                if (readBytesDelivered != -1) {
                    updateConnectionFlowControl(readBytesDelivered);
                    return readBytesDelivered;
                }

                if (errorCodeToDeliver != null) {
                    throw new StreamResetException(errorCodeToDeliver);
                }

                return -1;
            }
        }

        private void updateConnectionFlowControl(long read) {
            assert (!Thread.holdsLock(Http2Stream.this));
            connection.updateConnectionFlowControl(read);
        }

        void receive(BufferedSource in, long byteCount) throws IOException {
            assert (!Thread.holdsLock(Http2Stream.this));

            while (byteCount > 0) {
                boolean finished;
                boolean flowControlError;
                synchronized (Http2Stream.this) {
                    finished = this.finished;
                    flowControlError = byteCount + readBuffer.size() > maxByteCount;
                }

                // If the peer sends more data than we can handle, discard it and close the connection.
                if (flowControlError) {
                    in.skip(byteCount);
                    closeLater(ErrorCode.FLOW_CONTROL_ERROR);
                    return;
                }

                // Discard data received after the stream is finished. It's probably a benign race.
                if (finished) {
                    in.skip(byteCount);
                    return;
                }

                // Fill the receive buffer without holding any locks.
                long read = in.read(receiveBuffer, byteCount);
                if (read == -1) throw new EOFException();
                byteCount -= read;

                // Move the received data to the read buffer to the reader can read it.
                synchronized (Http2Stream.this) {
                    boolean wasEmpty = readBuffer.size() == 0;
                    readBuffer.writeAll(receiveBuffer);
                    if (wasEmpty) {
                        Http2Stream.this.notifyAll();
                    }
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
            Header.Listener headersListenerToNotify = null;
            synchronized (Http2Stream.this) {
                closed = true;
                bytesDiscarded = readBuffer.size();
                readBuffer.clear();
                if (!headersQueue.isEmpty() && headersListener != null) {
                    headersToDeliver = new ArrayList<>(headersQueue);
                    headersQueue.clear();
                    headersListenerToNotify = headersListener;
                }
                Http2Stream.this.notifyAll(); // TODO(jwilson): Unnecessary?
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

    final class FramingSink implements Sink {
        private static final long EMIT_BUFFER_SIZE = 16384;

        /**
         * Buffer of outgoing data. This batches writes of small writes into this sink as larges frames
         * written to the outgoing connection. Batching saves the (small) framing overhead.
         */
        private final Buffer sendBuffer = new Buffer();

        boolean closed;

        /**
         * True if either side has cleanly shut down this stream. We shall send no more bytes.
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
                        waitForIo(); // Wait until we receive a WINDOW_UPDATE for this stream.
                    }
                } finally {
                    writeTimeout.exitAndThrowIfTimedOut();
                }

                checkOutNotClosed(); // Kick out if the stream was reset or closed while waiting.
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
                // Emit the remaining data, setting the END_STREAM flag on the last frame.
                if (sendBuffer.size() > 0) {
                    while (sendBuffer.size() > 0) {
                        emitFrame(true);
                    }
                } else {
                    // Send an empty frame just so we can set the END_STREAM flag.
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
     * The org.aoju.bus.core.io.timeout watchdog will call {@link #timedOut} if the timeout is reached. In that case
     * we close the stream (asynchronously) which will notify the waiting thread.
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
            if (exit()) throw newTimeoutException(null /* cause */);
        }
    }

}
