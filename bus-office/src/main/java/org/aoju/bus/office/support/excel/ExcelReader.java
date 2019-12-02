/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.support.excel;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.*;
import org.aoju.bus.office.support.excel.cell.CellEditor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.extractor.ExcelExtractor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel读取器
 * 读取Excel工作簿
 *
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
public class ExcelReader extends ExcelBase<ExcelReader> {

    /**
     * 是否忽略空行
     */
    private boolean ignoreEmptyRow = true;
    /**
     * 单元格值处理接口
     */
    private CellEditor cellEditor;
    /**
     * 标题别名
     */
    private Map<String, String> headerAlias = new HashMap<>();

    /**
     * 构造
     *
     * @param excelFilePath Excel文件路径,绝对路径或相对于ClassPath路径
     * @param sheetIndex    sheet序号,0表示第一个sheet
     */
    public ExcelReader(String excelFilePath, int sheetIndex) {
        this(FileUtils.file(excelFilePath), sheetIndex);
    }

    /**
     * 构造
     *
     * @param bookFile   Excel文件
     * @param sheetIndex sheet序号,0表示第一个sheet
     */
    public ExcelReader(File bookFile, int sheetIndex) {
        this(BookUtils.createBook(bookFile), sheetIndex);
    }

    /**
     * 构造
     *
     * @param bookFile  Excel文件
     * @param sheetName sheet名,第一个默认是sheet1
     */
    public ExcelReader(File bookFile, String sheetName) {
        this(BookUtils.createBook(bookFile), sheetName);
    }

    /**
     * 构造
     *
     * @param bookStream     Excel文件的流
     * @param sheetIndex     sheet序号,0表示第一个sheet
     * @param closeAfterRead 读取结束是否关闭流
     */
    public ExcelReader(InputStream bookStream, int sheetIndex, boolean closeAfterRead) {
        this(BookUtils.createBook(bookStream, closeAfterRead), sheetIndex);
    }

    /**
     * 构造
     *
     * @param bookStream     Excel文件的流
     * @param sheetName      sheet名,第一个默认是sheet1
     * @param closeAfterRead 读取结束是否关闭流
     */
    public ExcelReader(InputStream bookStream, String sheetName, boolean closeAfterRead) {
        this(BookUtils.createBook(bookStream, closeAfterRead), sheetName);
    }

    /**
     * 构造
     *
     * @param book       {@link Workbook} 表示一个Excel文件
     * @param sheetIndex sheet序号,0表示第一个sheet
     */
    public ExcelReader(Workbook book, int sheetIndex) {
        this(book.getSheetAt(sheetIndex));
    }

    /**
     * 构造
     *
     * @param book      {@link Workbook} 表示一个Excel文件
     * @param sheetName sheet名,第一个默认是sheet1
     */
    public ExcelReader(Workbook book, String sheetName) {
        this(book.getSheet(sheetName));
    }

    /**
     * 构造
     *
     * @param sheet Excel中的sheet
     */
    public ExcelReader(Sheet sheet) {
        super(sheet);
    }

    /**
     * 是否忽略空行
     *
     * @return 是否忽略空行
     */
    public boolean isIgnoreEmptyRow() {
        return ignoreEmptyRow;
    }

    /**
     * 设置是否忽略空行
     *
     * @param ignoreEmptyRow 是否忽略空行
     * @return this
     */
    public ExcelReader setIgnoreEmptyRow(boolean ignoreEmptyRow) {
        this.ignoreEmptyRow = ignoreEmptyRow;
        return this;
    }

