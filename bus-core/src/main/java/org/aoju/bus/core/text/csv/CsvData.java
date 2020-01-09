/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.text.csv;

import java.util.Collections;
import java.util.List;

/**
 * CSV数据,包括头部信息和行数据,参考：FastCSV
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public final class CsvData {

    private final List<String> header;
    private final List<CsvRow> rows;

    public CsvData(final List<String> header, final List<CsvRow> rows) {
        this.header = header;
        this.rows = rows;
    }

    /**
     * Returns the number of rows in this container.
     *
     * @return the number of rows in this container
     */
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Returns the header row - might be {@code null} if no header exists. The returned list is unmodifiable.
     *
     * @return the header row - might be {@code null} if no header exists
     */
    public List<String> getHeader() {
        return header;
    }

    /**
     * Returns a CsvRow by its index (starting with 0).
     *
     * @param index index of the row to return
     * @return the row by its index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public CsvRow getRow(final int index) {
        return rows.get(index);
    }

    /**
     * Returns an unmodifiable list of rows.
     *
     * @return an unmodifiable list of rows
     */
    public List<CsvRow> getRows() {
        return Collections.unmodifiableList(rows);
    }

}
