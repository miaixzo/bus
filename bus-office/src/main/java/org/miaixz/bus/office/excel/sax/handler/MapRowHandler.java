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
import java.util.Map;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * A row handler that converts a row of data into a Map, where the key is from a specified header row and the value is
 * the corresponding cell content.
 *
 * @author Kimi Liu
 * @since Java 17+
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
