/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
import org.apache.poi.xssf.model.SharedStrings;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.office.excel.ExcelSaxKit;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;

/**
 * SAX-based reader for Excel 2007+ files (XLSX). For details on Excel 2007+ format, see:
 * <a href="http://www.cnblogs.com/wangmingshun/p/6654143.html">http://www.cnblogs.com/wangmingshun/p/6654143.html</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Excel07SaxReader implements ExcelSaxReader<Excel07SaxReader> {

    /**
     * Handler for parsing sheet data.
     */
    private final SheetDataSaxHandler handler;

    /**
     * Whether shared strings should be read through POI read-only mode.
     */
    private final boolean readOnlySharedStrings;

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
        this(rowHandler, padCellAtEndOfRow, null);
    }

    /**
     * Constructs a new {@code Excel07SaxReader}.
     *
     * @param rowHandler        The row handler to process each row.
     * @param padCellAtEndOfRow {@code true} to pad missing cells at the end of a row with {@code null} values,
     *                          {@code false} otherwise.
     * @param includeColumns    Optional included columns (sorted unique indexes).
     */
    public Excel07SaxReader(final RowHandler rowHandler, final boolean padCellAtEndOfRow, final int[] includeColumns) {
        this(rowHandler, padCellAtEndOfRow, includeColumns, false);
    }

    /**
     * Constructs a new {@code Excel07SaxReader}.
     *
     * @param rowHandler            The row handler to process each row.
     * @param padCellAtEndOfRow     {@code true} to pad missing cells at the end of a row with {@code null} values,
     *                              {@code false} otherwise.
     * @param includeColumns        Optional included columns (sorted unique indexes).
     * @param readOnlySharedStrings {@code true} to read shared strings through POI read-only mode, {@code false} to
     *                              keep the default shared strings behavior.
     */
    public Excel07SaxReader(final RowHandler rowHandler, final boolean padCellAtEndOfRow, final int[] includeColumns,
            final boolean readOnlySharedStrings) {
        this.handler = new SheetDataSaxHandler(rowHandler, padCellAtEndOfRow, includeColumns);
        this.readOnlySharedStrings = readOnlySharedStrings;
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

    /**
     * Reads an Excel file from a file source.
     *
     * @param file               Excel file to read.
     * @param idOrRidOrSheetName Sheet identifier (sheet ID, rId, or sheet name).
     * @return This reader instance, for chaining.
     * @throws InternalException If an {@link InvalidFormatException} or {@link IOException} occurs.
     */
    @Override
    public Excel07SaxReader read(final File file, final String idOrRidOrSheetName) throws InternalException {
        try (final OPCPackage open = OPCPackage.open(file, PackageAccess.READ)) {
            return read(open, idOrRidOrSheetName);
        } catch (final InvalidFormatException | IOException e) {
            Logger.warn(
                    false,
                    "Office",
                    e,
                    "Excel 2007 SAX file read failed: fileName={}, sheetSelectorPresent={}, exception={}",
                    file == null ? null : file.getName(),
                    idOrRidOrSheetName != null,
                    e.getClass().getSimpleName());
            throw new InternalException(e);
        }
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param in                 the input stream containing the Excel data
     * @param idOrRidOrSheetName the sheet identifier (sheet ID, rID, or sheet name)
     * @return this reader instance for chaining
     * @throws InternalException if an IOException or InvalidFormatException occurs
     */
    @Override
    public Excel07SaxReader read(final InputStream in, final String idOrRidOrSheetName) throws InternalException {
        try (final OPCPackage opcPackage = OPCPackage.open(in)) {
            return read(opcPackage, idOrRidOrSheetName);
        } catch (final IOException | InvalidFormatException e) {
            Logger.warn(
                    false,
                    "Office",
                    e,
                    "Excel 2007 SAX stream read failed: streamPresent={}, sheetSelectorPresent={}, exception={}",
                    in != null,
                    idOrRidOrSheetName != null,
                    e.getClass().getSimpleName());
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
        return read(opcPackage, String.valueOf(rid));
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
            return read(new XSSFReader(opcPackage, this.readOnlySharedStrings), idOrRidOrSheetName);
        } catch (final OpenXML4JException | IOException e) {
            Logger.warn(
                    false,
                    "Office",
                    e,
                    "Excel 2007 SAX package read failed: packagePresent={}, sheetSelectorPresent={}, exception={}",
                    opcPackage != null,
                    idOrRidOrSheetName != null,
                    e.getClass().getSimpleName());
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
        configureSharedStringsMode(xssfReader);

        // Get shared styles table, styles are not mandatory.
        try {
            this.handler.stylesTable = xssfReader.getStylesTable();
        } catch (final IOException | InvalidFormatException ignore) {
            // ignore
        }

        this.handler.sharedStrings = readSharedStrings(xssfReader);

        return readSheets(xssfReader, idOrRidOrSheetName);
    }

    /**
     * Configures the shared strings mode on an {@link XSSFReader}.
     *
     * @param xssfReader {@link XSSFReader}, the Excel reader.
     */
    private void configureSharedStringsMode(final XSSFReader xssfReader) {
        if (null != xssfReader) {
            xssfReader.setUseReadOnlySharedStringsTable(this.readOnlySharedStrings);
        }
    }

    /**
     * Reads the shared strings table with the configured shared strings mode.
     *
     * @param xssfReader {@link XSSFReader}, the Excel reader.
     * @return The shared strings table, or {@code null} when shared strings are not available.
     * @throws InternalException If shared strings cannot be read.
     */
    private SharedStrings readSharedStrings(final XSSFReader xssfReader) throws InternalException {
        try {
            return xssfReader.getSharedStringsTable();
        } catch (final IOException | InvalidFormatException e) {
            Logger.warn(
                    false,
                    "Office",
                    e,
                    "Excel 2007 SAX shared strings read failed: readOnly={}, exception={}",
                    this.readOnlySharedStrings,
                    e.getClass().getSimpleName());
            throw new InternalException(e);
        }
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
        this.handler.rid = getRid(xssfReader, idOrRidOrSheetName);
        InputStream sheetInputStream = null;
        try {
            if (this.handler.rid > -1) {
                // Find sheet by rId# or rSheet#.
                sheetInputStream = xssfReader.getSheet(RID_PREFIX + (this.handler.rid + 1));
                ExcelSaxKit.readFrom(sheetInputStream, this.handler);
                this.handler.rowHandler.doAfterAllAnalysed();
            } else {
                this.handler.rid = -1;
                // Iterate through all sheets.
                final Iterator<InputStream> sheetInputStreams = xssfReader.getSheetsData();
                while (sheetInputStreams.hasNext()) {
                    // Reset row index when reading a new sheet.
                    this.handler.index = 0;
                    this.handler.rid++;
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
    private int getRid(final XSSFReader xssfReader, String idOrRidOrSheetName) {
        // Process rId directly.
        if (StringKit.startWithIgnoreCase(idOrRidOrSheetName, RID_PREFIX)) {
            return Integer.parseInt(StringKit.removePrefixIgnoreCase(idOrRidOrSheetName, RID_PREFIX));
        }

        // Sheet index needs to be converted to rId.
        final SheetRidReader ridReader = SheetRidReader.parse(xssfReader);

        if (StringKit.startWithIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX)) {
            // Names starting with "sheetName:" are treated directly as sheet names.
            idOrRidOrSheetName = StringKit.removePrefixIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX);
            final Integer rid = ridReader.getRidByName(idOrRidOrSheetName);
            if (null != rid) {
                return rid;
            }
        } else {
            // Try to find by name.
            Integer rid = ridReader.getRidByName(idOrRidOrSheetName);
            if (null != rid) {
                return rid;
            }

            try {
                final int sheetId = Integer.parseInt(idOrRidOrSheetName);
                rid = ridReader.getRidBySheetId(sheetId);
                // If no corresponding index is found, assume the user directly provided the rId.
                return ObjectKit.defaultIfNull(rid, sheetId);
            } catch (final NumberFormatException ignore) {
                // Not a number, meaning it's not an index, and no corresponding name, throw exception.
            }
        }

        throw new IllegalArgumentException("Invalid rId or id or sheetName: " + idOrRidOrSheetName);
    }

}
