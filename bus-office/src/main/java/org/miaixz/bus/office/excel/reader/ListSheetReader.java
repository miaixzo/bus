/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.office.excel.reader;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.xyz.RowKit;

/**
 * Reads an {@link Sheet} into a list of lists.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListSheetReader extends AbstractSheetReader<List<List<Object>>> {

    /**
     * Whether the first row should be treated as a header row and aliases applied.
     */
    private final boolean aliasFirstLine;

    /**
     * Constructs a new {@code ListSheetReader}.
     *
     * @param startRowIndex  The starting row index (inclusive, 0-based).
     * @param endRowIndex    The ending row index (inclusive, 0-based).
     * @param aliasFirstLine {@code true} if the first row should be treated as a header and aliases applied,
     *                       {@code false} otherwise.
     */
    public ListSheetReader(final int startRowIndex, final int endRowIndex, final boolean aliasFirstLine) {
        super(startRowIndex, endRowIndex);
        this.aliasFirstLine = aliasFirstLine;
    }

    /**
     * Reads the sheet and converts each row into a list of cell values.
     *
     * @param sheet The {@link Sheet} to read.
     * @return A list of lists, where each inner list represents a row of cell values.
     */
    @Override
    public List<List<Object>> read(final Sheet sheet) {
        final List<List<Object>> resultList = new ArrayList<>();

        final int startRowIndex = Math.max(this.cellRangeAddress.getFirstRow(), sheet.getFirstRowNum());// Read starting
                                                                                                        // row
                                                                                                        // (inclusive).
        final int endRowIndex = Math.min(this.cellRangeAddress.getLastRow(), sheet.getLastRowNum());// Read ending row
                                                                                                    // (inclusive).

        List<Object> rowList;
        final CellEditor cellEditor = this.config.getCellEditor();
        final boolean ignoreEmptyRow = this.config.isIgnoreEmptyRow();
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            rowList = RowKit.readRow(sheet.getRow(i), cellEditor);
            if (CollKit.isNotEmpty(rowList) || !ignoreEmptyRow) {
                if (aliasFirstLine && i == startRowIndex) {
                    // The first row is treated as a header row, apply aliases.
                    rowList = this.config.aliasHeader(rowList);
                }
                resultList.add(rowList);
            }
        }
        return resultList;
    }

}
