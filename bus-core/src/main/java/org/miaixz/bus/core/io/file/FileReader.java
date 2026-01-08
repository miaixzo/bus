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
package org.miaixz.bus.core.io.file;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * File reader. This class provides utility methods for reading content from files, supporting various data types and
 * character encodings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileReader extends FileWrapper {

    @Serial
    private static final long serialVersionUID = 2852285708261L;

    /**
     * Constructs a new {@code FileReader} instance. The file is checked for existence and if it is a regular file upon
     * construction.
     *
     * @param file    The file to read. Must not be {@code null}.
     * @param charset The character set for reading the file, using {@link java.nio.charset.Charset}.
     * @throws InternalException if the file does not exist or is not a regular file.
     */
    public FileReader(final File file, final java.nio.charset.Charset charset) {
        super(file, charset);
        checkFile();
    }

    /**
     * Creates a {@code FileReader} instance with the specified file and character set.
     *
     * @param file    The file to read. Must not be {@code null}.
     * @param charset The character set for reading the file, using {@link java.nio.charset.Charset}.
     * @return A new {@code FileReader} instance.
     * @throws InternalException if the file does not exist or is not a regular file.
     */
    public static FileReader of(final File file, final java.nio.charset.Charset charset) {
        return new FileReader(file, charset);
    }

    /**
     * Creates a {@code FileReader} instance with the specified file and {@link Charset#UTF_8} encoding.
     *
     * @param file The file to read. Must not be {@code null}.
     * @return A new {@code FileReader} instance.
     * @throws InternalException if the file does not exist or is not a regular file.
     */
    public static FileReader of(final File file) {
        return new FileReader(file, Charset.UTF_8);
    }

    /**
     * Reads all bytes from the file. The file length cannot exceed {@link Integer#MAX_VALUE}.
     *
     * @return The byte array containing all file data.
     * @throws InternalException if an I/O error occurs during reading.
     */
    public byte[] readBytes() throws InternalException {
        try {
            return Files.readAllBytes(this.file.toPath());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Reads the entire content of the file as a string using the specified character set.
     *
     * @return The content of the file as a string.
     * @throws InternalException if an I/O error occurs during reading.
     */
    public String readString() throws InternalException {
        // JDK11+ no longer recommends this method; Files.readString is recommended.
        return new String(readBytes(), this.charset);
    }

    /**
     * Reads each line from the file and adds it to the given collection.
     *
     * @param <T>        The type of the collection, which must extend {@code Collection<String>}.
     * @param collection The collection to add the lines to. Must not be {@code null}.
     * @return The collection containing each line of the file.
     * @throws InternalException if an I/O error occurs during reading.
     */
    public <T extends Collection<String>> T readLines(final T collection) throws InternalException {
        return readLines(collection, null);
    }

    /**
     * Reads each line from the file and adds it to the given collection if it satisfies the provided predicate.
     *
     * @param <T>        The type of the collection, which must extend {@code Collection<String>}.
     * @param collection The collection to add the lines to. Must not be {@code null}.
     * @param predicate  The predicate to test each line. Only lines that return {@code true} will be added. If
     *                   {@code null}, all lines are added.
     * @return The collection containing each line of the file that satisfied the predicate.
     * @throws InternalException if an I/O error occurs during reading.
     */
    public <T extends Collection<String>> T readLines(final T collection, final Predicate<String> predicate)
            throws InternalException {
        readLines((ConsumerX<String>) s -> {
            if (null == predicate || predicate.test(s)) {
                collection.add(s);
            }
        });
        return collection;
    }

    /**
     * Processes each line of the file content with the given line handler. The file is read line by line, and each line
     * is passed to the {@code lineHandler}.
     *
     * @param lineHandler The handler to process each line. Must not be {@code null}.
     * @throws InternalException if an I/O error occurs during reading.
     */
    public void readLines(final ConsumerX<String> lineHandler) throws InternalException {
        BufferedReader reader = null;
        try {
            reader = FileKit.getReader(file, charset);
            IoKit.readLines(reader, lineHandler);
        } finally {
            IoKit.closeQuietly(reader);
        }
    }

    /**
     * Reads all lines from the file into a new {@link List}.
     *
     * @return A new {@link List} containing each line of the file.
     * @throws InternalException if an I/O error occurs during reading.
     */
    public List<String> readLines() throws InternalException {
        return readLines(new ArrayList<>());
    }

    /**
     * Reads data from the file using the given {@code readerHandler}. This method provides a {@link BufferedReader} to
     * the handler for custom reading logic.
     *
     * @param <T>           The type of the result object returned by the {@code readerHandler}.
     * @param readerHandler The handler to process the {@link BufferedReader}. Must not be {@code null}.
     * @return The data read from the file, as returned by the {@code readerHandler}.
     * @throws InternalException if an I/O error occurs during reading or if the {@code readerHandler} throws an
     *                           exception.
     */
    public <T> T read(final FunctionX<BufferedReader, T> readerHandler) throws InternalException {
        BufferedReader reader = null;
        T result;
        try {
            reader = FileKit.getReader(this.file, charset);
            result = readerHandler.applying(reader);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        } finally {
            IoKit.closeQuietly(reader);
        }
        return result;
    }

    /**
     * Gets a {@link BufferedReader} for this file, using the configured character set.
     *
     * @return A {@link BufferedReader} object for reading the file.
     * @throws InternalException if an I/O error occurs while creating the reader.
     */
    public BufferedReader getReader() throws InternalException {
        return IoKit.toReader(getInputStream(), this.charset);
    }

    /**
     * Gets an {@link BufferedInputStream} for this file.
     *
     * @return An {@link BufferedInputStream} object for reading the file.
     * @throws InternalException if an I/O error occurs while creating the input stream.
     */
    public BufferedInputStream getInputStream() throws InternalException {
        try {
            return new BufferedInputStream(Files.newInputStream(this.file.toPath()));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Writes the entire content of the file to the given output stream. The output stream is not closed by this method.
     *
     * @param out The output stream to write to. Must not be {@code null}.
     * @return The number of bytes written to the stream.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public long writeToStream(final OutputStream out) throws InternalException {
        return writeToStream(out, false);
    }

    /**
     * Writes the entire content of the file to the given output stream.
     *
     * @param out        The output stream to write to. Must not be {@code null}.
     * @param isCloseOut Whether to close the output stream after writing. If {@code true}, the stream will be closed.
     * @return The number of bytes written to the stream.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public long writeToStream(final OutputStream out, final boolean isCloseOut) throws InternalException {
        try (final FileInputStream in = new FileInputStream(this.file)) {
            return IoKit.copy(in, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (isCloseOut) {
                IoKit.closeQuietly(out);
            }
        }
    }

    /**
     * Checks the validity of the file associated with this reader. Ensures that the file exists and is a regular file.
     *
     * @throws InternalException if the file does not exist or is not a regular file.
     */
    private void checkFile() throws InternalException {
        if (!file.exists()) {
            throw new InternalException("File not exist: " + file);
        }
        if (!file.isFile()) {
            throw new InternalException("Not a file:" + file);
        }
    }

}
