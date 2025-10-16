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

import org.apache.poi.ss.usermodel.CellStyle;
import org.miaixz.bus.core.lang.exception.TerminateException;

/**
 * SAX-based Excel row handler.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface RowHandler {

    /**
     * Handles a row of data. If you want to stop reading, just throw {@link TerminateException}.
     *
     * @param sheetIndex The 0-based index of the current sheet.
     * @param rowIndex   The 0-based row number of the current row.
     * @param rowCells   The row data, where each {@link Object} represents a cell value.
     */
    void handle(int sheetIndex, long rowIndex, List<Object> rowCells);

    /**
     * Handles a single cell's data. If you want to stop reading, just throw {@link TerminateException}.
     *
     * @param sheetIndex    The 0-based index of the current sheet.
     * @param rowIndex      The row number of the current cell.
     * @param cellIndex     The column number of the current cell.
     * @param value         The value of the cell.
     * @param xssfCellStyle The cell style. This parameter is specific to XSSF (Excel 2007+).
     */
    default void handleCell(
            final int sheetIndex,
            final long rowIndex,
            final int cellIndex,
            final Object value,
            final CellStyle xssfCellStyle) {
        // pass
    }

    /**
     * Performs operations after a sheet has been completely analyzed.
     */
    default void doAfterAllAnalysed() {
        // pass
    }

}
