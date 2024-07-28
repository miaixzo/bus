/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.sink;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

/**
 * 一种接收器,它在内部保存缓冲区, 以便调用者可以进行小的写操作没有性能损失
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BufferSink extends Sink, WritableByteChannel {

    /**
     * Returns this sink's internal buffer.
     */
    Buffer buffer();

    BufferSink write(ByteString byteString) throws IOException;

    /**
     * Like {@link OutputStream#write(byte[])}, this writes a complete byte array to this sink.
     */
    BufferSink write(byte[] source) throws IOException;

    /**
     * Like {@link OutputStream#write(byte[], int, int)}, this writes {@code byteCount} bytes of {@code source},
     * starting at {@code offset}.
     */
    BufferSink write(byte[] source, int offset, int byteCount) throws IOException;

    /**
     * Removes all bytes from {@code source} and appends them to this sink. Returns the number of bytes read which will
     * be 0 if {@code source} is exhausted.
     */
    long writeAll(Source source) throws IOException;

    /**
     * Removes {@code byteCount} bytes from {@code source} and appends them to this sink.
     */
    BufferSink write(Source source, long byteCount) throws IOException;

    /**
     * Encodes {@code string} in UTF-8 and writes it to this sink.
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeUtf8("Uh uh uh!");
     * buffer.writeByte(' ');
     * buffer.writeUtf8("You didn't say the magic word!");
     *
     * assertEquals("Uh uh uh! You didn't say the magic word!", buffer.readUtf8());
     * }</pre>
     */
    BufferSink writeUtf8(String string) throws IOException;

    /**
     * Encodes the characters at {@code beginIndex} up to {@code endIndex} from {@code string} in UTF-8 and writes it to
     * this sink.
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeUtf8("I'm a hacker!\n", 6, 12);
     * buffer.writeByte(' ');
     * buffer.writeUtf8("That's what I said: you're a nerd.\n", 29, 33);
     * buffer.writeByte(' ');
     * buffer.writeUtf8("I prefer to be called a hacker!\n", 24, 31);
     *
     * assertEquals("hacker nerd hacker!", buffer.readUtf8());
     * }</pre>
     */
    BufferSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException;

    /**
     * Encodes {@code codePoint} in UTF-8 and writes it to this sink.
     */
    BufferSink writeUtf8CodePoint(int codePoint) throws IOException;

    /**
     * Encodes {@code string} in {@code charset} and writes it to this sink.
     */
    BufferSink writeString(String string, Charset charset) throws IOException;

    /**
     * Encodes the characters at {@code beginIndex} up to {@code endIndex} from {@code string} in {@code charset} and
     * writes it to this sink.
     */
    BufferSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException;

    /**
     * Writes a byte to this sink.
     */
    BufferSink writeByte(int b) throws IOException;

    /**
     * Writes a big-endian short to this sink using two bytes.
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeShort(32767);
     * buffer.writeShort(15);
     *
     * assertEquals(4, buffer.size());
     * assertEquals((byte) 0x7f, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x0f, buffer.readByte());
     * assertEquals(0, buffer.size());
     * }</pre>
     */
    BufferSink writeShort(int s) throws IOException;

    /**
     * Writes a little-endian short to this sink using two bytes.
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeShortLe(32767);
     * buffer.writeShortLe(15);
     *
     * assertEquals(4, buffer.size());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0x7f, buffer.readByte());
     * assertEquals((byte) 0x0f, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals(0, buffer.size());
     * }</pre>
     */
    BufferSink writeShortLe(int s) throws IOException;

    /**
     * Writes a big-endian int to this sink using four bytes.
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeInt(2147483647);
     * buffer.writeInt(15);
     *
     * assertEquals(8, buffer.size());
     * assertEquals((byte) 0x7f, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x0f, buffer.readByte());
     * assertEquals(0, buffer.size());
     * }</pre>
     */
    BufferSink writeInt(int i) throws IOException;

    /**
     * Writes a little-endian int to this sink using four bytes.
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeIntLe(2147483647);
     * buffer.writeIntLe(15);
     *
     * assertEquals(8, buffer.size());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0x7f, buffer.readByte());
     * assertEquals((byte) 0x0f, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals(0, buffer.size());
     * }</pre>
     */
    BufferSink writeIntLe(int i) throws IOException;

    /**
     * Writes a big-endian long to this sink using eight bytes.
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeLong(9223372036854775807L);
     * buffer.writeLong(15);
     *
     * assertEquals(16, buffer.size());
     * assertEquals((byte) 0x7f, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x0f, buffer.readByte());
     * assertEquals(0, buffer.size());
     * }</pre>
     */
    BufferSink writeLong(long v) throws IOException;

    /**
     * Writes a little-endian long to this sink using eight bytes.
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeLongLe(9223372036854775807L);
     * buffer.writeLongLe(15);
     *
     * assertEquals(16, buffer.size());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0xff, buffer.readByte());
     * assertEquals((byte) 0x7f, buffer.readByte());
     * assertEquals((byte) 0x0f, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals((byte) 0x00, buffer.readByte());
     * assertEquals(0, buffer.size());
     * }</pre>
     */
    BufferSink writeLongLe(long v) throws IOException;

    /**
     * Writes a long to this sink in signed decimal form (i.e., as a string in base 10).
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeDecimalLong(8675309L);
     * buffer.writeByte(' ');
     * buffer.writeDecimalLong(-123L);
     * buffer.writeByte(' ');
     * buffer.writeDecimalLong(1L);
     *
     * assertEquals("8675309 -123 1", buffer.readUtf8());
     * }</pre>
     */
    BufferSink writeDecimalLong(long v) throws IOException;

    /**
     * Writes a long to this sink in hexadecimal form (i.e., as a string in base 16).
     * 
     * <pre>{@code
     *
     * Buffer buffer = new Buffer();
     * buffer.writeHexadecimalUnsignedLong(65535L);
     * buffer.writeByte(' ');
     * buffer.writeHexadecimalUnsignedLong(0xcafebabeL);
     * buffer.writeByte(' ');
     * buffer.writeHexadecimalUnsignedLong(0x10L);
     *
     * assertEquals("ffff cafebabe 10", buffer.readUtf8());
     * }</pre>
     */
    BufferSink writeHexadecimalUnsignedLong(long v) throws IOException;

    /**
     * Writes all buffered data to the underlying sink, if one exists. Then that sink is recursively flushed which
     * pushes data as far as possible towards its ultimate destination. Typically that destination is a network socket
     * or file.
     * 
     * <pre>{@code
     *
     * BufferedSink b0 = new Buffer();
     * BufferedSink b1 = Okio.buffer(b0);
     * BufferedSink b2 = Okio.buffer(b1);
     *
     * b2.writeUtf8("hello");
     * assertEquals(5, b2.buffer().size());
     * assertEquals(0, b1.buffer().size());
     * assertEquals(0, b0.buffer().size());
     *
     * b2.flush();
     * assertEquals(0, b2.buffer().size());
     * assertEquals(0, b1.buffer().size());
     * assertEquals(5, b0.buffer().size());
     * }</pre>
     */
    @Override
    void flush() throws IOException;

    /**
     * Writes all buffered data to the underlying sink, if one exists. Like {@link #flush}, but weaker. Call this before
     * this buffered sink goes out of scope so that its data can reach its destination.
     * 
     * <pre>{@code
     *
     * BufferedSink b0 = new Buffer();
     * BufferedSink b1 = Okio.buffer(b0);
     * BufferedSink b2 = Okio.buffer(b1);
     *
     * b2.writeUtf8("hello");
     * assertEquals(5, b2.buffer().size());
     * assertEquals(0, b1.buffer().size());
     * assertEquals(0, b0.buffer().size());
     *
     * b2.emit();
     * assertEquals(0, b2.buffer().size());
     * assertEquals(5, b1.buffer().size());
     * assertEquals(0, b0.buffer().size());
     *
     * b1.emit();
     * assertEquals(0, b2.buffer().size());
     * assertEquals(0, b1.buffer().size());
     * assertEquals(5, b0.buffer().size());
     * }</pre>
     */
    BufferSink emit() throws IOException;

    /**
     * Writes complete segments to the underlying sink, if one exists. Like {@link #flush}, but weaker. Use this to
     * limit the memory held in the buffer to a single segment. Typically application code will not need to call this:
     * it is only necessary when application code writes directly to this {@linkplain #buffer() sink's buffer}.
     * 
     * <pre>{@code
     *
     * BufferedSink b0 = new Buffer();
     * BufferedSink b1 = Okio.buffer(b0);
     * BufferedSink b2 = Okio.buffer(b1);
     *
     * b2.buffer().write(new byte[20_000]);
     * assertEquals(20_000, b2.buffer().size());
     * assertEquals(0, b1.buffer().size());
     * assertEquals(0, b0.buffer().size());
     *
     * b2.emitCompleteSegments();
     * assertEquals(3_616, b2.buffer().size());
     * assertEquals(0, b1.buffer().size());
     * assertEquals(16_384, b0.buffer().size()); // This example assumes 8192 byte segments.
     * }</pre>
     */
    BufferSink emitCompleteSegments() throws IOException;

    /**
     * Returns an output stream that writes to this sink.
     */
    OutputStream outputStream();

}
