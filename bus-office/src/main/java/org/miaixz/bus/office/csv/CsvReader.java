/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.csv;

import java.io.Closeable;
import java.io.File;
import java.io.Reader;
import java.io.Serial;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * CSV file reader, inspired by FastCSV.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvReader extends CsvBaseReader implements Iterable<CsvRow>, Closeable {

    @Serial
    private static final long serialVersionUID = 2852283180950L;

    /**
     * The reader for the CSV data.
     */
    private final Reader reader;

    /**
     * Constructs a new {@code CsvReader} with default configuration.
     */
    public CsvReader() {
        this(null);
    }

    /**
     * Constructs a new {@code CsvReader} with the given configuration.
     *
     * @param config The CSV read configuration. May be {@code null} for default configuration.
     */
    public CsvReader(final CsvReadConfig config) {
        this((Reader) null, config);
    }

    /**
     * Constructs a new {@code CsvReader} for the specified CSV file path with default character set.
     *
     * @param file   The CSV file. May be {@code null} if the reader is to be set later.
     * @param config The CSV read configuration. May be {@code null} for default configuration.
     */
    public CsvReader(final File file, final CsvReadConfig config) {
        this(file, Charset.UTF_8, config);
    }

    /**
     * Constructs a new {@code CsvReader} for the specified CSV file path with default character set.
     *
     * @param path   The CSV file path. May be {@code null} if the reader is to be set later.
     * @param config The CSV read configuration. May be {@code null} for default configuration.
     */
    public CsvReader(final Path path, final CsvReadConfig config) {
        this(path, Charset.UTF_8, config);
    }

    /**
     * Constructs a new {@code CsvReader} for the specified CSV file with the given character set and configuration.
     *
     * @param file    The CSV file. May be {@code null} if the reader is to be set later.
     * @param charset The character set to use for reading the file.
     * @param config  The CSV read configuration. May be {@code null} for default configuration.
     */
    public CsvReader(final File file, final java.nio.charset.Charset charset, final CsvReadConfig config) {
        this(FileKit.getReader(file, charset), config);
    }

    /**
     * Constructs a new {@code CsvReader} for the specified CSV file path with the given character set and
     * configuration.
     *
     * @param path    The CSV file path. May be {@code null} if the reader is to be set later.
     * @param charset The character set to use for reading the file.
     * @param config  The CSV read configuration. May be {@code null} for default configuration.
     */
    public CsvReader(final Path path, final java.nio.charset.Charset charset, final CsvReadConfig config) {
        this(PathResolve.getReader(path, charset), config);
    }

    /**
     * Constructs a new {@code CsvReader} with the given {@link Reader} and configuration.
     *
     * @param reader The {@link Reader} to read CSV data from. May be {@code null} if not set initially.
     * @param config The CSV read configuration. May be {@code null} for default configuration.
     */
    public CsvReader(final Reader reader, final CsvReadConfig config) {
        super(config);
        this.reader = reader;
    }

    /**
     * Reads the entire CSV file into a {@link CsvData} object. This method can only be called once. This method
     * requires that a file path or {@link Reader} has been provided during construction.
     *
     * @return A {@link CsvData} object containing the data list and row information.
     * @throws InternalException If an I/O error occurs during reading.
     */
    public CsvData read() throws InternalException {
        return read(this.reader, false);
    }

    /**
     * Reads CSV data and processes each row using the provided {@link ConsumerX}. This method can only be called once.
     * This method requires that a file path or {@link Reader} has been provided during construction.
     *
     * @param rowHandler The row handler to process each {@link CsvRow}.
     * @throws InternalException If an I/O error occurs during reading.
     */
    public void read(final ConsumerX<CsvRow> rowHandler) throws InternalException {
        read(this.reader, false, rowHandler);
    }

    /**
     * Creates a {@link Stream} from the underlying {@link Reader} to allow for stream-based CSV row processing.
     *
     * @return A {@link Stream} of {@link CsvRow} objects.
     */
    public Stream<CsvRow> stream() {
        return StreamSupport.stream(spliterator(), false).onClose(this::close);
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return iterator over CSV rows
     */
    @Override
    public Iterator<CsvRow> iterator() {
        return parse(this.reader);
    }

    /**
     * Implements the behavior defined by the supertype.
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.reader);
    }

}
