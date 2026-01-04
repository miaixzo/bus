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
package org.miaixz.bus.office.excel.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.xyz.RowKit;

/**
 * Reads an {@link Sheet} into a list of maps.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapSheetReader extends AbstractSheetReader<List<Map<Object, Object>>> {

    /**
     * The row index where the header is located.
     */
    private final int headerRowIndex;

    /**
     * Constructs a new {@code MapSheetReader}.
     *
     * @param headerRowIndex The row index where the header is located. If the header row is in the middle of the
     *                       content rows to be read, this row will be ignored as data.
     * @param startRowIndex  The starting row index (inclusive, 0-based).
     * @param endRowIndex    The ending row index (inclusive, 0-based).
     */
    public MapSheetReader(final int headerRowIndex, final int startRowIndex, final int endRowIndex) {
        super(startRowIndex, endRowIndex);
        this.headerRowIndex = headerRowIndex;
    }

    /**
     * Reads the sheet and converts each row into a map with header keys.
     *
     * @param sheet The {@link Sheet} to read.
     * @return A list of maps, where each map represents a row with header keys.
     */
    @Override
    public List<Map<Object, Object>> read(final Sheet sheet) {
        // Boundary check.
        final int firstRowNum = sheet.getFirstRowNum();
        final int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum < 0) {
            return ListKit.empty();
        }

        if (headerRowIndex < firstRowNum) {
            throw new IndexOutOfBoundsException(StringKit
                    .format("Header row index {} is lower than first row index {}.", headerRowIndex, firstRowNum));
        } else if (headerRowIndex > lastRowNum) {
            throw new IndexOutOfBoundsException(StringKit
                    .format("Header row index {} is greater than last row index {}.", headerRowIndex, lastRowNum));
        }

        int startRowIndex = this.cellRangeAddress.getFirstRow();
        if (startRowIndex > lastRowNum) {
            // If only header row exists, starting row is 1, header row (last row number is 0).
            return ListKit.empty();
        }
        // Read starting row (inclusive).
        startRowIndex = Math.max(startRowIndex, firstRowNum);
        // Read ending row (inclusive).
        final int endRowIndex = Math.min(this.cellRangeAddress.getLastRow(), lastRowNum);

        // Read header.
        final List<Object> headerList = this.config.aliasHeader(readRow(sheet, headerRowIndex));

        final List<Map<Object, Object>> result = new ArrayList<>(endRowIndex - startRowIndex + 1);
        final boolean ignoreEmptyRow = this.config.isIgnoreEmptyRow();
        List<Object> rowList;
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            // Skip header row.
            if (i != headerRowIndex) {
                rowList = readRow(sheet, i);
                if (CollKit.isNotEmpty(rowList) || !ignoreEmptyRow) {
                    result.add(IteratorKit.toMap(headerList, rowList, true));
                }
            }
        }
        return result;
    }

    /**
     * Reads a single row of data.
     *
     * @param sheet    The {@link Sheet}.
     * @param rowIndex The row index, 0-based.
     * @return A list of data for the row.
     */
    private List<Object> readRow(final Sheet sheet, final int rowIndex) {
        return RowKit.readRow(sheet.getRow(rowIndex), this.config.getCellEditor());
    }

}
