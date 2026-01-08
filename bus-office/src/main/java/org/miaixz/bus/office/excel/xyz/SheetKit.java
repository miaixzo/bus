/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.cellwalk.CellHandler;
import org.apache.poi.ss.util.cellwalk.CellWalk;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.miaixz.bus.core.xyz.FieldKit;

/**
 * Utility class for {@link Sheet} related operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetKit {

    /**
     * Gets an existing sheet or creates a new one. If the sheet already exists in the workbook, it is retrieved;
     * otherwise, it is created.
     *
     * @param book      The workbook {@link Workbook}.
     * @param sheetName The name of the worksheet. {@code null} indicates the default sheet.
     * @return The worksheet {@link Sheet}.
     */
    public static Sheet getOrCreateSheet(final Workbook book, final String sheetName) {
        if (null == book) {
            return null;
        }

        Sheet sheet;
        if (null == sheetName) {
            sheet = getOrCreateSheet(book, 0);
        } else {
            sheet = book.getSheet(sheetName);
            if (null == sheet) {
                sheet = book.createSheet(sheetName);
            }
        }
        return sheet;
    }

    /**
     * Gets an existing sheet or creates a new one. This method is used to switch the sheet for reading or to
     * create/switch sheets for writing.
     *
     * @param book       The workbook {@link Workbook}.
     * @param sheetIndex The 0-based index of the worksheet.
     * @return The worksheet {@link Sheet}.
     */
    public static Sheet getOrCreateSheet(final Workbook book, final int sheetIndex) {
        Sheet sheet = null;
        try {
            sheet = book.getSheetAt(sheetIndex);
        } catch (final IllegalArgumentException ignore) {
            // ignore
        }
        if (null == sheet) {
            sheet = book.createSheet();
        }
        return sheet;
    }

    /**
     * Checks if the given sheet is empty.
     *
     * @param sheet The {@link Sheet} to check.
     * @return {@code true} if the sheet is {@code null} or contains no rows, {@code false} otherwise.
     */
    public static boolean isEmpty(final Sheet sheet) {
        return null == sheet || (sheet.getLastRowNum() == 0 && sheet.getPhysicalNumberOfRows() == 0);
    }

    /**
     * Traverses all cells in the sheet.
     *
     * @param sheet       The {@link Sheet} to traverse.
     * @param cellHandler The cell handler to process each cell.
     */
    public static void walk(final Sheet sheet, final CellHandler cellHandler) {
        walk(sheet, new CellRangeAddress(0, sheet.getLastRowNum(), 0, sheet.getLastRowNum()), cellHandler);
    }

    /**
     * Traverses cells within a specified range in the sheet.
     *
     * @param sheet       The {@link Sheet} to traverse.
     * @param range       The {@link CellRangeAddress} defining the area to traverse.
     * @param cellHandler The cell handler to process each cell.
     */
    public static void walk(final Sheet sheet, final CellRangeAddress range, final CellHandler cellHandler) {
        final CellWalk cellWalk = new CellWalk(sheet, range);
        cellWalk.traverse(cellHandler);
    }

    /**
     * Sets ignored errors for a specified cell range in the sheet. This is used to suppress green warning indicators in
     * Excel. This method only supports {@link XSSFSheet} and {@link SXSSFSheet}. See: <a href=
     * "https://stackoverflow.com/questions/23488221/how-to-remove-warning-in-excel-using-apache-poi-in-java">How to
     * remove warning in Excel using Apache POI in Java</a>
     *
     * @param sheet             The {@link Sheet}.
     * @param cellRangeAddress  The specified cell range.
     * @param ignoredErrorTypes A list of {@link IgnoredErrorType}s to ignore.
     * @throws UnsupportedOperationException if the sheet is not an instance of {@link XSSFSheet} or {@link SXSSFSheet}.
     */
    public static void addIgnoredErrors(
            final Sheet sheet,
            final CellRangeAddress cellRangeAddress,
            final IgnoredErrorType... ignoredErrorTypes) throws UnsupportedOperationException {
        if (sheet instanceof XSSFSheet) {
            ((XSSFSheet) sheet).addIgnoredErrors(cellRangeAddress, ignoredErrorTypes);
        } else if (sheet instanceof SXSSFSheet) {
            // SXSSFSheet does not provide a direct method to ignore errors. Access its internal _sh field.
            final XSSFSheet xssfSheet = (XSSFSheet) FieldKit.getFieldValue(sheet, "_sh");
            if (null != xssfSheet) {
                xssfSheet.addIgnoredErrors(cellRangeAddress, ignoredErrorTypes);
            }
        } else {
            throw new UnsupportedOperationException("Only XSSFSheet supports addIgnoredErrors");
        }
    }

    /**
     * Gets the merged cell region corresponding to the specified coordinates. If no merged region is found, returns
     * {@code null}.
     *
     * @param sheet The {@link Sheet}.
     * @param x     The x-coordinate (column index).
     * @param y     The y-coordinate (row index).
     * @return The {@link CellRangeAddress} of the merged region, or {@code null} if not found.
     */
    public static CellRangeAddress getMergedRegion(final Sheet sheet, final int x, final int y) {
        for (final CellRangeAddress ca : sheet.getMergedRegions()) {
            if (ca.isInRange(y, x)) {
                return ca;
            }
        }
        return null;
    }

}
