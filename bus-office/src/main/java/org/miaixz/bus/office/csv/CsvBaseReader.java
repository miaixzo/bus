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
package org.miaixz.bus.office.csv;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Base class for CSV file readers, providing flexible CSV reading from files and paths. This class allows for multiple
 * reads of different data after a single construction. Inspired by FastCSV.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvBaseReader implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852282185110L;

    /**
     * The configuration for reading CSV.
     */
    private final CsvReadConfig config;

    /**
     * Constructs a new {@code CsvBaseReader} with default configuration.
     */
    public CsvBaseReader() {
        this(null);
    }

    /**
     * Constructs a new {@code CsvBaseReader} with the given configuration.
     *
     * @param config The CSV read configuration. May be {@code null} for default configuration.
     */
    public CsvBaseReader(final CsvReadConfig config) {
        this.config = ObjectKit.defaultIfNull(config, CsvReadConfig::of);
    }

    /**
     * Sets the field separator character. Default is comma ','.
     *
     * @param fieldSeparator The field separator character.
     */
    public void setFieldSeparator(final char fieldSeparator) {
        this.config.setFieldSeparator(fieldSeparator);
    }

    /**
     * Sets the text delimiter character (text wrapper). Default is double quotes '"'
     *
     * @param textDelimiter The text delimiter character.
     */
    public void setTextDelimiter(final char textDelimiter) {
        this.config.setTextDelimiter(textDelimiter);
    }

    /**
     * Sets whether the first row should be treated as a header row. Default is {@code false}.
     *
     * @param containsHeader {@code true} if the first row is a header, {@code false} otherwise.
     */
    public void setContainsHeader(final boolean containsHeader) {
        this.config.setContainsHeader(containsHeader);
    }

    /**
     * Sets whether empty rows should be skipped. Default is {@code true}.
     *
     * @param skipEmptyRows {@code true} to skip empty rows, {@code false} otherwise.
     */
    public void setSkipEmptyRows(final boolean skipEmptyRows) {
        this.config.setSkipEmptyRows(skipEmptyRows);
    }

    /**
     * Sets whether an {@link InternalException} should be thrown if the number of fields in rows differs. Default is
     * {@code false}.
     *
     * @param errorOnDifferentFieldCount {@code true} to throw an exception on inconsistent field counts, {@code false}
     *                                   otherwise.
     */
    public void setErrorOnDifferentFieldCount(final boolean errorOnDifferentFieldCount) {
        this.config.setErrorOnDifferentFieldCount(errorOnDifferentFieldCount);
    }

    /**
     * Reads CSV data from the specified file using UTF-8 encoding.
     *
     * @param file The CSV file to read.
     * @return A {@link CsvData} object containing the data list and row information.
     * @throws InternalException If an I/O error occurs.
     */
    public CsvData read(final File file) throws InternalException {
        return read(file, Charset.UTF_8);
    }

    /**
     * Reads CSV data from the given string.
     *
     * @param csvStr The CSV string to read.
     * @return A {@link CsvData} object containing the data list and row information.
     */
    public CsvData readFromString(final String csvStr) {
        return read(new StringReader(csvStr), true);
    }

    /**
     * Reads CSV data from the given string and processes each row with a handler.
     *
     * @param csvStr     The CSV string to read.
     * @param rowHandler The row handler to process each {@link CsvRow}.
     */
    public void readFromString(final String csvStr, final ConsumerX<CsvRow> rowHandler) {
        read(parse(new StringReader(csvStr)), true, rowHandler);
    }

    /**
     * Reads CSV data from the specified file.
     *
     * @param file    The CSV file to read.
     * @param charset The character set of the file.
     * @return A {@link CsvData} object containing the data list and row information.
     * @throws InternalException    If an I/O error occurs.
     * @throws NullPointerException if {@code file} is {@code null}.
     */
    public CsvData read(final File file, final java.nio.charset.Charset charset) throws InternalException {
        return read(Objects.requireNonNull(file.toPath(), "file must not be null"), charset);
    }

    /**
     * Reads CSV data from the specified path using UTF-8 encoding.
     *
     * @param path The path to the CSV file.
     * @return A {@link CsvData} object containing the data list and row information.
     * @throws InternalException If an I/O error occurs.
     */
    public CsvData read(final Path path) throws InternalException {
        return read(path, Charset.UTF_8);
    }

    /**
     * Reads CSV data from the specified path.
     *
     * @param path    The path to the CSV file.
     * @param charset The character set of the file.
     * @return A {@link CsvData} object containing the data list and row information.
     * @throws InternalException    If an I/O error occurs.
     * @throws NullPointerException if {@code path} is {@code null}.
     */
    public CsvData read(final Path path, final java.nio.charset.Charset charset) throws InternalException {
        Assert.notNull(path, "path must not be null");
        return read(PathResolve.getReader(path, charset), true);
    }

    /**
     * Reads CSV data from the given {@link Reader}. The reader will be closed after reading if {@code closeReader} is
     * {@code true}.
     *
     * @param reader      The {@link Reader} to read CSV data from.
     * @param closeReader {@code true} to close the reader after reading, {@code false} otherwise.
     * @return A {@link CsvData} object containing the data list and row information.
     * @throws InternalException If an I/O error occurs.
     */
    public CsvData read(final Reader reader, final boolean closeReader) throws InternalException {
        final CsvParser csvParser = parse(reader);
        final List<CsvRow> rows = new ArrayList<>();
        read(csvParser, closeReader, rows::add);
        final List<String> header = config.headerLineNo > -1 ? csvParser.getHeader() : null;

        return new CsvData(header, rows);
    }

    /**
     * Reads CSV data from the given {@link Reader} and returns it as a list of maps. The reader will be closed after
     * reading if {@code closeReader} is {@code true}. This method assumes the first row is a header row.
     *
     * @param reader      The {@link Reader} to read CSV data from.
     * @param closeReader {@code true} to close the reader after reading, {@code false} otherwise.
     * @return A {@link List} of {@link Map}s, where each map represents a row with header names as keys and field
     *         values as values.
     * @throws InternalException If an I/O error occurs.
     */
    public List<Map<String, String>> readMapList(final Reader reader, final boolean closeReader)
            throws InternalException {
        // This method requires a header.
        this.config.setContainsHeader(true);

        final List<Map<String, String>> result = new ArrayList<>();
        read(reader, closeReader, (row) -> result.add(row.getFieldMap()));
        return result;
    }

    /**
     * Reads CSV data from the given {@link Reader} and converts it into a list of Bean objects. The reader will be
     * closed after reading if {@code closeReader} is {@code true}. This method assumes the first row is a header row.
     *
     * @param <T>         The type of the Bean.
     * @param reader      The {@link Reader} to read CSV data from.
     * @param closeReader {@code true} to close the reader after reading, {@code false} otherwise.
     * @param clazz       The class of the Bean to convert to.
     * @return A {@link List} of Bean objects.
     */
    public <T> List<T> read(final Reader reader, final boolean closeReader, final Class<T> clazz) {
        // This method requires a header.
        this.config.setContainsHeader(true);

        final List<T> result = new ArrayList<>();
        read(reader, closeReader, (row) -> result.add(row.toBean(clazz)));
        return result;
    }

    /**
     * Reads CSV data from a string and converts it into a list of Bean objects. The reader will be closed after
     * reading. This method assumes the first row is a header row.
     *
     * @param <T>    The type of the Bean.
     * @param csvStr The CSV string to read.
     * @param clazz  The class of the Bean to convert to.
     * @return A {@link List} of Bean objects.
     */
    public <T> List<T> read(final String csvStr, final Class<T> clazz) {
        // This method requires a header.
        this.config.setContainsHeader(true);

        final List<T> result = new ArrayList<>();
        read(new StringReader(csvStr), true, (row) -> result.add(row.toBean(clazz)));
        return result;
    }

    /**
     * Reads CSV data from the given {@link Reader} and processes each row with a handler. The reader will be closed
     * after reading if {@code closeReader} is {@code true}.
     *
     * @param reader      The {@link Reader} to read CSV data from.
     * @param closeReader {@code true} to close the reader after reading, {@code false} otherwise.
     * @param rowHandler  The row handler to process each {@link CsvRow}.
     * @throws InternalException If an I/O error occurs.
     */
    public void read(final Reader reader, final boolean closeReader, final ConsumerX<CsvRow> rowHandler)
            throws InternalException {
        read(parse(reader), closeReader, rowHandler);
    }

    /**
     * Reads CSV data from the given {@link CsvParser} and processes each row with a handler. The parser will be closed
     * after reading if {@code closeParser} is {@code true}.
     *
     * @param csvParser   The {@link CsvParser} to read CSV data from.
     * @param closeParser {@code true} to close the parser after reading, {@code false} otherwise.
     * @param rowHandler  The row handler to process each {@link CsvRow}.
     * @throws InternalException If an I/O error occurs.
     */
    private void read(final CsvParser csvParser, final boolean closeParser, final ConsumerX<CsvRow> rowHandler)
            throws InternalException {
        try {
            while (csvParser.hasNext()) {
                rowHandler.accept(csvParser.next());
            }
        } finally {
            if (closeParser) {
                IoKit.closeQuietly(csvParser);
            }
        }
    }

    /**
     * Builds a {@link CsvParser} from the given {@link Reader}.
     *
     * @param reader The {@link Reader} to build the parser from.
     * @return A new {@link CsvParser} instance.
     * @throws InternalException If an I/O error occurs during parser creation.
     */
    protected CsvParser parse(final Reader reader) throws InternalException {
        return new CsvParser(reader, this.config);
    }

}
