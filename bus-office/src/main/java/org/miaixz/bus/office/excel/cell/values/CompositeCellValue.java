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
package org.miaixz.bus.office.excel.cell.values;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.office.excel.cell.NullCell;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.CellKit;

/**
 * Composite cell value, used to read different values based on the cell type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CompositeCellValue implements CellValue<Object> {

    /**
     * The cell object.
     */
    private final Cell cell;
    /**
     * The cell type.
     */
    private final CellType cellType;
    /**
     * The cell editor.
     */
    private final CellEditor cellEditor;

    /**
     * Constructs a new {@code CompositeCellValue}.
     *
     * @param cell       The {@link Cell} object.
     * @param cellType   The {@link CellType} enum for the cell value type. If {@code null}, the cell's own type is
     *                   used.
     * @param cellEditor The cell editor. This editor can be used to customize cell values.
     */
    public CompositeCellValue(final Cell cell, final CellType cellType, final CellEditor cellEditor) {
        this.cell = cell;
        this.cellType = cellType;
        this.cellEditor = cellEditor;
    }

    /**
     * Creates a {@code CompositeCellValue} instance.
     *
     * @param cell       The {@link Cell} object.
     * @param cellType   The {@link CellType} enum for the cell value type. If {@code null}, the cell's own type is
     *                   used.
     * @param cellEditor The cell editor. This editor can be used to customize cell values.
     * @return A new {@code CompositeCellValue} instance.
     */
    public static CompositeCellValue of(final Cell cell, final CellType cellType, final CellEditor cellEditor) {
        return new CompositeCellValue(cell, cellType, cellEditor);
    }

    /**
     * Gets the cell value based on the cell type and applies any configured cell editor.
     *
     * @return The cell value, or {@code null} if the cell is null.
     */
    @Override
    public Object getValue() {
        Cell cell = this.cell;
        CellType cellType = this.cellType;
        final CellEditor cellEditor = this.cellEditor;

        if (null == cell) {
            return null;
        }
        if (cell instanceof NullCell) {
            return null == cellEditor ? null : cellEditor.edit(cell, null);
        }
        if (null == cellType) {
            cellType = cell.getCellType();
        }

        // Attempt to get merged cell. If it is a merged cell, re-get the cell type.
        final Cell mergedCell = CellKit.getFirstCellOfMerged(cell);
        if (mergedCell != cell) {
            cell = mergedCell;
            cellType = cell.getCellType();
        }

        final Object value = switch (cellType) {
            case NUMERIC -> new NumericCellValue(cell).getValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> of(cell, cell.getCachedFormulaResultType(), cellEditor).getValue();
            case BLANK -> Normal.EMPTY;
            case ERROR -> new ErrorCellValue(cell).getValue();
            default -> cell.getStringCellValue();
        };

        return null == cellEditor ? value : cellEditor.edit(cell, value);
    }

}
