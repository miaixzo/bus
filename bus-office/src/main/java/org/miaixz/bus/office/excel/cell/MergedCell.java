/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * Merged cell wrapper.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MergedCell {

    /**
     * The first cell in the merged region, i.e., the top-left cell.
     */
    private final Cell first;
    /**
     * The range of the merged cells.
     */
    private final CellRangeAddress range;

    /**
     * Constructor.
     *
     * @param first The first cell, i.e., the top-left cell of the merged region.
     * @param range The merged cell range.
     */
    public MergedCell(final Cell first, final CellRangeAddress range) {
        this.first = first;
        this.range = range;
    }

    /**
     * Creates a MergedCell.
     *
     * @param cell        The first cell, i.e., the top-left cell of the merged region.
     * @param rowCount    The number of rows to span.
     * @param columnCount The number of columns to span.
     * @return MergedCell
     */
    public static MergedCell of(final Cell cell, final int rowCount, final int columnCount) {
        final int rowIndex = cell.getRowIndex();
        final int columnIndex = cell.getColumnIndex();
        return of(
                cell,
                new CellRangeAddress(rowIndex, rowIndex + rowCount - 1, columnIndex, columnIndex + columnCount - 1));
    }

    /**
     * Creates a MergedCell.
     *
     * @param cell  The first cell, i.e., the top-left cell of the merged region.
     * @param range The merged cell range.
     * @return MergedCell
     */
    public static MergedCell of(final Cell cell, final CellRangeAddress range) {
        return new MergedCell(cell, range);
    }

    /**
     * Gets the first cell, i.e., the top-left cell of the merged region.
     *
     * @return The first cell.
     */
    public Cell getFirst() {
        return this.first;
    }

    /**
     * Gets the merged cell range.
     *
     * @return The CellRangeAddress.
     */
    public CellRangeAddress getRange() {
        return this.range;
    }

    /**
     * Sets the cell style for the merged region.
     *
     * @param cellStyle The cell style to apply.
     * @return this
     */
    public MergedCell setCellStyle(final CellStyle cellStyle) {
        this.first.setCellStyle(cellStyle);
        return this;
    }

    /**
     * Sets the value for the merged region.
     *
     * @param value The value to set.
     * @return this
     */
    public MergedCell setValue(final Object value) {
        CellKit.setCellValue(this.first, value);
        return this;
    }

}
