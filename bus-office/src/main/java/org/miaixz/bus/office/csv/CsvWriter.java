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
package org.miaixz.bus.office.csv;

import java.io.*;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

/**
 * CSV data writer.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class CsvWriter implements Closeable, Flushable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852283622258L;

    /**
     * The underlying writer for CSV data.
     */
    private final Writer writer;
    /**
     * The configuration for CSV writing.
     */
    private final CsvWriteConfig config;
    /**
     * Flag indicating whether the writer is at the beginning of a new line. This is used to determine whether to write
     * a field separator before writing a field.
     */
    private boolean newline = true;
    /**
     * Flag indicating whether this is the first line being written. Initially {@code true}, becomes {@code false} after
     * content is written. Used to determine whether to add a newline character.
     */
    private boolean isFirstLine = true;

    /**
     * Constructs a new {@code CsvWriter} that overwrites an existing file (if any), using UTF-8 encoding.
     *
     * @param filePath The path to the CSV file.
     */
    public CsvWriter(final String filePath) {
        this(FileKit.file(filePath));
    }

    /**
     * Constructs a new {@code CsvWriter} that overwrites an existing file (if any), using UTF-8 encoding.
     *
     * @param file The CSV file.
     */
    public CsvWriter(final File file) {
        this(file, Charset.UTF_8);
    }

    /**
     * Constructs a new {@code CsvWriter} that overwrites an existing file (if any).
     *
     * @param filePath The path to the CSV file.
     * @param charset  The character set to use for writing.
     */
    public CsvWriter(final String filePath, final java.nio.charset.Charset charset) {
        this(FileKit.file(filePath), charset);
    }

    /**
     * Constructs a new {@code CsvWriter} that overwrites an existing file (if any).
     *
     * @param file    The CSV file.
     * @param charset The character set to use for writing.
     */
    public CsvWriter(final File file, final java.nio.charset.Charset charset) {
        this(file, charset, false);
    }

    /**
     * Constructs a new {@code CsvWriter}.
     *
     * @param filePath The path to the CSV file.
     * @param charset  The character set to use for writing.
     * @param isAppend {@code true} to append to the file if it exists, {@code false} to overwrite.
     */
    public CsvWriter(final String filePath, final java.nio.charset.Charset charset, final boolean isAppend) {
        this(FileKit.file(filePath), charset, isAppend);
    }

    /**
     * Constructs a new {@code CsvWriter}.
     *
     * @param file     The CSV file.
     * @param charset  The character set to use for writing.
     * @param isAppend {@code true} to append to the file if it exists, {@code false} to overwrite.
     */
    public CsvWriter(final File file, final java.nio.charset.Charset charset, final boolean isAppend) {
        this(file, charset, isAppend, null);
    }

    /**
     * Constructs a new {@code CsvWriter}.
     *
     * @param filePath The path to the CSV file.
     * @param charset  The character set to use for writing.
     * @param isAppend {@code true} to append to the file if it exists, {@code false} to overwrite.
     * @param config   The write configuration. If {@code null}, default configuration will be used.
     */
    public CsvWriter(final String filePath, final java.nio.charset.Charset charset, final boolean isAppend,
            final CsvWriteConfig config) {
        this(FileKit.file(filePath), charset, isAppend, config);
    }

    /**
     * Constructs a new {@code CsvWriter}.
     *
     * @param file     The CSV file.
     * @param charset  The character set to use for writing.
     * @param isAppend {@code true} to append to the file if it exists, {@code false} to overwrite. If {@code isAppend}
     *                 is {@code true}, {@code endingLineBreak} in config is automatically set to {@code true}.
     * @param config   The write configuration. If {@code null}, default configuration will be used.
     */
    public CsvWriter(final File file, final java.nio.charset.Charset charset, final boolean isAppend,
            final CsvWriteConfig config) {
        this(FileKit.getWriter(file, charset, isAppend),
                isAppend ? (config == null ? CsvWriteConfig.defaultConfig().setEndingLineBreak(true)
                        : config.setEndingLineBreak(true)) : config);
    }

    /**
     * Constructs a new {@code CsvWriter} with the given {@link Writer} and default configuration.
     *
     * @param writer The {@link Writer} to write CSV data to.
     */
    public CsvWriter(final Writer writer) {
        this(writer, null);
    }

    /**
     * Constructs a new {@code CsvWriter} with the given {@link Writer} and configuration.
     *
     * @param writer The {@link Writer} to write CSV data to.
     * @param config The write configuration. If {@code null}, default configuration will be used.
     */
    public CsvWriter(final Writer writer, final CsvWriteConfig config) {
        this.writer = (writer instanceof BufferedWriter) ? writer : new BufferedWriter(writer);
        this.config = ObjectKit.defaultIfNull(config, CsvWriteConfig::defaultConfig);
    }

    /**
     * Checks if the given character is unsafe for DDE attacks.
     * <p>
     * Unsafe characters include:
     * <ul>
     * <li>{@code @ }</li>
     * <li>{@code + }</li>
     * <li>{@code - }</li>
     * <li>{@code = }</li>
     * </ul>
     *
     * @param c The character to check.
     * @return {@code true} if the character is unsafe, {@code false} otherwise.
     */
    private static boolean isDDEUnsafeChar(final char c) {
        return c == Symbol.C_AT || c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_EQUAL;
    }

    /**
     * Sets whether to always use text delimiters (quotes). Default is {@code false}, delimiters are added as needed.
     *
     * @param alwaysDelimitText {@code true} to always use text delimiters, {@code false} to use them only when
     *                          necessary.
     * @return This {@code CsvWriter} instance, for chaining.
     */
    public CsvWriter setAlwaysDelimitText(final boolean alwaysDelimitText) {
        this.config.setAlwaysDelimitText(alwaysDelimitText);
        return this;
    }

    /**
     * Sets the newline character(s).
     *
     * @param lineDelimiter The character array representing the newline sequence.
     * @return This {@code CsvWriter} instance, for chaining.
     */
    public CsvWriter setLineDelimiter(final char[] lineDelimiter) {
        this.config.setLineDelimiter(lineDelimiter);
        return this;
    }

    /**
     * Sets whether to enable DDE safe mode. Default is {@code false}. This prevents DDE attack risks when opening CSV
     * files with Excel. Note that this method adds a {@code '} prefix if the first character of a field is
     * {@code = + - @} to prevent formula execution.
     *
     * @param ddeSafe {@code true} to enable DDE safe mode, {@code false} otherwise.
     * @return This {@code CsvWriter} instance, for chaining.
     */
    public CsvWriter setDdeSafe(final boolean ddeSafe) {
        this.config.setDdeSafe(ddeSafe);
        return this;
    }

    /**
     * Writes multiple lines of data to the underlying {@link Writer}.
     *
     * @param lines An array of string arrays, where each inner array represents a line of fields.
     * @return This {@code CsvWriter} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public CsvWriter write(final String[]... lines) throws InternalException {
        return write(new ArrayIterator<>(lines));
    }

    /**
     * Writes multiple lines of data to the underlying {@link Writer}.
     *
     * @param lines An iterable collection of lines, where each line can be a collection or array of objects.
     * @return This {@code CsvWriter} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public CsvWriter write(final Iterable<?> lines) throws InternalException {
        if (CollKit.isNotEmpty(lines)) {
            for (final Object values : lines) {
                appendLine(Convert.toStringArray(values));
            }
            flush();
        }
        return this;
    }

    /**
     * Writes a {@link CsvData} object to the underlying {@link Writer}. This includes writing the header (if present)
     * and all data rows.
     *
     * @param csvData The {@link CsvData} object to write.
     * @return This {@code CsvWriter} instance, for chaining.
     */
    public CsvWriter write(final CsvData csvData) {
        if (csvData != null) {
            // 1. Write header
            final List<String> header = csvData.getHeader();
            if (CollKit.isNotEmpty(header)) {
                this.writeHeaderLine(header.toArray(new String[0]));
            }
            // 2. Write content
            this.write(csvData.getRows());
            flush();
        }
        return this;
    }

    /**
     * Writes a collection of Bean objects to the underlying {@link Writer}, automatically generating a header.
     *
     * @param beans      The collection of Bean objects.
     * @param properties Optional list of property names to include. If empty, all properties are included.
     * @param <T>        The type of the Bean objects.
     * @return This {@code CsvWriter} instance, for chaining.
     */
    public <T> CsvWriter writeBeans(final Iterable<T> beans, final String... properties) {
        return writeBeans(beans, true, properties);
    }

    /**
     * Writes a collection of Bean objects to the underlying {@link Writer}.
     *
     * @param beans           The collection of Bean objects.
     * @param writeHeaderLine {@code true} to write the header line (Bean property names) as the first row,
     *                        {@code false} otherwise.
     * @param properties      Optional list of property names to include. If empty, all properties are included.
     * @param <T>             The type of the Bean objects.
     * @return This {@code CsvWriter} instance, for chaining.
     */
    public <T> CsvWriter writeBeans(
            final Iterable<T> beans,
            final boolean writeHeaderLine,
            final String... properties) {
        if (CollKit.isNotEmpty(beans)) {
            boolean isFirst = writeHeaderLine;
            Map<String, Object> map;
            for (final Object bean : beans) {
                map = BeanKit.beanToMap(bean, properties);
                if (isFirst) {
                    writeHeaderLine(map.keySet().toArray(new String[0]));
                    isFirst = false;
                }
                writeLine(Convert.toStringArray(map.values()));
            }
            flush();
        }
        return this;
    }

    /**
     * Writes a header line, supporting header aliases.
     *
     * @param fields The list of fields for the header line. {@code null} values will be written as empty strings.
     * @return This {@code CsvWriter} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public CsvWriter writeHeaderLine(final String... fields) throws InternalException {
        final Map<String, String> headerAlias = this.config.headerAlias;
        if (MapKit.isNotEmpty(headerAlias)) {
            // Header alias replacement
            String alias;
            for (int i = 0; i < fields.length; i++) {
                alias = headerAlias.get(fields[i]);
                if (null != alias) {
                    fields[i] = alias;
                }
            }
        }
        return writeLine(fields);
    }

    /**
     * Writes a single line of fields.
     *
     * @param fields The list of fields for the line. {@code null} values will be written as empty strings.
     * @return This {@code CsvWriter} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public CsvWriter writeLine(final String... fields) throws InternalException {
        if (ArrayKit.isEmpty(fields)) {
            return writeLine();
        }
        appendLine(fields);
        return this;
    }

    /**
     * Appends a new line (newline character(s)).
     *
     * @return This {@code CsvWriter} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     */
    public CsvWriter writeLine() throws InternalException {
        try {
            writer.write(config.lineDelimiter);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        newline = true;
        return this;
    }

    /**
     * Writes a comment line. The comment character can be customized. If the comment character is not defined, an
     * {@link Assert} exception is thrown.
     *
     * @param comment The content of the comment.
     * @return This {@code CsvWriter} instance, for chaining.
     * @throws InternalException If an I/O error occurs.
     * @see CsvConfig#commentCharacter
     */
    public CsvWriter writeComment(final String comment) {
        Assert.notNull(this.config.commentCharacter, "Comment is disable!");
        try {
            if (isFirstLine) {
                // No newline for the first line.
                isFirstLine = false;
            } else {
                writer.write(config.lineDelimiter);
            }
            writer.write(this.config.commentCharacter);
            writer.write(comment);
            newline = true;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void close() {
        if (this.config.endingLineBreak) {
            writeLine();
        }
        IoKit.closeQuietly(this.writer);
    }

    @Override
    public void flush() throws InternalException {
        try {
            writer.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Appends a line of fields. A newline character will be automatically added at the end, but not before appending.
     *
     * @param fields The list of fields. {@code null} values will be treated as empty strings.
     * @throws InternalException If an I/O error occurs.
     */
    private void appendLine(final String... fields) throws InternalException {
        try {
            doAppendLine(fields);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Appends a line of fields. A newline character will be automatically added at the end, but not before appending.
     *
     * @param fields The list of fields. {@code null} values will be treated as empty strings.
     * @throws IOException If an I/O error occurs.
     */
    private void doAppendLine(final String... fields) throws IOException {
        if (null != fields) {
            if (isFirstLine) {
                // No newline for the first line.
                isFirstLine = false;
            } else {
                writer.write(config.lineDelimiter);
            }
            for (final String field : fields) {
                appendField(field);
            }
            newline = true;
        }
    }

    /**
     * Appends a field value to the current line, automatically adding a field separator and quoting the field if
     * necessary.
     *
     * @param value The field value. {@code null} will be written as an empty string.
     * @throws IOException If an I/O error occurs.
     */
    private void appendField(final String value) throws IOException {
        final boolean alwaysDelimitText = config.alwaysDelimitText;
        final char textDelimiter = config.textDelimiter;
        final char fieldSeparator = config.fieldSeparator;

        if (!newline) {
            writer.write(fieldSeparator);
        } else {
            newline = false;
        }

        if (null == value) {
            if (alwaysDelimitText) {
                writer.write(new char[] { textDelimiter, textDelimiter });
            }
            return;
        }

        final char[] valueChars = value.toCharArray();
        boolean needsTextDelimiter = alwaysDelimitText;
        boolean containsTextDelimiter = false;

        for (final char c : valueChars) {
            if (c == textDelimiter) {
                // Field value contains text delimiter.
                containsTextDelimiter = needsTextDelimiter = true;
                break;
            } else if (c == fieldSeparator || c == Symbol.C_LF || c == Symbol.C_CR) {
                // Contains separator or newline, requires quoting.
                needsTextDelimiter = true;
            }
        }

        // Start quoting
        if (needsTextDelimiter) {
            writer.write(textDelimiter);
        }

        // DDE protection: if enabled, do not execute formulas.
        if (config.ddeSafe && isDDEUnsafeChar(valueChars[0])) {
            writer.write('\'');
        }

        // Content
        if (containsTextDelimiter) {
            for (final char c : valueChars) {
                // Escape text delimiter, e.g., " becomes ""
                if (c == textDelimiter) {
                    writer.write(textDelimiter);
                }
                writer.write(c);
            }
        } else {
            writer.write(valueChars);
        }

        // End quoting
        if (needsTextDelimiter) {
            writer.write(textDelimiter);
        }
    }

}
