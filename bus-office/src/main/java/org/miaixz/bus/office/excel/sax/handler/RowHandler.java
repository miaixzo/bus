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
