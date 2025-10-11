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
package org.miaixz.bus.office.excel.sax;

import java.util.ArrayList;
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
import org.miaixz.bus.office.excel.xyz.ExcelSaxKit;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler for reading content within the sheetData tag.
 * 
 * <pre>
 * &lt;sheetData&gt;&lt;/sheetData&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetDataSaxHandler extends DefaultHandler {

    /** The content of the last parsed element. */
    private final StringBuilder lastContent = new StringBuilder();
    /** Configuration item: whether to align data by padding with null cells at the end of a row. */
    private final boolean padCellAtEndOfRow;
    /** The content of the last parsed formula element. */
    private final StringBuilder lastFormula = new StringBuilder();
    /** Row handler. */
    protected RowHandler rowHandler;
    /** The cell format table, corresponding to style.xml. */
    protected StylesTable stylesTable;
    /** The shared strings table for Excel 2007, corresponding to sharedString.xml. */
    protected SharedStrings sharedStrings;
    /** The 0-based index of the sheet. */
    protected int sheetIndex;
    /** The current non-empty row index. */
    protected int index;
    /** The current column index. */
    private int curCell;
    /** The data type of the cell. */
    private CellDataType cellDataType;
    /** The current row number, 0-based. */
    private long rowNumber;
    /** The current cell coordinate, e.g., A1, B5. */
    private String curCoordinate;
    /** The name of the current element. */
    private ElementName curElementName;
    /** The coordinate of the previous cell. */
    private String preCoordinate;
    /** The maximum cell coordinate in a row. */
    private String maxCellCoordinate;
    /** The cell style. */
    private XSSFCellStyle xssfCellStyle;
    /** The format string stored in the cell, the value of the formatCode attribute of numFmt. */
    private String numFmtString;
    /** Whether currently inside a sheetData tag. The SAX parser only parses content within this tag. */
    private boolean isInSheetData;
    /** Stores the cell elements for each row. */
    private List<Object> rowCellList = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param rowHandler        The row handler.
     * @param padCellAtEndOfRow Whether to align data by padding with null cells at the end of a row.
     */
    public SheetDataSaxHandler(final RowHandler rowHandler, final boolean padCellAtEndOfRow) {
        this.rowHandler = rowHandler;
        this.padCellAtEndOfRow = padCellAtEndOfRow;
    }

    /**
     * Sets the row handler.
     *
     * @param rowHandler The row handler.
     */
    public void setRowHandler(final RowHandler rowHandler) {
        this.rowHandler = rowHandler;
    }

    /**
     * Callback method for handling the start of an XML element.
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
     * @param attributes The attribute list.
     */
    private void startRow(final Attributes attributes) {
        final String rValue = AttributeName.r.getValue(attributes);
        this.rowNumber = (null == rValue) ? -1 : Long.parseLong(rValue) - 1;
    }

    /**
     * Starts processing a new cell.
     *
     * @param attributes The attribute list.
     */
    private void startCell(final Attributes attributes) {
        // Get the current cell coordinate.
        final String tempCurCoordinate = AttributeName.r.getValue(attributes);
        // If the previous coordinate is null, set it to "@". 'A' is the first column with ASCII 65, so the previous one
        // is '@' with ASCII 64.
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

        rowHandler.handle(sheetIndex, rowNumber, rowCellList);

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

        final String contentStr = StringKit.trim(lastContent);
        final Object value;
        if (this.lastFormula.length() > 0) {
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
        addCellValue(curCell++, value);
    }

    /**
     * Adds a value to a specified column in the row.
     *
     * @param index The position.
     * @param value The value.
     */
    private void addCellValue(final int index, final Object value) {
        this.rowCellList.add(index, value);
        this.rowHandler.handleCell(this.sheetIndex, this.rowNumber, index, value, this.xssfCellStyle);
    }

    /**
     * Fills empty cells. If the previous cell is greater than the current one, no filling is needed.
     *
     * @param preCoordinate The coordinate of the previous cell.
     * @param curCoordinate The coordinate of the current cell.
     * @param isEnd         Whether this is the last cell.
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
     * Sets the cell type.
     *
     * @param attributes The attributes.
     */
    private void setCellType(final Attributes attributes) {
        // Value of numFmtString
        numFmtString = Normal.EMPTY;
        this.cellDataType = CellDataType.of(AttributeName.t.getValue(attributes));

        // Get the xf index of the cell, corresponding to the xf sub-element of cellXfs in style.xml.
        if (null != this.stylesTable) {
            final String xfIndexStr = AttributeName.s.getValue(attributes);
            if (null != xfIndexStr) {
                this.xssfCellStyle = stylesTable.getStyleAt(Integer.parseInt(xfIndexStr));
                // The index of the cell's storage format, corresponding to the sub-element index of numFmts in
                // style.xml.
                final int numFmtIndex = xssfCellStyle.getDataFormat();
                this.numFmtString = ObjectKit.defaultIfNull(
                        xssfCellStyle.getDataFormatString(),
                        () -> BuiltinFormats.getBuiltinFormat(numFmtIndex));

                // Date-formatted cells may not have a 't' element.
                if ((CellDataType.NUMBER == this.cellDataType || CellDataType.NULL == this.cellDataType)
                        && ExcelSaxKit.isDateFormat(numFmtIndex, numFmtString)) {
                    cellDataType = CellDataType.DATE;
                }
            }
        }
    }
}
