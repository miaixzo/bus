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
package org.miaixz.bus.office.excel.sax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.cell.values.FormulaCellValue;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.miaixz.bus.office.excel.ExcelSaxKit;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler for reading content within the sheetData tag.
 * <p>
 * This class processes the stream of SAX events for Excel XML data, specifically looking for the content inside the
 * {@code <sheetData>} element.
 * </p>
 * 
 * <pre>
 * &lt;sheetData&gt;&lt;/sheetData&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetDataSaxHandler extends DefaultHandler {

    /**
     * The content of the last parsed element (e.g., cell value).
     */
    private final StringBuilder lastContent = new StringBuilder();

    /**
     * Configuration item: whether to align data by padding with null cells at the end of a row.
     */
    private final boolean padCellAtEndOfRow;

    /**
     * Include column marks for quick O(1) lookup by column index.
     */
    private final boolean[] includeColumnMarks;

    /**
     * Maximum included column index.
     */
    private final int maxIncludeColumn;

    /**
     * The content of the last parsed formula element.
     */
    private final StringBuilder lastFormula = new StringBuilder();

    /**
     * Row handler used to process complete rows or individual cells.
     */
    protected RowHandler rowHandler;

    /**
     * The cell format table, corresponding to {@code styles.xml} in the XLSX structure.
     */
    protected StylesTable stylesTable;

    /**
     * The shared strings table for Excel 2007+, corresponding to {@code sharedStrings.xml}.
     */
    protected SharedStrings sharedStrings;

    /**
     * The 0-based index of the current sheet.
     */
    protected int rid;

    /**
     * The current row count (index of non-empty rows processed).
     */
    protected int index;

    /**
     * The current column index (0-based).
     */
    private int curCell;

    /**
     * Cursor of includeColumns for current row.
     */
    private int includeColumnCursor;

    /**
     * The data type of the current cell.
     */
    private CellDataType cellDataType;

    /**
     * The current row number (0-based), extracted from the 'r' attribute.
     */
    private long rowNumber;

    /**
     * The current cell coordinate (e.g., "A1", "B5").
     */
    private String curCoordinate;

    /**
     * The name of the current XML element being processed.
     */
    private ElementName curElementName;

    /**
     * The coordinate of the previous cell, used for calculating empty cell padding.
     */
    private String preCoordinate;

    /**
     * The maximum cell coordinate encountered in the first row, used for padding subsequent rows if enabled.
     */
    private String maxCellCoordinate;

    /**
     * Cached current cell style (may be null).
     */
    private XSSFCellStyle xssfCellStyle;

    /**
     * Cached current cell format string.
     */
    private String numFmtString = Normal.EMPTY;

    /**
     * Cached style index to avoid repeated style lookups for adjacent cells.
     */
    private int cachedXfIndex = Integer.MIN_VALUE;

    /**
     * Cached style object for {@link #cachedXfIndex}.
     */
    private XSSFCellStyle cachedXssfCellStyle;

    /**
     * Cached format index for {@link #cachedXfIndex}.
     */
    private int cachedNumFmtIndex = -1;

    /**
     * Cached format string for {@link #cachedXfIndex}.
     */
    private String cachedNumFmtString = Normal.EMPTY;

    /**
     * Flag indicating whether the parser is currently inside a {@code <sheetData>} tag. The SAX parser only processes
     * content within this tag.
     */
    private boolean isInSheetData;

    /**
     * Container for storing cell values for the current row.
     */
    private List<Object> rowCellList = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param rowHandler        The row handler for processing data.
     * @param padCellAtEndOfRow Whether to align data by padding with null cells at the end of a row.
     */
    public SheetDataSaxHandler(final RowHandler rowHandler, final boolean padCellAtEndOfRow) {
        this(rowHandler, padCellAtEndOfRow, null);
    }

    /**
     * Constructor.
     *
     * @param rowHandler        The row handler for processing data.
     * @param padCellAtEndOfRow Whether to align data by padding with null cells at the end of a row.
     * @param includeColumns    Optional included columns (sorted unique indexes).
     */
    public SheetDataSaxHandler(final RowHandler rowHandler, final boolean padCellAtEndOfRow,
            final int[] includeColumns) {
        this.rowHandler = rowHandler;
        this.padCellAtEndOfRow = padCellAtEndOfRow;
        if (null == includeColumns || includeColumns.length == 0) {
            this.includeColumnMarks = null;
            this.maxIncludeColumn = -1;
        } else {
            int max = -1;
            for (final int column : includeColumns) {
                if (column > max) {
                    max = column;
                }
            }
            this.maxIncludeColumn = max;
            this.includeColumnMarks = new boolean[max + 1];
            Arrays.fill(this.includeColumnMarks, false);
            for (final int column : includeColumns) {
                if (column >= 0) {
                    this.includeColumnMarks[column] = true;
                }
            }
        }
    }

    /**
     * Sets the row handler.
     *
     * @param rowHandler The new row handler.
     */
    public void setRowHandler(final RowHandler rowHandler) {
        this.rowHandler = rowHandler;
    }

    /**
     * Callback method for handling the start of an XML element.
     *
     * @param uri        The Namespace URI.
     * @param localName  The local name (without prefix).
     * @param qName      The qualified name (with prefix).
     * @param attributes The attributes attached to the element.
     */
    @Override
    public void startElement(
            final String uri,
            final String localName,
            final String qName,
            final Attributes attributes) {
        if ("sheetData".equals(qName)) {
            this.isInSheetData = true;
            return;
        }

        if (!this.isInSheetData) {
            // Not in a sheetData tag, ignore parsing.
            return;
        }

        final ElementName name = ElementName.of(qName);
        this.curElementName = name;

        if (null != name) {
            switch (name) {
                case row:
                    // Start of a row
                    startRow(attributes);
                    break;

                case c:
                    // Cell element
                    startCell(attributes);
                    break;
            }
        }
    }

    /**
     * Callback method for handling the end of an XML element.
     *
     * @param uri       The Namespace URI.
     * @param localName The local name (without prefix).
     * @param qName     The qualified name (with prefix).
     */
    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        if ("sheetData".equals(qName)) {
            // End of sheetData, no more parsing of other tags.
            this.isInSheetData = false;
            return;
        }

        if (!this.isInSheetData) {
            // Not in a sheetData tag, ignore parsing.
            return;
        }

        this.curElementName = null;
        if (ElementName.c.match(qName)) { // End of a cell
            endCell();
        } else if (ElementName.row.match(qName)) { // End of a row
            endRow();
        }
        // Ignore other tags
    }

    /**
     * Callback method for processing character data inside an element.
     *
     * @param ch     The characters.
     * @param start  The start position in the character array.
     * @param length The number of characters to use from the character array.
     */
    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (!this.isInSheetData) {
            // Not in a sheetData tag, ignore parsing.
            return;
        }

        final ElementName elementName = this.curElementName;
        if (null != elementName) {
            switch (elementName) {
                case v:
                    // Get the value of the cell content.
                    lastContent.append(ch, start, length);
                    break;

                case f:
                    // Get the value of the cell formula.
                    lastFormula.append(ch, start, length);
                    break;
            }
        } else {
            // In theory, the content should be within "<v>content</v>".
            // However, some special XML content is not in a v or f tag, so we handle it here.
            lastContent.append(ch, start, length);
        }
    }

    /**
     * Starts processing a new row.
     *
     * @param attributes The attribute list associated with the row element.
     */
    private void startRow(final Attributes attributes) {
        final String rValue = AttributeName.r.getValue(attributes);
        this.rowNumber = (null == rValue) ? -1 : Long.parseLong(rValue) - 1;
        this.includeColumnCursor = 0;
    }

    /**
     * Starts processing a new cell.
     *
     * @param attributes The attribute list associated with the cell element.
     */
    private void startCell(final Attributes attributes) {
        // Get the current cell coordinate.
        final String tempCurCoordinate = AttributeName.r.getValue(attributes);
        // If the previous coordinate is null, set it to "@".
        // 'A' is the first column with ASCII 65, so the previous one is '@' with ASCII 64.
        if (preCoordinate == null) {
            preCoordinate = String.valueOf(Symbol.C_AT);
        } else {
            // If it exists, set the previous coordinate to the last cell's coordinate.
            preCoordinate = curCoordinate;
        }
        // Reset the current coordinate.
        curCoordinate = tempCurCoordinate;
        // Set the cell type.
        setCellType(attributes);

        // Clear previous data.
        lastContent.setLength(0);
        lastFormula.setLength(0);
    }

    /**
     * Ends processing a row.
     */
    private void endRow() {
        // The maximum cell coordinate is based on the first non-empty row.
        if (index == 0) {
            maxCellCoordinate = curCoordinate;
        }

        // Pad any missing cells at the end of the row.
        if (padCellAtEndOfRow && maxCellCoordinate != null) {
            padCell(curCoordinate, maxCellCoordinate, true);
        }

        if (null != this.includeColumnMarks) {
            while (includeColumnCursor <= this.maxIncludeColumn) {
                if (isIncludedColumn(includeColumnCursor)) {
                    this.rowCellList.add(null);
                }
                includeColumnCursor++;
            }
        }

        rowHandler.handle(rid, rowNumber, rowCellList);

        // End of a row.
        // Create a new list for the next row, discarding the old one.
        rowCellList = new ArrayList<>(curCell + 1);
        // Increment the row count.
        index++;
        // Reset the current cell to 0.
        curCell = 0;
        // Reset the current and previous coordinates.
        curCoordinate = null;
        preCoordinate = null;
    }

    /**
     * Ends processing a cell.
     */
    private void endCell() {
        // Pad any empty cells between the previous and current cell.
        padCell(preCoordinate, curCoordinate, false);

        final int cellIndex = curCell++;
        if (!isIncludedColumn(cellIndex)) {
            return;
        }

        final String contentStr = StringKit.trim(lastContent);
        final Object value;
        if (!this.lastFormula.isEmpty()) {
            if (CellDataType.NULL == this.cellDataType) {
                // For formulas, the default value type is number.
                this.cellDataType = CellDataType.NUMBER;
            }
            value = new FormulaCellValue(StringKit.trim(lastFormula),
                    ExcelSaxKit.getDataValue(this.cellDataType, contentStr, this.sharedStrings, this.numFmtString));
        } else {
            // The default cellDataType is NULL, not NUMBER.
            value = ExcelSaxKit.getDataValue(this.cellDataType, contentStr, this.sharedStrings, this.numFmtString);
        }
        addCellValue(cellIndex, value);
    }

    /**
     * Adds a value to current row with include-column projection applied when configured.
     *
     * @param index The column index (0-based).
     * @param value The value to add.
     */
    private void addCellValue(final int index, final Object value) {
        if (null == this.includeColumnMarks) {
            this.rowCellList.add(value);
            this.rowHandler.handleCell(this.rid, this.rowNumber, index, value, this.xssfCellStyle);
            return;
        }

        while (includeColumnCursor <= this.maxIncludeColumn && includeColumnCursor < index) {
            if (isIncludedColumn(includeColumnCursor)) {
                this.rowCellList.add(null);
            }
            includeColumnCursor++;
        }

        if (isIncludedColumn(index)) {
            this.rowCellList.add(value);
            this.rowHandler.handleCell(this.rid, this.rowNumber, index, value, this.xssfCellStyle);
            includeColumnCursor = index + 1;
        }
    }

    /**
     * Fills empty cells with {@code null}. If the previous cell is directly before the current one, no filling is
     * needed.
     *
     * @param preCoordinate The coordinate of the previous cell.
     * @param curCoordinate The coordinate of the current cell.
     * @param isEnd         Whether this is the last cell in the row.
     */
    private void padCell(final String preCoordinate, final String curCoordinate, final boolean isEnd) {
        if (null != curCoordinate && !curCoordinate.equals(preCoordinate)) {
            int len = ExcelSaxKit.countNullCell(preCoordinate, curCoordinate);
            if (isEnd) {
                len++;
            }
            while (len-- > 0) {
                addCellValue(curCell++, null);
            }
        }
    }

    /**
     * Sets the cell type based on attributes.
     *
     * @param attributes The attributes of the cell element.
     */
    private void setCellType(final Attributes attributes) {
        // Value of numFmtString
        this.numFmtString = Normal.EMPTY;
        this.xssfCellStyle = null;
        this.cellDataType = CellDataType.of(AttributeName.t.getValue(attributes));

        // Get the xf index of the cell, corresponding to the xf sub-element of cellXfs in style.xml.
        if (null != this.stylesTable) {
            final String xfIndexStr = AttributeName.s.getValue(attributes);
            if (null != xfIndexStr) {
                final int xfIndex = Integer.parseInt(xfIndexStr);
                final int numFmtIndex;
                if (xfIndex == this.cachedXfIndex) {
                    this.xssfCellStyle = this.cachedXssfCellStyle;
                    numFmtIndex = this.cachedNumFmtIndex;
                    this.numFmtString = this.cachedNumFmtString;
                } else {
                    this.xssfCellStyle = stylesTable.getStyleAt(xfIndex);
                    numFmtIndex = this.xssfCellStyle.getDataFormat();
                    this.numFmtString = ObjectKit.defaultIfNull(
                            this.xssfCellStyle.getDataFormatString(),
                            () -> BuiltinFormats.getBuiltinFormat(numFmtIndex));
                    this.cachedXfIndex = xfIndex;
                    this.cachedXssfCellStyle = this.xssfCellStyle;
                    this.cachedNumFmtIndex = numFmtIndex;
                    this.cachedNumFmtString = this.numFmtString;
                }

                // Date-formatted cells may not have a 't' element.
                if ((CellDataType.NUMBER == this.cellDataType || CellDataType.NULL == this.cellDataType)
                        && ExcelSaxKit.isDateFormat(numFmtIndex, this.numFmtString)) {
                    cellDataType = CellDataType.DATE;
                }
            }
        }
    }

    /**
     * Checks whether a column index is included when projection is configured.
     *
     * @param columnIndex Column index (0-based).
     * @return {@code true} if the column is included, {@code false} otherwise.
     */
    private boolean isIncludedColumn(final int columnIndex) {
        if (null == this.includeColumnMarks) {
            return true;
        }
        return columnIndex >= 0 && columnIndex <= this.maxIncludeColumn && this.includeColumnMarks[columnIndex];
    }

}
