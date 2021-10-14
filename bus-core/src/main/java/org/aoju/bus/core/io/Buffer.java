/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.io;

import org.aoju.bus.core.lang.Algorithm;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.ByteKit;
import org.aoju.bus.core.toolkit.IoKit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.channels.ByteChannel;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 内存中字节的集合.
 *
 * @author Kimi Liu
 * @version 6.3.0
 * @since JDK 1.8+
 */
public class Buffer implements BufferSource, BufferSink, Cloneable, ByteChannel {

    static final int REPLACEMENT_CHARACTER = '\ufffd';

    public Segment head;
    public long size;

    public Buffer() {
    }

    public final long size() {
        return size;
    }

    @Override
    public Buffer buffer() {
        return this;
    }

    @Override
    public Buffer getBuffer() {
        return this;
    }

    @Override
    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) {
                writeByte((byte) b);
            }

            @Override
            public void write(byte[] data, int offset, int byteCount) {
                Buffer.this.write(data, offset, byteCount);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }

            @Override
            public String toString() {
                return Buffer.this + ".outputStream()";
            }
        };
    }

    @Override
    public Buffer emitCompleteSegments() {
        return this;
    }

    @Override
    public BufferSink emit() {
        return this;
    }

    @Override
    public boolean exhausted() {
        return size == 0;
    }

    @Override
    public void require(long byteCount) throws EOFException {
        if (size < byteCount) throw new EOFException();
    }

    @Override
    public boolean request(long byteCount) {
        return size >= byteCount;
    }

    @Override
    public BufferSource peek() {
        return IoKit.buffer(new PeekSource(this));
    }

    @Override
    public InputStream inputStream() {
        return new InputStream() {
            @Override
            public int read() {
                if (size > 0) return readByte() & 0xff;
                return -1;
            }

            @Override
            public int read(byte[] sink, int offset, int byteCount) {
                return Buffer.this.read(sink, offset, byteCount);
            }

            @Override
            public int available() {
                return (int) Math.min(size, Integer.MAX_VALUE);
            }

            @Override
            public void close() {
            }

            @Override
            public String toString() {
                return Buffer.this + ".inputStream()";
            }
        };
    }

    /**
     * 将其内容复制到 {@code out}.
     *
     * @param out 输出流
     * @return Buffer 内容
     * @throws IOException 抛出异常
     */
    public final Buffer copyTo(OutputStream out) throws IOException {
        return copyTo(out, 0, size);
    }

    /**
     * 从这里复制{@code byteCount}字节，从{@code offset}开始，复制到  {@code out}.
     *
     * @param out       输出流
     * @param offset    偏移量
     * @param byteCount 偏移量
     * @return Buffer 内容
     * @throws IOException 抛出异常
     */
    public final Buffer copyTo(OutputStream out, long offset, long byteCount) throws IOException {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        IoKit.checkOffsetAndCount(size, offset, byteCount);
        if (byteCount == 0) return this;

        Segment s = head;
        for (; offset >= (s.limit - s.pos); s = s.next) {
            offset -= (s.limit - s.pos);
        }

        for (; byteCount > 0; s = s.next) {
            int pos = (int) (s.pos + offset);
            int toCopy = (int) Math.min(s.limit - pos, byteCount);
            out.write(s.data, pos, toCopy);
            byteCount -= toCopy;
            offset = 0;
        }

        return this;
    }

    /**
     * 从这里复制{@code byteCount}字节，从{@code offset}开始，复制到{@code out}.
     *
     * @param out       输出流
     * @param offset    偏移量
     * @param byteCount 偏移量
     * @return Buffer 内容
     */
    public final Buffer copyTo(Buffer out, long offset, long byteCount) {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        IoKit.checkOffsetAndCount(size, offset, byteCount);
        if (byteCount == 0) return this;

        out.size += byteCount;

        Segment s = head;
        for (; offset >= (s.limit - s.pos); s = s.next) {
            offset -= (s.limit - s.pos);
        }
        for (; byteCount > 0; s = s.next) {
            Segment copy = s.sharedCopy();
            copy.pos += offset;
            copy.limit = Math.min(copy.pos + (int) byteCount, copy.limit);
            if (null == out.head) {
                out.head = copy.next = copy.prev = copy;
            } else {
                out.head.prev.push(copy);
            }
            byteCount -= copy.limit - copy.pos;
            offset = 0;
        }

        return this;
    }

    /**
     * 将其内容写入{@code out}.
     *
     * @param out 输出流
     * @return Buffer 内容
     * @throws IOException 抛出异常
     */
    public final Buffer writeTo(OutputStream out) throws IOException {
        return writeTo(out, size);
    }

    /**
     * 将{@code byteCount}字节写入{@code out}.
     *
     * @param out       输出流
     * @param byteCount 偏移量
     * @return Buffer 内容
     * @throws IOException 抛出异常
     */
    public final Buffer writeTo(OutputStream out, long byteCount) throws IOException {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        IoKit.checkOffsetAndCount(size, 0, byteCount);

        Segment s = head;
        while (byteCount > 0) {
            int toCopy = (int) Math.min(byteCount, s.limit - s.pos);
            out.write(s.data, s.pos, toCopy);

            s.pos += toCopy;
            size -= toCopy;
            byteCount -= toCopy;

            if (s.pos == s.limit) {
                Segment toRecycle = s;
                head = s = toRecycle.pop();
                LifeCycle.recycle(toRecycle);
            }
        }

        return this;
    }

    /**
     * 将{@code in}中的字节读入并转为bytes
     *
     * @param in 输入流
     * @return Buffer 内容
     * @throws IOException 抛出异常
     */
    public final Buffer readFrom(InputStream in) throws IOException {
        readFrom(in, Long.MAX_VALUE, true);
        return this;
    }

    /**
     * Read {@code byteCount} bytes from {@code in} to this.
     *
     * @param in        输入流
     * @param byteCount 偏移量
     * @return Buffer 内容
     * @throws IOException 抛出异常
     */
    public final Buffer readFrom(InputStream in, long byteCount) throws IOException {
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        readFrom(in, byteCount, false);
        return this;
    }

    private void readFrom(InputStream in, long byteCount, boolean forever) throws IOException {
        if (null == in) {
            throw new IllegalArgumentException("in == null");
        }
        while (byteCount > 0 || forever) {
            Segment tail = writableSegment(1);
            int maxToCopy = (int) Math.min(byteCount, Segment.SIZE - tail.limit);
            int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
            if (bytesRead == -1) {
                if (forever) return;
                throw new EOFException();
            }
            tail.limit += bytesRead;
            size += bytesRead;
            byteCount -= bytesRead;
        }
    }

    public final long completeSegmentByteCount() {
        long result = size;
        if (result == 0) return 0;

        Segment tail = head.prev;
        if (tail.limit < Segment.SIZE && tail.owner) {
            result -= tail.limit - tail.pos;
        }

        return result;
    }

    @Override
    public byte readByte() {
        if (size == 0) throw new IllegalStateException("size == 0");

        Segment segment = head;
        int pos = segment.pos;
        int limit = segment.limit;

        byte[] data = segment.data;
        byte b = data[pos++];
        size -= 1;

        if (pos == limit) {
            head = segment.pop();
            LifeCycle.recycle(segment);
        } else {
            segment.pos = pos;
        }

        return b;
    }

    /**
     * 返回{@code pos}处的字节.
     *
     * @param pos long
     * @return byte 内容
     */
    public final byte getByte(long pos) {
        IoKit.checkOffsetAndCount(size, pos, 1);
        if (size - pos > pos) {
            for (Segment s = head; true; s = s.next) {
                int segmentByteCount = s.limit - s.pos;
                if (pos < segmentByteCount) return s.data[s.pos + (int) pos];
                pos -= segmentByteCount;
            }
        } else {
            pos -= size;
            for (Segment s = head.prev; true; s = s.prev) {
                pos += s.limit - s.pos;
                if (pos >= 0) return s.data[s.pos + (int) pos];
            }
        }
    }

    @Override
    public short readShort() {
        if (size < 2) throw new IllegalStateException("size < 2: " + size);

        Segment segment = head;
        int pos = segment.pos;
        int limit = segment.limit;

        if (limit - pos < 2) {
            int s = (readByte() & 0xff) << 8
                    | (readByte() & 0xff);
            return (short) s;
        }

        byte[] data = segment.data;
        int s = (data[pos++] & 0xff) << 8
                | (data[pos++] & 0xff);
        size -= 2;

        if (pos == limit) {
            head = segment.pop();
            LifeCycle.recycle(segment);
        } else {
            segment.pos = pos;
        }

        return (short) s;
    }

    @Override
    public int readInt() {
        if (size < 4) throw new IllegalStateException("size < 4: " + size);

        Segment segment = head;
        int pos = segment.pos;
        int limit = segment.limit;

        if (limit - pos < 4) {
            return (readByte() & 0xff) << 24
                    | (readByte() & 0xff) << 16
                    | (readByte() & 0xff) << 8
                    | (readByte() & 0xff);
        }

        byte[] data = segment.data;
        int i = (data[pos++] & 0xff) << 24
                | (data[pos++] & 0xff) << 16
                | (data[pos++] & 0xff) << 8
                | (data[pos++] & 0xff);
        size -= 4;

        if (pos == limit) {
            head = segment.pop();
            LifeCycle.recycle(segment);
        } else {
            segment.pos = pos;
        }

        return i;
    }

    @Override
    public long readLong() {
        if (size < 8) throw new IllegalStateException("size < 8: " + size);

        Segment segment = head;
        int pos = segment.pos;
        int limit = segment.limit;

        if (limit - pos < 8) {
            return (readInt() & 0xffffffffL) << 32
                    | (readInt() & 0xffffffffL);
        }

        byte[] data = segment.data;
        long v = (data[pos++] & 0xffL) << 56
                | (data[pos++] & 0xffL) << 48
                | (data[pos++] & 0xffL) << 40
                | (data[pos++] & 0xffL) << 32
                | (data[pos++] & 0xffL) << 24
                | (data[pos++] & 0xffL) << 16
                | (data[pos++] & 0xffL) << 8
                | (data[pos++] & 0xffL);
        size -= 8;

        if (pos == limit) {
            head = segment.pop();
            LifeCycle.recycle(segment);
        } else {
            segment.pos = pos;
        }

        return v;
    }

    @Override
    public short readShortLe() {
        return IoKit.reverseBytesShort(readShort());
    }

    @Override
    public int readIntLe() {
        return IoKit.reverseBytesInt(readInt());
    }

    @Override
    public long readLongLe() {
        return IoKit.reverseBytesLong(readLong());
    }

    @Override
    public long readDecimalLong() {
        if (size == 0) throw new IllegalStateException("size == 0");

        long value = 0;
        int seen = 0;
        boolean negative = false;
        boolean done = false;

        long overflowZone = Long.MIN_VALUE / 10;
        long overflowDigit = (Long.MIN_VALUE % 10) + 1;

        do {
            Segment segment = head;

            byte[] data = segment.data;
            int pos = segment.pos;
            int limit = segment.limit;

            for (; pos < limit; pos++, seen++) {
                byte b = data[pos];
                if (b >= Symbol.C_ZERO && b <= Symbol.C_NINE) {
                    int digit = Symbol.C_ZERO - b;

                    if (value < overflowZone || value == overflowZone && digit < overflowDigit) {
                        Buffer buffer = new Buffer().writeDecimalLong(value).writeByte(b);
                        if (!negative) buffer.readByte(); // Skip negative sign.
                        throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                    }
                    value *= 10;
                    value += digit;
                } else if (b == Symbol.C_MINUS && seen == 0) {
                    negative = true;
                    overflowDigit -= 1;
                } else {
                    if (seen == 0) {
                        throw new NumberFormatException(
                                "Expected leading [0-9] or '-' character but was 0x" + Integer.toHexString(b));
                    }
                    done = true;
                    break;
                }
            }

            if (pos == limit) {
                head = segment.pop();
                LifeCycle.recycle(segment);
            } else {
                segment.pos = pos;
            }
        } while (!done && null != head);

        size -= seen;
        return negative ? value : -value;
    }

    @Override
    public long readHexadecimalUnsignedLong() {
        if (size == 0) throw new IllegalStateException("size == 0");

        long value = 0;
        int seen = 0;
        boolean done = false;

        do {
            Segment segment = head;

            byte[] data = segment.data;
            int pos = segment.pos;
            int limit = segment.limit;

            for (; pos < limit; pos++, seen++) {
                int digit;

                byte b = data[pos];
                if (b >= Symbol.C_ZERO && b <= Symbol.C_NINE) {
                    digit = b - Symbol.C_ZERO;
                } else if (b >= 'a' && b <= 'f') {
                    digit = b - 'a' + 10;
                } else if (b >= 'A' && b <= 'F') {
                    digit = b - 'A' + 10;
                } else {
                    if (seen == 0) {
                        throw new NumberFormatException(
                                "Expected leading [0-9a-fA-F] character but was 0x" + Integer.toHexString(b));
                    }
                    done = true;
                    break;
                }

                if ((value & 0xf000000000000000L) != 0) {
                    Buffer buffer = new Buffer().writeHexadecimalUnsignedLong(value).writeByte(b);
                    throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                }

                value <<= 4;
                value |= digit;
            }

            if (pos == limit) {
                head = segment.pop();
                LifeCycle.recycle(segment);
            } else {
                segment.pos = pos;
            }
        } while (!done && null != head);

        size -= seen;
        return value;
    }

    @Override
    public ByteString readByteString() {
        return new ByteString(readByteArray());
    }

    @Override
    public ByteString readByteString(long byteCount) throws EOFException {
        return new ByteString(readByteArray(byteCount));
    }

    @Override
    public int select(AbstractBlending options) {
        int index = selectPrefix(options, false);
        if (index == -1) return -1;

        int selectedSize = options.byteStrings[index].size();
        try {
            skip(selectedSize);
        } catch (EOFException e) {
            throw new AssertionError();
        }
        return index;
    }

    /**
     * 返回此缓冲区前缀的选项中的值的索引。如果没有找到值，则返回-1
     * 此方法执行两个同步迭代:迭代trie和迭代这个缓冲区。当它在trie中到达一个结果时，
     * 当它在trie中不匹配时，以及当缓冲区耗尽时，它将返回
     *
     * @param selectTruncated 如果可能的结果出现但被截断，则true返回-2
     *                        例如，如果缓冲区包含[ab]，并且选项是[abc, abd]，则返回-2
     *                        请注意，由于选项是按优先顺序列出的，而且第一个选项可能是另一个选项的前缀，
     *                        这使得情况变得复杂。例如，如果缓冲区包含[ab]而选项是[abc, a]，则返回-2
     */
    int selectPrefix(AbstractBlending options, boolean selectTruncated) {
        Segment head = this.head;
        if (null == head) {
            if (selectTruncated) return -2;
            return options.indexOf(ByteString.EMPTY);
        }

        Segment s = head;
        byte[] data = head.data;
        int pos = head.pos;
        int limit = head.limit;

        int[] trie = options.trie;
        int triePos = 0;

        int prefixIndex = -1;

        navigateTrie:
        while (true) {
            int scanOrSelect = trie[triePos++];

            int possiblePrefixIndex = trie[triePos++];
            if (possiblePrefixIndex != -1) {
                prefixIndex = possiblePrefixIndex;
            }

            int nextStep;

            if (null == s) {
                break;
            } else if (scanOrSelect < 0) {
                int scanByteCount = -1 * scanOrSelect;
                int trieLimit = triePos + scanByteCount;
                while (true) {
                    int b = data[pos++] & 0xff;
                    if (b != trie[triePos++]) return prefixIndex;
                    boolean scanComplete = (triePos == trieLimit);

                    if (pos == limit) {
                        s = s.next;
                        pos = s.pos;
                        data = s.data;
                        limit = s.limit;
                        if (s == head) {
                            if (!scanComplete) break navigateTrie;
                            s = null;
                        }
                    }

                    if (scanComplete) {
                        nextStep = trie[triePos];
                        break;
                    }
                }
            } else {
                int selectChoiceCount = scanOrSelect;
                int b = data[pos++] & 0xff;
                int selectLimit = triePos + selectChoiceCount;
                while (true) {
                    if (triePos == selectLimit) return prefixIndex;

                    if (b == trie[triePos]) {
                        nextStep = trie[triePos + selectChoiceCount];
                        break;
                    }

                    triePos++;
                }

                if (pos == limit) {
                    s = s.next;
                    pos = s.pos;
                    data = s.data;
                    limit = s.limit;
                    if (s == head) {
                        s = null;
                    }
                }
            }

            if (nextStep >= 0) return nextStep;
            triePos = -nextStep;
        }


        if (selectTruncated) return -2;
        return prefixIndex;
    }

    @Override
    public void readFully(Buffer sink, long byteCount) throws EOFException {
        if (size < byteCount) {
            sink.write(this, size);
            throw new EOFException();
        }
        sink.write(this, byteCount);
    }

    @Override
    public long readAll(Sink sink) throws IOException {
        long byteCount = size;
        if (byteCount > 0) {
            sink.write(this, byteCount);
        }
        return byteCount;
    }

    @Override
    public String readUtf8() {
        try {
            return readString(size, Charset.UTF_8);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String readUtf8(long byteCount) throws EOFException {
        return readString(byteCount, Charset.UTF_8);
    }

    @Override
    public String readString(java.nio.charset.Charset charset) {
        try {
            return readString(size, charset);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String readString(long byteCount, java.nio.charset.Charset charset) throws EOFException {
        IoKit.checkOffsetAndCount(size, 0, byteCount);
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }
        if (byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        }
        if (byteCount == 0) return Normal.EMPTY;

        Segment s = head;
        if (s.pos + byteCount > s.limit) {
            return new String(readByteArray(byteCount), charset);
        }

        String result = new String(s.data, s.pos, (int) byteCount, charset);
        s.pos += byteCount;
        size -= byteCount;

        if (s.pos == s.limit) {
            head = s.pop();
            LifeCycle.recycle(s);
        }

        return result;
    }

    @Override
    public String readUtf8Line() throws EOFException {
        long newline = indexOf((byte) Symbol.C_LF);

        if (newline == -1) {
            return size != 0 ? readUtf8(size) : null;
        }

        return readUtf8Line(newline);
    }

    @Override
    public String readUtf8LineStrict() throws EOFException {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    @Override
    public String readUtf8LineStrict(long limit) throws EOFException {
        if (limit < 0) throw new IllegalArgumentException("limit < 0: " + limit);
        long scanLength = limit == Long.MAX_VALUE ? Long.MAX_VALUE : limit + 1;
        long newline = indexOf((byte) Symbol.C_LF, 0, scanLength);
        if (newline != -1) return readUtf8Line(newline);
        if (scanLength < size()
                && getByte(scanLength - 1) == Symbol.C_CR && getByte(scanLength) == Symbol.C_LF) {
            return readUtf8Line(scanLength);
        }
        Buffer data = new Buffer();
        copyTo(data, 0, Math.min(32, size()));
        throw new EOFException("\\n not found: limit=" + Math.min(size(), limit)
                + " content=" + data.readByteString().hex() + '…');
    }

    String readUtf8Line(long newline) throws EOFException {
        if (newline > 0 && getByte(newline - 1) == Symbol.C_CR) {
            String result = readUtf8((newline - 1));
            skip(2);
            return result;

        } else {
            String result = readUtf8(newline);
            skip(1);
            return result;
        }
    }

    @Override
    public int readUtf8CodePoint() throws EOFException {
        if (size == 0) throw new EOFException();

        byte b0 = getByte(0);
        int codePoint;
        int byteCount;
        int min;

        if ((b0 & 0x80) == 0) {
            // 0xxxxxxx.
            codePoint = b0 & 0x7f;
            byteCount = 1; // 7 bits (ASCII).
            min = 0x0;

        } else if ((b0 & 0xe0) == 0xc0) {
            // 0x110xxxxx
            codePoint = b0 & 0x1f;
            byteCount = 2; // 11 bits (5 + 6).
            min = 0x80;

        } else if ((b0 & 0xf0) == 0xe0) {
            // 0x1110xxxx
            codePoint = b0 & 0x0f;
            byteCount = 3; // 16 bits (4 + 6 + 6).
            min = 0x800;

        } else if ((b0 & 0xf8) == 0xf0) {
            // 0x11110xxx
            codePoint = b0 & 0x07;
            byteCount = 4; // 21 bits (3 + 6 + 6 + 6).
            min = 0x10000;

        } else {
            skip(1);
            return REPLACEMENT_CHARACTER;
        }

        if (size < byteCount) {
            throw new EOFException("size < " + byteCount + ": " + size
                    + " (to read code point prefixed 0x" + Integer.toHexString(b0) + ")");
        }

        for (int i = 1; i < byteCount; i++) {
            byte b = getByte(i);
            if ((b & 0xc0) == 0x80) {
                // 0x10xxxxxx
                codePoint <<= 6;
                codePoint |= b & 0x3f;
            } else {
                skip(i);
                return REPLACEMENT_CHARACTER;
            }
        }

        skip(byteCount);

        if (codePoint > 0x10ffff) {
            return REPLACEMENT_CHARACTER;
        }

        if (codePoint >= 0xd800 && codePoint <= 0xdfff) {
            return REPLACEMENT_CHARACTER;
        }

        if (codePoint < min) {
            return REPLACEMENT_CHARACTER;
        }

        return codePoint;
    }

    @Override
    public byte[] readByteArray() {
        try {
            return readByteArray(size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public byte[] readByteArray(long byteCount) throws EOFException {
        IoKit.checkOffsetAndCount(size, 0, byteCount);
        if (byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        }

        byte[] result = new byte[(int) byteCount];
        readFully(result);
        return result;
    }

    @Override
    public int read(byte[] sink) {
        return read(sink, 0, sink.length);
    }

    @Override
    public void readFully(byte[] sink) throws EOFException {
        int offset = 0;
        while (offset < sink.length) {
            int read = read(sink, offset, sink.length - offset);
            if (read == -1) throw new EOFException();
            offset += read;
        }
    }

    @Override
    public int read(byte[] sink, int offset, int byteCount) {
        IoKit.checkOffsetAndCount(sink.length, offset, byteCount);
        Segment s = head;
        if (null == s) {
            return -1;
        }
        int toCopy = Math.min(byteCount, s.limit - s.pos);
        System.arraycopy(s.data, s.pos, sink, offset, toCopy);

        s.pos += toCopy;
        size -= toCopy;

        if (s.pos == s.limit) {
            head = s.pop();
            LifeCycle.recycle(s);
        }

        return toCopy;
    }

    @Override
    public int read(java.nio.ByteBuffer sink) throws IOException {
        Segment s = head;
        if (null == s) {
            return -1;
        }

        int toCopy = Math.min(sink.remaining(), s.limit - s.pos);
        sink.put(s.data, s.pos, toCopy);

        s.pos += toCopy;
        size -= toCopy;

        if (s.pos == s.limit) {
            head = s.pop();
            LifeCycle.recycle(s);
        }

        return toCopy;
    }

    /**
     * 丢弃此缓冲区中的所有字节。在使用完缓冲区后调用此方法将把它的段返回到池中
     */
    public final void clear() {
        try {
            skip(size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 从这个缓冲区的头部丢弃{@code byteCount}字节.
     */
    @Override
    public void skip(long byteCount) throws EOFException {
        while (byteCount > 0) {
            if (null == head) {
                throw new EOFException();
            }

            int toSkip = (int) Math.min(byteCount, head.limit - head.pos);
            size -= toSkip;
            byteCount -= toSkip;
            head.pos += toSkip;

            if (head.pos == head.limit) {
                Segment toRecycle = head;
                head = toRecycle.pop();
                LifeCycle.recycle(toRecycle);
            }
        }
    }

    @Override
    public Buffer write(ByteString byteString) {
        if (null == byteString) {
            throw new IllegalArgumentException("byteString == null");
        }
        byteString.write(this);
        return this;
    }

    @Override
    public Buffer writeUtf8(String string) {
        return writeUtf8(string, 0, string.length());
    }

    @Override
    public Buffer writeUtf8(String string, int beginIndex, int endIndex) {
        if (null == string) {
            throw new IllegalArgumentException("string == null");
        }
        if (beginIndex < 0) {
            throw new IllegalArgumentException("beginIndex < 0: " + beginIndex);
        }
        if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        }
        if (endIndex > string.length()) {
            throw new IllegalArgumentException(
                    "endIndex > string.length: " + endIndex + " > " + string.length());
        }

        for (int i = beginIndex; i < endIndex; ) {
            int c = string.charAt(i);

            if (c < 0x80) {
                Segment tail = writableSegment(1);
                byte[] data = tail.data;
                int segmentOffset = tail.limit - i;
                int runLimit = Math.min(endIndex, Segment.SIZE - segmentOffset);

                data[segmentOffset + i++] = (byte) c;

                while (i < runLimit) {
                    c = string.charAt(i);
                    if (c >= 0x80) break;
                    data[segmentOffset + i++] = (byte) c;
                }

                int runSize = i + segmentOffset - tail.limit;
                tail.limit += runSize;
                size += runSize;

            } else if (c < 0x800) {
                writeByte(c >> 6 | 0xc0);
                writeByte(c & 0x3f | 0x80);
                i++;

            } else if (c < 0xd800 || c > 0xdfff) {
                writeByte(c >> 12 | 0xe0);
                writeByte(c >> 6 & 0x3f | 0x80);
                writeByte(c & 0x3f | 0x80);
                i++;

            } else {
                int low = i + 1 < endIndex ? string.charAt(i + 1) : 0;
                if (c > 0xdbff || low < 0xdc00 || low > 0xdfff) {
                    writeByte(Symbol.C_QUESTION_MARK);
                    i++;
                    continue;
                }
                int codePoint = 0x010000 + ((c & ~0xd800) << 10 | low & ~0xdc00);

                writeByte(codePoint >> 18 | 0xf0);
                writeByte(codePoint >> 12 & 0x3f | 0x80);
                writeByte(codePoint >> 6 & 0x3f | 0x80);
                writeByte(codePoint & 0x3f | 0x80);
                i += 2;
            }
        }

        return this;
    }

    @Override
    public Buffer writeUtf8CodePoint(int codePoint) {
        if (codePoint < 0x80) {
            writeByte(codePoint);

        } else if (codePoint < 0x800) {
            writeByte(codePoint >> 6 | 0xc0);
            writeByte(codePoint & 0x3f | 0x80);

        } else if (codePoint < 0x10000) {
            if (codePoint >= 0xd800 && codePoint <= 0xdfff) {
                writeByte(Symbol.C_QUESTION_MARK);
            } else {
                writeByte(codePoint >> 12 | 0xe0);
                writeByte(codePoint >> 6 & 0x3f | 0x80);
                writeByte(codePoint & 0x3f | 0x80);
            }

        } else if (codePoint <= 0x10ffff) {
            writeByte(codePoint >> 18 | 0xf0);
            writeByte(codePoint >> 12 & 0x3f | 0x80);
            writeByte(codePoint >> 6 & 0x3f | 0x80);
            writeByte(codePoint & 0x3f | 0x80);

        } else {
            throw new IllegalArgumentException(
                    "Unexpected code point: " + Integer.toHexString(codePoint));
        }

        return this;
    }

    @Override
    public Buffer writeString(String string, java.nio.charset.Charset charset) {
        return writeString(string, 0, string.length(), charset);
    }

    @Override
    public Buffer writeString(String string, int beginIndex, int endIndex, java.nio.charset.Charset charset) {
        if (null == string) {
            throw new IllegalArgumentException("string == null");
        }
        if (beginIndex < 0) {
            throw new IllegalAccessError("beginIndex < 0: " + beginIndex);
        }
        if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        }
        if (endIndex > string.length()) {
            throw new IllegalArgumentException(
                    "endIndex > string.length: " + endIndex + " > " + string.length());
        }
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }
        if (charset.equals(Symbol.C_SLASH)) {
            return writeUtf8(string, beginIndex, endIndex);
        }
        byte[] data = string.substring(beginIndex, endIndex).getBytes(charset);
        return write(data, 0, data.length);
    }

    @Override
    public Buffer write(byte[] source) {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        return write(source, 0, source.length);
    }

    @Override
    public Buffer write(byte[] source, int offset, int byteCount) {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        IoKit.checkOffsetAndCount(source.length, offset, byteCount);
        int limit = offset + byteCount;
        while (offset < limit) {
            Segment tail = writableSegment(1);

            int toCopy = Math.min(limit - offset, Segment.SIZE - tail.limit);
            System.arraycopy(source, offset, tail.data, tail.limit, toCopy);

            offset += toCopy;
            tail.limit += toCopy;
        }

        size += byteCount;
        return this;
    }

    @Override
    public int write(java.nio.ByteBuffer source) throws IOException {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }

        int byteCount = source.remaining();
        int remaining = byteCount;
        while (remaining > 0) {
            Segment tail = writableSegment(1);

            int toCopy = Math.min(remaining, Segment.SIZE - tail.limit);
            source.get(tail.data, tail.limit, toCopy);

            remaining -= toCopy;
            tail.limit += toCopy;
        }

        size += byteCount;
        return byteCount;
    }

    @Override
    public long writeAll(Source source) throws IOException {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        long totalBytesRead = 0;
        for (long readCount; (readCount = source.read(this, Segment.SIZE)) != -1; ) {
            totalBytesRead += readCount;
        }
        return totalBytesRead;
    }

    @Override
    public BufferSink write(Source source, long byteCount) throws IOException {
        while (byteCount > 0) {
            long read = source.read(this, byteCount);
            if (read == -1) throw new EOFException();
            byteCount -= read;
        }
        return this;
    }

    @Override
    public Buffer writeByte(int b) {
        Segment tail = writableSegment(1);
        tail.data[tail.limit++] = (byte) b;
        size += 1;
        return this;
    }

    @Override
    public Buffer writeShort(int s) {
        Segment tail = writableSegment(2);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte) ((s >>> 8) & 0xff);
        data[limit++] = (byte) (s & 0xff);
        tail.limit = limit;
        size += 2;
        return this;
    }

    @Override
    public Buffer writeShortLe(int s) {
        return writeShort(IoKit.reverseBytesShort((short) s));
    }

    @Override
    public Buffer writeInt(int i) {
        Segment tail = writableSegment(4);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte) ((i >>> 24) & 0xff);
        data[limit++] = (byte) ((i >>> 16) & 0xff);
        data[limit++] = (byte) ((i >>> 8) & 0xff);
        data[limit++] = (byte) (i & 0xff);
        tail.limit = limit;
        size += 4;
        return this;
    }

    @Override
    public Buffer writeIntLe(int i) {
        return writeInt(IoKit.reverseBytesInt(i));
    }

    @Override
    public Buffer writeLong(long v) {
        Segment tail = writableSegment(8);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte) ((v >>> 56L) & 0xff);
        data[limit++] = (byte) ((v >>> 48L) & 0xff);
        data[limit++] = (byte) ((v >>> 40L) & 0xff);
        data[limit++] = (byte) ((v >>> 32L) & 0xff);
        data[limit++] = (byte) ((v >>> 24L) & 0xff);
        data[limit++] = (byte) ((v >>> 16L) & 0xff);
        data[limit++] = (byte) ((v >>> 8L) & 0xff);
        data[limit++] = (byte) (v & 0xff);
        tail.limit = limit;
        size += 8;
        return this;
    }

    @Override
    public Buffer writeLongLe(long v) {
        return writeLong(IoKit.reverseBytesLong(v));
    }

    @Override
    public Buffer writeDecimalLong(long v) {
        if (v == 0) {
            return writeByte(Symbol.C_ZERO);
        }

        boolean negative = false;
        if (v < 0) {
            v = -v;
            if (v < 0) {
                return writeUtf8("-9223372036854775808");
            }
            negative = true;
        }

        int width = v < 100000000L
                ? v < 10000L
                ? v < 100L
                ? v < 10L ? 1 : 2
                : v < 1000L ? 3 : 4
                : v < 1000000L
                ? v < 100000L ? 5 : 6
                : v < 10000000L ? 7 : 8
                : v < 1000000000000L
                ? v < 10000000000L
                ? v < 1000000000L ? 9 : 10
                : v < 100000000000L ? 11 : 12
                : v < 1000000000000000L
                ? v < 10000000000000L ? 13
                : v < 100000000000000L ? 14 : 15
                : v < 100000000000000000L
                ? v < 10000000000000000L ? 16 : 17
                : v < 1000000000000000000L ? 18 : 19;
        if (negative) {
            ++width;
        }

        Segment tail = writableSegment(width);
        byte[] data = tail.data;
        int pos = tail.limit + width;
        while (v != 0) {
            int digit = (int) (v % 10);
            data[--pos] = ByteKit.getBytes(Normal.DIGITS_16_LOWER)[digit];
            v /= 10;
        }
        if (negative) {
            data[--pos] = Symbol.C_MINUS;
        }

        tail.limit += width;
        this.size += width;
        return this;
    }

    @Override
    public Buffer writeHexadecimalUnsignedLong(long v) {
        if (v == 0) {
            return writeByte(Symbol.C_ZERO);
        }

        int width = Long.numberOfTrailingZeros(Long.highestOneBit(v)) / 4 + 1;

        Segment tail = writableSegment(width);
        byte[] data = tail.data;
        for (int pos = tail.limit + width - 1, start = tail.limit; pos >= start; pos--) {
            data[pos] = ByteKit.getBytes(Normal.DIGITS_16_LOWER)[(int) (v & 0xF)];
            v >>>= 4;
        }
        tail.limit += width;
        size += width;
        return this;
    }

    /**
     * @param minimumCapacity int
     * @return segment Segment
     * Returns a tail segment that we can write at least {@code minimumCapacity}
     * bytes to, creating it if necessary.
     */
    public Segment writableSegment(int minimumCapacity) {
        if (minimumCapacity < 1 || minimumCapacity > Segment.SIZE) throw new IllegalArgumentException();

        if (null == head) {
            head = LifeCycle.take(); // Acquire a first segment.
            return head.next = head.prev = head;
        }

        Segment tail = head.prev;
        if (tail.limit + minimumCapacity > Segment.SIZE || !tail.owner) {
            tail = tail.push(LifeCycle.take()); // Append a new empty segment to fill up.
        }
        return tail;
    }

    @Override
    public void write(Buffer source, long byteCount) {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        if (source == this) {
            throw new IllegalArgumentException("source == this");
        }
        IoKit.checkOffsetAndCount(source.size, 0, byteCount);

        while (byteCount > 0) {
            if (byteCount < (source.head.limit - source.head.pos)) {
                Segment tail = null != head ? head.prev : null;
                if (null != tail && tail.owner
                        && (byteCount + tail.limit - (tail.shared ? 0 : tail.pos) <= Segment.SIZE)) {
                    source.head.writeTo(tail, (int) byteCount);
                    source.size -= byteCount;
                    size += byteCount;
                    return;
                } else {
                    source.head = source.head.split((int) byteCount);
                }
            }

            Segment segmentToMove = source.head;
            long movedByteCount = segmentToMove.limit - segmentToMove.pos;
            source.head = segmentToMove.pop();
            if (null == head) {
                head = segmentToMove;
                head.next = head.prev = head;
            } else {
                Segment tail = head.prev;
                tail = tail.push(segmentToMove);
                tail.compact();
            }
            source.size -= movedByteCount;
            size += movedByteCount;
            byteCount -= movedByteCount;
        }
    }

    @Override
    public long read(Buffer sink, long byteCount) {
        if (null == sink) {
            throw new IllegalArgumentException("sink == null");
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (size == 0) {
            return -1L;
        }
        if (byteCount > size) {
            byteCount = size;
        }
        sink.write(this, byteCount);
        return byteCount;
    }

    @Override
    public long indexOf(byte b) {
        return indexOf(b, 0, Long.MAX_VALUE);
    }

    @Override
    public long indexOf(byte b, long fromIndex) {
        return indexOf(b, fromIndex, Long.MAX_VALUE);
    }

    @Override
    public long indexOf(byte b, long fromIndex, long toIndex) {
        if (fromIndex < 0 || toIndex < fromIndex) {
            throw new IllegalArgumentException(
                    String.format("size=%s fromIndex=%s toIndex=%s", size, fromIndex, toIndex));
        }

        if (toIndex > size) toIndex = size;
        if (fromIndex == toIndex) return -1L;

        Segment s;
        long offset;

        findSegmentAndOffset:
        {
            s = head;
            if (null == s) {
                return -1L;
            } else if (size - fromIndex < fromIndex) {
                offset = size;
                while (offset > fromIndex) {
                    s = s.prev;
                    offset -= (s.limit - s.pos);
                }
            } else {
                offset = 0L;
                for (long nextOffset; (nextOffset = offset + (s.limit - s.pos)) < fromIndex; ) {
                    s = s.next;
                    offset = nextOffset;
                }
            }
        }

        while (offset < toIndex) {
            byte[] data = s.data;
            int limit = (int) Math.min(s.limit, s.pos + toIndex - offset);
            int pos = (int) (s.pos + fromIndex - offset);
            for (; pos < limit; pos++) {
                if (data[pos] == b) {
                    return pos - s.pos + offset;
                }
            }

            offset += (s.limit - s.pos);
            fromIndex = offset;
            s = s.next;
        }

        return -1L;
    }

    @Override
    public long indexOf(ByteString bytes) throws IOException {
        return indexOf(bytes, 0);
    }

    @Override
    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        if (bytes.size() == 0) throw new IllegalArgumentException("bytes is empty");
        if (fromIndex < 0) throw new IllegalArgumentException("fromIndex < 0");

        Segment s;
        long offset;

        // TODO(jwilson): extract this to a shared helper method when can do so without allocating.
        findSegmentAndOffset:
        {
            s = head;
            if (null == s) {
                return -1L;
            } else if (size - fromIndex < fromIndex) {
                offset = size;
                while (offset > fromIndex) {
                    s = s.prev;
                    offset -= (s.limit - s.pos);
                }
            } else {
                offset = 0L;
                for (long nextOffset; (nextOffset = offset + (s.limit - s.pos)) < fromIndex; ) {
                    s = s.next;
                    offset = nextOffset;
                }
            }
        }

        byte b0 = bytes.getByte(0);
        int bytesSize = bytes.size();
        long resultLimit = size - bytesSize + 1;
        while (offset < resultLimit) {
            byte[] data = s.data;
            int segmentLimit = (int) Math.min(s.limit, s.pos + resultLimit - offset);
            for (int pos = (int) (s.pos + fromIndex - offset); pos < segmentLimit; pos++) {
                if (data[pos] == b0 && rangeEquals(s, pos + 1, bytes, 1, bytesSize)) {
                    return pos - s.pos + offset;
                }
            }

            offset += (s.limit - s.pos);
            fromIndex = offset;
            s = s.next;
        }

        return -1L;
    }

    @Override
    public long indexOfElement(ByteString targetBytes) {
        return indexOfElement(targetBytes, 0);
    }

    @Override
    public long indexOfElement(ByteString targetBytes, long fromIndex) {
        if (fromIndex < 0) throw new IllegalArgumentException("fromIndex < 0");

        Segment s;
        long offset;

        findSegmentAndOffset:
        {

            s = head;
            if (null == s) {
                return -1L;
            } else if (size - fromIndex < fromIndex) {
                offset = size;
                while (offset > fromIndex) {
                    s = s.prev;
                    offset -= (s.limit - s.pos);
                }
            } else {
                offset = 0L;
                for (long nextOffset; (nextOffset = offset + (s.limit - s.pos)) < fromIndex; ) {
                    s = s.next;
                    offset = nextOffset;
                }
            }
        }

        if (targetBytes.size() == 2) {
            byte b0 = targetBytes.getByte(0);
            byte b1 = targetBytes.getByte(1);
            while (offset < size) {
                byte[] data = s.data;
                for (int pos = (int) (s.pos + fromIndex - offset), limit = s.limit; pos < limit; pos++) {
                    int b = data[pos];
                    if (b == b0 || b == b1) {
                        return pos - s.pos + offset;
                    }
                }

                offset += (s.limit - s.pos);
                fromIndex = offset;
                s = s.next;
            }
        } else {
            byte[] targetByteArray = targetBytes.internalArray();
            while (offset < size) {
                byte[] data = s.data;
                for (int pos = (int) (s.pos + fromIndex - offset), limit = s.limit; pos < limit; pos++) {
                    int b = data[pos];
                    for (byte t : targetByteArray) {
                        if (b == t) return pos - s.pos + offset;
                    }
                }

                offset += (s.limit - s.pos);
                fromIndex = offset;
                s = s.next;
            }
        }

        return -1L;
    }

    @Override
    public boolean rangeEquals(long offset, ByteString bytes) {
        return rangeEquals(offset, bytes, 0, bytes.size());
    }

    @Override
    public boolean rangeEquals(
            long offset, ByteString bytes, int bytesOffset, int byteCount) {
        if (offset < 0
                || bytesOffset < 0
                || byteCount < 0
                || size - offset < byteCount
                || bytes.size() - bytesOffset < byteCount) {
            return false;
        }
        for (int i = 0; i < byteCount; i++) {
            if (getByte(offset + i) != bytes.getByte(bytesOffset + i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the range within this buffer starting at {@code segmentPos} in {@code segment}
     * is equal to {@code bytes[bytesOffset..bytesLimit)}.
     */
    private boolean rangeEquals(
            Segment segment, int segmentPos, ByteString bytes, int bytesOffset, int bytesLimit) {
        int segmentLimit = segment.limit;
        byte[] data = segment.data;

        for (int i = bytesOffset; i < bytesLimit; ) {
            if (segmentPos == segmentLimit) {
                segment = segment.next;
                data = segment.data;
                segmentPos = segment.pos;
                segmentLimit = segment.limit;
            }

            if (data[segmentPos] != bytes.getByte(i)) {
                return false;
            }

            segmentPos++;
            i++;
        }

        return true;
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {
    }

    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }

    List<Integer> segmentSizes() {
        if (null == head) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>();
        result.add(head.limit - head.pos);
        for (Segment s = head.next; s != head; s = s.next) {
            result.add(s.limit - s.pos);
        }
        return result;
    }

    /**
     * @return the 128-bit MD5 hash of this buffer.
     */
    public final ByteString md5() {
        return digest(Algorithm.MD5.getValue());
    }

    /**
     * @return the 160-bit SHA-1 hash of this buffer.
     */
    public final ByteString sha1() {
        return digest(Algorithm.SHA1.getValue());
    }

    /**
     * @return the 256-bit SHA-256 hash of this buffer.
     */
    public final ByteString sha256() {
        return digest(Algorithm.SHA256.getValue());
    }

    /**
     * @return the 512-bit SHA-512 hash of this buffer.
     */
    public final ByteString sha512() {
        return digest(Algorithm.SHA512.getValue());
    }

    private ByteString digest(String algorithm) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            if (null != head) {
                messageDigest.update(head.data, head.pos, head.limit - head.pos);
                for (Segment s = head.next; s != head; s = s.next) {
                    messageDigest.update(s.data, s.pos, s.limit - s.pos);
                }
            }
            return ByteString.of(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    /**
     * @param key ByteString
     * @return the 160-bit SHA-1 HMAC of this buffer.
     */
    public final ByteString hmacSha1(ByteString key) {
        return hmac(Algorithm.HMACSHA1.getValue(), key);
    }

    /**
     * @param key ByteString
     * @return the 256-bit SHA-256 HMAC of this buffer.
     */
    public final ByteString hmacSha256(ByteString key) {
        return hmac(Algorithm.HMACSHA256.getValue(), key);
    }

    /**
     * @param key ByteString
     * @return the 512-bit SHA-512 HMAC of this buffer.
     */
    public final ByteString hmacSha512(ByteString key) {
        return hmac(Algorithm.HMACSHA512.getValue(), key);
    }

    private ByteString hmac(String algorithm, ByteString key) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
            if (null != head) {
                mac.update(head.data, head.pos, head.limit - head.pos);
                for (Segment s = head.next; s != head; s = s.next) {
                    mac.update(s.data, s.pos, s.limit - s.pos);
                }
            }
            return ByteString.of(mac.doFinal());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Buffer)) return false;
        Buffer that = (Buffer) o;
        if (size != that.size) return false;
        if (size == 0) return true;

        Segment sa = this.head;
        Segment sb = that.head;
        int posA = sa.pos;
        int posB = sb.pos;

        for (long pos = 0, count; pos < size; pos += count) {
            count = Math.min(sa.limit - posA, sb.limit - posB);

            for (int i = 0; i < count; i++) {
                if (sa.data[posA++] != sb.data[posB++]) return false;
            }

            if (posA == sa.limit) {
                sa = sa.next;
                posA = sa.pos;
            }

            if (posB == sb.limit) {
                sb = sb.next;
                posB = sb.pos;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        Segment s = head;
        if (null == s) {
            return 0;
        }
        int result = 1;
        do {
            for (int pos = s.pos, limit = s.limit; pos < limit; pos++) {
                result = 31 * result + s.data[pos];
            }
            s = s.next;
        } while (s != head);
        return result;
    }

    @Override
    public String toString() {
        return snapshot().toString();
    }

    @Override
    public Buffer clone() {
        Buffer result = new Buffer();
        if (size == 0) return result;

        result.head = head.sharedCopy();
        result.head.next = result.head.prev = result.head;
        for (Segment s = head.next; s != head; s = s.next) {
            result.head.prev.push(s.sharedCopy());
        }
        result.size = size;
        return result;
    }

    public final ByteString snapshot() {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("size > Integer.MAX_VALUE: " + size);
        }
        return snapshot((int) size);
    }

    public final ByteString snapshot(int byteCount) {
        if (byteCount == 0) return ByteString.EMPTY;
        return new ByteBuffer(this, byteCount);
    }

    public final UnsafeCursor readUnsafe() {
        return readUnsafe(new UnsafeCursor());
    }

    public final UnsafeCursor readUnsafe(UnsafeCursor unsafeCursor) {
        if (null != unsafeCursor.buffer) {
            throw new IllegalStateException("already attached to a buffer");
        }

        unsafeCursor.buffer = this;
        unsafeCursor.readWrite = false;
        return unsafeCursor;
    }

    public final UnsafeCursor readAndWriteUnsafe() {
        return readAndWriteUnsafe(new UnsafeCursor());
    }

    public final UnsafeCursor readAndWriteUnsafe(UnsafeCursor unsafeCursor) {
        if (null != unsafeCursor.buffer) {
            throw new IllegalStateException("already attached to a buffer");
        }

        unsafeCursor.buffer = this;
        unsafeCursor.readWrite = true;
        return unsafeCursor;
    }

    public static final class UnsafeCursor implements Closeable {
        public Buffer buffer;
        public boolean readWrite;
        public long offset = -1L;
        public byte[] data;
        public int start = -1;
        public int end = -1;
        private Segment segment;

        public final int next() {
            if (offset == buffer.size) throw new IllegalStateException();
            if (offset == -1L) return seek(0L);
            return seek(offset + (end - start));
        }

        public final int seek(long offset) {
            if (offset < -1 || offset > buffer.size) {
                throw new ArrayIndexOutOfBoundsException(
                        String.format("offset=%s > size=%s", offset, buffer.size));
            }

            if (offset == -1 || offset == buffer.size) {
                this.segment = null;
                this.offset = offset;
                this.data = null;
                this.start = -1;
                this.end = -1;
                return -1;
            }

            // Navigate to the segment that contains `offset`. Start from our current segment if possible.
            long min = 0L;
            long max = buffer.size;
            Segment head = buffer.head;
            Segment tail = buffer.head;
            if (null != this.segment) {
                long segmentOffset = this.offset - (this.start - this.segment.pos);
                if (segmentOffset > offset) {
                    // Set the cursor segment to be the 'end'
                    max = segmentOffset;
                    tail = this.segment;
                } else {
                    // Set the cursor segment to be the 'beginning'
                    min = segmentOffset;
                    head = this.segment;
                }
            }

            Segment next;
            long nextOffset;
            if (max - offset > offset - min) {
                // Start at the 'beginning' and search forwards
                next = head;
                nextOffset = min;
                while (offset >= nextOffset + (next.limit - next.pos)) {
                    nextOffset += (next.limit - next.pos);
                    next = next.next;
                }
            } else {
                // Start at the 'end' and search backwards
                next = tail;
                nextOffset = max;
                while (nextOffset > offset) {
                    next = next.prev;
                    nextOffset -= (next.limit - next.pos);
                }
            }

            // If we're going to write and our segment is shared, swap it for a read-write first.
            if (readWrite && next.shared) {
                Segment unsharedNext = next.unsharedCopy();
                if (buffer.head == next) {
                    buffer.head = unsharedNext;
                }
                next = next.push(unsharedNext);
                next.prev.pop();
            }

            this.segment = next;
            this.offset = offset;
            this.data = next.data;
            this.start = next.pos + (int) (offset - nextOffset);
            this.end = next.limit;
            return end - start;
        }

        public final long resizeBuffer(long newSize) {
            if (null == buffer) {
                throw new IllegalStateException("not attached to a buffer");
            }
            if (!readWrite) {
                throw new IllegalStateException("resizeBuffer() only permitted for read/write buffers");
            }

            long oldSize = buffer.size;
            if (newSize <= oldSize) {
                if (newSize < 0) {
                    throw new IllegalArgumentException("newSize < 0: " + newSize);
                }
                // Shrink the buffer by either shrinking segments or removing them.
                for (long bytesToSubtract = oldSize - newSize; bytesToSubtract > 0; ) {
                    Segment tail = buffer.head.prev;
                    int tailSize = tail.limit - tail.pos;
                    if (tailSize <= bytesToSubtract) {
                        buffer.head = tail.pop();
                        LifeCycle.recycle(tail);
                        bytesToSubtract -= tailSize;
                    } else {
                        tail.limit -= bytesToSubtract;
                        break;
                    }
                }
                // Seek to the end.
                this.segment = null;
                this.offset = newSize;
                this.data = null;
                this.start = -1;
                this.end = -1;
            } else if (newSize > oldSize) {
                // Enlarge the buffer by either enlarging segments or adding them.
                boolean needsToSeek = true;
                for (long bytesToAdd = newSize - oldSize; bytesToAdd > 0; ) {
                    Segment tail = buffer.writableSegment(1);
                    int segmentBytesToAdd = (int) Math.min(bytesToAdd, Segment.SIZE - tail.limit);
                    tail.limit += segmentBytesToAdd;
                    bytesToAdd -= segmentBytesToAdd;

                    // If this is the first segment we're adding, seek to it.
                    if (needsToSeek) {
                        this.segment = tail;
                        this.offset = oldSize;
                        this.data = tail.data;
                        this.start = tail.limit - segmentBytesToAdd;
                        this.end = tail.limit;
                        needsToSeek = false;
                    }
                }
            }

            buffer.size = newSize;

            return oldSize;
        }

        public final long expandBuffer(int minByteCount) {
            if (minByteCount <= 0) {
                throw new IllegalArgumentException("minByteCount <= 0: " + minByteCount);
            }
            if (minByteCount > Segment.SIZE) {
                throw new IllegalArgumentException("minByteCount > Segment.SIZE: " + minByteCount);
            }
            if (null == buffer) {
                throw new IllegalStateException("not attached to a buffer");
            }
            if (!readWrite) {
                throw new IllegalStateException("expandBuffer() only permitted for read/write buffers");
            }

            long oldSize = buffer.size;
            Segment tail = buffer.writableSegment(minByteCount);
            int result = Segment.SIZE - tail.limit;
            tail.limit = Segment.SIZE;
            buffer.size = oldSize + result;

            // Seek to the old size.
            this.segment = tail;
            this.offset = oldSize;
            this.data = tail.data;
            this.start = Segment.SIZE - result;
            this.end = Segment.SIZE;

            return result;
        }

        @Override
        public void close() {
            if (null == buffer) {
                throw new IllegalStateException("not attached to a buffer");
            }

            buffer = null;
            segment = null;
            offset = -1L;
            data = null;
            start = -1;
            end = -1;
        }
    }

}
