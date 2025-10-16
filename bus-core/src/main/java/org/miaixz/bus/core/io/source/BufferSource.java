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

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SegmentBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;

/**
 * A {@link Source} that maintains an internal buffer, allowing callers to perform small reads without performance
 * penalties. It also enables clients to read ahead, buffering necessary input before consumption.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BufferSource extends Source, ReadableByteChannel {

    /**
     * Returns the internal buffer of this source.
     *
     * @return The {@link Buffer} instance used by this source.
     */
    Buffer getBuffer();

    /**
     * Returns true if there are no more bytes in this source. This method will block until bytes are available to read
     * or the source is truly exhausted.
     *
     * @return True if the source is exhausted, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    boolean exhausted() throws IOException;

    /**
     * Ensures that at least {@code byteCount} bytes are in the buffer. If the source is exhausted before
     * {@code byteCount} bytes can be read, an {@link java.io.EOFException} is thrown.
     *
     * @param byteCount The minimum number of bytes required in the buffer.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    void require(long byteCount) throws IOException;

    /**
     * Returns true if the buffer contains at least {@code byteCount} bytes, expanding it as necessary. Returns false if
     * the source is exhausted before the requested bytes can be read.
     *
     * @param byteCount The minimum number of bytes to request in the buffer.
     * @return True if at least {@code byteCount} bytes are available, false if the source is exhausted.
     * @throws IOException If an I/O error occurs.
     */
    boolean request(long byteCount) throws IOException;

    /**
     * Removes and returns a single byte from this source.
     *
     * @return The byte read.
     * @throws IOException If an I/O error occurs or the source is exhausted.
     */
    byte readByte() throws IOException;

    /**
     * Removes two bytes from this source and returns them as a short.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeByte(0x7f).writeByte(0xff).writeByte(0x00).writeByte(0x0f);
     * assertEquals(4, buffer.size());
     *
     * assertEquals(32767, buffer.readShort());
     * assertEquals(2, buffer.size());
     *
     * assertEquals(15, buffer.readShort());
     * assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return The short value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 2 bytes are available.
     */
    short readShort() throws IOException;

    /**
     * Removes two bytes from this source and returns them as a short, in little-endian order.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeByte(0xff).writeByte(0x7f).writeByte(0x0f).writeByte(0x00);
     * assertEquals(4, buffer.size());
     *
     * assertEquals(32767, buffer.readShortLe());
     * assertEquals(2, buffer.size());
     *
     * assertEquals(15, buffer.readShortLe());
     * assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return The short value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 2 bytes are available.
     */
    short readShortLe() throws IOException;

    /**
     * Removes four bytes from this source and returns them as an integer.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeByte(0x7f).writeByte(0xff).writeByte(0xff).writeByte(0xff).writeByte(0x00)
     *         .writeByte(0x00).writeByte(0x00).writeByte(0x0f);
     * assertEquals(8, buffer.size());
     *
     * assertEquals(2147483647, buffer.readInt());
     * assertEquals(4, buffer.size());
     *
     * assertEquals(15, buffer.readInt());
     * assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return The integer value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 4 bytes are available.
     */
    int readInt() throws IOException;

    /**
     * Removes four bytes from this source and returns them as an integer, in little-endian order.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeByte(0xff).writeByte(0xff).writeByte(0xff).writeByte(0x7f).writeByte(0x0f)
     *         .writeByte(0x00).writeByte(0x00).writeByte(0x00);
     * assertEquals(8, buffer.size());
     *
     * assertEquals(2147483647, buffer.readIntLe());
     * assertEquals(4, buffer.size());
     *
     * assertEquals(15, buffer.readIntLe());
     * assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return The integer value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 4 bytes are available.
     */
    int readIntLe() throws IOException;

    /**
     * Removes eight bytes from this source and returns them as a long.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeByte(0x7f).writeByte(0xff).writeByte(0xff).writeByte(0xff).writeByte(0xff)
     *         .writeByte(0xff).writeByte(0xff).writeByte(0xff).writeByte(0x00).writeByte(0x00).writeByte(0x00)
     *         .writeByte(0x00).writeByte(0x00).writeByte(0x00).writeByte(0x00).writeByte(0x0f);
     * assertEquals(16, buffer.size());
     *
     * assertEquals(9223372036854775807L, buffer.readLong());
     * assertEquals(8, buffer.size());
     *
     * assertEquals(15, buffer.readLong());
     * assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return The long value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 8 bytes are available.
     */
    long readLong() throws IOException;

    /**
     * Removes eight bytes from this source and returns them as a long, in little-endian order.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeByte(0xff).writeByte(0xff).writeByte(0xff).writeByte(0xff).writeByte(0xff)
     *         .writeByte(0xff).writeByte(0xff).writeByte(0x7f).writeByte(0x0f).writeByte(0x00).writeByte(0x00)
     *         .writeByte(0x00).writeByte(0x00).writeByte(0x00).writeByte(0x00).writeByte(0x00);
     * assertEquals(16, buffer.size());
     *
     * assertEquals(9223372036854775807L, buffer.readLongLe());
     * assertEquals(8, buffer.size());
     *
     * assertEquals(15, buffer.readLongLe());
     * assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return The long value read.
     * @throws IOException If an I/O error occurs or the source is exhausted before 8 bytes are available.
     */
    long readLongLe() throws IOException;

    /**
     * Reads a signed decimal long from this source (i.e., a string of base-10 digits with an optional leading '-'
     * character). This operation will iterate until a non-digit character is found.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeUtf8("8675309 -123 00001");
     *
     * assertEquals(8675309L, buffer.readDecimalLong());
     * assertEquals(' ', buffer.readByte());
     * assertEquals(-123L, buffer.readDecimalLong());
     * assertEquals(' ', buffer.readByte());
     * assertEquals(1L, buffer.readDecimalLong());
     * }</pre>
     *
     * @return The decimal long value read.
     * @throws IOException           If an I/O error occurs.
     * @throws NumberFormatException If the number found does not fit in a {@code long} or no decimal number is present.
     */
    long readDecimalLong() throws IOException;

    /**
     * Reads an unsigned hexadecimal long from this source (i.e., a string of base-16 digits). This operation will
     * iterate until a non-hexadecimal character is found.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeUtf8("ffff CAFEBABE 10");
     *
     * assertEquals(65535L, buffer.readHexadecimalUnsignedLong());
     * assertEquals(' ', buffer.readByte());
     * assertEquals(0xcafebabeL, buffer.readHexadecimalUnsignedLong());
     * assertEquals(' ', buffer.readByte());
     * assertEquals(0x10L, buffer.readHexadecimalUnsignedLong());
     * }</pre>
     *
     * @return The hexadecimal unsigned long value read.
     * @throws IOException           If an I/O error occurs.
     * @throws NumberFormatException If the hexadecimal number found does not fit in a {@code long} or no hexadecimal
     *                               number is present.
     */
    long readHexadecimalUnsignedLong() throws IOException;

    /**
     * Removes and discards {@code byteCount} bytes from this source.
     *
     * @param byteCount The number of bytes to skip.
     * @throws IOException If the source is exhausted before the requested bytes can be skipped.
     */
    void skip(long byteCount) throws IOException;

    /**
     * Removes all bytes from this source and returns them as a {@link ByteString}.
     *
     * @return A {@link ByteString} containing all bytes from the source.
     * @throws IOException If an I/O error occurs.
     */
    ByteString readByteString() throws IOException;

    /**
     * Removes {@code byteCount} bytes from this source and returns them as a {@link ByteString}.
     *
     * @param byteCount The number of bytes to read.
     * @return A {@link ByteString} containing {@code byteCount} bytes from the source.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    ByteString readByteString(long byteCount) throws IOException;

    /**
     * Finds the first string in {@code options} that is a prefix of this buffer, consumes it from this buffer, and
     * returns its index. If no byte string in {@code options} is a prefix of this buffer, -1 is returned and no bytes
     * are consumed.
     *
     * This can be used as an alternative to {@link #readByteString} or even {@link #readUtf8} if the set of expected
     * values is known in advance.
     * 
     * <pre>{@code
     * Options FIELDS = Options
     *         .of(ByteString.encodeUtf8("depth="), ByteString.encodeUtf8("height="), ByteString.encodeUtf8("width="));
     *
     * Buffer buffer = new Buffer().writeUtf8("width=640\n").writeUtf8("height=480\n");
     *
     * assertEquals(2, buffer.select(FIELDS));
     * assertEquals(640, buffer.readDecimalLong());
     * assertEquals('\n', buffer.readByte());
     * assertEquals(1, buffer.select(FIELDS));
     * assertEquals(480, buffer.readDecimalLong());
     * assertEquals('\n', buffer.readByte());
     * }</pre>
     *
     * @param segmentBuffer The {@link SegmentBuffer} containing the options to match against.
     * @return The index of the matched {@link ByteString} in the {@link SegmentBuffer}, or -1 if no match is found and
     *         the source is exhausted.
     * @throws IOException If an I/O error occurs.
     */
    int select(SegmentBuffer segmentBuffer) throws IOException;

    /**
     * Removes all bytes from this source and returns them as a byte array.
     *
     * @return A byte array containing all bytes from the source.
     * @throws IOException If an I/O error occurs.
     */
    byte[] readByteArray() throws IOException;

    /**
     * Removes {@code byteCount} bytes from this source and returns them as a byte array.
     *
     * @param byteCount The number of bytes to read.
     * @return A byte array containing {@code byteCount} bytes from the source.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    byte[] readByteArray(long byteCount) throws IOException;

    /**
     * Removes up to {@code sink.length} bytes from this source and copies them into {@code sink}. Returns the number of
     * bytes read, or -1 if this source is exhausted.
     *
     * @param sink The byte array to write bytes into.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException If an I/O error occurs.
     */
    int read(byte[] sink) throws IOException;

    /**
     * Removes exactly {@code sink.length} bytes from this source and copies them into {@code sink}. An
     * {@link java.io.EOFException} is thrown if the requested number of bytes cannot be read.
     *
     * @param sink The byte array to write bytes into.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code sink.length} bytes are
     *                     available.
     */
    void readFully(byte[] sink) throws IOException;

    /**
     * Removes up to {@code byteCount} bytes from this source and copies them into {@code sink} at {@code offset}.
     * Returns the number of bytes read, or -1 if this source is exhausted.
     *
     * @param sink      The byte array to write bytes into.
     * @param offset    The starting offset in the byte array.
     * @param byteCount The maximum number of bytes to read.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException If an I/O error occurs.
     */
    int read(byte[] sink, int offset, int byteCount) throws IOException;

    /**
     * Removes exactly {@code byteCount} bytes from this source and appends them to {@code sink}. An
     * {@link java.io.EOFException} is thrown if the requested number of bytes cannot be read.
     *
     * @param sink      The buffer to write bytes into.
     * @param byteCount The number of bytes to read.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    void readFully(Buffer sink, long byteCount) throws IOException;

    /**
     * Removes all bytes from this source and appends them to {@code sink}. Returns the total number of bytes written to
     * {@code sink}, or 0 if exhausted.
     *
     * @param sink The {@link Sink} to write all bytes to.
     * @return The total number of bytes written to the sink.
     * @throws IOException If an I/O error occurs.
     */
    long readAll(Sink sink) throws IOException;

    /**
     * Removes all bytes from this source, decodes them as UTF-8, and returns the string. If this source is empty, an
     * empty string is returned.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeUtf8("Uh uh uh!").writeByte(' ').writeUtf8("You didn't say the magic word!");
     *
     * assertEquals("Uh uh uh! You didn't say the magic word!", buffer.readUtf8());
     * assertEquals(0, buffer.size());
     *
     * assertEquals("", buffer.readUtf8());
     * assertEquals(0, buffer.size());
     * }</pre>
     *
     * @return The decoded UTF-8 string.
     * @throws IOException If an I/O error occurs.
     */
    String readUtf8() throws IOException;

    /**
     * Removes {@code byteCount} bytes from this source, decodes them as UTF-8, and returns the string.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeUtf8("Uh uh uh!").writeByte(' ').writeUtf8("You didn't say the magic word!");
     * assertEquals(40, buffer.size());
     *
     * assertEquals("Uh uh uh! You ", buffer.readUtf8(14));
     * assertEquals(26, buffer.size());
     *
     * assertEquals("didn't say the", buffer.readUtf8(14));
     * assertEquals(12, buffer.size());
     *
     * assertEquals(" magic word!", buffer.readUtf8(12));
     * assertEquals(0, buffer.size());
     * }</pre>
     *
     * @param byteCount The number of bytes to read.
     * @return The decoded UTF-8 string.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    String readUtf8(long byteCount) throws IOException;

    /**
     * Removes and returns characters until the next newline (but not including the newline). The newline is either
     * {@code "\n"} or {@code "\r\n"}; these characters are not included in the result.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer().writeUtf8("I'm a hacker!\n").writeUtf8("That's what I said: you're a nerd.\n")
     *         .writeUtf8("I prefer to be called a hacker!\n");
     * assertEquals(81, buffer.size());
     *
     * assertEquals("I'm a hacker!", buffer.readUtf8Line());
     * assertEquals(67, buffer.size());
     *
     * assertEquals("That's what I said: you're a nerd.", buffer.readUtf8Line());
     * assertEquals(32, buffer.size());
     *
     * assertEquals("I prefer to be called a hacker!", buffer.readUtf8Line());
     * assertEquals(0, buffer.size());
     *
     * assertEquals(null, buffer.readUtf8Line());
     * assertEquals(0, buffer.size());
     * }</pre>
     *
     * <strong>At the end of the stream, this method returns null,</strong> just like {@link java.io.BufferedReader}. If
     * the source does not end with a newline, an implicit newline is assumed. Null will be returned once the source is
     * exhausted. Use this method for human-generated data where trailing newlines are optional.
     *
     * @return The decoded UTF-8 line, or null if the source is exhausted before a newline is found.
     * @throws IOException If an I/O error occurs.
     */
    String readUtf8Line() throws IOException;

    /**
     * Removes and returns characters until the next newline (but not including the newline). The newline is either
     * {@code "\n"} or {@code "\r\n"}; these characters are not included in the result. This method is strict and will
     * throw an {@link java.io.EOFException} if a newline is not found.
     *
     * @return The decoded UTF-8 line.
     * @throws IOException If an I/O error occurs or a newline is not found before the source is exhausted.
     */
    String readUtf8LineStrict() throws IOException;

    /**
     * Similar to {@link #readUtf8LineStrict()}, but allows the caller to specify the maximum length allowed for the
     * match. Use this to prevent streams that may not contain {@code "\n"} or {@code "\r\n"}.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer();
     * buffer.writeUtf8("12345\r\n");
     *
     * // This will throw! There must be \r\n or \n at the limit or before it.
     * buffer.readUtf8LineStrict(4);
     *
     * // No bytes have been consumed so the caller can retry.
     * assertEquals("12345", buffer.readUtf8LineStrict(5));
     * }</pre>
     *
     * @param limit The maximum number of bytes to scan for a newline character.
     * @return The decoded UTF-8 line.
     * @throws IOException              If an I/O error occurs or a newline is not found within the limit.
     * @throws IllegalArgumentException If {@code limit} is negative.
     */
    String readUtf8LineStrict(long limit) throws IOException;

    /**
     * Removes and returns a single UTF-8 code point, reading 1 to 4 bytes as necessary. If this source does not begin
     * with a correctly-encoded UTF-8 code point, this method will remove 1 or more malformed UTF-8 bytes and return the
     * replacement character ({@code U+FFFD}). This includes encoding problems (input is not correctly-encoded UTF-8),
     * out-of-range characters (beyond Unicode's 0x10ffff limit), code points that are UTF-16 surrogates
     * (U+d800..U+dfff), and overlong encodings (e.g., the NUL character as {@code 0xc080} in modified UTF-8).
     *
     * @return The decoded UTF-8 code point.
     * @throws IOException If an I/O error occurs or the source is exhausted before a complete code point can be read.
     */
    int readUtf8CodePoint() throws IOException;

    /**
     * Removes all bytes from this source, decodes them using {@code charset}, and returns the string.
     *
     * @param charset The charset to use for decoding.
     * @return The decoded string.
     * @throws IOException If an I/O error occurs.
     */
    String readString(Charset charset) throws IOException;

    /**
     * Removes {@code byteCount} bytes from this source, decodes them using {@code charset}, and returns the string.
     *
     * @param byteCount The number of bytes to read.
     * @param charset   The charset to use for decoding.
     * @return The decoded string.
     * @throws IOException If an I/O error occurs or the source is exhausted before {@code byteCount} bytes are
     *                     available.
     */
    String readString(long byteCount, Charset charset) throws IOException;

    /**
     * Equivalent to {@link #indexOf(byte, long) indexOf(b, 0)}.
     *
     * @param b The byte to search for.
     * @return The index of the first occurrence of the byte, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    long indexOf(byte b) throws IOException;

    /**
     * Returns the index of the first {@code b} in this buffer at or after {@code fromIndex}. This expands the buffer as
     * necessary until {@code b} is found. This will read an unbounded number of bytes into the buffer. Returns -1 if
     * the stream is exhausted before the requested byte is found.
     * 
     * <pre>{@code
     * Buffer buffer = new Buffer();
     * buffer.writeUtf8("Don't move! He can't see us if we don't move.");
     *
     * byte m = 'm';
     * assertEquals(6, buffer.indexOf(m));
     * assertEquals(40, buffer.indexOf(m, 12));
     * }</pre>
     *
     * @param b         The byte to search for.
     * @param fromIndex The index to start the search from.
     * @return The index of the first occurrence of the byte, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    long indexOf(byte b, long fromIndex) throws IOException;

    /**
     * Returns the index of {@code b} if found in the range of {@code fromIndex} to {@code toIndex}. Returns -1 if
     * {@code b} is not found, or if {@code fromIndex == toIndex}.
     *
     * @param b         The byte to search for.
     * @param fromIndex The index to start the search from (inclusive).
     * @param toIndex   The index to end the search at (exclusive).
     * @return The index of the first occurrence of the byte, or -1 if not found within the range.
     * @throws IOException If an I/O error occurs.
     */
    long indexOf(byte b, long fromIndex, long toIndex) throws IOException;

    /**
     * Equivalent to {@link #indexOf(ByteString, long) indexOf(bytes, 0)}.
     *
     * @param bytes The {@link ByteString} to search for.
     * @return The index of the first occurrence of the byte string, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    long indexOf(ByteString bytes) throws IOException;

    /**
     * Returns the index of the first match for {@code bytes} in this buffer at or after {@code fromIndex}. This expands
     * the buffer as necessary until {@code bytes} is found. This will read an unbounded number of bytes into the
     * buffer. Returns -1 if the stream is exhausted before the requested bytes are found.
     * 
     * <pre>{@code
     * ByteString MOVE = ByteString.encodeUtf8("move");
     * Buffer buffer = new Buffer();
     * buffer.writeUtf8("Don't move! He can't see us if we don't move.");
     *
     * assertEquals(6, buffer.indexOf(MOVE));
     * assertEquals(40, buffer.indexOf(MOVE, 12));
     * }</pre>
     *
     * @param bytes     The {@link ByteString} to search for.
     * @param fromIndex The index to start the search from.
     * @return The index of the first occurrence of the byte string, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    long indexOf(ByteString bytes, long fromIndex) throws IOException;

    /**
     * Equivalent to {@link #indexOfElement(ByteString, long) indexOfElement(targetBytes, 0)}.
     *
     * @param targetBytes The {@link ByteString} containing the bytes to search for.
     * @return The index of the first occurrence of any byte from {@code targetBytes}, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    long indexOfElement(ByteString targetBytes) throws IOException;

    /**
     * Returns the index in this buffer at or after {@code fromIndex} of the first byte that is in {@code targetBytes}.
     * This expands the buffer as necessary until a target byte is found. This will read an unbounded number of bytes
     * into the buffer. Returns -1 if the stream is exhausted before a requested byte is found.
     * 
     * <pre>{@code
     * ByteString ANY_VOWEL = ByteString.encodeUtf8("AEOIUaeoiu");
     * Buffer buffer = new Buffer();
     * buffer.writeUtf8("Dr. Alan Grant");
     *
     * assertEquals(4, buffer.indexOfElement(ANY_VOWEL)); // 'A' in 'Alan'.
     * assertEquals(11, buffer.indexOfElement(ANY_VOWEL, 9)); // 'a' in 'Grant'.
     * }</pre>
     *
     * @param targetBytes The {@link ByteString} containing the bytes to search for.
     * @param fromIndex   The index to start the search from.
     * @return The index of the first occurrence of any byte from {@code targetBytes}, or -1 if not found.
     * @throws IOException If an I/O error occurs.
     */
    long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException;

    /**
     * Returns true if the bytes in this source at {@code offset} equal {@code bytes}. This expands the buffer as
     * necessary until either a byte does not match, all bytes match, or the stream is exhausted before enough bytes can
     * be read to determine a match.
     * 
     * <pre>{@code
     * ByteString simonSays = ByteString.encodeUtf8("Simon says:");
     * Buffer standOnOneLeg = new Buffer().writeUtf8("Simon says: Stand on one leg.");
     * assertTrue(standOnOneLeg.rangeEquals(0, simonSays));
     *
     * Buffer payMeMoney = new Buffer().writeUtf8("Pay me $1,000,000.");
     * assertFalse(payMeMoney.rangeEquals(0, simonSays));
     * }</pre>
     *
     * @param offset The offset in this source to start the comparison.
     * @param bytes  The {@link ByteString} to compare against.
     * @return True if the bytes are equal, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    boolean rangeEquals(long offset, ByteString bytes) throws IOException;

    /**
     * Returns true if {@code byteCount} bytes in this source at {@code offset} equal the bytes in {@code bytes} at
     * {@code bytesOffset}. This expands the buffer as necessary until either a byte does not match, all bytes match, or
     * the stream is exhausted before enough bytes can be read to determine a match.
     *
     * @param offset      The offset in this source to start the comparison.
     * @param bytes       The {@link ByteString} to compare against.
     * @param bytesOffset The offset in {@code bytes} to start the comparison.
     * @param byteCount   The number of bytes to compare.
     * @return True if the bytes are equal, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) throws IOException;

    /**
     * Returns a new {@code BufferSource} that can read from this {@code BufferSource} without consuming its data. The
     * returned source becomes invalid once this source is next read or closed.
     * 
     * <pre> {@code
     *   Buffer buffer = new Buffer();
     *   buffer.writeUtf8("abcdefghi");
     *   buffer.readUtf8(3) // returns "abc", buffer contains "defghi"
     *
     *   BufferSource peek = buffer.peek();
     *   peek.readUtf8(3); // returns "def", buffer contains "defghi"
     *   peek.readUtf8(3); // returns "ghi", buffer contains "defghi"
     *   buffer.readUtf8(3); // returns "def", buffer contains "ghi"
     * }</pre>
     *
     * @return A new {@link BufferSource} for peeking.
     */
    BufferSource peek();

    /**
     * Returns an {@link InputStream} that reads from this source.
     *
     * @return An {@link InputStream} instance.
     */
    InputStream inputStream();

}
