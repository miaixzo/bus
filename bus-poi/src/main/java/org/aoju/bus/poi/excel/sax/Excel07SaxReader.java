/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.poi.excel.sax;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.poi.excel.ExcelSaxUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

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
 * @version 3.0.6
 * @since JDK 1.8
 */
public class Excel07SaxReader extends AbstractExcelSaxReader<Excel07SaxReader> implements ContentHandler {

    // saxParser
    private static final String CLASS_SAXPARSER = "org.apache.xerces.parsers.SAXParser";
    /**
     * Cell单元格元素
     */
    private static final String C_ELEMENT = "c";
    /**
     * 行元素
     */
    private static final String ROW_ELEMENT = "row";
    /**
     * Cell中的行列号
     */
    private static final String R_ATTR = "r";
    /**
     * Cell类型
     */
    private static final String T_ELEMENT = "t";
    /**
     * SST（SharedStringsTable） 的索引
     */
    private static final String S_ATTR_VALUE = "s";
    // 列中属性值
    private static final String T_ATTR_VALUE = "t";
    // sheet r:Id前缀
    private static final String RID_PREFIX = "rId";
    // 存储每行的列元素
    List<Object> rowCellList = new ArrayList<>();
    // excel 2007 的共享字符串表,对应sharedString.xml
    private SharedStringsTable sharedStringsTable;
    // 当前行
    private int curRow;
    // 当前列
    private int curCell;
    // 上一次的内容
    private String lastContent;
    // 单元数据类型
    private CellDataType cellDataType;
    // 当前列坐标， 如A1，B5
    private String curCoordinate;
    // 前一个列的坐标
    private String preCoordinate;
    // 行的最大列坐标
    private String maxCellCoordinate;
    // 单元格的格式表，对应style.xml
    private StylesTable stylesTable;
    // 单元格存储格式的索引，对应style.xml中的numFmts元素的子元素索引
    private int numFmtIndex;
    // 单元格存储的格式化字符串，nmtFmt的formateCode属性的值
    private String numFmtString;
    // sheet的索引
    private int sheetIndex;
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
        try {
            return read(OPCPackage.open(file), rid);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    @Override
    public Excel07SaxReader read(InputStream in, int rid) throws InstrumentException {
        try {
            return read(OPCPackage.open(in), rid);
        } catch (InstrumentException e) {
            throw e;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 开始读取Excel，Sheet编号从0开始计数
     *
     * @param opcPackage {@link OPCPackage}，Excel包
     * @param rid        Excel中的sheet rid编号，如果为-1处理所有编号的sheet
     * @return this
     * @throws InstrumentException POI异常
     */
    public Excel07SaxReader read(OPCPackage opcPackage, int rid) throws InstrumentException {
        InputStream sheetInputStream = null;
        try {
            final XSSFReader xssfReader = new XSSFReader(opcPackage);

            // 获取共享样式表
            stylesTable = xssfReader.getStylesTable();
            // 获取共享字符串表
            this.sharedStringsTable = xssfReader.getSharedStringsTable();

            if (rid > -1) {
                this.sheetIndex = rid;
                // 根据 rId# 或 rSheet# 查找sheet
                sheetInputStream = xssfReader.getSheet(RID_PREFIX + (rid + 1));
                parse(sheetInputStream);
            } else {
                this.sheetIndex = -1;
                // 遍历所有sheet
                final Iterator<InputStream> sheetInputStreams = xssfReader.getSheetsData();
                while (sheetInputStreams.hasNext()) {
                    // 重新读取一个sheet时行归零
                    curRow = 0;
                    this.sheetIndex++;
                    sheetInputStream = sheetInputStreams.next();
                    parse(sheetInputStream);
                }
            }
        } catch (InstrumentException e) {
            throw e;
        } catch (Exception e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(sheetInputStream);
            IoUtils.close(opcPackage);
        }
        return this;
    }

    /**
     * 读到一个xml开始标签时的回调处理方法
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // 单元格元素
        if (C_ELEMENT.equals(qName)) {

            // 获取当前列坐标
            String tempCurCoordinate = attributes.getValue(R_ATTR);
            // 前一列为null，则将其设置为"@",A为第一列，ascii码为65，前一列即为@，ascii码64
            if (preCoordinate == null) {
                preCoordinate = String.valueOf(ExcelSaxUtils.CELL_FILL_CHAR);
            } else {
                // 存在，则前一列要设置为上一列的坐标
                preCoordinate = curCoordinate;
            }
            // 重置当前列
            curCoordinate = tempCurCoordinate;
            // 设置单元格类型
            setCellType(attributes);
        }

        lastContent = "";
    }

    /**
     * 设置单元格的类型
     *
     * @param attribute
     */
    private void setCellType(Attributes attribute) {
        // 重置numFmtIndex,numFmtString的值
        numFmtIndex = 0;
        numFmtString = "";
        this.cellDataType = CellDataType.of(attribute.getValue(T_ATTR_VALUE));

        // 获取单元格的xf索引，对应style.xml中cellXfs的子元素xf
        final String xfIndexStr = attribute.getValue(S_ATTR_VALUE);
        if (xfIndexStr != null) {
            int xfIndex = Integer.parseInt(xfIndexStr);
            XSSFCellStyle xssfCellStyle = stylesTable.getStyleAt(xfIndex);
            numFmtIndex = xssfCellStyle.getDataFormat();
            numFmtString = xssfCellStyle.getDataFormatString();

            if (numFmtString == null) {
                numFmtString = BuiltinFormats.getBuiltinFormat(numFmtIndex);
            } else if (CellDataType.NUMBER == this.cellDataType && org.apache.poi.ss.usermodel.DateUtil.isADateFormat(numFmtIndex, numFmtString)) {
                cellDataType = CellDataType.DATE;
            }
        }

    }

    /**
     * 标签结束的回调处理方法
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        final String contentStr = StringUtils.trim(lastContent);

        if (T_ELEMENT.equals(qName)) {
            // type标签
            // rowCellList.add(curCell++, contentStr);
        } else if (C_ELEMENT.equals(qName)) {
            // cell标签
            Object value = ExcelSaxUtils.getDataValue(this.cellDataType, contentStr, this.sharedStringsTable, this.numFmtString);
            // 补全单元格之间的空格
            fillBlankCell(preCoordinate, curCoordinate, false);
            rowCellList.add(curCell++, value);
        } else if (ROW_ELEMENT.equals(qName)) {
            // 如果是row标签，说明已经到了一行的结尾
            // 最大列坐标以第一行的为准
            if (curRow == 0) {
                maxCellCoordinate = curCoordinate;
            }

            // 补全一行尾部可能缺失的单元格
            if (maxCellCoordinate != null) {
                fillBlankCell(curCoordinate, maxCellCoordinate, true);
            }

            rowHandler.handle(sheetIndex, curRow, rowCellList);

            // 一行结束
            // 清空rowCellList,
            rowCellList.clear();
            // 行数增加
            curRow++;
            // 当前列置0
            curCell = 0;
            // 置空当前列坐标和前一列坐标
            curCoordinate = null;
            preCoordinate = null;
        }
    }

    /**
     * s标签结束的回调处理方法
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // 得到单元格内容的值
        lastContent = lastContent.concat(new String(ch, start, length));
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // pass
    }

    /**
     * ?xml标签的回调处理方法
     */
    @Override
    public void startDocument() throws SAXException {
        // pass
    }

    @Override
    public void endDocument() throws SAXException {
        // pass
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // pass
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // pass
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // pass
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        // pass
    }

    @Override
    public void skippedEntity(String name) throws SAXException {

    }

    /**
     * 处理流中的Excel数据
     *
     * @param sheetInputStream sheet流
     * @throws IOException  IO异常
     * @throws SAXException SAX异常
     */
    private void parse(InputStream sheetInputStream) throws IOException, SAXException {
        fetchSheetReader().parse(new InputSource(sheetInputStream));
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
            int len = ExcelSaxUtils.countNullCell(preCoordinate, curCoordinate);
            if (isEnd) {
                len++;
            }
            while (len-- > 0) {
                rowCellList.add(curCell++, "");
            }
        }
    }

    /**
     * 获取sheet的解析器
     *
     * @return {@link XMLReader}
     * @throws SAXException SAX异常
     */
    private XMLReader fetchSheetReader() throws SAXException {
        XMLReader xmlReader = null;
        try {
            xmlReader = XMLReaderFactory.createXMLReader(CLASS_SAXPARSER);
        } catch (SAXException e) {
            if (e.getMessage().contains("org.apache.xerces.parsers.SAXParser")) {
                throw new InstrumentException("You need to add 'xerces:xercesImpl' to your project and version >= 2.11.0");
            } else {
                throw e;
            }
        }
        xmlReader.setContentHandler(this);
        return xmlReader;
    }

}
