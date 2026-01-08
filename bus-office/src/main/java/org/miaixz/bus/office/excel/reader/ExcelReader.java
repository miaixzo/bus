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
package org.miaixz.bus.office.excel.reader;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.office.excel.ExcelBase;
import org.miaixz.bus.office.excel.ExcelExtractor;
import org.miaixz.bus.office.excel.writer.ExcelWriter;
import org.miaixz.bus.office.excel.xyz.CellKit;
import org.miaixz.bus.office.excel.xyz.ExcelKit;
import org.miaixz.bus.office.excel.xyz.RowKit;
import org.miaixz.bus.office.excel.xyz.WorkbookKit;

/**
 * Excel reader for reading Excel workbooks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelReader extends ExcelBase<ExcelReader, ExcelReadConfig> {

    /**
     * Constructs a new {@code ExcelReader}.
     *
     * @param excelFilePath The path to the Excel file, either absolute or relative to the ClassPath.
     * @param sheetIndex    The sheet index, 0 means the first sheet.
     */
    public ExcelReader(final String excelFilePath, final int sheetIndex) {
        this(FileKit.file(excelFilePath), sheetIndex);
    }

    /**
     * Constructs a new {@code ExcelReader}.
     *
     * @param excelFilePath The path to the Excel file, either absolute or relative to the ClassPath.
     * @param sheetName     The sheet name. The first sheet is typically named "sheet1".
     */
    public ExcelReader(final String excelFilePath, final String sheetName) {
        this(FileKit.file(excelFilePath), sheetName);
    }

    /**
     * Constructs a new {@code ExcelReader} in read-write mode.
     *
     * @param bookFile   The Excel file.
     * @param sheetIndex The sheet index, 0 means the first sheet.
     */
    public ExcelReader(final File bookFile, final int sheetIndex) {
        this(WorkbookKit.createBook(bookFile, true), sheetIndex);
        this.targetFile = bookFile;
    }

    /**
     * Constructs a new {@code ExcelReader} in read-write mode.
     *
     * @param bookFile  The Excel file.
     * @param sheetName The sheet name. The first sheet is typically named "sheet1".
     */
    public ExcelReader(final File bookFile, final String sheetName) {
        this(WorkbookKit.createBook(bookFile, true), sheetName);
        this.targetFile = bookFile;
    }

    /**
     * Constructs a new {@code ExcelReader} in read-only mode.
     *
     * @param bookStream The input stream of the Excel file.
     * @param sheetIndex The sheet index, 0 means the first sheet.
     */
    public ExcelReader(final InputStream bookStream, final int sheetIndex) {
        this(WorkbookKit.createBook(bookStream), sheetIndex);
    }

    /**
     * Constructs a new {@code ExcelReader} in read-only mode.
     *
     * @param bookStream The input stream of the Excel file.
     * @param sheetName  The sheet name. The first sheet is typically named "sheet1".
     */
    public ExcelReader(final InputStream bookStream, final String sheetName) {
        this(WorkbookKit.createBook(bookStream), sheetName);
    }

    /**
     * Constructs a new {@code ExcelReader}.
     *
     * @param book       The {@link Workbook} representing an Excel file.
     * @param sheetIndex The sheet index, 0 means the first sheet.
     */
    public ExcelReader(final Workbook book, final int sheetIndex) {
        this(getSheetOrCloseWorkbook(book, sheetIndex));
    }

    /**
     * Constructs a new {@code ExcelReader}.
     *
     * @param book      The {@link Workbook} representing an Excel file.
     * @param sheetName The sheet name. The first sheet is typically named "sheet1".
     */
    public ExcelReader(final Workbook book, final String sheetName) {
        this(getSheetOrCloseWorkbook(book, sheetName));
    }

    /**
     * Constructs a new {@code ExcelReader}.
     *
     * @param sheet The {@link Sheet} to read from.
     */
    public ExcelReader(final Sheet sheet) {
        super(new ExcelReadConfig(), sheet);
    }

    /**
     * Gets the {@link Sheet}. If the sheet does not exist, the {@link Workbook} is closed and an exception is thrown,
     * addressing the issue of the file remaining occupied when the sheet does not exist.
     *
     * @param workbook The {@link Workbook}, cannot be null.
     * @param name     The sheet name. If it does not exist, an exception is thrown.
     * @return The {@link Sheet}.
     * @throws IllegalArgumentException if the workbook is null or the sheet does not exist.
     */
    private static Sheet getSheetOrCloseWorkbook(final Workbook workbook, String name) throws IllegalArgumentException {
        Assert.notNull(workbook);
        if (null == name) {
            name = "sheet1";
        }
        final Sheet sheet = workbook.getSheet(name);
        if (null == sheet) {
            IoKit.closeQuietly(workbook);
            throw new IllegalArgumentException("Sheet [" + name + "] not exist!");
        }
        return sheet;
    }

    /**
     * Gets the {@link Sheet}. If the sheet does not exist, the {@link Workbook} is closed and an exception is thrown,
     * addressing the issue of the file remaining occupied when the sheet does not exist.
     *
     * @param workbook   The {@link Workbook}, cannot be null.
     * @param sheetIndex The sheet index.
     * @return The {@link Sheet}.
     * @throws IllegalArgumentException if the workbook is null or the sheet does not exist.
     */
    private static Sheet getSheetOrCloseWorkbook(final Workbook workbook, final int sheetIndex)
            throws IllegalArgumentException {
        Assert.notNull(workbook);
        final Sheet sheet;
        try {
            sheet = workbook.getSheetAt(sheetIndex);
        } catch (final IllegalArgumentException e) {
            IoKit.closeQuietly(workbook);
            throw e;
        }
        if (null == sheet) {
            IoKit.closeQuietly(workbook);
            throw new IllegalArgumentException("Sheet at [" + sheetIndex + "] not exist!");
        }
        return sheet;
    }

    /**
     * Reads all row data from the specified sheet in the workbook.
     *
     * @return A list of rows, where each row is represented by a List of objects.
     */
    public List<List<Object>> read() {
        return read(0);
    }

    /**
     * Reads the specified sheet in the workbook.
     *
     * @param startRowIndex The starting row index (inclusive, 0-based).
     * @return A list of rows, where each row is represented by a List of objects.
     */
    public List<List<Object>> read(final int startRowIndex) {
        return read(startRowIndex, Integer.MAX_VALUE);
    }

    /**
     * Reads the specified sheet in the workbook. The first row is treated as a header row and aliases are applied.
     *
     * @param startRowIndex The starting row index (inclusive, 0-based).
     * @param endRowIndex   The ending row index (inclusive, 0-based).
     * @return A list of rows, where each row is represented by a List of objects.
     */
    public List<List<Object>> read(final int startRowIndex, final int endRowIndex) {
        return read(startRowIndex, endRowIndex, false);
    }

    /**
     * Reads the specified sheet in the workbook.
     *
     * @param startRowIndex  The starting row index (inclusive, 0-based).
     * @param endRowIndex    The ending row index (inclusive, 0-based).
     * @param aliasFirstLine {@code true} if the first row should be treated as a header and aliases applied,
     *                       {@code false} otherwise.
     * @return A list of rows, where each row is represented by a List of objects.
     */
    public List<List<Object>> read(final int startRowIndex, final int endRowIndex, final boolean aliasFirstLine) {
        final ListSheetReader reader = new ListSheetReader(startRowIndex, endRowIndex, aliasFirstLine);
        reader.setExcelConfig(this.config);
        return read(reader);
    }

    /**
     * Reads a specific column from the specified sheet in the workbook.
     *
     * @param columnIndex   The column index (0-based).
     * @param startRowIndex The starting row index (inclusive, 0-based).
     * @return A list of column values.
     */
    public List<Object> readColumn(final int columnIndex, final int startRowIndex) {
        return readColumn(columnIndex, startRowIndex, Integer.MAX_VALUE);
    }

    /**
     * Reads a specific column from the specified sheet in the workbook.
     *
     * @param columnIndex   The column index (0-based).
     * @param startRowIndex The starting row index (inclusive, 0-based).
     * @param endRowIndex   The ending row index (inclusive, 0-based).
     * @return A list of column values.
     */
    public List<Object> readColumn(final int columnIndex, final int startRowIndex, final int endRowIndex) {
        final ColumnSheetReader reader = new ColumnSheetReader(columnIndex, startRowIndex, endRowIndex);
        reader.setExcelConfig(this.config);
        return read(reader);
    }

    /**
     * Reads the specified sheet in the workbook using a stream-like processing approach. When a cell is read, the
     * {@link BiConsumerX} is called. Users can implement this interface to flexibly process data for each cell.
     *
     * @param cellHandler The cell handler, used to process the read cell and its data.
     */
    public void read(final BiConsumerX<Cell, Object> cellHandler) {
        read(0, Integer.MAX_VALUE, cellHandler);
    }

    /**
     * Reads the specified sheet in the workbook using a stream-like processing approach. When a cell is read, the
     * {@link BiConsumerX} is called. Users can implement this interface to flexibly process data for each cell.
     *
     * @param startRowIndex The starting row index (inclusive, 0-based).
     * @param endRowIndex   The ending row index (inclusive, 0-based).
     * @param cellHandler   The cell handler, used to process the read cell and its data.
     */
    public void read(final int startRowIndex, final int endRowIndex, final BiConsumerX<Cell, Object> cellHandler) {
        checkClosed();

        final WalkSheetReader reader = new WalkSheetReader(startRowIndex, endRowIndex, cellHandler);
        reader.setExcelConfig(this.config);
        reader.read(sheet);
    }

    /**
     * Reads the Excel file into a list of maps, reading all rows. By default, the first row is treated as the header,
     * and data starts from the second row. Each map represents a row, with headers as keys and cell content as values.
     *
     * @return A list of maps.
     */
    public List<Map<Object, Object>> readAll() {
        return read(0, 1, Integer.MAX_VALUE);
    }

    /**
     * Reads the Excel file into a list of maps. Each map represents a row, with headers as keys and cell content as
     * values.
     *
     * @param headerRowIndex The row index where the header is located. If the header row is in the middle of the
     *                       content rows to be read, this row will be ignored as data.
     * @param startRowIndex  The starting row index (inclusive, 0-based).
     * @param endRowIndex    The ending row index (inclusive, 0-based).
     * @return A list of maps.
     */
    public List<Map<Object, Object>> read(final int headerRowIndex, final int startRowIndex, final int endRowIndex) {
        final MapSheetReader reader = new MapSheetReader(headerRowIndex, startRowIndex, endRowIndex);
        reader.setExcelConfig(this.config);
        return read(reader);
    }

    /**
     * Reads the Excel file into a list of Bean objects, reading all rows. By default, the first row is treated as the
     * header, and data starts from the second row.
     *
     * @param <T>      The type of the Bean.
     * @param beanType The type of the Bean corresponding to each row.
     * @return A list of Bean objects.
     */
    public <T> List<T> readAll(final Class<T> beanType) {
        return read(0, 1, Integer.MAX_VALUE, beanType);
    }

    /**
     * Reads the Excel file into a list of Bean objects.
     *
     * @param <T>            The type of the Bean.
     * @param headerRowIndex The row index where the header is located. If the header row is in the middle of the
     *                       content rows to be read, this row will be ignored as data.
     * @param startRowIndex  The starting row index (inclusive, 0-based).
     * @param beanType       The type of the Bean corresponding to each row.
     * @return A list of Bean objects.
     */
    public <T> List<T> read(final int headerRowIndex, final int startRowIndex, final Class<T> beanType) {
        return read(headerRowIndex, startRowIndex, Integer.MAX_VALUE, beanType);
    }

    /**
     * Reads the Excel file into a list of Bean objects.
     *
     * @param <T>            The type of the Bean.
     * @param headerRowIndex The row index where the header is located. If the header row is in the middle of the
     *                       content rows to be read, this row will be ignored as data.
     * @param startRowIndex  The starting row index (inclusive, 0-based).
     * @param endRowIndex    The ending row index (inclusive, 0-based).
     * @param beanType       The type of the Bean corresponding to each row.
     * @return A list of Bean objects.
     */
    public <T> List<T> read(
            final int headerRowIndex,
            final int startRowIndex,
            final int endRowIndex,
            final Class<T> beanType) {
        final BeanSheetReader<T> reader = new BeanSheetReader<>(headerRowIndex, startRowIndex, endRowIndex, beanType);
        reader.setExcelConfig(this.config);
        return read(reader);
    }

    /**
     * Reads data into a specified type.
     *
     * @param <T>         The type of data to read.
     * @param sheetReader The {@link SheetReader} implementation.
     * @return The data read result.
     */
    public <T> T read(final SheetReader<T> sheetReader) {
        checkClosed();
        return Assert.notNull(sheetReader).read(this.sheet);
    }

    /**
     * Reads the Excel content as plain text using {@link org.apache.poi.ss.extractor.ExcelExtractor}.
     *
     * @param withSheetName {@code true} to include sheet names in the extracted text, {@code false} otherwise.
     * @return The extracted Excel content as a string.
     */
    public String readAsText(final boolean withSheetName) {
        return ExcelExtractor.readAsText(this.workbook, withSheetName);
    }

    /**
     * Gets an {@link org.apache.poi.ss.extractor.ExcelExtractor} object.
     *
     * @return An {@link org.apache.poi.ss.extractor.ExcelExtractor} instance.
     */
    public org.apache.poi.ss.extractor.ExcelExtractor getExtractor() {
        return ExcelExtractor.getExtractor(this.workbook);
    }

    /**
     * Reads a specific row of data.
     *
     * @param rowIndex The row index, 0-based.
     * @return A list of data for the row.
     */
    public List<Object> readRow(final int rowIndex) {
        return readRow(this.sheet.getRow(rowIndex));
    }

    /**
     * Reads the value of a specific cell.
     *
     * @param x X-coordinate (column index), 0-based.
     * @param y Y-coordinate (row index), 0-based.
     * @return The cell value, or {@code null} if the cell has no value.
     */
    public Object readCellValue(final int x, final int y) {
        return CellKit.getCellValue(getCell(x, y), this.config.getCellEditor());
    }

    /**
     * Gets an {@link ExcelWriter} instance. After reading and editing Excel, this writer can be used to write out the
     * changes. The rules are as follows:
     * <ul>
     * <li>1. When reading from a stream, the {@link Sheet} object is used directly by the Writer. Modifications will
     * not affect the source file, and {@code Writer.flush()} needs to specify a new path.</li>
     * <li>2. When reading from a file, the file and sheet name are directly obtained. In this case, the original file
     * can be modified.</li>
     * </ul>
     *
     * @return An {@link ExcelWriter} instance.
     */
    public ExcelWriter getWriter() {
        if (null == this.targetFile) {
            // Not reading from a file, directly operate on the sheet.
            return new ExcelWriter(this.sheet);
        }
        return ExcelKit.getWriter(this.targetFile, this.sheet.getSheetName());
    }

    /**
     * Reads a single row of data.
     *
     * @param row The row to read.
     * @return A list of cell values for the row.
     */
    private List<Object> readRow(final Row row) {
        return RowKit.readRow(row, this.config.getCellEditor());
    }

}
