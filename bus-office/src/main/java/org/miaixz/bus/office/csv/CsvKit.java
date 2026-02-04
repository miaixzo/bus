/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
