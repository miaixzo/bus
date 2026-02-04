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
