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
 * Utility class for SAX-based Excel reading.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelSaxKit {

    /**
     * Creates an {@link ExcelSaxReader} instance.
     *
     * @param isXlsx     {@code true} if the Excel file is in XLSX format (07 format), {@code false} otherwise.
     * @param rowHandler The row handler to process each row.
     * @return An {@link ExcelSaxReader} instance.
     */
    public static ExcelSaxReader<?> createSaxReader(final boolean isXlsx, final RowHandler rowHandler) {
        return isXlsx ? new Excel07SaxReader(rowHandler) : new Excel03SaxReader(rowHandler);
    }

    /**
     * Retrieves data value based on cell data type.
     *
     * @param cellDataType  The data type enum of the cell.
     * @param value         The raw value of the cell.
     * @param sharedStrings The {@link SharedStrings} table.
     * @param numFmtString  The number format string.
     * @return The processed data value.
     */
    public static Object getDataValue(
            CellDataType cellDataType,
            final String value,
            final SharedStrings sharedStrings,
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
     * Formats a number or date value.
     *
     * @param value        The value to format.
     * @param numFmtIndex  The index of the number format.
     * @param numFmtString The number format string.
     * @return The formatted value.
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
     * Calculates the number of null cells between two cells in the same row.
     *
     * @param preRef The coordinate of the previous cell, e.g., A1.
     * @param ref    The coordinate of the current cell, e.g., A8.
     * @return The number of empty cells between the two cells in the same row.
     */
    public static int countNullCell(final String preRef, final String ref) {
        // Excel 2007 maximum row count is 1048576, maximum column count is 16384, last column name is XFD.
        // Remove row information from column names.
        String preXfd = ObjectKit.defaultIfNull(preRef, Symbol.AT).replaceAll("\\d+", "");
        String xfd = ObjectKit.defaultIfNull(ref, Symbol.AT).replaceAll("\\d+", "");

        // A represents 65, @ represents 64. If A is counted as 1, then @ represents 0.
        // Pad to a maximum of 3 digits.
        preXfd = StringKit.fillBefore(preXfd, Symbol.C_AT, Normal._3);
        xfd = StringKit.fillBefore(xfd, Symbol.C_AT, Normal._3);

        final char[] preLetter = preXfd.toCharArray();
        final char[] letter = xfd.toCharArray();
        // For letters, maximum three digits, increment by one every 26 letters.
        final int res = (letter[0] - preLetter[0]) * 26 * 26 + (letter[1] - preLetter[1]) * 26
                + (letter[2] - preLetter[2]);
        return res - 1;
    }

    /**
     * Reads content from an Excel XML document and processes it using a {@link ContentHandler}.
     *
     * @param xmlDocStream The input stream of the Excel XML document.
     * @param handler      The document content handler, implementing this interface for callback data processing.
     * @throws DependencyException If a required dependency (e.g., Xerces) is missing.
     * @throws InternalException   If a POI-related exception occurs (wraps SAXException).
     * @throws RevisedException    If an I/O exception occurs, such as stream closure or error.
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
            // This exception is thrown by the user to force termination of reading.
        }
    }

    /**
     * Checks if a numeric record in Excel 03 SAX reading is a date format.
     *
     * @param cell           The cell record.
     * @param formatListener The {@link FormatTrackingHSSFListener}.
     * @return {@code true} if it is a date format, {@code false} otherwise.
     */
    public static boolean isDateFormat(
            final CellValueRecordInterface cell,
            final FormatTrackingHSSFListener formatListener) {
        final int formatIndex = formatListener.getFormatIndex(cell);
        final String formatString = formatListener.getFormatString(cell);
        return isDateFormat(formatIndex, formatString);
    }

    /**
     * Checks if the given format index and format string represent a date format.
     *
     * @param formatIndex  The format index, usually for built-in formats.
     * @param formatString The format string.
     * @return {@code true} if it is a date format, {@code false} otherwise.
     * @see Builder#isDateFormat(int, String)
     */
    public static boolean isDateFormat(final int formatIndex, final String formatString) {
        return Builder.isDateFormat(formatIndex, formatString);
    }

    /**
     * Gets a {@link DateTime} object from a cell value string.
     *
     * @param value The cell value string.
     * @return A {@link DateTime} object.
     */
    public static DateTime getDateValue(final String value) {
        return getDateValue(Double.parseDouble(value));
    }

    /**
     * Gets a {@link DateTime} object from a cell value (double).
     *
     * @param value The cell value (double).
     * @return A {@link DateTime} object.
     */
    public static DateTime getDateValue(final double value) {
        return DateKit.date(org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value, false));
    }

    /**
     * Gets the result value of a date or number type in Excel 03 SAX reading.
     *
     * @param cell           The cell record.
     * @param value          The value.
     * @param formatListener The {@link FormatTrackingHSSFListener}.
     * @return The value, which can be {@link DateTime}, {@link Double}, or {@link Long}.
     */
    public static Object getNumberOrDateValue(
            final CellValueRecordInterface cell,
            final double value,
            final FormatTrackingHSSFListener formatListener) {
        if (isDateFormat(cell, formatListener)) {
            // May be date format.
            return getDateValue(value);
        }
        return getNumberValue(value, formatListener.getFormatString(cell));
    }

    /**
     * Gets a numeric value. Unless the format explicitly specifies decimal places, it returns a {@link Long} if there
     * are no decimal places, otherwise a {@link Double}.
     *
     * @param value        The value string.
     * @param numFmtString The number format string.
     * @return A numeric value, which can be {@link Double} or {@link Long}.
     */
    private static Number getNumberValue(final String value, final String numFmtString) {
        if (StringKit.isBlank(value)) {
            return null;
        }
        // Possible precision loss, convert to BigDecimal if value contains decimal part.
        final double number = Double.parseDouble(value);
        if (StringKit.contains(value, Symbol.C_DOT) && !value.equals(Double.toString(number))) {
            // Precision loss.
            return MathKit.toBigDecimal(value);
        }

        return getNumberValue(number, numFmtString);
    }

    /**
     * Gets a numeric value. Unless the format explicitly specifies decimal places, it returns a {@link Long} if there
     * are no decimal places, otherwise a {@link Double}.
     *
     * @param numValue     The numeric value.
     * @param numFmtString The number format string.
     * @return A numeric value, which can be {@link Double} or {@link Long}.
     */
    private static Number getNumberValue(final double numValue, final String numFmtString) {
        // Normal number.
        if (null != numFmtString && !StringKit.contains(numFmtString, Symbol.C_DOT)) {
            final long longPart = (long) numValue;
            if (longPart == numValue) {
                // For numeric types without decimal parts, convert to Long.
                return longPart;
            }
        }
        return numValue;
    }

}
