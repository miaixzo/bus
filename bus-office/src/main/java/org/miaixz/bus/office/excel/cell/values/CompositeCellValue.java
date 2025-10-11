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
package org.miaixz.bus.office.excel.cell.values;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.office.excel.cell.NullCell;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.xyz.CellKit;

/**
 * Composite cell value, used to read different values based on the cell type.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompositeCellValue implements CellValue<Object> {

    private final Cell cell;
    private final CellType cellType;
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

        final Object value;
        switch (cellType) {
            case NUMERIC:
                value = new NumericCellValue(cell).getValue();
                break;

            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;

            case FORMULA:
                value = of(cell, cell.getCachedFormulaResultType(), cellEditor).getValue();
                break;

            case BLANK:
                value = Normal.EMPTY;
                break;

            case ERROR:
                value = new ErrorCellValue(cell).getValue();
                break;

            default:
                value = cell.getStringCellValue();
        }

        return null == cellEditor ? value : cellEditor.edit(cell, value);
    }

}
