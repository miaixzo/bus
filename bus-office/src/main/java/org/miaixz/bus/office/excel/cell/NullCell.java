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
package org.miaixz.bus.office.excel.cell;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * 当单元格不存在时使用此对象表示，得到的值都为null,此对象只用于标注单元格所在位置信息。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NullCell implements Cell {

    private final Row row;
    private final int columnIndex;

    /**
     * 构造
     *
     * @param row         行
     * @param columnIndex 列号，从0开始
     */
    public NullCell(final Row row, final int columnIndex) {
        this.row = row;
        this.columnIndex = columnIndex;
    }

    @Override
    public int getColumnIndex() {
        return this.columnIndex;
    }

    @Override
    public int getRowIndex() {
        return getRow().getRowNum();
    }

    @Override
    public Sheet getSheet() {
        return getRow().getSheet();
    }

    @Override
    public Row getRow() {
        return this.row;
    }

    @Deprecated
    public void setCellType(final CellType cellType) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void setBlank() {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public CellType getCellType() {
        return null;
    }

    @Override
    public CellType getCachedFormulaResultType() {
        return null;
    }

    @Override
    public void setCellValue(final double value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void setCellValue(final Date value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void setCellValue(final LocalDateTime value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void setCellValue(final Calendar value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void setCellValue(final RichTextString value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void setCellValue(final String value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void removeFormula() throws IllegalStateException {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public String getCellFormula() {
        return null;
    }

    @Override
    public void setCellFormula(final String formula) throws FormulaParseException, IllegalStateException {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public double getNumericCellValue() {
        throw new UnsupportedOperationException("Cell value is null!");
    }

    @Override
    public Date getDateCellValue() {
        return null;
    }

    @Override
    public LocalDateTime getLocalDateTimeCellValue() {
        return null;
    }

    @Override
    public RichTextString getRichStringCellValue() {
        return null;
    }

    @Override
    public String getStringCellValue() {
        return null;
    }

    @Override
    public void setCellValue(final boolean value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void setCellErrorValue(final byte value) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public boolean getBooleanCellValue() {
        throw new UnsupportedOperationException("Cell value is null!");
    }

    @Override
    public byte getErrorCellValue() {
        throw new UnsupportedOperationException("Cell value is null!");
    }

    @Override
    public CellStyle getCellStyle() {
        return null;
    }

    @Override
    public void setCellStyle(final CellStyle style) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void setAsActiveCell() {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public CellAddress getAddress() {
        return null;
    }

    @Override
    public Comment getCellComment() {
        return null;
    }

    @Override
    public void setCellComment(final Comment comment) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void removeCellComment() {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public Hyperlink getHyperlink() {
        return null;
    }

    @Override
    public void setHyperlink(final Hyperlink link) {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public void removeHyperlink() {
        throw new UnsupportedOperationException("Can not set any thing to null cell!");
    }

    @Override
    public CellRangeAddress getArrayFormulaRange() {
        return null;
    }

    @Override
    public boolean isPartOfArrayFormulaGroup() {
        throw new UnsupportedOperationException("Cell value is null!");
    }

}
