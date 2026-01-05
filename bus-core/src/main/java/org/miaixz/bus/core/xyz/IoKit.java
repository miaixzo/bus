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
package org.miaixz.bus.core.xyz;

import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.iterator.LineIterator;
import org.miaixz.bus.core.io.BomReader;
import org.miaixz.bus.core.io.LifeCycle;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.StreamProgress;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.copier.ChannelCopier;
import org.miaixz.bus.core.io.copier.FileChannelCopier;
import org.miaixz.bus.core.io.copier.ReaderWriterCopier;
import org.miaixz.bus.core.io.copier.StreamCopier;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.RealSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.RealSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.io.stream.StreamReader;
import org.miaixz.bus.core.io.stream.StreamWriter;
import org.miaixz.bus.core.io.timout.AsyncTimeout;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * IO utility class. This class provides helper methods for reading and writing streams, but does not handle stream
 * closing. This is because streams may be read and written multiple times, and closing them prematurely can lead to
 * issues.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IoKit {

    /**
     * Constructs a new IoKit. Utility class constructor for static access.
     */
    private IoKit() {
    }

    /**
     * Copies content from a {@link Reader} to a {@link Writer} using the default buffer size. The Reader is not closed
     * after copying.
     *
     * @param reader The source Reader.
     * @param writer The destination Writer.
     * @return The number of characters copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(final Reader reader, final Writer writer) throws InternalException {
        return copy(reader, writer, Normal._8192);
    }

    /**
     * Copies content from a {@link Reader} to a {@link Writer}. The Reader is not closed after copying.
     *
     * @param reader     The source Reader.
     * @param writer     The destination Writer.
     * @param bufferSize The size of the buffer to use for copying.
     * @return The number of characters copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(final Reader reader, final Writer writer, final int bufferSize) throws InternalException {
        return copy(reader, writer, bufferSize, null);
    }

    /**
     * Copies content from a {@link Reader} to a {@link Writer}. The Reader is not closed after copying.
     *
     * @param reader         The source Reader.
     * @param writer         The destination Writer.
     * @param bufferSize     The size of the buffer to use for copying.
     * @param streamProgress The progress handler for monitoring the copy operation, can be {@code null}.
     * @return The number of characters copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(
            final Reader reader,
            final Writer writer,
            final int bufferSize,
            final StreamProgress streamProgress) throws InternalException {
        return copy(reader, writer, bufferSize, -1, streamProgress);
    }

    /**
     * Copies content from a {@link Reader} to a {@link Writer}. The Reader is not closed after copying.
     *
     * @param reader         The source Reader, must not be {@code null}.
     * @param writer         The destination Writer, must not be {@code null}.
     * @param bufferSize     The size of the buffer to use for copying. Use -1 for default size.
     * @param count          The maximum number of characters to copy. Use -1 for unlimited.
     * @param streamProgress The progress handler for monitoring the copy operation, can be {@code null}.
     * @return The number of characters copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(
            final Reader reader,
            final Writer writer,
            final int bufferSize,
            final long count,
            final StreamProgress streamProgress) throws InternalException {
        Assert.notNull(reader, "Reader is null !");
        Assert.notNull(writer, "Writer is null !");
        return new ReaderWriterCopier(bufferSize, count, streamProgress).copy(reader, writer);
    }

    /**
     * Copies content from an {@link InputStream} to an {@link OutputStream} using the default buffer size. The streams
     * are not closed after copying.
     *
     * @param in  The source InputStream.
     * @param out The destination OutputStream.
     * @return The number of bytes copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(final InputStream in, final OutputStream out) throws InternalException {
        return copy(in, out, Normal._8192);
    }

    /**
     * Copies content from an {@link InputStream} to an {@link OutputStream}. The streams are not closed after copying.
     *
     * @param in         The source InputStream.
     * @param out        The destination OutputStream.
     * @param bufferSize The size of the buffer to use for copying.
     * @return The number of bytes copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(final InputStream in, final OutputStream out, final int bufferSize)
            throws InternalException {
        return copy(in, out, bufferSize, (StreamProgress) null);
    }

    /**
     * Copies content from an {@link InputStream} to an {@link OutputStream}. The streams are not closed after copying.
     *
     * @param in             The source InputStream.
     * @param out            The destination OutputStream.
     * @param bufferSize     The size of the buffer to use for copying.
     * @param streamProgress The progress handler for monitoring the copy operation, can be {@code null}.
     * @return The number of bytes copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(
            final InputStream in,
            final OutputStream out,
            final int bufferSize,
            final StreamProgress streamProgress) throws InternalException {
        return copy(in, out, bufferSize, -1, streamProgress);
    }

    /**
     * Copies content from an {@link InputStream} to an {@link OutputStream}. The streams are not closed after copying.
     *
     * @param in             The source InputStream.
     * @param out            The destination OutputStream.
     * @param bufferSize     The size of the buffer to use for copying.
     * @param count          The total number of bytes to copy. Use -1 for unlimited.
     * @param streamProgress The progress handler for monitoring the copy operation, can be {@code null}.
     * @return The number of bytes copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(
            final InputStream in,
            final OutputStream out,
            final int bufferSize,
            final long count,
            final StreamProgress streamProgress) throws InternalException {
        Assert.notNull(in, "InputStream is null !");
        Assert.notNull(out, "OutputStream is null !");
        return new StreamCopier(bufferSize, count, streamProgress).copy(in, out);
    }

    /**
     * Copies content from a {@link FileInputStream} to a {@link FileOutputStream} using NIO. The streams are not closed
     * after copying.
     *
     * @param in  The source FileInputStream.
     * @param out The destination FileOutputStream.
     * @return The number of bytes copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(final FileInputStream in, final FileOutputStream out) throws InternalException {
        Assert.notNull(in, "FileInputStream is null!");
        Assert.notNull(out, "FileOutputStream is null!");

        return FileChannelCopier.of().copy(in, out);
    }

    /**
     * Copies content from an {@link InputStream} to an {@link OutputStream} using a provided buffer. The streams are
     * not closed after copying.
     *
     * @param in     The source InputStream.
     * @param out    The destination OutputStream.
     * @param buffer The byte array buffer to use for copying.
     * @throws IOException If an I/O error occurs.
     */
    public static void copy(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        int count;
        while ((count = in.read(buffer, 0, buffer.length)) > 0)
            if (out != null)
                out.write(buffer, 0, count);
    }

    /**
     * Copies a specified length of content from an {@link InputStream} to an {@link OutputStream}, potentially swapping
     * bytes. The streams are not closed after copying.
     *
     * @param in        The source InputStream.
     * @param out       The destination OutputStream.
     * @param len       The number of bytes to copy.
     * @param swapBytes The number of bytes to swap (e.g., 2 for short, 4 for int).
     * @throws InternalException If an I/O error occurs.
     */
    public static void copy(InputStream in, OutputStream out, int len, int swapBytes) throws InternalException {
        copy(in, out, len & 0xffffffffL, swapBytes);
    }

    /**
     * Copies a specified length of content from an {@link InputStream} to an {@link OutputStream}, potentially swapping
     * bytes. The streams are not closed after copying.
     *
     * @param in        The source InputStream.
     * @param out       The destination OutputStream.
     * @param len       The number of bytes to copy.
     * @param swapBytes The number of bytes to swap (e.g., 2 for short, 4 for int).
     * @throws InternalException If an I/O error occurs.
     */
    public static void copy(InputStream in, OutputStream out, long len, int swapBytes) throws InternalException {
        copy(in, out, len, swapBytes, new byte[(int) Math.min(len, Normal._2048)]);
    }

    /**
     * Copies a specified length of content from an {@link InputStream} to an {@link OutputStream} using a given buffer
     * size. The streams are not closed after copying.
     *
     * @param in         The source InputStream.
     * @param out        The destination OutputStream.
     * @param bufferSize The size of the buffer to use for copying.
     * @throws InternalException If an I/O error occurs.
     */
    public static void copy(InputStream in, OutputStream out, long bufferSize) throws InternalException {
        copy(in, out, bufferSize & 0xffffffffL, new byte[(int) Math.min(bufferSize, Normal._2048)]);
    }

    /**
     * Copies a specified length of content from an {@link InputStream} to an {@link OutputStream} using a provided
     * buffer. The streams are not closed after copying.
     *
     * @param in         The source InputStream.
     * @param out        The destination OutputStream.
     * @param bufferSize The size of the buffer to use for copying.
     * @param buffer     The byte array buffer to use for copying.
     * @throws InternalException If an I/O error occurs.
     */
    public static void copy(InputStream in, OutputStream out, int bufferSize, byte[] buffer) throws InternalException {
        copy(in, out, bufferSize & 0xffffffffL, buffer);
    }

    /**
     * Copies a specified length of content from an {@link InputStream} to an {@link OutputStream} using a provided
     * buffer. The streams are not closed after copying.
     *
     * @param in         The source InputStream.
     * @param out        The destination OutputStream.
     * @param bufferSize The size of the buffer to use for copying.
     * @param buffer     The byte array buffer to use for copying.
     * @throws InternalException If an I/O error occurs.
     */
    public static void copy(InputStream in, OutputStream out, long bufferSize, byte[] buffer) throws InternalException {
        if (bufferSize < 0)
            throw new IndexOutOfBoundsException();
        try {
            while (bufferSize > 0) {
                int count = in.read(buffer, 0, (int) Math.min(bufferSize, buffer.length));
                if (count < 0)
                    throw new InternalException();
                out.write(buffer, 0, count);
                bufferSize -= count;
            }
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Copies a specified length of content from an {@link InputStream} to an {@link OutputStream}, potentially swapping
     * bytes, using a provided buffer. The streams are not closed after copying.
     *
     * @param in         The source InputStream.
     * @param out        The destination OutputStream.
     * @param bufferSize The size of the buffer to use for copying.
     * @param swapBytes  The number of bytes to swap (e.g., 2 for short, 4 for int).
     * @param buffer     The byte array buffer to use for copying.
     * @throws InternalException If an I/O error occurs.
     */
    public static void copy(InputStream in, OutputStream out, int bufferSize, int swapBytes, byte[] buffer)
            throws InternalException {
        copy(in, out, bufferSize & 0xffffffffL, swapBytes, buffer);
    }

    /**
     * Copies a specified length of content from an {@link InputStream} to an {@link OutputStream}, potentially swapping
     * bytes, using a provided buffer. The streams are not closed after copying.
     *
     * @param in         The source InputStream.
     * @param out        The destination OutputStream.
     * @param bufferSize The size of the buffer to use for copying.
     * @param swapBytes  The number of bytes to swap (e.g., 2 for short, 4 for int).
     * @param buffer     The byte array buffer to use for copying.
     * @throws InternalException If an I/O error occurs.
     */
    public static void copy(InputStream in, OutputStream out, long bufferSize, int swapBytes, byte[] buffer)
            throws InternalException {
        if (swapBytes == 1) {
            copy(in, out, bufferSize, buffer);
            return;
        }
        if (!(swapBytes == 2 || swapBytes == 4))
            throw new IllegalArgumentException("swapBytes: " + swapBytes);
        if (bufferSize < 0 || (bufferSize % swapBytes) != 0)
            throw new IllegalArgumentException("length: " + bufferSize);
        int off = 0;
        try {
            while (bufferSize > 0) {
                int count = in.read(buffer, off, (int) Math.min(bufferSize, buffer.length - off));
                if (count < 0)
                    throw new InternalException("" + count);
                bufferSize -= count;
                count += off;
                off = count % swapBytes;
                count -= off;
                switch (swapBytes) {
                    case 2:
                        ByteKit.swapShorts(buffer, 0, count);
                        break;

                    case 4:
                        ByteKit.swapInts(buffer, 0, count);
                        break;

                    case 8:
                        ByteKit.swapLongs(buffer, 0, count);
                        break;
                }
                out.write(buffer, 0, count);
                if (off > 0)
                    System.arraycopy(buffer, count, buffer, 0, off);
            }
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a {@link BufferedReader} from an {@link InputStream} using UTF-8 encoding by default.
     *
     * @param in The input stream.
     * @return A new {@link BufferedReader} object, or {@code null} if the input stream is {@code null}.
     */
    public static BufferedReader toUtf8Reader(final InputStream in) {
        return toReader(in, Charset.UTF_8);
    }

    /**
     * Creates a {@link BomReader} from an {@link InputStream}.
     *
     * @param in The input stream.
     * @return A new {@link BomReader}.
     */
    public static BomReader toBomReader(final InputStream in) {
        return new BomReader(in);
    }

    /**
     * Creates a {@link BufferedReader} from an {@link InputStream} with a specified charset.
     *
     * @param in      The input stream.
     * @param charset The character set to use. If {@code null}, the default charset is used.
     * @return A new {@link BufferedReader} object, or {@code null} if the input stream is {@code null}.
     */
    public static BufferedReader toReader(final InputStream in, final java.nio.charset.Charset charset) {
        if (null == in) {
            return null;
        }

        final InputStreamReader reader;
        if (null == charset) {
            reader = new InputStreamReader(in);
        } else {
            reader = new InputStreamReader(in, charset);
        }

        return new BufferedReader(reader);
    }

    /**
     * Creates an {@link OutputStreamWriter} from an {@link OutputStream} using UTF-8 encoding by default.
     *
     * @param out The output stream.
     * @return A new {@link OutputStreamWriter} object, or {@code null} if the output stream is {@code null}.
     */
    public static OutputStreamWriter toUtf8Writer(final OutputStream out) {
        return toWriter(out, Charset.UTF_8);
    }

    /**
     * Creates an {@link OutputStreamWriter} from an {@link OutputStream} with a specified charset.
     *
     * @param out     The output stream.
     * @param charset The character set to use. If {@code null}, the default charset is used.
     * @return A new {@link OutputStreamWriter} object, or {@code null} if the output stream is {@code null}.
     */
    public static OutputStreamWriter toWriter(final OutputStream out, final java.nio.charset.Charset charset) {
        if (null == out) {
            return null;
        }

        if (null == charset) {
            return new OutputStreamWriter(out);
        } else {
            return new OutputStreamWriter(out, charset);
        }
    }

    /**
     * Reads the content from an {@link InputStream} as a UTF-8 encoded string. The stream is closed after reading.
     *
     * @param in The input stream.
     * @return The content of the stream as a string.
     * @throws InternalException If an I/O error occurs.
     */
    public static String readUtf8(final InputStream in) throws InternalException {
        return read(in, Charset.UTF_8);
    }

    /**
     * Reads the content from an {@link InputStream} with a specified charset. The stream is closed after reading.
     *
     * @param in      The input stream, which will be closed after reading.
     * @param charset The character set to use for decoding the bytes.
     * @return The content of the stream as a string.
     * @throws InternalException If an I/O error occurs.
     */
    public static String read(final InputStream in, final java.nio.charset.Charset charset) throws InternalException {
        return StringKit.toString(readBytes(in), charset);
    }

    /**
     * Reads the content from an {@link InputStream} into a {@link FastByteArrayOutputStream}. The input stream is
     * closed after reading.
     *
     * @param in The input stream.
     * @return A {@link FastByteArrayOutputStream} containing the read content.
     * @throws InternalException If an I/O error occurs.
     */
    public static FastByteArrayOutputStream read(final InputStream in) throws InternalException {
        return read(in, true);
    }

    /**
     * Reads the content from an {@link InputStream} into a {@link FastByteArrayOutputStream}.
     *
     * @param in      The input stream.
     * @param isClose Whether to close the input stream after reading.
     * @return A {@link FastByteArrayOutputStream} containing the read content.
     * @throws InternalException If an I/O error occurs.
     */
    public static FastByteArrayOutputStream read(final InputStream in, final boolean isClose) throws InternalException {
        return StreamReader.of(in, isClose).read();
    }

    /**
     * Reads the content from a {@link Reader} into a String. The Reader is closed after reading.
     *
     * @param reader The source Reader.
     * @return The content of the Reader as a string.
     * @throws InternalException If an I/O error occurs.
     */
    public static String read(final Reader reader) throws InternalException {
        return read(reader, true);
    }

    /**
     * Reads the content from a {@link Reader} into a String.
     *
     * @param reader  The source {@link Reader}.
     * @param isClose Whether to close the {@link Reader} after reading.
     * @return The content of the Reader as a string.
     * @throws InternalException If an I/O error occurs.
     */
    public static String read(final Reader reader, final boolean isClose) throws InternalException {
        final StringBuilder builder = StringKit.builder();
        final CharBuffer buffer = CharBuffer.allocate(Normal._8192);
        try {
            while (-1 != reader.read(buffer)) {
                builder.append(buffer.flip());
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (isClose) {
                IoKit.closeQuietly(reader);
            }
        }
        return builder.toString();
    }

    /**
     * Reads all bytes from an {@link InputStream}. The input stream is closed after reading.
     *
     * @param in The {@link InputStream}.
     * @return A byte array containing the read bytes.
     * @throws InternalException If an I/O error occurs.
     */
    public static byte[] readBytes(final InputStream in) throws InternalException {
        return readBytes(in, true);
    }

    /**
     * Reads all bytes from an {@link InputStream}.
     *
     * @param in      The {@link InputStream}.
     * @param isClose Whether to close the input stream after reading.
     * @return A byte array containing the read bytes.
     * @throws InternalException If an I/O error occurs.
     */
    public static byte[] readBytes(final InputStream in, final boolean isClose) throws InternalException {
        return StreamReader.of(in, isClose).readBytes();
    }

    /**
     * Reads a specified length of bytes from an {@link InputStream}. The stream is not closed after reading.
     *
     * @param in     The {@link InputStream}. Returns {@code null} if the input stream is {@code null}.
     * @param length The number of bytes to read. If less than or equal to 0, an empty byte array is returned.
     * @return A byte array containing the read bytes.
     * @throws InternalException If an I/O error occurs.
     */
    public static byte[] readBytes(final InputStream in, final int length) throws InternalException {
        return StreamReader.of(in, false).readBytes(length);
    }

    /**
     * Reads a specified length of bytes from an {@link InputStream} and converts them to a hexadecimal string.
     *
     * @param in          The {@link InputStream}.
     * @param length      The number of bytes to read.
     * @param toLowerCase If {@code true}, the hexadecimal string will be in lowercase; otherwise, uppercase.
     * @return A hexadecimal string representation of the read bytes.
     * @throws InternalException If an I/O error occurs.
     */
    public static String readHex(final InputStream in, final int length, final boolean toLowerCase)
            throws InternalException {
        return HexKit.encodeString(readBytes(in, length), toLowerCase);
    }

    /**
     * Reads an object from an {@link InputStream} by deserialization. The stream is not closed after reading.
     * <p>
     * WARNING: This method does not check for deserialization security and may be vulnerable to deserialization
     * attacks!
     *
     * @param <T>           The type of the object to read.
     * @param in            The input stream.
     * @param acceptClasses The classes that are allowed to be deserialized.
     * @return The deserialized object.
     * @throws InternalException If an I/O error occurs or a {@link ClassNotFoundException} is wrapped.
     */
    public static <T> T readObject(final InputStream in, final Class<?>... acceptClasses) throws InternalException {
        return StreamReader.of(in, false).readObject(acceptClasses);
    }

    /**
     * Reads lines from an {@link InputStream} using UTF-8 encoding and adds them to a {@link Collection}.
     *
     * @param <T>        The type of the collection.
     * @param in         The input stream.
     * @param collection The collection to which the lines will be added.
     * @return The collection containing the read lines.
     * @throws InternalException If an I/O error occurs.
     */
    public static <T extends Collection<String>> T readLines(final InputStream in, final T collection)
            throws InternalException {
        return readLines(in, Charset.UTF_8, collection);
    }

    /**
     * Reads lines from an {@link InputStream} with a specified charset and adds them to a {@link Collection}.
     *
     * @param <T>        The type of the collection.
     * @param in         The input stream.
     * @param charset    The character set to use for decoding the bytes.
     * @param collection The collection to which the lines will be added.
     * @return The collection containing the read lines.
     * @throws InternalException If an I/O error occurs.
     */
    public static <T extends Collection<String>> T readLines(
            final InputStream in,
            final java.nio.charset.Charset charset,
            final T collection) throws InternalException {
        return readLines(toReader(in, charset), collection);
    }

    /**
     * Reads lines from a {@link Reader} and adds them to a {@link Collection}.
     *
     * @param <T>        The type of the collection.
     * @param reader     The source {@link Reader}.
     * @param collection The collection to which the lines will be added.
     * @return The collection containing the read lines.
     * @throws InternalException If an I/O error occurs.
     */
    public static <T extends Collection<String>> T readLines(final Reader reader, final T collection)
            throws InternalException {
        readLines(reader, (ConsumerX<String>) collection::add);
        return collection;
    }

    /**
     * Reads lines from an {@link InputStream} using UTF-8 encoding and processes each line with a {@link ConsumerX}.
     *
     * @param in          The {@link InputStream}.
     * @param lineHandler The line handler to process each line.
     * @throws InternalException If an I/O error occurs.
     */
    public static void readLines(final InputStream in, final ConsumerX<String> lineHandler) throws InternalException {
        readLines(in, Charset.UTF_8, lineHandler);
    }

    /**
     * Reads lines from an {@link InputStream} with a specified charset and processes each line with a
     * {@link ConsumerX}.
     *
     * @param in          The {@link InputStream}.
     * @param charset     The {@link java.nio.charset.Charset} encoding to use.
     * @param lineHandler The line handler to process each line.
     * @throws InternalException If an I/O error occurs.
     */
    public static void readLines(
            final InputStream in,
            final java.nio.charset.Charset charset,
            final ConsumerX<String> lineHandler) throws InternalException {
        readLines(toReader(in, charset), lineHandler);
    }

    /**
     * Reads lines from a {@link Reader} and processes each line with a {@link ConsumerX}. The encoding of the data
     * follows the Reader's own encoding definition. This method does not close the stream unless an exception is
     * thrown.
     *
     * @param reader      The {@link Reader}.
     * @param lineHandler The line handler to process each line.
     * @throws InternalException If an I/O error occurs.
     */
    public static void readLines(final Reader reader, final ConsumerX<String> lineHandler) throws InternalException {
        Assert.notNull(reader);
        Assert.notNull(lineHandler);

        for (final String line : lineIter(reader)) {
            lineHandler.accept(line);
        }
    }

    /**
     * Reads content from an {@link InputStream} until a specified token is encountered.
     *
     * @param in    The input stream.
     * @param token The character token to stop reading at.
     * @return A {@link FastByteArrayOutputStream} containing the read content up to the token.
     */
    public static FastByteArrayOutputStream readToToken(final InputStream in, final int token) {
        return readTo(in, (c) -> c == token);
    }

    /**
     * Reads content from an {@link InputStream} until a specified condition defined by a {@link Predicate} is met.
     *
     * @param in        The input stream.
     * @param predicate The predicate to test each character. Reading stops when {@link Predicate#test(Object)} returns
     *                  {@code true}.
     * @return A {@link FastByteArrayOutputStream} containing the read content.
     */
    public static FastByteArrayOutputStream readTo(final InputStream in, final Predicate<Integer> predicate) {
        return StreamReader.of(in, false).readTo(predicate);
    }

    /**
     * Converts a {@link String} to a {@link ByteArrayInputStream} using UTF-8 encoding.
     *
     * @param content The string content.
     * @return A new {@link ByteArrayInputStream}, or {@code null} if the content is {@code null}.
     */
    public static ByteArrayInputStream toStream(final String content) {
        return toStream(content, Charset.UTF_8);
    }

    /**
     * Converts a {@link String} to a {@link ByteArrayInputStream} with a specified charset.
     *
     * @param content The string content.
     * @param charset The character set to use for encoding the string.
     * @return A new {@link ByteArrayInputStream}, or {@code null} if the content is {@code null}.
     */
    public static ByteArrayInputStream toStream(final String content, final java.nio.charset.Charset charset) {
        if (content == null) {
            return null;
        }
        return toStream(ByteKit.toBytes(content, charset));
    }

    /**
     * Converts a {@link File} to an {@link InputStream}.
     *
     * @param file The file, must not be {@code null}.
     * @return A new {@link InputStream} for the file.
     */
    public static InputStream toStream(final File file) {
        Assert.notNull(file);
        return toStream(file.toPath());
    }

    /**
     * Converts a {@link Path} to an {@link InputStream}.
     *
     * @param path The {@link Path}, must not be {@code null}.
     * @return A new {@link InputStream} for the path.
     * @throws InternalException If an I/O error occurs.
     */
    public static InputStream toStream(final Path path) {
        Assert.notNull(path);
        try {
            return Files.newInputStream(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts a byte array to a {@link ByteArrayInputStream}.
     *
     * @param content The byte array content.
     * @return A new {@link ByteArrayInputStream}, or {@code null} if the content is {@code null}.
     */
    public static ByteArrayInputStream toStream(final byte[] content) {
        if (content == null) {
            return null;
        }
        return new ByteArrayInputStream(content);
    }

    /**
     * Converts a {@link ByteArrayOutputStream} to a {@link ByteArrayInputStream}.
     *
     * @param out The {@link ByteArrayOutputStream}.
     * @return A new {@link ByteArrayInputStream}, or {@code null} if the output stream is {@code null}.
     */
    public static ByteArrayInputStream toStream(final ByteArrayOutputStream out) {
        if (out == null) {
            return null;
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Converts a {@link FastByteArrayOutputStream} to a {@link ByteArrayInputStream}.
     *
     * @param out The {@link FastByteArrayOutputStream}.
     * @return A new {@link ByteArrayInputStream}, or {@code null} if the output stream is {@code null}.
     */
    public static ByteArrayInputStream toStream(final FastByteArrayOutputStream out) {
        if (out == null) {
            return null;
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Converts an {@link InputStream} to a {@link BufferedInputStream}. If the input stream is already a
     * {@link BufferedInputStream}, it is cast and returned.
     *
     * @param in The {@link InputStream}, must not be {@code null}.
     * @return A {@link BufferedInputStream}.
     */
    public static BufferedInputStream toBuffered(final InputStream in) {
        Assert.notNull(in, "InputStream must be not null!");
        return (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new BufferedInputStream(in);
    }

    /**
     * Converts an {@link InputStream} to a {@link BufferedInputStream} with a specified buffer size. If the input
     * stream is already a {@link BufferedInputStream}, it is cast and returned.
     *
     * @param in         The {@link InputStream}, must not be {@code null}.
     * @param bufferSize The buffer size.
     * @return A {@link BufferedInputStream}.
     */
    public static BufferedInputStream toBuffered(final InputStream in, final int bufferSize) {
        Assert.notNull(in, "InputStream must be not null!");
        return (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new BufferedInputStream(in, bufferSize);
    }

    /**
     * Converts an {@link OutputStream} to a {@link BufferedOutputStream}. If the output stream is already a
     * {@link BufferedOutputStream}, it is cast and returned.
     *
     * @param out The {@link OutputStream}, must not be {@code null}.
     * @return A {@link BufferedOutputStream}.
     */
    public static BufferedOutputStream toBuffered(final OutputStream out) {
        Assert.notNull(out, "OutputStream must be not null!");
        return (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new BufferedOutputStream(out);
    }

    /**
     * Converts an {@link OutputStream} to a {@link BufferedOutputStream} with a specified buffer size. If the output
     * stream is already a {@link BufferedOutputStream}, it is cast and returned.
     *
     * @param out        The {@link OutputStream}, must not be {@code null}.
     * @param bufferSize The buffer size.
     * @return A {@link BufferedOutputStream}.
     */
    public static BufferedOutputStream toBuffered(final OutputStream out, final int bufferSize) {
        Assert.notNull(out, "OutputStream must be not null!");
        return (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out
                : new BufferedOutputStream(out, bufferSize);
    }

    /**
     * Converts a {@link Reader} to a {@link BufferedReader}. If the reader is already a {@link BufferedReader}, it is
     * cast and returned.
     *
     * @param reader The {@link Reader}, must not be {@code null}.
     * @return A {@link BufferedReader}.
     */
    public static BufferedReader toBuffered(final Reader reader) {
        Assert.notNull(reader, "Reader must be not null!");
        return (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * Converts a {@link Reader} to a {@link BufferedReader} with a specified buffer size. If the reader is already a
     * {@link BufferedReader}, it is cast and returned.
     *
     * @param reader     The {@link Reader}, must not be {@code null}.
     * @param bufferSize The buffer size.
     * @return A {@link BufferedReader}.
     */
    public static BufferedReader toBuffered(final Reader reader, final int bufferSize) {
        Assert.notNull(reader, "Reader must be not null!");
        return (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader, bufferSize);
    }

    /**
     * Converts a {@link Writer} to a {@link BufferedWriter}. If the writer is already a {@link BufferedWriter}, it is
     * cast and returned.
     *
     * @param writer The {@link Writer}, must not be {@code null}.
     * @return A {@link BufferedWriter}.
     */
    public static BufferedWriter toBuffered(final Writer writer) {
        Assert.notNull(writer, "Writer must be not null!");
        return (writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    /**
     * Converts a {@link Writer} to a {@link BufferedWriter} with a specified buffer size. If the writer is already a
     * {@link BufferedWriter}, it is cast and returned.
     *
     * @param writer     The {@link Writer}, must not be {@code null}.
     * @param bufferSize The buffer size.
     * @return A {@link BufferedWriter}.
     */
    public static BufferedWriter toBuffered(final Writer writer, final int bufferSize) {
        Assert.notNull(writer, "Writer must be not null!");
        return (writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer, bufferSize);
    }

    /**
     * Converts an {@link InputStream} to a mark-supported stream. If the original stream already supports marking, it
     * is returned directly. Otherwise, it is wrapped in a {@link BufferedInputStream}.
     *
     * @param in The input stream.
     * @return A mark-supported {@link InputStream}.
     */
    public static InputStream toMarkSupport(final InputStream in) {
        if (null == in) {
            return null;
        }
        if (!in.markSupported()) {
            return new BufferedInputStream(in);
        }
        return in;
    }

    /**
     * Converts a {@link Reader} to a mark-supported Reader. If the original Reader already supports marking, it is
     * returned directly. Otherwise, it is wrapped in a {@link BufferedReader}.
     *
     * @param reader The reader.
     * @return A mark-supported {@link Reader}.
     */
    public static Reader toMarkSupport(final Reader reader) {
        if (null == reader) {
            return null;
        }
        if (!reader.markSupported()) {
            return new BufferedReader(reader);
        }
        return reader;
    }

    /**
     * Creates a {@link PushbackReader}. If the provided reader is already a {@link PushbackReader}, it is cast and
     * returned. Otherwise, a new {@link PushbackReader} is created.
     *
     * @param reader       The ordinary Reader.
     * @param pushBackSize The size of the pushback buffer.
     * @return A {@link PushbackReader}.
     */
    public static PushbackReader toPushBackReader(final Reader reader, final int pushBackSize) {
        return (reader instanceof PushbackReader) ? (PushbackReader) reader : new PushbackReader(reader, pushBackSize);
    }

    /**
     * Converts an {@link InputStream} to a {@link PushbackInputStream}. If the provided input stream is already a
     * {@link PushbackInputStream}, it is cast and returned. Otherwise, a new {@link PushbackInputStream} is created.
     *
     * @param in           The {@link InputStream}.
     * @param pushBackSize The size of the pushback buffer.
     * @return A {@link PushbackInputStream}.
     */
    public static PushbackInputStream toPushbackStream(final InputStream in, final int pushBackSize) {
        return (in instanceof PushbackInputStream) ? (PushbackInputStream) in
                : new PushbackInputStream(in, pushBackSize);
    }

    /**
     * Converts a given {@link InputStream} to a stream where {@link InputStream#available()} works reliably. In socket
     * communication streams, {@link InputStream#available()} often returns {@code 0} when no data has been received
     * yet. To make it work, one byte needs to be read (which blocks if no data is available). Once a byte is read,
     * {@link InputStream#available()} will function correctly. Note that for network streams,
     * {@link InputStream#available()} may return the size of the current chunk, not the total length.
     * <p>
     * The returned object follows these rules:
     * <ul>
     * <li>{@link FileInputStream} returns the original object, as its {@code available} method is inherently
     * reliable.</li>
     * <li>Other {@link InputStream} types return a {@link PushbackInputStream}.</li>
     * </ul>
     *
     * @param in The input stream to convert.
     * @return The converted stream, potentially a {@link PushbackInputStream}.
     */
    public static InputStream toAvailableStream(final InputStream in) {
        if (in instanceof FileInputStream) {
            // FileInputStream's available method is inherently reliable.
            return in;
        }

        final PushbackInputStream pushbackInputStream = toPushbackStream(in, 1);
        try {
            final int available = pushbackInputStream.available();
            if (available <= 0) {
                // This operation will block until data is read.
                final int b = pushbackInputStream.read();
                pushbackInputStream.unread(b);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return pushbackInputStream;
    }

    /**
     * Writes a byte array to an {@link OutputStream} and closes the target stream.
     *
     * @param out     The output stream.
     * @param content The content to write.
     * @throws InternalException If an I/O error occurs.
     */
    public static void write(final OutputStream out, final byte[] content) throws InternalException {
        write(out, true, content);
    }

    /**
     * Writes a byte array to an {@link OutputStream}.
     *
     * @param out        The output stream.
     * @param isCloseOut Whether to close the output stream after writing.
     * @param content    The content to write.
     * @throws InternalException If an I/O error occurs.
     */
    public static void write(final OutputStream out, final boolean isCloseOut, final byte[] content)
            throws InternalException {
        StreamWriter.of(out, isCloseOut).write(content);
    }

    /**
     * Writes multiple {@link CharSequence} contents to an {@link OutputStream}, automatically converting them to UTF-8
     * strings.
     *
     * @param out        The output stream.
     * @param isCloseOut Whether to close the output stream after writing.
     * @param contents   The contents to write. Each element's {@code toString()} method is called. Newlines are not
     *                   automatically added.
     * @throws InternalException If an I/O error occurs.
     */
    public static void write(final OutputStream out, final boolean isCloseOut, final CharSequence... contents)
            throws InternalException {
        write(out, Charset.UTF_8, isCloseOut, contents);
    }

    /**
     * Writes multiple {@link CharSequence} contents to an {@link OutputStream} with a specified charset.
     *
     * @param out        The output stream.
     * @param charset    The character set to use for encoding the contents.
     * @param isCloseOut Whether to close the output stream after writing.
     * @param contents   The contents to write. Each element's {@code toString()} method is called. Newlines are not
     *                   automatically added.
     * @throws InternalException If an I/O error occurs.
     */
    public static void write(
            final OutputStream out,
            final java.nio.charset.Charset charset,
            final boolean isCloseOut,
            final CharSequence... contents) throws InternalException {
        StreamWriter.of(out, isCloseOut).writeString(charset, contents);
    }

    /**
     * Writes multiple {@link Object} contents to an {@link OutputStream}.
     *
     * @param out        The output stream.
     * @param isCloseOut Whether to close the output stream after writing.
     * @param contents   The contents to write.
     * @throws InternalException If an I/O error occurs.
     */
    public static void write(final OutputStream out, final boolean isCloseOut, final Object... contents)
            throws InternalException {
        StreamWriter.of(out, isCloseOut).writeObject(contents);
    }

    /**
     * Reads content from a {@link FileChannel} as a UTF-8 encoded string.
     *
     * @param fileChannel The file channel.
     * @return The content of the file channel as a string.
     * @throws InternalException If an I/O error occurs.
     */
    public static String read(final FileChannel fileChannel) throws InternalException {
        return read(fileChannel, Charset.UTF_8);
    }

    /**
     * Reads content from a {@link FileChannel} with a specified charset.
     *
     * @param fileChannel The file channel.
     * @param charset     The character set to use for decoding the bytes.
     * @return The content of the file channel as a string.
     * @throws InternalException If an I/O error occurs.
     */
    public static String read(final FileChannel fileChannel, final java.nio.charset.Charset charset)
            throws InternalException {
        final MappedByteBuffer buffer;
        try {
            buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()).load();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return StringKit.toString(buffer, charset);
    }

    /**
     * Reads content from a {@link ReadableByteChannel} into a {@link FastByteArrayOutputStream}. The channel is not
     * closed after reading.
     *
     * @param channel The readable byte channel.
     * @return A {@link FastByteArrayOutputStream} containing the read content.
     * @throws InternalException If an I/O error occurs.
     */
    public static FastByteArrayOutputStream read(final ReadableByteChannel channel) throws InternalException {
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        copy(channel, Channels.newChannel(out));
        return out;
    }

    /**
     * Reads content from a {@link ReadableByteChannel} with a specified charset. The channel is not closed after
     * reading.
     *
     * @param channel The readable byte channel.
     * @param charset The character set to use for decoding the bytes.
     * @return The content of the channel as a string.
     * @throws InternalException If an I/O error occurs.
     */
    public static String read(final ReadableByteChannel channel, final java.nio.charset.Charset charset)
            throws InternalException {
        final FastByteArrayOutputStream out = read(channel);
        return null == charset ? out.toString() : out.toString(charset);
    }

    /**
     * Copies content from one {@link FileChannel} to another using NIO. The channels are not closed after copying.
     *
     * @param in  The source {@link FileChannel}, must not be {@code null}.
     * @param out The destination {@link FileChannel}, must not be {@code null}.
     * @return The number of bytes copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(final FileChannel in, final FileChannel out) throws InternalException {
        Assert.notNull(in, "In channel is null!");
        Assert.notNull(out, "Out channel is null!");

        return FileChannelCopier.of().copy(in, out);
    }

    /**
     * Copies content from a {@link ReadableByteChannel} to a {@link WritableByteChannel} using NIO. The channels are
     * not closed after copying.
     *
     * @param in  The source {@link ReadableByteChannel}.
     * @param out The destination {@link WritableByteChannel}.
     * @return The number of bytes copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(final ReadableByteChannel in, final WritableByteChannel out) throws InternalException {
        return copy(in, out, Normal._8192);
    }

    /**
     * Copies content from a {@link ReadableByteChannel} to a {@link WritableByteChannel} using NIO with a specified
     * buffer size. The channels are not closed after copying.
     *
     * @param in         The source {@link ReadableByteChannel}.
     * @param out        The destination {@link WritableByteChannel}.
     * @param bufferSize The buffer size. If less than or equal to 0, the default size is used.
     * @return The number of bytes copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(final ReadableByteChannel in, final WritableByteChannel out, final int bufferSize)
            throws InternalException {
        return copy(in, out, bufferSize, null);
    }

    /**
     * Copies content from a {@link ReadableByteChannel} to a {@link WritableByteChannel} using NIO with a specified
     * buffer size and progress handler. The channels are not closed after copying.
     *
     * @param in             The source {@link ReadableByteChannel}.
     * @param out            The destination {@link WritableByteChannel}.
     * @param bufferSize     The buffer size. If less than or equal to 0, the default size is used.
     * @param streamProgress The {@link StreamProgress} progress handler.
     * @return The number of bytes copied.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copy(
            final ReadableByteChannel in,
            final WritableByteChannel out,
            final int bufferSize,
            final StreamProgress streamProgress) throws InternalException {
        return copy(in, out, bufferSize, -1, streamProgress);
    }

    /**
     * Copies content from a {@link ReadableByteChannel} to a {@link WritableByteChannel} using NIO with a specified
     * buffer size, total count, and progress handler. The channels are not closed after copying.
     *
     * @param in             The source {@link ReadableByteChannel}.
     * @param out            The destination {@link WritableByteChannel}.
     * @param bufferSize     The buffer size. If less than or equal to 0, the default size is used.
     * @param totalCount     The total number of bytes to read.
     * @param streamProgress The {@link StreamProgress} progress handler.
     * @return The number of bytes copied.
     */
    public static long copy(
            final ReadableByteChannel in,
            final WritableByteChannel out,
            final int bufferSize,
            final long totalCount,
            final StreamProgress streamProgress) {
        Assert.notNull(in, "In channel is null!");
        Assert.notNull(out, "Out channel is null!");
        return new ChannelCopier(bufferSize, totalCount, streamProgress).copy(in, out);
    }

    /**
     * Copies content from an {@link InputStream} to an {@link OutputStream} using NIO. This method does not close the
     * streams.
     *
     * @param in             The input stream.
     * @param out            The output stream.
     * @param bufferSize     The buffer size.
     * @param streamProgress The progress handler.
     * @return The number of bytes transferred.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copyNio(
            final InputStream in,
            final OutputStream out,
            final int bufferSize,
            final StreamProgress streamProgress) throws InternalException {
        return copyNio(in, out, bufferSize, -1, streamProgress);
    }

    /**
     * Copies content from an {@link InputStream} to an {@link OutputStream} using NIO. This method does not close the
     * streams.
     *
     * @param in             The input stream, must not be {@code null}.
     * @param out            The output stream, must not be {@code null}.
     * @param bufferSize     The buffer size. Use -1 for default size.
     * @param count          The maximum length to copy. Use -1 for unlimited.
     * @param streamProgress The progress handler, can be {@code null}.
     * @return The number of bytes transferred.
     * @throws InternalException If an I/O error occurs.
     */
    public static long copyNio(
            final InputStream in,
            final OutputStream out,
            final int bufferSize,
            final long count,
            final StreamProgress streamProgress) throws InternalException {
        Assert.notNull(in, "InputStream channel is null!");
        Assert.notNull(out, "OutputStream channel is null!");
        final long copySize = copy(
                Channels.newChannel(in),
                Channels.newChannel(out),
                bufferSize,
                count,
                streamProgress);
        flush(out);
        return copySize;
    }

    /**
     * Flushes data from a {@link Flushable} object. Any exceptions during flushing are silently ignored.
     *
     * @param flushable The {@link Flushable} object to flush.
     */
    public static void flush(final Flushable flushable) {
        if (null != flushable) {
            try {
                flushable.flush();
            } catch (final Exception e) {
                // Silently ignore
            }
        }
    }

    /**
     * Attempts to close the specified object. If the object implements {@link AutoCloseable}, its {@code close()}
     * method is called.
     *
     * @param object The object to close.
     */
    public static void close(final Object object) {
        if (object instanceof AutoCloseable) {
            closeQuietly((AutoCloseable) object);
        }
    }

    /**
     * Closes a {@link Closeable} object. Failure to close will not throw an exception, but runtime exceptions will be
     * rethrown.
     *
     * @param closeable The {@link Closeable} object to close.
     */
    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
                // Silently ignore other exceptions
            }
        }
    }

    /**
     * Closes an {@link AutoCloseable} object. Failure to close will not throw an exception.
     *
     * @param autoCloseable The {@link AutoCloseable} object to close.
     */
    public static void close(AutoCloseable autoCloseable) {
        if (null != autoCloseable) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                // Silently close
            }
        }
    }

    /**
     * Closes a {@link Socket} object. Failure to close will not throw an exception, but runtime exceptions will be
     * rethrown. Handles Android-specific `getsockname failed` AssertionError.
     *
     * @param socket The {@link Socket} object to close.
     */
    public static void close(Socket socket) {
        if (null != socket) {
            try {
                socket.close();
            } catch (AssertionError e) {
                if (!isAndroidGetsocknameError(e))
                    throw e;
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
                // Silently ignore other exceptions
            }
        }
    }

    /**
     * Closes a {@link ServerSocket} object, ignoring any checked exceptions. If the {@code serverSocket} is
     * {@code null}, no operation is performed.
     *
     * @param serverSocket The {@link ServerSocket} object to close.
     */
    public static void close(ServerSocket serverSocket) {
        if (null != serverSocket) {
            try {
                serverSocket.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
                // Silently ignore other exceptions
            }
        }
    }

    /**
     * Closes an {@link AsynchronousSocketChannel}. Attempts to shutdown input and output before closing the channel.
     *
     * @param channel The {@link AsynchronousSocketChannel} to close.
     */
    public static void close(AsynchronousSocketChannel channel) {
        boolean connected = true;
        try {
            channel.shutdownInput();
        } catch (IOException ignored) {
        } catch (NotYetConnectedException e) {
            connected = false;
        }
        try {
            if (connected) {
                channel.shutdownOutput();
            }
        } catch (IOException | NotYetConnectedException ignored) {
        }
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Closes a series of {@link AutoCloseable} objects in the given order. These objects must be closed in sequence,
     * otherwise errors may occur. Exceptions during closing are silently ignored.
     *
     * @param closeables The {@link AutoCloseable} objects to close.
     */
    public static void closeQuietly(final AutoCloseable... closeables) {
        for (final AutoCloseable closeable : closeables) {
            close(closeable);
        }
    }

    /**
     * Returns true if the given {@link AssertionError} is due to a firmware bug fixed after Android 4.2.2. Refer to:
     * <a href="https://code.google.com/p/android/issues/detail?id=54072">Android Issue 54072</a>
     *
     * @param e The {@link AssertionError} to check.
     * @return {@code true} if the error is an Android getsockname error, {@code false} otherwise.
     */
    public static boolean isAndroidGetsocknameError(AssertionError e) {
        return null != e.getCause() && null != e.getMessage() && e.getMessage().contains("getsockname failed");
    }

    /**
     * Closes a {@link Closeable} object. If the object is not {@code null}, its {@code close()} method is called.
     * Throws an {@link IOException} if closing fails.
     *
     * @param closeable The {@link Closeable} object to close.
     * @throws IOException If an I/O error occurs during closing.
     */
    public static void nullSafeClose(final Closeable closeable) throws IOException {
        if (null != closeable) {
            closeable.close();
        }
    }

    /**
     * Returns a {@link Sink} that writes to the given {@link OutputStream}.
     *
     * @param out The output stream.
     * @return A new {@link Sink}.
     */
    public static Sink sink(OutputStream out) {
        return sink(out, new Timeout());
    }

    /**
     * Returns a {@link Sink} that writes to the given {@link OutputStream} with a specified timeout. This method is
     * preferred over {@link #sink(OutputStream)} when dealing with sockets, as it supports timeouts. When a socket
     * write times out, the socket will be asynchronously closed by a watchdog thread.
     *
     * @param out     The data output stream.
     * @param timeout The timeout information.
     * @return A new {@link Sink}.
     * @throws IllegalArgumentException if {@code out} or {@code timeout} is {@code null}.
     */
    private static Sink sink(final OutputStream out, final Timeout timeout) {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        if (null == timeout) {
            throw new IllegalArgumentException("timeout == null");
        }

        return new Sink() {

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                checkOffsetAndCount(source.size, 0, byteCount);
                while (byteCount > 0) {
                    timeout.throwIfReached();
                    SectionBuffer head = source.head;
                    int toCopy = (int) Math.min(byteCount, head.limit - head.pos);
                    out.write(head.data, head.pos, toCopy);

                    head.pos += toCopy;
                    byteCount -= toCopy;
                    source.size -= toCopy;

                    if (head.pos == head.limit) {
                        source.head = head.pop();
                        LifeCycle.recycle(head);
                    }
                }
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public void flush() throws IOException {
                out.flush();
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public void close() throws IOException {
                out.close();
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public Timeout timeout() {
                return timeout;
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public String toString() {
                return "sink(" + out + Symbol.PARENTHESE_RIGHT;
            }
        };
    }

    /**
     * Returns a {@link Sink} that writes to the given {@link Socket}. This method is preferred over
     * {@link #sink(OutputStream)} as it supports timeouts. When a socket write times out, the socket will be
     * asynchronously closed by a task thread.
     *
     * @param socket The socket.
     * @return A new {@link Sink}.
     * @throws IOException              If an I/O error occurs or the socket's output stream is {@code null}.
     * @throws IllegalArgumentException if {@code socket} is {@code null}.
     */
    public static Sink sink(Socket socket) throws IOException {
        if (null == socket) {
            throw new IllegalArgumentException("socket == null");
        }
        if (null == socket.getOutputStream()) {
            throw new IOException("socket's output stream == null");
        }
        AsyncTimeout timeout = timeout(socket);
        Sink sink = sink(socket.getOutputStream(), timeout);
        return timeout.sink(sink);
    }

    /**
     * Returns a buffered {@link Source} that reads from the given {@link InputStream}.
     *
     * @param in The data input stream.
     * @return A new buffered {@link Source}.
     */
    public static Source source(InputStream in) {
        return source(in, new Timeout());
    }

    /**
     * Returns a buffered {@link Source} that reads from the given {@link InputStream} with a specified timeout.
     *
     * @param in      The data input stream.
     * @param timeout The timeout information.
     * @return A new buffered {@link Source}.
     * @throws IllegalArgumentException if {@code in} or {@code timeout} is {@code null}.
     */
    private static Source source(final InputStream in, final Timeout timeout) {
        if (null == in) {
            throw new IllegalArgumentException("in == null");
        }
        if (null == timeout) {
            throw new IllegalArgumentException("timeout == null");
        }

        return new Source() {

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                if (byteCount < 0)
                    throw new IllegalArgumentException("byteCount < 0: " + byteCount);
                if (byteCount == 0)
                    return 0;
                try {
                    timeout.throwIfReached();
                    SectionBuffer tail = sink.writableSegment(1);
                    int maxToCopy = (int) Math.min(byteCount, SectionBuffer.SIZE - tail.limit);
                    int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
                    if (bytesRead == -1) {
                        if (tail.pos == tail.limit) {
                            // We allocated a tail segment, but didn't end up needing it. Recycle!
                            sink.head = tail.pop();
                            LifeCycle.recycle(tail);
                        }
                        return -1;
                    }
                    tail.limit += bytesRead;
                    sink.size += bytesRead;
                    return bytesRead;
                } catch (AssertionError e) {
                    if (isAndroidGetsocknameError(e))
                        throw new IOException(e);
                    throw e;
                }
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public void close() throws IOException {
                in.close();
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public Timeout timeout() {
                return timeout;
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public String toString() {
                return "source(" + in + Symbol.PARENTHESE_RIGHT;
            }
        };
    }

    /**
     * Returns a buffered {@link Source} that reads from the given {@link File}.
     *
     * @param file The file.
     * @return A new buffered {@link Source}.
     * @throws FileNotFoundException    If the file does not exist.
     * @throws IllegalArgumentException if {@code file} is {@code null}.
     */
    public static Source source(File file) throws FileNotFoundException {
        if (null == file) {
            throw new IllegalArgumentException("file == null");
        }
        return source(new FileInputStream(file));
    }

    /**
     * Returns a buffered {@link Source} that reads from the given {@link Path}.
     *
     * @param path    The path.
     * @param options Open options for the file.
     * @return A new buffered {@link Source}.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException if {@code path} is {@code null}.
     */
    public static Source source(Path path, OpenOption... options) throws IOException {
        if (null == path) {
            throw new IllegalArgumentException("path == null");
        }
        return source(Files.newInputStream(path, options));
    }

    /**
     * Returns a {@link Sink} that writes to the given {@link File}.
     *
     * @param file The file.
     * @return A new {@link Sink}.
     * @throws FileNotFoundException    If the file cannot be opened for writing.
     * @throws IllegalArgumentException if {@code file} is {@code null}.
     */
    public static Sink sink(File file) throws FileNotFoundException {
        if (null == file) {
            throw new IllegalArgumentException("file == null");
        }
        return sink(new FileOutputStream(file));
    }

    /**
     * Returns a {@link Sink} that appends to the given {@link File}.
     *
     * @param file The file.
     * @return A new {@link Sink} for appending.
     * @throws FileNotFoundException    If the file cannot be opened for appending.
     * @throws IllegalArgumentException if {@code file} is {@code null}.
     */
    public static Sink appendingSink(File file) throws FileNotFoundException {
        if (null == file) {
            throw new IllegalArgumentException("file == null");
        }
        return sink(new FileOutputStream(file, true));
    }

    /**
     * Returns a {@link Sink} that writes to the given {@link Path}.
     *
     * @param path    The path.
     * @param options Open options for the file.
     * @return A new {@link Sink} for writing data.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException if {@code path} is {@code null}.
     */
    public static Sink sink(Path path, OpenOption... options) throws IOException {
        if (null == path) {
            throw new IllegalArgumentException("path == null");
        }
        return sink(Files.newOutputStream(path, options));
    }

    /**
     * Returns a "blackhole" {@link Sink} that discards all written data.
     *
     * @return A new blackhole {@link Sink}.
     */
    public static Sink blackhole() {
        return new Sink() {

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                source.skip(byteCount);
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public void flush() {
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public Timeout timeout() {
                return Timeout.NONE;
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public void close() {
            }
        };
    }

    /**
     * Returns a buffered {@link Source} that reads from the given {@link Socket}. This method is preferred over
     * {@link #source(InputStream)} as it supports timeouts. When a socket read times out, the socket will be
     * asynchronously closed by a task thread.
     *
     * @param socket The socket.
     * @return A new buffered {@link Source}.
     * @throws IOException              If an I/O error occurs or the socket's input stream is {@code null}.
     * @throws IllegalArgumentException if {@code socket} is {@code null}.
     */
    public static Source source(Socket socket) throws IOException {
        if (null == socket) {
            throw new IllegalArgumentException("socket == null");
        }
        if (null == socket.getInputStream()) {
            throw new IOException("socket's input stream == null");
        }
        AsyncTimeout timeout = timeout(socket);
        Source source = source(socket.getInputStream(), timeout);
        return timeout.source(source);
    }

    /**
     * Creates an {@link AsyncTimeout} for the given {@link Socket}. This timeout will close the socket if a timeout
     * occurs.
     *
     * @param socket The socket to associate with the timeout.
     * @return A new {@link AsyncTimeout}.
     */
    private static AsyncTimeout timeout(final Socket socket) {
        return new AsyncTimeout() {

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            protected IOException newTimeoutException(IOException cause) {
                InterruptedIOException ioe = new SocketTimeoutException("timeout");
                if (null != cause) {
                    ioe.initCause(cause);
                }
                return ioe;
            }

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            protected void timedOut() {
                try {
                    socket.close();
                } catch (Exception e) {
                    Console.log("Failed to close timed out socket " + socket, e);
                } catch (AssertionError e) {
                    if (isAndroidGetsocknameError(e)) {
                        Console.log("Failed to close timed out socket " + socket, e);
                    } else {
                        throw e;
                    }
                }
            }
        };
    }

    /**
     * Compares the content of two {@link InputStream}s for equality. Internally, streams are converted to
     * {@link BufferedInputStream}s.
     *
     * @param input1 The first input stream.
     * @param input2 The second input stream.
     * @return {@code true} if the contents of the two streams are identical, {@code false} otherwise.
     * @throws InternalException If an I/O error occurs.
     */
    public static boolean contentEquals(InputStream input1, InputStream input2) throws InternalException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }

        try {
            int ch = input1.read();
            while (Normal.__1 != ch) {
                final int ch2 = input2.read();
                if (ch != ch2) {
                    return false;
                }
                ch = input1.read();
            }

            final int ch2 = input2.read();
            return ch2 == Normal.__1;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Compares the content of two {@link Reader}s for equality. Internally, readers are converted to
     * {@link BufferedReader}s.
     *
     * @param input1 The first reader.
     * @param input2 The second reader.
     * @return {@code true} if the contents of the two readers are identical, {@code false} otherwise.
     * @throws InternalException If an I/O error occurs.
     */
    public static boolean contentEquals(Reader input1, Reader input2) throws InternalException {
        input1 = toBuffered(input1);
        input2 = toBuffered(input2);

        try {
            int ch = input1.read();
            while (Normal.__1 != ch) {
                final int ch2 = input2.read();
                if (ch != ch2) {
                    return false;
                }
                ch = input1.read();
            }

            final int ch2 = input2.read();
            return ch2 == Normal.__1;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Compares the content of two {@link Reader}s for equality, ignoring End-Of-Line (EOL) characters. Internally,
     * readers are converted to {@link BufferedReader}s.
     *
     * @param input1 The first reader.
     * @param input2 The second reader.
     * @return {@code true} if the contents of the two readers are identical (ignoring EOL), {@code false} otherwise.
     * @throws InternalException If an I/O error occurs.
     */
    public static boolean contentEqualsIgnoreEOL(final Reader input1, final Reader input2) throws InternalException {
        final BufferedReader br1 = toBuffered(input1);
        final BufferedReader br2 = toBuffered(input2);

        try {
            String line1 = br1.readLine();
            String line2 = br2.readLine();
            while (line1 != null && line1.equals(line2)) {
                line1 = br1.readLine();
                line2 = br2.readLine();
            }
            return Objects.equals(line1, line2);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Returns a {@link LineIterator} for the given {@link Reader}.
     * <p>
     * Example usage:
     * 
     * <pre>
     * LineIterator it = null;
     * try {
     *     it = IoKit.lineIter(reader);
     *     while (it.hasNext()) {
     *         String line = it.nextLine();
     *         // do something with line
     *     }
     * } finally {
     *     it.close();
     * }
     * </pre>
     *
     * @param reader The {@link Reader}.
     * @return A new {@link LineIterator}.
     */
    public static LineIterator lineIter(final Reader reader) {
        return new LineIterator(reader);
    }

    /**
     * Returns a {@link LineIterator} for the given {@link InputStream} and charset.
     * <p>
     * Example usage:
     * 
     * <pre>
     * LineIterator it = null;
     * try {
     *     it = IoKit.lineIter(in, Charset.UTF_8);
     *     while (it.hasNext()) {
     *         String line = it.nextLine();
     *         // do something with line
     *     }
     * } finally {
     *     it.close();
     * }
     * </pre>
     *
     * @param in      The {@link InputStream}.
     * @param charset The encoding.
     * @return A new {@link LineIterator}.
     */
    public static LineIterator lineIter(final InputStream in, final java.nio.charset.Charset charset) {
        return new LineIterator(in, charset);
    }

    /**
     * Converts a {@link ByteArrayOutputStream} to a {@link String} using the specified charset.
     *
     * @param out     The {@link ByteArrayOutputStream}.
     * @param charset The encoding.
     * @return The string representation of the output stream's content.
     */
    public static String toString(final ByteArrayOutputStream out, final java.nio.charset.Charset charset) {
        return out.toString(charset);
    }

    /**
     * Checks if the given offset and byte count are within the bounds of the total size. Throws an
     * {@link ArrayIndexOutOfBoundsException} if the range is invalid.
     *
     * @param size      The total size.
     * @param offset    The starting offset.
     * @param byteCount The number of bytes.
     * @throws ArrayIndexOutOfBoundsException if the offset or byteCount are negative, or if the range extends beyond
     *                                        the size.
     */
    public static void checkOffsetAndCount(long size, long offset, long byteCount) {
        if ((offset | byteCount) < 0 || offset > size || size - offset < byteCount) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("size=%s offset=%s byteCount=%s", size, offset, byteCount));
        }
    }

    /**
     * Rethrows a {@link Throwable} as an unchecked exception without modifying its stack trace. This is a dangerous
     * practice and should be used with extreme caution.
     *
     * @param t The {@link Throwable} to rethrow.
     */
    public static void sneakyRethrow(Throwable t) {
        IoKit.<Error>sneakyThrow2(t);
    }

    /**
     * Helper method for {@link #sneakyRethrow(Throwable)} to bypass checked exception compilation.
     *
     * @param t   The {@link Throwable} to rethrow.
     * @param <T> The type of the throwable.
     * @throws T The rethrown throwable.
     */
    private static <T extends Throwable> void sneakyThrow2(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * Compares a range of bytes in two byte arrays for equality.
     *
     * @param a         The first byte array.
     * @param aOffset   The starting offset in the first array.
     * @param b         The second byte array.
     * @param bOffset   The starting offset in the second array.
     * @param byteCount The number of bytes to compare.
     * @return {@code true} if the specified ranges are equal, {@code false} otherwise.
     */
    public static boolean arrayRangeEquals(byte[] a, int aOffset, byte[] b, int bOffset, int byteCount) {
        for (int i = 0; i < byteCount; i++) {
            if (a[i + aOffset] != b[i + bOffset])
                return false;
        }
        return true;
    }

    /**
     * Returns a buffered {@link BufferSource} that reads bytes from the given {@link Source}. The returned source will
     * perform bulk reads into its memory buffer.
     *
     * @param source The byte stream source.
     * @return A new {@link BufferSource}.
     */
    public static BufferSource buffer(Source source) {
        return new RealSource(source);
    }

    /**
     * Returns a new {@link BufferSink} that buffers writes to the given {@link Sink}. The returned sink will perform
     * bulk writes to the underlying {@code sink}.
     *
     * @param sink The byte stream sink.
     * @return A new {@link BufferSink}.
     */
    public static BufferSink buffer(Sink sink) {
        return new RealSink(sink);
    }

    /**
     * Reverses the byte order of a short integer.
     *
     * @param s The short integer.
     * @return The short integer with reversed byte order.
     */
    public static short reverseBytesShort(short s) {
        int i = s & 0xffff;
        int reversed = (i & 0xff00) >>> 8 | (i & 0x00ff) << 8;
        return (short) reversed;
    }

    /**
     * Reverses the byte order of an integer.
     *
     * @param i The integer.
     * @return The integer with reversed byte order.
     */
    public static int reverseBytesInt(int i) {
        return (i & 0xff000000) >>> 24 | (i & 0x00ff0000) >>> 8 | (i & 0x0000ff00) << 8 | (i & 0x000000ff) << 24;
    }

    /**
     * Reverses the byte order of a long integer.
     *
     * @param v The long integer.
     * @return The long integer with reversed byte order.
     */
    public static long reverseBytesLong(long v) {
        return (v & 0xff00000000000000L) >>> 56 | (v & 0x00ff000000000000L) >>> 40 | (v & 0x0000ff0000000000L) >>> 24
                | (v & 0x000000ff00000000L) >>> 8 | (v & 0x00000000ff000000L) << 8 | (v & 0x0000000000ff0000L) << 24
                | (v & 0x000000000000ff00L) << 40 | (v & 0x00000000000000ffL) << 56;
    }

    /**
     * Opens an {@link InputStream} for a given file path or URL string. Supports "resource:" prefix for classpath
     * resources.
     *
     * @param name The file path or URL string.
     * @return A new {@link InputStream}.
     * @throws IOException           If an I/O error occurs.
     * @throws FileNotFoundException If the resource or file is not found.
     */
    public static InputStream openFileOrURL(String name) throws IOException {
        if (name.startsWith("resource:")) {
            URL url = ResourceKit.getResourceUrl(name.substring(9), IoKit.class);
            if (null == url)
                throw new FileNotFoundException(name);
            return url.openStream();
        }
        if (name.indexOf(Symbol.C_COLON) < 2)
            return new FileInputStream(name);
        return URI.create(name).toURL().openStream();
    }

    /**
     * Gets the length of an {@link InputStream}. For {@link FileInputStream}, it calls
     * {@link FileInputStream#available()}. For other streams, it returns -1. Note that for network streams,
     * {@code available()} might return the segment size, not the total length.
     *
     * @param in The input stream.
     * @return The length of the stream, or -1 if the length is unknown.
     * @throws InternalException If an I/O error occurs when checking available bytes for a {@link FileInputStream}.
     */
    public static int length(final InputStream in) {
        if (in instanceof FileInputStream) {
            try {
                return in.available();
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
        return -1;
    }

}
