/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel.reader;

import org.miaixz.bus.office.excel.ExcelConfig;

/**
 * Excel read configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelReadConfig extends ExcelConfig {

    /**
     * Read mode.
     */
    protected ReadMode readMode = ReadMode.AUTO;
    /**
     * Whether to ignore empty rows.
     */
    protected boolean ignoreEmptyRow = true;
    /**
     * Start row index (global in streaming reads).
     */
    protected long startRow;
    /**
     * End row index (global in streaming reads), inclusive.
     */
    protected long endRow = Long.MAX_VALUE;
    /**
     * Included column indexes for projection.
     */
    protected int[] includeColumns;
    /**
     * Batch size for callbacks. 0 means row-by-row.
     */
    protected int batchSize;
    /**
     * Whether to pad row cells to include trailing empty cells.
     */
    protected boolean padCellAtEndOfRow;
    /**
     * Queue capacity for transfer pipeline. Non-positive means auto sizing.
     */
    protected int transferQueueCapacity;
    /**
     * Poll timeout in milliseconds for transfer pipeline. Non-positive means default timeout.
     */
    protected long transferPollTimeoutMs;
    /**
     * Estimated column count for transfer auto tuning.
     */
    protected int transferEstimatedColumnCount = 80;

    /**
     * Read mode enum.
     */
    public enum ReadMode {
        /**
         * Automatically choose mode.
         */
        AUTO,
        /**
         * Force workbook in-memory mode.
         */
        MEMORY,
        /**
         * Force SAX streaming mode.
         */
        STREAMING
    }

    /**
     * Gets read mode.
     *
     * @return Read mode.
     */
    public ReadMode getReadMode() {
        return this.readMode;
    }

    /**
     * Sets read mode.
     *
     * @param readMode read mode.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setReadMode(final ReadMode readMode) {
        this.readMode = null == readMode ? ReadMode.AUTO : readMode;
        return this;
    }

    /**
     * Checks whether empty rows should be ignored.
     *
     * @return {@code true} if empty rows are ignored, {@code false} otherwise.
     */
    public boolean isIgnoreEmptyRow() {
        return this.ignoreEmptyRow;
    }

    /**
     * Sets whether empty rows should be ignored.
     *
     * @param ignoreEmptyRow {@code true} to ignore empty rows, {@code false} otherwise.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setIgnoreEmptyRow(final boolean ignoreEmptyRow) {
        this.ignoreEmptyRow = ignoreEmptyRow;
        return this;
    }

    /**
     * Gets start row index.
     *
     * @return Start row index.
     */
    public long getStartRow() {
        return this.startRow;
    }

    /**
     * Sets start row index.
     *
     * @param startRow start row index.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setStartRow(final long startRow) {
        this.startRow = Math.max(startRow, 0);
        return this;
    }

    /**
     * Gets end row index.
     *
     * @return End row index.
     */
    public long getEndRow() {
        return this.endRow;
    }

    /**
     * Sets end row index.
     *
     * @param endRow end row index.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setEndRow(final long endRow) {
        this.endRow = endRow < 0 ? Long.MAX_VALUE : endRow;
        return this;
    }

    /**
     * Gets included column indexes.
     *
     * @return Included column indexes.
     */
    public int[] getIncludeColumns() {
        return this.includeColumns;
    }

    /**
     * Sets included column indexes for projection.
     *
     * @param includeColumns included column indexes.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setIncludeColumns(final int[] includeColumns) {
        this.includeColumns = includeColumns;
        return this;
    }

    /**
     * Gets batch size.
     *
     * @return Batch size.
     */
    public int getBatchSize() {
        return this.batchSize;
    }

    /**
     * Sets batch size. 0 means row-by-row callbacks.
     *
     * @param batchSize batch size.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setBatchSize(final int batchSize) {
        this.batchSize = Math.max(batchSize, 0);
        return this;
    }

    /**
     * Checks whether trailing empty cells should be padded.
     *
     * @return {@code true} if row tail cells should be padded, {@code false} otherwise.
     */
    public boolean isPadCellAtEndOfRow() {
        return this.padCellAtEndOfRow;
    }

    /**
     * Sets whether trailing empty cells should be padded.
     *
     * @param padCellAtEndOfRow {@code true} to pad row tail cells, {@code false} otherwise.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setPadCellAtEndOfRow(final boolean padCellAtEndOfRow) {
        this.padCellAtEndOfRow = padCellAtEndOfRow;
        return this;
    }

    /**
     * Gets transfer queue capacity.
     *
     * @return Queue capacity.
     */
    public int getTransferQueueCapacity() {
        return this.transferQueueCapacity;
    }

    /**
     * Sets transfer queue capacity.
     *
     * @param transferQueueCapacity queue capacity, non-positive means auto sizing.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setTransferQueueCapacity(final int transferQueueCapacity) {
        this.transferQueueCapacity = transferQueueCapacity;
        return this;
    }

    /**
     * Gets transfer poll timeout in milliseconds.
     *
     * @return Poll timeout in milliseconds.
     */
    public long getTransferPollTimeoutMs() {
        return this.transferPollTimeoutMs;
    }

    /**
     * Sets transfer poll timeout in milliseconds.
     *
     * @param transferPollTimeoutMs poll timeout in milliseconds, non-positive means default timeout.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setTransferPollTimeoutMs(final long transferPollTimeoutMs) {
        this.transferPollTimeoutMs = transferPollTimeoutMs;
        return this;
    }

    /**
     * Gets estimated column count for transfer auto tuning.
     *
     * @return Estimated column count.
     */
    public int getTransferEstimatedColumnCount() {
        return this.transferEstimatedColumnCount;
    }

    /**
     * Sets estimated column count for transfer auto tuning.
     *
     * @param transferEstimatedColumnCount estimated column count.
     * @return This {@code ExcelReadConfig} instance, for chaining.
     */
    public ExcelReadConfig setTransferEstimatedColumnCount(final int transferEstimatedColumnCount) {
        this.transferEstimatedColumnCount = transferEstimatedColumnCount > 0 ? transferEstimatedColumnCount : 1;
        return this;
    }

}
