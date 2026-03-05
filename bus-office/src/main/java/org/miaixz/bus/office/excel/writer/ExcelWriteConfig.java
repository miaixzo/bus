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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.center.map.TableMap;
import org.miaixz.bus.core.center.map.multiple.RowKeyTable;
import org.miaixz.bus.core.center.map.multiple.Table;
import org.miaixz.bus.core.compare.IndexedCompare;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.ExcelConfig;

/**
 * Excel write configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelWriteConfig extends ExcelConfig {

    /**
     * Maximum rows allowed per XLSX sheet.
     */
    public static final int XLSX_MAX_ROWS_PER_SHEET = 1_048_576;

    /**
     * Whether to retain only fields corresponding to aliases. If {@code true}, fields without aliases will not be
     * output.
     */
    protected boolean onlyAlias;
    /**
     * Whether to force row insertion. If {@code true}, existing rows below the written row will be shifted down. If
     * {@code false}, existing rows will be filled, and new rows will be created only if necessary.
     */
    protected boolean insertRow = true;
    /**
     * Comparator for sorting header names.
     */
    protected Comparator<String> aliasComparator;
    /**
     * Whether to automatically split into multiple sheets when row limit is reached.
     */
    protected boolean autoSplitSheet = true;
    /**
     * Maximum rows allowed in each sheet when auto split is enabled.
     */
    protected int maxRowsPerSheet = 1_000_000;
    /**
     * Name pattern for newly split sheets.
     */
    protected String sheetNamePattern = "sheet_%03d";
    /**
     * Expected total rows for routing decisions.
     */
    protected long expectedRows;
    /**
     * SXSSF row access window size used by big writer.
     */
    protected int bigWriterRowAccessWindowSize = 1024;
    /**
     * Whether to compress SXSSF temporary files.
     */
    protected boolean bigWriterCompressTmpFiles;
    /**
     * Whether to enable SXSSF shared strings table.
     */
    protected boolean bigWriterUseSharedStringsTable;
    /**
     * Big data mode flag.
     */
    protected boolean bigDataMode;

    /**
     * Sets the header alias mapping and resets the alias comparator.
     *
     * @param headerAlias The mapping of header names to aliases.
     * @return This {@code ExcelWriteConfig} instance for chaining.
     */
    @Override
    public ExcelWriteConfig setHeaderAlias(final Map<String, String> headerAlias) {
        this.aliasComparator = null;
        return (ExcelWriteConfig) super.setHeaderAlias(headerAlias);
    }

    /**
     * Adds a header alias and resets the alias comparator.
     *
     * @param header The header name to alias.
     * @param alias  The alias to use for the header.
     * @return This {@code ExcelWriteConfig} instance for chaining.
     */
    @Override
    public ExcelWriteConfig addHeaderAlias(final String header, final String alias) {
        this.aliasComparator = null;
        return (ExcelWriteConfig) super.addHeaderAlias(header, alias);
    }

    /**
     * Removes a header alias and resets the alias comparator.
     *
     * @param header The header name whose alias should be removed.
     * @return This {@code ExcelWriteConfig} instance for chaining.
     */
    @Override
    public ExcelWriteConfig removeHeaderAlias(final String header) {
        this.aliasComparator = null;
        return (ExcelWriteConfig) super.removeHeaderAlias(header);
    }

    /**
     * Sets whether to retain only fields corresponding to aliases. If {@code true}, fields without aliases will not be
     * output. If {@code false}, they will be output as is. When {@code @Alias} is set in a Bean, {@code setOnlyAlias}
     * is ineffective. This parameter is only used in conjunction with {@code addHeaderAlias}, because annotations are
     * internal operations of the Bean, while {@code addHeaderAlias} is an operation of the Writer, and they do not
     * interoperate.
     *
     * @param isOnlyAlias {@code true} to retain only aliased fields, {@code false} to output all fields.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setOnlyAlias(final boolean isOnlyAlias) {
        this.onlyAlias = isOnlyAlias;
        return this;
    }

    /**
     * Sets whether to insert rows. If {@code true}, existing rows below the written row will be shifted down. If
     * {@code false}, existing rows will be filled, and new rows will be created only if necessary.
     *
     * @param insertRow {@code true} to insert rows, {@code false} to fill existing rows.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setInsertRow(final boolean insertRow) {
        this.insertRow = insertRow;
        return this;
    }

    /**
     * Checks whether auto sheet splitting is enabled.
     *
     * @return {@code true} if auto split is enabled, {@code false} otherwise.
     */
    public boolean isAutoSplitSheet() {
        return this.autoSplitSheet;
    }

    /**
     * Sets whether to auto split sheets when row limit is reached.
     *
     * @param autoSplitSheet {@code true} to enable auto split, {@code false} otherwise.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setAutoSplitSheet(final boolean autoSplitSheet) {
        this.autoSplitSheet = autoSplitSheet;
        return this;
    }

    /**
     * Gets maximum rows per sheet for auto splitting.
     *
     * @return Maximum rows per sheet.
     */
    public int getMaxRowsPerSheet() {
        return this.maxRowsPerSheet;
    }

    /**
     * Sets maximum rows per sheet for auto splitting.
     *
     * @param maxRowsPerSheet maximum rows, must be in range [1, 1_048_576].
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setMaxRowsPerSheet(final int maxRowsPerSheet) {
        if (maxRowsPerSheet < 1 || maxRowsPerSheet > XLSX_MAX_ROWS_PER_SHEET) {
            throw new IllegalArgumentException("maxRowsPerSheet must be between 1 and " + XLSX_MAX_ROWS_PER_SHEET
                    + ", but was " + maxRowsPerSheet);
        }
        this.maxRowsPerSheet = maxRowsPerSheet;
        return this;
    }

    /**
     * Gets sheet name pattern for split sheets.
     *
     * @return Sheet name pattern.
     */
    public String getSheetNamePattern() {
        return this.sheetNamePattern;
    }

    /**
     * Sets sheet name pattern for split sheets.
     *
     * @param sheetNamePattern pattern used with {@link String#format(String, Object...)}.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setSheetNamePattern(final String sheetNamePattern) {
        this.sheetNamePattern = StringKit.isBlank(sheetNamePattern) ? "sheet_%03d" : sheetNamePattern;
        return this;
    }

    /**
     * Gets expected total rows for write routing.
     *
     * @return Expected row count.
     */
    public long getExpectedRows() {
        return this.expectedRows;
    }

    /**
     * Sets expected total rows for write routing.
     *
     * @param expectedRows expected row count.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setExpectedRows(final long expectedRows) {
        this.expectedRows = Math.max(expectedRows, 0);
        return this;
    }

    /**
     * Gets SXSSF row access window size.
     *
     * @return Row access window size.
     */
    public int getBigWriterRowAccessWindowSize() {
        return this.bigWriterRowAccessWindowSize;
    }

    /**
     * Sets SXSSF row access window size.
     *
     * @param bigWriterRowAccessWindowSize row access window size.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setBigWriterRowAccessWindowSize(final int bigWriterRowAccessWindowSize) {
        this.bigWriterRowAccessWindowSize = bigWriterRowAccessWindowSize;
        return this;
    }

    /**
     * Checks whether SXSSF temp files are compressed.
     *
     * @return {@code true} if compressed, {@code false} otherwise.
     */
    public boolean isBigWriterCompressTmpFiles() {
        return this.bigWriterCompressTmpFiles;
    }

    /**
     * Sets whether SXSSF temp files are compressed.
     *
     * @param bigWriterCompressTmpFiles {@code true} to compress temp files, {@code false} otherwise.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setBigWriterCompressTmpFiles(final boolean bigWriterCompressTmpFiles) {
        this.bigWriterCompressTmpFiles = bigWriterCompressTmpFiles;
        return this;
    }

    /**
     * Checks whether SXSSF shared strings table is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isBigWriterUseSharedStringsTable() {
        return this.bigWriterUseSharedStringsTable;
    }

    /**
     * Sets whether SXSSF shared strings table is enabled.
     *
     * @param bigWriterUseSharedStringsTable {@code true} to enable shared strings, {@code false} otherwise.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setBigWriterUseSharedStringsTable(final boolean bigWriterUseSharedStringsTable) {
        this.bigWriterUseSharedStringsTable = bigWriterUseSharedStringsTable;
        return this;
    }

    /**
     * Checks whether big data mode is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isBigDataMode() {
        return this.bigDataMode;
    }

    /**
     * Sets big data mode.
     *
     * @param bigDataMode {@code true} to enable big data mode, {@code false} otherwise.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setBigDataMode(final boolean bigDataMode) {
        this.bigDataMode = bigDataMode;
        return this;
    }

    /**
     * Gets the singleton alias comparator. The order of comparison is the order in which aliases were added.
     *
     * @return The {@link Comparator} for aliases.
     */
    public Comparator<String> getCachedAliasComparator() {
        final Map<String, String> headerAlias = this.headerAlias;
        if (MapKit.isEmpty(headerAlias)) {
            return null;
        }
        Comparator<String> aliasComparator = this.aliasComparator;
        if (null == aliasComparator) {
            final Set<String> keySet = headerAlias.keySet();
            aliasComparator = new IndexedCompare<>(keySet.toArray(new String[0]));
            this.aliasComparator = aliasComparator;
        }
        return aliasComparator;
    }

    /**
     * Creates an alias table for the specified key list. If no alias is defined for a key, the original key is used
     * when {@code onlyAlias} is {@code false}. The key of the table is the alias, and the value is the field value.
     *
     * @param rowMap A single row of data.
     * @return A {@link Table} representing the aliased data.
     */
    public Table<?, ?, ?> aliasTable(final Map<?, ?> rowMap) {
        final Table<Object, Object, Object> filteredTable = new RowKeyTable<>(new LinkedHashMap<>(), TableMap::new);
        if (MapKit.isEmpty(headerAlias)) {
            rowMap.forEach((key, value) -> filteredTable.put(key, key, value));
        } else {
            rowMap.forEach((key, value) -> {
                final String aliasName = headerAlias.get(StringKit.toString(key));
                if (null != aliasName) {
                    // Add alias key-value pair.
                    filteredTable.put(key, aliasName, value);
                } else if (!onlyAlias) {
                    // Retain key-value pairs without alias settings.
                    filteredTable.put(key, key, value);
                }
            });
        }

        return filteredTable;
    }

}
