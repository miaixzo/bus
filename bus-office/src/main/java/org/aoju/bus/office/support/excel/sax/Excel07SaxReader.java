/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.support.excel.sax;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.text.Builders;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.MathKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.support.excel.ExcelSaxKit;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Sax方式读取Excel文件
 * Excel2007格式说明见：http://www.cnblogs.com/wangmingshun/p/6654143.html
 *
 * @author Kimi Liu
 * @version 6.1.8
 * @since JDK 1.8+
 */
public class Excel07SaxReader extends DefaultHandler implements ExcelSaxReader<Excel07SaxReader> {

    // sheet r:Id前缀
    public static final String RID_PREFIX = "rId";
    // 上一次的内容
    private final Builders lastContent = StringKit.builders();
    // 单元格的格式表，对应style.xml
    private StylesTable stylesTable;
    // excel 2007 的共享字符串表,对应sharedString.xml
    private SharedStringsTable sharedStringsTable;
    // sheet的索引
    private int sheetIndex;
    // 当前非空行
    private int index;
    // 当前列
    private int curCell;
    // 单元数据类型
    private CellDataType cellDataType;
    // 当前行号，从0开始
    private long rowNumber;
    // 当前列坐标， 如A1，B5
    private String curCoordinate;
    // 前一个列的坐标
    private String preCoordinate;
    // 行的最大列坐标
    private String maxCellCoordinate;
    // 单元格样式
    private XSSFCellStyle xssfCellStyle;
    // 单元格存储的格式化字符串，nmtFmt的formatCode属性的值
    private String numFmtString;
    // 存储每行的列元素
    private List<Object> rowCellList = new ArrayList<>();

    /**
     * 行处理器
     */
    private RowHandler rowHandler;

    /**
     * 构造
     *
     * @param rowHandler 行处理器
     */
    public Excel07SaxReader(RowHandler rowHandler) {
        this.rowHandler = rowHandler;
    }

    /**
     * 设置行处理器
     *
     * @param rowHandler 行处理器
     * @return this
     */
    public Excel07SaxReader setRowHandler(RowHandler rowHandler) {
        this.rowHandler = rowHandler;
        return this;
    }

    @Override
    public Excel07SaxReader read(File file, int rid) throws InstrumentException {
        return read(file, RID_PREFIX + rid);
    }

    @Override
    public Excel07SaxReader read(File file, String idOrRid) throws InstrumentException {
        try {
            return read(OPCPackage.open(file), idOrRid);
        } catch (InvalidFormatException e) {
            throw new InstrumentException(e);
        }
    }

    @Override
    public Excel07SaxReader read(InputStream in, int rid) throws InstrumentException {
        return read(in, RID_PREFIX + rid);
    }

