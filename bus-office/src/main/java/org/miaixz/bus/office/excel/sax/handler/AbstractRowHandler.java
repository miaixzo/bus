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
package org.miaixz.bus.office.excel.sax.handler;

import java.util.List;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;

/**
 * Abstract row data handler. It processes raw data by implementing {@link #handle(int, long, List)} and processes
 * converted data by calling {@link #handleData(int, long, Object)}.
 *
 * @param <T> The type of the converted data.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractRowHandler<T> implements RowHandler {

    /**
     * The starting row index (inclusive, 0-based) for reading.
     */
    protected final int startRowIndex;
    /**
     * The ending row index (inclusive, 0-based) for reading.
     */
    protected final int endRowIndex;
    /**
     * Function to convert raw row data to type T.
     */
    protected Function<List<Object>, T> convertFunc;

    /**
     * Constructs a new {@code AbstractRowHandler}.
     *
     * @param startRowIndex The starting row index (inclusive, 0-based) for reading.
     * @param endRowIndex   The ending row index (inclusive, 0-based) for reading.
     */
    public AbstractRowHandler(final int startRowIndex, final int endRowIndex) {
        this.startRowIndex = startRowIndex;
        this.endRowIndex = endRowIndex;
    }

    /**
     * Handles a row of data by checking row bounds and converting raw data.
     *
     * @param sheetIndex The 0-based index of the current sheet.
     * @param rowIndex   The 0-based row number of the current row.
     * @param rowCells   The list of cell values in the row.
     */
    @Override
    public void handle(final int sheetIndex, final long rowIndex, final List<Object> rowCells) {
        Assert.notNull(convertFunc);
        if (rowIndex < this.startRowIndex || rowIndex > this.endRowIndex) {
            return;
        }
        handleData(sheetIndex, rowIndex, convertFunc.apply(rowCells));
    }

    /**
     * Processes the converted data.
     *
     * @param sheetIndex The 0-based index of the current sheet.
     * @param rowIndex   The 0-based row number of the current row.
     * @param data       The converted row data.
     */
    public abstract void handleData(int sheetIndex, long rowIndex, T data);

}
