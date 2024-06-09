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
package org.miaixz.bus.office.excel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.miaixz.bus.core.data.ID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.cell.CellKit;
import org.miaixz.bus.office.excel.style.Styles;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel基础类，用于抽象ExcelWriter和ExcelReader中共用部分的对象和方法
 *
 * @param <T> 子类类型，用于返回this
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelBase<T extends ExcelBase<T>> implements Closeable {
    /**
     * 是否被关闭
     */
    protected boolean isClosed;
    /**
     * 目标文件，如果用户读取为流或自行创建的Workbook或Sheet,此参数为{@code null}
     */
    protected File destFile;
    /**
     * 工作簿
     */
    protected Workbook workbook;
    /**
     * Excel中对应的Sheet
     */
    protected Sheet sheet;
    /**
     * 标题行别名
     */
    protected Map<String, String> headerAlias;

    /**
     * 构造
     *
     * @param sheet Excel中的sheet
     */
    public ExcelBase(final Sheet sheet) {
        Assert.notNull(sheet, "No Sheet provided.");
        this.sheet = sheet;
        this.workbook = sheet.getWorkbook();
    }

    /**
     * 获取Workbook
     *
     * @return Workbook
     */
    public Workbook getWorkbook() {
        return this.workbook;
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
     * 返回工作簿表格数
     *
     * @return 工作簿表格数
     */
    public int getSheetCount() {
        return this.workbook.getNumberOfSheets();
    }

    /**
     * 获取此工作簿所有Sheet表
     *
     * @return sheet表列表
     */
    public List<Sheet> getSheets() {
        final int totalSheet = getSheetCount();
        final List<Sheet> result = new ArrayList<>(totalSheet);
        for (int i = 0; i < totalSheet; i++) {
            result.add(this.workbook.getSheetAt(i));
        }
        return result;
    }

    /**
     * 获取表名列表
     *
     * @return 表名列表
     */
    public List<String> getSheetNames() {
        final int totalSheet = workbook.getNumberOfSheets();
        final List<String> result = new ArrayList<>(totalSheet);
        for (int i = 0; i < totalSheet; i++) {
            result.add(this.workbook.getSheetAt(i).getSheetName());
        }
        return result;
    }

    /**
     * 获取当前Sheet
     *
     * @return {@link Sheet}
     */
    public Sheet getSheet() {
        return this.sheet;
    }

    /**
     * 自定义需要读取或写出的Sheet，如果给定的sheet不存在，创建之。
     * 在读取中，此方法用于切换读取的sheet，在写出时，此方法用于新建或者切换sheet。
     *
     * @param sheetName sheet名
     * @return this
     */
    public T setSheet(final String sheetName) {
        return setSheet(WorkbookKit.getOrCreateSheet(this.workbook, sheetName));
    }

    /**
     * 自定义需要读取或写出的Sheet，如果给定的sheet不存在，创建之（命名为默认）
     * 在读取中，此方法用于切换读取的sheet，在写出时，此方法用于新建或者切换sheet
     *
     * @param sheetIndex sheet序号，从0开始计数
     * @return this
     */
    public T setSheet(final int sheetIndex) {
        return setSheet(WorkbookKit.getOrCreateSheet(this.workbook, sheetIndex));
    }

    /**
     * 设置自定义Sheet
     *
     * @param sheet 自定义sheet，可以通过{@link WorkbookKit#getOrCreateSheet(Workbook, String)} 创建
     * @return this
     */
    public T setSheet(final Sheet sheet) {
        this.sheet = sheet;
        return (T) this;
    }

    /**
     * 重命名当前sheet
     *
     * @param newName 新名字
     * @return this
     * @see Workbook#setSheetName(int, String)
     */
    public T renameSheet(final String newName) {
        this.workbook.setSheetName(this.workbook.getSheetIndex(this.sheet), newName);
        return (T) this;
    }

    /**
     * 复制当前sheet为新sheet
     *
     * @param sheetIndex        sheet位置
     * @param newSheetName      新sheet名
     * @param setAsCurrentSheet 是否切换为当前sheet
     * @return this
     */
    public T cloneSheet(final int sheetIndex, final String newSheetName, final boolean setAsCurrentSheet) {
        final Sheet sheet;
        if (this.workbook instanceof XSSFWorkbook) {
            final XSSFWorkbook workbook = (XSSFWorkbook) this.workbook;
            sheet = workbook.cloneSheet(sheetIndex, newSheetName);
        } else {
            sheet = this.workbook.cloneSheet(sheetIndex);
            // clone后的sheet的index应该重新获取
            this.workbook.setSheetName(workbook.getSheetIndex(sheet), newSheetName);
        }
        if (setAsCurrentSheet) {
            this.sheet = sheet;
        }
        return (T) this;
    }

    /**
     * 获取指定坐标单元格，单元格不存在时返回{@code null}
     *
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return {@link Cell}
     */
    public Cell getCell(final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return getCell(cellReference.getCol(), cellReference.getRow());
    }

    /**
     * 获取指定坐标单元格，单元格不存在时返回{@code null}
     *
     * @param x X坐标，从0计数，即列号
     * @param y Y坐标，从0计数，即行号
     * @return {@link Cell}
     */
    public Cell getCell(final int x, final int y) {
        return getCell(x, y, false);
    }

    /**
     * 获取或创建指定坐标单元格
     *
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return {@link Cell}
     */
    public Cell getOrCreateCell(final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return getOrCreateCell(cellReference.getCol(), cellReference.getRow());
    }

    /**
     * 获取或创建指定坐标单元格
     *
     * @param x X坐标，从0计数，即列号
     * @param y Y坐标，从0计数，即行号
     * @return {@link Cell}
     */
    public Cell getOrCreateCell(final int x, final int y) {
        return getCell(x, y, true);
    }

    /**
     * 获取指定坐标单元格，如果isCreateIfNotExist为false，则在单元格不存在时返回{@code null}
     *
     * @param locationRef        单元格地址标识符，例如A11，B5
     * @param isCreateIfNotExist 单元格不存在时是否创建
     * @return {@link Cell}
     */
    public Cell getCell(final String locationRef, final boolean isCreateIfNotExist) {
        final CellReference cellReference = new CellReference(locationRef);
        return getCell(cellReference.getCol(), cellReference.getRow(), isCreateIfNotExist);
    }

    /**
     * 获取指定坐标单元格，如果isCreateIfNotExist为false，则在单元格不存在时返回{@code null}
     *
     * @param x                  X坐标，从0计数，即列号
     * @param y                  Y坐标，从0计数，即行号
     * @param isCreateIfNotExist 单元格不存在时是否创建
     * @return {@link Cell}
     */
    public Cell getCell(final int x, final int y, final boolean isCreateIfNotExist) {
        final Row row = isCreateIfNotExist ? RowKit.getOrCreateRow(this.sheet, y) : this.sheet.getRow(y);
        if (null != row) {
            return isCreateIfNotExist ? CellKit.getOrCreateCell(row, x) : row.getCell(x);
        }
        return null;
    }


    /**
     * 获取或者创建行
     *
     * @param y Y坐标，从0计数，即行号
     * @return {@link Row}
     */
    public Row getOrCreateRow(final int y) {
        return RowKit.getOrCreateRow(this.sheet, y);
    }

    /**
     * 为指定单元格获取或者创建样式，返回样式后可以设置样式内容
     *
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return {@link CellStyle}
     */
    public CellStyle getOrCreateCellStyle(final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return getOrCreateCellStyle(cellReference.getCol(), cellReference.getRow());
    }

    /**
     * 为指定单元格获取或者创建样式，返回样式后可以设置样式内容
     *
     * @param x X坐标，从0计数，即列号
     * @param y Y坐标，从0计数，即行号
     * @return {@link CellStyle}
     */
    public CellStyle getOrCreateCellStyle(final int x, final int y) {
        final CellStyle cellStyle = getOrCreateCell(x, y).getCellStyle();
        return Styles.isNullOrDefaultStyle(this.workbook, cellStyle) ? createCellStyle(x, y) : cellStyle;
    }

    /**
     * 为指定单元格创建样式，返回样式后可以设置样式内容
     *
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return {@link CellStyle}
     */
    public CellStyle createCellStyle(final String locationRef) {
        final CellReference cellReference = new CellReference(locationRef);
        return createCellStyle(cellReference.getCol(), cellReference.getRow());
    }

    /**
     * 为指定单元格创建样式，返回样式后可以设置样式内容
     *
     * @param x X坐标，从0计数，即列号
     * @param y Y坐标，从0计数，即行号
     * @return {@link CellStyle}
     */
    public CellStyle createCellStyle(final int x, final int y) {
        final Cell cell = getOrCreateCell(x, y);
        final CellStyle cellStyle = this.workbook.createCellStyle();
        cell.setCellStyle(cellStyle);
        return cellStyle;
    }

    /**
     * 创建单元格样式
     *
     * @return {@link CellStyle}
     * @see Workbook#createCellStyle()
     */
    public CellStyle createCellStyle() {
        return Styles.createCellStyle(this.workbook);
    }

    /**
     * 获取或创建某一行的样式，返回样式后可以设置样式内容
     * 需要注意，此方法返回行样式，设置背景色在单元格设置值后会被覆盖，需要单独设置其单元格的样式。
     *
     * @param y Y坐标，从0计数，即行号
     * @return {@link CellStyle}
     */
    public CellStyle getOrCreateRowStyle(final int y) {
        final CellStyle rowStyle = getOrCreateRow(y).getRowStyle();
        return Styles.isNullOrDefaultStyle(this.workbook, rowStyle) ? createRowStyle(y) : rowStyle;
    }

    /**
     * 创建某一行的样式，返回样式后可以设置样式内容
     *
     * @param y Y坐标，从0计数，即行号
     * @return {@link CellStyle}
     */
    public CellStyle createRowStyle(final int y) {
        final CellStyle rowStyle = this.workbook.createCellStyle();
        getOrCreateRow(y).setRowStyle(rowStyle);
        return rowStyle;
    }

    /**
     * 获取或创建某一列的样式，返回样式后可以设置样式内容
     * 需要注意，此方法返回行样式，设置背景色在单元格设置值后会被覆盖，需要单独设置其单元格的样式。
     *
     * @param x X坐标，从0计数，即列号
     * @return {@link CellStyle}
     */
    public CellStyle getOrCreateColumnStyle(final int x) {
        final CellStyle columnStyle = this.sheet.getColumnStyle(x);
        return Styles.isNullOrDefaultStyle(this.workbook, columnStyle) ? createColumnStyle(x) : columnStyle;
    }

    /**
     * 创建某一列的样式，返回样式后可以设置样式内容
     *
     * @param x X坐标，从0计数，即列号
     * @return {@link CellStyle}
     */
    public CellStyle createColumnStyle(final int x) {
        final CellStyle columnStyle = this.workbook.createCellStyle();
        this.sheet.setDefaultColumnStyle(x, columnStyle);
        return columnStyle;
    }

    /**
     * 创建 {@link Hyperlink}，默认内容（标签为链接地址本身）
     *
     * @param type    链接类型
     * @param address 链接地址
     * @return 链接
     */
    public Hyperlink createHyperlink(final HyperlinkType type, final String address) {
        return createHyperlink(type, address, address);
    }

    /**
     * 创建 {@link Hyperlink}，默认内容
     *
     * @param type    链接类型
     * @param address 链接地址
     * @param label   标签，即单元格中显示的内容
     * @return 链接
     */
    public Hyperlink createHyperlink(final HyperlinkType type, final String address, final String label) {
        final Hyperlink hyperlink = this.workbook.getCreationHelper().createHyperlink(type);
        hyperlink.setAddress(address);
        hyperlink.setLabel(label);
        return hyperlink;
    }

    /**
     * 获取总行数，计算方法为：
     *
     * <pre>
     * 最后一行序号 + 1
     * </pre>
     *
     * @return 行数
     */
    public int getRowCount() {
        return this.sheet.getLastRowNum() + 1;
    }

    /**
     * 获取有记录的行数，计算方法为：
     *
     * <pre>
     * 最后一行序号 - 第一行序号 + 1
     * </pre>
     *
     * @return 行数
     */
    public int getPhysicalRowCount() {
        return this.sheet.getPhysicalNumberOfRows();
    }

    /**
     * 获取第一行总列数，计算方法为：
     *
     * <pre>
     * 最后一列序号 + 1
     * </pre>
     *
     * @return 列数
     */
    public int getColumnCount() {
        return getColumnCount(0);
    }

    /**
     * 获取总列数，计算方法为：
     *
     * <pre>
     * 最后一列序号 + 1
     * </pre>
     *
     * @param rowNum 行号
     * @return 列数，-1表示获取失败
     */
    public int getColumnCount(final int rowNum) {
        final Row row = this.sheet.getRow(rowNum);
        if (null != row) {
            // getLastCellNum方法返回序号+1的值
            return row.getLastCellNum();
        }
        return -1;
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
     * 获取Content-Type头对应的值，可以通过调用以下方法快速设置下载Excel的头信息：
     *
     * <pre>
     * response.setContentType(excelWriter.getContentType());
     * </pre>
     *
     * @return Content-Type值
     */
    public String getContentType() {
        return isXlsx() ? ExcelKit.XLSX_CONTENT_TYPE : ExcelKit.XLS_CONTENT_TYPE;
    }

    /**
     * 获取Content-Disposition头对应的值，可以通过调用以下方法快速设置下载Excel的头信息：
     *
     * <pre>
     * response.setHeader("Content-Disposition", excelWriter.getDisposition("test.xlsx", Charset.UTF_8));
     * </pre>
     *
     * @param fileName 文件名，如果文件名没有扩展名，会自动按照生成Excel类型补齐扩展名，如果提供空，使用随机UUID
     * @param charset  编码，null则使用默认UTF-8编码
     * @return Content-Disposition值
     */
    public String getDisposition(String fileName, java.nio.charset.Charset charset) {
        if (null == charset) {
            charset = Charset.UTF_8;
        }

        if (StringKit.isBlank(fileName)) {
            // 未提供文件名使用随机UUID作为文件名
            fileName = ID.fastSimpleUUID();
        }

        fileName = StringKit.addSuffixIfNot(UrlEncoder.encodeAll(fileName, charset), isXlsx() ? ".xlsx" : ".xls");
        return StringKit.format("attachment; filename=\"{}\"", fileName);
    }

    /**
     * 关闭工作簿
     * 如果用户设定了目标文件，先写出目标文件后给关闭工作簿
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.workbook);
        this.sheet = null;
        this.workbook = null;
        this.isClosed = true;
    }

    /**
     * 获得标题行的别名Map
     *
     * @return 别名Map
     */
    public Map<String, String> getHeaderAlias() {
        return headerAlias;
    }

    /**
     * 设置标题行的别名Map
     *
     * @param headerAlias 别名Map
     * @return this
     */
    public T setHeaderAlias(final Map<String, String> headerAlias) {
        this.headerAlias = headerAlias;
        return (T) this;
    }

    /**
     * 增加标题别名
     *
     * @param header 标题
     * @param alias  别名
     * @return this
     */
    public T addHeaderAlias(final String header, final String alias) {
        Map<String, String> headerAlias = this.headerAlias;
        if (null == headerAlias) {
            headerAlias = new LinkedHashMap<>();
        }
        this.headerAlias = headerAlias;
        this.headerAlias.put(header, alias);
        return (T) this;
    }

    /**
     * 去除标题别名
     *
     * @param header 标题
     * @return this
     */
    public T removeHeaderAlias(final String header) {
        this.headerAlias.remove(header);
        return (T) this;
    }

    /**
     * 清空标题别名，key为Map中的key，value为别名
     *
     * @return this
     */
    public T clearHeaderAlias() {
        this.headerAlias = null;
        return (T) this;
    }

}
