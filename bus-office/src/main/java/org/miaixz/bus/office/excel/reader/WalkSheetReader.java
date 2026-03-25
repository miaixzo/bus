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
package org.miaixz.bus.office.excel.reader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.CellKit;

/**
 * Reads an Excel {@link Sheet} and processes cells using a {@link BiConsumerX}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WalkSheetReader extends AbstractSheetReader<Void> {

    /**
     * The cell handler for processing each cell.
     */
    private final BiConsumerX<Cell, Object> cellHandler;

    /**
     * Constructs a new {@code WalkSheetReader}.
     *
     * @param startRowIndex The starting row index (inclusive, 0-based).
     * @param endRowIndex   The ending row index (inclusive, 0-based).
     * @param cellHandler   The cell handler, used to process the read cell and its data.
     */
    public WalkSheetReader(final int startRowIndex, final int endRowIndex,
            final BiConsumerX<Cell, Object> cellHandler) {
        super(startRowIndex, endRowIndex);
        this.cellHandler = cellHandler;
    }

    /**
     * Reads the sheet and processes each cell using the configured cell handler.
     *
     * @param sheet The {@link Sheet} to read.
     * @return {@code null} as this reader processes cells through a handler.
     */
    @Override
    public Void read(final Sheet sheet) {
        final int startRowIndex = Math.max(this.cellRangeAddress.getFirstRow(), sheet.getFirstRowNum());// Read starting
                                                                                                        // row
                                                                                                        // (inclusive).
        final int endRowIndex = Math.min(this.cellRangeAddress.getLastRow(), sheet.getLastRowNum());// Read ending row
                                                                                                    // (inclusive).
        final CellEditor cellEditor = this.config.getCellEditor();

        Row row;
        for (int y = startRowIndex; y <= endRowIndex; y++) {
            row = sheet.getRow(y);
            if (null != row) {
                final short startColumnIndex = (short) Math
                        .max(this.cellRangeAddress.getFirstColumn(), row.getFirstCellNum());
                final short endColumnIndex = (short) Math
                        .min(this.cellRangeAddress.getLastColumn(), row.getLastCellNum());
                Cell cell;
                for (short x = startColumnIndex; x < endColumnIndex; x++) {
                    cell = CellKit.getCell(row, x);
                    cellHandler.accept(cell, CellKit.getCellValue(cell, cellEditor));
                }
            }
        }

        return null;
    }

}
