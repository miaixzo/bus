/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.office.excel.RowKit;
import org.miaixz.bus.office.excel.cell.CellEditor;

/**
 * 读取{@link Sheet}为List列表形式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListSheetReader extends AbstractSheetReader<List<List<Object>>> {

    /**
     * 是否首行作为标题行转换别名
     */
    private final boolean aliasFirstLine;

    /**
     * 构造
     *
     * @param startRowIndex  起始行（包含，从0开始计数）
     * @param endRowIndex    结束行（包含，从0开始计数）
     * @param aliasFirstLine 是否首行作为标题行转换别名
     */
    public ListSheetReader(final int startRowIndex, final int endRowIndex, final boolean aliasFirstLine) {
        super(startRowIndex, endRowIndex);
        this.aliasFirstLine = aliasFirstLine;
    }

    @Override
    public List<List<Object>> read(final Sheet sheet) {
        final List<List<Object>> resultList = new ArrayList<>();

        final int startRowIndex = Math.max(this.cellRangeAddress.getFirstRow(), sheet.getFirstRowNum());// 读取起始行（包含）
        final int endRowIndex = Math.min(this.cellRangeAddress.getLastRow(), sheet.getLastRowNum());// 读取结束行（包含）

        List<Object> rowList;
        final CellEditor cellEditor = this.config.getCellEditor();
        final boolean ignoreEmptyRow = this.config.isIgnoreEmptyRow();
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            rowList = RowKit.readRow(sheet.getRow(i), cellEditor);
            if (CollKit.isNotEmpty(rowList) || !ignoreEmptyRow) {
                if (aliasFirstLine && i == startRowIndex) {
                    // 第一行作为标题行，替换别名
                    rowList = Convert.toList(Object.class, this.config.aliasHeader(rowList));
                }
                resultList.add(rowList);
            }
        }
        return resultList;
    }

}