    @Override
    public Excel07SaxReader read(InputStream in, String idOrRid) throws InstrumentException {
        try (final OPCPackage opcPackage = OPCPackage.open(in)) {
            return read(opcPackage, idOrRid);
        } catch (IOException | InvalidFormatException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 开始读取Excel，Sheet编号从0开始计数
     *
     * @param opcPackage {@link OPCPackage}，Excel包，读取后不关闭
     * @param rid        Excel中的sheet rid编号，如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    public Excel07SaxReader read(OPCPackage opcPackage, int rid) throws InstrumentException {
        return read(opcPackage, RID_PREFIX + rid);
    }

    /**
     * 开始读取Excel，Sheet编号从0开始计数
     *
     * @param opcPackage {@link OPCPackage}，Excel包，读取后不关闭
     * @param idOrRid    Excel中的sheet id或者rid编号，rid必须加rId前缀，例如rId1，如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    public Excel07SaxReader read(OPCPackage opcPackage, String idOrRid) throws InstrumentException {
        try {
            return read(new XSSFReader(opcPackage), idOrRid);
        } catch (OpenXML4JException e) {
            throw new InstrumentException(e);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 开始读取Excel，Sheet编号从0开始计数
     *
     * @param xssfReader {@link XSSFReader}，Excel读取器
     * @param idOrRid    Excel中的sheet id或者rid编号，rid必须加rId前缀，例如rId1，如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    public Excel07SaxReader read(XSSFReader xssfReader, String idOrRid) throws InstrumentException {
        // 获取共享样式表，样式非必须
        try {
            this.stylesTable = xssfReader.getStylesTable();
        } catch (IOException | InvalidFormatException ignore) {
            // ignore
        }

        // 获取共享字符串表
        try {
            this.sharedStringsTable = xssfReader.getSharedStringsTable();
        } catch (IOException | InvalidFormatException e) {
            throw new InstrumentException(e);
        }

        return readSheets(xssfReader, idOrRid);
    }

    /**
     * 读到一个xml开始标签时的回调处理方法
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (ElementName.row.match(localName)) {// 行开始
            startRow(attributes);
        } else if (ElementName.c.match(localName)) {// 单元格元素
            startCell(attributes);
        }
    }

    /**
     * 标签结束的回调处理方法
     */
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (ElementName.c.match(localName)) { // 单元格结束
            endCell();
        } else if (ElementName.row.match(localName)) {// 行结束
            endRow();
        }
    }

    /**
     * s标签结束的回调处理方法
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        // 得到单元格内容的值
        lastContent.append(ch, start, length);
    }

    @Override
    public void setDocumentLocator(Locator locator) {

    }

    /**
     * 开始读取Excel，Sheet编号从0开始计数
     *
     * @param xssfReader {@link XSSFReader}，Excel读取器
     * @param idOrRid    Excel中的sheet id或者rid编号，rid必须加rId前缀，例如rId0，如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    private Excel07SaxReader readSheets(XSSFReader xssfReader, String idOrRid) throws InstrumentException {
        // 将sheetId转换为rid
        if (MathKit.isInteger(idOrRid)) {
            final SheetSaxReader ridReader = new SheetSaxReader();
            final String rid = ridReader.read(xssfReader).getRidBySheetId(idOrRid);
            if (StringKit.isNotEmpty(rid)) {
                idOrRid = rid;
            }
        }
        this.sheetIndex = Integer.parseInt(StringKit.removePrefixIgnoreCase(idOrRid, RID_PREFIX));
        InputStream sheetInputStream = null;
        try {
            if (this.sheetIndex > -1) {
                // 根据 rId# 或 rSheet# 查找sheet
                sheetInputStream = xssfReader.getSheet(RID_PREFIX + (this.sheetIndex + 1));
                ExcelSaxKit.readFrom(sheetInputStream, this);
                rowHandler.doAfterAllAnalysed();
            } else {
                this.sheetIndex = -1;
                // 遍历所有sheet
                final Iterator<InputStream> sheetInputStreams = xssfReader.getSheetsData();
                while (sheetInputStreams.hasNext()) {
                    // 重新读取一个sheet时行归零
                    index = 0;
                    this.sheetIndex++;
                    sheetInputStream = sheetInputStreams.next();
                    ExcelSaxKit.readFrom(sheetInputStream, this);
                    rowHandler.doAfterAllAnalysed();
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InstrumentException(e);
        } finally {
            IoKit.close(sheetInputStream);
        }
        return this;
    }

    /**
     * 行开始
     *
     * @param attributes 属性列表
     */
    private void startRow(Attributes attributes) {
        this.rowNumber = Long.parseLong(AttributeName.r.getValue(attributes)) - 1;
    }

    /**
     * 单元格开始
     *
     * @param attributes 属性列表
     */
    private void startCell(Attributes attributes) {
        // 获取当前列坐标
        final String tempCurCoordinate = AttributeName.r.getValue(attributes);
        // 前一列为null，则将其设置为"@",A为第一列，ascii码为65，前一列即为@，ascii码64
        if (preCoordinate == null) {
            preCoordinate = String.valueOf(ExcelSaxKit.CELL_FILL_CHAR);
        } else {
            // 存在，则前一列要设置为上一列的坐标
            preCoordinate = curCoordinate;
        }
        // 重置当前列
        curCoordinate = tempCurCoordinate;
        // 设置单元格类型
        setCellType(attributes);

        // 清空之前的数据
        lastContent.reset();
    }

    /**
     * 一个单元格结尾
     */
    private void endCell() {
        final String contentStr = StringKit.trim(lastContent);
        final Object value = ExcelSaxKit.getDataValue(this.cellDataType, contentStr, this.sharedStringsTable, this.numFmtString);
        // 补全单元格之间的空格
        fillBlankCell(preCoordinate, curCoordinate, false);
        addCellValue(curCell++, value);
    }

    /**
     * 一行结尾
     */
    private void endRow() {
        // 最大列坐标以第一个非空行的为准
        if (index == 0) {
            maxCellCoordinate = curCoordinate;
        }

        // 补全一行尾部可能缺失的单元格
        if (maxCellCoordinate != null) {
            fillBlankCell(curCoordinate, maxCellCoordinate, true);
        }

        rowHandler.handle(sheetIndex, rowNumber, rowCellList);

        // 一行结束
        // 新建一个新列，之前的列抛弃（可能被回收或rowHandler处理）
        rowCellList = new ArrayList<>(curCell + 1);
        // 行数增加
        index++;
        // 当前列置0
        curCell = 0;
        // 置空当前列坐标和前一列坐标
        curCoordinate = null;
        preCoordinate = null;
    }

    /**
     * 在一行中的指定列增加值
     *
     * @param index 位置
     * @param value 值
     */
    private void addCellValue(int index, Object value) {
        this.rowCellList.add(index, value);
        this.rowHandler.handleCell(this.sheetIndex, this.rowNumber, index, value, this.xssfCellStyle);
    }

    /**
     * 填充空白单元格，如果前一个单元格大于后一个，不需要填充
     *
     * @param preCoordinate 前一个单元格坐标
     * @param curCoordinate 当前单元格坐标
     * @param isEnd         是否为最后一个单元格
     */
    private void fillBlankCell(String preCoordinate, String curCoordinate, boolean isEnd) {
        if (false == curCoordinate.equals(preCoordinate)) {
            int len = ExcelSaxKit.countNullCell(preCoordinate, curCoordinate);
            if (isEnd) {
                len++;
            }
            while (len-- > 0) {
                addCellValue(curCell++, Normal.EMPTY);
            }
        }
    }

    /**
     * 设置单元格的类型
     *
     * @param attributes 属性
     */
    private void setCellType(Attributes attributes) {
        numFmtString = Normal.EMPTY;
        this.cellDataType = CellDataType.of(AttributeName.t.getValue(attributes));

        // 获取单元格的xf索引，对应style.xml中cellXfs的子元素xf
        if (null != this.stylesTable) {
            final String xfIndexStr = AttributeName.s.getValue(attributes);
            if (null != xfIndexStr) {
                this.xssfCellStyle = stylesTable.getStyleAt(Integer.parseInt(xfIndexStr));
                // 单元格存储格式的索引，对应style.xml中的numFmts元素的子元素索引
                final int numFmtIndex = xssfCellStyle.getDataFormat();
                this.numFmtString = ObjectKit.defaultIfNull(
                        xssfCellStyle.getDataFormatString(),
                        BuiltinFormats.getBuiltinFormat(numFmtIndex));
                if (CellDataType.NUMBER == this.cellDataType && Builder.isDateFormat(numFmtIndex, numFmtString)) {
                    cellDataType = CellDataType.DATE;
                }
            }
        }
    }

    /**
     * 标签名枚举
     */
    public enum ElementName {
        /**
         * 行标签名，表示一行
         */
        row,
        /**
         * 单元格标签名，表示一个单元格
         */
        c;

        /**
         * 给定标签名是否匹配当前标签
         *
         * @param elementName 标签名
         * @return 是否匹配
         */
        public boolean match(String elementName) {
            return this.name().equals(elementName);
        }
    }

    /**
     * Excel的XML中属性名枚举
     */
    public enum AttributeName {

        /**
         * 行列号属性，行标签下此为行号属性名，cell标签下下为列号属性名
         */
        r,
        /**
         * ST（StylesTable） 的索引，样式index，用于获取行或单元格样式
         */
        s,
        /**
         * Type类型，单元格类型属性，见{@link CellDataType}
         */
        t;

        /**
         * 是否匹配给定属性
         *
         * @param attributeName 属性
         * @return 是否匹配
         */
        public boolean match(String attributeName) {
            return this.name().equals(attributeName);
        }

        /**
         * 从属性里列表中获取对应属性值
         *
         * @param attributes 属性列表
         * @return 属性值
         */
        public String getValue(Attributes attributes) {
            return attributes.getValue(name());
        }
    }

}
