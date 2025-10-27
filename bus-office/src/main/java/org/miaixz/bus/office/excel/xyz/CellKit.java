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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.cell.MergedCell;
import org.miaixz.bus.office.excel.cell.NullCell;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.cell.editors.TrimEditor;
import org.miaixz.bus.office.excel.cell.setters.CellSetter;
import org.miaixz.bus.office.excel.cell.setters.CellSetterFactory;
import org.miaixz.bus.office.excel.cell.values.CompositeCellValue;
import org.miaixz.bus.office.excel.style.StyleSet;

/**
 * Utility class for cell operations in Excel tables.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CellKit {

    /**
     * Gets the value of a cell.
     *
     * @param cell The {@link Cell} object.
     * @return The cell value, which can be of type Date, Double, Boolean, or String.
     */
    public static Object getCellValue(final Cell cell) {
        return getCellValue(cell, false);
    }

    /**
     * Gets the value of a cell.
     *
     * @param cell            The {@link Cell} object.
     * @param isTrimCellValue If the cell type is String, whether to trim leading and trailing whitespace.
     * @return The cell value, which can be of type Date, Double, Boolean, or String.
     */
    public static Object getCellValue(final Cell cell, final boolean isTrimCellValue) {
        if (null == cell) {
            return null;
        }
        return getCellValue(cell, cell.getCellType(), isTrimCellValue);
    }

    /**
     * Gets the value of a cell with a custom cell editor.
     *
     * @param cell       The {@link Cell} object.
     * @param cellEditor The cell editor. This editor can be used to customize cell values.
     * @return The cell value, which can be of type Date, Double, Boolean, or String.
     */
    public static Object getCellValue(final Cell cell, final CellEditor cellEditor) {
        return getCellValue(cell, null, cellEditor);
    }

    /**
     * Gets the value of a cell with a specified cell type and trim option.
     *
     * @param cell            The {@link Cell} object.
     * @param cellType        The {@link CellType} enum for the cell value type.
     * @param isTrimCellValue If the cell type is String, whether to trim leading and trailing whitespace.
     * @return The cell value, which can be of type Date, Double, Boolean, or String.
     */
    public static Object getCellValue(final Cell cell, final CellType cellType, final boolean isTrimCellValue) {
        return getCellValue(cell, cellType, isTrimCellValue ? new TrimEditor() : null);
    }

    /**
     * Gets the value of a cell with a specified cell type and custom cell editor. If the cell value is in number
     * format, it checks if it has a decimal part. If not, it returns a Long type; otherwise, it returns a Double type.
     *
     * @param cell       The {@link Cell} object.
     * @param cellType   The {@link CellType} enum for the cell value type. If {@code null}, the cell's own type is
     *                   used.
     * @param cellEditor The cell editor. This editor can be used to customize cell values.
     * @return The cell value, which can be of type Date, Double, Boolean, or String.
     */
    public static Object getCellValue(final Cell cell, final CellType cellType, final CellEditor cellEditor) {
        return CompositeCellValue.of(cell, cellType, cellEditor).getValue();
    }

    /**
     * Sets the value of a cell. Automatically matches the style based on the provided {@link StyleSet}. When it is a
     * header style, the header style is assigned by default, but if the header contains numeric, date, or other types,
     * it will be set according to the numeric or date style.
     *
     * @param cell       The cell to set the value for.
     * @param value      The value to set.
     * @param styleSet   The cell style set, including date and other styles. {@code null} indicates no styling.
     * @param isHeader   {@code true} if this is a header cell, {@code false} otherwise.
     * @param cellEditor The cell editor, which can modify cell values or cells. {@code null} indicates no editing.
     */
    public static void setCellValue(
            final Cell cell,
            final Object value,
            final StyleSet styleSet,
            final boolean isHeader,
            final CellEditor cellEditor) {
        if (null == cell) {
            return;
        }

        CellStyle cellStyle = null;
        if (null != styleSet) {
            cellStyle = styleSet.getStyleFor(new CellReference(cell), value, isHeader);
        }

        setCellValue(cell, value, cellStyle, cellEditor);
    }

    /**
     * Sets the value of a cell. Automatically matches the style based on the provided {@link StyleSet}. When it is a
     * header style, the header style is assigned by default, but if the header contains numeric, date, or other types,
     * it will be set according to the numeric or date style.
     *
     * @param cell       The cell to set the value for.
     * @param value      The value to set.
     * @param style      Custom style. {@code null} indicates no styling.
     * @param cellEditor The cell editor, which can modify cell values or cells. {@code null} indicates no editing.
     */
    public static void setCellValue(
            final Cell cell,
            final Object value,
            final CellStyle style,
            final CellEditor cellEditor) {
        cell.setCellStyle(style);
        setCellValue(cell, value, cellEditor);
    }

    /**
     * Sets the value of a cell. Automatically matches the style based on the provided {@link StyleSet}. When it is a
     * header style, the header style is assigned by default, but if the header contains numeric, date, or other types,
     * it will be set according to the numeric or date style.
     *
     * @param cell       The cell to set the value for.
     * @param value      The value or {@link CellSetter} to set.
     * @param cellEditor The cell editor, which can modify cell values or cells. {@code null} indicates no editing.
     */
    public static void setCellValue(final Cell cell, Object value, final CellEditor cellEditor) {
        if (null == cell) {
            return;
        }

        if (null != cellEditor) {
            value = cellEditor.edit(cell, value);
        }

        setCellValue(cell, value);
    }

    /**
     * Sets the value of a cell. Automatically matches the style based on the provided {@link StyleSet}. When it is a
     * header style, the header style is assigned by default, but if the header contains numeric, date, or other types,
     * it will be set according to the numeric or date style.
     *
     * @param cell  The cell to set the value for.
     * @param value The value or {@link CellSetter} to set.
     */
    public static void setCellValue(final Cell cell, final Object value) {
        if (null == cell) {
            return;
        }

        // When writing data in BigWriter (SXSSF) mode, cell values are direct values, not reference values (is tag).
        // However, when editing with ExcelWriter (XSSF), reference values are written, causing invalidation.
        // The approach here is to clear the cell value first, then write.
        if (CellType.BLANK != cell.getCellType()) {
            cell.setBlank();
        }

        CellSetterFactory.createCellSetter(value).setValue(cell);
    }

    /**
     * Gets an existing cell or creates a new one at the specified coordinates.
     *
     * @param sheet The {@link Sheet} object.
     * @param x     The X-coordinate (column index), 0-based.
     * @param y     The Y-coordinate (row index), 0-based.
     * @return The {@link Cell} at the specified coordinates.
     */
    public static Cell getOrCreateCell(final Sheet sheet, final int x, final int y) {
        return getCell(sheet, x, y, true);
    }

    /**
     * Gets the cell at the specified coordinates. If {@code isCreateIfNotExist} is {@code false}, it returns
     * {@code null} if the cell does not exist.
     *
     * @param sheet              The {@link Sheet} object.
     * @param x                  The X-coordinate (column index), 0-based.
     * @param y                  The Y-coordinate (row index), 0-based.
     * @param isCreateIfNotExist {@code true} to create the cell if it does not exist, {@code false} otherwise.
     * @return The {@link Cell} at the specified coordinates.
     */
    public static Cell getCell(final Sheet sheet, final int x, final int y, final boolean isCreateIfNotExist) {
        final Row row = isCreateIfNotExist ? RowKit.getOrCreateRow(sheet, y) : sheet.getRow(y);
        if (null != row) {
            return isCreateIfNotExist ? getOrCreateCell(row, x) : row.getCell(x);
        }
        return null;
    }

    /**
     * Gets the cell. If the cell does not exist, it returns a {@link NullCell}.
     *
     * @param row       The Excel row.
     * @param cellIndex The column index.
     * @return The {@link Cell} object.
     */
    public static Cell getCell(final Row row, final int cellIndex) {
        if (null == row) {
            return null;
        }
        final Cell cell = row.getCell(cellIndex);
        if (null == cell) {
            return new NullCell(row, cellIndex);
        }
        return cell;
    }

    /**
     * Gets an existing cell or creates a new one at the specified column index within the given row.
     *
     * @param row       The Excel row.
     * @param cellIndex The column index.
     * @return The {@link Cell} at the specified index.
     */
    public static Cell getOrCreateCell(final Row row, final int cellIndex) {
        if (null == row) {
            return null;
        }
        Cell cell = row.getCell(cellIndex);
        if (null == cell) {
            cell = row.createCell(cellIndex);
        }
        return cell;
    }

    /**
     * Checks if the specified cell is a merged cell.
     *
     * @param sheet       The {@link Sheet} object.
     * @param locationRef The cell address identifier, e.g., A11, B5.
     * @return {@code true} if it is a merged cell, {@code false} otherwise.
     */
    public static boolean isMergedRegion(final Sheet sheet, final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return isMergedRegion(sheet, cellReference.getCol(), cellReference.getRow());
    }

    /**
     * Checks if the specified cell is a merged cell.
     *
     * @param cell The {@link Cell} object.
     * @return {@code true} if it is a merged cell, {@code false} otherwise.
     */
    public static boolean isMergedRegion(final Cell cell) {
        return isMergedRegion(cell.getSheet(), cell.getColumnIndex(), cell.getRowIndex());
    }

    /**
     * Checks if the specified cell is a merged cell.
     *
     * @param sheet The {@link Sheet} object.
     * @param x     The column index, 0-based.
     * @param y     The row index, 0-based.
     * @return {@code true} if it is a merged cell, {@code false} otherwise.
     */
    public static boolean isMergedRegion(final Sheet sheet, final int x, final int y) {
        final int sheetMergeCount = sheet.getNumMergedRegions();
        CellRangeAddress ca;
        for (int i = 0; i < sheetMergeCount; i++) {
            ca = sheet.getMergedRegion(i);
            if (y >= ca.getFirstRow() && y <= ca.getLastRow() && x >= ca.getFirstColumn() && x <= ca.getLastColumn()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Merges cells. Can merge rows and columns based on the set values.
     *
     * @param sheet            The sheet object.
     * @param cellRangeAddress The range of cells to merge, defining the starting and ending rows and columns.
     * @return The index of the merged region.
     */
    public static int mergingCells(final Sheet sheet, final CellRangeAddress cellRangeAddress) {
        return mergingCells(sheet, cellRangeAddress, null);
    }

    /**
     * Merges cells. Can merge rows and columns based on the set values.
     *
     * @param sheet            The sheet object.
     * @param cellRangeAddress The range of cells to merge, defining the starting and ending rows and columns.
     * @param cellStyle        The cell style. Only border styles are extracted. {@code null} indicates no styling.
     * @return The index of the merged region.
     */
    public static int mergingCells(
            final Sheet sheet,
            final CellRangeAddress cellRangeAddress,
            final CellStyle cellStyle) {
        if (cellRangeAddress.getNumberOfCells() <= 1) {
            // Not a merged cell, no need to merge.
            return -1;
        }
        StyleKit.setBorderStyle(sheet, cellRangeAddress, cellStyle);
        return sheet.addMergedRegion(cellRangeAddress);
    }

    /**
     * Gets the first cell of a merged region. The provided cell can be any cell within the merged region.
     *
     * @param cell The {@link Cell} object.
     * @return The first cell of the merged region, or the cell itself if it's not part of a merged region.
     */
    public static Cell getFirstCellOfMerged(final Cell cell) {
        if (null == cell) {
            return null;
        }

        final MergedCell mergedCell = getMergedCell(cell.getSheet(), cell.getColumnIndex(), cell.getRowIndex());
        if (null != mergedCell) {
            return mergedCell.getFirst();
        }

        return cell;
    }

    /**
     * Gets the merged cell information. The x,y coordinates (column, row) can be any cell within the merged region.
     *
     * @param sheet The {@link Sheet} object.
     * @param x     The column index, 0-based, can be any column within the merged cell range.
     * @param y     The row index, 0-based, can be any row within the merged cell range.
     * @return The {@link MergedCell} object. If it's not a merged cell, returns the cell corresponding to the
     *         coordinates.
     */
    public static MergedCell getMergedCell(final Sheet sheet, final int x, final int y) {
        if (null == sheet) {
            return null;
        }

        final CellRangeAddress mergedRegion = SheetKit.getMergedRegion(sheet, x, y);
        if (null != mergedRegion) {
            return MergedCell
                    .of(getCell(sheet, mergedRegion.getFirstColumn(), mergedRegion.getFirstRow(), false), mergedRegion);
        }
        return null;
    }

    /**
     * Adds a comment to a specific cell.
     *
     * @param cell          The cell.
     * @param commentText   The content of the comment.
     * @param commentAuthor The author of the comment.
     */
    public static void setComment(final Cell cell, final String commentText, final String commentAuthor) {
        setComment(cell, commentText, commentAuthor, null);
    }

    /**
     * Adds a comment to a specific cell.
     *
     * @param cell          The cell.
     * @param commentText   The content of the comment.
     * @param commentAuthor The author of the comment. {@code null} indicates no author.
     * @param anchor        The position, size, and other information of the comment. {@code null} indicates using
     *                      default settings.
     */
    public static void setComment(
            final Cell cell,
            final String commentText,
            final String commentAuthor,
            ClientAnchor anchor) {
        final Sheet sheet = cell.getSheet();
        final CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        if (anchor == null) {
            anchor = factory.createClientAnchor();
            // Default position, to the right of the commented cell.
            anchor.setCol1(cell.getColumnIndex() + 1);
            anchor.setCol2(cell.getColumnIndex() + 3);
            anchor.setRow1(cell.getRowIndex());
            anchor.setRow2(cell.getRowIndex() + 2);
            // Adaptive resizing.
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        }

        final Comment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
        // Fix the issue of incorrect positioning when the address is not set in XSSFCell.
        comment.setAddress(cell.getAddress());
        comment.setString(factory.createRichTextString(commentText));
        if (null != commentAuthor) {
            comment.setAuthor(commentAuthor);
        }
        cell.setCellComment(comment);
    }

    /**
     * Removes the specified cell.
     *
     * @param cell The cell to remove.
     */
    public static void remove(final Cell cell) {
        if (null != cell) {
            cell.getRow().removeCell(cell);
        }
    }

    /**
     * Creates a {@link CellRangeAddress} object from a string representation. This method provides a convenient way to
     * create a {@link CellRangeAddress} object by directly using a string reference. The string reference should
     * conform to the conventional representation of cell ranges in Excel, such as "B1" or "A1:B2".
     *
     * @param ref The string representing the cell range, in the format "A1:B2".
     * @return A {@link CellRangeAddress} object created from the given string.
     * @see CellRangeAddress#valueOf(String)
     */
    public static CellRangeAddress of(final String ref) {
        return CellRangeAddress.valueOf(ref);
    }

    /**
     * Creates a {@link CellRangeAddress} object based on the given row and column indices. This method provides a
     * concise way to represent a specific area in an Excel table, defined by starting and ending rows and columns.
     *
     * @param firstRow The index of the starting row, 0-based.
     * @param lastRow  The index of the ending row, 0-based, must be greater than or equal to {@code firstRow}.
     * @param firstCol The index of the starting column, 0-based.
     * @param lastCol  The index of the ending column, 0-based, must be greater than or equal to {@code firstCol}.
     * @return A new {@link CellRangeAddress} object representing the area defined by the parameters.
     */
    public static CellRangeAddress of(final int firstRow, final int lastRow, final int firstCol, final int lastCol) {
        return new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
    }

    /**
     * Creates a single-row {@link CellRangeAddress} object based on the given row and ending column index, starting
     * from the first column. This method provides a concise way to represent a specific area in an Excel table, defined
     * by starting and ending rows and columns.
     *
     * @param row     The index of the row, 0-based.
     * @param lastCol The index of the ending column, 0-based, must be greater than or equal to 0.
     * @return A new {@link CellRangeAddress} object representing the area defined by the parameters.
     */
    public static CellRangeAddress ofSingleRow(final int row, final int lastCol) {
        return ofSingleRow(row, 0, lastCol);
    }

    /**
     * Creates a single-row {@link CellRangeAddress} object based on the given row and column indices. This method
     * provides a concise way to represent a specific area in an Excel table, defined by starting and ending rows and
     * columns.
     *
     * @param row      The index of the row, 0-based.
     * @param firstCol The index of the starting column, 0-based.
     * @param lastCol  The index of the ending column, 0-based, must be greater than or equal to {@code firstCol}.
     * @return A new {@link CellRangeAddress} object representing the area defined by the parameters.
     */
    public static CellRangeAddress ofSingleRow(final int row, final int firstCol, final int lastCol) {
        return of(row, row, firstCol, lastCol);
    }

    /**
     * Creates a single-column {@link CellRangeAddress} object based on the given ending row and column index, starting
     * from the first row. This method provides a concise way to represent a specific area in an Excel table, defined by
     * starting and ending rows and columns.
     *
     * @param lastRow The index of the ending row, 0-based, must be greater than or equal to 0.
     * @param col     The index of the column, 0-based.
     * @return A new {@link CellRangeAddress} object representing the area defined by the parameters.
     */
    public static CellRangeAddress ofSingleColumn(final int lastRow, final int col) {
        return ofSingleColumn(0, lastRow, col);
    }

    /**
     * Creates a single-column {@link CellRangeAddress} object based on the given row and column indices. This method
     * provides a concise way to represent a specific area in an Excel table, defined by starting and ending rows and
     * columns.
     *
     * @param firstRow The index of the starting row, 0-based.
     * @param lastRow  The index of the ending row, 0-based, must be greater than or equal to {@code firstRow}.
     * @param col      The index of the column, 0-based.
     * @return A new {@link CellRangeAddress} object representing the area defined by the parameters.
     */
    public static CellRangeAddress ofSingleColumn(final int firstRow, final int lastRow, final int col) {
        return of(firstRow, lastRow, col, col);
    }

    /**
     * Gets the merged cell {@link CellRangeAddress}. If the cell is not part of a merged region, returns {@code null}.
     *
     * @param sheet       The {@link Sheet} object.
     * @param locationRef The cell address identifier, e.g., A11, B5.
     * @return The {@link CellRangeAddress} of the merged region, or {@code null} if not merged.
     */
    public static CellRangeAddress getCellRangeAddress(final Sheet sheet, final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return getCellRangeAddress(sheet, cellReference.getCol(), cellReference.getRow());
    }

    /**
     * Gets the merged cell {@link CellRangeAddress}. If the cell is not part of a merged region, returns {@code null}.
     *
     * @param cell The {@link Cell} object.
     * @return The {@link CellRangeAddress} of the merged region, or {@code null} if not merged.
     */
    public static CellRangeAddress getCellRangeAddress(final Cell cell) {
        return getCellRangeAddress(cell.getSheet(), cell.getColumnIndex(), cell.getRowIndex());
    }

    /**
     * Gets the merged cell {@link CellRangeAddress}. If the cell is not part of a merged region, returns {@code null}.
     *
     * @param sheet The {@link Sheet} object.
     * @param x     The column index, 0-based.
     * @param y     The row index, 0-based.
     * @return The {@link CellRangeAddress} of the merged region, or {@code null} if not merged.
     */
    public static CellRangeAddress getCellRangeAddress(final Sheet sheet, final int x, final int y) {
        if (sheet != null) {
            final int sheetMergeCount = sheet.getNumMergedRegions();
            CellRangeAddress ca;
            for (int i = 0; i < sheetMergeCount; i++) {
                ca = sheet.getMergedRegion(i);
                if (y >= ca.getFirstRow() && y <= ca.getLastRow() && x >= ca.getFirstColumn()
                        && x <= ca.getLastColumn()) {
                    return ca;
                }
            }
        }
        return null;
    }

    /**
     * Converts a sheet column index to its Excel column name representation.
     *
     * @param index The column index, 0-based.
     * @return The Excel column name (e.g., 0 -> A; 1 -> B; 26 -> AA).
     */
    public static String indexToColName(int index) {
        if (index < 0) {
            return null;
        }
        final StringBuilder colName = StringKit.builder();
        do {
            if (!colName.isEmpty()) {
                index--;
            }
            final int remainder = index % 26;
            colName.append((char) (remainder + 'A'));
            index = (index - remainder) / 26;
        } while (index > 0);
        return colName.reverse().toString();
    }

    /**
     * Converts an Excel column name to its 0-based column index.
     *
     * @param colName The column name, starting from A.
     * @return The 0-based column index (e.g., A1 -> 0; B1 -> 1; AA1 -> 26).
     */
    public static int colNameToIndex(final String colName) {
        final int length = colName.length();
        char c;
        int index = -1;
        for (int i = 0; i < length; i++) {
            c = Character.toUpperCase(colName.charAt(i));
            if (Character.isDigit(c)) {
                break; // Determine if the specified char value is a digit.
            }
            index = (index + 1) * 26 + (int) c - 'A';
        }
        return index;
    }

    /**
     * Converts an Excel address identifier (e.g., A11, B5) to row and column representation. For example: A11 -> col:0,
     * row:10; B5 -> col:1, row:4.
     *
     * @param locationRef The cell address identifier, e.g., A11, B5.
     * @return A {@link CellReference} object representing the coordinates.
     */
    public static CellReference toCellReference(final String locationRef) {
        return new CellReference(locationRef);
    }

}
