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
package org.miaixz.bus.core.io.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;

/**
 * An {@link InputStream} implementation that reads bytes from a {@link Reader}. This class adapts a character-based
 * input stream (Reader) to a byte-based input stream (InputStream), performing character encoding using a specified
 * {@link CharsetEncoder}.
 * <p>
 * This implementation is inspired by Apache Commons IO.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReaderInputStream extends InputStream {

    /**
     * The underlying {@link Reader} providing character data.
     */
    private final Reader reader;
    /**
     * The {@link CharsetEncoder} used to convert characters to bytes.
     */
    private final CharsetEncoder encoder;
    /**
     * The {@link CharBuffer} used as input for the encoder.
     */
    private final CharBuffer encoderIn;
    /**
     * The {@link ByteBuffer} used as output for the encoder.
     */
    private final ByteBuffer encoderOut;
    /**
     * The result of the last encoding operation.
     */
    private CoderResult lastCoderResult;
    /**
     * Flag indicating whether the end of the input {@link Reader} has been reached.
     */
    private boolean endOfInput;

    /**
     * Constructs a new {@code ReaderInputStream} with the specified {@link Reader} and
     * {@link java.nio.charset.Charset}. Uses a default buffer size of 8192 bytes.
     *
     * @param reader  The {@link Reader} providing character data.
     * @param charset The {@link java.nio.charset.Charset} to use for encoding characters to bytes.
     */
    public ReaderInputStream(final Reader reader, final java.nio.charset.Charset charset) {
        this(reader, charset, Normal._8192);
    }

    /**
     * Constructs a new {@code ReaderInputStream} with the specified {@link Reader}, {@link java.nio.charset.Charset},
     * and buffer size.
     *
     * @param reader     The {@link Reader} providing character data.
     * @param charset    The {@link java.nio.charset.Charset} to use for encoding characters to bytes.
     * @param bufferSize The size of the internal character and byte buffers.
     */
    public ReaderInputStream(final Reader reader, final java.nio.charset.Charset charset, final int bufferSize) {
        this(reader, Charset.newEncoder(charset, CodingErrorAction.REPLACE), bufferSize);
    }

    /**
     * Constructs a new {@code ReaderInputStream} with the specified {@link Reader} and {@link CharsetEncoder}. Uses a
     * default buffer size of 8192 bytes.
     *
     * @param reader  The {@link Reader} providing character data.
     * @param encoder The {@link CharsetEncoder} to use for encoding characters to bytes.
     */
    public ReaderInputStream(final Reader reader, final CharsetEncoder encoder) {
        this(reader, encoder, Normal._8192);
    }

    /**
     * Constructs a new {@code ReaderInputStream} with the specified {@link Reader}, {@link CharsetEncoder}, and buffer
     * size.
     *
     * @param reader     The {@link Reader} providing character data.
     * @param encoder    The {@link CharsetEncoder} to use for encoding characters to bytes.
     * @param bufferSize The size of the internal character and byte buffers.
     */
    public ReaderInputStream(final Reader reader, final CharsetEncoder encoder, final int bufferSize) {
        this.reader = reader;
        this.encoder = encoder;

        encoderIn = CharBuffer.allocate(bufferSize);
        encoderIn.flip();
        encoderOut = ByteBuffer.allocate(bufferSize);
        encoderOut.flip();
    }

    /**
     * Reads up to {@code len} bytes of data from the input stream into an array of bytes. An attempt is made to read as
     * many as {@code len} bytes, but a smaller number may be read.
     *
     * @param b   The buffer into which the data is read.
     * @param off The start offset in the destination array {@code b} at which the data is written.
     * @param len The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached.
     * @throws IOException               If an I/O error occurs.
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len} is negative, or {@code off+len} is
     *                                   greater than {@code b.length}.
     */
    @Override
    public int read(final byte[] b, int off, int len) throws IOException {
        Assert.notNull(b, "Byte array must not be null");
        if ((len < 0) || (off < 0) || (off + len > b.length)) {
            throw new IndexOutOfBoundsException("Array Size=" + b.length + ", offset=" + off + ", length=" + len);
        }

        int read = 0;
        if (len == 0) {
            return 0;
        }
        while (len > 0) {
            if (encoderOut.hasRemaining()) {
                final int c = Math.min(encoderOut.remaining(), len);
                encoderOut.get(b, off, c);
                off += c;
                len -= c;
                read += c;
            } else {
                fillBuffer();
                if ((endOfInput) && (!encoderOut.hasRemaining())) {
                    break;
                }
            }
        }
        return (read == 0) && (endOfInput) ? -1 : read;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an {@code int} in the range
     * {@code 0} to {@code 255}. If no byte is available because the end of the stream has been reached, the value
     * {@code -1} is returned.
     *
     * @return The next byte of data, or {@code -1} if the end of the stream is reached.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        do {
            if (encoderOut.hasRemaining()) {
                return encoderOut.get() & 0xFF;
            }
            fillBuffer();
        } while ((!endOfInput) || (encoderOut.hasRemaining()));
        return -1;
    }

    /**
     * Closes this input stream and releases any system resources associated with the stream. This method closes the
     * underlying {@link Reader}.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Fills the internal buffers by reading characters from the underlying {@link Reader} and encoding them into bytes.
     * This method manages the state of the {@link CharsetEncoder} and the input/output buffers. It continues to read
     * and encode data until the end of the input is reached or an encoding condition requires stopping.
     *
     * @throws IOException If an I/O error occurs while reading from the underlying {@link Reader}.
     */
    private void fillBuffer() throws IOException {
        // If input is not yet ended and the last encoding result was normal (no overflow or error),
        // try to read more data.
        if ((!endOfInput) && ((lastCoderResult == null) || (lastCoderResult.isUnderflow()))) {
            encoderIn.compact(); // Prepare the input buffer to receive new data.
            final int position = encoderIn.position(); // Record the current read position.

            // Read data from the reader into the encoderIn buffer.
            final int c = reader.read(encoderIn.array(), position, encoderIn.remaining());
            if (c == -1) // If end of input is reached.
                endOfInput = true;
            else {
                // Update the read position, ready to process the next batch of data.
                encoderIn.position(position + c);
            }
            encoderIn.flip(); // Flip the input buffer to prepare it for encoding.
        }

        // Prepare the output buffer to receive encoded data.
        encoderOut.compact();
        // Perform the encoding operation, encoding data from the input buffer to the output buffer.
        lastCoderResult = encoder.encode(encoderIn, encoderOut, endOfInput);
        // Flip the output buffer to prepare it for being written to the final destination.
        encoderOut.flip();
    }

}
