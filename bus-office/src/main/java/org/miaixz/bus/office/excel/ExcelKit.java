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

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.*;
import org.miaixz.bus.office.Builder;
import org.miaixz.bus.office.excel.cell.CellLocation;
import org.miaixz.bus.office.excel.sax.ExcelSaxReader;
import org.miaixz.bus.office.excel.sax.RowHandler;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Excel工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelKit {

    /**
     * 通过Sax方式读取Excel,同时支持03和07格式
     *
     * @param path       Excel文件路径
     * @param rid        sheet rid，-1表示全部Sheet, 0表示第一个Sheet
     * @param rowHandler 行处理器
     */
    public static void readBySax(String path, int rid, RowHandler rowHandler) {
        readBySax(FileKit.file(path), rid, rowHandler);
    }

    /**
     * 通过Sax方式读取Excel，同时支持03和07格式
     *
     * @param path       Excel文件路径
     * @param idOrRid    Excel中的sheet id或者rid编号，rid必须加rId前缀，例如rId1，如果为-1处理所有编号的sheet
     * @param rowHandler 行处理器
     */
    public static void readBySax(String path, String idOrRid, RowHandler rowHandler) {
        readBySax(FileKit.file(path), idOrRid, rowHandler);
    }

    /**
     * 通过Sax方式读取Excel，同时支持03和07格式
     *
     * @param file       Excel文件
     * @param rid        sheet rid，-1表示全部Sheet, 0表示第一个Sheet
     * @param rowHandler 行处理器
     */
    public static void readBySax(File file, int rid, RowHandler rowHandler) {
        final ExcelSaxReader<?> reader = ExcelSaxKit.createSaxReader(ExcelFileKit.isXlsx(file), rowHandler);
        reader.read(file, rid);
    }

    /**
     * 通过Sax方式读取Excel，同时支持03和07格式
     *
     * @param file       Excel文件
     * @param idOrRid    Excel中的sheet id或者rid编号，rid必须加rId前缀，例如rId1，如果为-1处理所有编号的sheet
     * @param rowHandler 行处理器
     */
    public static void readBySax(File file, String idOrRid, RowHandler rowHandler) {
        final ExcelSaxReader<?> reader = ExcelSaxKit.createSaxReader(ExcelFileKit.isXlsx(file), rowHandler);
        reader.read(file, idOrRid);
    }

    /**
     * 通过Sax方式读取Excel，同时支持03和07格式
     *
     * @param in         Excel流
     * @param rid        sheet rid，-1表示全部Sheet, 0表示第一个Sheet
     * @param rowHandler 行处理器
     */
    public static void readBySax(InputStream in, int rid, RowHandler rowHandler) {
        in = IoKit.toMarkSupportStream(in);
        final ExcelSaxReader<?> reader = ExcelSaxKit.createSaxReader(ExcelFileKit.isXlsx(in), rowHandler);
        reader.read(in, rid);
    }

    /**
     * 通过Sax方式读取Excel，同时支持03和07格式
     *
     * @param in         Excel流
     * @param idOrRid    Excel中的sheet id或者rid编号，rid必须加rId前缀，例如rId1，如果为-1处理所有编号的sheet
     * @param rowHandler 行处理器
     */
    public static void readBySax(InputStream in, String idOrRid, RowHandler rowHandler) {
        in = IoKit.toMarkSupportStream(in);
        final ExcelSaxReader<?> reader = ExcelSaxKit.createSaxReader(ExcelFileKit.isXlsx(in), rowHandler);
        reader.read(in, idOrRid);
    }

    /**
     * 获取Excel读取器,通过调用{@link ExcelReader}的read或readXXX方法读取Excel内容
     * 默认调用第一个sheet
     *
     * @param bookFilePath Excel文件路径,绝对路径或相对于ClassPath路径
     * @return {@link ExcelReader}
     */
    public static ExcelReader getReader(String bookFilePath) {
        return getReader(bookFilePath, 0);
    }

    /**
     * 获取Excel读取器,通过调用{@link ExcelReader}的read或readXXX方法读取Excel内容
     * 默认调用第一个sheet
     *
     * @param bookFile Excel文件
     * @return {@link ExcelReader}
     */
    public static ExcelReader getReader(File bookFile) {
        return getReader(bookFile, 0);
    }

    /**
     * 获取Excel读取器,通过调用{@link ExcelReader}的read或readXXX方法读取Excel内容
     *
     * @param bookFilePath Excel文件路径,绝对路径或相对于ClassPath路径
     * @param sheetIndex   sheet序号,0表示第一个sheet
     * @return {@link ExcelReader}
     */
    public static ExcelReader getReader(String bookFilePath, int sheetIndex) {
        try {
            return new ExcelReader(bookFilePath, sheetIndex);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获取Excel读取器,通过调用{@link ExcelReader}的read或readXXX方法读取Excel内容
     *
     * @param bookFile   Excel文件
     * @param sheetIndex sheet序号,0表示第一个sheet
     * @return {@link ExcelReader}
     */
    public static ExcelReader getReader(File bookFile, int sheetIndex) {
        try {
            return new ExcelReader(bookFile, sheetIndex);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获取Excel读取器,通过调用{@link ExcelReader}的read或readXXX方法读取Excel内容
     *
     * @param bookFile  Excel文件
     * @param sheetName sheet名,第一个默认是sheet1
     * @return {@link ExcelReader}
     */
    public static ExcelReader getReader(File bookFile, String sheetName) {
        try {
            return new ExcelReader(bookFile, sheetName);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获取Excel读取器,通过调用{@link ExcelReader}的read或readXXX方法读取Excel内容
     * 默认调用第一个sheet,读取结束自动关闭流
     *
     * @param bookStream Excel文件的流
     * @return {@link ExcelReader}
     */
    public static ExcelReader getReader(InputStream bookStream) {
        return getReader(bookStream, 0);
    }

    /**
     * 获取Excel读取器,通过调用{@link ExcelReader}的read或readXXX方法读取Excel内容
     * 读取结束自动关闭流
     *
     * @param bookStream Excel文件的流
     * @param sheetIndex sheet序号,0表示第一个sheet
     * @return {@link ExcelReader}
     */
    public static ExcelReader getReader(InputStream bookStream, int sheetIndex) {
        try {
            return new ExcelReader(bookStream, sheetIndex);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获取Excel读取器,通过调用{@link ExcelReader}的read或readXXX方法读取Excel内容
     * 读取结束自动关闭流
     *
     * @param bookStream Excel文件的流
     * @param sheetName  sheet名,第一个默认是sheet1
     * @return {@link ExcelReader}
     */
    public static ExcelReader getReader(InputStream bookStream, String sheetName) {
        try {
            return new ExcelReader(bookStream, sheetName);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获取Excel读取器，通过调用{@link ExcelReader}的read或readXXX方法读取Excel内容
     *
     * @param bookFilePath Excel文件路径，绝对路径或相对于ClassPath路径
     * @param sheetName    sheet名，第一个默认是sheet1
     * @return {@link ExcelReader}
     */
    public static ExcelReader getReader(String bookFilePath, String sheetName) {
        try {
            return new ExcelReader(bookFilePath, sheetName);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link ExcelWriter},默认写出到第一个sheet
     * 不传入写出的Excel文件路径,只能调用{@link ExcelWriter#flush(OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link ExcelWriter#setDestFile(File)}方法自定义写出的文件,然后调用{@link ExcelWriter#flush()}方法写出到文件
     *
     * @return {@link ExcelWriter}
     */
    public static ExcelWriter getWriter() {
        try {
            return new ExcelWriter();
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link ExcelWriter},默认写出到第一个sheet
     * 不传入写出的Excel文件路径,只能调用{@link ExcelWriter#flush(OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link ExcelWriter#setDestFile(File)}方法自定义写出的文件,然后调用{@link ExcelWriter#flush()}方法写出到文件
     *
     * @param isXlsx 是否为xlsx格式
     * @return {@link ExcelWriter}
     */
    public static ExcelWriter getWriter(boolean isXlsx) {
        try {
            return new ExcelWriter(isXlsx);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link ExcelWriter},默认写出到第一个sheet
     *
     * @param destFilePath 目标文件路径
     * @return {@link ExcelWriter}
     */
    public static ExcelWriter getWriter(String destFilePath) {
        try {
            return new ExcelWriter(destFilePath);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link ExcelWriter},默认写出到第一个sheet,名字为sheet1
     *
     * @param destFile 目标文件
     * @return {@link ExcelWriter}
     */
    public static ExcelWriter getWriter(File destFile) {
        try {
            return new ExcelWriter(destFile);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link ExcelWriter}
     *
     * @param destFilePath 目标文件路径
     * @param sheetName    sheet表名
     * @return {@link ExcelWriter}
     */
    public static ExcelWriter getWriter(String destFilePath, String sheetName) {
        try {
            return new ExcelWriter(destFilePath, sheetName);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link ExcelWriter}
     *
     * @param destFile  目标文件
     * @param sheetName sheet表名
     * @return {@link ExcelWriter}
     */
    public static ExcelWriter getWriter(File destFile, String sheetName) {
        try {
            return new ExcelWriter(destFile, sheetName);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link BigExcelWriter},默认写出到第一个sheet
     * 不传入写出的Excel文件路径,只能调用{@link BigExcelWriter#flush(OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link BigExcelWriter#setDestFile(File)}方法自定义写出的文件,然后调用{@link BigExcelWriter#flush()}方法写出到文件
     *
     * @return {@link BigExcelWriter}
     */
    public static BigExcelWriter getBigWriter() {
        try {
            return new BigExcelWriter();
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link BigExcelWriter},默认写出到第一个sheet
     * 不传入写出的Excel文件路径,只能调用{@link BigExcelWriter#flush(OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link BigExcelWriter#setDestFile(File)}方法自定义写出的文件,然后调用{@link BigExcelWriter#flush()}方法写出到文件
     *
     * @param rowAccessWindowSize 在内存中的行数
     * @return {@link BigExcelWriter}
     */
    public static BigExcelWriter getBigWriter(int rowAccessWindowSize) {
        try {
            return new BigExcelWriter(rowAccessWindowSize);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link BigExcelWriter},默认写出到第一个sheet
     *
     * @param destFilePath 目标文件路径
     * @return {@link BigExcelWriter}
     */
    public static BigExcelWriter getBigWriter(String destFilePath) {
        try {
            return new BigExcelWriter(destFilePath);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link BigExcelWriter},默认写出到第一个sheet,名字为sheet1
     *
     * @param destFile 目标文件
     * @return {@link BigExcelWriter}
     */
    public static BigExcelWriter getBigWriter(File destFile) {
        try {
            return new BigExcelWriter(destFile);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link BigExcelWriter}
     *
     * @param destFilePath 目标文件路径
     * @param sheetName    sheet表名
     * @return {@link BigExcelWriter}
     */
    public static BigExcelWriter getBigWriter(String destFilePath, String sheetName) {
        try {
            return new BigExcelWriter(destFilePath, sheetName);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 获得{@link BigExcelWriter}
     *
     * @param destFile  目标文件
     * @param sheetName sheet表名
     * @return {@link BigExcelWriter}
     */
    public static BigExcelWriter getBigWriter(File destFile, String sheetName) {
        try {
            return new BigExcelWriter(destFile, sheetName);
        } catch (NoClassDefFoundError e) {
            throw new InternalException(Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * 将Sheet列号变为列名
     *
     * @param index 列号, 从0开始
     * @return the sring
     */
    public static String indexToColName(int index) {
        if (index < 0) {
            return null;
        }
        final StringBuilder colName = StringKit.builder();
        do {
            if (colName.length() > 0) {
                index--;
            }
            int remainder = index % 26;
            colName.append((char) (remainder + 'A'));
            index = (index - remainder) / 26;
        } while (index > 0);
        return colName.reverse().toString();
    }

    /**
     * 根据表元的列名转换为列号
     *
     * @param colName 列名, 从A开始
     * @return the int
     */
    public static int colNameToIndex(String colName) {
        int length = colName.length();
        char c;
        int index = -1;
        for (int i = 0; i < length; i++) {
            c = Character.toUpperCase(colName.charAt(i));
            if (Character.isDigit(c)) {
                break;// 确定指定的char值是否为数字
            }
            index = (index + 1) * 26 + (int) c - 'A';
        }
        return index;
    }

    /**
     * 将Excel中地址标识符(例如A11，B5)等转换为行列表示
     * 例如：A11 -  x:0,y:10，B5 - x:1,y:4
     *
     * @param locationRef 单元格地址标识符，例如A11，B5
     * @return 坐标点，x表示行，从0开始，y表示列，从0开始
     */
    public static CellLocation toLocation(String locationRef) {
        final int x = colNameToIndex(locationRef);
        final int y = PatternKit.getFirstNumber(locationRef) - 1;
        return new CellLocation(x, y);
    }

}
