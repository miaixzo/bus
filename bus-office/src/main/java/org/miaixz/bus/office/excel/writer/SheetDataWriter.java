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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.miaixz.bus.core.center.map.multiple.Table;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.office.excel.RowGroup;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.style.StyleSet;
import org.miaixz.bus.office.excel.xyz.CellKit;
import org.miaixz.bus.office.excel.xyz.RowKit;

/**
 * Sheet data writer. This object only encapsulates writing data to a Sheet and does not flush to a file.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetDataWriter {

    /**
     * The sheet being written to.
     */
    private final Sheet sheet;
    /**
     * Excel output configuration.
     */
    private final ExcelWriteConfig config;
    /**
     * The current row, used to mark the initial writable row and the current row after partial writing.
     */
    private final AtomicInteger currentRow;
    /**
     * Style set, defining styles for different data types.
     */
    private StyleSet styleSet;
    /**
     * Header item to column number mapping cache. This cache is updated each time a header is written. This cache is
     * used to find the header position when the user writes multiple times.
     */
    private Map<String, Integer> headerLocationCache;

    /**
     * Constructs a new {@code SheetDataWriter}.
     *
     * @param sheet    The {@link Sheet} to write to.
     * @param config   The Excel write configuration.
     * @param styleSet The style set.
     */
    public SheetDataWriter(final Sheet sheet, final ExcelWriteConfig config, final StyleSet styleSet) {
        this.sheet = sheet;
        this.config = config;
        this.styleSet = styleSet;
        this.currentRow = new AtomicInteger(0);
    }

    /**
     * Sets the style set.
     *
     * @param styleSet The style set.
     * @return This {@code SheetDataWriter} instance, for chaining.
     */
    public SheetDataWriter setStyleSet(final StyleSet styleSet) {
        this.styleSet = styleSet;
        return this;
    }

    /**
     * Sets the header location mapping cache.
     *
     * @param headerLocationCache The header location mapping cache, where keys are header names and values are column
     *                            numbers.
     * @return This {@code SheetDataWriter} instance, for chaining.
     */
    public SheetDataWriter setHeaderLocationCache(final Map<String, Integer> headerLocationCache) {
        this.headerLocationCache = headerLocationCache;
        return this;
    }

    /**
     * Writes a grouped header row.
     *
     * @param x        The starting column index (0-based).
     * @param y        The starting row index (0-based).
     * @param rowCount The number of rows occupied by the current group row. This value is the sum of rows occupied by
     *                 the header and the maximum rows occupied by child groups. Pass 1 if uncertain.
     * @param rowGroup The {@link RowGroup} representing the grouped header.
     * @return This {@code SheetDataWriter} instance, for chaining.
     */
    public SheetDataWriter writeHeader(int x, int y, int rowCount, final RowGroup rowGroup) {

        // Write main header.
        final String name = rowGroup.getName();
        final List<RowGroup> children = rowGroup.getChildren();
        if (null != name) {
            if (CollKit.isNotEmpty(children)) {
                // If there are child nodes, the header row only occupies the rows not occupied by child nodes.
                rowCount = Math.max(1, rowCount - rowGroup.childrenMaxRowCount());
                // nameRowCount = 1;
            }

            // If there are no child nodes, the header row occupies all rows.
            final CellRangeAddress cellRangeAddresses = CellKit
                    .of(y, y + rowCount - 1, x, x + rowGroup.maxColumnCount() - 1);
            CellStyle style = rowGroup.getStyle();
            if (null == style && null != this.styleSet) {
                style = styleSet.getStyleFor(
                        new CellReference(cellRangeAddresses.getFirstRow(), cellRangeAddresses.getFirstColumn()),
                        name,
                        true);
            }
            CellKit.mergingCells(this.sheet, cellRangeAddresses, style);
            final Cell cell = CellKit
                    .getOrCreateCell(this.sheet, cellRangeAddresses.getFirstColumn(), cellRangeAddresses.getFirstRow());
            if (null != cell) {
                CellKit.setCellValue(cell, name, style, this.config.getCellEditor());
            }

            // Child groups are written to the next N rows.
            y += rowCount;
        }

        // Write groups.
        final int childrenMaxRowCount = rowGroup.childrenMaxRowCount();
        if (childrenMaxRowCount > 0) {
            for (final RowGroup child : children) {
                // Child group row height is filled to the maximum value of the current group.
                writeHeader(x, y, childrenMaxRowCount, child);
                x += child.maxColumnCount();
            }
        }

        return this;
    }

    /**
     * Writes a row. The behavior depends on the type of {@code rowBean}:
     *
     * <pre>
     * 1. If it is an {@link Iterable}, a single row is written directly.
     * 2. If it is a {@link Map}, and {@code
     * isWriteKeyAsHead
     * } is {@code
     * true
     * }, two rows are written: the Map's keys as the first row, and its values as the second row. Otherwise, only the values are written as a single row.
     * 3. If it is a Bean, it is converted to a Map. If {@code
     * isWriteKeyAsHead
     * } is {@code
     * true
     * }, two rows are written: the Bean's field names as the first row, and its field values as the second row. Otherwise, only the field values are written as a single row.
     * </pre>
     *
     * @param rowBean          The Bean to write, which can be a {@link Map}, Bean, or {@link Iterable}.
     * @param isWriteKeyAsHead {@code true} to write two rows (keys as header, values as data) for Map or Bean,
     *                         {@code false} to write only values as a single row.
     * @return This {@code SheetDataWriter} instance, for chaining.
     * @see #writeRow(Iterable)
     * @see #writeRow(Map, boolean)
     */
    public SheetDataWriter writeRow(final Object rowBean, final boolean isWriteKeyAsHead) {
        final ExcelWriteConfig config = this.config;

        final Map<?, ?> rowMap;
        if (rowBean instanceof Map) {
            if (MapKit.isNotEmpty(config.getHeaderAlias())) {
                rowMap = MapKit.newTreeMap((Map) rowBean, config.getCachedAliasComparator());
            } else {
                rowMap = (Map) rowBean;
            }
        } else if (rowBean instanceof Iterable) {
            // MapWrapper, since it implements Iterable, should be processed as a Map first.
            return writeRow((Iterable<?>) rowBean);
        } else if (rowBean instanceof Hyperlink) {
            // Hyperlink is treated as a single value.
            return writeRow(ListKit.of(rowBean), isWriteKeyAsHead);
        } else if (BeanKit.isReadableBean(rowBean.getClass())) {
            if (MapKit.isEmpty(config.getHeaderAlias())) {
                rowMap = BeanKit.beanToMap(rowBean, new LinkedHashMap<>(), false, false);
            } else {
                // If aliases exist, sort Bean data according to the order of aliases.
                rowMap = BeanKit.beanToMap(rowBean, new TreeMap<>(config.getCachedAliasComparator()), false, false);
            }
        } else {
            // Other types are converted to string and output by default.
            return writeRow(ListKit.of(rowBean), isWriteKeyAsHead);
        }
        return writeRow(rowMap, isWriteKeyAsHead);
    }

    /**
     * Writes a {@link Map} to Excel. If {@code isWriteKeyAsHead} is {@code true}, two rows are written: the Map's keys
     * as the first row, and its values as the second row. Otherwise, only the values are written as a single row. If
     * {@code rowMap} is empty (including {@code null}), an empty row is written.
     *
     * @param rowMap           The Map to write. If empty (including {@code null}), an empty row is written.
     * @param isWriteKeyAsHead {@code true} to write two rows (keys as header, values as data), {@code false} to write
     *                         only values as a single row.
     * @return This {@code SheetDataWriter} instance, for chaining.
     */
    public SheetDataWriter writeRow(final Map<?, ?> rowMap, final boolean isWriteKeyAsHead) {
        if (MapKit.isEmpty(rowMap)) {
            // If the data to write is null or empty, skip the current row.
            passAndGet();
            return this;
        }

        final Table<?, ?, ?> aliasTable = this.config.aliasTable(rowMap);
        if (isWriteKeyAsHead) {
            // Write header row and record the relationship between header aliases and column numbers.
            writeHeaderRow(aliasTable.columnKeys());
            // Record original data key and corresponding column number for alias.
            int i = 0;
            for (final Object key : aliasTable.rowKeySet()) {
                this.headerLocationCache.putIfAbsent(StringKit.toString(key), i);
                i++;
            }
        }

        // If the header row has already been written, write values according to the header row's positions.
        if (MapKit.isNotEmpty(this.headerLocationCache)) {
            final Row row = RowKit.getOrCreateRow(this.sheet, this.currentRow.getAndIncrement());
            final CellEditor cellEditor = this.config.getCellEditor();
            Integer columnIndex;
            for (final Table.Cell<?, ?, ?> cell : aliasTable) {
                columnIndex = getColumnIndex(cell);
                if (null != columnIndex) {
                    CellKit.setCellValue(
                            CellKit.getOrCreateCell(row, columnIndex),
                            cell.getValue(),
                            this.styleSet,
                            false,
                            cellEditor);
                }
            }
        } else {
            writeRow(aliasTable.values());
        }
        return this;
    }

    /**
     * Writes a header row. Header data does not apply aliases. This method only writes data to the Workbook's Sheet and
     * does not flush to a file. The starting row for writing is the current row number, which can be obtained using
     * {@link #getCurrentRow()}. The current row number automatically increments based on the number of rows written.
     *
     * @param rowData The data for the header row.
     * @return This {@code SheetDataWriter} instance, for chaining.
     */
    public SheetDataWriter writeHeaderRow(final Iterable<?> rowData) {
        final int rowNum = this.currentRow.getAndIncrement();
        final Row row = this.config.insertRow ? this.sheet.createRow(rowNum)
                : RowKit.getOrCreateRow(this.sheet, rowNum);

        final Map<String, Integer> headerLocationCache = new LinkedHashMap<>();
        final CellEditor cellEditor = this.config.getCellEditor();
        int i = 0;
        Cell cell;
        for (final Object value : rowData) {
            cell = CellKit.getOrCreateCell(row, i);
            CellKit.setCellValue(cell, value, this.styleSet, true, cellEditor);
            headerLocationCache.put(StringKit.toString(value), i);
            i++;
        }
        return setHeaderLocationCache(headerLocationCache);
    }

    /**
     * Writes a row of data. This method only writes data to the Workbook's Sheet and does not flush to a file. The
     * starting row for writing is the current row number, which can be obtained using {@link #getCurrentRow()}. The
     * current row number automatically increments based on the number of rows written.
     *
     * @param rowData The data for the row.
     * @return This {@code SheetDataWriter} instance, for chaining.
     */
    public SheetDataWriter writeRow(final Iterable<?> rowData) {
        final int rowNum = this.currentRow.getAndIncrement();
        final Row row = this.config.insertRow ? this.sheet.createRow(rowNum)
                : RowKit.getOrCreateRow(this.sheet, rowNum);
        RowKit.writeRow(row, rowData, this.styleSet, false, this.config.getCellEditor());
        return this;
    }

    /**
     * Gets the current row number.
     *
     * @return The current row number (0-based).
     */
    public int getCurrentRow() {
        return this.currentRow.get();
    }

    /**
     * Sets the current row number.
     *
     * @param rowIndex The row number to set.
     * @return This {@code SheetDataWriter} instance, for chaining.
     */
    public SheetDataWriter setCurrentRow(final int rowIndex) {
        this.currentRow.set(rowIndex);
        return this;
    }

    /**
     * Skips the current row and gets the next row number.
     *
     * @return The next row number.
     */
    public int passAndGet() {
        return this.currentRow.incrementAndGet();
    }

    /**
     * Skips a specified number of rows and gets the current row number.
     *
     * @param rowNum The number of rows to skip.
     * @return The current row number after skipping.
     */
    public int passRowsAndGet(final int rowNum) {
        return this.currentRow.addAndGet(rowNum);
    }

    /**
     * Resets the current row number to 0.
     *
     * @return This {@code SheetDataWriter} instance, for chaining.
     */
    public SheetDataWriter resetRow() {
        this.currentRow.set(0);
        return this;
    }

    /**
     * Finds the column number corresponding to a header or header alias.
     *
     * @param cell The alias table, where rowKey is the original name and columnKey is the alias.
     * @return The column number, or {@code null} if not found.
     */
    private Integer getColumnIndex(final Table.Cell<?, ?, ?> cell) {
        // First, look for the column number corresponding to the original name.
        Integer location = this.headerLocationCache.get(StringKit.toString(cell.getRowKey()));
        if (null == location) {
            // If not found, look for the column number corresponding to the alias.
            location = this.headerLocationCache.get(StringKit.toString(cell.getColumnKey()));
        }
        return location;
    }

}
