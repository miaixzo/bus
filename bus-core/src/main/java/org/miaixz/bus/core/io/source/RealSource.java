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
package org.miaixz.bus.core.io.source;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.SegmentBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * 原始缓冲流
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealSource implements BufferSource {

    public final Buffer buffer = new Buffer();
    public final Source source;
    boolean closed;

    public RealSource(Source source) {
        if (null == source) {
            throw new NullPointerException("source == null");
        }
        this.source = source;
    }

    @Override
    public Buffer getBuffer() {
        return buffer;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (null == sink) {
            throw new IllegalArgumentException("sink == null");
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (closed) {
            throw new IllegalStateException("closed");
        }

        if (buffer.size == 0) {
            long read = source.read(buffer, SectionBuffer.SIZE);
            if (read == -1)
                return -1;
        }

        long toRead = Math.min(byteCount, buffer.size);
        return buffer.read(sink, toRead);
    }

    @Override
    public boolean exhausted() throws IOException {
        if (closed) {
            throw new IllegalStateException("closed");
        }
        return buffer.exhausted() && source.read(buffer, SectionBuffer.SIZE) == -1;
    }

    @Override
    public void require(long byteCount) throws IOException {
        if (!request(byteCount)) {
            throw new EOFException();
        }
    }

    @Override
    public boolean request(long byteCount) throws IOException {
        if (byteCount < 0)
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (closed)
            throw new IllegalStateException("closed");
        while (buffer.size < byteCount) {
            if (source.read(buffer, SectionBuffer.SIZE) == -1)
                return false;
        }
        return true;
    }

    @Override
    public byte readByte() throws IOException {
        require(1);
        return buffer.readByte();
    }

    @Override
    public ByteString readByteString() throws IOException {
        buffer.writeAll(source);
        return buffer.readByteString();
    }

    @Override
    public ByteString readByteString(long byteCount) throws IOException {
        require(byteCount);
        return buffer.readByteString(byteCount);
    }

    @Override
    public int select(SegmentBuffer segmentBuffer) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");

        while (true) {
            int index = buffer.selectPrefix(segmentBuffer, true);
            if (index == -1)
                return -1;
            if (index == -2) {
                if (source.read(buffer, SectionBuffer.SIZE) == -1L)
                    return -1;
            } else {
                int selectedSize = segmentBuffer.byteStrings[index].size();
                buffer.skip(selectedSize);
                return index;
            }
        }
    }

    @Override
    public byte[] readByteArray() throws IOException {
        buffer.writeAll(source);
        return buffer.readByteArray();
    }

    @Override
    public byte[] readByteArray(long byteCount) throws IOException {
        require(byteCount);
        return buffer.readByteArray(byteCount);
    }

    @Override
    public int read(byte[] sink) throws IOException {
        return read(sink, 0, sink.length);
    }

    @Override
    public void readFully(byte[] sink) throws IOException {
        try {
            require(sink.length);
        } catch (EOFException e) {
            int offset = 0;
            while (buffer.size > 0) {
                int read = buffer.read(sink, offset, (int) buffer.size);
                if (read == -1)
                    throw new AssertionError();
                offset += read;
            }
            throw e;
        }
        buffer.readFully(sink);
    }

    @Override
    public int read(byte[] sink, int offset, int byteCount) throws IOException {
        IoKit.checkOffsetAndCount(sink.length, offset, byteCount);

        if (buffer.size == 0) {
            long read = source.read(buffer, SectionBuffer.SIZE);
            if (read == -1)
                return -1;
        }

        int toRead = (int) Math.min(byteCount, buffer.size);
        return buffer.read(sink, offset, toRead);
    }

    @Override
    public int read(ByteBuffer sink) throws IOException {
        if (buffer.size == 0) {
            long read = source.read(buffer, SectionBuffer.SIZE);
            if (read == -1)
                return -1;
        }

        return buffer.read(sink);
    }

    @Override
    public void readFully(Buffer sink, long byteCount) throws IOException {
        try {
            require(byteCount);
        } catch (EOFException e) {
            // The underlying source is exhausted. Copy the bytes we got before rethrowing.
            sink.writeAll(buffer);
            throw e;
        }
        buffer.readFully(sink, byteCount);
    }

    @Override
    public long readAll(Sink sink) throws IOException {
        if (null == sink) {
            throw new IllegalArgumentException("sink == null");
        }

        long totalBytesWritten = 0;
        while (source.read(buffer, SectionBuffer.SIZE) != -1) {
            long emitByteCount = buffer.completeSegmentByteCount();
            if (emitByteCount > 0) {
                totalBytesWritten += emitByteCount;
                sink.write(buffer, emitByteCount);
            }
        }
        if (buffer.size() > 0) {
            totalBytesWritten += buffer.size();
            sink.write(buffer, buffer.size());
        }
        return totalBytesWritten;
    }

    @Override
    public String readUtf8() throws IOException {
        buffer.writeAll(source);
        return buffer.readUtf8();
    }

    @Override
    public String readUtf8(long byteCount) throws IOException {
        require(byteCount);
        return buffer.readUtf8(byteCount);
    }

    @Override
    public String readString(Charset charset) throws IOException {
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }

        buffer.writeAll(source);
        return buffer.readString(charset);
    }

    @Override
    public String readString(long byteCount, Charset charset) throws IOException {
        require(byteCount);
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }
        return buffer.readString(byteCount, charset);
    }

    @Override
    public String readUtf8Line() throws IOException {
        long newline = indexOf((byte) Symbol.C_LF);

        if (newline == -1) {
            return buffer.size != 0 ? readUtf8(buffer.size) : null;
        }

        return buffer.readUtf8Line(newline);
    }

    @Override
    public String readUtf8LineStrict() throws IOException {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    @Override
    public String readUtf8LineStrict(long limit) throws IOException {
        if (limit < 0)
            throw new IllegalArgumentException("limit < 0: " + limit);
        long scanLength = limit == Long.MAX_VALUE ? Long.MAX_VALUE : limit + 1;
        long newline = indexOf((byte) Symbol.C_LF, 0, scanLength);
        if (newline != -1)
            return buffer.readUtf8Line(newline);
        if (scanLength < Long.MAX_VALUE && request(scanLength) && buffer.getByte(scanLength - 1) == Symbol.C_CR
                && request(scanLength + 1) && buffer.getByte(scanLength) == Symbol.C_LF) {
            return buffer.readUtf8Line(scanLength); // The line was 'limit' UTF-8 bytes followed by \r\n.
        }
        Buffer data = new Buffer();
        buffer.copyTo(data, 0, Math.min(Normal._32, buffer.size()));
        throw new EOFException("\\n not found: limit=" + Math.min(buffer.size(), limit) + " content="
                + data.readByteString().hex() + '…');
    }

    @Override
    public int readUtf8CodePoint() throws IOException {
        require(1);

        byte b0 = buffer.getByte(0);
        if ((b0 & 0xe0) == 0xc0) {
            require(2);
        } else if ((b0 & 0xf0) == 0xe0) {
            require(3);
        } else if ((b0 & 0xf8) == 0xf0) {
            require(4);
        }

        return buffer.readUtf8CodePoint();
    }

    @Override
    public short readShort() throws IOException {
        require(2);
        return buffer.readShort();
    }

    @Override
    public short readShortLe() throws IOException {
        require(2);
        return buffer.readShortLe();
    }

    @Override
    public int readInt() throws IOException {
        require(4);
        return buffer.readInt();
    }

    @Override
    public int readIntLe() throws IOException {
        require(4);
        return buffer.readIntLe();
    }

    @Override
    public long readLong() throws IOException {
        require(8);
        return buffer.readLong();
    }

    @Override
    public long readLongLe() throws IOException {
        require(8);
        return buffer.readLongLe();
    }

    @Override
    public long readDecimalLong() throws IOException {
        require(1);

        for (int pos = 0; request(pos + 1); pos++) {
            byte b = buffer.getByte(pos);
            if ((b < Symbol.C_ZERO || b > Symbol.C_NINE) && (pos != 0 || b != Symbol.C_MINUS)) {
                // Non-digit, or non-leading negative sign.
                if (pos == 0) {
                    throw new NumberFormatException(
                            String.format("Expected leading [0-9] or '-' character but was %#x", b));
                }
                break;
            }
        }

        return buffer.readDecimalLong();
    }

    @Override
    public long readHexadecimalUnsignedLong() throws IOException {
        require(1);

        for (int pos = 0; request(pos + 1); pos++) {
            byte b = buffer.getByte(pos);
            if ((b < Symbol.C_ZERO || b > Symbol.C_NINE) && (b < 'a' || b > 'f') && (b < 'A' || b > 'F')) {
                // Non-digit, or non-leading negative sign.
                if (pos == 0) {
                    throw new NumberFormatException(
                            String.format("Expected leading [0-9a-fA-F] character but was %#x", b));
                }
                break;
            }
        }

        return buffer.readHexadecimalUnsignedLong();
    }

    @Override
    public void skip(long byteCount) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        while (byteCount > 0) {
            if (buffer.size == 0 && source.read(buffer, SectionBuffer.SIZE) == -1) {
                throw new EOFException();
            }
            long toSkip = Math.min(byteCount, buffer.size());
            buffer.skip(toSkip);
            byteCount -= toSkip;
        }
    }

    @Override
    public long indexOf(byte b) throws IOException {
        return indexOf(b, 0, Long.MAX_VALUE);
    }

    @Override
    public long indexOf(byte b, long fromIndex) throws IOException {
        return indexOf(b, fromIndex, Long.MAX_VALUE);
    }

    @Override
    public long indexOf(byte b, long fromIndex, long toIndex) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");
        if (fromIndex < 0 || toIndex < fromIndex) {
            throw new IllegalArgumentException(String.format("fromIndex=%s toIndex=%s", fromIndex, toIndex));
        }

        while (fromIndex < toIndex) {
            long result = buffer.indexOf(b, fromIndex, toIndex);
            if (result != -1L)
                return result;

            // The byte wasn't in the buffer. Give up if we've already reached our target size or if the
            // underlying stream is exhausted.
            long lastBufferSize = buffer.size;
            if (lastBufferSize >= toIndex || source.read(buffer, SectionBuffer.SIZE) == -1)
                return -1L;

            // Continue the search from where we left off.
            fromIndex = Math.max(fromIndex, lastBufferSize);
        }
        return -1L;
    }

    @Override
    public long indexOf(ByteString bytes) throws IOException {
        return indexOf(bytes, 0);
    }

    @Override
    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");

        while (true) {
            long result = buffer.indexOf(bytes, fromIndex);
            if (result != -1)
                return result;

            long lastBufferSize = buffer.size;
            if (source.read(buffer, SectionBuffer.SIZE) == -1)
                return -1L;

            // Keep searching, picking up from where we left off.
            fromIndex = Math.max(fromIndex, lastBufferSize - bytes.size() + 1);
        }
    }

    @Override
    public long indexOfElement(ByteString targetBytes) throws IOException {
        return indexOfElement(targetBytes, 0);
    }

    @Override
    public long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");

        while (true) {
            long result = buffer.indexOfElement(targetBytes, fromIndex);
            if (result != -1)
                return result;

            long lastBufferSize = buffer.size;
            if (source.read(buffer, SectionBuffer.SIZE) == -1)
                return -1L;

            // Keep searching, picking up from where we left off.
            fromIndex = Math.max(fromIndex, lastBufferSize);
        }
    }

    @Override
    public boolean rangeEquals(long offset, ByteString bytes) throws IOException {
        return rangeEquals(offset, bytes, 0, bytes.size());
    }

    @Override
    public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) throws IOException {
        if (closed)
            throw new IllegalStateException("closed");

        if (offset < 0 || bytesOffset < 0 || byteCount < 0 || bytes.size() - bytesOffset < byteCount) {
            return false;
        }
        for (int i = 0; i < byteCount; i++) {
            long bufferOffset = offset + i;
            if (!request(bufferOffset + 1))
                return false;
            if (buffer.getByte(bufferOffset) != bytes.getByte(bytesOffset + i))
                return false;
        }
        return true;
    }

    @Override
    public BufferSource peek() {
        return IoKit.buffer(new PeekSource(this));
    }

    @Override
    public InputStream inputStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                if (closed)
                    throw new IOException("closed");
                if (buffer.size == 0) {
                    long count = source.read(buffer, SectionBuffer.SIZE);
                    if (count == -1)
                        return -1;
                }
                return buffer.readByte() & 0xff;
            }

            @Override
            public int read(byte[] data, int offset, int byteCount) throws IOException {
                if (closed)
                    throw new IOException("closed");
                IoKit.checkOffsetAndCount(data.length, offset, byteCount);

                if (buffer.size == 0) {
                    long count = source.read(buffer, SectionBuffer.SIZE);
                    if (count == -1)
                        return -1;
                }

                return buffer.read(data, offset, byteCount);
            }

            @Override
            public int available() throws IOException {
                if (closed)
                    throw new IOException("closed");
                return (int) Math.min(buffer.size, Integer.MAX_VALUE);
            }

            @Override
            public void close() throws IOException {
                RealSource.this.close();
            }

            @Override
            public String toString() {
                return RealSource.this + ".inputStream()";
            }
        };
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public void close() throws IOException {
        if (closed)
            return;
        closed = true;
        source.close();
        buffer.clear();
    }

    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    @Override
    public String toString() {
        return "buffer(" + source + Symbol.PARENTHESE_RIGHT;
    }

}
