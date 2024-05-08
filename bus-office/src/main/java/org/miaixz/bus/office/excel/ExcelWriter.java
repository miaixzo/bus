/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.office.excel;

import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.key.UUID;
import org.miaixz.bus.core.lang.Align;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.FileType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.map.TableMap;
import org.miaixz.bus.core.map.multi.RowKeyTable;
import org.miaixz.bus.core.map.multi.Table;
import org.miaixz.bus.core.toolkit.*;
import org.miaixz.bus.office.excel.cell.CellEditor;
import org.miaixz.bus.office.excel.cell.CellLocation;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Excel 写入器
 * 此工具用于通过POI将数据写出到Excel,此对象可完成以下两个功能
 *
 * <pre>
 * 1. 编辑已存在的Excel,可写出原Excel文件,也可写出到其它地方(到文件或到流)
 * 2. 新建一个空的Excel工作簿,完成数据填充后写出(到文件或到流)
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelWriter extends ExcelBase<ExcelWriter> {

    /**
     * 当前行
     */
    private AtomicInteger currentRow = new AtomicInteger(0);
    /**
     * 是否只保留别名对应的字段
     */
    private boolean onlyAlias;
    /**
     * 标题顺序比较器
     */
    private Comparator<String> aliasComparator;
    /**
     * 样式集,定义不同类型数据样式
     */
    private StyleSet styleSet;
    /**
     * 标题项对应列号缓存，每次写标题更新此缓存
     */
    private Map<String, Integer> headLocationCache;
    /**
     * 单元格值处理接口
     */
    private CellEditor cellEditor;

    /**
     * 构造,默认生成xls格式的Excel文件
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link #setDestFile(File)}方法自定义写出的文件,然后调用{@link #flush()}方法写出到文件
     */
    public ExcelWriter() {
        this(false);
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件,需要调用{@link #flush(File)} 写出到文件
     *
     * @param isXlsx 是否为xlsx格式
     */
    public ExcelWriter(boolean isXlsx) {
        this(WorksKit.createBook(isXlsx), null);
    }

    /**
     * 构造,默认写出到第一个sheet,第一个sheet名为sheet1
     *
     * @param destFilePath 目标文件路径,可以不存在
     */
    public ExcelWriter(String destFilePath) {
        this(destFilePath, null);
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件,需要调用{@link #flush(File)} 写出到文件
     *
     * @param isXlsx    是否为xlsx格式
     * @param sheetName sheet名,第一个sheet名并写出到此sheet,例如sheet1
     */
    public ExcelWriter(boolean isXlsx, String sheetName) {
        this(WorksKit.createBook(isXlsx), sheetName);
    }

    /**
     * 构造
     *
     * @param destFilePath 目标文件路径,可以不存在
     * @param sheetName    sheet名,第一个sheet名并写出到此sheet,例如sheet1
     */
    public ExcelWriter(String destFilePath, String sheetName) {
        this(FileKit.file(destFilePath), sheetName);
    }

    /**
     * 构造,默认写出到第一个sheet,第一个sheet名为sheet1
     *
     * @param destFile 目标文件,可以不存在
     */
    public ExcelWriter(File destFile) {
        this(destFile, null);
    }

    /**
     * 构造
     *
     * @param destFile  目标文件,可以不存在
     * @param sheetName sheet名,做为第一个sheet名并写出到此sheet,例如sheet1
     */
    public ExcelWriter(File destFile, String sheetName) {
        this(WorksKit.createBookForWriter(destFile), sheetName);
        this.destFile = destFile;
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link #setDestFile(File)}方法自定义写出的文件,然后调用{@link #flush()}方法写出到文件
     *
     * @param workbook  {@link Workbook}
     * @param sheetName sheet名,做为第一个sheet名并写出到此sheet,例如sheet1
     */
    public ExcelWriter(Workbook workbook, String sheetName) {
        this(WorksKit.getOrCreateSheet(workbook, sheetName));
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link #setDestFile(File)}方法自定义写出的文件,然后调用{@link #flush()}方法写出到文件
     *
     * @param sheet {@link Sheet}
     */
    public ExcelWriter(Sheet sheet) {
        super(sheet);
        this.styleSet = new StyleSet(workbook);
    }

    /**
     * 设置单元格值处理逻辑<br>
     * 当Excel中的值并不能满足我们的读取要求时，通过传入一个编辑接口，可以对单元格值自定义，例如对数字和日期类型值转换为字符串等
     *
     * @param cellEditor 单元格值处理接口
     * @return this
     */
    public ExcelWriter setCellEditor(final CellEditor cellEditor) {
        this.cellEditor = cellEditor;
        return this;
    }

    @Override
    public ExcelWriter setSheet(int sheetIndex) {
        // 切换到新sheet需要重置开始行
        reset();
        return super.setSheet(sheetIndex);
    }

    @Override
    public ExcelWriter setSheet(String sheetName) {
        // 切换到新sheet需要重置开始行
        reset();
        return super.setSheet(sheetName);
    }

    /**
     * 重置Writer,包括：
     *
     * <pre>
     * 1. 当前行游标归零
     * 2. 清空别名比较器
     * </pre>
     *
     * @return this
     */
    public ExcelWriter reset() {
        resetRow();
        return this;
    }

    /**
     * 重命名当前sheet
     *
     * @param sheetName 新的sheet名
     * @return this
     */
    public ExcelWriter renameSheet(String sheetName) {
        return renameSheet(this.workbook.getSheetIndex(this.sheet), sheetName);
    }

    /**
     * 重命名sheet
     *
     * @param sheet     sheet需要,0表示第一个sheet
     * @param sheetName 新的sheet名
     * @return this
     */
    public ExcelWriter renameSheet(int sheet, String sheetName) {
        this.workbook.setSheetName(sheet, sheetName);
        return this;
    }

    /**
     * 设置所有列为自动宽度,不考虑合并单元格
     * 此方法必须在指定列数据完全写出后调用才有效
     * 列数计算是通过第一行计算的
     *
     * @return this
     */
    public ExcelWriter autoSizeColumnAll() {
        final int columnCount = this.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            autoSizeColumn(i);
        }
        return this;
    }

    /**
     * 设置某列为自动宽度,不考虑合并单元格
     * 此方法必须在指定列数据完全写出后调用才有效
     *
     * @param columnIndex 第几列,从0计数
     * @return this
     */
    public ExcelWriter autoSizeColumn(int columnIndex) {
        SXSSFSheet sheet = (SXSSFSheet) this.sheet;
        sheet.trackAllColumnsForAutoSizing();
        this.sheet.autoSizeColumn(columnIndex);
        return this;
    }

    /**
     * 设置某列为自动宽度
     * 此方法必须在指定列数据完全写出后调用才有效
     *
     * @param columnIndex    第几列,从0计数
     * @param useMergedCells 是否适用于合并单元格
     * @return this
     */
    public ExcelWriter autoSizeColumn(int columnIndex, boolean useMergedCells) {
        this.sheet.autoSizeColumn(columnIndex, useMergedCells);
        return this;
    }

    /**
     * 获取样式集,样式集可以自定义包括：
     *
     * <pre>
     * 1. 头部样式
     * 2. 一般单元格样式
     * 3. 默认数字样式
     * 4. 默认日期样式
     * </pre>
     *
     * @return 样式集
     */
    public StyleSet getStyleSet() {
        return this.styleSet;
    }

    /**
     * 设置样式集,如果不使用样式,传入{@code null}
     *
     * @param styleSet 样式集,{@code null}表示无样式
     * @return this
     */
    public ExcelWriter setStyleSet(StyleSet styleSet) {
        this.styleSet = styleSet;
        return this;
    }

    /**
     * 获取头部样式,获取样式后可自定义样式
     *
     * @return 头部样式
     */
    public CellStyle getHeadCellStyle() {
        return this.styleSet.headCellStyle;
    }

    /**
     * 获取单元格样式,获取样式后可自定义样式
     *
     * @return 单元格样式
     */
    public CellStyle getCellStyle() {
        if (null == this.styleSet) {
            return null;
        }
        return this.styleSet.cellStyle;
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
    public ExcelWriter setCurrentRow(int rowIndex) {
        this.currentRow.set(rowIndex);
        return this;
    }

    /**
     * 跳过当前行
     *
     * @return this
     */
    public ExcelWriter passCurrentRow() {
        this.currentRow.incrementAndGet();
        return this;
    }

    /**
     * 跳过指定行数
     *
     * @param rows 跳过的行数
     * @return this
     */
    public ExcelWriter passRows(int rows) {
        this.currentRow.addAndGet(rows);
        return this;
    }

    /**
     * 重置当前行为0
     *
     * @return this
     */
    public ExcelWriter resetRow() {
        this.currentRow.set(0);
        return this;
    }

    /**
     * 设置写出的目标文件
     *
     * @param destFile 目标文件
     * @return this
     */
    public ExcelWriter setDestFile(File destFile) {
        this.destFile = destFile;
        return this;
    }

    @Override
    public ExcelWriter setHeaderAlias(Map<String, String> headerAlias) {
        this.aliasComparator = null;
        return super.setHeaderAlias(headerAlias);
    }

    @Override
    public ExcelWriter clearHeaderAlias() {
        this.aliasComparator = null;
        return super.clearHeaderAlias();
    }

    @Override
    public ExcelWriter addHeaderAlias(String name, String alias) {
        this.aliasComparator = null;
        return super.addHeaderAlias(name, alias);
    }

    /**
     * 设置是否只保留别名中的字段值,如果为true,则不设置alias的字段将不被输出,false表示原样输出
     *
     * @param isOnlyAlias 是否只保留别名中的字段值
     * @return this
     */
    public ExcelWriter setOnlyAlias(boolean isOnlyAlias) {
        this.onlyAlias = isOnlyAlias;
        return this;
    }

    /**
     * 设置窗口冻结，之前冻结的窗口会被覆盖，如果rowSplit为0表示取消冻结
     *
     * @param rowSplit 冻结的行及行数，2表示前两行
     * @return this
     */
    public ExcelWriter setFreezePane(int rowSplit) {
        return setFreezePane(0, rowSplit);
    }

    /**
     * 设置窗口冻结，之前冻结的窗口会被覆盖，如果colSplit和rowSplit为0表示取消冻结
     *
     * @param colSplit 冻结的列及列数，2表示前两列
     * @param rowSplit 冻结的行及行数，2表示前两行
     * @return this
     */
    public ExcelWriter setFreezePane(int colSplit, int rowSplit) {
        getSheet().createFreezePane(colSplit, rowSplit);
        return this;
    }

    /**
     * 设置列宽(单位为一个字符的宽度,例如传入width为10,表示10个字符的宽度)
     *
     * @param columnIndex 列号(从0开始计数,-1表示所有列的默认宽度)
     * @param width       宽度(单位1~256个字符宽度)
     * @return this
     */
    public ExcelWriter setColumnWidth(int columnIndex, int width) {
        if (columnIndex < 0) {
            this.sheet.setDefaultColumnWidth(width);
        } else {
            this.sheet.setColumnWidth(columnIndex, width * Normal._256);
        }
        return this;
    }

    /**
     * 设置行高,值为一个点的高度
     *
     * @param rownum 行号(从0开始计数,-1表示所有行的默认高度)
     * @param height 高度
     * @return this
     */
    public ExcelWriter setRowHeight(int rownum, int height) {
        if (rownum < 0) {
            this.sheet.setDefaultRowHeightInPoints(height);
        } else {
            final Row row = this.sheet.getRow(rownum);
            if (null != row) {
                row.setHeightInPoints(height);
            }
        }
        return this;
    }

    /**
     * 设置Excel页眉或页脚
     *
     * @param text     页脚的文本
     * @param align    对齐方式枚举 {@link Align}
     * @param isFooter 是否为页脚,false表示页眉,true表示页脚
     * @return this
     */
    public ExcelWriter setHeaderOrFooter(String text, Align align, boolean isFooter) {
        final HeaderFooter headerFooter = isFooter ? this.sheet.getFooter() : this.sheet.getHeader();
        switch (align) {
            case LEFT:
                headerFooter.setLeft(text);
                break;
            case RIGHT:
                headerFooter.setRight(text);
                break;
            case CENTER:
                headerFooter.setCenter(text);
                break;
            default:
                break;
        }
        return this;
    }

    /**
     * 合并当前行的单元格
     * 样式为默认标题样式,可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param lastColumn 合并到的最后一个列号
     * @return this
     */
    public ExcelWriter merge(int lastColumn) {
        return merge(lastColumn, null);
    }

    /**
     * 合并当前行的单元格,并写入对象到单元格
     * 如果写到单元格中的内容非null,行号自动+1,否则当前行号不变
     * 样式为默认标题样式,可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param lastColumn 合并到的最后一个列号
     * @param content    合并单元格后的内容
     * @return this
     */
    public ExcelWriter merge(int lastColumn, Object content) {
        return merge(lastColumn, content, true);
    }

    /**
     * 合并某行的单元格,并写入对象到单元格
     * 如果写到单元格中的内容非null,行号自动+1,否则当前行号不变
     * 样式为默认标题样式,可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param lastColumn       合并到的最后一个列号
     * @param content          合并单元格后的内容
     * @param isSetHeaderStyle 是否为合并后的单元格设置默认标题样式
     * @return this
     */
    public ExcelWriter merge(int lastColumn, Object content, boolean isSetHeaderStyle) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");

        final int rowIndex = this.currentRow.get();
        merge(rowIndex, rowIndex, 0, lastColumn, content, isSetHeaderStyle);

        // 设置内容后跳到下一行
        if (null != content) {
            this.currentRow.incrementAndGet();
        }
        return this;
    }

    /**
     * 合并某行的单元格,并写入对象到单元格
     * 如果写到单元格中的内容非null,行号自动+1,否则当前行号不变
     * 样式为默认标题样式,可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param firstRow         第一行
     * @param lastRow          最后一行
     * @param firstColumn      第一列
     * @param lastColumn       合并到的最后一个列号
     * @param content          合并单元格后的内容
     * @param isSetHeaderStyle 是否为合并后的单元格设置默认标题样式
     * @return this
     */
    public ExcelWriter merge(int firstRow, int lastRow, int firstColumn, int lastColumn, Object content, boolean isSetHeaderStyle) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");

        CellStyle style = null;
        if (null != this.styleSet) {
            style = styleSet.getStyleByValueType(content, isSetHeaderStyle);
        }

        return merge(firstRow, lastRow, firstColumn, lastColumn, content, style);
    }

    /**
     * 合并单元格，并写入对象到单元格,使用指定的样式
     * 指定样式传入null，则不使用任何样式
     *
     * @param firstRow    起始行，0开始
     * @param lastRow     结束行，0开始
     * @param firstColumn 起始列，0开始
     * @param lastColumn  结束列，0开始
     * @param content     合并单元格后的内容
     * @param cellStyle   合并后单元格使用的样式，可以为null
     * @return this
     */
    public ExcelWriter merge(int firstRow, int lastRow, int firstColumn, int lastColumn, Object content, CellStyle cellStyle) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");

        CellKit.mergingCells(this.getSheet(), firstRow, lastRow, firstColumn, lastColumn, cellStyle);

        // 设置内容
        if (null != content) {
            final Cell cell = getOrCreateCell(firstColumn, firstRow);
            CellKit.setCellValue(cell, content, cellStyle, this.cellEditor);
        }
        return this;
    }

    /**
     * 写出数据,本方法只是将数据写入Workbook中的Sheet,并不写出到文件
     * 写出的起始行为当前行号,可使用{@link #getCurrentRow()}方法调用,根据写出的的行数,当前行号自动增加
     * 样式为默认样式,可使用{@link #getCellStyle()}方法调用后自定义默认样式
     * 默认的,当当前行号为0时,写出标题(如果为Map或Bean),否则不写标题
     *
     * <p>
     * data中元素支持的类型有：
     *
     * <pre>
     * 1. Iterable,既元素为一个集合,元素被当作一行,data表示多行
     * 2. Map,既元素为一个Map,第一个Map的keys作为首行,剩下的行为Map的values,data表示多行
     * 3. Bean,既元素为一个Bean,第一个Bean的字段名列表会作为首行,剩下的行为Bean的字段值列表,data表示多行
     * 4. 其它类型,按照基本类型输出(例如字符串)
     * </pre>
     *
     * @param data 数据
     * @return this
     */
    public ExcelWriter write(Iterable<?> data) {
        return write(data, 0 == getCurrentRow());
    }

    /**
     * 写出数据,本方法只是将数据写入Workbook中的Sheet,并不写出到文件
     * 写出的起始行为当前行号,可使用{@link #getCurrentRow()}方法调用,根据写出的的行数,当前行号自动增加
     * 样式为默认样式,可使用{@link #getCellStyle()}方法调用后自定义默认样式
     *
     * <p>
     * data中元素支持的类型有：
     *
     * <pre>
     * 1. Iterable,既元素为一个集合,元素被当作一行,data表示多行
     * 2. Map,既元素为一个Map,第一个Map的keys作为首行,剩下的行为Map的values,data表示多行
     * 3. Bean,既元素为一个Bean,第一个Bean的字段名列表会作为首行,剩下的行为Bean的字段值列表,data表示多行
     * 4. 其它类型,按照基本类型输出(例如字符串)
     * </pre>
     *
     * @param data             数据
     * @param isWriteKeyAsHead 是否强制写出标题行(Map或Bean)
     * @return this
     */
    public ExcelWriter write(Iterable<?> data, boolean isWriteKeyAsHead) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        boolean isFirst = true;
        for (Object object : data) {
            writeRow(object, isFirst && isWriteKeyAsHead);
            if (isFirst) {
                isFirst = false;
            }
        }
        return this;
    }

    /**
     * 写出数据,本方法只是将数据写入Workbook中的Sheet,并不写出到文件
     * 写出的起始行为当前行号,可使用{@link #getCurrentRow()}方法调用,根据写出的的行数,当前行号自动增加
     * 样式为默认样式,可使用{@link #getCellStyle()}方法调用后自定义默认样式
     * data中元素支持的类型有：
     *
     * <p>
     * 1. Map,既元素为一个Map,第一个Map的keys作为首行,剩下的行为Map的values,data表示多行
     * 2. Bean,既元素为一个Bean,第一个Bean的字段名列表会作为首行,剩下的行为Bean的字段值列表,data表示多行
     * </p>
     *
     * @param data       数据
     * @param comparator 比较器,用于字段名的排序
     * @return this
     */
    public ExcelWriter write(Iterable<?> data, Comparator<String> comparator) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        boolean isFirstRow = true;
        Map<?, ?> map;
        for (Object object : data) {
            if (object instanceof Map) {
                map = new TreeMap<>(comparator);
                map.putAll((Map) object);
            } else {
                map = BeanKit.beanToMap(object, new TreeMap<>(comparator), false, false);
            }
            writeRow(map, isFirstRow);
            if (isFirstRow) {
                isFirstRow = false;
            }
        }
        return this;
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 添加图片到当前sheet中 / 默认图片类型png / 默认的起始坐标和结束坐标都为0
     *
     * @param imgFile 图片文件
     * @param col1    指定起始的列，下标从0开始
     * @param row1    指定起始的行，下标从0开始
     * @param col2    指定结束的列，下标从0开始
     * @param row2    指定结束的行，下标从0开始
     * @return this
     */
    public ExcelWriter writeImg(File imgFile, int col1, int row1, int col2, int row2) {
        return this.writeImg(imgFile, 0, 0, 0, 0, col1, row1, col2, row2);
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 添加图片到当前sheet中 / 默认图片类型png
     *
     * @param imgFile 图片文件
     * @param dx1     起始单元格中的x坐标
     * @param dy1     起始单元格中的y坐标
     * @param dx2     结束单元格中的x坐标
     * @param dy2     结束单元格中的y坐标
     * @param col1    指定起始的列，下标从0开始
     * @param row1    指定起始的行，下标从0开始
     * @param col2    指定结束的列，下标从0开始
     * @param row2    指定结束的行，下标从0开始
     * @return this
     */
    public ExcelWriter writeImg(File imgFile, int dx1, int dy1, int dx2, int dy2, int col1, int row1,
                                int col2, int row2) {
        return this.writeImg(imgFile, Workbook.PICTURE_TYPE_PNG, dx1, dy1, dx2, dy2, col1, row1, col2, row2);
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 添加图片到当前sheet中
     *
     * @param imgFile 图片文件
     * @param imgType 图片类型，对应poi中Workbook类中的图片类型2-7变量
     * @param dx1     起始单元格中的x坐标
     * @param dy1     起始单元格中的y坐标
     * @param dx2     结束单元格中的x坐标
     * @param dy2     结束单元格中的y坐标
     * @param col1    指定起始的列，下标从0开始
     * @param row1    指定起始的行，下标从0开始
     * @param col2    指定结束的列，下标从0开始
     * @param row2    指定结束的行，下标从0开始
     * @return this
     */
    public ExcelWriter writeImg(File imgFile, int imgType, int dx1, int dy1, int dx2,
                                int dy2, int col1, int row1, int col2, int row2) {
        return writeImg(FileKit.readBytes(imgFile), imgType, dx1,
                dy1, dx2, dy2, col1, row1, col2, row2);
    }

    /**
     * 写出数据，本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 添加图片到当前sheet中
     *
     * @param pictureData 数据bytes
     * @param imgType     图片类型，对应poi中Workbook类中的图片类型2-7变量
     * @param dx1         起始单元格中的x坐标
     * @param dy1         起始单元格中的y坐标
     * @param dx2         结束单元格中的x坐标
     * @param dy2         结束单元格中的y坐标
     * @param col1        指定起始的列，下标从0开始
     * @param row1        指定起始的行，下标从0开始
     * @param col2        指定结束的列，下标从0开始
     * @param row2        指定结束的行，下标从0开始
     * @return this
     */
    public ExcelWriter writeImg(byte[] pictureData, int imgType, int dx1, int dy1, int dx2,
                                int dy2, int col1, int row1, int col2, int row2) {
        Drawing<?> patriarch = this.sheet.createDrawingPatriarch();
        ClientAnchor anchor = this.workbook.getCreationHelper().createClientAnchor();
        anchor.setDx1(dx1);
        anchor.setDy1(dy1);
        anchor.setDx2(dx2);
        anchor.setDy2(dy2);
        anchor.setCol1(col1);
        anchor.setRow1(row1);
        anchor.setCol2(col2);
        anchor.setRow2(row2);

        patriarch.createPicture(anchor, this.workbook.addPicture(pictureData, imgType));
        return this;
    }

    /**
     * 写出一行标题数据
     * 本方法只是将数据写入Workbook中的Sheet,并不写出到文件
     * 写出的起始行为当前行号,可使用{@link #getCurrentRow()}方法调用,根据写出的的行数,当前行号自动+1
     * 样式为默认标题样式,可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param rowData 一行的数据
     * @return this
     */
    public ExcelWriter writeHeadRow(Iterable<?> rowData) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        this.headLocationCache = new ConcurrentHashMap<>();
        final Row row = this.sheet.createRow(this.currentRow.getAndIncrement());
        int i = 0;
        Cell cell;
        for (Object value : rowData) {
            cell = row.createCell(i);
            CellKit.setCellValue(cell, value, this.styleSet, true, this.cellEditor);
            this.headLocationCache.put(StringKit.toString(value), i);
            i++;
        }
        return this;
    }

    /**
     * 写出一行,根据rowBean数据类型不同,写出情况如下：
     *
     * <pre>
     * 1、如果为Iterable,直接写出一行
     * 2、如果为Map,isWriteKeyAsHead为true写出两行,Map的keys做为一行,values做为第二行,否则只写出一行values
     * 3、如果为Bean,转为Map写出,isWriteKeyAsHead为true写出两行,Map的keys做为一行,values做为第二行,否则只写出一行values
     * </pre>
     *
     * @param rowBean          写出的Bean
     * @param isWriteKeyAsHead 为true写出两行,Map的keys做为一行,values做为第二行,否则只写出一行values
     * @return this
     * @see #writeRow(Iterable)
     * @see #writeRow(Map, boolean)
     */
    public ExcelWriter writeRow(Object rowBean, boolean isWriteKeyAsHead) {
        if (rowBean instanceof Iterable) {
            return writeRow((Iterable<?>) rowBean);
        }
        Map rowMap;
        if (rowBean instanceof Map) {
            if (MapKit.isNotEmpty(this.headerAlias)) {
                rowMap = MapKit.newTreeMap((Map) rowBean, getCachedAliasComparator());
            } else {
                rowMap = (Map) rowBean;
            }
        } else if (rowBean instanceof Hyperlink) {
            // Hyperlink当成一个值
            return writeRow(CollKit.newArrayList(rowBean), isWriteKeyAsHead);
        } else if (BeanKit.isBean(rowBean.getClass())) {
            if (MapKit.isEmpty(this.headerAlias)) {
                rowMap = BeanKit.beanToMap(rowBean, new LinkedHashMap<>(), false, false);
            } else {
                // 别名存在情况下按照别名的添加顺序排序Bean数据
                rowMap = BeanKit.beanToMap(rowBean, new TreeMap<>(getCachedAliasComparator()), false, false);
            }
        } else {
            // 其它转为字符串默认输出
            return writeRow(CollKit.newArrayList(rowBean), isWriteKeyAsHead);
        }
        return writeRow(rowMap, isWriteKeyAsHead);
    }

    /**
     * 将一个Map写入到Excel,isWriteKeyAsHead为true写出两行,Map的keys做为一行,values做为第二行,否则只写出一行values
     * 如果rowMap为空(包括null),则写出空行
     *
     * @param rowMap           写出的Map,为空(包括null),则写出空行
     * @param isWriteKeyAsHead 为true写出两行,Map的keys做为一行,values做为第二行,否则只写出一行values
     * @return this
     */
    public ExcelWriter writeRow(Map<?, ?> rowMap, boolean isWriteKeyAsHead) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        if (MapKit.isEmpty(rowMap)) {
            // 如果写出数据为null或空，跳过当前行
            return passCurrentRow();
        }

        final Table<?, ?, ?> aliasTable = aliasTable(rowMap);
        if (isWriteKeyAsHead) {
            // 写出标题行，并记录标题别名和列号的关系
            writeHeadRow(aliasTable.columnKeys());
            // 记录原数据key对应列号
            int i = 0;
            for (Object key : aliasTable.rowKeySet()) {
                this.headLocationCache.putIfAbsent(StringKit.toString(key), i);
                i++;
            }
        }

        // 如果已经写出标题行，根据标题行找对应的值写入
        if (MapKit.isNotEmpty(this.headLocationCache)) {
            final Row row = RowKit.getOrCreateRow(this.sheet, this.currentRow.getAndIncrement());
            Integer location;
            for (Table.Cell<?, ?, ?> cell : aliasTable) {
                // 首先查找原名对应的列号
                location = this.headLocationCache.get(StringKit.toString(cell.getRowKey()));
                if (null == location) {
                    // 未找到，则查找别名对应的列号
                    location = this.headLocationCache.get(StringKit.toString(cell.getColumnKey()));
                }
                if (null != location) {
                    CellKit.setCellValue(CellKit.getOrCreateCell(row, location), cell.getValue(), this.styleSet, false, this.cellEditor);
                }
            }
        } else {
            writeRow(aliasTable.values());
        }
        return this;
    }

    /**
     * 写出一行数据
     * 本方法只是将数据写入Workbook中的Sheet,并不写出到文件
     * 写出的起始行为当前行号,可使用{@link #getCurrentRow()}方法调用,根据写出的的行数,当前行号自动+1
     * 样式为默认样式,可使用{@link #getCellStyle()}方法调用后自定义默认样式
     *
     * @param rowData 一行的数据
     * @return this
     */
    public ExcelWriter writeRow(Iterable<?> rowData) {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        RowKit.writeRow(this.sheet.createRow(this.currentRow.getAndIncrement()), rowData, this.styleSet, false, this.cellEditor);
        return this;
    }


    /**
     * 写出复杂标题的第二行标题数据
     * 本方法只是将数据写入Workbook中的Sheet，并不写出到文件
     * 写出的起始行为当前行号，可使用{@link #getCurrentRow()}方法调用，根据写出的的行数，当前行号自动+1
     * 样式为默认标题样式，可使用{@link #getHeadCellStyle()}方法调用后自定义默认样式
     *
     * @param rowData 一行的数据
     * @return this
     */
    public ExcelWriter writeSecHeadRow(Iterable<?> rowData) {
        final Row row = RowKit.getOrCreateRow(this.sheet, this.currentRow.getAndIncrement());
        Iterator<?> iterator = rowData.iterator();
        if (row.getLastCellNum() != 0) {
            for (int i = 0; i < this.workbook.getSpreadsheetVersion().getMaxColumns(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    continue;
                }
                if (iterator.hasNext()) {
                    cell = row.createCell(i);
                    CellKit.setCellValue(cell, iterator.next(), this.styleSet, true, this.cellEditor);
                } else {
                    break;
                }
            }
        } else {
            writeHeadRow(rowData);
        }
        return this;
    }

    /**
     * 给指定单元格赋值,使用默认单元格样式
     *
     * @param x     X坐标,从0计数,既列号
     * @param y     Y坐标,从0计数,既行号
     * @param value 值
     * @return this
     */
    public ExcelWriter writeCellValue(int x, int y, Object value) {
        final Cell cell = getOrCreateCell(x, y);
        CellKit.setCellValue(cell, value, this.styleSet, false, this.cellEditor);
        return this;
    }

    /**
     * 给指定单元格赋值，使用默认单元格样式
     *
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @param value       值
     * @return this
     */
    public ExcelWriter writeCellValue(String locationRef, Object value) {
        final CellLocation cellLocation = ExcelKit.toLocation(locationRef);
        return writeCellValue(cellLocation.getX(), cellLocation.getY(), value);
    }

    /**
     * 设置某个单元格的样式
     * 此方法用于多个单元格共享样式的情况
     * 可以调用{@link #getOrCreateCellStyle(int, int)} 方法创建或取得一个样式对象
     * 需要注意的是，共享样式会共享同一个{@link CellStyle}，一个单元格样式改变，全部改变
     *
     * @param style 单元格样式
     * @param x     X坐标，从0计数，即列号
     * @param y     Y坐标，从0计数，即行号
     * @return this
     */
    public ExcelWriter setStyle(CellStyle style, int x, int y) {
        final Cell cell = getOrCreateCell(x, y);
        cell.setCellStyle(style);
        return this;
    }

    /**
     * 设置某个单元格的样式
     * 此方法用于多个单元格共享样式的情况
     * 可以调用{@link #getOrCreateCellStyle(int, int)} 方法创建或取得一个样式对象
     * 需要注意的是，共享样式会共享同一个{@link CellStyle}，一个单元格样式改变，全部改变
     *
     * @param style       单元格样式
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return this
     */
    public ExcelWriter setStyle(CellStyle style, String locationRef) {
        final CellLocation cellLocation = ExcelKit.toLocation(locationRef);
        return setStyle(style, cellLocation.getX(), cellLocation.getY());
    }

    /**
     * 设置行样式
     *
     * @param y     Y坐标，从0计数，即行号
     * @param style 样式
     * @return this
     * @see Row#setRowStyle(CellStyle)
     */
    public ExcelWriter setRowStyle(int y, CellStyle style) {
        getOrCreateRow(y).setRowStyle(style);
        return this;
    }

    /**
     * 设置列的默认样式
     *
     * @param x     列号，从0开始
     * @param style 样式
     * @return this
     */
    public ExcelWriter setColumnStyle(int x, CellStyle style) {
        this.sheet.setDefaultColumnStyle(x, style);
        return this;
    }

    /**
     * 创建字体
     *
     * @return 字体
     */
    public Font createFont() {
        return getWorkbook().createFont();
    }

    /**
     * 将Excel Workbook刷出到预定义的文件
     * 如果用户未自定义输出的文件,将抛出{@link NullPointerException}
     * 预定义文件可以通过{@link #setDestFile(File)} 方法预定义,或者通过构造定义
     *
     * @return this
     * @throws InternalException IO异常
     */
    public ExcelWriter flush() throws InternalException {
        return flush(this.destFile);
    }

    /**
     * 将Excel Workbook刷出到文件
     * 如果用户未自定义输出的文件,将抛出{@link InternalException}
     *
     * @param destFile 写出到的文件
     * @return this
     * @throws InternalException IO异常
     */
    public ExcelWriter flush(File destFile) throws InternalException {
        Assert.notNull(destFile, "[destFile] is null, and you must call setDestFile(File) first or call flush(OutputStream).");
        return flush(FileKit.getOutputStream(destFile), true);
    }

    /**
     * 将Excel Workbook刷出到输出流
     *
     * @param out 输出流
     * @return this
     * @throws InternalException IO异常
     */
    public ExcelWriter flush(OutputStream out) throws InternalException {
        return flush(out, false);
    }

    /**
     * 将Excel Workbook刷出到输出流
     *
     * @param out        输出流
     * @param isCloseOut 是否关闭输出流
     * @return this
     * @throws InternalException IO异常
     */
    public ExcelWriter flush(OutputStream out, boolean isCloseOut) throws InternalException {
        Assert.isFalse(this.isClosed, "ExcelWriter has been closed!");
        try {
            this.workbook.write(out);
            out.flush();
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            if (isCloseOut) {
                IoKit.close(out);
            }
        }
        return this;
    }


    /**
     * 增加下拉列表
     *
     * @param x          x坐标，列号，从0开始
     * @param y          y坐标，行号，从0开始
     * @param selectList 下拉列表
     * @return this
     */
    public ExcelWriter addSelect(int x, int y, String... selectList) {
        return addSelect(new CellRangeAddressList(y, y, x, x), selectList);
    }

    /**
     * 增加下拉列表
     *
     * @param regions    {@link CellRangeAddressList} 指定下拉列表所占的单元格范围
     * @param selectList 下拉列表内容
     * @return this
     */
    public ExcelWriter addSelect(CellRangeAddressList regions, String... selectList) {
        final DataValidationHelper validationHelper = this.sheet.getDataValidationHelper();
        final DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(selectList);

        //设置下拉框数据
        final DataValidation dataValidation = validationHelper.createValidation(constraint, regions);

        //处理Excel兼容性问题
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }

        return addValidationData(dataValidation);
    }

    /**
     * 增加单元格控制，比如下拉列表、日期验证、数字范围验证等
     *
     * @param dataValidation {@link DataValidation}
     * @return this
     */
    public ExcelWriter addValidationData(DataValidation dataValidation) {
        this.sheet.addValidationData(dataValidation);
        return this;
    }

    /**
     * 关闭工作簿
     * 如果用户设定了目标文件,先写出目标文件后给关闭工作簿
     */
    @Override
    public void close() {
        if (null != this.destFile) {
            flush();
        }
        closeWithoutFlush();
    }

    /**
     * 获取Content-Disposition头对应的值，可以通过调用以下方法快速设置下载Excel的头信息：
     *
     * <pre>
     * response.setHeader("Content-Disposition", excelWriter.getDisposition("test.xlsx", CharsetUtil.CHARSET_UTF_8));
     * </pre>
     *
     * @param fileName 文件名，如果文件名没有扩展名，会自动按照生成Excel类型补齐扩展名，如果提供空，使用随机UUID
     * @param charset  编码，null则使用默认UTF-8编码
     * @return Content-Disposition值
     */
    public String getDisposition(String fileName, Charset charset) {
        if (null == charset) {
            charset = org.miaixz.bus.core.lang.Charset.UTF_8;
        }

        if (StringKit.isBlank(fileName)) {
            // 未提供文件名使用随机UUID作为文件名
            fileName = UUID.randomUUID15();
        }

        fileName = StringKit.addSuffixIfNot(UriKit.encodeAll(fileName, charset), isXlsx() ? FileType.TYPE_XLSX : FileType.TYPE_XLS);
        return StringKit.format("attachment; filename=\"{}\"", fileName);
    }

    /**
     * 获取Content-Type头对应的值，可以通过调用以下方法快速设置下载Excel的头信息：
     *
     * <pre>
     * response.setContentType(excelWriter.getContentType());
     * </pre>
     *
     * @return Content-Type值
     */
    public String getContentType() {
        return isXlsx() ? FileType.DOCS.get(FileType.TYPE_XLSX) : FileType.DOCS.get(FileType.TYPE_XLS);
    }

    /**
     * 判断是否为xlsx格式的Excel表（Excel07格式）
     *
     * @return 是否为xlsx格式的Excel表（Excel07格式）
     */
    public boolean isXlsx() {
        return this.sheet instanceof XSSFSheet || this.sheet instanceof SXSSFSheet;
    }

    /**
     * 关闭工作簿但是不写出
     */
    protected void closeWithoutFlush() {
        super.close();

        // 清空对象
        this.currentRow = null;
        this.styleSet = null;
    }

    /**
     * 为指定的key列表添加标题别名，如果没有定义key的别名，在onlyAlias为false时使用原key<br>
     * key为别名，value为字段值
     *
     * @param rowMap 一行数据
     * @return 别名列表
     */
    private Table<?, ?, ?> aliasTable(Map<?, ?> rowMap) {
        final Table<Object, Object, Object> filteredTable = new RowKeyTable<>(new LinkedHashMap<>(), TableMap::new);
        if (MapKit.isEmpty(this.headerAlias)) {
            rowMap.forEach((key, value) -> filteredTable.put(key, key, value));
        } else {
            rowMap.forEach((key, value) -> {
                final String aliasName = this.headerAlias.get(StringKit.toString(key));
                if (null != aliasName) {
                    // 别名键值对加入
                    filteredTable.put(key, aliasName, value);
                } else if (false == this.onlyAlias) {
                    // 保留无别名设置的键值对
                    filteredTable.put(key, key, value);
                }
            });
        }

        return filteredTable;
    }

    /**
     * 获取单例的别名比较器,比较器的顺序为别名加入的顺序
     *
     * @return Comparator 比较器
     */
    private Comparator<String> getCachedAliasComparator() {
        if (MapKit.isEmpty(this.headerAlias)) {
            return null;
        }
        Comparator<String> aliasComparator = this.aliasComparator;
        if (null == aliasComparator) {
            Set<String> keySet = this.headerAlias.keySet();
            aliasComparator = new IndexedComparator<>(keySet.toArray(new String[keySet.size()]));
            this.aliasComparator = aliasComparator;
        }
        return aliasComparator;
    }

    class IndexedComparator<T> implements Comparator<T> {

        private final T[] array;

        /**
         * 构造
         *
         * @param objs 参与排序的数组,数组的元素位置决定了对象的排序先后
         */
        public IndexedComparator(T... objs) {
            this.array = objs;
        }

        @Override
        public int compare(T o1, T o2) {
            final int index1 = ArrayKit.indexOf(array, o1);
            final int index2 = ArrayKit.indexOf(array, o2);
            if (index1 == index2) {
                //位置相同使用自然排序
                return compare(o1, o2, true);
            }
            return index1 < index2 ? -1 : 1;
        }

        /**
         * {@code null}安全的对象比较,{@code null}对象排在末尾
         *
         * @param <T> 被比较对象类型
         * @param c1  对象1,可以为{@code null}
         * @param c2  对象2,可以为{@code null}
         * @return 比较结果, 如果c1 &lt; c2,返回数小于0,c1==c2返回0,c1 &gt; c2 大于0
         * @see java.util.Comparator#compare(Object, Object)
         */
        public <T extends Comparable<? super T>> int compare(T c1, T c2) {
            return compare(c1, c2, false);
        }

        /**
         * {@code null}安全的对象比较
         *
         * @param <T>           被比较对象类型(必须实现Comparable接口)
         * @param c1            对象1,可以为{@code null}
         * @param c2            对象2,可以为{@code null}
         * @param isNullGreater 当被比较对象为null时是否排在前面
         * @return 比较结果, 如果c1 &lt; c2,返回数小于0,c1==c2返回0,c1 &gt; c2 大于0
         * @see java.util.Comparator#compare(Object, Object)
         */
        public <T extends Comparable<? super T>> int compare(T c1, T c2, boolean isNullGreater) {
            if (c1 == c2) {
                return 0;
            } else if (null == c1) {
                return isNullGreater ? 1 : -1;
            } else if (null == c2) {
                return isNullGreater ? -1 : 1;
            }
            return c1.compareTo(c2);
        }

        /**
         * 自然比较两个对象的大小,比较规则如下：
         *
         * <pre>
         * 1、如果实现Comparable调用compareTo比较
         * 2、o1.equals(o2)返回0
         * 3、比较hashCode值
         * 4、比较toString值
         * </pre>
         *
         * @param o1            对象1
         * @param o2            对象2
         * @param isNullGreater null值是否做为最大值
         * @return 比较结果, 如果o1 &lt; o2,返回数小于0,o1==o2返回0,o1 &gt; o2 大于0
         */
        public <T> int compare(T o1, T o2, boolean isNullGreater) {
            if (o1 == o2) {
                return 0;
            } else if (null == o1) {// null 排在后面
                return isNullGreater ? 1 : -1;
            } else if (null == o2) {
                return isNullGreater ? -1 : 1;
            }

            if (o1 instanceof Comparable && o2 instanceof Comparable) {
                //如果bean可比较,直接比较bean
                return ((Comparable) o1).compareTo(o2);
            }

            if (o1.equals(o2)) {
                return 0;
            }

            int result = Integer.compare(o1.hashCode(), o2.hashCode());
            if (0 == result) {
                result = compare(o1.toString(), o2.toString());
            }

            return result;
        }

    }

}
