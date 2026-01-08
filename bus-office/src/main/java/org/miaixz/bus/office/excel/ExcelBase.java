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
package org.miaixz.bus.office.excel;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.office.Builder;
import org.miaixz.bus.office.excel.reader.ExcelReader;
import org.miaixz.bus.office.excel.writer.ExcelWriter;
import org.miaixz.bus.office.excel.xyz.CellKit;
import org.miaixz.bus.office.excel.xyz.RowKit;
import org.miaixz.bus.office.excel.xyz.SheetKit;
import org.miaixz.bus.office.excel.xyz.StyleKit;

/**
 * Base class for Excel operations, abstracting common objects and methods for {@link ExcelWriter} and
 * {@link ExcelReader}.
 *
 * @param <T> The type of the subclass, used for returning {@code this}.
 * @param <C> The type of the Excel configuration.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelBase<T extends ExcelBase<T, C>, C extends ExcelConfig> implements Closeable {

    /**
     * Excel configuration, cannot be null.
     */
    protected C config;
    /**
     * Flag indicating whether the Excel workbook has been closed.
     */
    protected boolean isClosed;
    /**
     * The target file. This parameter is {@code null} if the user reads from a stream or creates their own Workbook or
     * Sheet.
     */
    protected File targetFile;
    /**
     * The Excel workbook instance.
     */
    protected Workbook workbook;
    /**
     * The current Sheet in the Excel workbook.
     */
    protected Sheet sheet;

    /**
     * Constructs a new {@code ExcelBase} instance.
     *
     * @param config The Excel configuration.
     * @param sheet  The Excel sheet.
     * @throws NullPointerException if {@code config} or {@code sheet} is {@code null}.
     */
    public ExcelBase(final C config, final Sheet sheet) {
        this.config = Assert.notNull(config);
        this.sheet = Assert.notNull(sheet, "No Sheet provided.");
        this.workbook = sheet.getWorkbook();
    }

    /**
     * Gets the Excel configuration.
     *
     * @return The Excel configuration.
     */
    public C getConfig() {
        return this.config;
    }

    /**
     * Sets the Excel configuration.
     *
     * @param config The Excel configuration.
     * @return This {@code ExcelBase} instance, for chaining.
     */
    public T setConfig(final C config) {
        this.config = config;
        return (T) this;
    }

    /**
     * Gets the underlying {@link Workbook}.
     *
     * @return The {@link Workbook} instance.
     */
    public Workbook getWorkbook() {
        return this.workbook;
    }

    /**
     * Returns the number of sheets in the workbook.
     *
     * @return The total number of sheets.
     */
    public int getSheetCount() {
        return this.workbook.getNumberOfSheets();
    }

    /**
     * Gets all sheets in this workbook.
     *
     * @return A list of {@link Sheet} objects.
     */
    public List<Sheet> getSheets() {
        final int totalSheet = getSheetCount();
        final List<Sheet> result = new ArrayList<>(totalSheet);
        for (int i = 0; i < totalSheet; i++) {
            result.add(this.workbook.getSheetAt(i));
        }
        return result;
    }

    /**
     * Gets the names of all sheets in this workbook.
     *
     * @return A list of sheet names.
     */
    public List<String> getSheetNames() {
        final int totalSheet = workbook.getNumberOfSheets();
        final List<String> result = new ArrayList<>(totalSheet);
        for (int i = 0; i < totalSheet; i++) {
            result.add(this.workbook.getSheetAt(i).getSheetName());
        }
        return result;
    }

    /**
     * Gets the current active {@link Sheet}.
     *
     * @return The current {@link Sheet}.
     */
    public Sheet getSheet() {
        return this.sheet;
    }

    /**
     * Sets the sheet to read from or write to. If the given sheet does not exist, it will be created. In reading, this
     * method is used to switch the sheet to read from. In writing, this method is used to create or switch sheets.
     *
     * @param sheetName The name of the sheet.
     * @return This {@code ExcelBase} instance, for chaining.
     */
    public T setSheet(final String sheetName) {
        return setSheet(SheetKit.getOrCreateSheet(this.workbook, sheetName));
    }

    /**
     * Sets the sheet to read from or write to. If the given sheet index does not exist, it will be created (with a
     * default name). In reading, this method is used to switch the sheet to read from. In writing, this method is used
     * to create or switch sheets.
     *
     * @param sheetIndex The index of the sheet, starting from 0.
     * @return This {@code ExcelBase} instance, for chaining.
     */
    public T setSheet(final int sheetIndex) {
        return setSheet(SheetKit.getOrCreateSheet(this.workbook, sheetIndex));
    }

    /**
     * Sets the custom {@link Sheet}.
     *
     * @param sheet The custom {@link Sheet} instance, which can be created using
     *              {@link SheetKit#getOrCreateSheet(Workbook, String)}.
     * @return This {@code ExcelBase} instance, for chaining.
     */
    public T setSheet(final Sheet sheet) {
        this.sheet = sheet;
        return (T) this;
    }

    /**
     * Renames the current active sheet.
     *
     * @param newName The new name for the sheet.
     * @return This {@code ExcelBase} instance, for chaining.
     * @see Workbook#setSheetName(int, String)
     */
    public T renameSheet(final String newName) {
        this.workbook.setSheetName(this.workbook.getSheetIndex(this.sheet), newName);
        return (T) this;
    }

    /**
     * Clones the current sheet to a new sheet.
     *
     * @param sheetIndex        The index of the sheet to clone.
     * @param newSheetName      The name of the new sheet.
     * @param setAsCurrentSheet {@code true} to set the cloned sheet as the current active sheet, {@code false}
     *                          otherwise.
     * @return This {@code ExcelBase} instance, for chaining.
     */
    public T cloneSheet(final int sheetIndex, final String newSheetName, final boolean setAsCurrentSheet) {
        final Sheet sheet;
        if (this.workbook instanceof XSSFWorkbook xssfWorkbook) {
            sheet = xssfWorkbook.cloneSheet(sheetIndex, newSheetName);
        } else {
            sheet = this.workbook.cloneSheet(sheetIndex);
            // The index of the cloned sheet should be re-obtained.
            this.workbook.setSheetName(workbook.getSheetIndex(sheet), newSheetName);
        }
        if (setAsCurrentSheet) {
            this.sheet = sheet;
        }
        return (T) this;
    }

    /**
     * Gets the cell at the specified coordinates. Returns {@code null} if the cell does not exist.
     *
     * @param locationRef The cell address identifier, e.g., A11, B5.
     * @return The {@link Cell} at the specified location, or {@code null} if not found.
     */
    public Cell getCell(final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return getCell(cellReference.getCol(), cellReference.getRow());
    }

    /**
     * Gets the cell at the specified coordinates. Returns {@code null} if the cell does not exist.
     *
     * @param x X-coordinate (column index), starting from 0.
     * @param y Y-coordinate (row index), starting from 0.
     * @return The {@link Cell} at the specified coordinates, or {@code null} if not found.
     */
    public Cell getCell(final int x, final int y) {
        return getCell(x, y, false);
    }

    /**
     * Gets or creates the cell at the specified coordinates.
     *
     * @param locationRef The cell address identifier, e.g., A11, B5.
     * @return The {@link Cell} at the specified location.
     */
    public Cell getOrCreateCell(final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return getOrCreateCell(cellReference.getCol(), cellReference.getRow());
    }

    /**
     * Gets or creates the cell at the specified coordinates.
     *
     * @param x X-coordinate (column index), starting from 0.
     * @param y Y-coordinate (row index), starting from 0.
     * @return The {@link Cell} at the specified coordinates.
     */
    public Cell getOrCreateCell(final int x, final int y) {
        return getCell(x, y, true);
    }

    /**
     * Gets the cell at the specified coordinates. If {@code isCreateIfNotExist} is {@code false}, returns {@code null}
     * if the cell does not exist.
     *
     * @param locationRef        The cell address identifier, e.g., A11, B5.
     * @param isCreateIfNotExist {@code true} to create the cell if it does not exist, {@code false} otherwise.
     * @return The {@link Cell} at the specified location.
     */
    public Cell getCell(final String locationRef, final boolean isCreateIfNotExist) {
        final CellReference cellReference = new CellReference(locationRef);
        return getCell(cellReference.getCol(), cellReference.getRow(), isCreateIfNotExist);
    }

    /**
     * Gets the cell at the specified coordinates. If {@code isCreateIfNotExist} is {@code false}, returns {@code null}
     * if the cell does not exist.
     *
     * @param x                  X-coordinate (column index), starting from 0.
     * @param y                  Y-coordinate (row index), starting from 0.
     * @param isCreateIfNotExist {@code true} to create the cell if it does not exist, {@code false} otherwise.
     * @return The {@link Cell} at the specified coordinates.
     */
    public Cell getCell(final int x, final int y, final boolean isCreateIfNotExist) {
        return CellKit.getCell(this.sheet, x, y, isCreateIfNotExist);
    }

    /**
     * Gets or creates a row at the specified index.
     *
     * @param y Y-coordinate (row index), starting from 0.
     * @return The {@link Row} at the specified index.
     */
    public Row getOrCreateRow(final int y) {
        return RowKit.getOrCreateRow(this.sheet, y);
    }

    /**
     * Gets the total number of rows in the current sheet. The calculation method is: {@code last row index + 1}.
     *
     * @return The total number of rows.
     */
    public int getRowCount() {
        return this.sheet.getLastRowNum() + 1;
    }

    /**
     * Gets the number of physical rows (rows with content) in the current sheet. The calculation method is:
     * {@code last row index - first row index + 1}.
     *
     * @return The number of physical rows.
     */
    public int getPhysicalRowCount() {
        return this.sheet.getPhysicalNumberOfRows();
    }

    /**
     * Gets or creates the cell style for the specified cell. After obtaining the style, its content can be set.
     *
     * @param locationRef The cell address identifier, e.g., A11, B5.
     * @return The {@link CellStyle} for the specified cell.
     */
    public CellStyle getOrCreateCellStyle(final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return getOrCreateCellStyle(cellReference.getCol(), cellReference.getRow());
    }

    /**
     * Gets or creates the cell style for the specified cell. After obtaining the style, its content can be set.
     *
     * @param x X-coordinate (column index), starting from 0.
     * @param y Y-coordinate (row index), starting from 0.
     * @return The {@link CellStyle} for the specified cell.
     */
    public CellStyle getOrCreateCellStyle(final int x, final int y) {
        final CellStyle cellStyle = getOrCreateCell(x, y).getCellStyle();
        return StyleKit.isNullOrDefaultStyle(this.workbook, cellStyle) ? createCellStyle(x, y) : cellStyle;
    }

    /**
     * Creates a new cell style for the specified cell. After obtaining the style, its content can be set.
     *
     * @param locationRef The cell address identifier, e.g., A11, B5.
     * @return The newly created {@link CellStyle} for the specified cell.
     */
    public CellStyle createCellStyle(final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return createCellStyle(cellReference.getCol(), cellReference.getRow());
    }

    /**
     * Creates a new cell style for the specified cell. After obtaining the style, its content can be set.
     *
     * @param x X-coordinate (column index), starting from 0.
     * @param y Y-coordinate (row index), starting from 0.
     * @return The newly created {@link CellStyle} for the specified cell.
     */
    public CellStyle createCellStyle(final int x, final int y) {
        final Cell cell = getOrCreateCell(x, y);
        final CellStyle cellStyle = this.workbook.createCellStyle();
        cell.setCellStyle(cellStyle);
        return cellStyle;
    }

    /**
     * Creates a new cell style for the workbook.
     *
     * @return The newly created {@link CellStyle}.
     * @see Workbook#createCellStyle()
     */
    public CellStyle createCellStyle() {
        return StyleKit.createCellStyle(this.workbook);
    }

    /**
     * Gets or creates the row style for a specific row. After obtaining the style, its content can be set. Note that
     * setting the background color via row style might be overwritten after setting cell values; individual cell styles
     * should be set for that.
     *
     * @param y Y-coordinate (row index), starting from 0.
     * @return The {@link CellStyle} for the specified row.
     */
    public CellStyle getOrCreateRowStyle(final int y) {
        final CellStyle rowStyle = getOrCreateRow(y).getRowStyle();
        return StyleKit.isNullOrDefaultStyle(this.workbook, rowStyle) ? createRowStyle(y) : rowStyle;
    }

    /**
     * Creates a new row style for a specific row. After obtaining the style, its content can be set.
     *
     * @param y Y-coordinate (row index), starting from 0.
     * @return The newly created {@link CellStyle} for the specified row.
     */
    public CellStyle createRowStyle(final int y) {
        final CellStyle rowStyle = this.workbook.createCellStyle();
        getOrCreateRow(y).setRowStyle(rowStyle);
        return rowStyle;
    }

    /**
     * Gets or creates the column style for a specific column. After obtaining the style, its content can be set. Note
     * that setting the background color via column style might be overwritten after setting cell values; individual
     * cell styles should be set for that.
     *
     * @param x X-coordinate (column index), starting from 0.
     * @return The {@link CellStyle} for the specified column.
     */
    public CellStyle getOrCreateColumnStyle(final int x) {
        final CellStyle columnStyle = this.sheet.getColumnStyle(x);
        return StyleKit.isNullOrDefaultStyle(this.workbook, columnStyle) ? createColumnStyle(x) : columnStyle;
    }

    /**
     * Creates a new column style for a specific column. After obtaining the style, its content can be set.
     *
     * @param x X-coordinate (column index), starting from 0.
     * @return The newly created {@link CellStyle} for the specified column.
     */
    public CellStyle createColumnStyle(final int x) {
        final CellStyle columnStyle = this.workbook.createCellStyle();
        this.sheet.setDefaultColumnStyle(x, columnStyle);
        return columnStyle;
    }

    /**
     * Creates a new font for the workbook.
     *
     * @return The newly created {@link Font}.
     */
    public Font createFont() {
        return getWorkbook().createFont();
    }

    /**
     * Creates a {@link Hyperlink} with default content (label is the link address itself).
     *
     * @param type    The type of the hyperlink.
     * @param address The address of the hyperlink.
     * @return The created {@link Hyperlink}.
     */
    public Hyperlink createHyperlink(final HyperlinkType type, final String address) {
        return createHyperlink(type, address, address);
    }

    /**
     * Creates a {@link Hyperlink} with specified type, address, and label.
     *
     * @param type    The type of the hyperlink.
     * @param address The address of the hyperlink.
     * @param label   The label to display in the cell.
     * @return The created {@link Hyperlink}.
     */
    public Hyperlink createHyperlink(final HyperlinkType type, final String address, final String label) {
        final Hyperlink hyperlink = this.workbook.getCreationHelper().createHyperlink(type);
        hyperlink.setAddress(address);
        hyperlink.setLabel(label);
        return hyperlink;
    }

    /**
     * Gets the total number of columns in the first row. The calculation method is: {@code last column index + 1}.
     *
     * @return The total number of columns.
     */
    public int getColumnCount() {
        return getColumnCount(0);
    }

    /**
     * Gets the total number of columns in the specified row. The calculation method is: {@code last column index + 1}.
     *
     * @param rowNum The row number.
     * @return The total number of columns, or -1 if the row does not exist.
     */
    public int getColumnCount(final int rowNum) {
        final Row row = this.sheet.getRow(rowNum);
        if (null != row) {
            // The getLastCellNum method returns the value of index + 1.
            return row.getLastCellNum();
        }
        return -1;
    }

    /**
     * Checks if the Excel file is in XLSX format (Excel 2007+ format).
     *
     * @return {@code true} if it is an XLSX format Excel file, {@code false} otherwise.
     */
    public boolean isXlsx() {
        return this.sheet instanceof XSSFSheet || this.sheet instanceof SXSSFSheet;
    }

    /**
     * Gets the value for the Content-Type header. This can be used to quickly set the download header for Excel files:
     *
     * <pre>
     * response.setContentType(excelWriter.getContentType());
     * </pre>
     *
     * @return The Content-Type value.
     */
    public String getContentType() {
        return isXlsx() ? Builder.XLSX_CONTENT_TYPE : Builder.XLS_CONTENT_TYPE;
    }

    /**
     * Closes the workbook. If a target file is set, the workbook will be flushed to it before closing.
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.workbook);
        this.sheet = null;
        this.workbook = null;
        this.isClosed = true;
    }

    /**
     * Checks if the Excel workbook has been closed. Throws an {@link Assert} exception if it is closed.
     */
    protected void checkClosed() {
        Assert.isFalse(this.isClosed, "Excel has been closed!");
    }

}
