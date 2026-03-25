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
package org.miaixz.bus.office.excel.writer;

import java.io.File;
import java.io.OutputStream;
import java.util.IllegalFormatException;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.SheetKit;
import org.miaixz.bus.office.excel.WorkbookKit;

/**
 * Excel writer for large datasets, supporting only XLSX (Excel 07+ version). By encapsulating {@link SXSSFWorkbook}, it
 * limits access to rows in a sliding window to achieve low memory usage. Note that if the written data exceeds the
 * sliding window size, it will be written to a temporary file, and the written data cannot be accessed or edited at
 * that point.
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * Auto-splitting sheet sequence.
     */
    private int autoSplitSheetSeq;

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
     * Constructs a new {@code BigExcelWriter} with SXSSF tuning options.
     *
     * @param destFile              The target file.
     * @param rowAccessWindowSize   The number of rows to keep in memory.
     * @param compressTmpFiles      Whether to compress SXSSF temp files.
     * @param useSharedStringsTable Whether to use shared strings table.
     * @param sheetName             The sheet name.
     */
    public BigExcelWriter(final File destFile, final int rowAccessWindowSize, final boolean compressTmpFiles,
            final boolean useSharedStringsTable, final String sheetName) {
        this(destFile.exists() ? WorkbookKit.createSXSSFBook(destFile)
                : WorkbookKit.createSXSSFBook(rowAccessWindowSize, compressTmpFiles, useSharedStringsTable), sheetName);
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

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param config the Excel write configuration
     * @return this writer instance for chaining
     */
    @Override
    public BigExcelWriter setConfig(final ExcelWriteConfig config) {
        super.setConfig(config);
        if (config.isBigDataMode()) {
            disableDefaultStyle();
        }
        return this;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param rowData data of a row.
     * @return this.
     */
    @Override
    public BigExcelWriter writeHeaderRow(final Iterable<?> rowData) {
        ensureCapacityForNextRows(1);
        super.writeHeaderRow(rowData);
        return this;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param rowBean          the Bean to be written, can be Map, Bean or Iterable.
     * @param isWriteKeyAsHead when true, write two rows, otherwise one row.
     * @return this.
     */
    @Override
    public BigExcelWriter writeRow(final Object rowBean, final boolean isWriteKeyAsHead) {
        ensureCapacityForNextRows(estimateRowsForWriteRow(rowBean, isWriteKeyAsHead));
        super.writeRow(rowBean, isWriteKeyAsHead);
        return this;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param rowData data of a row.
     * @return this.
     */
    @Override
    public BigExcelWriter writeRow(final Iterable<?> rowData) {
        ensureCapacityForNextRows(1);
        super.writeRow(rowData);
        return this;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param sheetIndex the zero-based sheet index
     * @return this writer instance for chaining
     */
    @Override
    public BigExcelWriter setSheet(final int sheetIndex) {
        super.setSheet(sheetIndex);
        return this;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param sheetName the sheet name
     * @return this writer instance for chaining
     */
    @Override
    public BigExcelWriter setSheet(final String sheetName) {
        super.setSheet(sheetName);
        return this;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param rowIndex row number.
     * @return this.
     */
    @Override
    public BigExcelWriter setCurrentRow(final int rowIndex) {
        super.setCurrentRow(rowIndex);
        return this;
    }

    /**
     * Auto-sizes the specified column width for SXSSF sheets.
     *
     * @param columnIndex    The 0-based column index to auto-size.
     * @param useMergedCells Whether to consider merged cells when calculating width.
     * @param widthRatio     The width ratio multiplier to apply after auto-sizing.
     * @return This {@code BigExcelWriter} instance for chaining.
     */
    @Override
    public BigExcelWriter autoSizeColumn(final int columnIndex, final boolean useMergedCells, final float widthRatio) {
        final SXSSFSheet sheet = (SXSSFSheet) this.sheet;
        sheet.trackColumnForAutoSizing(columnIndex);
        super.autoSizeColumn(columnIndex, useMergedCells, widthRatio);
        sheet.untrackColumnForAutoSizing(columnIndex);
        return this;
    }

    /**
     * Auto-sizes all columns in the sheet for SXSSF sheets.
     *
     * @param useMergedCells Whether to consider merged cells when calculating width.
     * @param widthRatio     The width ratio multiplier to apply after auto-sizing.
     * @return This {@code BigExcelWriter} instance for chaining.
     */
    @Override
    public BigExcelWriter autoSizeColumnAll(final boolean useMergedCells, final float widthRatio) {
        final SXSSFSheet sheet = (SXSSFSheet) this.sheet;
        sheet.trackAllColumnsForAutoSizing();
        super.autoSizeColumnAll(useMergedCells, widthRatio);
        sheet.untrackAllColumnsForAutoSizing();
        return this;
    }

    /**
     * Flushes the Excel workbook to the output stream. This method can only be called once.
     *
     * @param out        The output stream to write to.
     * @param isCloseOut Whether to close the output stream after writing.
     * @return This {@code ExcelWriter} instance for chaining.
     * @throws InternalException If an I/O error occurs during writing.
     */
    @Override
    public ExcelWriter flush(final OutputStream out, final boolean isCloseOut) throws InternalException {
        if (!isFlushed) {
            isFlushed = true;
            return super.flush(out, isCloseOut);
        }
        return this;
    }

    /**
     * Closes the writer, flushing to the target file if set and cleaning up temporary files.
     */
    @Override
    public void close() {
        if (null != this.targetFile && !isFlushed) {
            flush();
        }

        // Clean up temporary files.
        IoKit.close(this.workbook);
        super.closeWithoutFlush();
    }

    /**
     * Ensures there is enough capacity for the next rows.
     *
     * @param rowsToWrite rows that will be written next.
     */
    private void ensureCapacityForNextRows(final int rowsToWrite) {
        if (rowsToWrite <= 0 || !isAutoSplitEnabled()) {
            return;
        }
        if (rowsToWrite > getMaxRowsPerSheet()) {
            throw new IllegalArgumentException("rowsToWrite exceeds maxRowsPerSheet: " + rowsToWrite);
        }
        if (isTemplateMode()) {
            throw new IllegalStateException("autoSplitSheet is not supported in template mode");
        }
        switchToNextSheetIfNeeded(rowsToWrite);
    }

    /**
     * Switches to next sheet if current sheet has no remaining row capacity.
     *
     * @param rowsToWrite rows that will be written next.
     */
    private void switchToNextSheetIfNeeded(final int rowsToWrite) {
        if (getCurrentRow() + rowsToWrite <= getMaxRowsPerSheet()) {
            return;
        }

        String nextSheetName;
        do {
            this.autoSplitSheetSeq++;
            nextSheetName = formatSheetName(this.autoSplitSheetSeq);
        } while (null != this.workbook.getSheet(nextSheetName));

        super.setSheet(nextSheetName);
        super.setCurrentRow(0);
    }

    /**
     * Formats sheet name by configured pattern.
     *
     * @param sheetSeq sheet sequence.
     * @return Formatted sheet name.
     */
    private String formatSheetName(final int sheetSeq) {
        final String pattern = this.config.getSheetNamePattern();
        try {
            return String.format(pattern, sheetSeq);
        } catch (final IllegalFormatException e) {
            return StringKit.format("sheet_{}", sheetSeq);
        }
    }

    /**
     * Returns whether auto split is enabled.
     *
     * @return {@code true} if auto split enabled, {@code false} otherwise.
     */
    private boolean isAutoSplitEnabled() {
        return this.config.isAutoSplitSheet();
    }

    /**
     * Gets configured max rows per sheet.
     *
     * @return max rows per sheet.
     */
    private int getMaxRowsPerSheet() {
        return this.config.getMaxRowsPerSheet();
    }

    /**
     * Estimates how many rows a writeRow call will consume.
     *
     * @param rowBean          row object.
     * @param isWriteKeyAsHead whether map/bean header row is written.
     * @return estimated rows to write.
     */
    private int estimateRowsForWriteRow(final Object rowBean, final boolean isWriteKeyAsHead) {
        if (!isWriteKeyAsHead || null == rowBean) {
            return 1;
        }
        return rowBean instanceof Iterable ? 1 : 2;
    }

}
