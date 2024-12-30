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
package org.miaixz.bus.office.excel.xyz;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DependencyException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.RevisedException;
import org.miaixz.bus.core.lang.exception.TerminateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.Builder;
import org.miaixz.bus.office.excel.sax.CellDataType;
import org.miaixz.bus.office.excel.sax.Excel03SaxReader;
import org.miaixz.bus.office.excel.sax.Excel07SaxReader;
import org.miaixz.bus.office.excel.sax.ExcelSaxReader;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Sax方式读取Excel相关工具类
 *
 * @author Kimi Liu
 */
public class ExcelSaxKit {

    /**
     * 创建 {@link ExcelSaxReader}
     *
     * @param isXlsx     是否为xlsx格式（07格式）
     * @param rowHandler 行处理器
     * @return {@link ExcelSaxReader}
     */
    public static ExcelSaxReader<?> createSaxReader(final boolean isXlsx, final RowHandler rowHandler) {
        return isXlsx ? new Excel07SaxReader(rowHandler) : new Excel03SaxReader(rowHandler);
    }

    /**
     * 根据数据类型获取数据
     *
     * @param cellDataType  数据类型枚举
     * @param value         数据值
     * @param sharedStrings {@link SharedStrings}
     * @param numFmtString  数字格式名
     * @return 数据值
     */
    public static Object getDataValue(CellDataType cellDataType, final String value, final SharedStrings sharedStrings,
            final String numFmtString) {
        if (null == value) {
            return null;
        }

        if (null == cellDataType) {
            cellDataType = CellDataType.NULL;
        }

        Object result = null;
        switch (cellDataType) {
        case BOOL:
            result = (value.charAt(0) != '0');
            break;
        case ERROR:
            result = StringKit.format("\\\"ERROR: {} ", value);
            break;
        case FORMULA:
            result = StringKit.format("\"{}\"", value);
            break;
        case INLINESTR:
            result = new XSSFRichTextString(value).toString();
            break;
        case SSTINDEX:
            try {
                final int index = Integer.parseInt(value);
                result = sharedStrings.getItemAt(index).getString();
            } catch (final NumberFormatException e) {
                result = value;
            }
            break;
        case DATE:
            try {
                result = getDateValue(value);
            } catch (final Exception e) {
                result = value;
            }
            break;
        default:
            try {
                result = getNumberValue(value, numFmtString);
            } catch (final NumberFormatException ignore) {
            }

            if (null == result) {
                result = value;
            }
            break;
        }
        return result;
    }

    /**
     * 格式化数字或日期值
     *
     * @param value        值
     * @param numFmtIndex  数字格式索引
     * @param numFmtString 数字格式名
     * @return 格式化后的值
     */
    public static String formatCellContent(String value, final int numFmtIndex, final String numFmtString) {
        if (null != numFmtString) {
            try {
                value = new DataFormatter().formatRawCellContents(Double.parseDouble(value), numFmtIndex, numFmtString);
            } catch (final NumberFormatException e) {
                // ignore
            }
        }
        return value;
    }

    /**
     * 计算两个单元格之间的单元格数目(同一行)
     *
     * @param preRef 前一个单元格位置，例如A1
     * @param ref    当前单元格位置，例如A8
     * @return 同一行中两个单元格之间的空单元格数
     */
    public static int countNullCell(final String preRef, final String ref) {
        // excel2007最大行数是1048576，最大列数是16384，最后一列列名是XFD
        // 数字代表列，去掉列信息
        String preXfd = ObjectKit.defaultIfNull(preRef, Symbol.AT).replaceAll("\\d+", "");
        String xfd = ObjectKit.defaultIfNull(ref, Symbol.AT).replaceAll("\\d+", "");

        // A表示65，@表示64，如果A算作1，那@代表0
        // 填充最大位数3
        preXfd = StringKit.fillBefore(preXfd, Symbol.C_AT, Normal._3);
        xfd = StringKit.fillBefore(xfd, Symbol.C_AT, Normal._3);

        final char[] preLetter = preXfd.toCharArray();
        final char[] letter = xfd.toCharArray();
        // 用字母表示则最多三位，每26个字母进一位
        final int res = (letter[0] - preLetter[0]) * 26 * 26 + (letter[1] - preLetter[1]) * 26
                + (letter[2] - preLetter[2]);
        return res - 1;
    }

