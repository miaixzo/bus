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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Excel工作簿{@link Workbook}相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WorkbookKit {

    /**
     * 创建或加载工作簿（读写模式）
     *
     * @param excelFilePath Excel文件路径，绝对路径或相对于ClassPath路径
     * @return {@link Workbook}
     */
    public static Workbook createBook(final String excelFilePath) {
        return createBook(excelFilePath, false);
    }

    /**
     * 创建或加载工作簿
     *
     * @param excelFilePath Excel文件路径，绝对路径或相对于ClassPath路径
     * @param readOnly      是否只读模式打开，true:是（不可编辑），false:否（可编辑）
     * @return {@link Workbook}
     */
    public static Workbook createBook(final String excelFilePath, final boolean readOnly) {
        return createBook(FileKit.file(excelFilePath), null, readOnly);
    }

    /**
     * 创建或加载工作簿（读写模式）
     *
     * @param excelFile Excel文件
     * @return {@link Workbook}
     */
    public static Workbook createBook(final File excelFile) {
        return createBook(excelFile, false);
    }

    /**
     * 创建或加载工作簿
     *
     * @param excelFile Excel文件
     * @param readOnly  是否只读模式打开，true:是（不可编辑），false:否（可编辑）
     * @return {@link Workbook}
     */
    public static Workbook createBook(final File excelFile, final boolean readOnly) {
        return createBook(excelFile, null, readOnly);
    }

    /**
     * 创建工作簿，用于Excel写出（读写模式）
     *
     * <pre>
     * 1. excelFile为null时直接返回一个空的工作簿，默认xlsx格式
     * 2. 文件已存在则通过流的方式读取到这个工作簿
     * 3. 文件不存在则检查传入文件路径是否以xlsx为扩展名，是则创建xlsx工作簿，否则创建xls工作簿
     * </pre>
     *
     * @param excelFile Excel文件
     * @return {@link Workbook}
     */
    public static Workbook createBookForWriter(final File excelFile) {
        if (null == excelFile) {
            return createBook(true);
        }

        if (excelFile.exists()) {
            return createBook(FileKit.getInputStream(excelFile));
        }

        return createBook(StringKit.endWithIgnoreCase(excelFile.getName(), ".xlsx"));
    }

    /**
     * 创建或加载工作簿（读写模式）
     *
     * @param excelFile Excel文件
     * @param password  Excel工作簿密码，如果无密码传{@code null}
     * @return {@link Workbook}
     */
    public static Workbook createBook(final File excelFile, final String password) {
        return createBook(excelFile, password, false);
    }

    /**
     * 创建或加载工作簿
     *
     * @param excelFile Excel文件
     * @param password  Excel工作簿密码，如果无密码传{@code null}
     * @param readOnly  是否只读模式打开，true:是（不可编辑），false:否（可编辑）
     * @return {@link Workbook}
     */
    public static Workbook createBook(final File excelFile, final String password, final boolean readOnly) {
        try {
            return WorkbookFactory.create(excelFile, password, readOnly);
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建或加载工作簿（只读模式）
     *
     * @param in Excel输入流
     * @return {@link Workbook}
     */
    public static Workbook createBook(final InputStream in) {
        return createBook(in, null);
    }

    /**
     * 创建或加载工作簿（只读模式）
     *
     * @param in       Excel输入流，使用完毕自动关闭流
     * @param password 密码
     * @return {@link Workbook}
     */
    public static Workbook createBook(final InputStream in, final String password) {
        try {
            return WorkbookFactory.create(IoKit.toMarkSupport(in), password);
        } catch (final Exception e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * 创建新的空白Excel工作簿
     *
     * @param isXlsx 是否为xlsx格式的Excel
     * @return {@link Workbook}
     */
    public static Workbook createBook(final boolean isXlsx) {
        try {
            return WorkbookFactory.create(isXlsx);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建或加载SXSSFWorkbook工作簿（读写模式）
     *
     * @param excelFilePath Excel文件路径，绝对路径或相对于ClassPath路径
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final String excelFilePath) {
        return createSXSSFBook(excelFilePath, false);
    }

    /**
     * 创建或加载SXSSFWorkbook工作簿
     *
     * @param excelFilePath Excel文件路径，绝对路径或相对于ClassPath路径
     * @param readOnly      是否只读模式打开，true:是（不可编辑），false:否（可编辑）
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final String excelFilePath, final boolean readOnly) {
        return createSXSSFBook(FileKit.file(excelFilePath), null, readOnly);
    }

    /**
     * 创建或加载SXSSFWorkbook工作簿（读写模式）
     *
     * @param excelFile Excel文件
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final File excelFile) {
        return createSXSSFBook(excelFile, false);
    }

    /**
     * 创建或加载SXSSFWorkbook工作簿
     *
     * @param excelFile Excel文件
     * @param readOnly  是否只读模式打开，true:是（不可编辑），false:否（可编辑）
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final File excelFile, final boolean readOnly) {
        return createSXSSFBook(excelFile, null, readOnly);
    }

    /**
     * 创建或加载SXSSFWorkbook工作簿（读写模式）
     *
     * @param excelFile Excel文件
     * @param password  Excel工作簿密码，如果无密码传{@code null}
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final File excelFile, final String password) {
        return createSXSSFBook(excelFile, password, false);
    }

    /**
     * 创建或加载{@link SXSSFWorkbook}工作簿
     *
     * @param excelFile Excel文件
     * @param password  Excel工作簿密码，如果无密码传{@code null}
     * @param readOnly  是否只读模式打开，true:是（不可编辑），false:否（可编辑）
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final File excelFile, final String password, final boolean readOnly) {
        return toSXSSFBook(createBook(excelFile, password, readOnly));
    }

    /**
     * 创建或加载{@link SXSSFWorkbook}工作簿（只读模式）
     *
     * @param in Excel输入流
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final InputStream in) {
        return createSXSSFBook(in, null);
    }

    /**
     * 创建或加载{@link SXSSFWorkbook}工作簿（只读模式）
     *
     * @param in       Excel输入流
     * @param password 密码
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final InputStream in, final String password) {
        return toSXSSFBook(createBook(in, password));
    }

    /**
     * 创建空的{@link SXSSFWorkbook}，用于大批量数据写出
     *
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook() {
        return new SXSSFWorkbook();
    }

    /**
     * 创建空的{@link SXSSFWorkbook}，用于大批量数据写出
     *
     * @param rowAccessWindowSize 在内存中的行数，-1表示不限制，此时需要手动刷出
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final int rowAccessWindowSize) {
        return new SXSSFWorkbook(rowAccessWindowSize);
    }

    /**
     * 创建空的{@link SXSSFWorkbook}，用于大批量数据写出
     *
     * @param rowAccessWindowSize   在内存中的行数，-1表示不限制，此时需要手动刷出
     * @param compressTmpFiles      是否使用Gzip压缩临时文件
     * @param useSharedStringsTable 是否使用共享字符串表，一般大量重复字符串时开启可节省内存
     * @return {@link SXSSFWorkbook}
     */
    public static SXSSFWorkbook createSXSSFBook(final int rowAccessWindowSize, final boolean compressTmpFiles,
            final boolean useSharedStringsTable) {
        return new SXSSFWorkbook(null, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
    }

    /**
     * 将Excel Workbook刷出到输出流，不关闭流
     *
     * @param book {@link Workbook}
     * @param out  输出流
     * @throws InternalException IO异常
     */
    public static void writeBook(final Workbook book, final OutputStream out) throws InternalException {
        try {
            book.write(out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 将普通工作簿转换为SXSSFWorkbook
     *
     * @param book 工作簿
     * @return SXSSFWorkbook
     */
    private static SXSSFWorkbook toSXSSFBook(final Workbook book) {
        if (book instanceof SXSSFWorkbook) {
            return (SXSSFWorkbook) book;
        }
        if (book instanceof XSSFWorkbook) {
            return new SXSSFWorkbook((XSSFWorkbook) book);
        }
        throw new InternalException("The input is not a [xlsx] format.");
    }

}
