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
package org.miaixz.bus.core.io.source;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SegmentBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

/**
 * 内部保存一个缓冲区,以便调用者可以在没有性能的情况下进行少量读取
 * 它还允许客户端提前读取,在消费之前进行必要的缓冲输入
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BufferSource extends Source, ReadableByteChannel {

    /**
     * 该源的内部缓冲区
     *
     * @return {@link Buffer}
     */
    Buffer getBuffer();

    /**
     * 如果此源中没有更多字节，则返回 true。
     * 这将阻塞，直到有字节可读取或源确实已耗尽
     *
     * @return the true/false
     * @throws IOException 异常
     */
    boolean exhausted() throws IOException;

    /**
     * 当缓冲区至少包含 {@code byteCount} 个字节时返回。
     * 如果在读取所需字节之前源已耗尽，则抛出 {@link java.io.EOFException}。
     *
     * @param byteCount 字节数
     * @throws IOException 异常
     */
    void require(long byteCount) throws IOException;

    /**
     * 如果缓冲区至少包含 {@code byteCount} 个字节，则返回 true，并根据需要对其进行扩展。
     * 如果在读取请求的字节之前源已耗尽，则返回 false。
     *
     * @param byteCount 字节数
     * @return the true/false
     * @throws IOException 异常
     */
    boolean request(long byteCount) throws IOException;

    /**
     * 从该源中删除一个字节并返回它
     *
     * @return the true/false
     * @throws IOException 异常
     */
    byte readByte() throws IOException;

    /**
     * 从此源中删除两个字节并返回一个短整型
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeByte(0x7f)
     *       .writeByte(0xff)
     *       .writeByte(0x00)
     *       .writeByte(0x0f);
     *   assertEquals(4, buffer.size());
     *
     *   assertEquals(32767, buffer.readShort());
     *   assertEquals(2, buffer.size());
     *
     *   assertEquals(15, buffer.readShort());
     *   assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return the short
     * @throws IOException 异常
     */
    short readShort() throws IOException;

    /**
     * 从此源中删除两个字节并返回一个整型
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeByte(0xff)
     *       .writeByte(0x7f)
     *       .writeByte(0x0f)
     *       .writeByte(0x00);
     *   assertEquals(4, buffer.size());
     *
     *   assertEquals(32767, buffer.readShortLe());
     *   assertEquals(2, buffer.size());
     *
     *   assertEquals(15, buffer.readShortLe());
     *   assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return the short
     * @throws IOException 异常
     */
    short readShortLe() throws IOException;

    /**
     * 从此源中删除四个字节并返回一个大整数
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeByte(0x7f)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x0f);
     *   assertEquals(8, buffer.size());
     *
     *   assertEquals(2147483647, buffer.readInt());
     *   assertEquals(4, buffer.size());
     *
     *   assertEquals(15, buffer.readInt());
     *   assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return the int
     */
    int readInt() throws IOException;

    /**
     * 从该源中删除四个字节并返回一个小整数
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0x7f)
     *       .writeByte(0x0f)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00);
     *   assertEquals(8, buffer.size());
     *
     *   assertEquals(2147483647, buffer.readIntLe());
     *   assertEquals(4, buffer.size());
     *
     *   assertEquals(15, buffer.readIntLe());
     *   assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return the int
     */
    int readIntLe() throws IOException;

    /**
     * 该源中删除八个字节并返回一个大长整型
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeByte(0x7f)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x0f);
     *   assertEquals(16, buffer.size());
     *
     *   assertEquals(9223372036854775807L, buffer.readLong());
     *   assertEquals(8, buffer.size());
     *
     *   assertEquals(15, buffer.readLong());
     *   assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return the long
     */
    long readLong() throws IOException;

    /**
     * 从此源中删除八个字节并返回一个小长整型
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0xff)
     *       .writeByte(0x7f)
     *       .writeByte(0x0f)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00)
     *       .writeByte(0x00);
     *   assertEquals(16, buffer.size());
     *
     *   assertEquals(9223372036854775807L, buffer.readLongLe());
     *   assertEquals(8, buffer.size());
     *
     *   assertEquals(15, buffer.readLongLe());
     *   assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return the long
     */
    long readLongLe() throws IOException;

    /**
     * 以有符号十进制形式从此源读取一个长整型值（即以十进制为基数的字符串，前导字符可选为“-”）。此操作将不断迭代，直到找到非数字字符。
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeUtf8("8675309 -123 00001");
     *
     *   assertEquals(8675309L, buffer.readDecimalLong());
     *   assertEquals(' ', buffer.readByte());
     *   assertEquals(-123L, buffer.readDecimalLong());
     *   assertEquals(' ', buffer.readByte());
     *   assertEquals(1L, buffer.readDecimalLong());
     * }</pre>
     *
     * @return the long
     * @throws NumberFormatException 如果找到的数字不适合 {@code long} 或不存在十进制数。
     */
    long readDecimalLong() throws IOException;

    /**
     * 以十六进制形式（即以 16 进制表示的字符串）读取此源的长格式。此过程将不断迭代，直到找到非十六进制字符
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeUtf8("ffff CAFEBABE 10");
     *
     *   assertEquals(65535L, buffer.readHexadecimalUnsignedLong());
     *   assertEquals(' ', buffer.readByte());
     *   assertEquals(0xcafebabeL, buffer.readHexadecimalUnsignedLong());
     *   assertEquals(' ', buffer.readByte());
     *   assertEquals(0x10L, buffer.readHexadecimalUnsignedLong());
     * }</pre>
     *
     * @return the long
     * @throws NumberFormatException 如果找到的十六进制数不适合 {@code long} 或未找到十六进制数。
     */
    long readHexadecimalUnsignedLong() throws IOException;

    /**
     * 从此源读取并丢弃 {@code byteCount} 个字节
     *
     * @param byteCount 字节数
     * @throws IOException 如果在跳过请求的字节之前源已耗尽，则抛出。
     */
    void skip(long byteCount) throws IOException;

    /**
     * 从中删除所有字节并将它们作为字节字符串返回
     *
     * @return the {@link ByteString}
     * @throws IOException 异常
     */
    ByteString readByteString() throws IOException;

    /**
     * 从中删除 {@code byteCount} 个字节并将其作为字节字符串返回。
     *
     * @return the {@link ByteString}
     * @throws IOException 异常
     */
    ByteString readByteString(long byteCount) throws IOException;

    /**
     * 在{@code options} 中查找第一个作为此缓冲区前缀的字符串，从此缓冲区中使用它，并返回其索引。
     * 如果 {@code options} 中没有字节字符串是此缓冲区的前缀，则返回 -1，并且不消耗任何字节。
     *
     * <p>如果事先知道预期值集，则可以将其用作 {@link #readByteString} 甚至 {@link #readUtf8} 的替代。
     * <pre>{@code
     *   Options FIELDS = Options.of(
     *       ByteString.encodeUtf8("depth="),
     *       ByteString.encodeUtf8("height="),
     *       ByteString.encodeUtf8("width="));
     *
     *   Buffer buffer = new Buffer()
     *       .writeUtf8("width=640\n")
     *       .writeUtf8("height=480\n");
     *
     *   assertEquals(2, buffer.select(FIELDS));
     *   assertEquals(640, buffer.readDecimalLong());
     *   assertEquals('\n', buffer.readByte());
     *   assertEquals(1, buffer.select(FIELDS));
     *   assertEquals(480, buffer.readDecimalLong());
     *   assertEquals('\n', buffer.readByte());
     * }</pre>
     *
     * @param segmentBuffer 索引值
     * @return the int
     * @throws IOException 异常
     */
    int select(SegmentBuffer segmentBuffer) throws IOException;

    /**
     * 从中删除所有字节并将它们作为字节数组返回。
     *
     * @return the byte
     * @throws IOException 异常
     */
    byte[] readByteArray() throws IOException;

    /**
     * 从中删除 {@code byteCount} 个字节并将其作为字节数组返回。
     *
     * @return the byte
     * @throws IOException 异常
     */
    byte[] readByteArray(long byteCount) throws IOException;

    /**
     * 从中移除最多 {@code sink.length} 个字节并将其复制到 {@code sink}。
     * 返回读取的字节数，如果此源已耗尽，则返回 -1。
     *
     * @param sink 字节集合
     * @return the int
     * @throws IOException 异常
     */
    int read(byte[] sink) throws IOException;

    /**
     * 从中删除恰好 {@code sink.length} 个字节并将其复制到 {@code sink}。
     * 如果无法读取请求的字节数，则抛出 {@link java.io.EOFException}。
     *
     * @param sink 字节集合
     * @throws IOException 异常
     */
    void readFully(byte[] sink) throws IOException;

    /**
     * 从此处删除最多 {@code byteCount} 个字节并将其复制到 {@code offset} 处的 {@code sink}。
     * 返回读取的字节数，如果此源已耗尽，则返回 -1。
     *
     * @param sink      字节集合
     * @param offset    偏移位
     * @param byteCount 字节数
     * @return the int
     * @throws IOException 异常
     */
    int read(byte[] sink, int offset, int byteCount) throws IOException;

    /**
     * 从中删除精确的 {@code byteCount} 个字节并将其附加到 {@code sink}。
     * 如果无法读取请求的字节数，则抛出 {@link java.io.EOFException}。
     *
     * @param sink      字节集合
     * @param byteCount 字节数
     * @throws IOException 异常
     */
    void readFully(Buffer sink, long byteCount) throws IOException;

    /**
     * 从中删除所有字节并将其附加到 {@code sink}。返回写入 {@code sink} 的总字节数，如果已用尽，则为 0。
     *
     * @param sink 字节集合
     * @return the long
     * @throws IOException 异常
     */
    long readAll(Sink sink) throws IOException;

    /**
     * 从中删除所有字节，将其解码为 UTF-8，然后返回字符串。如果此源为空，则返回空字符串。
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeUtf8("Uh uh uh!")
     *       .writeByte(' ')
     *       .writeUtf8("You didn't say the magic word!");
     *
     *   assertEquals("Uh uh uh! You didn't say the magic word!", buffer.readUtf8());
     *   assertEquals(0, buffer.size());
     *
     *   assertEquals("", buffer.readUtf8());
     *   assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return the string
     * @throws IOException 异常
     */
    String readUtf8() throws IOException;

    /**
     * 从中删除 {@code byteCount} 个字节，将其解码为 UTF-8，并返回字符串。
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeUtf8("Uh uh uh!")
     *       .writeByte(' ')
     *       .writeUtf8("You didn't say the magic word!");
     *   assertEquals(40, buffer.size());
     *
     *   assertEquals("Uh uh uh! You ", buffer.readUtf8(14));
     *   assertEquals(26, buffer.size());
     *
     *   assertEquals("didn't say the", buffer.readUtf8(14));
     *   assertEquals(12, buffer.size());
     *
     *   assertEquals(" magic word!", buffer.readUtf8(12));
     *   assertEquals(0, buffer.size());
     * }</pre>
     *
     * @param byteCount 字节数
     * @return the string
     * @throws IOException 异常
     */
    String readUtf8(long byteCount) throws IOException;

    /**
     * 删除并返回直到下一个换行符（但不包括该换行符）的字符。
     * 换行符为 {@code "\n"} 或 {@code "\r\n"}；这些字符不包含在结果中。
     * <pre>{@code
     *   Buffer buffer = new Buffer()
     *       .writeUtf8("I'm a hacker!\n")
     *       .writeUtf8("That's what I said: you're a nerd.\n")
     *       .writeUtf8("I prefer to be called a hacker!\n");
     *   assertEquals(81, buffer.size());
     *
     *   assertEquals("I'm a hacker!", buffer.readUtf8Line());
     *   assertEquals(67, buffer.size());
     *
     *   assertEquals("That's what I said: you're a nerd.", buffer.readUtf8Line());
     *   assertEquals(32, buffer.size());
     *
     *   assertEquals("I prefer to be called a hacker!", buffer.readUtf8Line());
     *   assertEquals(0, buffer.size());
     *
     *   assertEquals(null, buffer.readUtf8Line());
     *   assertEquals(0, buffer.size());
     * }</pre>
     *
     * <strong>在流的末尾，此方法返回 null，</strong> 就像 {@link java.io.BufferedReader} 一样。
     * 如果源未以换行符结尾，则假定为隐式换行符。一旦源耗尽，将返回 Null。将此方法用于人工生成的数据，其中尾随换行符是可选的。
     *
     * @return the string
     * @throws IOException 异常
     */
    String readUtf8Line() throws IOException;

    /**
     * 删除并返回直到下一个换行符（但不包括该换行符）的字符。
     * 换行符为 {@code "\n"} 或 {@code "\r\n"}；这些字符不包含在结果中
     *
     * @return the string
     * @throws IOException 异常
     */
    String readUtf8LineStrict() throws IOException;

    /**
     * 与 {@link #readUtf8LineStrict()} 类似，不同之处在于它允许调用者指定允许的最长匹配。
     * 使用它来防止可能不包含 {@code "\n"} 或 {@code "\r\n"} 的流。
     * <pre>{@code
     *   Buffer buffer = new Buffer();
     *   buffer.writeUtf8("12345\r\n");
     *
     *   // This will throw! There must be \r\n or \n at the limit or before it.
     *   buffer.readUtf8LineStrict(4);
     *
     *   // No bytes have been consumed so the caller can retry.
     *   assertEquals("12345", buffer.readUtf8LineStrict(5));
     * }</pre>
     *
     * @param limit 限制
     * @return the string
     * @throws IOException 异常
     */
    String readUtf8LineStrict(long limit) throws IOException;

    /**
     * 删除并返回单个 UTF-8 代码点，根据需要读取 1 到 4 个字节。
     * <p>
     * 如果此源不是以正确编码的 UTF-8 代码点开头，则此方法将删除 1 个或多个非 UTF-8 字节并返回替换字符 ({@code U+FFFD})。
     * 这包括编码问题（输入不是正确编码的 UTF-8）、字符超出范围（超出 Unicode 的 0x10ffff 限制）、UTF-16 代理的代码
     * 点 (U+d800..U+dfff) 和过长编码（例如，修改版 UTF-8 中的 NUL 字符为 {@code 0xc080}）
     *
     * @return the int
     * @throws IOException 异常
     */
    int readUtf8CodePoint() throws IOException;

    /**
     * 从中删除所有字节，将其解码为{@code charset}，并返回字符串。
     *
     * @param charset 字符编码
     * @return the string
     * @throws IOException 异常
     */
    String readString(Charset charset) throws IOException;

    /**
     * 从中删除 {@code byteCount} 个字节，将其解码为 {@code charset}，并返回字符串。
     *
     * @param byteCount 字节数
     * @param charset   字符编码
     * @return the string
     * @throws IOException 异常
     */
    String readString(long byteCount, Charset charset) throws IOException;

    /**
     * Equivalent to {@link #indexOf(byte, long) indexOf(b, 0)}.
     *
     * @param b
     * @return the long
     * @throws IOException 异常
     */
    long indexOf(byte b) throws IOException;

    /**
     * 返回缓冲区中第一个 {@code b} 的索引，位于 {@code fromIndex} 处或之后。
     * 这会根据需要扩展缓冲区，直到找到 {@code b}。这会将无限数量的字节读入缓冲区。如果在找到请求的字节之前流已耗尽，则返回 -1。
     * <pre>{@code
     *   Buffer buffer = new Buffer();
     *   buffer.writeUtf8("Don't move! He can't see us if we don't move.");
     *
     *   byte m = 'm';
     *   assertEquals(6,  buffer.indexOf(m));
     *   assertEquals(40, buffer.indexOf(m, 12));
     * }</pre>
     *
     * @param b
     * @param fromIndex
     * @return the long
     * @throws IOException 异常
     */
    long indexOf(byte b, long fromIndex) throws IOException;

    /**
     * 如果在 {@code fromIndex} 到 {@code toIndex} 范围内找到 {@code b}，则返回其索引。
     * 如果未找到 {@code b}，或者 {@code fromIndex == toIndex}，则返回 -1。
     *
     * @param b         字节
     * @param fromIndex 开始索引
     * @param toIndex   目标索引
     * @return the long
     * @throws IOException 异常
     */
    long indexOf(byte b, long fromIndex, long toIndex) throws IOException;

    /**
     * 相当于{@link #indexOf(ByteString, long) indexOf(bytes, 0)}。
     *
     * @param bytes 字节
     * @return the long
     * @throws IOException 异常
     */
    long indexOf(ByteString bytes) throws IOException;

    /**
     * 返回缓冲区中 {@code bytes} 的第一个匹配项的索引，位于 {@code fromIndex} 处或之后。
     * 这会根据需要扩展缓冲区，直到找到 {@code bytes}。这会将无限数量的字节读入缓冲区。如果在找到请求的字节之前流已耗尽，则返回 -1。
     * <pre>{@code
     *   ByteString MOVE = ByteString.encodeUtf8("move");
     *   Buffer buffer = new Buffer();
     *   buffer.writeUtf8("Don't move! He can't see us if we don't move.");
     *
     *   assertEquals(6,  buffer.indexOf(MOVE));
     *   assertEquals(40, buffer.indexOf(MOVE, 12));
     * }</pre>
     *
     * @param bytes     字节
     * @param fromIndex 字节索引
     * @return the long
     * @throws IOException 异常
     */
    long indexOf(ByteString bytes, long fromIndex) throws IOException;

    /**
     * 相当于{@link #indexOfElement(ByteString, long) indexOfElement(targetBytes, 0)}。
     *
     * @param targetBytes 目标字节
     * @return the long
     * @throws IOException 异常
     */
    long indexOfElement(ByteString targetBytes) throws IOException;

    /**
     * 返回此缓冲区中位于 {@code fromIndex} 或之后且包含 {@code targetBytes} 中任意字节的第一个索引。
     * 这会根据需要扩展缓冲区，直到找到目标字节。这会将无限数量的字节读入缓冲区。如果在找到请求的字节之前流已耗尽，则返回 -1。
     * <pre>{@code
     *   ByteString ANY_VOWEL = ByteString.encodeUtf8("AEOIUaeoiu");
     *   Buffer buffer = new Buffer();
     *   buffer.writeUtf8("Dr. Alan Grant");
     *
     *   assertEquals(4,  buffer.indexOfElement(ANY_VOWEL));    // 'A' in 'Alan'.
     *   assertEquals(11, buffer.indexOfElement(ANY_VOWEL, 9)); // 'a' in 'Grant'.
     * }</pre>
     *
     * @param targetBytes 目标字节
     * @param fromIndex   开始索引
     * @return the long
     * @throws IOException 异常
     */
    long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException;

    /**
     * 如果此源中 {@code offset} 处的字节等于 {@code bytes}，则返回 true。
     * 这会根据需要扩展缓冲区，直到某个字节不匹配、所有字节都匹配，或者在足够的字节确定匹配之前流已耗尽。
     * <pre>{@code
     *   ByteString simonSays = ByteString.encodeUtf8("Simon says:");
     *   Buffer standOnOneLeg = new Buffer().writeUtf8("Simon says: Stand on one leg.");
     *   assertTrue(standOnOneLeg.rangeEquals(0, simonSays));
     *
     *   Buffer payMeMoney = new Buffer().writeUtf8("Pay me $1,000,000.");
     *   assertFalse(payMeMoney.rangeEquals(0, simonSays));
     * }</pre>
     *
     * @param offset 偏移量
     * @param bytes  字节
     * @return
     * @throws IOException 异常
     */
    boolean rangeEquals(long offset, ByteString bytes) throws IOException;

    /**
     * 如果此源中 {@code offset} 处的 {@code byteCount} 个字节等于 {@code bytesOffset} 处的 {@code bytes}，则返回 true。
     * 这会根据需要扩展缓冲区，直到某个字节不匹配、所有字节都匹配，或者在足够的字节确定匹配之前流已耗尽。
     *
     * @param offset      偏移量
     * @param bytes       字节
     * @param bytesOffset 字节偏移量
     * @param byteCount   字节数
     * @return the true/false
     * @throws IOException 异常
     */
    boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount)
            throws IOException;

    /**
     * 返回一个新的 {@code BufferedSource}，可从此 {@code BufferedSource}读取数据但不使用它。
     * 一旦下次读取或关闭此源，返回的源将变为无效。
     * <pre> {@code
     *   Buffer buffer = new Buffer();
     *   buffer.writeUtf8("abcdefghi");
     *   buffer.readUtf8(3) // returns "abc", buffer contains "defghi"
     *
     *   BufferedSource peek = buffer.peek();
     *   peek.readUtf8(3); // returns "def", buffer contains "defghi"
     *   peek.readUtf8(3); // returns "ghi", buffer contains "defghi"
     *   buffer.readUtf8(3); // returns "def", buffer contains "ghi"
     * }</pre>
     *
     * @return {@link BufferSource}
     */
    BufferSource peek();

    /**
     * 返回从该源读取的输入流
     *
     * @return {@link InputStream}
     */
    InputStream inputStream();

}