    /**
     * 从Excel的XML文档中读取内容，并使用{@link ContentHandler}处理
     *
     * @param xmlDocStream Excel的XML文档流
     * @param handler      文档内容处理接口，实现此接口用于回调处理数据
     * @throws DependencyException 依赖异常
     * @throws InternalException   POI异常，包装了SAXException
     * @throws RevisedException    IO异常，如流关闭或异常等
     */
    public static void readFrom(final InputStream xmlDocStream, final ContentHandler handler)
            throws DependencyException, InternalException, RevisedException {
        final XMLReader xmlReader;
        try {
            xmlReader = XMLHelper.newXMLReader();
        } catch (final SAXException | ParserConfigurationException e) {
            if (e.getMessage().contains("org.apache.xerces.parsers.SAXParser")) {
                throw new DependencyException(e,
                        "You need to add 'xerces:xercesImpl' to your project and version >= 2.11.0");
            } else {
                throw new InternalException(e);
            }
        }
        xmlReader.setContentHandler(handler);
        try {
            xmlReader.parse(new InputSource(xmlDocStream));
        } catch (final IOException e) {
            throw new RevisedException(e);
        } catch (final SAXException e) {
            throw new InternalException(e);
        } catch (final TerminateException e) {
            // 用户抛出此异常，表示强制结束读取
        }
    }

    /**
     * 判断数字Record中是否为日期格式
     *
     * @param cell           单元格记录
     * @param formatListener {@link FormatTrackingHSSFListener}
     * @return 是否为日期格式
     */
    public static boolean isDateFormat(final CellValueRecordInterface cell,
            final FormatTrackingHSSFListener formatListener) {
        final int formatIndex = formatListener.getFormatIndex(cell);
        final String formatString = formatListener.getFormatString(cell);
        return isDateFormat(formatIndex, formatString);
    }

    /**
     * 判断日期格式
     *
     * @param formatIndex  格式索引，一般用于内建格式
     * @param formatString 格式字符串
     * @return 是否为日期格式
     * @see Builder#isDateFormat(int, String)
     */
    public static boolean isDateFormat(final int formatIndex, final String formatString) {
        return Builder.isDateFormat(formatIndex, formatString);
    }

    /**
     * 获取日期
     *
     * @param value 单元格值
     * @return 日期
     */
    public static DateTime getDateValue(final String value) {
        return getDateValue(Double.parseDouble(value));
    }

    /**
     * 获取日期
     *
     * @param value 单元格值
     * @return 日期
     */
    public static DateTime getDateValue(final double value) {
        return DateKit.date(org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value, false));
    }

    /**
     * 在Excel03 sax读取中获取日期或数字类型的结果值
     *
     * @param cell           记录单元格
     * @param value          值
     * @param formatListener {@link FormatTrackingHSSFListener}
     * @return 值，可能为Date或Double或Long
     */
    public static Object getNumberOrDateValue(final CellValueRecordInterface cell, final double value,
            final FormatTrackingHSSFListener formatListener) {
        if (isDateFormat(cell, formatListener)) {
            // 可能为日期格式
            return getDateValue(value);
        }
        return getNumberValue(value, formatListener.getFormatString(cell));
    }

    /**
     * 获取数字类型值
     *
     * @param value        值
     * @param numFmtString 格式
     * @return 数字，可以是Double、Long
     */
    private static Number getNumberValue(final String value, final String numFmtString) {
        if (StringKit.isBlank(value)) {
            return null;
        }
        // 可能精度丢失，对含有小数的value判断并转为BigDecimal
        final double number = Double.parseDouble(value);
        if (StringKit.contains(value, Symbol.C_DOT) && !value.equals(Double.toString(number))) {
            // 精度丢失
            return MathKit.toBigDecimal(value);
        }

        return getNumberValue(number, numFmtString);
    }

    /**
     * 获取数字类型值，除非格式中明确数字保留小数，否则无小数情况下按照long返回
     *
     * @param numValue     值
     * @param numFmtString 格式
     * @return 数字，可以是Double、Long
     */
    private static Number getNumberValue(final double numValue, final String numFmtString) {
        // 普通数字
        if (null != numFmtString && !StringKit.contains(numFmtString, Symbol.C_DOT)) {
            final long longPart = (long) numValue;
            if (longPart == numValue) {
                // 对于无小数部分的数字类型，转为Long
                return longPart;
            }
        }
        return numValue;
    }

}
