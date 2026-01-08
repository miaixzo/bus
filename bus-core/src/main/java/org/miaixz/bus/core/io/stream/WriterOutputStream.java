/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;

/**
 * An {@link OutputStream} implementation that converts byte data into character data using a {@link CharsetDecoder} and
 * writes it to a {@link Writer}. This class allows configuring buffer sizes and whether to write immediately to the
 * underlying writer.
 * <p>
 * Source:
 * https://github.com/subchen/jetbrick-commons/blob/master/src/main/java/jetbrick/io/stream/WriterOutputStream.java
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WriterOutputStream extends OutputStream {

    /**
     * The target {@link Writer} to which character data is written.
     */
    private final Writer writer;
    /**
     * The {@link CharsetDecoder} used to decode byte data into character data.
     */
    private final CharsetDecoder decoder;
    /**
     * A flag indicating whether data should be written immediately to the writer without buffering.
     */
    private final boolean writeImmediately;
    /**
     * The {@link ByteBuffer} used as input for the decoder.
     */
    private final ByteBuffer decoderIn;
    /**
     * The {@link CharBuffer} used as output for the decoder.
     */
    private final CharBuffer decoderOut;

    /**
     * Constructs a new {@code WriterOutputStream} with the specified {@link Writer} and
     * {@link java.nio.charset.Charset}. Uses a default buffer size of 8192 bytes and does not write immediately.
     *
     * @param writer  The target {@link Writer} for character data.
     * @param charset The {@link java.nio.charset.Charset} to use for decoding byte data.
     */
    public WriterOutputStream(final Writer writer, final java.nio.charset.Charset charset) {
        this(writer, charset, Normal._8192, false);
    }

    /**
     * Constructs a new {@code WriterOutputStream} with the specified {@link Writer}, {@link java.nio.charset.Charset},
     * buffer size, and immediate write configuration.
     *
     * @param writer           The target {@link Writer} for character data.
     * @param charset          The {@link java.nio.charset.Charset} to use for decoding byte data.
     * @param bufferSize       The size of the internal character buffer.
     * @param writeImmediately {@code true} to write immediately to the writer (no internal buffering), {@code false} to
     *                         buffer characters before writing.
     */
    public WriterOutputStream(final Writer writer, final java.nio.charset.Charset charset, final int bufferSize,
            final boolean writeImmediately) {
        this(writer, Charset.newDecoder(charset, CodingErrorAction.REPLACE), bufferSize, writeImmediately);
    }

    /**
     * Constructs a new {@code WriterOutputStream} with the specified {@link Writer} and {@link CharsetDecoder}. Uses a
     * default buffer size of 8192 bytes and does not write immediately.
     *
     * @param writer  The target {@link Writer} for character data.
     * @param decoder The {@link CharsetDecoder} to use for decoding byte data.
     */
    public WriterOutputStream(final Writer writer, final CharsetDecoder decoder) {
        this(writer, decoder, Normal._8192, false);
    }

    /**
     * Constructs a new {@code WriterOutputStream} with the specified {@link Writer}, {@link CharsetDecoder}, buffer
     * size, and immediate write configuration.
     *
     * @param writer           The target {@link Writer} for character data.
     * @param decoder          The {@link CharsetDecoder} to use for decoding byte data.
     * @param bufferSize       The size of the internal character buffer.
     * @param writeImmediately {@code true} to write immediately to the writer (no internal buffering), {@code false} to
     *                         buffer characters before writing.
     */
    public WriterOutputStream(final Writer writer, final CharsetDecoder decoder, final int bufferSize,
            final boolean writeImmediately) {
        this.writer = writer;
        this.decoder = decoder;
        this.writeImmediately = writeImmediately;
        this.decoderOut = CharBuffer.allocate(bufferSize);
        this.decoderIn = ByteBuffer.allocate(128);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting at offset {@code off} to this output stream. The
     * bytes are decoded into characters and then written to the underlying {@link Writer}.
     *
     * @param b   The data to write.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(final byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            final int c = Math.min(len, decoderIn.remaining());
            decoderIn.put(b, off, c);
            processInput(false);
            len -= c;
            off += c;
        }
        if (writeImmediately)
            flushOutput();
    }

    /**
     * Writes {@code b.length} bytes from the specified byte array to this output stream. The bytes are decoded into
     * characters and then written to the underlying {@link Writer}.
     *
     * @param b The data to write.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes the specified byte to this output stream. The byte is decoded into a character and then written to the
     * underlying {@link Writer}.
     *
     * @param b The byte to write.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(final int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out to the underlying stream. This
     * method first flushes any internally buffered characters to the writer, then flushes the writer itself.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        flushOutput();
        writer.flush();
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream. This method processes
     * any remaining input, flushes all buffered output, and then closes the underlying {@link Writer}.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        processInput(true);
        flushOutput();
        writer.close();
    }

    /**
     * Processes the input {@link ByteBuffer} by decoding its contents into the output {@link CharBuffer}. This method
     * handles the decoding process, including buffer management and flushing when the output buffer is full.
     *
     * @param endOfInput {@code true} if this is the last input to be processed, {@code false} otherwise.
     * @throws IOException If an I/O error occurs during decoding or flushing.
     */
    private void processInput(final boolean endOfInput) throws IOException {
        decoderIn.flip();
        CoderResult coderResult;
        while (true) {
            coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
            if (!coderResult.isOverflow())
                break;
            flushOutput();
        }
        if (!coderResult.isUnderflow()) {
            throw new IOException("Unexpected coder result");
        }

        decoderIn.compact();
    }

    /**
     * Flushes the content of the internal character buffer to the underlying {@link Writer}. This method writes all
     * characters currently in {@code decoderOut} to the writer and then rewinds the buffer.
     *
     * @throws IOException If an I/O error occurs during writing.
     */
    private void flushOutput() throws IOException {
        if (decoderOut.position() > 0) {
            writer.write(decoderOut.array(), 0, decoderOut.position());
            decoderOut.rewind();
        }
    }

}
