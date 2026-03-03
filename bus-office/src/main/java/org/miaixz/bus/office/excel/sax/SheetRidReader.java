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

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.xyz.ExcelSaxKit;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads the mapping between sheetId and r:id in the sheet tag when reading Excel using SAX.
 * <p>
 * Similar to:
 * 
 * <pre>
 *  sheet name="Sheet6" sheetId="4" r:id="rId6"
 * </pre>
 *
 * The reading result is:
 *
 * <pre>
 *     {"4": "6"}
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetRidReader extends DefaultHandler {

    /**
     * The tag name for a sheet element in the Excel XML.
     */
    private final static String TAG_NAME = "sheet";
    /**
     * The attribute name for the relationship ID (r:id).
     */
    private final static String RID_ATTR = "r:id";
    /**
     * The attribute name for the sheet ID.
     */
    private final static String SHEET_ID_ATTR = "sheetId";
    /**
     * The attribute name for the sheet name.
     */
    private final static String NAME_ATTR = "name";
    /**
     * Map storing the mapping from sheet ID (1-based) to relationship ID (1-based).
     */
    private final Map<Integer, Integer> ID_RID_MAP = new LinkedHashMap<>();
    /**
     * Map storing the mapping from sheet name to relationship ID (1-based).
     */
    private final Map<String, Integer> NAME_RID_MAP = new LinkedHashMap<>();

    /**
     * Parses sheet name, sheet ID, and other related information from {@link XSSFReader}.
     *
     * @param reader The {@link XSSFReader} instance.
     * @return A {@code SheetRidReader} instance with parsed data.
     */
    public static SheetRidReader parse(final XSSFReader reader) {
        return new SheetRidReader().read(reader);
    }

    /**
     * Reads the mapping between sheetId and r:id from the sheet tags in the Workbook XML.
     *
     * @param xssfReader The XSSF reader.
     * @return This {@code SheetRidReader} instance, for chaining.
     * @throws InternalException if an {@link InvalidFormatException} or {@link IOException} occurs.
     */
    public SheetRidReader read(final XSSFReader xssfReader) {
        InputStream workbookData = null;
        try {
            workbookData = xssfReader.getWorkbookData();
            ExcelSaxKit.readFrom(workbookData, this);
        } catch (final InvalidFormatException | IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(workbookData);
        }
        return this;
    }

    /**
     * Gets the relationship ID (rId) by sheet ID. The rId is 1-based.
     *
     * @param sheetId The sheet ID (1-based).
     * @return The relationship ID (1-based), or {@code null} if not found.
     */
    public Integer getRidBySheetId(final int sheetId) {
        return ID_RID_MAP.get(sheetId);
    }

    /**
     * Gets the relationship ID (rId) by sheet name. The rId is 1-based.
     *
     * @param sheetName The name of the sheet.
     * @return The relationship ID (1-based), or {@code null} if not found.
     */
    public Integer getRidByName(final String sheetName) {
        return NAME_RID_MAP.get(sheetName);
    }

    /**
     * Gets the relationship ID (rId) by sheet index. The rId is 1-based.
     *
     * @param index The index of the sheet, starting from 0.
     * @return The relationship ID (1-based), or {@code null} if not found.
     */
    public Integer getRidByIndex(final int index) {
        return CollKit.get(this.NAME_RID_MAP.values(), index);
    }

    /**
     * Gets a list of all sheet names.
     *
     * @return A list of sheet names.
     */
    public List<String> getSheetNames() {
        return ListKit.of(this.NAME_RID_MAP.keySet());
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param uri        the namespace URI
     * @param localName  the local name
     * @param qName      the qualified name
     * @param attributes the attached attributes
     */
    @Override
    public void startElement(
            final String uri,
            final String localName,
            final String qName,
            final Attributes attributes) {
        if (TAG_NAME.equalsIgnoreCase(localName)) {
            final String ridStr = attributes.getValue(RID_ATTR);
            if (StringKit.isEmpty(ridStr)) {
                return;
            }
            final int rid = Integer.parseInt(StringKit.removePrefixIgnoreCase(ridStr, Excel07SaxReader.RID_PREFIX));

            // Map sheet name to rId
            final String name = attributes.getValue(NAME_ATTR);
            if (StringKit.isNotEmpty(name)) {
                NAME_RID_MAP.put(name, rid);
            }

            // Map sheetId to rId
            final String sheetIdStr = attributes.getValue(SHEET_ID_ATTR);
            if (StringKit.isNotEmpty(sheetIdStr)) {
                ID_RID_MAP.put(Integer.parseInt(sheetIdStr), rid);
            }
        }
    }

}
