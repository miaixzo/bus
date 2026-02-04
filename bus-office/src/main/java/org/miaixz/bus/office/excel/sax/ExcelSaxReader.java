/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel.sax;

import java.io.File;
import java.io.InputStream;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;

/**
 * Interface for SAX-based Excel readers, providing common methods for reading Excel files.
 *
 * @param <T> The type of the implementing object, used for method chaining (returning {@code this}).
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ExcelSaxReader<T> {

    /**
     * The prefix for sheet r:Id.
     */
    String RID_PREFIX = "rId";
    /**
     * The prefix for sheet name.
     */
    String SHEET_NAME_PREFIX = "sheetName:";

    /**
     * Starts reading Excel from a file.
     *
     * @param file               The Excel file.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId, or a sheet name. The
     *                           rules are as follows:
     *                           <ul>
     *                           <li>If -1, process all sheets.</li>
     *                           <li>If it starts with "rId" (e.g., "rId1"), it reads the sheet with the specified rId.
     *                           rId counts from 1 (e.g., "rId1" is the first sheet).</li>
     *                           <li>If it is a sheet name (e.g., "sheet1"), it reads the corresponding sheet
     *                           directly.</li>
     *                           <li>If it is a pure number: in Excel 2003 it represents the index (0-based); in Excel
     *                           2007+ it represents the sheet ID (1-based).</li>
     *                           </ul>
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    T read(File file, String idOrRidOrSheetName) throws InternalException;

    /**
     * Starts reading Excel from an input stream. The stream is not closed after reading.
     *
     * @param in                 The Excel input stream.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId, or a sheet name. The
     *                           rules are as follows:
     *                           <ul>
     *                           <li>If -1, process all sheets.</li>
     *                           <li>If it starts with "rId" (e.g., "rId1"), it reads the sheet with the specified rId.
     *                           rId counts from 1 (e.g., "rId1" is the first sheet).</li>
     *                           <li>If it is a sheet name (e.g., "sheet1"), it reads the corresponding sheet
     *                           directly.</li>
     *                           <li>If it is a pure number: in Excel 2003 it represents the index (0-based); in Excel
     *                           2007+ it represents the sheet ID (1-based).</li>
     *                           </ul>
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    T read(InputStream in, String idOrRidOrSheetName) throws InternalException;

    /**
     * Starts reading Excel from a file path, processing all sheets.
     *
     * @param path The path to the Excel file. If it is a relative path, it is relative to the classpath.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final String path) throws InternalException {
        return read(FileKit.file(path));
    }

    /**
     * Starts reading Excel from a file, processing all sheets.
     *
     * @param file The Excel file.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final File file) throws InternalException {
        return read(file, -1);
    }

    /**
     * Starts reading Excel from an input stream, processing all sheets. The stream is not closed after reading.
     *
     * @param in The Excel input stream.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final InputStream in) throws InternalException {
        return read(in, -1);
    }

    /**
     * Starts reading Excel from a file path.
     *
     * @param path    The path to the Excel file. If it is a relative path, it is relative to the classpath.
     * @param idOrRid The sheet identifier. It can be a sheet ID or an rId. If -1, all sheets are processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final String path, final int idOrRid) throws InternalException {
        return read(FileKit.file(path), idOrRid);
    }

    /**
     * Starts reading Excel from a file path.
     *
     * @param path               The path to the Excel file.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId, or a sheet name. The
     *                           rules are as follows:
     *                           <ul>
     *                           <li>If -1, process all sheets.</li>
     *                           <li>If it starts with "rId" (e.g., "rId1"), it reads the sheet with the specified rId.
     *                           rId counts from 1 (e.g., "rId1" is the first sheet).</li>
     *                           <li>If it is a sheet name (e.g., "sheet1"), it reads the corresponding sheet
     *                           directly.</li>
     *                           <li>If it is a pure number: in Excel 2003 it represents the index (0-based); in Excel
     *                           2007+ it represents the sheet ID (1-based).</li>
     *                           </ul>
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final String path, final String idOrRidOrSheetName) throws InternalException {
        return read(FileKit.file(path), idOrRidOrSheetName);
    }

    /**
     * Starts reading Excel from a file.
     *
     * @param file    The Excel file.
     * @param idOrRid The sheet identifier. The rules are as follows:
     *                <ul>
     *                <li>If -1, process all sheets.</li>
     *                <li>If it is a pure number: in Excel 2003 it represents the index (0-based); in Excel 2007+ it
     *                represents the sheet ID (1-based).</li>
     *                </ul>
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final File file, final int idOrRid) throws InternalException {
        return read(file, String.valueOf(idOrRid));
    }

    /**
     * Starts reading Excel from an input stream. The stream is not closed after reading.
     *
     * @param in      The Excel input stream.
     * @param idOrRid The sheet identifier. The rules are as follows:
     *                <ul>
     *                <li>If -1, process all sheets.</li>
     *                <li>If it is a pure number: in Excel 2003 it represents the index (0-based); in Excel 2007+ it
     *                represents the sheet ID (1-based).</li>
     *                </ul>
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final InputStream in, final int idOrRid) throws InternalException {
        return read(in, String.valueOf(idOrRid));
    }

}
