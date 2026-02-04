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
package org.miaixz.bus.office.excel.reader;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.xyz.CellKit;

/**
 * Reads a single column from an Excel sheet.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ColumnSheetReader extends AbstractSheetReader<List<Object>> {

    /**
     * Constructs a new {@code ColumnSheetReader}.
     *
     * @param columnIndex   The column index (0-based).
     * @param startRowIndex The starting row index (inclusive, 0-based).
     * @param endRowIndex   The ending row index (inclusive, 0-based).
     */
    public ColumnSheetReader(final int columnIndex, final int startRowIndex, final int endRowIndex) {
        super(new CellRangeAddress(startRowIndex, endRowIndex, columnIndex, columnIndex));
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param sheet the sheet to read from
     * @return list of cell values from the specified column range
     */
    @Override
    public List<Object> read(final Sheet sheet) {
        final List<Object> resultList = new ArrayList<>();

        final int startRowIndex = Math.max(this.cellRangeAddress.getFirstRow(), sheet.getFirstRowNum());// Read starting
                                                                                                        // row
                                                                                                        // (inclusive).
        final int endRowIndex = Math.min(this.cellRangeAddress.getLastRow(), sheet.getLastRowNum());// Read ending row
                                                                                                    // (inclusive).
        final int columnIndex = this.cellRangeAddress.getFirstColumn();

        final CellEditor cellEditor = this.config.getCellEditor();
        final boolean ignoreEmptyRow = this.config.isIgnoreEmptyRow();
        Object value;
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            value = CellKit.getCellValue(CellKit.getCell(sheet.getRow(i), columnIndex), cellEditor);
            if (null != value || !ignoreEmptyRow) {
                resultList.add(value);
            }
        }

        return resultList;
    }

}
