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
 * @since Java 17+
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
     * @return The 0-based column index.
     */
    @Override
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * @return The 0-based row index.
     */
    @Override
    public int getRowIndex() {
        return getRow().getRowNum();
    }

    /**
     * @return The sheet this cell belongs to.
     */
    @Override
    public Sheet getSheet() {
        return getRow().getSheet();
    }

    /**
     * @return The row this cell belongs to.
     */
    @Override
    public Row getRow() {
        return this.row;
    }

    /**
     * This operation is not supported for a NullCell.
     * 
     * @param cellType The type to set.
     * @throws UnsupportedOperationException always
     */
    @Deprecated
    @Override
    public void setCellType(final CellType cellType) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
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
     * @return {@code null} always.
     */
    @Override
    public CellType getCellType() {
        return null;
    }

    /**
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
     */
    @Override
    public void removeFormula() throws IllegalStateException {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    /**
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
     * @return {@code null} always.
     */
    @Override
    public Date getDateCellValue() {
        return null;
    }

    /**
     * @return {@code null} always.
     */
    @Override
    public LocalDateTime getLocalDateTimeCellValue() {
        return null;
    }

    /**
     * @return {@code null} always.
     */
    @Override
    public RichTextString getRichStringCellValue() {
        return null;
    }

    /**
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
     * @return {@code null} always.
     */
    @Override
    public CellAddress getAddress() {
        return null;
    }

    /**
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
