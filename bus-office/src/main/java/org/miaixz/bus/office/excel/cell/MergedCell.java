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
