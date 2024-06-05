/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.office.excel.cell.values;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.office.excel.cell.CellEditor;
import org.miaixz.bus.office.excel.cell.CellKit;
import org.miaixz.bus.office.excel.cell.CellValue;
import org.miaixz.bus.office.excel.cell.NullCell;

/**
 * 复合单元格值，用于根据单元格类型读取不同的值
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompositeCellValue implements CellValue<Object> {

    private final Cell cell;
    private final CellType cellType;
    private final CellEditor cellEditor;

    /**
     * 构造
     *
     * @param cell       {@link Cell}单元格
     * @param cellType   单元格值类型{@link CellType}枚举，如果为{@code null}默认使用cell的类型
     * @param cellEditor 单元格值编辑器。可以通过此编辑器对单元格值做自定义操作
     */
    public CompositeCellValue(final Cell cell, final CellType cellType, final CellEditor cellEditor) {
        this.cell = cell;
        this.cellType = cellType;
        this.cellEditor = cellEditor;
    }

    /**
     * 创建CompositeCellValue
     *
     * @param cell       {@link Cell}单元格
     * @param cellType   单元格值类型{@link CellType}枚举，如果为{@code null}默认使用cell的类型
     * @param cellEditor 单元格值编辑器。可以通过此编辑器对单元格值做自定义操作
     * @return CompositeCellValue
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

        // 尝试获取合并单元格，如果是合并单元格，则重新获取单元格类型
        final Cell mergedCell = CellKit.getMergedRegionCell(cell);
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
