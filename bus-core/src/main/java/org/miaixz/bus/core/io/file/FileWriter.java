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
package org.miaixz.bus.core.io.file;

import java.io.*;
import java.nio.file.OpenOption;
import java.util.Map;
import java.util.Map.Entry;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * File writer. This class provides utility methods for writing content to files, supporting various data types,
 * character encodings, and append/overwrite modes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileWriter extends FileWrapper {

    @Serial
    private static final long serialVersionUID = 2852227982959L;

    /**
     * Constructs a new {@code FileWriter} instance. The file is checked for validity upon construction.
     *
     * @param file    The file to write to. Must not be {@code null}.
     * @param charset The character set for writing, using {@link java.nio.charset.Charset}.
     * @throws InternalException if the file is {@code null} or if it exists and is not a regular file.
     */
    public FileWriter(final File file, final java.nio.charset.Charset charset) {
        super(file, charset);
        checkFile();
    }

    /**
     * Constructs a new {@code FileWriter} instance. The file path is resolved, and the file is checked for validity
     * upon construction.
     *
     * @param filePath The file path. Relative paths will be converted to paths relative to the ClassPath.
     * @param charset  The character set for writing, using {@link java.nio.charset.Charset}.
     * @throws InternalException if the file is {@code null} or if it exists and is not a regular file.
     */
    public FileWriter(final String filePath, final java.nio.charset.Charset charset) {
        this(FileKit.file(filePath), charset);
    }

    /**
     * Constructs a new {@code FileWriter} instance. The file path is resolved, and the file is checked for validity
     * upon construction.
     *
     * @param filePath The file path. Relative paths will be converted to paths relative to the ClassPath.
     * @param charset  The character set for writing, using {@link Charset#charset(String)}.
     * @throws InternalException if the file is {@code null} or if it exists and is not a regular file.
     */
    public FileWriter(final String filePath, final String charset) {
        this(FileKit.file(filePath), Charset.charset(charset));
    }

    /**
     * Constructs a new {@code FileWriter} instance with {@link Charset#UTF_8} encoding. The file is checked for
     * validity upon construction.
     *
     * @param file The file to write to. Must not be {@code null}.
     * @throws InternalException if the file is {@code null} or if it exists and is not a regular file.
     */
    public FileWriter(final File file) {
        this(file, Charset.UTF_8);
    }

    /**
     * Constructs a new {@code FileWriter} instance with {@link Charset#UTF_8} encoding. The file path is resolved, and
     * the file is checked for validity upon construction.
     *
     * @param filePath The file path. Relative paths will be converted to paths relative to the ClassPath.
     * @throws InternalException if the file is {@code null} or if it exists and is not a regular file.
     */
    public FileWriter(final String filePath) {
        this(filePath, Charset.UTF_8);
    }

    /**
     * Creates a {@code FileWriter} instance with the specified file and character set.
     *
     * @param file    The file to write to. Must not be {@code null}.
     * @param charset The character set for writing, using {@link java.nio.charset.Charset}.
     * @return A new {@code FileWriter} instance.
     * @throws InternalException if the file is {@code null} or if it exists and is not a regular file.
     */
    public static FileWriter of(final File file, final java.nio.charset.Charset charset) {
        return new FileWriter(file, charset);
    }

    /**
     * Creates a {@code FileWriter} instance with the specified file and {@link Charset#UTF_8} encoding.
     *
     * @param file The file to write to. Must not be {@code null}.
     * @return A new {@code FileWriter} instance.
     * @throws InternalException if the file is {@code null} or if it exists and is not a regular file.
     */
    public static FileWriter of(final File file) {
        return new FileWriter(file);
    }

    /**
     * Writes a string to the file.
     *
     * @param content  The content string to write. Must not be {@code null}.
     * @param isAppend {@code true} to append to the file; {@code false} to overwrite existing content.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public File write(final String content, final boolean isAppend) throws InternalException {
        BufferedWriter writer = null;
        try {
            writer = getWriter(isAppend);
            writer.write(content);
            writer.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(writer);
        }
        return file;
    }

    /**
     * Writes a string to the file, overwriting any existing content.
     *
     * @param content The content string to write. Must not be {@code null}.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public File write(final String content) throws InternalException {
        return write(content, false);
    }

    /**
     * Appends a string to the end of the file.
     *
     * @param content The content string to append. Must not be {@code null}.
     * @return The target file after the append operation.
     * @throws InternalException if an I/O error occurs during appending.
     */
    public File append(final String content) throws InternalException {
        return write(content, true);
    }

    /**
     * Writes a list of strings to the file, overwriting existing content. Each string in the list will be written on a
     * new line.
     *
     * @param <T>  The type of elements in the list, which will be converted to strings.
     * @param list The list of objects to write. Each object's {@code toString()} method will be used.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public <T> File writeLines(final Iterable<T> list) throws InternalException {
        return writeLines(list, false);
    }

    /**
     * Appends a list of strings to the file. Each string in the list will be appended on a new line.
     *
     * @param <T>  The type of elements in the list, which will be converted to strings.
     * @param list The list of objects to append. Each object's {@code toString()} method will be used.
     * @return The target file after the append operation.
     * @throws InternalException if an I/O error occurs during appending.
     */
    public <T> File appendLines(final Iterable<T> list) throws InternalException {
        return writeLines(list, true);
    }

    /**
     * Writes a list of strings to the file, with an option to append or overwrite. Each string in the list will be
     * written on a new line.
     *
     * @param <T>      The type of elements in the list, which will be converted to strings.
     * @param list     The list of objects to write. Each object's {@code toString()} method will be used.
     * @param isAppend {@code true} to append to the file; {@code false} to overwrite existing content.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public <T> File writeLines(final Iterable<T> list, final boolean isAppend) throws InternalException {
        return writeLines(list, null, isAppend);
    }

    /**
     * Writes a list of strings to the file with a specified line separator, and an option to append or overwrite.
     *
     * @param <T>           The type of elements in the list, which will be converted to strings.
     * @param list          The list of objects to write. Each object's {@code toString()} method will be used.
     * @param lineSeparator The {@link LineSeparator} to use between lines. If {@code null}, the default system line
     *                      separator is used.
     * @param isAppend      {@code true} to append to the file; {@code false} to overwrite existing content.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public <T> File writeLines(final Iterable<T> list, final LineSeparator lineSeparator, final boolean isAppend) {
        return writeLines(list, lineSeparator, isAppend, true);
    }

    /**
     * Writes a list of strings to the file with a specified line separator and control over appending a line separator
     * to the last line.
     *
     * @param <T>                 The type of elements in the list, which will be converted to strings.
     * @param list                The list of objects to write. Each object's {@code toString()} method will be used.
     * @param lineSeparator       The {@link LineSeparator} to use between lines. If {@code null}, the default system
     *                            line separator is used.
     * @param isAppend            {@code true} to append to the file; {@code false} to overwrite existing content.
     * @param appendLineSeparator {@code true} to append a line separator to the very last line written; {@code false}
     *                            otherwise. In some systems (e.g., Linux), the last line is typically expected to have
     *                            a line separator.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public <T> File writeLines(
            final Iterable<T> list,
            final LineSeparator lineSeparator,
            final boolean isAppend,
            final boolean appendLineSeparator) throws InternalException {
        try (final PrintWriter writer = getPrintWriter(isAppend)) {
            boolean isFirst = true;
            for (final T t : list) {
                if (null != t) {
                    if (isFirst) {
                        isFirst = false;
                        if (isAppend && FileKit.isNotEmpty(this.file)) {
                            // In append mode and file is not empty, add a newline before the first new content.
                            printNewLine(writer, lineSeparator);
                        }
                    } else {
                        printNewLine(writer, lineSeparator);
                    }
                    writer.print(t);
                }
            }
            if (appendLineSeparator) {
                printNewLine(writer, lineSeparator);
            }
            writer.flush();
        }
        return this.file;
    }

    /**
     * Writes a map to the file, with each key-value pair on a new line, separated by {@code kvSeparator}. The file
     * content will be overwritten if {@code isAppend} is {@code false}.
     *
     * @param map         The map to write. Each entry's key and value will be converted to strings.
     * @param kvSeparator The separator string between key and value. If {@code null}, the default separator " = " is
     *                    used.
     * @param isAppend    {@code true} to append to the file; {@code false} to overwrite existing content.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public File writeMap(final Map<?, ?> map, final String kvSeparator, final boolean isAppend)
            throws InternalException {
        return writeMap(map, null, kvSeparator, isAppend);
    }

    /**
     * Writes a map to the file, with each key-value pair on a new line, separated by {@code kvSeparator} and using a
     * specified line separator. The file content will be overwritten if {@code isAppend} is {@code false}.
     *
     * @param map           The map to write. Each entry's key and value will be converted to strings.
     * @param lineSeparator The {@link LineSeparator} to use between lines. If {@code null}, the default system line
     *                      separator is used.
     * @param kvSeparator   The separator string between key and value. If {@code null}, the default separator " = " is
     *                      used.
     * @param isAppend      {@code true} to append to the file; {@code false} to overwrite existing content.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public File writeMap(
            final Map<?, ?> map,
            final LineSeparator lineSeparator,
            String kvSeparator,
            final boolean isAppend) throws InternalException {
        if (null == kvSeparator) {
            kvSeparator = " = ";
        }
        try (final PrintWriter writer = getPrintWriter(isAppend)) {
            for (final Entry<?, ?> entry : map.entrySet()) {
                if (null != entry) {
                    writer.print(StringKit.format("{}{}{}", entry.getKey(), kvSeparator, entry.getValue()));
                    printNewLine(writer, lineSeparator);
                    writer.flush();
                }
            }
        }
        return this.file;
    }

    /**
     * Writes a byte array to the file, overwriting existing content.
     *
     * @param data The byte array containing the data to write. Must not be {@code null}.
     * @param off  The start offset in the data array.
     * @param len  The number of bytes to write from the data array.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public File write(final byte[] data, final int off, final int len) throws InternalException {
        return write(data, off, len, false);
    }

    /**
     * Appends a byte array to the end of the file.
     *
     * @param data The byte array containing the data to append. Must not be {@code null}.
     * @param off  The start offset in the data array.
     * @param len  The number of bytes to append from the data array.
     * @return The target file after the append operation.
     * @throws InternalException if an I/O error occurs during appending.
     */
    public File append(final byte[] data, final int off, final int len) throws InternalException {
        return write(data, off, len, true);
    }

    /**
     * Writes a byte array to the file with an option to append or overwrite.
     *
     * @param data     The byte array containing the data to write. Must not be {@code null}.
     * @param off      The start offset in the data array.
     * @param len      The number of bytes to write from the data array.
     * @param isAppend {@code true} to append to the file; {@code false} to overwrite existing content.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public File write(final byte[] data, final int off, final int len, final boolean isAppend)
            throws InternalException {
        try (final FileOutputStream out = new FileOutputStream(FileKit.touch(file), isAppend)) {
            out.write(data, off, len);
            out.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return file;
    }

    /**
     * Writes the entire content of an input stream to the file, overwriting existing content. This method automatically
     * closes the input stream after writing.
     *
     * @param in The input stream to read data from. Must not be {@code null}.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public File writeFromStream(final InputStream in) throws InternalException {
        return writeFromStream(in, true);
    }

    /**
     * Writes the content of an input stream to the file, with options for closing the input stream and file open
     * options.
     *
     * @param in        The input stream to read data from. Must not be {@code null}.
     * @param isCloseIn {@code true} to close the input stream after writing; {@code false} otherwise.
     * @param options   Options for opening the file, such as {@link java.nio.file.StandardOpenOption#APPEND} for append
     *                  mode. If no options are provided, the file will be truncated if it exists, or created if it
     *                  doesn't.
     * @return The target file after the write operation.
     * @throws InternalException if an I/O error occurs during writing.
     */
    public File writeFromStream(final InputStream in, final boolean isCloseIn, final OpenOption... options) {
        OutputStream out = null;
        try {
            out = FileKit.getOutputStream(file, options);
            IoKit.copy(in, out);
        } finally {
            IoKit.closeQuietly(out);
            if (isCloseIn) {
                IoKit.closeQuietly(in);
            }
        }
        return file;
    }

    /**
     * Gets a {@link BufferedOutputStream} object for this file. This stream can be used for efficient byte-based
     * writing.
     *
     * @param options Options for opening the file, such as {@link java.nio.file.StandardOpenOption#APPEND} for append
     *                mode. If no options are provided, the file will be truncated if it exists, or created if it
     *                doesn't.
     * @return A {@link BufferedOutputStream} object.
     */
    public BufferedOutputStream getOutputStream(final OpenOption... options) {
        return FileKit.getOutputStream(file, options);
    }

    /**
     * Gets a buffered writer object for this file, using the configured character set. This writer can be used for
     * efficient character-based writing.
     *
     * @param isAppend {@code true} to append to the file; {@code false} to overwrite existing content.
     * @return A {@link BufferedWriter} object.
     * @throws InternalException if an I/O error occurs while creating the writer.
     */
    public BufferedWriter getWriter(final boolean isAppend) throws InternalException {
        try {
            return new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(FileKit.touch(file), isAppend), charset));
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets a print writer object for this file, which supports convenient {@code print} and {@code println} methods.
     * This writer uses the configured character set.
     *
     * @param isAppend {@code true} to append to the file; {@code false} to overwrite existing content.
     * @return A {@link PrintWriter} object.
     * @throws InternalException if an I/O error occurs while creating the writer.
     */
    public PrintWriter getPrintWriter(final boolean isAppend) throws InternalException {
        return new PrintWriter(getWriter(isAppend));
    }

    /**
     * Checks the validity of the file associated with this writer. Ensures that the file object is not null and, if the
     * file exists, that it is a regular file.
     *
     * @throws InternalException if the file is {@code null} or if it exists and is not a regular file.
     */
    private void checkFile() throws InternalException {
        Assert.notNull(file, "File to write content is null !");
        if (this.file.exists() && !file.isFile()) {
            throw new InternalException("File [{}] is not a file !", this.file.getAbsoluteFile());
        }
    }

    /**
     * Prints a new line to the provided {@link PrintWriter}. The line separator used can be customized.
     *
     * @param writer        The {@link PrintWriter} to write to. Must not be {@code null}.
     * @param lineSeparator The {@link LineSeparator} enum. If {@code null}, the default system line separator is used.
     */
    private void printNewLine(final PrintWriter writer, final LineSeparator lineSeparator) {
        if (null == lineSeparator) {
            // Default line separator
            writer.println();
        } else {
            // Custom line separator
            writer.print(lineSeparator.getValue());
        }
    }

}
