/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
 * Utility class for Excel {@link Workbook}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WorkbookKit {

    /**
     * Creates or loads a workbook (read-write mode).
     *
     * @param excelFilePath The path to the Excel file, absolute or relative to the classpath.
     * @return A {@link Workbook} instance.
     */
    public static Workbook createBook(final String excelFilePath) {
        return createBook(excelFilePath, false);
    }

    /**
     * Creates or loads a workbook.
     *
     * @param excelFilePath The path to the Excel file, absolute or relative to the classpath.
     * @param readOnly      Whether to open in read-only mode (true for non-editable, false for editable).
     * @return A {@link Workbook} instance.
     */
    public static Workbook createBook(final String excelFilePath, final boolean readOnly) {
        return createBook(FileKit.file(excelFilePath), null, readOnly);
    }

    /**
     * Creates or loads a workbook (read-write mode).
     *
     * @param excelFile The Excel file.
     * @return A {@link Workbook} instance.
     */
    public static Workbook createBook(final File excelFile) {
        return createBook(excelFile, false);
    }

    /**
     * Creates or loads a workbook.
     *
     * @param excelFile The Excel file.
     * @param readOnly  Whether to open in read-only mode (true for non-editable, false for editable).
     * @return A {@link Workbook} instance.
     */
    public static Workbook createBook(final File excelFile, final boolean readOnly) {
        return createBook(excelFile, null, readOnly);
    }

    /**
     * Creates a workbook for Excel writing (read-write mode).
     *
     * <pre>
     * 1. If excelFile is null, an empty workbook is returned, defaulting to xlsx format.
     * 2. If the file exists, it is read into the workbook via a stream.
     * 3. If the file does not exist, it checks if the file path ends with .xlsx. If so, an xlsx workbook is created; otherwise, an xls workbook is created.
     * </pre>
     *
     * @param excelFile The Excel file.
     * @return A {@link Workbook} instance.
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
     * Creates or loads a workbook (read-write mode).
     *
     * @param excelFile The Excel file.
     * @param password  The password for the workbook, or {@code null} if no password.
     * @return A {@link Workbook} instance.
     */
    public static Workbook createBook(final File excelFile, final String password) {
        return createBook(excelFile, password, false);
    }

    /**
     * Creates or loads a workbook.
     *
     * @param excelFile The Excel file.
     * @param password  The password for the workbook, or {@code null} if no password.
     * @param readOnly  Whether to open in read-only mode (true for non-editable, false for editable).
     * @return A {@link Workbook} instance.
     */
    public static Workbook createBook(final File excelFile, final String password, final boolean readOnly) {
        try {
            return WorkbookFactory.create(excelFile, password, readOnly);
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates or loads a workbook (read-only mode).
     *
     * @param in The Excel input stream.
     * @return A {@link Workbook} instance.
     */
    public static Workbook createBook(final InputStream in) {
        return createBook(in, null);
    }

    /**
     * Creates or loads a workbook (read-only mode). The stream is closed automatically after use.
     *
     * @param in       The Excel input stream.
     * @param password The password.
     * @return A {@link Workbook} instance.
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
     * Creates a new blank Excel workbook.
     *
     * @param isXlsx Whether the format is xlsx.
     * @return A {@link Workbook} instance.
     */
    public static Workbook createBook(final boolean isXlsx) {
        try {
            return WorkbookFactory.create(isXlsx);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates or loads an SXSSFWorkbook workbook (read-write mode).
     *
     * @param excelFilePath The path to the Excel file, absolute or relative to the classpath.
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(final String excelFilePath) {
        return createSXSSFBook(excelFilePath, false);
    }

    /**
     * Creates or loads an SXSSFWorkbook workbook.
     *
     * @param excelFilePath The path to the Excel file, absolute or relative to the classpath.
     * @param readOnly      Whether to open in read-only mode (true for non-editable, false for editable).
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(final String excelFilePath, final boolean readOnly) {
        return createSXSSFBook(FileKit.file(excelFilePath), null, readOnly);
    }

    /**
     * Creates or loads an SXSSFWorkbook workbook (read-write mode).
     *
     * @param excelFile The Excel file.
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(final File excelFile) {
        return createSXSSFBook(excelFile, false);
    }

    /**
     * Creates or loads an SXSSFWorkbook workbook.
     *
     * @param excelFile The Excel file.
     * @param readOnly  Whether to open in read-only mode (true for non-editable, false for editable).
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(final File excelFile, final boolean readOnly) {
        return createSXSSFBook(excelFile, null, readOnly);
    }

    /**
     * Creates or loads an SXSSFWorkbook workbook (read-write mode).
     *
     * @param excelFile The Excel file.
     * @param password  The password for the workbook, or {@code null} if no password.
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(final File excelFile, final String password) {
        return createSXSSFBook(excelFile, password, false);
    }

    /**
     * Creates or loads an {@link SXSSFWorkbook} workbook.
     *
     * @param excelFile The Excel file.
     * @param password  The password for the workbook, or {@code null} if no password.
     * @param readOnly  Whether to open in read-only mode (true for non-editable, false for editable).
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(final File excelFile, final String password, final boolean readOnly) {
        return toSXSSFBook(createBook(excelFile, password, readOnly));
    }

    /**
     * Creates or loads an {@link SXSSFWorkbook} workbook (read-only mode).
     *
     * @param in The Excel input stream.
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(final InputStream in) {
        return createSXSSFBook(in, null);
    }

    /**
     * Creates or loads an {@link SXSSFWorkbook} workbook (read-only mode).
     *
     * @param in       The Excel input stream.
     * @param password The password.
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(final InputStream in, final String password) {
        return toSXSSFBook(createBook(in, password));
    }

    /**
     * Creates an empty {@link SXSSFWorkbook}, suitable for writing large amounts of data.
     *
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook() {
        return new SXSSFWorkbook();
    }

    /**
     * Creates an empty {@link SXSSFWorkbook}, suitable for writing large amounts of data.
     *
     * @param rowAccessWindowSize The number of rows to keep in memory. -1 means no limit, requiring manual flushing.
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(final int rowAccessWindowSize) {
        return new SXSSFWorkbook(rowAccessWindowSize);
    }

    /**
     * Creates an empty {@link SXSSFWorkbook}, suitable for writing large amounts of data.
     *
     * @param rowAccessWindowSize   The number of rows to keep in memory. -1 means no limit, requiring manual flushing.
     * @param compressTmpFiles      Whether to compress temporary files with Gzip.
     * @param useSharedStringsTable Whether to use a shared strings table, which can save memory with many duplicate
     *                              strings.
     * @return An {@link SXSSFWorkbook} instance.
     */
    public static SXSSFWorkbook createSXSSFBook(
            final int rowAccessWindowSize,
            final boolean compressTmpFiles,
            final boolean useSharedStringsTable) {
        return new SXSSFWorkbook(null, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
    }

    /**
     * Flushes the Excel Workbook to an output stream without closing the stream.
     *
     * @param book The {@link Workbook}.
     * @param out  The output stream.
     * @throws InternalException if an I/O error occurs.
     */
    public static void writeBook(final Workbook book, final OutputStream out) throws InternalException {
        try {
            book.write(out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts a regular workbook to an SXSSFWorkbook.
     *
     * @param book The workbook.
     * @return An {@link SXSSFWorkbook} instance.
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
