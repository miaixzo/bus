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
package org.miaixz.bus.office.excel.xyz;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.style.StyleSet;

/**
 * Utility class for {@link Row} operations in Excel.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RowKit {

    /**
     * Gets an existing row or creates a new one if it does not exist.
     *
     * @param sheet    The Excel sheet.
     * @param rowIndex The row index (0-based).
     * @return The {@link Row} at the specified index.
     */
    public static Row getOrCreateRow(final Sheet sheet, final int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (null == row) {
            row = sheet.createRow(rowIndex);
        }
        return row;
    }

    /**
     * Reads a single row.
     *
     * @param row        The row to read.
     * @param cellEditor The cell editor to apply to each cell value. May be {@code null} for no editing.
     * @return A list of cell values.
     */
    public static List<Object> readRow(final Row row, final CellEditor cellEditor) {
        return readRow(row, 0, Short.MAX_VALUE, cellEditor);
    }

    /**
     * Reads a single row within a specified cell range.
     *
     * @param row                 The row to read.
     * @param startCellNumInclude The starting cell index (0-based, inclusive).
     * @param endCellNumInclude   The ending cell index (0-based, inclusive).
     * @param cellEditor          The cell editor to apply to each cell value. May be {@code null} for no editing.
     * @return A list of cell values.
     */
    public static List<Object> readRow(
            final Row row,
            final int startCellNumInclude,
            final int endCellNumInclude,
            final CellEditor cellEditor) {
        if (null == row) {
            return ListKit.empty();
        }
        final short rowLength = row.getLastCellNum();
        if (rowLength < 0) {
            return ListKit.empty();
        }

        final int size = Math.min(endCellNumInclude + 1, rowLength);
        final List<Object> cellValues = new ArrayList<>(size);
        Object cellValue;
        boolean isAllNull = true;
        for (int i = startCellNumInclude; i < size; i++) {
            cellValue = CellKit.getCellValue(CellKit.getCell(row, i), cellEditor);
            isAllNull &= ObjectKit.isEmptyIfString(cellValue);
            cellValues.add(cellValue);
        }

        if (isAllNull) {
            // If every element is null, it is defined as an empty row.
            return ListKit.empty();
        }
        return cellValues;
    }

    /**
     * Writes a single row of data without styling and not as a header.
     *
     * @param row        The row to write to.
     * @param rowData    The data for the row.
     * @param cellEditor The cell editor to modify cell values or cells. {@code null} indicates no editing.
     */
    public static void writeRow(final Row row, final Iterable<?> rowData, final CellEditor cellEditor) {
        writeRow(row, rowData, null, false, cellEditor);
    }

    /**
     * Writes a single row of data.
     *
     * @param row        The row to write to.
     * @param rowData    The data for the row.
     * @param styleSet   The cell style set, including date styles. {@code null} indicates no specific styling.
     * @param isHeader   {@code true} if this is a header row, {@code false} otherwise.
     * @param cellEditor The cell editor to modify cell values or cells. {@code null} indicates no editing.
     */
    public static void writeRow(
            final Row row,
            final Iterable<?> rowData,
            final StyleSet styleSet,
            final boolean isHeader,
            final CellEditor cellEditor) {
        int i = 0;
        Cell cell;
        for (final Object value : rowData) {
            cell = row.createCell(i);
            CellKit.setCellValue(cell, value, styleSet, isHeader, cellEditor);
            i++;
        }
    }

    /**
     * Inserts rows into the worksheet.
     *
     * @param sheet        The worksheet.
     * @param startRow     The starting row index (0-based) for insertion.
     * @param insertNumber The number of rows to insert.
     */
    public static void insertRow(final Sheet sheet, final int startRow, final int insertNumber) {
        if (insertNumber <= 0) {
            return;
        }
        // The row at the insertion position. If the row to be inserted does not exist, a new row is created.
        final Row sourceRow = getOrCreateRow(sheet, startRow);
        // Shift rows down from the insertion row to the last row.
        sheet.shiftRows(startRow, sheet.getLastRowNum(), insertNumber, true, false);

        // Fill in the empty rows left after shifting.
        IntStream.range(startRow, startRow + insertNumber).forEachOrdered(i -> {
            final Row row = sheet.createRow(i);
            row.setHeightInPoints(sourceRow.getHeightInPoints());
            final short lastCellNum = sourceRow.getLastCellNum();
            IntStream.range(0, lastCellNum).forEachOrdered(j -> {
                final Cell cell = row.createCell(j);
                cell.setCellStyle(sourceRow.getCell(j).getCellStyle());
            });
        });
    }

    /**
     * Deletes the specified row from the worksheet. This method fixes the issue where {@code sheet.shiftRows} splits
     * merged cells when deleting rows.
     *
     * @param row The row to be deleted.
     * @see <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=56454">sheet.shiftRows bug</a>
     */
    public static void removeRow(final Row row) {
        if (row == null) {
            return;
        }
        final int rowIndex = row.getRowNum();
        final Sheet sheet = row.getSheet();
        final int lastRow = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRow) {
            final List<CellRangeAddress> updateMergedRegions = new ArrayList<>();
            // Find merged cells that need adjustment.
            IntStream.range(0, sheet.getNumMergedRegions()).forEach(i -> {
                final CellRangeAddress mr = sheet.getMergedRegion(i);
                if (!mr.containsRow(rowIndex)) {
                    return;
                }
                // If it shrinks to a single cell after reduction, delete the merged cell.
                if (mr.getFirstRow() == mr.getLastRow() - 1 && mr.getFirstColumn() == mr.getLastColumn()) {
                    return;
                }
                updateMergedRegions.add(mr);
            });

            // Shift rows up.
            sheet.shiftRows(rowIndex + 1, lastRow, -1);

            // Find merged cells that were in the deleted row.
            final List<Integer> removeMergedRegions = IntStream.range(0, sheet.getNumMergedRegions())
                    .filter(
                            i -> updateMergedRegions.stream()
                                    .anyMatch(umr -> CellRangeUtil.contains(umr, sheet.getMergedRegion(i))))
                    .boxed().collect(Collectors.toList());

            sheet.removeMergedRegions(removeMergedRegions);
            updateMergedRegions.forEach(mr -> {
                mr.setLastRow(mr.getLastRow() - 1);
                sheet.addMergedRegion(mr);
            });
            sheet.validateMergedRegions();
        }
        if (rowIndex == lastRow) {
            final Row removingRow = sheet.getRow(rowIndex);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }

}
