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

import java.io.Serial;
import java.io.Serializable;

/**
 * CSV read configuration options.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvReadConfig extends CsvConfig<CsvReadConfig> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852283116503L;

    /**
     * Specifies the header line number. A value of -1 indicates no header line.
     */
    protected long headerLineNo = -1;
    /**
     * Whether to skip empty rows. Default is {@code true}.
     */
    protected boolean skipEmptyRows = true;
    /**
     * Whether to throw an exception if the number of fields in rows differs. Default is {@code false}.
     */
    protected boolean errorOnDifferentFieldCount;
    /**
     * The starting line number (inclusive) for reading. This refers to the original file line number.
     */
    protected long beginLineNo;
    /**
     * The ending line number (inclusive) for reading. This refers to the original file line number. Default is
     * unlimited.
     */
    protected long endLineNo = Long.MAX_VALUE - 1;
    /**
     * Whether to trim whitespace from each field.
     */
    protected boolean trimField;

    /**
     * Creates a new default {@code CsvReadConfig} instance.
     *
     * @return A new {@code CsvReadConfig} with default settings.
     */
    public static CsvReadConfig of() {
        return new CsvReadConfig();
    }

    /**
     * Sets whether the first row should be treated as a header row. Default is {@code false}. If set to {@code true},
     * the header line number defaults to {@link #beginLineNo}. If {@code false}, it is -1, indicating no header.
     *
     * @param containsHeader {@code true} if the first row is a header, {@code false} otherwise.
     * @return This configuration object, for chaining.
     * @see #setHeaderLineNo(long)
     */
    public CsvReadConfig setContainsHeader(final boolean containsHeader) {
        return setHeaderLineNo(containsHeader ? beginLineNo : -1);
    }

    /**
     * Sets the header line number. A value of -1 indicates no header line.
     *
     * @param headerLineNo The header line number.
     * @return This configuration object, for chaining.
     */
    public CsvReadConfig setHeaderLineNo(final long headerLineNo) {
        this.headerLineNo = headerLineNo;
        return this;
    }

    /**
     * Sets whether to skip empty rows. Default is {@code true}.
     *
     * @param skipEmptyRows {@code true} to skip empty rows, {@code false} otherwise.
     * @return This configuration object, for chaining.
     */
    public CsvReadConfig setSkipEmptyRows(final boolean skipEmptyRows) {
        this.skipEmptyRows = skipEmptyRows;
        return this;
    }

    /**
     * Sets whether an exception should be thrown if the number of fields in rows differs. Default is {@code false}.
     *
     * @param errorOnDifferentFieldCount {@code true} to throw an exception on inconsistent field counts, {@code false}
     *                                   otherwise.
     * @return This configuration object, for chaining.
     */
    public CsvReadConfig setErrorOnDifferentFieldCount(final boolean errorOnDifferentFieldCount) {
        this.errorOnDifferentFieldCount = errorOnDifferentFieldCount;
        return this;
    }

    /**
     * Sets the starting line number (inclusive) for reading. Default is 0. This refers to the original file line
     * number.
     *
     * @param beginLineNo The starting line number (inclusive).
     * @return This configuration object, for chaining.
     */
    public CsvReadConfig setBeginLineNo(final long beginLineNo) {
        this.beginLineNo = beginLineNo;
        return this;
    }

    /**
     * Sets the ending line number (inclusive) for reading. Default is unlimited. This refers to the original file line
     * number.
     *
     * @param endLineNo The ending line number (inclusive).
     * @return This configuration object, for chaining.
     */
    public CsvReadConfig setEndLineNo(final long endLineNo) {
        this.endLineNo = endLineNo;
        return this;
    }

    /**
     * Sets whether to trim whitespace from each field. If a field is enclosed by {@link #textDelimiter}, leading and
     * trailing spaces within the delimiters are preserved.
     *
     * @param trimField {@code true} to trim whitespace from fields, {@code false} otherwise.
     * @return This configuration object, for chaining.
     */
    public CsvReadConfig setTrimField(final boolean trimField) {
        this.trimField = trimField;
        return this;
    }

}
