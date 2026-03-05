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

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.miaixz.bus.office.excel.cell.values.FormulaCellValue;
import org.miaixz.bus.office.excel.CellKit;

/**
 * A virtual cell representing a cell's position, value, and style, but not an actually created cell. Note: Setting
 * values and styles on a virtual cell will not take effect in the actual workbook.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VirtualCell extends CellBase {

    /**
     * The row this virtual cell belongs to.
     */
    private final Row row;
    /**
     * The column index (0-based).
     */
    private final int columnIndex;
    /**
     * The row index (0-based).
     */
    private final int rowIndex;

    /**
     * The cell type.
     */
    private CellType cellType;
    /**
     * The cell value.
     */
    private Object value;
    /**
     * The cell style.
     */
    private CellStyle style;
    /**
     * The cell comment.
     */
    private Comment comment;

    /**
     * Constructs a {@code VirtualCell} based on an existing cell and new coordinates.
     *
     * @param cell The reference cell.
     * @param x    The new 0-based column index.
     * @param y    The new 0-based row index.
     */
    public VirtualCell(final Cell cell, final int x, final int y) {
        this(cell.getRow(), x, y);
        this.cellType = cell.getCellType();
        this.value = CellKit.getCellValue(cell);
        this.style = cell.getCellStyle();
        this.comment = cell.getCellComment();
    }

    /**
     * Constructs a {@code VirtualCell} based on an existing cell, new coordinates, and a new value.
     *
     * @param cell  The reference cell.
     * @param x     The new 0-based column index.
     * @param y     The new 0-based row index.
     * @param value The new value for the cell.
     */
    public VirtualCell(final Cell cell, final int x, final int y, final Object value) {
        this(cell.getRow(), x, y);
        this.style = cell.getCellStyle();
        this.comment = cell.getCellComment();
        CellKit.setCellValue(this, value);
    }

    /**
     * Constructs a {@code VirtualCell} with specified row, column, and row index.
     *
     * @param row The row this virtual cell belongs to.
     * @param x   The 0-based column index.
     * @param y   The 0-based row index.
     */
    public VirtualCell(final Row row, final int x, final int y) {
        this.row = row;
        this.rowIndex = y;
        this.columnIndex = x;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param cellType the cell type to set
     */
    @Override
    protected void setCellTypeImpl(final CellType cellType) {
        this.cellType = cellType;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param formula the formula string to set
     */
    @Override
    protected void setCellFormulaImpl(final String formula) {
        this.value = new FormulaCellValue(formula);
    }

    /**
     * Implements the behavior defined by the supertype.
     */
    @Override
    protected void removeFormulaImpl() {
        if (this.value instanceof FormulaCellValue) {
            this.value = null;
        }
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param value the numeric value to set
     */
    @Override
    protected void setCellValueImpl(final double value) {
        this.cellType = CellType.NUMERIC;
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param value the date value to set
     */
    @Override
    protected void setCellValueImpl(final Date value) {
        this.cellType = CellType.NUMERIC;
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param value the local date time value to set
     */
    @Override
    protected void setCellValueImpl(final LocalDateTime value) {
        this.cellType = CellType.NUMERIC;
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param value the calendar value to set
     */
    @Override
    protected void setCellValueImpl(final Calendar value) {
        this.cellType = CellType.NUMERIC;
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param value the string value to set
     */
    @Override
    protected void setCellValueImpl(final String value) {
        this.cellType = CellType.STRING;
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param value the rich text string value to set
     */
    @Override
    protected void setCellValueImpl(final RichTextString value) {
        this.cellType = CellType.STRING;
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the spreadsheet version (always Excel2007 for virtual cells)
     */
    @Override
    protected SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL2007;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the column index of this cell
     */
    @Override
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the row index of this cell
     */
    @Override
    public int getRowIndex() {
        return this.rowIndex;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the sheet that this cell belongs to
     */
    @Override
    public Sheet getSheet() {
        return this.row.getSheet();
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the row that this cell belongs to
     */
    @Override
    public Row getRow() {
        return this.row;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the cell type
     */
    @Override
    public CellType getCellType() {
        return this.cellType;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the cached formula result type, or null if not a formula
     */
    @Override
    public CellType getCachedFormulaResultType() {
        if (this.value instanceof FormulaCellValue) {
            return ((FormulaCellValue) this.value).getResultType();
        }
        return null;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the formula string, or null if not a formula
     */
    @Override
    public String getCellFormula() {
        if (this.value instanceof FormulaCellValue) {
            return ((FormulaCellValue) this.value).getValue();
        }
        return null;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the numeric value
     */
    @Override
    public double getNumericCellValue() {
        return (double) this.value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the date value
     */
    @Override
    public Date getDateCellValue() {
        return (Date) this.value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the local date time value
     */
    @Override
    public LocalDateTime getLocalDateTimeCellValue() {
        return (LocalDateTime) this.value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the rich text string value
     */
    @Override
    public RichTextString getRichStringCellValue() {
        return (RichTextString) this.value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the string value
     */
    @Override
    public String getStringCellValue() {
        return (String) this.value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param value the boolean value to set
     */
    @Override
    public void setCellValue(final boolean value) {
        this.cellType = CellType.BOOLEAN;
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param value the error value to set
     */
    @Override
    public void setCellErrorValue(final byte value) {
        this.cellType = CellType.ERROR;
        this.value = value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the boolean value
     */
    @Override
    public boolean getBooleanCellValue() {
        return (boolean) this.value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the error value
     */
    @Override
    public byte getErrorCellValue() {
        return (byte) this.value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the cell style
     */
    @Override
    public CellStyle getCellStyle() {
        return this.style;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param style the cell style to set
     */
    @Override
    public void setCellStyle(final CellStyle style) {
        this.style = style;
    }

    /**
     * Implements the behavior defined by the supertype.
     */
    @Override
    public void setAsActiveCell() {
        throw new UnsupportedOperationException("Virtual cell cannot be set as active cell");
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the cell comment
     */
    @Override
    public Comment getCellComment() {
        return this.comment;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param comment the cell comment to set
     */
    @Override
    public void setCellComment(final Comment comment) {
        this.comment = comment;
    }

    /**
     * Implements the behavior defined by the supertype.
     */
    @Override
    public void removeCellComment() {
        this.comment = null;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the hyperlink associated with this cell
     */
    @Override
    public Hyperlink getHyperlink() {
        return (Hyperlink) this.value;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param link the hyperlink to set
     */
    @Override
    public void setHyperlink(final Hyperlink link) {
        this.value = link;
    }

    /**
     * Implements the behavior defined by the supertype.
     */
    @Override
    public void removeHyperlink() {
        if (this.value instanceof Hyperlink) {
            this.value = null;
        }
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return null (virtual cells are not part of array formulas)
     */
    @Override
    public CellRangeAddress getArrayFormulaRange() {
        return null;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return false (virtual cells are not part of array formula groups)
     */
    @Override
    public boolean isPartOfArrayFormulaGroup() {
        return false;
    }

}
