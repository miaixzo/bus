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
package org.miaixz.bus.office;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Provides auxiliary functions for office documents, such as Excel date judgment, reading, and processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * Content type for XLS files.
     */
    public static final String XLS_CONTENT_TYPE = "application/vnd.ms-excel";

    /**
     * Content type for XLSX files.
     */
    public static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Error message when POI dependency is missing.
     */
    public static final String NO_POI_ERROR_MSG = "You need to add dependency of 'poi-ooxml' to your project, and version >= 5.3.0";

    /**
     * Some special custom date formats.
     */
    private static final int[] CUSTOM_FORMATS = new int[] { 28, 30, 31, 32, 33, 55, 56, 57, 58 };

    /**
     * Checks if the cell is in date format.
     *
     * @param cell The cell to check.
     * @return {@code true} if the cell is in date format, {@code false} otherwise.
     */
    public static boolean isDateFormat(final Cell cell) {
        return isDateFormat(cell, null);
    }

    /**
     * Checks if the cell is in date format.
     *
     * @param cell        The cell to check.
     * @param cfEvaluator The {@link ConditionalFormattingEvaluator} for evaluating conditional formats.
     * @return {@code true} if the cell is in date format, {@code false} otherwise.
     */
    public static boolean isDateFormat(final Cell cell, final ConditionalFormattingEvaluator cfEvaluator) {
        final ExcelNumberFormat nf = ExcelNumberFormat.from(cell, cfEvaluator);
        return isDateFormat(nf);
    }

    /**
     * Checks if the number format is a date format.
     *
     * @param numFmt The {@link ExcelNumberFormat} to check.
     * @return {@code true} if the number format is a date format, {@code false} otherwise.
     */
    public static boolean isDateFormat(final ExcelNumberFormat numFmt) {
        return isDateFormat(numFmt.getIdx(), numFmt.getFormat());
    }

    /**
     * Checks if the given format index and format string represent a date format.
     *
     * @param formatIndex  The format index, usually for built-in formats.
     * @param formatString The format string.
     * @return {@code true} if it is a date format, {@code false} otherwise.
     */
    public static boolean isDateFormat(final int formatIndex, final String formatString) {
        if (ArrayKit.contains(CUSTOM_FORMATS, formatIndex)) {
            return true;
        }

        // Custom format judgment
        if (StringKit.isNotEmpty(formatString) && StringKit.containsAny(formatString, "周", "星期", "aa")) {
            // aa -> Mon
            // aaa -> Monday
            return true;
        }

        return org.apache.poi.ss.usermodel.DateUtil.isADateFormat(formatIndex, formatString);
    }

    /**
     * Checks if the given input stream is an XLS format Excel file (HSSF). XLS files are mainly used for Excel 97~2003.
     * This method automatically calls {@link InputStream#reset()}.
     *
     * @param in The Excel input stream.
     * @return {@code true} if it is an XLS format Excel file, {@code false} otherwise.
     */
    public static boolean isXls(final InputStream in) {
        return FileMagic.OLE2 == getFileMagic(in);
    }

    /**
     * Checks if the given input stream is an XLSX format Excel file (XSSF). XLSX files are mainly used for Excel 2007+.
     * This method automatically calls {@link InputStream#reset()}.
     *
     * @param in The Excel input stream.
     * @return {@code true} if it is an XLSX format Excel file, {@code false} otherwise.
     */
    public static boolean isXlsx(final InputStream in) {
        return FileMagic.OOXML == getFileMagic(in);
    }

    /**
     * Checks if the given file is an XLSX format Excel file (XSSF). XLSX files are mainly used for Excel 2007+.
     *
     * @param file The Excel file.
     * @return {@code true} if it is an XLSX format Excel file, {@code false} otherwise.
     * @throws InternalException if an I/O error occurs.
     */
    public static boolean isXlsx(final File file) {
        try {
            return FileMagic.valueOf(file) == FileMagic.OOXML;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the {@link FileMagic} from the input stream. Handles cases where {@link java.io.PushbackInputStream} does
     * not support mark and reset. The input stream is prepared using
     * {@link FileMagic#prepareToCheckMagic(InputStream)}.
     *
     * @param in The {@link InputStream} to check.
     * @return The {@link FileMagic} of the stream.
     * @throws InternalException if an I/O error occurs during magic number detection.
     */
    private static FileMagic getFileMagic(InputStream in) {
        final FileMagic magic;
        in = FileMagic.prepareToCheckMagic(in);
        try {
            magic = FileMagic.valueOf(in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return magic;
    }

}
