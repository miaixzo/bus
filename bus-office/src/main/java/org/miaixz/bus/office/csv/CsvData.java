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

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.xyz.ListKit;

/**
 * Represents CSV data, including header information and row data. Inspired by FastCSV.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvData implements Iterable<CsvRow>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852282752523L;

    /**
     * The header information of the CSV data.
     */
    private final List<String> header;
    /**
     * The list of rows in the CSV data.
     */
    private final List<CsvRow> rows;

    /**
     * Constructs a new {@code CsvData} instance.
     *
     * @param header The header information, may be {@code null}.
     * @param rows   The list of rows.
     */
    public CsvData(final List<String> header, final List<CsvRow> rows) {
        this.header = header;
        this.rows = rows;
    }

    /**
     * Gets the total number of rows in the CSV data.
     *
     * @return The total number of rows.
     */
    public int getRowCount() {
        return this.rows.size();
    }

    /**
     * Gets the header row list. If no header exists, {@code null} is returned. The returned list is unmodifiable.
     *
     * @return The header row, or {@code null} if no header exists.
     */
    public List<String> getHeader() {
        return ListKit.unmodifiable(this.header);
    }

    /**
     * Gets the row at the specified index.
     *
     * @param index The index of the row, starting from 0.
     * @return The {@link CsvRow} at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= getRowCount()}).
     */
    public CsvRow getRow(final int index) {
        return this.rows.get(index);
    }

    /**
     * Gets all rows in the CSV data.
     *
     * @return A list of all {@link CsvRow} objects.
     */
    public List<CsvRow> getRows() {
        return this.rows;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return iterator over the CSV rows
     */
    @Override
    public Iterator<CsvRow> iterator() {
        return this.rows.iterator();
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return string representation of this CSV data
     */
    @Override
    public String toString() {
        return "CsvData{" + "header=" + header + ", rows=" + rows + '}';
    }

}
