/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.excel;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.function.XBiConsumer;
import org.aoju.bus.core.toolkit.*;
import org.aoju.bus.office.excel.cell.CellEditor;
import org.aoju.bus.office.excel.reader.ListSheetReader;
import org.aoju.bus.office.excel.reader.SheetReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.extractor.ExcelExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel读取器
 * 读取Excel工作簿
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * 构造
     *
     * @param sheet Excel中的sheet
     */
    public ExcelReader(Sheet sheet) {
        super(sheet);
    }

    /**
     * 构造
     *
     * @param excelFilePath Excel文件路径，绝对路径或相对于ClassPath路径
     * @param sheetIndex    sheet序号,0表示第一个sheet
     */
    public ExcelReader(String excelFilePath, int sheetIndex) {
        this(FileKit.file(excelFilePath), sheetIndex);
    }

    /**
     * 构造
     *
     * @param bookFile   Excel文件
     * @param sheetIndex sheet序号,0表示第一个sheet
     */
    public ExcelReader(File bookFile, int sheetIndex) {
        this(WorksKit.createBook(bookFile, true), sheetIndex);
        this.destFile = bookFile;
    }

    /**
     * 构造
     *
     * @param bookFile  Excel文件
     * @param sheetName sheet名,第一个默认是sheet1
     */
    public ExcelReader(File bookFile, String sheetName) {
        this(WorksKit.createBook(bookFile, true), sheetName);
        this.destFile = bookFile;
    }

    /**
     * 构造
     *
     * @param bookStream Excel文件的流
     * @param sheetIndex sheet序号，0表示第一个sheet
     */
    public ExcelReader(InputStream bookStream, int sheetIndex) {
        this(WorksKit.createBook(bookStream), sheetIndex);
    }

    /**
     * 构造
     *
     * @param bookStream Excel文件的流
     * @param sheetName  sheet名，第一个默认是sheet1
     */
    public ExcelReader(InputStream bookStream, String sheetName) {
        this(WorksKit.createBook(bookStream), sheetName);
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
     * @param excelFilePath Excel文件路径，绝对路径或相对于ClassPath路径
     * @param sheetName     sheet名，第一个默认是sheet1
     */
    public ExcelReader(String excelFilePath, String sheetName) {
        this(FileKit.file(excelFilePath), sheetName);
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
     * @param startRowIndex 起始行(包含,从0开始计数)
     * @return 行的集合, 一行使用List表示
     */
    public List<List<Object>> read(int startRowIndex) {
        return read(startRowIndex, Integer.MAX_VALUE);
    }

    /**
     * 读取工作簿中指定的Sheet
     *
     * @param startRowIndex 起始行(包含,从0开始计数)
     * @param endRowIndex   结束行(包含,从0开始计数)
     * @return 行的集合, 一行使用List表示
     */
    public List<List<Object>> read(int startRowIndex, int endRowIndex) {
        checkNotClosed();
        List<List<Object>> resultList = new ArrayList<>();

        startRowIndex = Math.max(startRowIndex, this.sheet.getFirstRowNum());// 读取起始行(包含)
        endRowIndex = Math.min(endRowIndex, this.sheet.getLastRowNum());// 读取结束行(包含)
        boolean isFirstLine = true;
        List rowList;
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            rowList = readRow(i);
            if (CollKit.isNotEmpty(rowList) || false == ignoreEmptyRow) {
                if (null == rowList) {
                    rowList = new ArrayList<>(0);
                }
                if (isFirstLine) {
                    isFirstLine = false;
                    if (MapKit.isNotEmpty(this.headerAlias)) {
                        rowList = aliasHeader(rowList);
                    }
                }
                resultList.add(rowList);
            }
        }
        return resultList;
    }


    /**
     * 读取数据为指定类型
     *
     * @param <T>         读取数据类型
     * @param sheetReader {@link SheetReader}实现
     * @return 数据读取结果
     */
    public <T> T read(SheetReader<T> sheetReader) {
        checkNotClosed();
        return Assert.notNull(sheetReader).read(this.sheet);
    }

    /**
     * 读取工作簿中指定的Sheet，此方法为类流处理方式，当读到指定单元格时，会调用CellEditor接口
     * 用户通过实现此接口，可以更加灵活的处理每个单元格的数据
     *
     * @param cellHandler 单元格处理器，用于处理读到的单元格及其数据
     */
    public void read(XBiConsumer<Cell, Object> cellHandler) {
        read(0, Integer.MAX_VALUE, cellHandler);
    }

    /**
     * 读取工作簿中指定的Sheet
     *
     * @param startRowIndex  起始行（包含，从0开始计数）
     * @param endRowIndex    结束行（包含，从0开始计数）
     * @param aliasFirstLine 是否首行作为标题行转换别名
     * @return 行的集合，一行使用List表示
     */
    public List<List<Object>> read(int startRowIndex, int endRowIndex, boolean aliasFirstLine) {
        final ListSheetReader reader = new ListSheetReader(startRowIndex, endRowIndex, aliasFirstLine);
        reader.setCellEditor(this.cellEditor);
        reader.setIgnoreEmptyRow(this.ignoreEmptyRow);
        reader.setHeaderAlias(headerAlias);
        return read(reader);
    }

    /**
     * 读取工作簿中指定的Sheet，此方法为类流处理方式，当读到指定单元格时，会调用CellEditor接口
     * 用户通过实现此接口，可以更加灵活的处理每个单元格的数据
     *
     * @param startRowIndex 起始行（包含，从0开始计数）
     * @param endRowIndex   结束行（包含，从0开始计数）
     * @param cellHandler   单元格处理器，用于处理读到的单元格及其数据
     */
    public void read(int startRowIndex, int endRowIndex, XBiConsumer<Cell, Object> cellHandler) {
        checkNotClosed();

        // 读取起始行（包含）
        startRowIndex = Math.max(startRowIndex, this.sheet.getFirstRowNum());
        // 读取结束行（包含）
        endRowIndex = Math.min(endRowIndex, this.sheet.getLastRowNum());

        Row row;
        short columnSize;
        for (int y = startRowIndex; y <= endRowIndex; y++) {
            row = this.sheet.getRow(y);
            if (null != row) {
                columnSize = row.getLastCellNum();
                Cell cell;
                for (short x = 0; x < columnSize; x++) {
                    cell = row.getCell(x);
                    cellHandler.accept(cell, CellKit.getCellValue(cell));
                }
            }
        }
    }

    /**
     * 读取Excel为Map的列表
     * Map表示一行,标题为key,单元格内容为value
     *
     * @param headerRowIndex 标题所在行,如果标题行在读取的内容行中间,这行做为数据将忽略
     * @param startRowIndex  起始行(包含,从0开始计数)
     * @param endRowIndex    读取结束行(包含,从0开始计数)
     * @return Map的列表
     */
    public List<Map<String, Object>> read(int headerRowIndex, int startRowIndex, int endRowIndex) {
        checkNotClosed();
        // 边界判断
        final int firstRowNum = sheet.getFirstRowNum();
        final int lastRowNum = sheet.getLastRowNum();
        if (headerRowIndex < firstRowNum) {
            throw new IndexOutOfBoundsException(StringKit.format("Header row index {} is lower than first row index {}.", headerRowIndex, firstRowNum));
        } else if (headerRowIndex > lastRowNum) {
            throw new IndexOutOfBoundsException(StringKit.format("Header row index {} is greater than last row index {}.", headerRowIndex, firstRowNum));
        }
        startRowIndex = Math.max(startRowIndex, firstRowNum);// 读取起始行(包含)
        endRowIndex = Math.min(endRowIndex, lastRowNum);// 读取结束行(包含)

        // 读取header
        List<Object> headerList = readRow(sheet.getRow(headerRowIndex));

        final List<Map<String, Object>> result = new ArrayList<>(endRowIndex - startRowIndex + 1);
        List<Object> rowList;
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            if (i != headerRowIndex) {
                // 跳过标题行
                rowList = readRow(sheet.getRow(i));
                if (CollKit.isNotEmpty(rowList) || false == ignoreEmptyRow) {
                    result.add(IterKit.toMap(aliasHeader(headerList), rowList, true));
                }
            }
        }
        return result;
    }

    /**
     * 读取Excel为Bean的列表
     *
     * @param <T>            Bean类型
     * @param headerRowIndex 标题所在行,如果标题行在读取的内容行中间,这行做为数据将忽略,从0开始计数
     * @param startRowIndex  起始行(包含,从0开始计数)
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
     * @param startRowIndex  起始行(包含,从0开始计数)
     * @param endRowIndex    读取结束行(包含,从0开始计数)
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
            beanList.add(BeanKit.toBeanIgnoreCase(map, beanType, false));
        }
        return beanList;
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
     * 获取 {@link ExcelExtractor} 对象
     *
     * @param wb 工作薄
     * @return {@link ExcelExtractor}
     */
    public ExcelExtractor getExtractor(Workbook wb) {
        ExcelExtractor extractor;
        if (wb instanceof HSSFWorkbook) {
            extractor = new org.apache.poi.hssf.extractor.ExcelExtractor((HSSFWorkbook) wb);
        } else {
            extractor = new XSSFExcelExtractor((XSSFWorkbook) wb);
        }
        return extractor;
    }

    /**
     * 读取为文本格式
     * 使用{@link ExcelExtractor} 提取Excel内容
     *
     * @param wb            {@link Workbook}
     * @param withSheetName 是否附带sheet名
     * @return Excel文本
     */
    public String readAsText(Workbook wb, boolean withSheetName) {
        final ExcelExtractor extractor = getExtractor(wb);
        extractor.setIncludeSheetNames(withSheetName);
        return extractor.getText();
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
        return CellKit.getCellValue(getCell(x, y), this.cellEditor);
    }

    /**
     * 获取Excel写出器
     * 在读取Excel并做一定编辑后,获取写出器写出
     *
     * @return {@link ExcelWriter}
     */
    public ExcelWriter getWriter() {
        return ExcelKit.getWriter(this.destFile, this.sheet.getSheetName());
    }

    /**
     * 读取一行
     *
     * @param row 行
     * @return 单元格值列表
     */
    private List<Object> readRow(Row row) {
        return RowKit.readRow(row, this.cellEditor);
    }

    /**
     * 转换标题别名,如果没有别名则使用原标题,当标题为空时,列号对应的字母便是header
     *
     * @param headerList 原标题列表
     * @return 转换别名列表
     */
    private List<String> aliasHeader(List<Object> headerList) {
        if (CollKit.isEmpty(headerList)) {
            return new ArrayList<>(0);
        }

        final int size = headerList.size();
        final ArrayList<String> result = new ArrayList<>(size);
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
            return ExcelKit.indexToColName(index);
        }

        final String header = headerObj.toString();
        return ObjectKit.defaultIfNull(this.headerAlias.get(header), header);
    }

    /**
     * 检查是否未关闭状态
     */
    private void checkNotClosed() {
        Assert.isFalse(this.isClosed, "ExcelReader has been closed!");
    }

}
