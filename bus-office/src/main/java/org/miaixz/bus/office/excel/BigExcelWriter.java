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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;

import java.io.File;
import java.io.OutputStream;

/**
 * 大数据量Excel写出，只支持XLSX（Excel07版本） 通过封装{@link SXSSFWorkbook}，限制对滑动窗口中的行的访问来实现其低内存使用。
 * 注意如果写出数据大于滑动窗口大小，就会写出到临时文件，此时写出的数据无法访问和编辑。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BigExcelWriter extends ExcelWriter {

    public static final int DEFAULT_WINDOW_SIZE = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;

    /**
     * BigExcelWriter只能flush一次，因此调用后不再重复写出
     */
    private boolean isFlushed;

    /**
     * 构造，默认生成xlsx格式的Excel文件 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件，还需调用{@link #setDestFile(File)}方法自定义写出的文件，然后调用{@link #flush()}方法写出到文件
     */
    public BigExcelWriter() {
        this(DEFAULT_WINDOW_SIZE);
    }

    /**
     * 构造 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流 若写出到文件，需要调用{@link #flush(File)} 写出到文件
     *
     * @param rowAccessWindowSize 在内存中的行数
     */
    public BigExcelWriter(final int rowAccessWindowSize) {
        this(WorkbookKit.createSXSSFBook(rowAccessWindowSize), null);
    }

    /**
     * 构造 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流 若写出到文件，需要调用{@link #flush(File)} 写出到文件
     *
     * @param rowAccessWindowSize   在内存中的行数，-1表示不限制，此时需要手动刷出
     * @param compressTmpFiles      是否使用Gzip压缩临时文件
     * @param useSharedStringsTable 是否使用共享字符串表，一般大量重复字符串时开启可节省内存
     * @param sheetName             写出的sheet名称
     */
    public BigExcelWriter(final int rowAccessWindowSize, final boolean compressTmpFiles,
            final boolean useSharedStringsTable, final String sheetName) {
        this(WorkbookKit.createSXSSFBook(rowAccessWindowSize, compressTmpFiles, useSharedStringsTable), sheetName);
    }

    /**
     * 构造，默认写出到第一个sheet，第一个sheet名为sheet1
     *
     * @param destFilePath 目标文件路径，可以不存在
     */
    public BigExcelWriter(final String destFilePath) {
        this(destFilePath, null);
    }

    /**
     * 构造 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流 若写出到文件，需要调用{@link #flush(File)} 写出到文件
     *
     * @param rowAccessWindowSize 在内存中的行数
     * @param sheetName           sheet名，第一个sheet名并写出到此sheet，例如sheet1
     */
    public BigExcelWriter(final int rowAccessWindowSize, final String sheetName) {
        this(WorkbookKit.createSXSSFBook(rowAccessWindowSize), sheetName);
    }

    /**
     * 构造
     *
     * @param destFilePath 目标文件路径，可以不存在
     * @param sheetName    sheet名，第一个sheet名并写出到此sheet，例如sheet1
     */
    public BigExcelWriter(final String destFilePath, final String sheetName) {
        this(FileKit.file(destFilePath), sheetName);
    }

    /**
     * 构造，默认写出到第一个sheet，第一个sheet名为sheet1
     *
     * @param destFile 目标文件，可以不存在
     */
    public BigExcelWriter(final File destFile) {
        this(destFile, null);
    }

    /**
     * 构造
     *
     * @param destFile  目标文件，可以不存在
     * @param sheetName sheet名，做为第一个sheet名并写出到此sheet，例如sheet1
     */
    public BigExcelWriter(final File destFile, final String sheetName) {
        this(destFile.exists() ? WorkbookKit.createSXSSFBook(destFile) : WorkbookKit.createSXSSFBook(), sheetName);
        this.destFile = destFile;
    }

    /**
     * 构造 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件，还需调用{@link #setDestFile(File)}方法自定义写出的文件，然后调用{@link #flush()}方法写出到文件
     *
     * @param workbook  {@link SXSSFWorkbook}
     * @param sheetName sheet名，做为第一个sheet名并写出到此sheet，例如sheet1
     */
    public BigExcelWriter(final SXSSFWorkbook workbook, final String sheetName) {
        this(WorkbookKit.getOrCreateSheet(workbook, sheetName));
    }

    /**
     * 构造 此构造不传入写出的Excel文件路径，只能调用{@link #flush(java.io.OutputStream)}方法写出到流
     * 若写出到文件，还需调用{@link #setDestFile(File)}方法自定义写出的文件，然后调用{@link #flush()}方法写出到文件
     *
     * @param sheet {@link Sheet}
     */
    public BigExcelWriter(final Sheet sheet) {
        super(sheet);
    }

    @Override
    public BigExcelWriter autoSizeColumn(final int columnIndex) {
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
    public ExcelWriter flush(final OutputStream out, final boolean isCloseOut) throws InternalException {
        if (!isFlushed) {
            isFlushed = true;
            return super.flush(out, isCloseOut);
        }
        return this;
    }

    @Override
    public void close() {
        if (null != this.destFile && !isFlushed) {
            flush();
        }

        // 清理临时文件
        ((SXSSFWorkbook) this.workbook).dispose();
        super.closeWithoutFlush();
    }

}
