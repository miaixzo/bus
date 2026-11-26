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
package org.miaixz.bus.office.excel.cell;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * This object represents a non-existent cell. All retrieved values will be {@code null}. This object is only used to
 * mark the location information of a cell.
 *
 * @author Kimi Liu
 */
public class NullCell implements Cell {

    /**
     * The row this cell belongs to.
     */
    private final Row row;

    /**
     * The 0-based column index of this cell.
     */
    private final int columnIndex;

    /**
     * Constructs a {@code NullCell} instance.
     *
     * @param row         The row this null cell belongs to.
     * @param columnIndex The 0-based column index of this null cell.
     */
    public NullCell(final Row row, final int columnIndex) {
        this.row = row;
        this.columnIndex = columnIndex;
    }

    /**
     * Returns the zero-based column index of this null cell.
     *
     * @return The 0-based column index.
     */
    @Override
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * Returns the zero-based index of the containing row.
     *
     * @return The 0-based row index.
     */
    @Override
    public int getRowIndex() {
        return getRow().getRowNum();
    }

    /**
     * Returns the containing sheet.
     *
     * @return The sheet this cell belongs to.
     */
    @Override
    public Sheet getSheet() {
        return getRow().getSheet();
    }

    /**
     * Returns the containing row.
     *
     * @return The row this cell belongs to.
     */
    @Override
    public Row getRow() {
        return this.row;
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setBlank() {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * Returns no cell type for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public CellType getCellType() {
        return null;
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param cellType The type to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellType(final CellType cellType) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * Returns no cached formula result type for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public CellType getCachedFormulaResultType() {
        return null;
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param value The value to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellValue(final double value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param value The value to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellValue(final Date value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param value The value to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellValue(final LocalDateTime value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param value The value to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellValue(final Calendar value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param value The value to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellValue(final RichTextString value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param value The value to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellValue(final String value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @throws UnsupportedOperationException always
     * @throws IllegalStateException         not thrown by this implementation because the operation always raises
     *                                       UnsupportedOperationException
     */
    @Override
    public void removeFormula() throws IllegalStateException {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * Returns no formula for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public String getCellFormula() {
        return null;
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param formula The formula to set.
     * @throws UnsupportedOperationException always
     * @throws FormulaParseException         not thrown by this implementation because the operation always raises
     *                                       UnsupportedOperationException
     * @throws IllegalStateException         not thrown by this implementation because the operation always raises
     *                                       UnsupportedOperationException
     */
    @Override
    public void setCellFormula(final String formula) throws FormulaParseException, IllegalStateException {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @return nothing, always throws UnsupportedOperationException
     * @throws UnsupportedOperationException always
     */
    @Override
    public double getNumericCellValue() {
        throw new UnsupportedOperationException("Cell value is null!");
    }

    /**
     * Returns no date value for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public Date getDateCellValue() {
        return null;
    }

    /**
     * Returns no local date-time value for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public LocalDateTime getLocalDateTimeCellValue() {
        return null;
    }

    /**
     * Returns no rich-text value for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public RichTextString getRichStringCellValue() {
        return null;
    }

    /**
     * Returns no string value for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public String getStringCellValue() {
        return null;
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param value The value to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellValue(final boolean value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param value The value to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellErrorValue(final byte value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @return nothing, always throws UnsupportedOperationException
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean getBooleanCellValue() {
        throw new UnsupportedOperationException("Cell value is null!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @return nothing, always throws UnsupportedOperationException
     * @throws UnsupportedOperationException always
     */
    @Override
    public byte getErrorCellValue() {
        throw new UnsupportedOperationException("Cell value is null!");
    }

    /**
     * Returns no style for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public CellStyle getCellStyle() {
        return null;
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param style The style to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellStyle(final CellStyle style) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setAsActiveCell() {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * Returns no address object for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public CellAddress getAddress() {
        return null;
    }

    /**
     * Returns no comment for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public Comment getCellComment() {
        return null;
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param comment The comment to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCellComment(final Comment comment) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void removeCellComment() {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * Returns no hyperlink for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public Hyperlink getHyperlink() {
        return null;
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @param link The hyperlink to set.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setHyperlink(final Hyperlink link) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void removeHyperlink() {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
     * Returns no array-formula range for this null cell.
     *
     * @return {@code null} always.
     */
    @Override
    public CellRangeAddress getArrayFormulaRange() {
        return null;
    }

    /**
     * This operation is not supported for a NullCell.
     *
     * @return nothing, always throws UnsupportedOperationException
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean isPartOfArrayFormulaGroup() {
        throw new UnsupportedOperationException("Cell value is null!");
    }

}