    /**
     * 设置单元格值处理逻辑
     * 当Excel中的值并不能满足我们的读取要求时,通过传入一个编辑接口,可以对单元格值自定义,例如对数字和日期类型值转换为字符串等
     *
     * @param cellEditor 单元格值处理接口
     * @return this
     */
    public ExcelReader setCellEditor(CellEditor cellEditor) {
        this.cellEditor = cellEditor;
        return this;
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
    public ExcelReader setHeaderAlias(Map<String, String> headerAlias) {
        this.headerAlias = headerAlias;
        return this;
    }

    /**
     * 增加标题别名
     *
     * @param header 标题
     * @param alias  别名
     * @return this
     */
    public ExcelReader addHeaderAlias(String header, String alias) {
        this.headerAlias.put(header, alias);
        return this;
    }

    /**
     * 去除标题别名
     *
     * @param header 标题
     * @return this
     */
    public ExcelReader removeHeaderAlias(String header) {
        this.headerAlias.remove(header);
        return this;
    }

    /**
     * 读取工作簿中指定的Sheet的所有行列数据
     *
     * @return 行的集合, 一行使用List表示
     */
    public List<List<Object>> read() {
        return read(0);
    }

    /**
     * 读取工作簿中指定的Sheet
     *
     * @param startRowIndex 起始行（包含,从0开始计数）
     * @return 行的集合, 一行使用List表示
     */
    public List<List<Object>> read(int startRowIndex) {
        return read(startRowIndex, Integer.MAX_VALUE);
    }

    /**
     * 读取工作簿中指定的Sheet
     *
     * @param startRowIndex 起始行（包含,从0开始计数）
     * @param endRowIndex   结束行（包含,从0开始计数）
     * @return 行的集合, 一行使用List表示
     */
    public List<List<Object>> read(int startRowIndex, int endRowIndex) {
        checkNotClosed();
        List<List<Object>> resultList = new ArrayList<>();

        startRowIndex = Math.max(startRowIndex, this.sheet.getFirstRowNum());// 读取起始行（包含）
        endRowIndex = Math.min(endRowIndex, this.sheet.getLastRowNum());// 读取结束行（包含）
        boolean isFirstLine = true;
        List rowList;
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            rowList = readRow(i);
            if (CollUtils.isNotEmpty(rowList) || false == ignoreEmptyRow) {
                if (null == rowList) {
                    rowList = new ArrayList<>(0);
                }
                if (isFirstLine) {
                    isFirstLine = false;
                    if (MapUtils.isNotEmpty(this.headerAlias)) {
                        rowList = aliasHeader(rowList);
                    }
                }
                resultList.add(rowList);
            }
        }
        return resultList;
    }

    /**
     * 读取Excel为Map的列表,读取所有行,默认第一行做为标题,数据从第二行开始
     * Map表示一行,标题为key,单元格内容为value
     *
     * @return Map的列表
     */
    public List<Map<String, Object>> readAll() {
        return read(0, 1, Integer.MAX_VALUE);
    }

