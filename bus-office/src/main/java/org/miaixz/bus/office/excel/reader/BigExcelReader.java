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

import org.apache.poi.ss.usermodel.Workbook;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.Builder;
import org.miaixz.bus.office.excel.WorkbookKit;
import org.miaixz.bus.office.excel.sax.ExcelSaxReader;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.miaixz.bus.office.excel.ExcelSaxKit;
import org.miaixz.bus.office.excel.RowKit;

/**
 * Streaming Excel reader for large datasets.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BigExcelReader implements AutoCloseable {

    /**
     * Lightweight signal exception for normal end-of-read flow.
     */
    private static final class EndOfReadException extends RuntimeException {

        /**
         * Shared singleton instance used to abort SAX iteration without allocating a full stack trace.
         */
        private static final EndOfReadException INSTANCE = new EndOfReadException();

        /**
         * Creates the end-of-read signal exception.
         */
        private EndOfReadException() {
        }

        /**
         * Skips stack trace generation because this exception is only used as an internal control signal.
         *
         * @return current exception instance
         */
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
                null,
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
        doRead(handler, startRow, endRow, this.config.getIncludeColumns(), this.config.getBatchSize(), null, null);
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
                null,
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
                handler,
                null);
    }

    /**
     * Reads in batch mode with sheet context metadata.
     *
     * @param batchSize batch size.
     * @param handler   contextual batch handler.
     */
    public void readBatchWithContext(final int batchSize, final ContextBatchRowHandler handler) {
        Assert.notNull(handler, "context batch handler must not be null");
        doRead(
                null,
                this.config.getStartRow(),
                this.config.getEndRow(),
                this.config.getIncludeColumns(),
                Math.max(batchSize, 1),
                null,
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
     * @param rowHandler             Row handler for row-by-row callbacks.
     * @param startRow               Start row (inclusive, global index across selected sheets).
     * @param endRow                 End row (inclusive, global index across selected sheets).
     * @param includeColumns         Included column indexes, optional.
     * @param batchSize              Batch size for batch callback mode.
     * @param batchRowHandler        Batch callback, optional.
     * @param contextBatchRowHandler Context-aware batch callback, optional.
     */
    private void doRead(
            final RowHandler rowHandler,
            final long startRow,
            final long endRow,
            final int[] includeColumns,
            final int batchSize,
            final BatchRowHandler batchRowHandler,
            final ContextBatchRowHandler contextBatchRowHandler) {
        checkClosed();

        final long safeStart = Math.max(0, startRow);
        final long safeEnd = endRow < 0 ? Long.MAX_VALUE : endRow;
        final ExcelReadListener listener = this.config.getReadListener();
        final boolean useContextBatch = batchSize > 0 && null != contextBatchRowHandler;
        final long previewRowsPerSheet = Math.max(0, this.config.getPreviewRowsPerSheet());
        final long progressIntervalRows = Math.max(0L, this.config.getProgressReportIntervalRows());
        final long startedAt = System.currentTimeMillis();
        final boolean isXlsx = isXlsx();
        final ExcelReadState.WorkbookContext workbookContext = resolveWorkbookContext(isXlsx);
        if (safeStart > safeEnd) {
            if (null != rowHandler) {
                rowHandler.doAfterAllAnalysed();
            }
            if (null != listener) {
                listener.onWorkbookStart(workbookContext);
                listener.onWorkbookEnd(new ExcelReadState.Progress(workbookContext, null, 0L, 0L, -1L, 0L));
            }
            return;
        }
        final int[] projectedColumns = RowKit.normalizeIncludeColumns(includeColumns);
        final int[] saxIncludeColumns = isXlsx ? projectedColumns : null;
        final boolean hasProjectedColumns = !isXlsx && null != projectedColumns && projectedColumns.length > 0;
        final List<List<Object>>[] batchRowsHolder = batchSize > 0 ? new List[] { new ArrayList<>(batchSize) } : null;
        final long[] batchStartRowHolder = batchSize > 0 ? new long[] { -1L } : null;
        final long[] batchEndRowHolder = batchSize > 0 ? new long[] { -1L } : null;
        final ExcelReadState.SheetContext[] batchSheetHolder = batchSize > 0
                ? new ExcelReadState.SheetContext[] { null }
                : null;
        final long[] globalRowCursor = new long[] { 0L };
        final boolean[] terminated = new boolean[] { false };
        final boolean[] workbookDone = new boolean[] { false };
        final int[] currentSheetIndexHolder = new int[] { -1 };
        final ExcelReadState.SheetContext[] currentSheetHolder = new ExcelReadState.SheetContext[] { null };
        final long[] currentSheetProcessedRows = new long[] { 0L };
        final long[] processedRows = new long[] { 0L };
        final long[] lastReportedRows = new long[] { 0L };

        if (null != listener) {
            listener.onWorkbookStart(workbookContext);
        }

        final RowHandler filterHandler = new RowHandler() {

            @Override
            public void handle(final int sheetIndex, final long rowIndex, final List<Object> rowCells) {
                switchSheetIfNecessary(
                        sheetIndex,
                        listener,
                        workbookContext,
                        currentSheetIndexHolder,
                        currentSheetHolder,
                        currentSheetProcessedRows,
                        batchSize,
                        batchRowHandler,
                        contextBatchRowHandler,
                        batchRowsHolder,
                        batchStartRowHolder,
                        batchEndRowHolder,
                        batchSheetHolder);
                final long currentGlobalRow = globalRowCursor[0]++;
                if (currentGlobalRow > safeEnd) {
                    throw EndOfReadException.INSTANCE;
                }
                if (currentGlobalRow < safeStart) {
                    return;
                }
                if (previewRowsPerSheet > 0 && rowIndex >= previewRowsPerSheet) {
                    return;
                }

                List<Object> projected = rowCells;
                if (hasProjectedColumns) {
                    projected = RowKit.projectColumns(rowCells, projectedColumns);
                }

                if (config.isIgnoreEmptyRow() && RowKit.isEmptyRow(projected)) {
                    return;
                }

                processedRows[0]++;
                currentSheetProcessedRows[0]++;
                if (batchSize > 0 && null != batchRowHandler) {
                    final List<List<Object>> batchRows = batchRowsHolder[0];
                    if (batchRows.isEmpty()) {
                        batchStartRowHolder[0] = rowIndex;
                    }
                    batchEndRowHolder[0] = rowIndex;
                    batchSheetHolder[0] = currentSheetHolder[0];
                    batchRows.add(projected);
                    if (batchRows.size() >= batchSize) {
                        batchRowHandler.handle(batchRows);
                        batchRowsHolder[0] = new ArrayList<>(batchSize);
                        batchStartRowHolder[0] = -1L;
                        batchEndRowHolder[0] = -1L;
                        batchSheetHolder[0] = currentSheetHolder[0];
                    }
                } else if (useContextBatch) {
                    final List<List<Object>> batchRows = batchRowsHolder[0];
                    if (batchRows.isEmpty()) {
                        batchStartRowHolder[0] = rowIndex;
                    }
                    batchEndRowHolder[0] = rowIndex;
                    batchSheetHolder[0] = currentSheetHolder[0];
                    batchRows.add(projected);
                    if (batchRows.size() >= batchSize) {
                        contextBatchRowHandler.handle(
                                new RowBatch(batchSheetHolder[0], batchStartRowHolder[0], batchEndRowHolder[0],
                                        batchRows));
                        batchRowsHolder[0] = new ArrayList<>(batchSize);
                        batchStartRowHolder[0] = -1L;
                        batchEndRowHolder[0] = -1L;
                        batchSheetHolder[0] = currentSheetHolder[0];
                    }
                } else if (null != rowHandler) {
                    rowHandler.handle(sheetIndex, rowIndex, projected);
                }

                if (null != listener && progressIntervalRows > 0
                        && processedRows[0] - lastReportedRows[0] >= progressIntervalRows) {
                    lastReportedRows[0] = processedRows[0];
                    listener.onProgress(
                            new ExcelReadState.Progress(workbookContext, currentSheetHolder[0], processedRows[0],
                                    currentSheetProcessedRows[0], rowIndex,
                                    Math.max(0L, System.currentTimeMillis() - startedAt)));
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
                finishSheet(
                        rowHandler,
                        batchSize,
                        batchRowHandler,
                        contextBatchRowHandler,
                        batchRowsHolder,
                        batchStartRowHolder,
                        batchEndRowHolder,
                        batchSheetHolder,
                        currentSheetIndexHolder,
                        currentSheetHolder,
                        currentSheetProcessedRows,
                        listener,
                        currentSheetHolder[0]);
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
            finishWorkbook(
                    rowHandler,
                    batchSize,
                    batchRowHandler,
                    contextBatchRowHandler,
                    batchRowsHolder,
                    batchStartRowHolder,
                    batchEndRowHolder,
                    batchSheetHolder,
                    currentSheetIndexHolder,
                    currentSheetHolder,
                    currentSheetProcessedRows,
                    workbookDone,
                    listener,
                    workbookContext,
                    processedRows[0],
                    startedAt);
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
     * @param rowHandler                Row handler for row-by-row callbacks.
     * @param batchSize                 Batch size for batch callback mode.
     * @param batchRowHandler           Batch callback, optional.
     * @param contextBatchRowHandler    Context-aware batch callback, optional.
     * @param batchRowsHolder           Holder of current batch rows.
     * @param batchStartRowHolder       Holder of current batch start row index.
     * @param batchEndRowHolder         Holder of current batch end row index.
     * @param batchSheetHolder          Holder of current batch sheet metadata.
     * @param currentSheetIndexHolder   Holder of current sheet index.
     * @param currentSheetHolder        Holder of current sheet context.
     * @param currentSheetProcessedRows Holder of processed row count in current sheet.
     * @param listener                  Lifecycle listener, optional.
     * @param currentSheet              Current sheet context.
     * @param ignored                   Reserved trailing parameters for call-site compatibility.
     */
    private void finishSheet(
            final RowHandler rowHandler,
            final int batchSize,
            final BatchRowHandler batchRowHandler,
            final ContextBatchRowHandler contextBatchRowHandler,
            final List<List<Object>>[] batchRowsHolder,
            final long[] batchStartRowHolder,
            final long[] batchEndRowHolder,
            final ExcelReadState.SheetContext[] batchSheetHolder,
            final int[] currentSheetIndexHolder,
            final ExcelReadState.SheetContext[] currentSheetHolder,
            final long[] currentSheetProcessedRows,
            final ExcelReadListener listener,
            final ExcelReadState.SheetContext currentSheet,
            final long... ignored) {
        if (null == currentSheet) {
            return;
        }
        flushPendingBatch(
                batchSize,
                batchRowHandler,
                contextBatchRowHandler,
                batchRowsHolder,
                batchStartRowHolder,
                batchEndRowHolder,
                batchSheetHolder);
        if (null != rowHandler) {
            rowHandler.doAfterAllAnalysed();
        }
        if (null != listener) {
            listener.onSheetEnd(currentSheet);
        }
        currentSheetIndexHolder[0] = -1;
        currentSheetHolder[0] = null;
        currentSheetProcessedRows[0] = 0L;
    }

    /**
     * Finishes workbook lifecycle exactly once.
     *
     * @param rowHandler                Row handler for row-by-row callbacks.
     * @param batchSize                 Batch size for batch callback mode.
     * @param batchRowHandler           Batch callback, optional.
     * @param contextBatchRowHandler    Context-aware batch callback, optional.
     * @param batchRowsHolder           Holder of current batch rows.
     * @param batchStartRowHolder       Holder of current batch start row index.
     * @param batchEndRowHolder         Holder of current batch end row index.
     * @param batchSheetHolder          Holder of current batch sheet metadata.
     * @param currentSheetIndexHolder   Holder of current sheet index.
     * @param currentSheetHolder        Holder of current sheet context.
     * @param currentSheetProcessedRows Holder of processed row count in current sheet.
     * @param workbookDone              Single-invocation guard for workbook completion.
     * @param listener                  Lifecycle listener, optional.
     * @param workbookContext           Workbook callback context.
     * @param processedRows             Total processed rows after filtering.
     * @param startedAt                 Start timestamp in milliseconds.
     */
    private void finishWorkbook(
            final RowHandler rowHandler,
            final int batchSize,
            final BatchRowHandler batchRowHandler,
            final ContextBatchRowHandler contextBatchRowHandler,
            final List<List<Object>>[] batchRowsHolder,
            final long[] batchStartRowHolder,
            final long[] batchEndRowHolder,
            final ExcelReadState.SheetContext[] batchSheetHolder,
            final int[] currentSheetIndexHolder,
            final ExcelReadState.SheetContext[] currentSheetHolder,
            final long[] currentSheetProcessedRows,
            final boolean[] workbookDone,
            final ExcelReadListener listener,
            final ExcelReadState.WorkbookContext workbookContext,
            final long processedRows,
            final long startedAt) {
        if (workbookDone[0]) {
            return;
        }
        workbookDone[0] = true;
        final ExcelReadState.SheetContext finalSheet = currentSheetHolder[0];
        final long finalSheetRows = currentSheetProcessedRows[0];
        finishSheet(
                rowHandler,
                batchSize,
                batchRowHandler,
                contextBatchRowHandler,
                batchRowsHolder,
                batchStartRowHolder,
                batchEndRowHolder,
                batchSheetHolder,
                currentSheetIndexHolder,
                currentSheetHolder,
                currentSheetProcessedRows,
                listener,
                finalSheet);
        if (null != listener) {
            listener.onWorkbookEnd(
                    new ExcelReadState.Progress(workbookContext, finalSheet, processedRows, finalSheetRows,
                            null == batchEndRowHolder ? -1L : batchEndRowHolder[0],
                            Math.max(0L, System.currentTimeMillis() - startedAt)));
        }
    }

    /**
     * Switches current sheet context when crossing sheet boundaries.
     *
     * @param sheetIndex                Current sheet index reported by SAX reader.
     * @param listener                  Lifecycle listener, optional.
     * @param workbookContext           Workbook callback context.
     * @param currentSheetIndexHolder   Holder of current sheet index.
     * @param currentSheetHolder        Holder of current sheet context.
     * @param currentSheetProcessedRows Holder of processed row count in current sheet.
     * @param batchSize                 Batch size for batch callback mode.
     * @param batchRowHandler           Batch callback, optional.
     * @param contextBatchRowHandler    Context-aware batch callback, optional.
     * @param batchRowsHolder           Holder of current batch rows.
     * @param batchStartRowHolder       Holder of current batch start row index.
     * @param batchEndRowHolder         Holder of current batch end row index.
     * @param batchSheetHolder          Holder of current batch sheet metadata.
     */
    private void switchSheetIfNecessary(
            final int sheetIndex,
            final ExcelReadListener listener,
            final ExcelReadState.WorkbookContext workbookContext,
            final int[] currentSheetIndexHolder,
            final ExcelReadState.SheetContext[] currentSheetHolder,
            final long[] currentSheetProcessedRows,
            final int batchSize,
            final BatchRowHandler batchRowHandler,
            final ContextBatchRowHandler contextBatchRowHandler,
            final List<List<Object>>[] batchRowsHolder,
            final long[] batchStartRowHolder,
            final long[] batchEndRowHolder,
            final ExcelReadState.SheetContext[] batchSheetHolder) {
        if (currentSheetIndexHolder[0] == sheetIndex) {
            return;
        }
        currentSheetIndexHolder[0] = sheetIndex;
        currentSheetProcessedRows[0] = 0L;
        currentSheetHolder[0] = resolveSheetContext(workbookContext, sheetIndex);
        if (null != listener && null != currentSheetHolder[0]) {
            listener.onSheetStart(currentSheetHolder[0]);
        }
    }

    /**
     * Flushes pending batch rows.
     *
     * @param batchSize              Batch size for batch callback mode.
     * @param batchRowHandler        Batch callback, optional.
     * @param contextBatchRowHandler Context-aware batch callback, optional.
     * @param batchRowsHolder        Holder of current batch rows.
     * @param batchStartRowHolder    Holder of current batch start row index.
     * @param batchEndRowHolder      Holder of current batch end row index.
     * @param batchSheetHolder       Holder of current batch sheet metadata.
     */
    private void flushPendingBatch(
            final int batchSize,
            final BatchRowHandler batchRowHandler,
            final ContextBatchRowHandler contextBatchRowHandler,
            final List<List<Object>>[] batchRowsHolder,
            final long[] batchStartRowHolder,
            final long[] batchEndRowHolder,
            final ExcelReadState.SheetContext[] batchSheetHolder) {
        if (batchSize <= 0 || null == batchRowsHolder || null == batchRowsHolder[0] || batchRowsHolder[0].isEmpty()) {
            return;
        }
        if (null != batchRowHandler) {
            batchRowHandler.handle(batchRowsHolder[0]);
        } else if (null != contextBatchRowHandler) {
            contextBatchRowHandler.handle(
                    new RowBatch(null == batchSheetHolder ? null : batchSheetHolder[0],
                            null == batchStartRowHolder ? -1L : batchStartRowHolder[0],
                            null == batchEndRowHolder ? -1L : batchEndRowHolder[0], batchRowsHolder[0]));
        }
        batchRowsHolder[0] = new ArrayList<>(batchSize);
        if (null != batchStartRowHolder) {
            batchStartRowHolder[0] = -1L;
        }
        if (null != batchEndRowHolder) {
            batchEndRowHolder[0] = -1L;
        }
    }

    /**
     * Resolves workbook context for lifecycle callbacks.
     *
     * @param isXlsx whether the source workbook is in xlsx format
     * @return workbook callback context
     */
    private ExcelReadState.WorkbookContext resolveWorkbookContext(final boolean isXlsx) {
        if (null == this.sourceFile) {
            return new ExcelReadState.WorkbookContext("stream", isXlsx, List.of());
        }
        final List<ExcelReadState.SheetContext> sheets = new ArrayList<>();
        try (Workbook workbook = WorkbookKit.createBook(this.sourceFile, true)) {
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                sheets.add(new ExcelReadState.SheetContext(sheetIndex, workbook.getSheetName(sheetIndex)));
            }
        } catch (final Exception e) {
            return new ExcelReadState.WorkbookContext(this.sourceFile.getName(), isXlsx, List.of());
        }
        return new ExcelReadState.WorkbookContext(this.sourceFile.getName(), isXlsx, sheets);
    }

    /**
     * Resolves sheet context by index.
     *
     * @param workbookContext workbook callback context
     * @param sheetIndex      zero-based sheet index
     * @return resolved sheet context
     */
    private ExcelReadState.SheetContext resolveSheetContext(
            final ExcelReadState.WorkbookContext workbookContext,
            final int sheetIndex) {
        if (null != workbookContext && null != workbookContext.sheets() && workbookContext.sheets().size() > sheetIndex
                && sheetIndex >= 0) {
            return workbookContext.sheets().get(sheetIndex);
        }
        return new ExcelReadState.SheetContext(sheetIndex, "");
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

    /**
     * Context-aware batch callback.
     */
    @FunctionalInterface
    public interface ContextBatchRowHandler {

        /**
         * Handles a contextual row batch.
         *
         * @param batch row batch with sheet metadata.
         */
        void handle(RowBatch batch);
    }

    /**
     * Batch payload with sheet metadata.
     *
     * @param sheet         current sheet context
     * @param startRowIndex first row index in this batch
     * @param endRowIndex   last row index in this batch
     * @param rows          row batch data
     */
    public record RowBatch(ExcelReadState.SheetContext sheet, long startRowIndex, long endRowIndex,
            List<List<Object>> rows) {
    }

}
