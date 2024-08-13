/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * Word中表格相关工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DocxTable {

    /**
     * 创建空表，只有一行
     *
     * @param doc {@link XWPFDocument}
     * @return {@link XWPFTable}
     */
    public static XWPFTable createTable(final XWPFDocument doc) {
        return createTable(doc, null);
    }

    /**
     * 创建表格并填充数据，默认表格
     *
     * @param doc  {@link XWPFDocument}
     * @param data 数据
     * @return {@link XWPFTable}
     */
    public static XWPFTable createTable(final XWPFDocument doc, final Iterable<?> data) {
        Assert.notNull(doc, "XWPFDocument must be not null !");
        final XWPFTable table = doc.createTable();
        // 新建table的时候默认会新建一行，此处移除之
        table.removeRow(0);
        return writeTable(table, data);
    }

    /**
     * 为table填充数据
     *
     * @param table {@link XWPFTable}
     * @param data  数据
     * @return {@link XWPFTable}
     */
    public static XWPFTable writeTable(final XWPFTable table, final Iterable<?> data) {
        Assert.notNull(table, "XWPFTable must be not null !");
        if (IteratorKit.isEmpty(data)) {
            // 数据为空，返回空表
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
     * 写一行数据
     *
     * @param row              行
     * @param rowBean          行数据
     * @param isWriteKeyAsHead 如果为Map或者Bean，是否写标题
     */
    public static void writeRow(final XWPFTableRow row, final Object rowBean, final boolean isWriteKeyAsHead) {
        if (rowBean instanceof Iterable) {
            writeRow(row, (Iterable<?>) rowBean);
            return;
        }

        final Map rowMap;
        if (rowBean instanceof Map) {
            rowMap = (Map) rowBean;
        } else if (BeanKit.isWritableBean(rowBean.getClass())) {
            rowMap = BeanKit.beanToMap(rowBean, new LinkedHashMap<>(), false, false);
        } else {
            // 其它转为字符串默认输出
            writeRow(row, ListKit.of(rowBean), isWriteKeyAsHead);
            return;
        }

        writeRow(row, rowMap, isWriteKeyAsHead);
    }

    /**
     * 写行数据
     *
     * @param row              行
     * @param rowMap           行数据
     * @param isWriteKeyAsHead 是否写标题
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
     * 写行数据
     *
     * @param row     行
     * @param rowData 行数据
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
     * 获取或创建新行 存在则直接返回，不存在创建新的行
     *
     * @param table {@link XWPFTable}
     * @param index 索引（行号），从0开始
     * @return {@link XWPFTableRow}
     */
    public static XWPFTableRow getOrCreateRow(final XWPFTable table, final int index) {
        XWPFTableRow row = table.getRow(index);
        if (null == row) {
            row = table.createRow();
        }

        return row;
    }

    /**
     * 获取或创建新单元格 存在则直接返回，不存在创建新的单元格
     *
     * @param row   {@link XWPFTableRow} 行
     * @param index index 索引（列号），从0开始
     * @return {@link XWPFTableCell}
     */
    public static XWPFTableCell getOrCreateCell(final XWPFTableRow row, final int index) {
        XWPFTableCell cell = row.getCell(index);
        if (null == cell) {
            cell = row.createCell();
        }
        return cell;
    }

}
