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
package org.miaixz.bus.office.excel.sax.handler;

import org.apache.poi.ss.usermodel.CellStyle;

import java.util.List;

/**
 * Sax方式读取Excel行处理器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface RowHandler {

    /**
     * 处理一行数据
     *
     * @param sheetIndex 当前Sheet序号
     * @param rowIndex   当前行号，从0开始计数
     * @param rowCells   行数据，每个Object表示一个单元格的值
     */
    void handle(int sheetIndex, long rowIndex, List<Object> rowCells);

    /**
     * 处理一个单元格的数据
     *
     * @param sheetIndex    当前Sheet序号
     * @param rowIndex      当前行号
     * @param cellIndex     当前列号
     * @param value         单元格的值
     * @param xssfCellStyle 单元格样式
     */
    default void handleCell(final int sheetIndex, final long rowIndex, final int cellIndex, final Object value, final CellStyle xssfCellStyle) {
        //pass
    }

    /**
     * 处理一个sheet页完成的操作
     */
    default void doAfterAllAnalysed() {
        //pass
    }

}
