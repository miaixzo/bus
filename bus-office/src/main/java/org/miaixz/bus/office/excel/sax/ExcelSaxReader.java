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
     * Prefix for sheet r:Id attribute.
     */
    String RID_PREFIX = "rId";
    /**
     * Prefix for sheet name.
     */
    String SHEET_NAME_PREFIX = "sheetName:";

    /**
     * Starts reading an Excel file.
     *
     * @param file               The Excel file.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId1"), or a sheet name. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    T read(File file, String idOrRidOrSheetName) throws InternalException;

    /**
     * Starts reading an Excel file from an input stream. The stream will not be closed after reading.
     *
     * @param in                 The Excel input stream.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId1"), or a sheet name. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    T read(InputStream in, String idOrRidOrSheetName) throws InternalException;

    /**
     * Starts reading an Excel file from a path, processing all sheets.
     *
     * @param path The path to the Excel file.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final String path) throws InternalException {
        return read(FileKit.file(path));
    }

    /**
     * Starts reading an Excel file, processing all sheets.
     *
     * @param file The Excel file.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final File file) throws InternalException {
        return read(file, -1);
    }

    /**
     * Starts reading an Excel file from an input stream, processing all sheets. The stream will not be closed after
     * reading.
     *
     * @param in The Excel input stream.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final InputStream in) throws InternalException {
        return read(in, -1);
    }

    /**
     * Starts reading an Excel file from a path.
     *
     * @param path               The path to the Excel file.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId1"), or a sheet name. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final String path, final int idOrRidOrSheetName) throws InternalException {
        return read(FileKit.file(path), idOrRidOrSheetName);
    }

    /**
     * Starts reading an Excel file from a path.
     *
     * @param path               The path to the Excel file.
     * @param idOrRidOrSheetName The sheet identifier in Excel, which can be a sheet ID, an rId (prefixed with "rId",
     *                           e.g., "rId1"), or a sheet name. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final String path, final String idOrRidOrSheetName) throws InternalException {
        return read(FileKit.file(path), idOrRidOrSheetName);
    }

    /**
     * Starts reading an Excel file.
     *
     * @param file The Excel file.
     * @param rid  The sheet rId in Excel. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final File file, final int rid) throws InternalException {
        return read(file, String.valueOf(rid));
    }

    /**
     * Starts reading an Excel file from an input stream. The stream will not be closed after reading.
     *
     * @param in  The Excel input stream.
     * @param rid The sheet rId in Excel. If -1, all sheets will be processed.
     * @return This reader instance, for chaining.
     * @throws InternalException If a POI-related exception occurs.
     */
    default T read(final InputStream in, final int rid) throws InternalException {
        return read(in, String.valueOf(rid));
    }

}
