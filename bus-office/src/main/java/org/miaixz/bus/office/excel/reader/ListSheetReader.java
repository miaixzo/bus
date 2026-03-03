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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.xyz.RowKit;

/**
 * Reads an {@link Sheet} into a list of lists.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListSheetReader extends AbstractSheetReader<List<List<Object>>> {

    /**
     * Whether the first row should be treated as a header row and aliases applied.
     */
    private final boolean aliasFirstLine;

    /**
     * Constructs a new {@code ListSheetReader}.
     *
     * @param startRowIndex  The starting row index (inclusive, 0-based).
     * @param endRowIndex    The ending row index (inclusive, 0-based).
     * @param aliasFirstLine {@code true} if the first row should be treated as a header and aliases applied,
     *                       {@code false} otherwise.
     */
    public ListSheetReader(final int startRowIndex, final int endRowIndex, final boolean aliasFirstLine) {
        super(startRowIndex, endRowIndex);
        this.aliasFirstLine = aliasFirstLine;
    }

    /**
     * Reads the sheet and converts each row into a list of cell values.
     *
     * @param sheet The {@link Sheet} to read.
     * @return A list of lists, where each inner list represents a row of cell values.
     */
    @Override
    public List<List<Object>> read(final Sheet sheet) {
        final List<List<Object>> resultList = new ArrayList<>();

        final int startRowIndex = Math.max(this.cellRangeAddress.getFirstRow(), sheet.getFirstRowNum());// Read starting
                                                                                                        // row
                                                                                                        // (inclusive).
        final int endRowIndex = Math.min(this.cellRangeAddress.getLastRow(), sheet.getLastRowNum());// Read ending row
                                                                                                    // (inclusive).

        List<Object> rowList;
        final CellEditor cellEditor = this.config.getCellEditor();
        final boolean ignoreEmptyRow = this.config.isIgnoreEmptyRow();
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            rowList = RowKit.readRow(sheet.getRow(i), cellEditor);
            if (CollKit.isNotEmpty(rowList) || !ignoreEmptyRow) {
                if (aliasFirstLine && i == startRowIndex) {
                    // The first row is treated as a header row, apply aliases.
                    rowList = this.config.aliasHeader(rowList);
                }
                resultList.add(rowList);
            }
        }
        return resultList;
    }

}
