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
import org.miaixz.bus.core.center.map.multi.Table;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.office.excel.RowGroup;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.style.StyleSet;
import org.miaixz.bus.office.excel.xyz.CellKit;
import org.miaixz.bus.office.excel.xyz.RowKit;

/**
 * Sheet数据写出器 此对象只封装将数据写出到Sheet中，并不刷新到文件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetDataWriter {

    private final Sheet sheet;
    /**
     * Excel输出配置
     */
    private final ExcelWriteConfig config;
    /**
     * 当前行，用于标记初始可写数据的行和部分写完后当前的行
     */
    private final AtomicInteger currentRow;
    /**
     * 样式集，定义不同类型数据样式
     */
    private StyleSet styleSet;
    /**
     * 标题项对应列号缓存，每次写标题更新此缓存 此缓存用于用户多次write时，寻找标题位置
     */
    private Map<String, Integer> headerLocationCache;

    /**
     * 构造
     *
     * @param sheet    {@link Sheet}
     * @param config   Excel配置
     * @param styleSet 样式表
     */
    public SheetDataWriter(final Sheet sheet, final ExcelWriteConfig config, final StyleSet styleSet) {
        this.sheet = sheet;
        this.config = config;
        this.styleSet = styleSet;
        this.currentRow = new AtomicInteger(0);
    }

    /**
     * 设置样式表
     *
     * @param styleSet 样式表
     * @return this
     */
    public SheetDataWriter setStyleSet(final StyleSet styleSet) {
        this.styleSet = styleSet;
        return this;
    }

    /**
     * 设置标题位置映射缓存
     *
     * @param headerLocationCache 标题位置映射缓存，key为表明名，value为列号
     * @return this
     */
    public SheetDataWriter setHeaderLocationCache(final Map<String, Integer> headerLocationCache) {
        this.headerLocationCache = headerLocationCache;
        return this;
    }

    /**
     * 写出分组标题行
     *
     * @param x        开始的列，下标从0开始
     * @param y        开始的行，下标从0开始
     * @param rowCount 当前分组行所占行数，此数值为标题占用行数+子分组占用的最大行数，不确定传1
     * @param rowGroup 分组行
     * @return this
     */
    public SheetDataWriter writeHeader(int x, int y, int rowCount, final RowGroup rowGroup) {

        // 写主标题
        final String name = rowGroup.getName();
        final List<RowGroup> children = rowGroup.getChildren();
        if (null != name) {
            if (CollKit.isNotEmpty(children)) {
                // 有子节点，标题行只占用除子节点占用的行数
                rowCount = Math.max(1, rowCount - rowGroup.childrenMaxRowCount());
                // nameRowCount = 1;
            }

            // 如果无子节点，则标题行占用所有行
            final CellRangeAddress cellRangeAddresses = CellKit.of(y, y + rowCount - 1, x,
                    x + rowGroup.maxColumnCount() - 1);
            CellStyle style = rowGroup.getStyle();
            if (null == style && null != this.styleSet) {
                style = styleSet.getStyleFor(
                        new CellReference(cellRangeAddresses.getFirstRow(), cellRangeAddresses.getFirstColumn()), name,
                        true);
            }
            CellKit.mergingCells(this.sheet, cellRangeAddresses, style);
            final Cell cell = CellKit.getOrCreateCell(this.sheet, cellRangeAddresses.getFirstColumn(),
                    cellRangeAddresses.getFirstRow());
            if (null != cell) {
                CellKit.setCellValue(cell, name, style, this.config.getCellEditor());
            }

            // 子分组写到下N行
            y += rowCount;
        }

        // 写分组
        final int childrenMaxRowCount = rowGroup.childrenMaxRowCount();
        if (childrenMaxRowCount > 0) {
            for (final RowGroup child : children) {
                // 子分组行高填充为当前分组最大值
                writeHeader(x, y, childrenMaxRowCount, child);
                x += child.maxColumnCount();
            }
        }

        return this;
    }

    /**
     * 写出一行，根据rowBean数据类型不同，写出情况如下：
     *
     * <pre>
     * 1、如果为Iterable，直接写出一行
     * 2、如果为Map，isWriteKeyAsHead为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values
     * 3、如果为Bean，转为Map写出，isWriteKeyAsHead为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values
     * </pre>
     *
     * @param rowBean          写出的Bean
     * @param isWriteKeyAsHead 为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values
     * @return this
     * @see #writeRow(Iterable)
     * @see #writeRow(Map, boolean)
     */
    public SheetDataWriter writeRow(final Object rowBean, final boolean isWriteKeyAsHead) {
        final ExcelWriteConfig config = this.config;

        final Map rowMap;
        if (rowBean instanceof Map) {
            if (MapKit.isNotEmpty(config.getHeaderAlias())) {
                rowMap = MapKit.newTreeMap((Map) rowBean, config.getCachedAliasComparator());
            } else {
                rowMap = (Map) rowBean;
            }
        } else if (rowBean instanceof Iterable) {
            // MapWrapper由于实现了Iterable接口，应该优先按照Map处理
            return writeRow((Iterable<?>) rowBean);
        } else if (rowBean instanceof Hyperlink) {
            // Hyperlink当成一个值
            return writeRow(ListKit.of(rowBean), isWriteKeyAsHead);
        } else if (BeanKit.isReadableBean(rowBean.getClass())) {
            if (MapKit.isEmpty(config.getHeaderAlias())) {
                rowMap = BeanKit.beanToMap(rowBean, new LinkedHashMap<>(), false, false);
            } else {
                // 别名存在情况下按照别名的添加顺序排序Bean数据
                rowMap = BeanKit.beanToMap(rowBean, new TreeMap<>(config.getCachedAliasComparator()), false, false);
            }
        } else {
            // 其它转为字符串默认输出
            return writeRow(ListKit.of(rowBean), isWriteKeyAsHead);
        }
        return writeRow(rowMap, isWriteKeyAsHead);
    }

    /**
     * 将一个Map写入到Excel，isWriteKeyAsHead为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values 如果rowMap为空（包括null），则写出空行
     *
     * @param rowMap           写出的Map，为空（包括null），则写出空行
     * @param isWriteKeyAsHead 为true写出两行，Map的keys做为一行，values做为第二行，否则只写出一行values
     * @return this
     */
    public SheetDataWriter writeRow(final Map<?, ?> rowMap, final boolean isWriteKeyAsHead) {
        if (MapKit.isEmpty(rowMap)) {
            // 如果写出数据为null或空，跳过当前行
            passAndGet();
            return this;
        }

        final Table<?, ?, ?> aliasTable = this.config.aliasTable(rowMap);
        if (isWriteKeyAsHead) {
            // 写出标题行，并记录标题别名和列号的关系
            writeHeaderRow(aliasTable.columnKeys());
            // 记录原数据key和别名对应列号
            int i = 0;
            for (final Object key : aliasTable.rowKeySet()) {
                this.headerLocationCache.putIfAbsent(StringKit.toString(key), i);
                i++;
            }
        }

        // 如果已经写出标题行，根据标题行找对应的值写入
        if (MapKit.isNotEmpty(this.headerLocationCache)) {
            final Row row = RowKit.getOrCreateRow(this.sheet, this.currentRow.getAndIncrement());
            final CellEditor cellEditor = this.config.getCellEditor();
            Integer columnIndex;
            for (final Table.Cell<?, ?, ?> cell : aliasTable) {
                columnIndex = getColumnIndex(cell);
                if (null != columnIndex) {
                    CellKit.setCellValue(CellKit.getOrCreateCell(row, columnIndex), cell.getValue(), this.styleSet,
                            false, cellEditor);
                }
            }
        } else {
            writeRow(aliasTable.values());
        }
        return this;
    }

    /**
     * 写出一行标题数据，标题数据不替换别名 本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     *
     * @param rowData 一行的数据
     * @return this
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
     * 写出一行数据 本方法只是将数据写入Workbook中的Sheet，并不写出到文件 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     *
     * @param rowData 一行的数据
     * @return this
     */
    public SheetDataWriter writeRow(final Iterable<?> rowData) {
        final int rowNum = this.currentRow.getAndIncrement();
        final Row row = this.config.insertRow ? this.sheet.createRow(rowNum)
                : RowKit.getOrCreateRow(this.sheet, rowNum);
        RowKit.writeRow(row, rowData, this.styleSet, false, this.config.getCellEditor());
        return this;
    }

    /**
     * 获得当前行
     *
     * @return 当前行
     */
    public int getCurrentRow() {
        return this.currentRow.get();
    }

    /**
     * 设置当前所在行
     *
     * @param rowIndex 行号
     * @return this
     */
    public SheetDataWriter setCurrentRow(final int rowIndex) {
        this.currentRow.set(rowIndex);
        return this;
    }

    /**
     * 跳过当前行，并获取下一行的行号
     *
     * @return this
     */
    public int passAndGet() {
        return this.currentRow.incrementAndGet();
    }

    /**
     * 跳过指定行数，并获取当前行号
     *
     * @param rowNum 跳过的行数
     * @return this
     */
    public int passRowsAndGet(final int rowNum) {
        return this.currentRow.addAndGet(rowNum);
    }

    /**
     * 重置当前行为0
     *
     * @return this
     */
    public SheetDataWriter resetRow() {
        this.currentRow.set(0);
        return this;
    }

    /**
     * 查找标题或标题别名对应的列号
     *
     * @param cell 别名表，rowKey：原名，columnKey：别名
     * @return 列号，如果未找到返回null
     */
    private Integer getColumnIndex(final Table.Cell<?, ?, ?> cell) {
        // 首先查找原名对应的列号
        Integer location = this.headerLocationCache.get(StringKit.toString(cell.getRowKey()));
        if (null == location) {
            // 未找到，则查找别名对应的列号
            location = this.headerLocationCache.get(StringKit.toString(cell.getColumnKey()));
        }
        return location;
    }

}
