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
package org.miaixz.bus.office.excel.sax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.miaixz.bus.office.excel.xyz.ExcelSaxKit;

/**
 * SAX-based reader for Excel 2007+ files (XLSX). For details on Excel 2007+ format, see:
 * <a href="http://www.cnblogs.com/wangmingshun/p/6654143.html">http://www.cnblogs.com/wangmingshun/p/6654143.html</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Excel07SaxReader implements ExcelSaxReader<Excel07SaxReader> {

    private final SheetDataSaxHandler handler;

    /**
     * Constructs a new {@code Excel07SaxReader}.
     *
     * @param rowHandler The row handler to process each row.
     */
    public Excel07SaxReader(final RowHandler rowHandler) {
        this(rowHandler, false);
    }

    /**
     * Constructs a new {@code Excel07SaxReader}.
     *
     * @param rowHandler        The row handler to process each row.
     * @param padCellAtEndOfRow {@code true} to pad missing cells at the end of a row with {@code null} values,
     *                          {@code false} otherwise.
     */
    public Excel07SaxReader(final RowHandler rowHandler, final boolean padCellAtEndOfRow) {
        this.handler = new SheetDataSaxHandler(rowHandler, padCellAtEndOfRow);
    }

    /**
     * Sets the row handler for processing rows.
     *
     * @param rowHandler The row handler.
     * @return This reader instance, for chaining.
     */
    public Excel07SaxReader setRowHandler(final RowHandler rowHandler) {
        this.handler.setRowHandler(rowHandler);
        return this;
    }

    @Override
    public Excel07SaxReader read(final File file, final int rid) throws InternalException {
        return read(file, RID_PREFIX + rid);
    }

    @Override
    public Excel07SaxReader read(final File file, final String idOrRidOrSheetName) throws InternalException {
        try (final OPCPackage open = OPCPackage.open(file, PackageAccess.READ)) {
            return read(open, idOrRidOrSheetName);
        } catch (final InvalidFormatException | IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public Excel07SaxReader read(final InputStream in, final int rid) throws InternalException {
        return read(in, RID_PREFIX + rid);
    }

    @Override
    public Excel07SaxReader read(final InputStream in, final String idOrRidOrSheetName) throws InternalException {
        try (final OPCPackage opcPackage = OPCPackage.open(in)) {
            return read(opcPackage, idOrRidOrSheetName);
        } catch (final IOException | InvalidFormatException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Starts reading an Excel file. Sheet numbers are 0-based.
     *
     * @param opcPackage {@link OPCPackage}, the Excel package. The package will not be closed after reading.
     * @param rid        The sheet rId in Excel. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    public Excel07SaxReader read(final OPCPackage opcPackage, final int rid) throws InternalException {
        return read(opcPackage, RID_PREFIX + rid);
    }

    /**
     * Starts reading an Excel file. Sheet numbers are 0-based.
     *
     * @param opcPackage         {@link OPCPackage}, the Excel package. The package will not be closed after reading.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId1"), or a sheet name. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    public Excel07SaxReader read(final OPCPackage opcPackage, final String idOrRidOrSheetName)
            throws InternalException {
        try {
            return read(new XSSFReader(opcPackage), idOrRidOrSheetName);
        } catch (final OpenXML4JException | IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Starts reading an Excel file. Sheet numbers are 0-based.
     *
     * @param xssfReader         {@link XSSFReader}, the Excel reader.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId1"), or a sheet name. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    public Excel07SaxReader read(final XSSFReader xssfReader, final String idOrRidOrSheetName)
            throws InternalException {
        // Get shared styles table, styles are not mandatory.
        try {
            this.handler.stylesTable = xssfReader.getStylesTable();
        } catch (final IOException | InvalidFormatException ignore) {
            // ignore
        }

        // Get shared strings table.
        // Starting from POI-5.2.0, the return value has changed, causing MethodNotFoundException in actual use.
        // Reflection is used here to call the method, resolving the return value change issue across different
        // versions.
        // this.handler.sharedStrings = xssfReader.getSharedStringsTable();
        this.handler.sharedStrings = MethodKit.invoke(xssfReader, "getSharedStringsTable");

        return readSheets(xssfReader, idOrRidOrSheetName);
    }

    /**
     * Starts reading Excel sheets. Sheet numbers are 0-based.
     *
     * @param xssfReader         {@link XSSFReader}, the Excel reader.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId0"), or a sheet name. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    private Excel07SaxReader readSheets(final XSSFReader xssfReader, final String idOrRidOrSheetName)
            throws InternalException {
        this.handler.sheetIndex = getSheetIndex(xssfReader, idOrRidOrSheetName);
        InputStream sheetInputStream = null;
        try {
            if (this.handler.sheetIndex > -1) {
                // Find sheet by rId# or rSheet#.
                sheetInputStream = xssfReader.getSheet(RID_PREFIX + (this.handler.sheetIndex + 1));
                ExcelSaxKit.readFrom(sheetInputStream, this.handler);
                this.handler.rowHandler.doAfterAllAnalysed();
            } else {
                this.handler.sheetIndex = -1;
                // Iterate through all sheets.
                final Iterator<InputStream> sheetInputStreams = xssfReader.getSheetsData();
                while (sheetInputStreams.hasNext()) {
                    // Reset row index when reading a new sheet.
                    this.handler.index = 0;
                    this.handler.sheetIndex++;
                    sheetInputStream = sheetInputStreams.next();
                    ExcelSaxKit.readFrom(sheetInputStream, this.handler);
                    this.handler.rowHandler.doAfterAllAnalysed();
                }
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(sheetInputStream);
        }
        return this;
    }

    /**
     * Gets the sheet index (0-based).
     * <ul>
     * <li>If the input starts with 'rId', the 'rId' prefix is removed directly.</li>
     * <li>If the input is a pure number, it is treated as a sheet index and converted to rId via
     * {@link SheetRidReader}.</li>
     * <li>If the input is any other string, it is treated as a sheet name and converted to rId via
     * {@link SheetRidReader}.</li>
     * </ul>
     *
     * @param xssfReader         {@link XSSFReader}, the Excel reader.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId0"), or a sheet name. If -1, all sheets will be processed.
     * @return The sheet index (0-based).
     * @throws IllegalArgumentException if the provided {@code idOrRidOrSheetName} is invalid or not found.
     */
    private int getSheetIndex(final XSSFReader xssfReader, String idOrRidOrSheetName) {
        // Process rId directly.
        if (StringKit.startWithIgnoreCase(idOrRidOrSheetName, RID_PREFIX)) {
            return Integer.parseInt(StringKit.removePrefixIgnoreCase(idOrRidOrSheetName, RID_PREFIX));
        }

        // Sheet index needs to be converted to rId.
        final SheetRidReader ridReader = SheetRidReader.parse(xssfReader);

        if (StringKit.startWithIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX)) {
            // Names starting with "name:" are treated directly as sheet names.
            idOrRidOrSheetName = StringKit.removePrefixIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX);
            final Integer rid = ridReader.getRidByNameBase0(idOrRidOrSheetName);
            if (null != rid) {
                return rid;
            }
        } else {
            // Try to find by name.
            Integer rid = ridReader.getRidByNameBase0(idOrRidOrSheetName);
            if (null != rid) {
                return rid;
            }

            try {
                final int sheetIndex = Integer.parseInt(idOrRidOrSheetName);
                rid = ridReader.getRidBySheetIdBase0(sheetIndex);
                // If no corresponding index is found, assume the user directly provided the rId.
                return ObjectKit.defaultIfNull(rid, sheetIndex);
            } catch (final NumberFormatException ignore) {
                // Not a number, meaning it's not an index, and no corresponding name, throw exception.
            }
        }

        throw new IllegalArgumentException("Invalid rId or id or sheetName: " + idOrRidOrSheetName);
    }

}
