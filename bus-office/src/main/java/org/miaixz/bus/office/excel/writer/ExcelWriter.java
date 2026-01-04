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
package org.miaixz.bus.office.excel.writer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.office.excel.ExcelBase;
import org.miaixz.bus.office.excel.RowGroup;
import org.miaixz.bus.office.excel.SimpleAnchor;
import org.miaixz.bus.office.excel.shape.ExcelPictureType;
import org.miaixz.bus.office.excel.style.DefaultStyleSet;
import org.miaixz.bus.office.excel.style.LineStyle;
import org.miaixz.bus.office.excel.style.ShapeConfig;
import org.miaixz.bus.office.excel.style.StyleSet;
import org.miaixz.bus.office.excel.xyz.CellKit;
import org.miaixz.bus.office.excel.xyz.SheetKit;
import org.miaixz.bus.office.excel.xyz.WorkbookKit;

/**
 * Excel Writer. This utility is used to write data to Excel through POI. This object can accomplish the following two
 * functions:
 *
 * <pre>
 * 1. Edit an existing Excel file, can write to the original Excel file, or write to another location (to file or to stream)
 * 2. Create a new empty Excel workbook, complete data filling and write out (to file or to stream)
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelWriter extends ExcelBase<ExcelWriter, ExcelWriteConfig> {

    /**
     * Style set, defining styles for different data types.
     */
    private StyleSet styleSet;
    /**
     * Sheet data writer.
     */
    private SheetDataWriter sheetDataWriter;
    /**
     * Template Excel writer.
     */
    private SheetTemplateWriter sheetTemplateWriter;

    /**
     * Constructor, creates an Excel file in xlsx format by default. This constructor does not pass the Excel file path
     * to be written, only can call {@link #flush(OutputStream)} method to write to the stream. If writing to a file,
     * you also need to call {@link #setTargetFile(File)} method to customize the file to be written, then call
     * {@link #flush()} method to write to the file.
     */
    public ExcelWriter() {
        this(true);
    }

    /**
     * This constructor does not pass the Excel file path to be written, only can call {@link #flush(OutputStream)}
     * method to write to the stream. If writing to a file, you need to call {@link #flush(File, boolean)} to write to
     * the file.
     *
     * @param isXlsx whether it is xlsx format.
     */
    public ExcelWriter(final boolean isXlsx) {
        this(WorkbookKit.createBook(isXlsx), null);
    }

    /**
     * Constructor, writes to the first sheet by default, the first sheet is named sheet1.
     *
     * @param templateFilePath template file, if it does not exist, it defaults to the target output file.
     */
    public ExcelWriter(final String templateFilePath) {
        this(templateFilePath, null);
    }

    /**
     * Constructor. This constructor does not pass the Excel file path to be written, only can call
     * {@link #flush(OutputStream)} method to write to the stream. If writing to a file, you need to call
     * {@link #flush(File, boolean)} to write to the file.
     *
     * @param isXlsx    whether it is xlsx format.
     * @param sheetName sheet name, as the first sheet name and write to this sheet, e.g. sheet1.
     */
    public ExcelWriter(final boolean isXlsx, final String sheetName) {
        this(WorkbookKit.createBook(isXlsx), sheetName);
    }

    /**
     * Constructor.
     *
     * @param templateFilePath template file, if it does not exist, it defaults to the target output file.
     * @param sheetName        sheet name, as the first sheet name and write to this sheet, e.g. sheet1.
     */
    public ExcelWriter(final String templateFilePath, final String sheetName) {
        this(FileKit.file(templateFilePath), sheetName);
    }

    /**
     * Constructor, writes to the first sheet by default, the first sheet is named sheet1.
     *
     * @param templateFile template file, if it does not exist, it defaults to the target output file.
     */
    public ExcelWriter(final File templateFile) {
        this(templateFile, null);
    }

    /**
     * Constructor.
     *
     * @param templateFile template file, if it does not exist, it defaults to the target output file.
     * @param sheetName    sheet name, as the first sheet name and write to this sheet, e.g. sheet1.
     */
    public ExcelWriter(final File templateFile, final String sheetName) {
        this(WorkbookKit.createBookForWriter(templateFile), sheetName);

        if (!FileKit.exists(templateFile)) {
            this.targetFile = templateFile;
        } else {
            // If the file already exists, it will be loaded as a template, and cannot be written to the template file
            // at this time
            this.sheetTemplateWriter = new SheetTemplateWriter(this.sheet, this.config);
        }
    }

    /**
     * Constructor. This constructor does not pass the Excel file path to be written, only can call
     * {@link #flush(OutputStream)} method to write to the stream. If writing to a file, you also need to call
     * {@link #setTargetFile(File)} method to customize the file to be written, then call {@link #flush()} method to
     * write to the file.
     *
     * @param templateWorkbook template {@link Workbook}.
     * @param sheetName        sheet name, as the first sheet name and write to this sheet, e.g. sheet1.
     */
    public ExcelWriter(final Workbook templateWorkbook, final String sheetName) {
        this(SheetKit.getOrCreateSheet(templateWorkbook, sheetName));
    }

    /**
     * Constructor. This constructor does not pass the Excel file path to be written, only can call
     * {@link #flush(OutputStream)} method to write to the stream. If writing to a file, you also need to call
     * {@link #setTargetFile(File)} method to customize the file to be written, then call {@link #flush()} method to
     * write to the file.
     *
     * @param sheet {@link Sheet}.
     */
    public ExcelWriter(final Sheet sheet) {
        super(new ExcelWriteConfig(), sheet);
        this.styleSet = new DefaultStyleSet(workbook);
    }

    /**
     * Add dropdown list.
     *
     * @param sheet      {@link Sheet}.
     * @param regions    {@link CellRangeAddressList} specifies the cell range occupied by the dropdown list.
     * @param selectList dropdown list content.
     */
    public static void addSelect(final Sheet sheet, final CellRangeAddressList regions, final String... selectList) {
        final DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        final DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(selectList);

        // Set dropdown box data
        final DataValidation dataValidation = validationHelper.createValidation(constraint, regions);

        // Handle Excel compatibility issues
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }

        sheet.addValidationData(dataValidation);
    }

    /**
     * {@inheritDoc}
     *
     * @param config the Excel write configuration
     * @return this writer instance for chaining
     */
    @Override
    public ExcelWriter setConfig(final ExcelWriteConfig config) {
        return super.setConfig(config);
    }

    /**
     * Reset Writer, including:
     *
     * <pre>
     * 1. Reset current row cursor to zero
     * 2. Clear header cache
     * </pre>
     *
     * @return this.
     */
    public ExcelWriter reset() {
        this.sheetDataWriter = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (null != this.targetFile) {
            flush();
        }
        closeWithoutFlush();
    }

    /**
     * Close workbook but do not flush.
     */
    protected void closeWithoutFlush() {
        super.close();
        this.reset();

        // Clear style
        this.styleSet = null;
    }

    /**
     * {@inheritDoc}
     *
     * @param sheetIndex the zero-based sheet index
     * @return this writer instance for chaining
     */
    @Override
    public ExcelWriter setSheet(final int sheetIndex) {
        super.setSheet(sheetIndex);
        // Switching to a new sheet needs to reset the start row
        return reset();
    }

    /**
     * {@inheritDoc}
     *
     * @param sheetName the sheet name
     * @return this writer instance for chaining
     */
    @Override
    public ExcelWriter setSheet(final String sheetName) {
        super.setSheet(sheetName);
        // Switching to a new sheet needs to reset the start row
        return reset();
    }

    /**
     * Rename the current sheet.
     *
     * @param sheetName new sheet name.
     * @return this.
     */
    public ExcelWriter renameSheet(final String sheetName) {
        return renameSheet(this.workbook.getSheetIndex(this.sheet), sheetName);
    }

    /**
     * Rename sheet.
     *
     * @param sheet     sheet number, 0 means the first sheet.
     * @param sheetName new sheet name.
     * @return this.
     */
    public ExcelWriter renameSheet(final int sheet, final String sheetName) {
        this.workbook.setSheetName(sheet, sheetName);
        return this;
    }

    /**
     * Set all columns to auto width, without considering merged cells. This method must be called after the specified
     * column data is completely written. The number of columns is calculated by the first row.
     *
     * @param useMergedCells whether to apply to merged cells.
     * @param widthRatio     column width multiplier. If all content is English, it can be set to 1. If there is
     *                       Chinese, it is recommended to set it between 1.6-2.0.
     * @return this.
     */
    public ExcelWriter autoSizeColumnAll(final boolean useMergedCells, final float widthRatio) {
        final int columnCount = this.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            autoSizeColumn(i, useMergedCells, widthRatio);
        }
        return this;
    }

    /**
     * Set a column to auto width. Note that in the case of Chinese, you need to adjust the width expansion ratio
     * according to your needs. This method must be called after the specified column data is completely written.
     *
     * @param columnIndex    which column, counting from 0.
     * @param useMergedCells whether to apply to merged cells.
     * @param widthRatio     column width multiplier. If all content is English, it can be set to 1. If there is
     *                       Chinese, it is recommended to set it between 1.6-2.0.
     * @return this.
     */
    public ExcelWriter autoSizeColumn(final int columnIndex, final boolean useMergedCells, final float widthRatio) {
        if (widthRatio > 0) {
            sheet.setColumnWidth(columnIndex, (int) (sheet.getColumnWidth(columnIndex) * widthRatio));
        } else {
            sheet.autoSizeColumn(columnIndex, useMergedCells);
        }
        return this;
    }

    /**
     * Disable default style.
     *
     * @return this.
     * @see #setStyleSet(StyleSet)
     */
    public ExcelWriter disableDefaultStyle() {
        return setStyleSet(null);
    }

    /**
     * Get the style set. The style set can be customized including:
     *
     * <pre>
     * 1. Header style
     * 2. General cell style
     * 3. Default number style
     * 4. Default date style
     * </pre>
     *
     * @return style set.
     */
    public StyleSet getStyleSet() {
        return this.styleSet;
    }

    /**
     * Set the style set. If you don't use styles, pass in {@code null}.
     *
     * @param styleSet style set, {@code null} means no style.
     * @return this.
     */
    public ExcelWriter setStyleSet(final StyleSet styleSet) {
        this.styleSet = styleSet;
        if (null != this.sheetDataWriter) {
            this.sheetDataWriter.setStyleSet(styleSet);
        }
        return this;
    }

    /**
     * Get the current row.
     *
     * @return current row.
     */
    public int getCurrentRow() {
        return null == this.sheetDataWriter ? 0 : this.sheetDataWriter.getCurrentRow();
    }

    /**
     * Set the current row.
     *
     * @param rowIndex row number.
     * @return this.
     */
    public ExcelWriter setCurrentRow(final int rowIndex) {
        getSheetDataWriter().setCurrentRow(rowIndex);
        return this;
    }

    /**
     * Position to the end of the last row, used to append data.
     *
     * @return this.
     */
    public ExcelWriter setCurrentRowToEnd() {
        return setCurrentRow(getRowCount());
    }

    /**
     * Skip the current row.
     *
     * @return this.
     */
    public ExcelWriter passCurrentRow() {
        getSheetDataWriter().passAndGet();
        return this;
    }

    /**
     * Skip the specified number of rows.
     *
     * @param rowNum the number of rows to skip.
     * @return this.
     */
    public ExcelWriter passRows(final int rowNum) {
        getSheetDataWriter().passRowsAndGet(rowNum);
        return this;
    }

    /**
     * Reset the current row to 0.
     *
     * @return this.
     */
    public ExcelWriter resetRow() {
        getSheetDataWriter().resetRow();
        return this;
    }

    /**
     * Set the target file to be written to. Note that this file cannot exist. If it exists, it will be overwritten when
     * {@link #flush()} is called.
     *
     * @param targetFile target file.
     * @return this.
     */
    public ExcelWriter setTargetFile(final File targetFile) {
        this.targetFile = targetFile;
        return this;
    }

    /**
     * Set freeze pane. The previously frozen window will be overwritten. If rowSplit is 0, it means unfreeze.
     *
     * @param rowSplit the number of rows to freeze, 2 means the first two rows.
     * @return this.
     */
    public ExcelWriter setFreezePane(final int rowSplit) {
        return setFreezePane(0, rowSplit);
    }

    /**
     * Set freeze pane. The previously frozen window will be overwritten. If both colSplit and rowSplit are 0, it means
     * unfreeze.
     *
     * @param colSplit the number of columns to freeze, 2 means the first two columns.
     * @param rowSplit the number of rows to freeze, 2 means the first two rows.
     * @return this.
     */
    public ExcelWriter setFreezePane(final int colSplit, final int rowSplit) {
        getSheet().createFreezePane(colSplit, rowSplit);
        return this;
    }

    /**
     * Set column width (unit is the width of one character. For example, if width is 10, it means the width of 10
     * characters).
     *
     * @param columnIndex column number (counting from 0, -1 means the default width of all columns).
     * @param width       width (unit 1~255 character widths).
     * @return this.
     */
    public ExcelWriter setColumnWidth(final int columnIndex, final int width) {
        if (columnIndex < 0) {
            this.sheet.setDefaultColumnWidth(width);
        } else {
            this.sheet.setColumnWidth(columnIndex, width * 256);
        }
        return this;
    }

    /**
     * Set the default row height, value is the height of one point.
     *
     * @param height height.
     * @return this.
     */
    public ExcelWriter setDefaultRowHeight(final int height) {
        return setRowHeight(-1, height);
    }

    /**
     * Set row height, value is the height of one point.
     *
     * @param rowNum row number (counting from 0, -1 means the default height of all rows).
     * @param height height.
     * @return this.
     */
    public ExcelWriter setRowHeight(final int rowNum, final int height) {
        if (rowNum < 0) {
            this.sheet.setDefaultRowHeightInPoints(height);
        } else {
            final Row row = this.sheet.getRow(rowNum);
            if (null != row) {
                row.setHeightInPoints(height);
            }
        }
        return this;
    }

    /**
     * Set Excel header or footer.
     *
     * @param text     footer text.
     * @param align    alignment enum {@link EnumValue.Align}.
     * @param isFooter whether it is footer, false means header, true means footer.
     * @return this.
     */
    public ExcelWriter setHeaderOrFooter(final String text, final EnumValue.Align align, final boolean isFooter) {
        final HeaderFooter headerFooter = isFooter ? this.sheet.getFooter() : this.sheet.getHeader();
        switch (align) {
            case LEFT:
                headerFooter.setLeft(text);
                break;

            case RIGHT:
                headerFooter.setRight(text);
                break;

            case CENTER:
                headerFooter.setCenter(text);
                break;

            default:
                break;
        }
        return this;
    }

    /**
     * Set ignored errors, i.e. green warning markers in Excel, only supports XSSFSheet and SXSSFSheet. See:
     * https://stackoverflow.com/questions/23488221/how-to-remove-warning-in-excel-using-apache-poi-in-java
     *
     * @param cellRangeAddress  specifies the cell range.
     * @param ignoredErrorTypes list of ignored error types.
     * @return this.
     * @throws UnsupportedOperationException if sheet is not XSSFSheet.
     */
    public ExcelWriter addIgnoredErrors(
            final CellRangeAddress cellRangeAddress,
            final IgnoredErrorType... ignoredErrorTypes) throws UnsupportedOperationException {
        SheetKit.addIgnoredErrors(this.sheet, cellRangeAddress, ignoredErrorTypes);
        return this;
    }

    /**
     * Add dropdown list.
     *
     * @param x          x coordinate, column number, starting from 0.
     * @param y          y coordinate, row number, starting from 0.
     * @param selectList dropdown list.
     * @return this.
     */
    public ExcelWriter addSelect(final int x, final int y, final String... selectList) {
        return addSelect(new CellRangeAddressList(y, y, x, x), selectList);
    }

    /**
     * Add dropdown list.
     *
     * @param regions    {@link CellRangeAddressList} specifies the cell range occupied by the dropdown list.
     * @param selectList dropdown list content.
     * @return this.
     */
    public ExcelWriter addSelect(final CellRangeAddressList regions, final String... selectList) {
        addSelect(this.sheet, regions, selectList);
        return this;
    }

    /**
     * Add cell control, such as dropdown list, date validation, number range validation, etc.
     *
     * @param dataValidation {@link DataValidation}.
     * @return this.
     */
    public ExcelWriter addValidationData(final DataValidation dataValidation) {
        this.sheet.addValidationData(dataValidation);
        return this;
    }

    /**
     * Merge cells in the current row.
     *
     * @param lastColumn the last column number to merge to.
     * @return this.
     */
    public ExcelWriter merge(final int lastColumn) {
        return merge(lastColumn, null);
    }

    /**
     * Merge cells in the current row and write the object to the cell. If the content written to the cell is not null,
     * the row number is automatically increased by 1, otherwise the current row number remains unchanged.
     *
     * @param lastColumn the last column number to merge to.
     * @param content    the content of the merged cell.
     * @return this.
     */
    public ExcelWriter merge(final int lastColumn, final Object content) {
        return merge(lastColumn, content, true);
    }

    /**
     * Merge cells in a row and write the object to the cell. If the content written to the cell is not null, the row
     * number is automatically increased by 1, otherwise the current row number remains unchanged.
     *
     * @param lastColumn       the last column number to merge to.
     * @param content          the content of the merged cell.
     * @param isSetHeaderStyle whether to set the default header style for the merged cell, only extract border style.
     * @return this.
     */
    public ExcelWriter merge(final int lastColumn, final Object content, final boolean isSetHeaderStyle) {
        checkClosed();

        final int rowIndex = getCurrentRow();
        merge(CellKit.ofSingleRow(rowIndex, lastColumn), content, isSetHeaderStyle);

        // Skip to the next line after setting the content
        if (null != content) {
            this.passCurrentRow();
        }
        return this;
    }

    /**
     * Merge cells in a row and write the object to the cell.
     *
     * @param cellRangeAddress merged cell range, defines the starting and ending rows and columns.
     * @param content          the content of the merged cell.
     * @param isSetHeaderStyle whether to set the default header style for the merged cell, only extract border style.
     * @return this.
     */
    public ExcelWriter merge(
            final CellRangeAddress cellRangeAddress,
            final Object content,
            final boolean isSetHeaderStyle) {
        checkClosed();

        CellStyle style = null;
        if (null != this.styleSet) {
            style = styleSet.getStyleFor(
                    new CellReference(cellRangeAddress.getFirstRow(), cellRangeAddress.getFirstColumn()),
                    content,
                    isSetHeaderStyle);
        }

        return merge(cellRangeAddress, content, style);
    }

    /**
     * Merge cells and write the object to the cell, using the specified style. If the specified style is null, no style
     * is used.
     *
     * @param cellRangeAddress merged cell range, defines the starting and ending rows and columns.
     * @param content          the content of the merged cell.
     * @param cellStyle        the style used for the merged cell, can be null.
     * @return this.
     */
    public ExcelWriter merge(final CellRangeAddress cellRangeAddress, final Object content, final CellStyle cellStyle) {
        checkClosed();

        CellKit.mergingCells(this.getSheet(), cellRangeAddress, cellStyle);

        // Set content
        if (null != content) {
            final Cell cell = getOrCreateCell(cellRangeAddress.getFirstColumn(), cellRangeAddress.getFirstRow());
            CellKit.setCellValue(cell, content, cellStyle, this.config.getCellEditor());
        }
        return this;
    }

    /**
     * Write data. This method only writes data to the Sheet in the Workbook, not to the file. The starting row for
     * writing is the current row number, which can be obtained by calling {@link #getCurrentRow()} method. The current
     * row number automatically increases according to the number of rows written. By default, when the current row
     * number is 0, the header (if it is a Map or Bean) is written, otherwise the header is not written.
     *
     * <p>
     * The types of elements supported in data are:
     *
     * <pre>
     * 1. Iterable, i.e. the element is a collection, the element is treated as a row, data represents multiple rows
     * 2. Map, i.e. the element is a Map, the keys of the first Map are used as the first row, the remaining rows are the values of the Map, data represents multiple rows
     * 3. Bean, i.e. the element is a Bean, the field name list of the first Bean will be used as the first row, the remaining rows are the field value list of the Bean, data represents multiple rows
     * 4. Other types, output as basic types (e.g. strings)
     * </pre>
     *
     * @param data data.
     * @return this.
     */
    public ExcelWriter write(final Iterable<?> data) {
        return write(data, 0 == getCurrentRow());
    }

    /**
     * Write data. This method only writes data to the Sheet in the Workbook, not to the file. The starting row for
     * writing is the current row number, which can be obtained by calling {@link #getCurrentRow()} method. The current
     * row number automatically increases according to the number of rows written.
     *
     * <p>
     * The types of elements supported in data are:
     *
     * <pre>
     * 1. Iterable, i.e. the element is a collection, the element is treated as a row, data represents multiple rows
     * 2. Map, i.e. the element is a Map, the keys of the first Map are used as the first row, the remaining rows are the values of the Map, data represents multiple rows
     * 3. Bean, i.e. the element is a Bean, the field name list of the first Bean will be used as the first row, the remaining rows are the field value list of the Bean, data represents multiple rows
     * 4. Other types, output as basic types (e.g. strings)
     * </pre>
     *
     * @param data             data.
     * @param isWriteKeyAsHead whether to force writing the header row (Map or Bean).
     * @return this.
     */
    public ExcelWriter write(final Iterable<?> data, final boolean isWriteKeyAsHead) {
        checkClosed();
        boolean isFirst = true;
        for (final Object object : data) {
            writeRow(object, isFirst && isWriteKeyAsHead);
            if (isFirst) {
                isFirst = false;
            }
        }
        return this;
    }

    /**
     * Write data. This method only writes data to the Sheet in the Workbook, not to the file. The starting row for
     * writing is the current row number, which can be obtained by calling {@link #getCurrentRow()} method. The current
     * row number automatically increases according to the number of rows written. The types of elements supported in
     * data are:
     *
     * <p>
     * 1. Map, i.e. the element is a Map, the keys of the first Map are used as the first row, the remaining rows are
     * the values of the Map, data represents multiple rows 2. Bean, i.e. the element is a Bean, the field name list of
     * the first Bean will be used as the first row, the remaining rows are the field value list of the Bean, data
     * represents multiple rows
     * </p>
     *
     * @param data       data.
     * @param comparator comparator, used for sorting field names.
     * @return this.
     */
    public ExcelWriter write(final Iterable<?> data, final Comparator<String> comparator) {
        checkClosed();
        boolean isFirstRow = true;
        Map<?, ?> map;
        for (final Object object : data) {
            if (isFirstRow) {
                // Only sort the first row (header), subsequent data are filled according to the position of the first
                // row key, no need to reorder
                if (object instanceof Map) {
                    map = new TreeMap<>(comparator);
                    map.putAll((Map) object);
                } else {
                    map = BeanKit.beanToMap(object, new TreeMap<>(comparator), false, false);
                }
            } else {
                if (object instanceof Map) {
                    map = (Map) object;
                } else {
                    map = BeanKit.beanToMap(object, new HashMap<>(), false, false);
                }
            }
            writeRow(map, isFirstRow);
            if (isFirstRow) {
                isFirstRow = false;
            }
        }
        return this;
    }

    /**
     * Write grouped header row.
     *
     * @param rowGroup grouped row.
     * @return this.
     */
    public ExcelWriter writeHeader(final RowGroup rowGroup) {
        return writeHeader(0, getCurrentRow(), 1, rowGroup);
    }

    /**
     * Write grouped header row.
     *
     * @param x        starting column, index starts from 0.
     * @param y        starting row, index starts from 0.
     * @param rowCount the number of rows occupied by the current group, this value is the number of rows occupied by
     *                 the header + the maximum number of rows occupied by subgroups, uncertain pass 1.
     * @param rowGroup grouped row.
     * @return this.
     */
    public ExcelWriter writeHeader(final int x, final int y, final int rowCount, final RowGroup rowGroup) {
        checkClosed();
        this.getSheetDataWriter().writeHeader(x, y, rowCount, rowGroup);
        return this;
    }

    /**
     * Write data. This method only writes data to the Sheet in the Workbook, not to the file. Add an image to the
     * current sheet / default image type is png / default starting and ending coordinates are both 0.
     *
     * @param imgFile image file.
     * @param col1    specifies the starting column, index starts from 0.
     * @param row1    specifies the starting row, index starts from 0.
     * @param col2    specifies the ending column, index starts from 0.
     * @param row2    specifies the ending row, index starts from 0.
     * @return this.
     */
    public ExcelWriter writeImg(final File imgFile, final int col1, final int row1, final int col2, final int row2) {
        return writeImg(imgFile, new SimpleAnchor(col1, row1, col2, row2));
    }

    /**
     * Write data. This method only writes data to the Sheet in the Workbook, not to the file. Add an image to the
     * current sheet / default image type is png.
     *
     * @param imgFile      image file.
     * @param clientAnchor position and size information of the image.
     * @return this.
     */
    public ExcelWriter writeImg(final File imgFile, final SimpleAnchor clientAnchor) {
        return writeImg(imgFile, ExcelPictureType.getType(imgFile), clientAnchor);
    }

    /**
     * Write data. This method only writes data to the Sheet in the Workbook, not to the file. Add an image to the
     * current sheet.
     *
     * @param imgFile      image file.
     * @param imgType      image type, corresponding to the image type 2-7 variables in the Workbook class in poi.
     * @param clientAnchor position and size information of the image.
     * @return this.
     */
    public ExcelWriter writeImg(final File imgFile, final ExcelPictureType imgType, final SimpleAnchor clientAnchor) {
        return writeImg(FileKit.readBytes(imgFile), imgType, clientAnchor);
    }

    /**
     * Write data. This method only writes data to the Sheet in the Workbook, not to the file. Add an image to the
     * current sheet.
     *
     * @param pictureData  data bytes.
     * @param imgType      image type, corresponding to the image type 2-7 variables in the Workbook class in poi.
     * @param clientAnchor position and size information of the image.
     * @return this.
     */
    public ExcelWriter writeImg(
            final byte[] pictureData,
            final ExcelPictureType imgType,
            final SimpleAnchor clientAnchor) {
        ExcelDrawing.drawingPicture(this.sheet, pictureData, imgType, clientAnchor);
        return this;
    }

    /**
     * Draw line shape.
     *
     * @param clientAnchor drawing area information.
     * @return this.
     */
    public ExcelWriter writeLineShape(final SimpleAnchor clientAnchor) {
        return writeSimpleShape(clientAnchor, ShapeConfig.of());
    }

    /**
     * Draw line shape.
     *
     * @param clientAnchor drawing area information.
     * @param lineStyle    line style.
     * @param lineWidth    line width.
     * @param lineColor    line color.
     * @return this.
     */
    public ExcelWriter writeLineShape(
            final SimpleAnchor clientAnchor,
            final LineStyle lineStyle,
            final int lineWidth,
            final Color lineColor) {
        return writeSimpleShape(
                clientAnchor,
                ShapeConfig.of().setLineStyle(lineStyle).setLineWidth(lineWidth).setLineColor(lineColor));
    }

    /**
     * Draw simple shape.
     *
     * @param clientAnchor drawing area information.
     * @param shapeConfig  shape configuration, including shape type, line style, line width, line color, fill color,
     *                     etc.
     * @return this.
     */
    public ExcelWriter writeSimpleShape(final SimpleAnchor clientAnchor, final ShapeConfig shapeConfig) {
        ExcelDrawing.drawingSimpleShape(this.sheet, clientAnchor, shapeConfig);
        return this;
    }

    /**
     * Write a row of header data. This method only writes data to the Sheet in the Workbook, not to the file. The
     * starting row for writing is the current row number, which can be obtained by calling {@link #getCurrentRow()}
     * method. The current row number automatically increases by 1 according to the number of rows written.
     *
     * @param rowData data of a row.
     * @return this.
     */
    public ExcelWriter writeHeaderRow(final Iterable<?> rowData) {
        checkClosed();
        getSheetDataWriter().writeHeaderRow(rowData);
        return this;
    }

    /**
     * Fill non-list template variables (one-time variables).
     *
     * @param rowMap row data.
     * @return this.
     */
    public ExcelWriter fillOnce(final Map<?, ?> rowMap) {
        checkClosed();
        Assert.notNull(this.sheetTemplateWriter, () -> new InternalException("No template for this writer!"));
        this.sheetTemplateWriter.fillOnce(rowMap);
        return this;
    }

    /**
     * Write a row. According to the different data types of rowBean, the writing situation is as follows:
     *
     * <pre>
     * 1. If it is Iterable, write a row directly
     * 2. If it is Map, when isWriteKeyAsHead is true, write two rows, the keys of the Map are used as one row, the values are used as the second row, otherwise only write one row of values
     * 3. If it is Bean, convert to Map and write, when isWriteKeyAsHead is true, write two rows, the keys of the Map are used as one row, the values are used as the second row, otherwise only write one row of values
     * </pre>
     *
     * @param rowBean          the Bean to be written, can be Map, Bean or Iterable.
     * @param isWriteKeyAsHead when true, write two rows, the keys of the Map are used as one row, the values are used
     *                         as the second row, otherwise only write one row of values.
     * @return this.
     */
    public ExcelWriter writeRow(final Object rowBean, final boolean isWriteKeyAsHead) {
        checkClosed();

        // Template writing
        if (null != this.sheetTemplateWriter) {
            this.sheetTemplateWriter.fillRow(rowBean);
            return this;
        }

        getSheetDataWriter().writeRow(rowBean, isWriteKeyAsHead);
        return this;
    }

    /**
     * Write a row of data. This method only writes data to the Sheet in the Workbook, not to the file. The starting row
     * for writing is the current row number, which can be obtained by calling {@link #getCurrentRow()} method. The
     * current row number automatically increases by 1 according to the number of rows written.
     *
     * @param rowData data of a row.
     * @return this.
     */
    public ExcelWriter writeRow(final Iterable<?> rowData) {
        checkClosed();
        getSheetDataWriter().writeRow(rowData);
        return this;
    }

    /**
     * Write data by column starting from column 1 (index starts from 0). This method only writes data to the Sheet in
     * the Workbook, not to the file. The starting row for writing is the current row number, which can be obtained by
     * calling {@link #getCurrentRow()} method. The current row number automatically increases by 1 according to the
     * number of rows written.
     *
     * @param colMap           data of a column.
     * @param isWriteKeyAsHead whether to use the Key of the Map as the header output. If true, the first row is the
     *                         header, followed by values.
     * @return this.
     */
    public ExcelWriter writeCol(final Map<?, ? extends Iterable<?>> colMap, final boolean isWriteKeyAsHead) {
        return writeCol(colMap, 0, isWriteKeyAsHead);
    }

    /**
     * Write data by column starting from the specified column (index starts from 0). This method only writes data to
     * the Sheet in the Workbook, not to the file. The starting row for writing is the current row number, which can be
     * obtained by calling {@link #getCurrentRow()} method. The current row number automatically increases by 1
     * according to the number of rows written.
     *
     * @param colMap           data of a column.
     * @param startColIndex    starting column number, starting from 0.
     * @param isWriteKeyAsHead whether to use the Key of the Map as the header output. If true, the first row is the
     *                         header, followed by values.
     * @return this.
     */
    public ExcelWriter writeCol(
            final Map<?, ? extends Iterable<?>> colMap,
            int startColIndex,
            final boolean isWriteKeyAsHead) {
        for (final Object k : colMap.keySet()) {
            final Iterable<?> v = colMap.get(k);
            if (v != null) {
                writeCol(isWriteKeyAsHead ? k : null, startColIndex, v, startColIndex != colMap.size() - 1);
                startColIndex++;
            }
        }
        return this;
    }

    /**
     * Write data for the first column. This method only writes data to the Sheet in the Workbook, not to the file. The
     * starting row for writing is the current row number, which can be obtained by calling {@link #getCurrentRow()}
     * method. The current row number automatically increases by 1 according to the number of rows written.
     *
     * @param headerVal       header name. If null, it will not be written.
     * @param colData         column data to be written.
     * @param isResetRowIndex if true, the Row index will be reset to the position before writing after writing is
     *                        completed. If false, the Row index will be below the written data after writing is
     *                        completed.
     * @return this.
     */
    public ExcelWriter writeCol(final Object headerVal, final Iterable<?> colData, final boolean isResetRowIndex) {
        return writeCol(headerVal, 0, colData, isResetRowIndex);
    }

    /**
     * Write data for the specified column. This method only writes data to the Sheet in the Workbook, not to the file.
     * The starting row for writing is the current row number, which can be obtained by calling {@link #getCurrentRow()}
     * method. The current row number automatically increases by 1 according to the number of rows written.
     *
     * @param headerVal       header name. If null, it will not be written.
     * @param colIndex        column index.
     * @param colData         column data to be written.
     * @param isResetRowIndex if true, the Row index will be reset to the position before writing after writing is
     *                        completed. If false, the Row index will be below the written data after writing is
     *                        completed.
     * @return this.
     */
    public ExcelWriter writeCol(
            final Object headerVal,
            final int colIndex,
            final Iterable<?> colData,
            final boolean isResetRowIndex) {
        checkClosed();
        int currentRowIndex = getCurrentRow();
        if (null != headerVal) {
            writeCellValue(colIndex, currentRowIndex, headerVal, true);
            currentRowIndex++;
        }
        for (final Object colDatum : colData) {
            writeCellValue(colIndex, currentRowIndex, colDatum);
            currentRowIndex++;
        }
        if (!isResetRowIndex) {
            setCurrentRow(currentRowIndex);
        }
        return this;
    }

    /**
     * Assign value to the specified cell, using the default cell style.
     *
     * @param locationRef cell address identifier, e.g. A11, B5.
     * @param value       value.
     * @return this.
     */
    public ExcelWriter writeCellValue(final String locationRef, final Object value) {
        final CellReference cellReference = new CellReference(locationRef);
        return writeCellValue(cellReference.getCol(), cellReference.getRow(), value);
    }

    /**
     * Assign value to the specified cell, using the default cell style.
     *
     * @param x     X coordinate, counting from 0, i.e. column number.
     * @param y     Y coordinate, counting from 0, i.e. row number.
     * @param value value.
     * @return this.
     */
    public ExcelWriter writeCellValue(final int x, final int y, final Object value) {
        return writeCellValue(x, y, value, false);
    }

    /**
     * Assign value to the specified cell, using the default cell style.
     *
     * @param x        X coordinate, counting from 0, i.e. column number.
     * @param y        Y coordinate, counting from 0, i.e. row number.
     * @param isHeader whether it is Header.
     * @param value    value.
     * @return this.
     */
    public ExcelWriter writeCellValue(final int x, final int y, final Object value, final boolean isHeader) {
        final Cell cell = getOrCreateCell(x, y);
        CellKit.setCellValue(cell, value, this.styleSet, isHeader, this.config.getCellEditor());
        return this;
    }

    /**
     * Set the style of a cell. This method is used for situations where multiple cells share a style. You can call
     * {@link #getOrCreateCellStyle(int, int)} method to create or obtain a style object. Note that sharing a style will
     * share the same {@link CellStyle}. When one cell style changes, all change.
     *
     * @param style       cell style.
     * @param locationRef cell address identifier, e.g. A11, B5.
     * @return this.
     */
    public ExcelWriter setStyle(final CellStyle style, final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return setStyle(style, cellReference.getCol(), cellReference.getRow());
    }

    /**
     * Set the style of a cell. This method is used for situations where multiple cells share a style. You can call
     * {@link #getOrCreateCellStyle(int, int)} method to create or obtain a style object. Note that sharing a style will
     * share the same {@link CellStyle}. When one cell style changes, all change.
     *
     * @param style cell style.
     * @param x     X coordinate, counting from 0, i.e. column number.
     * @param y     Y coordinate, counting from 0, i.e. row number.
     * @return this.
     */
    public ExcelWriter setStyle(final CellStyle style, final int x, final int y) {
        final Cell cell = getOrCreateCell(x, y);
        cell.setCellStyle(style);
        return this;
    }

    /**
     * Set row style.
     *
     * @param y     Y coordinate, counting from 0, i.e. row number.
     * @param style style.
     * @return this.
     * @see Row#setRowStyle(CellStyle)
     */
    public ExcelWriter setRowStyle(final int y, final CellStyle style) {
        getOrCreateRow(y).setRowStyle(style);
        return this;
    }

    /**
     * Add custom style to the entire data row. Only set for data cells. Call after write
     * {@link ExcelWriter#setRowStyle(int, org.apache.poi.ss.usermodel.CellStyle)}. The style added by this method will
     * make the entire row have styles even if there are no data cells. Especially when adding background colors, it is
     * very unsightly. And the styles of cells with data will be overwritten by the styles in StyleSet.
     *
     * @param y     row coordinate.
     * @param style custom style.
     * @return this.
     */
    public ExcelWriter setRowStyleIfHasData(final int y, final CellStyle style) {
        if (y < 0) {
            throw new IllegalArgumentException("Invalid row number (" + y + ")");
        }
        final int columnCount = this.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            this.setStyle(style, i, y);
        }
        return this;
    }

    /**
     * Set the default style for a column.
     *
     * @param x     column number, starting from 0.
     * @param style style.
     * @return this.
     */
    public ExcelWriter setColumnStyle(final int x, final CellStyle style) {
        this.sheet.setDefaultColumnStyle(x, style);
        return this;
    }

    /**
     * Set the style for the entire column. Only set for data cells. Call after write
     * {@link ExcelWriter#setColumnStyle(int, org.apache.poi.ss.usermodel.CellStyle)}. The style added by this method
     * will make the entire column have styles even if there are no data cells. Especially when adding background
     * colors, it is very unsightly. And the styles of cells with data will be overwritten by the styles in StyleSet.
     *
     * @param x     column index.
     * @param y     starting row.
     * @param style style.
     * @return this.
     */
    public ExcelWriter setColumnStyleIfHasData(final int x, final int y, final CellStyle style) {
        if (x < 0) {
            throw new IllegalArgumentException("Invalid column number (" + x + ")");
        }
        if (y < 0) {
            throw new IllegalArgumentException("Invalid row number (" + y + ")");
        }
        final int rowCount = this.getRowCount();
        for (int i = y; i < rowCount; i++) {
            this.setStyle(style, x, i);
        }
        return this;
    }

    /**
     * Flush Excel Workbook to a predefined file. If the user has not customized the output file, a
     * {@link NullPointerException} will be thrown. The predefined file can be predefined by
     * {@link #setTargetFile(File)} method, or defined by constructor.
     *
     * @return this.
     * @throws InternalException IO exception.
     */
    public ExcelWriter flush() throws InternalException {
        return flush(false);
    }

    /**
     * Flush Excel Workbook to a predefined file. If the user has not customized the output file, a
     * {@link NullPointerException} will be thrown. The predefined file can be predefined by
     * {@link #setTargetFile(File)} method, or defined by constructor.
     *
     * @param override whether to overwrite existing files.
     * @return this.
     * @throws InternalException IO exception.
     */
    public ExcelWriter flush(final boolean override) throws InternalException {
        Assert.notNull(this.targetFile, "[targetFile] is null, and you must call setTargetFile(File) first.");
        return flush(this.targetFile, override);
    }

    /**
     * Flush Excel Workbook to a file. If the user has not customized the output file, a {@link NullPointerException}
     * will be thrown.
     *
     * @param targetFile file to write to.
     * @param override   whether to overwrite existing files.
     * @return this.
     * @throws InternalException IO exception.
     */
    public ExcelWriter flush(final File targetFile, final boolean override) throws InternalException {
        Assert.notNull(targetFile, "targetFile is null!");
        if (FileKit.exists(targetFile) && !override) {
            throw new InternalException("File to write exist: " + targetFile);
        }
        return flush(FileKit.getOutputStream(targetFile), true);
    }

    /**
     * Flush Excel Workbook to output stream.
     *
     * @param out output stream.
     * @return this.
     * @throws InternalException IO exception.
     */
    public ExcelWriter flush(final OutputStream out) throws InternalException {
        return flush(out, false);
    }

    /**
     * Flush Excel Workbook to output stream.
     *
     * @param out        output stream.
     * @param isCloseOut whether to close the output stream.
     * @return this.
     * @throws InternalException IO exception.
     */
    public ExcelWriter flush(final OutputStream out, final boolean isCloseOut) throws InternalException {
        checkClosed();

        try {
            this.workbook.write(out);
            out.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (isCloseOut) {
                IoKit.closeQuietly(out);
            }
        }
        return this;
    }

    /**
     * Get SheetDataWriter, create if it doesn't exist.
     *
     * @return SheetDataWriter.
     */
    private SheetDataWriter getSheetDataWriter() {
        if (null == this.sheetDataWriter) {
            this.sheetDataWriter = new SheetDataWriter(this.sheet, this.config, this.styleSet);
        }
        return this.sheetDataWriter;
    }

}
