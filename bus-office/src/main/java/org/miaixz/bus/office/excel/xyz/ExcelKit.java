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

import java.io.File;
import java.io.InputStream;

import org.miaixz.bus.core.lang.exception.DependencyException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.office.Builder;
import org.miaixz.bus.office.excel.reader.ExcelReader;
import org.miaixz.bus.office.excel.sax.ExcelSaxReader;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.miaixz.bus.office.excel.writer.BigExcelWriter;
import org.miaixz.bus.office.excel.writer.ExcelWriter;

/**
 * Excel utility class. It is not recommended to operate on sheets directly using an index, as the display order of
 * sheets in WPS/Excel is not related to the index, and there may be hidden sheets.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelKit {

    /**
     * Reads an Excel file using SAX, supporting both '03 and '07 formats.
     *
     * @param path       The path to the Excel file.
     * @param rid        The sheet rid. -1 for all sheets, 0 for the first sheet.
     * @param rowHandler The row handler.
     */
    public static void readBySax(final String path, final int rid, final RowHandler rowHandler) {
        readBySax(FileKit.file(path), rid, rowHandler);
    }

    /**
     * Reads an Excel file using SAX, supporting both '03 and '07 formats.
     *
     * @param path       The path to the Excel file.
     * @param idOrRid    The sheet ID or rid in the Excel file. The rid must be prefixed with "rId", e.g., "rId1". If
     *                   -1, all numbered sheets are processed.
     * @param rowHandler The row handler.
     */
    public static void readBySax(final String path, final String idOrRid, final RowHandler rowHandler) {
        readBySax(FileKit.file(path), idOrRid, rowHandler);
    }

    /**
     * Reads an Excel file using SAX, supporting both '03 and '07 formats.
     *
     * @param file       The Excel file.
     * @param rid        The sheet rid. -1 for all sheets, 0 for the first sheet.
     * @param rowHandler The row handler.
     */
    public static void readBySax(final File file, final int rid, final RowHandler rowHandler) {
        final ExcelSaxReader<?> reader = ExcelSaxKit.createSaxReader(Builder.isXlsx(file), rowHandler);
        reader.read(file, rid);
    }

    /**
     * Reads an Excel file using SAX, supporting both '03 and '07 formats.
     *
     * @param file               The Excel file.
     * @param idOrRidOrSheetName The sheet ID, rid, or sheet name in the Excel file. The rid must be prefixed with
     *                           "rId", e.g., "rId1". If -1, all numbered sheets are processed.
     * @param rowHandler         The row handler.
     */
    public static void readBySax(final File file, final String idOrRidOrSheetName, final RowHandler rowHandler) {
        final ExcelSaxReader<?> reader = ExcelSaxKit.createSaxReader(Builder.isXlsx(file), rowHandler);
        reader.read(file, idOrRidOrSheetName);
    }

    /**
     * Reads an Excel file from a stream using SAX, supporting both '03 and '07 formats.
     *
     * @param in         The Excel stream.
     * @param rid        The sheet rid. -1 for all sheets, 0 for the first sheet.
     * @param rowHandler The row handler.
     */
    public static void readBySax(InputStream in, final int rid, final RowHandler rowHandler) {
        in = IoKit.toMarkSupport(in);
        final ExcelSaxReader<?> reader = ExcelSaxKit.createSaxReader(Builder.isXlsx(in), rowHandler);
        reader.read(in, rid);
    }

    /**
     * Reads an Excel file from a stream using SAX, supporting both '03 and '07 formats.
     *
     * @param in                 The Excel stream.
     * @param idOrRidOrSheetName The sheet ID, rid, or sheet name in the Excel file. The rid must be prefixed with
     *                           "rId", e.g., "rId1". If -1, all numbered sheets are processed.
     * @param rowHandler         The row handler.
     */
    public static void readBySax(InputStream in, final String idOrRidOrSheetName, final RowHandler rowHandler) {
        in = IoKit.toMarkSupport(in);
        final ExcelSaxReader<?> reader = ExcelSaxKit.createSaxReader(Builder.isXlsx(in), rowHandler);
        reader.read(in, idOrRidOrSheetName);
    }

    /**
     * Gets an Excel reader for reading Excel content. By default, it reads the first sheet.
     *
     * @param bookFilePath The path to the Excel file, absolute or relative to the classpath.
     * @return An {@link ExcelReader}.
     */
    public static ExcelReader getReader(final String bookFilePath) {
        return getReader(bookFilePath, 0);
    }

    /**
     * Gets an Excel reader for reading Excel content. By default, it reads the first sheet.
     *
     * @param bookFile The Excel file.
     * @return An {@link ExcelReader}.
     */
    public static ExcelReader getReader(final File bookFile) {
        return getReader(bookFile, 0);
    }

    /**
     * Gets an Excel reader for reading Excel content.
     *
     * @param bookFilePath The path to the Excel file, absolute or relative to the classpath.
     * @param sheetIndex   The sheet index (0 for the first sheet).
     * @return An {@link ExcelReader}.
     */
    public static ExcelReader getReader(final String bookFilePath, final int sheetIndex) {
        try {
            return new ExcelReader(bookFilePath, sheetIndex);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an Excel reader for reading Excel content.
     *
     * @param bookFilePath The path to the Excel file, absolute or relative to the classpath.
     * @param sheetName    The sheet name (defaults to "sheet1").
     * @return An {@link ExcelReader}.
     */
    public static ExcelReader getReader(final String bookFilePath, final String sheetName) {
        try {
            return new ExcelReader(bookFilePath, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an Excel reader for reading Excel content.
     *
     * @param bookFile   The Excel file.
     * @param sheetIndex The sheet index (0 for the first sheet).
     * @return An {@link ExcelReader}.
     */
    public static ExcelReader getReader(final File bookFile, final int sheetIndex) {
        try {
            return new ExcelReader(bookFile, sheetIndex);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an Excel reader for reading Excel content.
     *
     * @param bookFile  The Excel file.
     * @param sheetName The sheet name (defaults to "sheet1").
     * @return An {@link ExcelReader}.
     */
    public static ExcelReader getReader(final File bookFile, final String sheetName) {
        try {
            return new ExcelReader(bookFile, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an Excel reader for reading Excel content. By default, it reads the first sheet and closes the stream
     * automatically after reading.
     *
     * @param bookStream The Excel file stream.
     * @return An {@link ExcelReader}.
     */
    public static ExcelReader getReader(final InputStream bookStream) {
        return getReader(bookStream, 0);
    }

    /**
     * Gets an Excel reader for reading Excel content. The stream is closed automatically after reading.
     *
     * @param bookStream The Excel file stream.
     * @param sheetIndex The sheet index (0 for the first sheet).
     * @return An {@link ExcelReader}.
     */
    public static ExcelReader getReader(final InputStream bookStream, final int sheetIndex) {
        try {
            return new ExcelReader(bookStream, sheetIndex);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an Excel reader for reading Excel content. The stream is closed automatically after reading.
     *
     * @param bookStream The Excel file stream.
     * @param sheetName  The sheet name (defaults to "sheet1").
     * @return An {@link ExcelReader}.
     */
    public static ExcelReader getReader(final InputStream bookStream, final String sheetName) {
        try {
            return new ExcelReader(bookStream, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an {@link ExcelWriter}, which defaults to writing to the first sheet. If no output file path is provided,
     * you can only write to a stream using {@code ExcelWriter#flush(OutputStream)}. To write to a file, you must set
     * the target file using {@link ExcelWriter#setTargetFile(File)} and then call {@link ExcelWriter#flush()}.
     *
     * @return An {@link ExcelWriter}.
     */
    public static ExcelWriter getWriter() {
        try {
            return new ExcelWriter();
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an {@link ExcelWriter}, which defaults to writing to the first sheet.
     *
     * @param isXlsx Whether the format is xlsx.
     * @return An {@link ExcelWriter}.
     */
    public static ExcelWriter getWriter(final boolean isXlsx) {
        try {
            return new ExcelWriter(isXlsx);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an {@link ExcelWriter}, which defaults to writing to the first sheet.
     *
     * @param templateFilePath The path to the template file.
     * @return An {@link ExcelWriter}.
     */
    public static ExcelWriter getWriter(final String templateFilePath) {
        try {
            return new ExcelWriter(templateFilePath);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an {@link ExcelWriter}, which defaults to writing to the first sheet.
     *
     * @param sheetName The sheet name.
     * @return An {@link ExcelWriter}.
     */
    public static ExcelWriter getWriterWithSheet(final String sheetName) {
        try {
            return new ExcelWriter((File) null, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an {@link ExcelWriter}, which defaults to writing to the first sheet, named "sheet1".
     *
     * @param templateFile The target file.
     * @return An {@link ExcelWriter}.
     */
    public static ExcelWriter getWriter(final File templateFile) {
        try {
            return new ExcelWriter(templateFile);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an {@link ExcelWriter}.
     *
     * @param templateFilePath The path to the target file.
     * @param sheetName        The sheet name.
     * @return An {@link ExcelWriter}.
     */
    public static ExcelWriter getWriter(final String templateFilePath, final String sheetName) {
        try {
            return new ExcelWriter(templateFilePath, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets an {@link ExcelWriter}.
     *
     * @param templateFilePath The target file.
     * @param sheetName        The sheet name.
     * @return An {@link ExcelWriter}.
     */
    public static ExcelWriter getWriter(final File templateFilePath, final String sheetName) {
        try {
            return new ExcelWriter(templateFilePath, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a {@link BigExcelWriter}, which defaults to writing to the first sheet. If no output file path is provided,
     * you can only write to a stream using {@code ExcelWriter#flush(OutputStream)}.
     *
     * @return A {@link BigExcelWriter}.
     */
    public static BigExcelWriter getBigWriter() {
        try {
            return new BigExcelWriter();
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a {@link BigExcelWriter}, which defaults to writing to the first sheet.
     *
     * @param rowAccessWindowSize The number of rows to keep in memory.
     * @return A {@link BigExcelWriter}.
     */
    public static BigExcelWriter getBigWriter(final int rowAccessWindowSize) {
        try {
            return new BigExcelWriter(rowAccessWindowSize);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a {@link BigExcelWriter}, which defaults to writing to the first sheet.
     *
     * @param destFilePath The path to the destination file.
     * @return A {@link BigExcelWriter}.
     */
    public static BigExcelWriter getBigWriter(final String destFilePath) {
        try {
            return new BigExcelWriter(destFilePath);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a {@link BigExcelWriter}, which defaults to writing to the first sheet, named "sheet1".
     *
     * @param destFile The destination file.
     * @return A {@link BigExcelWriter}.
     */
    public static BigExcelWriter getBigWriter(final File destFile) {
        try {
            return new BigExcelWriter(destFile);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a {@link BigExcelWriter}.
     *
     * @param destFilePath The path to the destination file.
     * @param sheetName    The sheet name.
     * @return A {@link BigExcelWriter}.
     */
    public static BigExcelWriter getBigWriter(final String destFilePath, final String sheetName) {
        try {
            return new BigExcelWriter(destFilePath, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a {@link BigExcelWriter}.
     *
     * @param destFile  The destination file.
     * @param sheetName The sheet name.
     * @return A {@link BigExcelWriter}.
     */
    public static BigExcelWriter getBigWriter(final File destFile, final String sheetName) {
        try {
            return new BigExcelWriter(destFile, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

}
