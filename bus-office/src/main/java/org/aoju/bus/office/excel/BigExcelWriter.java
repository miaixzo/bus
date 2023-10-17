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

import org.aoju.bus.core.toolkit.FileKit;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.OutputStream;

/**
 * 大数据量Excel写出
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BigExcelWriter extends ExcelWriter {

    public static final int DEFAULT_WINDOW_SIZE = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;
    /**
     * 只能flush一次，调用后不再重复写出
     */
    private boolean isFlushed;

    /**
     * 构造,默认生成xlsx格式的Excel文件
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link #setDestFile(File)}方法自定义写出的文件,然后调用{@link #flush()}方法写出到文件
     */
    public BigExcelWriter() {
        this(DEFAULT_WINDOW_SIZE);
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件,需要调用{@link #flush(File)} 写出到文件
     *
     * @param rowAccessWindowSize 在内存中的行数
     */
    public BigExcelWriter(int rowAccessWindowSize) {
        this(WorksKit.createSXSSFBook(rowAccessWindowSize), null);
    }

    /**
     * 构造,默认写出到第一个sheet,第一个sheet名为sheet1
     *
     * @param destFilePath 目标文件路径,可以不存在
     */
    public BigExcelWriter(String destFilePath) {
        this(destFilePath, null);
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件,需要调用{@link #flush(File)} 写出到文件
     *
     * @param rowAccessWindowSize 在内存中的行数
     * @param sheetName           sheet名,第一个sheet名并写出到此sheet,例如sheet1
     */
    public BigExcelWriter(int rowAccessWindowSize, String sheetName) {
        this(WorksKit.createSXSSFBook(rowAccessWindowSize), sheetName);
    }

    /**
     * 构造
     *
     * @param destFilePath 目标文件路径,可以不存在
     * @param sheetName    sheet名,第一个sheet名并写出到此sheet,例如sheet1
     */
    public BigExcelWriter(String destFilePath, String sheetName) {
        this(FileKit.file(destFilePath), sheetName);
    }

    /**
     * 构造,默认写出到第一个sheet,第一个sheet名为sheet1
     *
     * @param destFile 目标文件,可以不存在
     */
    public BigExcelWriter(File destFile) {
        this(destFile, null);
    }

    /**
     * 构造
     *
     * @param destFile  目标文件,可以不存在
     * @param sheetName sheet名,做为第一个sheet名并写出到此sheet,例如sheet1
     */
    public BigExcelWriter(File destFile, String sheetName) {
        this(destFile.exists() ? WorksKit.createSXSSFBook(destFile) : WorksKit.createSXSSFBook(), sheetName);
        this.destFile = destFile;
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link #setDestFile(File)}方法自定义写出的文件,然后调用{@link #flush()}方法写出到文件
     *
     * @param workbook  {@link SXSSFWorkbook}
     * @param sheetName sheet名,做为第一个sheet名并写出到此sheet,例如sheet1
     */
    public BigExcelWriter(SXSSFWorkbook workbook, String sheetName) {
        this(WorksKit.getOrCreateSheet(workbook, sheetName));
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径,只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件,还需调用{@link #setDestFile(File)}方法自定义写出的文件,然后调用{@link #flush()}方法写出到文件
     *
     * @param sheet {@link Sheet}
     */
    public BigExcelWriter(Sheet sheet) {
        super(sheet);
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件，需要调用{@link #flush(File)} 写出到文件
     *
     * @param rowAccessWindowSize   在内存中的行数，-1表示不限制，此时需要手动刷出
     * @param compressTmpFiles      是否使用Gzip压缩临时文件
     * @param useSharedStringsTable 是否使用共享字符串表，一般大量重复字符串时开启可节省内存
     * @param sheetName             写出的sheet名称
     */
    public BigExcelWriter(int rowAccessWindowSize, boolean compressTmpFiles, boolean useSharedStringsTable, String sheetName) {
        this(WorksKit.createSXSSFBook(rowAccessWindowSize, compressTmpFiles, useSharedStringsTable), sheetName);
    }

    @Override
    public BigExcelWriter autoSizeColumn(int columnIndex) {
        final SXSSFSheet sheet = (SXSSFSheet) this.sheet;
        sheet.trackColumnForAutoSizing(columnIndex);
        super.autoSizeColumn(columnIndex);
        sheet.untrackColumnForAutoSizing(columnIndex);
        return this;
    }

    @Override
    public BigExcelWriter autoSizeColumnAll() {
        final SXSSFSheet sheet = (SXSSFSheet) this.sheet;
        sheet.trackAllColumnsForAutoSizing();
        super.autoSizeColumnAll();
        sheet.untrackAllColumnsForAutoSizing();
        return this;
    }

    @Override
    public ExcelWriter flush(OutputStream out, boolean isCloseOut) {
        if (false == isFlushed) {
            isFlushed = true;
            return super.flush(out, isCloseOut);
        }
        return this;
    }

    @Override
    public void close() {
        if (null != this.destFile && false == isFlushed) {
            flush();
        }

        ((SXSSFWorkbook) this.workbook).dispose();
        super.closeWithoutFlush();
    }

}
