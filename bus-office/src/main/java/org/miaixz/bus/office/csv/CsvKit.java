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

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Utility class for CSV operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvKit {

    /**
     * Gets a CSV reader. The caller must specify the resource to read from.
     *
     * @param config Configuration for the CSV reader, may be {@code null} for default configuration.
     * @return A new {@link CsvReader} instance.
     */
    public static CsvReader getReader(final CsvReadConfig config) {
        return new CsvReader(config);
    }

    /**
     * Gets a CSV reader with default configuration. The caller must specify the resource to read from.
     *
     * @return A new {@link CsvReader} instance.
     */
    public static CsvReader getReader() {
        return new CsvReader();
    }

    /**
     * Gets a CSV reader for the given {@link Reader} and configuration.
     *
     * @param reader The {@link Reader} to read CSV data from.
     * @param config Configuration for the CSV reader, may be {@code null} for default configuration.
     * @return A new {@link CsvReader} instance.
     */
    public static CsvReader getReader(final Reader reader, final CsvReadConfig config) {
        return new CsvReader(reader, config);
    }

    /**
     * Gets a CSV reader for the given {@link Reader} with default configuration.
     *
     * @param reader The {@link Reader} to read CSV data from.
     * @return A new {@link CsvReader} instance.
     */
    public static CsvReader getReader(final Reader reader) {
        return getReader(reader, null);
    }

    /**
     * Gets a CSV writer with default configuration, overwriting the file if it exists.
     *
     * @param filePath The path to the CSV file.
     * @param charset  The character set to use for writing.
     * @return A new {@link CsvWriter} instance.
     */
    public static CsvWriter getWriter(final String filePath, final Charset charset) {
        return new CsvWriter(filePath, charset);
    }

    /**
     * Gets a CSV writer with default configuration, overwriting the file if it exists.
     *
     * @param file    The CSV file.
     * @param charset The character set to use for writing.
     * @return A new {@link CsvWriter} instance.
     */
    public static CsvWriter getWriter(final File file, final Charset charset) {
        return new CsvWriter(file, charset);
    }

    /**
     * Gets a CSV writer with default configuration.
     *
     * @param filePath The path to the CSV file.
     * @param charset  The character set to use for writing.
     * @param isAppend {@code true} to append to the file if it exists, {@code false} to overwrite.
     * @return A new {@link CsvWriter} instance.
     */
    public static CsvWriter getWriter(final String filePath, final Charset charset, final boolean isAppend) {
        return new CsvWriter(filePath, charset, isAppend);
    }

    /**
     * Gets a CSV writer with default configuration.
     *
     * @param file     The CSV file.
     * @param charset  The character set to use for writing.
     * @param isAppend {@code true} to append to the file if it exists, {@code false} to overwrite.
     * @return A new {@link CsvWriter} instance.
     */
    public static CsvWriter getWriter(final File file, final Charset charset, final boolean isAppend) {
        return new CsvWriter(file, charset, isAppend);
    }

    /**
     * Gets a CSV writer.
     *
     * @param file     The CSV file.
     * @param charset  The character set to use for writing.
     * @param isAppend {@code true} to append to the file if it exists, {@code false} to overwrite.
     * @param config   Write configuration, {@code null} for default configuration.
     * @return A new {@link CsvWriter} instance.
     */
    public static CsvWriter getWriter(
            final File file,
            final Charset charset,
            final boolean isAppend,
            final CsvWriteConfig config) {
        return new CsvWriter(file, charset, isAppend, config);
    }

    /**
     * Gets a CSV writer for the given {@link Writer} with default configuration.
     *
     * @param writer The {@link Writer} to write CSV data to.
     * @return A new {@link CsvWriter} instance.
     */
    public static CsvWriter getWriter(final Writer writer) {
        return new CsvWriter(writer);
    }

    /**
     * Gets a CSV writer for the given {@link Writer} and configuration.
     *
     * @param writer The {@link Writer} to write CSV data to.
     * @param config Write configuration, {@code null} for default configuration.
     * @return A new {@link CsvWriter} instance.
     */
    public static CsvWriter getWriter(final Writer writer, final CsvWriteConfig config) {
        return new CsvWriter(writer, config);
    }

}
