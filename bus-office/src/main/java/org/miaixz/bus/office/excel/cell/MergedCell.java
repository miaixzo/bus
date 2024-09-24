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
package org.miaixz.bus.office.excel.cell;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.miaixz.bus.office.excel.xyz.CellKit;

/**
 * 合并单元格封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MergedCell {

    private final Cell first;
    private final CellRangeAddress range;

    /**
     * 构造
     *
     * @param first 第一个单元格，即左上角的单元格
     * @param range 合并单元格范围
     */
    public MergedCell(final Cell first, final CellRangeAddress range) {
        this.first = first;
        this.range = range;
    }

    /**
     * 创建MergedCell
     *
     * @param cell        第一个单元格，即左上角的单元格
     * @param rowCount    占用行数
     * @param columnCount 占用列数
     * @return MergedCell
     */
    public static MergedCell of(final Cell cell, final int rowCount, final int columnCount) {
        final int rowIndex = cell.getRowIndex();
        final int columnIndex = cell.getColumnIndex();
        return of(cell,
                new CellRangeAddress(rowIndex, rowIndex + rowCount - 1, columnIndex, columnIndex + columnCount - 1));
    }

    /**
     * 创建MergedCell
     *
     * @param cell  第一个单元格，即左上角的单元格
     * @param range 合并单元格范围
     * @return MergedCell
     */
    public static MergedCell of(final Cell cell, final CellRangeAddress range) {
        return new MergedCell(cell, range);
    }

    /**
     * 获取第一个单元格，即左上角的单元格
     *
     * @return Cell
     */
    public Cell getFirst() {
        return this.first;
    }

    /**
     * 获取合并单元格范围
     *
     * @return CellRangeAddress
     */
    public CellRangeAddress getRange() {
        return this.range;
    }

    /**
     * 设置单元格样式
     *
     * @param cellStyle 单元格样式
     * @return this
     */
    public MergedCell setCellStyle(final CellStyle cellStyle) {
        this.first.setCellStyle(cellStyle);
        return this;
    }

    /**
     * 设置单元格值
     *
     * @param value 值
     * @return this
     */
    public MergedCell setValue(final Object value) {
        CellKit.setCellValue(this.first, value);
        return this;
    }

}