    /**
     * 读取Excel为Map的列表
     * Map表示一行,标题为key,单元格内容为value
     *
     * @param headerRowIndex 标题所在行,如果标题行在读取的内容行中间,这行做为数据将忽略
     * @param startRowIndex  起始行（包含,从0开始计数）
     * @param endRowIndex    读取结束行（包含,从0开始计数）
     * @return Map的列表
     */
    public List<Map<String, Object>> read(int headerRowIndex, int startRowIndex, int endRowIndex) {
        checkNotClosed();
        // 边界判断
        final int firstRowNum = sheet.getFirstRowNum();
        final int lastRowNum = sheet.getLastRowNum();
        if (headerRowIndex < firstRowNum) {
            throw new IndexOutOfBoundsException(StringUtils.format("Header row index {} is lower than first row index {}.", headerRowIndex, firstRowNum));
        } else if (headerRowIndex > lastRowNum) {
            throw new IndexOutOfBoundsException(StringUtils.format("Header row index {} is greater than last row index {}.", headerRowIndex, firstRowNum));
        }
        startRowIndex = Math.max(startRowIndex, firstRowNum);// 读取起始行（包含）
        endRowIndex = Math.min(endRowIndex, lastRowNum);// 读取结束行（包含）

        // 读取header
        List<Object> headerList = readRow(sheet.getRow(headerRowIndex));

        final List<Map<String, Object>> result = new ArrayList<>(endRowIndex - startRowIndex + 1);
        List<Object> rowList;
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            if (i != headerRowIndex) {
                // 跳过标题行
                rowList = readRow(sheet.getRow(i));
                if (CollUtils.isNotEmpty(rowList) || false == ignoreEmptyRow) {
                    if (null == rowList) {
                        rowList = new ArrayList<>(0);
                    }
                    result.add(IterUtils.toMap(aliasHeader(headerList), rowList, true));
                }
            }
        }
        return result;
    }

    /**
     * 读取Excel为Bean的列表,读取所有行,默认第一行做为标题,数据从第二行开始
     *
     * @param <T>      Bean类型
     * @param beanType 每行对应Bean的类型
     * @return Map的列表
     */
    public <T> List<T> readAll(Class<T> beanType) {
        return read(0, 1, Integer.MAX_VALUE, beanType);
    }

    /**
     * 读取Excel为Bean的列表
     *
     * @param <T>            Bean类型
     * @param headerRowIndex 标题所在行,如果标题行在读取的内容行中间,这行做为数据将忽略,,从0开始计数
     * @param startRowIndex  起始行（包含,从0开始计数）
     * @param beanType       每行对应Bean的类型
     * @return Map的列表
     */
    public <T> List<T> read(int headerRowIndex, int startRowIndex, Class<T> beanType) {
        return read(headerRowIndex, startRowIndex, Integer.MAX_VALUE, beanType);
    }

    /**
     * 读取Excel为Bean的列表
     *
     * @param <T>            Bean类型
     * @param headerRowIndex 标题所在行,如果标题行在读取的内容行中间,这行做为数据将忽略,,从0开始计数
     * @param startRowIndex  起始行（包含,从0开始计数）
     * @param endRowIndex    读取结束行（包含,从0开始计数）
     * @param beanType       每行对应Bean的类型
     * @return Map的列表
     */
    public <T> List<T> read(int headerRowIndex, int startRowIndex, int endRowIndex, Class<T> beanType) {
        checkNotClosed();
        final List<Map<String, Object>> mapList = read(headerRowIndex, startRowIndex, endRowIndex);
        if (Map.class.isAssignableFrom(beanType)) {
            return (List<T>) mapList;
        }

        final List<T> beanList = new ArrayList<>(mapList.size());
        for (Map<String, Object> map : mapList) {
            beanList.add(BeanUtils.mapToBean(map, beanType, false));
        }
        return beanList;
    }

    /**
     * 读取为文本格式
     * 使用{@link ExcelExtractor} 提取Excel内容
     *
     * @param withSheetName 是否附带sheet名
     * @return Excel文本
     */
    public String readAsText(boolean withSheetName) {
        final ExcelExtractor extractor = getExtractor();
        extractor.setIncludeSheetNames(withSheetName);
        return extractor.getText();
    }

    /**
     * 获取 {@link ExcelExtractor} 对象
     *
     * @return {@link ExcelExtractor}
     */
    public ExcelExtractor getExtractor() {
        ExcelExtractor extractor;
        Workbook wb = this.workbook;
        if (wb instanceof HSSFWorkbook) {
            extractor = new org.apache.poi.hssf.extractor.ExcelExtractor((HSSFWorkbook) wb);
        } else {
            extractor = new XSSFExcelExtractor((XSSFWorkbook) wb);
        }
        return extractor;
    }

    /**
     * 读取某一行数据
     *
     * @param rowIndex 行号,从0开始
     * @return 一行数据
     */
    public List<Object> readRow(int rowIndex) {
        return readRow(this.sheet.getRow(rowIndex));
    }

    /**
     * 读取某个单元格的值
     *
     * @param x X坐标,从0计数,既列号
     * @param y Y坐标,从0计数,既行号
     * @return 值, 如果单元格无值返回null
     */
    public Object readCellValue(int x, int y) {
        return CellUtils.getCellValue(getCell(x, y), this.cellEditor);
    }

    /**
     * 获取Excel写出器
     * 在读取Excel并做一定编辑后,获取写出器写出
     *
     * @return {@link ExcelWriter}
     */
    public ExcelWriter getWriter() {
        return new ExcelWriter(this.sheet);
    }

    /**
     * 读取一行
     *
     * @param row 行
     * @return 单元格值列表
     */
    private List<Object> readRow(Row row) {
        return RowUtils.readRow(row, this.cellEditor);
    }

    /**
     * 转换标题别名,如果没有别名则使用原标题,当标题为空时,列号对应的字母便是header
     *
     * @param headerList 原标题列表
     * @return 转换别名列表
     */
    private List<String> aliasHeader(List<Object> headerList) {
        final int size = headerList.size();
        final ArrayList<String> result = new ArrayList<>(size);
        if (CollUtils.isEmpty(headerList)) {
            return result;
        }

        for (int i = 0; i < size; i++) {
            result.add(aliasHeader(headerList.get(i), i));
        }
        return result;
    }

    /**
     * 转换标题别名,如果没有别名则使用原标题,当标题为空时,列号对应的字母便是header
     *
     * @param headerObj 原标题
     * @param index     标题所在列号,当标题为空时,列号对应的字母便是header
     * @return 转换别名列表
     */
    private String aliasHeader(Object headerObj, int index) {
        if (null == headerObj) {
            return ExcelUtils.indexToColName(index);
        }

        final String header = headerObj.toString();
        return ObjectUtils.defaultIfNull(this.headerAlias.get(header), header);
    }

    /**
     * 检查是否未关闭状态
     */
    private void checkNotClosed() {
        Assert.isFalse(this.isClosed, "ExcelReader has been closed!");
    }

}
