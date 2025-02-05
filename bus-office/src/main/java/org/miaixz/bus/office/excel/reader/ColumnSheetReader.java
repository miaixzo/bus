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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.xyz.CellKit;

/**
 * 读取单独一列
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ColumnSheetReader extends AbstractSheetReader<List<Object>> {

    /**
     * 构造
     *
     * @param columnIndex   列号，从0开始计数
     * @param startRowIndex 起始行（包含，从0开始计数）
     * @param endRowIndex   结束行（包含，从0开始计数）
     */
    public ColumnSheetReader(final int columnIndex, final int startRowIndex, final int endRowIndex) {
        super(new CellRangeAddress(startRowIndex, endRowIndex, columnIndex, columnIndex));
    }

    @Override
    public List<Object> read(final Sheet sheet) {
        final List<Object> resultList = new ArrayList<>();

        final int startRowIndex = Math.max(this.cellRangeAddress.getFirstRow(), sheet.getFirstRowNum());// 读取起始行（包含）
        final int endRowIndex = Math.min(this.cellRangeAddress.getLastRow(), sheet.getLastRowNum());// 读取结束行（包含）
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
