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
package org.miaixz.bus.office.excel.reader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.Builder;
import org.miaixz.bus.office.excel.sax.ExcelSaxReader;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.miaixz.bus.office.excel.xyz.ExcelSaxKit;
import org.miaixz.bus.office.excel.xyz.RowKit;

/**
 * Streaming Excel reader for large datasets.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BigExcelReader implements AutoCloseable {

    /**
     * Lightweight signal exception for normal end-of-read flow.
     */
    private static final class EndOfReadException extends RuntimeException {

        private static final EndOfReadException INSTANCE = new EndOfReadException();

        private EndOfReadException() {
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    /**
     * Source file when reading from file.
     */
    private final File sourceFile;
    /**
     * Source stream when reading from stream.
     */
    private InputStream sourceStream;
    /**
     * Sheet selector, -1 means all sheets.
     */
    private final String idOrRidOrSheetName;
    /**
     * Read configuration.
     */
    private ExcelReadConfig config = new ExcelReadConfig();
    /**
     * Closed flag.
     */
    private boolean closed;

    /**
     * Constructs a big reader from file path, reading all sheets.
     *
     * @param excelFilePath Excel path.
     */
    public BigExcelReader(final String excelFilePath) {
        this(FileKit.file(excelFilePath), -1);
    }

    /**
     * Constructs a big reader from file path.
     *
     * @param excelFilePath Excel path.
     * @param sheetIndex    sheet index.
     */
    public BigExcelReader(final String excelFilePath, final int sheetIndex) {
        this(FileKit.file(excelFilePath), sheetIndex);
    }

    /**
     * Constructs a big reader from file path.
     *
     * @param excelFilePath Excel path.
     * @param sheetName     sheet name.
     */
    public BigExcelReader(final String excelFilePath, final String sheetName) {
        this(FileKit.file(excelFilePath), sheetName);
    }

    /**
     * Constructs a big reader from file, reading all sheets.
     *
     * @param excelFile Excel file.
     */
    public BigExcelReader(final File excelFile) {
        this(excelFile, -1);
    }

    /**
     * Constructs a big reader from file.
     *
     * @param excelFile  Excel file.
     * @param sheetIndex sheet index.
     */
    public BigExcelReader(final File excelFile, final int sheetIndex) {
        this(excelFile, String.valueOf(sheetIndex));
    }

    /**
     * Constructs a big reader from file.
     *
     * @param excelFile Excel file.
     * @param sheetName sheet name.
     */
    public BigExcelReader(final File excelFile, final String sheetName) {
        this.sourceFile = Assert.notNull(excelFile, "excelFile must not be null");
        this.idOrRidOrSheetName = StringKit.isBlank(sheetName) ? String.valueOf(-1) : sheetName;
    }

    /**
     * Constructs a big reader from stream, reading all sheets.
     *
     * @param excelStream Excel stream.
     */
    public BigExcelReader(final InputStream excelStream) {
        this(excelStream, -1);
    }

    /**
     * Constructs a big reader from stream.
     *
     * @param excelStream Excel stream.
     * @param sheetIndex  sheet index.
     */
    public BigExcelReader(final InputStream excelStream, final int sheetIndex) {
        this(excelStream, String.valueOf(sheetIndex));
    }

    /**
     * Constructs a big reader from stream.
     *
     * @param excelStream Excel stream.
     * @param sheetName   sheet name.
     */
    public BigExcelReader(final InputStream excelStream, final String sheetName) {
        this.sourceFile = null;
        this.sourceStream = IoKit.toMarkSupport(Assert.notNull(excelStream, "excelStream must not be null"));
        this.idOrRidOrSheetName = StringKit.isBlank(sheetName) ? String.valueOf(-1) : sheetName;
    }

    /**
     * Gets read config.
     *
     * @return Read config.
     */
    public ExcelReadConfig getConfig() {
        return this.config;
    }

    /**
     * Sets read config.
     *
     * @param config read config.
     * @return This reader instance for chaining.
     */
    public BigExcelReader setConfig(final ExcelReadConfig config) {
        this.config = null == config ? new ExcelReadConfig() : config;
        return this;
    }

    /**
     * Reads with current config.
     *
     * @param handler row handler.
     */
    public void read(final RowHandler handler) {
        doRead(
                handler,
                this.config.getStartRow(),
                this.config.getEndRow(),
                this.config.getIncludeColumns(),
                this.config.getBatchSize(),
                null);
    }

    /**
     * Reads a row range using streaming mode.
     *
     * @param startRow start row (inclusive, global row index across sheets).
     * @param endRow   end row (inclusive, global row index across sheets).
     * @param handler  row handler.
     */
    public void read(final long startRow, final long endRow, final RowHandler handler) {
        doRead(handler, startRow, endRow, this.config.getIncludeColumns(), this.config.getBatchSize(), null);
    }

    /**
     * Reads with selected columns.
     *
     * @param includeColumns included column indexes.
     * @param handler        row handler.
     */
    public void read(final int[] includeColumns, final RowHandler handler) {
        doRead(
                handler,
                this.config.getStartRow(),
                this.config.getEndRow(),
                includeColumns,
                this.config.getBatchSize(),
                null);
    }

    /**
     * Reads in batch mode.
     *
     * @param batchSize batch size.
     * @param handler   batch handler.
     */
    public void readBatch(final int batchSize, final BatchRowHandler handler) {
        Assert.notNull(handler, "batch handler must not be null");
        doRead(
                null,
                this.config.getStartRow(),
                this.config.getEndRow(),
                this.config.getIncludeColumns(),
                Math.max(batchSize, 1),
                handler);
    }

    /**
     * Closes current reader.
     */
    @Override
    public void close() {
        this.closed = true;
        IoKit.closeQuietly(this.sourceStream);
        this.sourceStream = null;
    }

    /**
     * Performs read with filtering and projection.
     *
     * @param rowHandler      Row handler for row-by-row callbacks.
     * @param startRow        Start row (inclusive, global index across selected sheets).
     * @param endRow          End row (inclusive, global index across selected sheets).
     * @param includeColumns  Included column indexes, optional.
     * @param batchSize       Batch size for batch callback mode.
     * @param batchRowHandler Batch callback, optional.
     */
    private void doRead(
            final RowHandler rowHandler,
            final long startRow,
            final long endRow,
            final int[] includeColumns,
            final int batchSize,
            final BatchRowHandler batchRowHandler) {
        checkClosed();

        final long safeStart = Math.max(0, startRow);
        final long safeEnd = endRow < 0 ? Long.MAX_VALUE : endRow;
        if (safeStart > safeEnd) {
            if (null != rowHandler) {
                rowHandler.doAfterAllAnalysed();
            }
            return;
        }
        final int[] projectedColumns = RowKit.normalizeIncludeColumns(includeColumns);
        final boolean isXlsx = isXlsx();
        final int[] saxIncludeColumns = isXlsx ? projectedColumns : null;
        final boolean hasProjectedColumns = !isXlsx && null != projectedColumns && projectedColumns.length > 0;
        final List<List<Object>>[] batchRowsHolder = batchSize > 0 ? new List[] { new ArrayList<>(batchSize) } : null;
        final long[] globalRowCursor = new long[] { 0L };
        final boolean[] terminated = new boolean[] { false };
        final boolean[] afterAllDone = new boolean[] { false };

        final RowHandler filterHandler = new RowHandler() {

            @Override
            public void handle(final int sheetIndex, final long rowIndex, final List<Object> rowCells) {
                final long currentGlobalRow = globalRowCursor[0]++;
                if (currentGlobalRow > safeEnd) {
                    throw EndOfReadException.INSTANCE;
                }
                if (currentGlobalRow < safeStart) {
                    return;
                }

                List<Object> projected = rowCells;
                if (hasProjectedColumns) {
                    projected = RowKit.projectColumns(rowCells, projectedColumns);
                }

                if (config.isIgnoreEmptyRow() && RowKit.isEmptyRow(projected)) {
                    return;
                }

                if (batchSize > 0 && null != batchRowHandler) {
                    final List<List<Object>> batchRows = batchRowsHolder[0];
                    batchRows.add(projected);
                    if (batchRows.size() >= batchSize) {
                        batchRowHandler.handle(batchRows);
                        batchRowsHolder[0] = new ArrayList<>(batchSize);
                    }
                } else if (null != rowHandler) {
                    rowHandler.handle(sheetIndex, rowIndex, projected);
                }
            }

            @Override
            public void handleCell(
                    final int sheetIndex,
                    final long rowIndex,
                    final int cellIndex,
                    final Object value,
                    final org.apache.poi.ss.usermodel.CellStyle xssfCellStyle) {
                if (null != rowHandler) {
                    rowHandler.handleCell(sheetIndex, rowIndex, cellIndex, value, xssfCellStyle);
                }
            }

            @Override
            public void doAfterAllAnalysed() {
                finishReading(rowHandler, batchSize, batchRowHandler, batchRowsHolder, afterAllDone);
            }
        };

        final ExcelSaxReader<?> saxReader = ExcelSaxKit
                .createSaxReader(isXlsx, filterHandler, this.config.isPadCellAtEndOfRow(), saxIncludeColumns);
        try {
            if (null != this.sourceFile) {
                saxReader.read(this.sourceFile, this.idOrRidOrSheetName);
            } else {
                saxReader.read(this.sourceStream, this.idOrRidOrSheetName);
            }
        } catch (final EndOfReadException e) {
            terminated[0] = true;
        } finally {
            if (terminated[0]) {
                finishReading(rowHandler, batchSize, batchRowHandler, batchRowsHolder, afterAllDone);
            }
        }
    }

    /**
     * Checks if source file format is xlsx.
     *
     * @return {@code true} if xlsx, {@code false} otherwise.
     */
    private boolean isXlsx() {
        if (null != this.sourceFile) {
            return Builder.isXlsx(this.sourceFile);
        }
        this.sourceStream = IoKit.toMarkSupport(this.sourceStream);
        return Builder.isXlsx(this.sourceStream);
    }

    /**
     * Flushes remaining batches and triggers after callback once.
     *
     * @param rowHandler      Row handler for row-by-row callbacks.
     * @param batchSize       Batch size for batch callback mode.
     * @param batchRowHandler Batch callback, optional.
     * @param batchRowsHolder Holder of current batch rows.
     * @param afterAllDone    Single-invocation guard for after callback.
     */
    private void finishReading(
            final RowHandler rowHandler,
            final int batchSize,
            final BatchRowHandler batchRowHandler,
            final List<List<Object>>[] batchRowsHolder,
            final boolean[] afterAllDone) {
        if (afterAllDone[0]) {
            return;
        }
        afterAllDone[0] = true;

        final List<List<Object>> batchRows = null == batchRowsHolder ? null : batchRowsHolder[0];
        if (batchSize > 0 && null != batchRowHandler && null != batchRows && !batchRows.isEmpty()) {
            batchRowHandler.handle(batchRows);
            batchRowsHolder[0] = new ArrayList<>(batchSize);
        }
        if (null != rowHandler) {
            rowHandler.doAfterAllAnalysed();
        }
    }

    /**
     * Checks closed state.
     *
     * @throws IllegalStateException If the reader has already been closed.
     */
    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("BigExcelReader has been closed");
        }
    }

    /**
     * Batch row callback.
     */
    @FunctionalInterface
    public interface BatchRowHandler {

        /**
         * Handles a batch of rows.
         *
         * @param rows row batch.
         */
        void handle(List<List<Object>> rows);
    }

}
