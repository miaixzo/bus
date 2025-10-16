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
package org.miaixz.bus.office.word;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * Utility class for Word table related operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DocxTable {

    /**
     * Creates an empty table with a single row.
     *
     * @param doc The {@link XWPFDocument} to which the table will be added.
     * @return The newly created {@link XWPFTable}.
     */
    public static XWPFTable createTable(final XWPFDocument doc) {
        return createTable(doc, null);
    }

    /**
     * Creates a table and populates it with data. Uses default table settings.
     *
     * @param doc  The {@link XWPFDocument} to which the table will be added.
     * @param data The data to populate the table with. Can be an {@link Iterable} of rows.
     * @return The newly created and populated {@link XWPFTable}.
     * @throws NullPointerException if {@code doc} is {@code null}.
     */
    public static XWPFTable createTable(final XWPFDocument doc, final Iterable<?> data) {
        Assert.notNull(doc, "XWPFDocument must be not null !");
        final XWPFTable table = doc.createTable();
        // A new table by default creates one row, remove it here.
        table.removeRow(0);
        return writeTable(table, data);
    }

    /**
     * Populates the given table with data.
     *
     * @param table The {@link XWPFTable} to populate.
     * @param data  The data to populate the table with. Can be an {@link Iterable} of rows.
     * @return The populated {@link XWPFTable}.
     * @throws NullPointerException if {@code table} is {@code null}.
     */
    public static XWPFTable writeTable(final XWPFTable table, final Iterable<?> data) {
        Assert.notNull(table, "XWPFTable must be not null !");
        if (IteratorKit.isEmpty(data)) {
            // If data is empty, return an empty table.
            return table;
        }

        boolean isFirst = true;
        for (final Object rowData : data) {
            writeRow(table.createRow(), rowData, isFirst);
            if (isFirst) {
                isFirst = false;
            }
        }

        return table;
    }

    /**
     * Writes a single row of data to the table.
     *
     * @param row              The {@link XWPFTableRow} to write data to.
     * @param rowBean          The data for the row. Can be an {@link Iterable}, {@link Map}, or a Bean.
     * @param isWriteKeyAsHead If {@code rowBean} is a {@link Map} or a Bean, specifies whether to write keys as
     *                         headers.
     */
    public static void writeRow(final XWPFTableRow row, final Object rowBean, final boolean isWriteKeyAsHead) {
        if (rowBean instanceof Iterable) {
            writeRow(row, (Iterable<?>) rowBean);
            return;
        }

        final Map<?, ?> rowMap;
        if (rowBean instanceof Map) {
            rowMap = (Map<?, ?>) rowBean;
        } else if (BeanKit.isWritableBean(rowBean.getClass())) {
            rowMap = BeanKit.beanToMap(rowBean, new LinkedHashMap<>(), false, false);
        } else {
            // Other types are converted to string and output by default.
            writeRow(row, ListKit.of(rowBean), isWriteKeyAsHead);
            return;
        }

        writeRow(row, rowMap, isWriteKeyAsHead);
    }

    /**
     * Writes a single row of data to the table.
     *
     * @param row              The {@link XWPFTableRow} to write data to.
     * @param rowMap           The map representing the row data.
     * @param isWriteKeyAsHead If {@code true}, writes the map keys as a header row before writing the values.
     */
    public static void writeRow(XWPFTableRow row, final Map<?, ?> rowMap, final boolean isWriteKeyAsHead) {
        if (MapKit.isEmpty(rowMap)) {
            return;
        }

        if (isWriteKeyAsHead) {
            writeRow(row, rowMap.keySet());
            row = row.getTable().createRow();
        }
        writeRow(row, rowMap.values());
    }

    /**
     * Writes a single row of data to the table.
     *
     * @param row     The {@link XWPFTableRow} to write data to.
     * @param rowData The iterable collection of data for the row's cells.
     */
    public static void writeRow(final XWPFTableRow row, final Iterable<?> rowData) {
        XWPFTableCell cell;
        int index = 0;
        for (final Object cellData : rowData) {
            cell = getOrCreateCell(row, index);
            cell.setText(Convert.toString(cellData));
            index++;
        }
    }

    /**
     * Gets an existing row or creates a new one if it doesn't exist.
     *
     * @param table The {@link XWPFTable} to get or create the row in.
     * @param index The index (row number) of the row, starting from 0.
     * @return The {@link XWPFTableRow} at the specified index.
     */
    public static XWPFTableRow getOrCreateRow(final XWPFTable table, final int index) {
        XWPFTableRow row = table.getRow(index);
        if (null == row) {
            row = table.createRow();
        }

        return row;
    }

    /**
     * Gets an existing cell or creates a new one if it doesn't exist.
     *
     * @param row   The {@link XWPFTableRow} to get or create the cell in.
     * @param index The index (column number) of the cell, starting from 0.
     * @return The {@link XWPFTableCell} at the specified index.
     */
    public static XWPFTableCell getOrCreateCell(final XWPFTableRow row, final int index) {
        XWPFTableCell cell = row.getCell(index);
        if (null == cell) {
            cell = row.createCell();
        }
        return cell;
    }

}
