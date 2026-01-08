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
     * Description inherited from parent class or interface.
     *
     * @return iterator over the CSV rows
     */
    @Override
    public Iterator<CsvRow> iterator() {
        return this.rows.iterator();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return string representation of this CSV data
     */
    @Override
    public String toString() {
        return "CsvData{" + "header=" + header + ", rows=" + rows + '}';
    }

}
