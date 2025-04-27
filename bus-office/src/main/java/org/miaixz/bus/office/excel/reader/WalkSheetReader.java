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
package org.miaixz.bus.office.excel.reader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.xyz.CellKit;

/**
 * 读取Excel的Sheet，使用Consumer方式处理单元格
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WalkSheetReader extends AbstractSheetReader<Void> {

    private final BiConsumerX<Cell, Object> cellHandler;

    /**
     * 构造
     *
     * @param startRowIndex 起始行（包含，从0开始计数）
     * @param endRowIndex   结束行（包含，从0开始计数）
     * @param cellHandler   单元格处理器，用于处理读到的单元格及其数据
     */
    public WalkSheetReader(final int startRowIndex, final int endRowIndex,
            final BiConsumerX<Cell, Object> cellHandler) {
        super(startRowIndex, endRowIndex);
        this.cellHandler = cellHandler;
    }

    @Override
    public Void read(final Sheet sheet) {
        final int startRowIndex = Math.max(this.cellRangeAddress.getFirstRow(), sheet.getFirstRowNum());// 读取起始行（包含）
        final int endRowIndex = Math.min(this.cellRangeAddress.getLastRow(), sheet.getLastRowNum());// 读取结束行（包含）
        final CellEditor cellEditor = this.config.getCellEditor();

        Row row;
        for (int y = startRowIndex; y <= endRowIndex; y++) {
            row = sheet.getRow(y);
            if (null != row) {
                final short startColumnIndex = (short) Math.max(this.cellRangeAddress.getFirstColumn(),
                        row.getFirstCellNum());
                final short endColumnIndex = (short) Math.min(this.cellRangeAddress.getLastColumn(),
                        row.getLastCellNum());
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
