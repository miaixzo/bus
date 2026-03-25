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
package org.miaixz.bus.office.excel.sax.handler;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * A row handler that converts a row of data into a Map, where the key is from a specified header row and the value is
 * the corresponding cell content.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class MapRowHandler extends AbstractRowHandler<Map<String, Object>> {

    /**
     * The 0-based index of the header row.
     */
    private final int headerRowIndex;
    /**
     * The list of header names.
     */
    List<String> headerList;

    /**
     * Constructs a new {@code MapRowHandler}.
     *
     * @param headerRowIndex The 0-based index of the header row. If the header row is in the middle of the content rows
     *                       to be read, this row will be ignored as data.
     * @param startRowIndex  The starting row index (inclusive, 0-based) for reading.
     * @param endRowIndex    The ending row index (inclusive, 0-based) for reading.
     */
    public MapRowHandler(final int headerRowIndex, final int startRowIndex, final int endRowIndex) {
        super(startRowIndex, endRowIndex);
        this.headerRowIndex = headerRowIndex;
        this.convertFunc = (rowList) -> IteratorKit.toMap(headerList, rowList, true);
    }

    /**
     * Handles a row of data from the Excel sheet. If the row is the header row, stores the header values. Otherwise,
     * processes the row as data.
     *
     * @param sheetIndex The 0-based index of the current sheet.
     * @param rowIndex   The 0-based row number of the current row.
     * @param rowCells   The list of cell values in the row.
     */
    @Override
    public void handle(final int sheetIndex, final long rowIndex, final List<Object> rowCells) {
        if (rowIndex == this.headerRowIndex) {
            this.headerList = ListKit.view(Convert.toList(String.class, rowCells));
            return;
        }
        super.handle(sheetIndex, rowIndex, rowCells);
    }

}
