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
package org.aoju.bus.poi.excel;

import org.aoju.bus.core.utils.FileUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;

/**
 * 大数据量Excel写出
 *
 * @author Kimi Liu
 * @version 3.2.5
 * @since JDK 1.8
 */
public class BigExcelWriter extends ExcelWriter {

    public static final int DEFAULT_WINDOW_SIZE = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;

    /**
     * 构造，默认生成xls格式的Excel文件
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件，还需调用{@link #setDestFile(File)}方法自定义写出的文件，然后调用{@link #flush()}方法写出到文件
     */
    public BigExcelWriter() {
        this(DEFAULT_WINDOW_SIZE);
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件，需要调用{@link #flush(File)} 写出到文件
     *
     * @param rowAccessWindowSize 在内存中的行数
     */
    public BigExcelWriter(int rowAccessWindowSize) {
        this(BookUtils.createSXSSFBook(rowAccessWindowSize), null);
    }

    /**
     * 构造，默认写出到第一个sheet，第一个sheet名为sheet1
     *
     * @param destFilePath 目标文件路径，可以不存在
     */
    public BigExcelWriter(String destFilePath) {
        this(destFilePath, null);
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件，需要调用{@link #flush(File)} 写出到文件
     *
     * @param rowAccessWindowSize 在内存中的行数
     * @param sheetName           sheet名，第一个sheet名并写出到此sheet，例如sheet1
     * @since 4.1.8
     */
    public BigExcelWriter(int rowAccessWindowSize, String sheetName) {
        this(BookUtils.createSXSSFBook(rowAccessWindowSize), sheetName);
    }

    /**
     * 构造
     *
     * @param destFilePath 目标文件路径，可以不存在
     * @param sheetName    sheet名，第一个sheet名并写出到此sheet，例如sheet1
     */
    public BigExcelWriter(String destFilePath, String sheetName) {
        this(FileUtils.file(destFilePath), sheetName);
    }

    /**
     * 构造，默认写出到第一个sheet，第一个sheet名为sheet1
     *
     * @param destFile 目标文件，可以不存在
     */
    public BigExcelWriter(File destFile) {
        this(destFile, null);
    }

    /**
     * 构造
     *
     * @param destFile  目标文件，可以不存在
     * @param sheetName sheet名，做为第一个sheet名并写出到此sheet，例如sheet1
     */
    public BigExcelWriter(File destFile, String sheetName) {
        this(destFile.exists() ? BookUtils.createSXSSFBook(destFile) : BookUtils.createSXSSFBook(), sheetName);
        this.destFile = destFile;
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件，还需调用{@link #setDestFile(File)}方法自定义写出的文件，然后调用{@link #flush()}方法写出到文件
     *
     * @param workbook  {@link SXSSFWorkbook}
     * @param sheetName sheet名，做为第一个sheet名并写出到此sheet，例如sheet1
     */
    public BigExcelWriter(SXSSFWorkbook workbook, String sheetName) {
        this(BookUtils.getOrCreateSheet(workbook, sheetName));
    }

    /**
     * 构造
     * 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件，还需调用{@link #setDestFile(File)}方法自定义写出的文件，然后调用{@link #flush()}方法写出到文件
     *
     * @param sheet {@link Sheet}
     * @since 4.0.6
     */
    public BigExcelWriter(Sheet sheet) {
        super(sheet);
    }

    @Override
    public void close() {
        if (null != this.destFile) {
            flush();
        }
        ((SXSSFWorkbook) this.workbook).dispose();
        super.closeWithoutFlush();
    }

}
