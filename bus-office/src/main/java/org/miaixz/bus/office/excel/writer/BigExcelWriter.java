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

import java.io.File;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.office.excel.xyz.SheetKit;
import org.miaixz.bus.office.excel.xyz.WorkbookKit;

/**
 * Excel writer for large datasets, supporting only XLSX (Excel 07+ version). By encapsulating {@link SXSSFWorkbook}, it
 * limits access to rows in a sliding window to achieve low memory usage. Note that if the written data exceeds the
 * sliding window size, it will be written to a temporary file, and the written data cannot be accessed or edited at
 * that point.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BigExcelWriter extends ExcelWriter {

    /**
     * Default number of rows to keep in memory, default is 100.
     */
    public static final int DEFAULT_WINDOW_SIZE = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;

    /**
     * BigExcelWriter can only be flushed once, so it will not write again after the first call.
     */
    private boolean isFlushed;

    /**
     * Constructs a new {@code BigExcelWriter}, generating an XLSX format Excel file by default. This constructor does
     * not take an Excel file path for writing; only the {@link #flush(OutputStream)} method can be called to write to a
     * stream. To write to a file, you must first call {@link #setTargetFile(File)} to customize the output file, and
     * then call {@link #flush()} to write to the file.
     */
    public BigExcelWriter() {
        this(DEFAULT_WINDOW_SIZE);
    }

    /**
     * Constructs a new {@code BigExcelWriter}. This constructor does not take an Excel file path for writing; only the
     * {@link #flush(OutputStream)} method can be called to write to a stream. To write to a file, you must call
     * {@link #flush(File, boolean)} to write to a file.
     *
     * @param rowAccessWindowSize The number of rows to keep in memory.
     */
    public BigExcelWriter(final int rowAccessWindowSize) {
        this(WorkbookKit.createSXSSFBook(rowAccessWindowSize), null);
    }

    /**
     * Constructs a new {@code BigExcelWriter}. This constructor does not take an Excel file path for writing; only the
     * {@link #flush(OutputStream)} method can be called to write to a stream. To write to a file, you must call
     * {@link #flush(File, boolean)} to write to a file.
     *
     * @param rowAccessWindowSize   The number of rows to keep in memory. -1 means no limit, requiring manual flushing.
     * @param compressTmpFiles      Whether to use Gzip compression for temporary files.
     * @param useSharedStringsTable Whether to use a shared strings table. Generally, enabling this for a large number
     *                              of duplicate strings can save memory.
     * @param sheetName             The name of the sheet to write to.
     */
    public BigExcelWriter(final int rowAccessWindowSize, final boolean compressTmpFiles,
            final boolean useSharedStringsTable, final String sheetName) {
        this(WorkbookKit.createSXSSFBook(rowAccessWindowSize, compressTmpFiles, useSharedStringsTable), sheetName);
    }

    /**
     * Constructs a new {@code BigExcelWriter}. By default, it writes to the first sheet, named "sheet1".
     *
     * @param destFilePath The path to the target file, which may not exist.
     */
    public BigExcelWriter(final String destFilePath) {
        this(destFilePath, null);
    }

    /**
     * Constructs a new {@code BigExcelWriter}. This constructor does not take an Excel file path for writing; only the
     * {@link #flush(OutputStream)} method can be called to write to a stream. To write to a file, you must call
     * {@link #flush(File, boolean)} to write to a file.
     *
     * @param rowAccessWindowSize The number of rows to keep in memory.
     * @param sheetName           The name of the sheet. The first sheet is typically named "sheet1".
     */
    public BigExcelWriter(final int rowAccessWindowSize, final String sheetName) {
        this(WorkbookKit.createSXSSFBook(rowAccessWindowSize), sheetName);
    }

    /**
     * Constructs a new {@code BigExcelWriter}.
     *
     * @param destFilePath The path to the target file, which may not exist.
     * @param sheetName    The name of the sheet. The first sheet is typically named "sheet1".
     */
    public BigExcelWriter(final String destFilePath, final String sheetName) {
        this(FileKit.file(destFilePath), sheetName);
    }

    /**
     * Constructs a new {@code BigExcelWriter}. By default, it writes to the first sheet, named "sheet1".
     *
     * @param destFile The target file, which may not exist.
     */
    public BigExcelWriter(final File destFile) {
        this(destFile, null);
    }

    /**
     * Constructs a new {@code BigExcelWriter}.
     *
     * @param destFile  The target file, which may not exist.
     * @param sheetName The name of the sheet. The first sheet is typically named "sheet1".
     */
    public BigExcelWriter(final File destFile, final String sheetName) {
        this(destFile.exists() ? WorkbookKit.createSXSSFBook(destFile) : WorkbookKit.createSXSSFBook(), sheetName);
        this.targetFile = destFile;
    }

    /**
     * Constructs a new {@code BigExcelWriter}. This constructor does not take an Excel file path for writing; only the
     * {@link #flush(OutputStream)} method can be called to write to a stream. To write to a file, you must first call
     * {@link #setTargetFile(File)} to customize the output file, and then call {@link #flush()} to write to the file.
     *
     * @param workbook  The {@link SXSSFWorkbook} instance.
     * @param sheetName The name of the sheet. The first sheet is typically named "sheet1".
     */
    public BigExcelWriter(final SXSSFWorkbook workbook, final String sheetName) {
        this(SheetKit.getOrCreateSheet(workbook, sheetName));
    }

    /**
     * Constructs a new {@code BigExcelWriter}. This constructor does not take an Excel file path for writing; only the
     * {@link #flush(OutputStream)} method can be called to write to a stream. To write to a file, you must first call
     * {@link #setTargetFile(File)} to customize the output file, and then call {@link #flush()} to write to the file.
     *
     * @param sheet The {@link Sheet} to write to.
     */
    public BigExcelWriter(final Sheet sheet) {
        super(sheet);
    }

    @Override
    public BigExcelWriter autoSizeColumn(final int columnIndex, final boolean useMergedCells, final float widthRatio) {
        final SXSSFSheet sheet = (SXSSFSheet) this.sheet;
        sheet.trackColumnForAutoSizing(columnIndex);
        super.autoSizeColumn(columnIndex, useMergedCells, widthRatio);
        sheet.untrackColumnForAutoSizing(columnIndex);
        return this;
    }

    @Override
    public BigExcelWriter autoSizeColumnAll(final boolean useMergedCells, final float widthRatio) {
        final SXSSFSheet sheet = (SXSSFSheet) this.sheet;
        sheet.trackAllColumnsForAutoSizing();
        super.autoSizeColumnAll(useMergedCells, widthRatio);
        sheet.untrackAllColumnsForAutoSizing();
        return this;
    }

    @Override
    public ExcelWriter flush(final OutputStream out, final boolean isCloseOut) throws InternalException {
        if (!isFlushed) {
            isFlushed = true;
            return super.flush(out, isCloseOut);
        }
        return this;
    }

    @Override
    public void close() {
        if (null != this.targetFile && !isFlushed) {
            flush();
        }

        // Clean up temporary files.
        IoKit.close(this.workbook);
        super.closeWithoutFlush();
    }

}
