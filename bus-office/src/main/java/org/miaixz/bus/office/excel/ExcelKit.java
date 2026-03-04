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
package org.miaixz.bus.office.excel;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.exception.DependencyException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.office.Builder;
import org.miaixz.bus.office.excel.reader.BigExcelReader;
import org.miaixz.bus.office.excel.reader.ExcelReadConfig;
import org.miaixz.bus.office.excel.reader.ExcelReadConfig.ReadMode;
import org.miaixz.bus.office.excel.reader.ExcelReader;
import org.miaixz.bus.office.excel.sax.ExcelSaxReader;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.miaixz.bus.office.excel.writer.BigExcelWriter;
import org.miaixz.bus.office.excel.writer.ExcelWriteConfig;
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
     * File size threshold for AUTO read mode to prefer streaming.
     */
    private static final long AUTO_STREAM_FILE_SIZE_THRESHOLD = 64L * 1024 * 1024;

    /**
     * Minimum transfer batch size.
     */
    private static final int TRANSFER_BATCH_SIZE_MIN = 2_000;

    /**
     * Maximum transfer batch size.
     */
    private static final int TRANSFER_BATCH_SIZE_MAX = 20_000;

    /**
     * Target cell count per transfer batch used for adaptive sizing.
     */
    private static final int TRANSFER_TARGET_CELLS_PER_BATCH = 300_000;

    /**
     * Default queue capacity for transfer pipeline.
     */
    private static final int TRANSFER_QUEUE_CAPACITY_DEFAULT = 16;

    /**
     * Minimum queue capacity for transfer pipeline.
     */
    private static final int TRANSFER_QUEUE_CAPACITY_MIN = 8;

    /**
     * Maximum queue capacity for transfer pipeline.
     */
    private static final int TRANSFER_QUEUE_CAPACITY_MAX = 64;

    /**
     * Default queue poll timeout in milliseconds.
     */
    private static final long TRANSFER_POLL_TIMEOUT_MS_DEFAULT = 200L;

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

    /**
     * Gets a {@link BigExcelWriter} with SXSSF tuning options.
     *
     * @param destFile              The destination file.
     * @param rowAccessWindowSize   The number of rows to keep in memory.
     * @param compressTmpFiles      Whether to compress SXSSF temp files.
     * @param useSharedStringsTable Whether to use shared strings table.
     * @param sheetName             The sheet name.
     * @return A {@link BigExcelWriter}.
     */
    public static BigExcelWriter getBigWriter(
            final File destFile,
            final int rowAccessWindowSize,
            final boolean compressTmpFiles,
            final boolean useSharedStringsTable,
            final String sheetName) {
        try {
            return new BigExcelWriter(destFile, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable,
                    sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a streaming reader for large Excel files.
     *
     * @param bookFilePath Excel file path.
     * @return A {@link BigExcelReader}.
     */
    public static BigExcelReader getBigReader(final String bookFilePath) {
        return getBigReader(bookFilePath, -1);
    }

    /**
     * Gets a streaming reader for large Excel files.
     *
     * @param bookFilePath Excel file path.
     * @param sheetIndex   sheet index, -1 means all sheets.
     * @return A {@link BigExcelReader}.
     */
    public static BigExcelReader getBigReader(final String bookFilePath, final int sheetIndex) {
        try {
            return new BigExcelReader(bookFilePath, sheetIndex);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a streaming reader for large Excel files.
     *
     * @param bookFilePath Excel file path.
     * @param sheetName    sheet name.
     * @return A {@link BigExcelReader}.
     */
    public static BigExcelReader getBigReader(final String bookFilePath, final String sheetName) {
        try {
            return new BigExcelReader(bookFilePath, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a streaming reader for large Excel files.
     *
     * @param bookFile Excel file.
     * @return A {@link BigExcelReader}.
     */
    public static BigExcelReader getBigReader(final File bookFile) {
        return getBigReader(bookFile, -1);
    }

    /**
     * Gets a streaming reader for large Excel files.
     *
     * @param bookFile   Excel file.
     * @param sheetIndex sheet index, -1 means all sheets.
     * @return A {@link BigExcelReader}.
     */
    public static BigExcelReader getBigReader(final File bookFile, final int sheetIndex) {
        try {
            return new BigExcelReader(bookFile, sheetIndex);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a streaming reader for large Excel files.
     *
     * @param bookFile  Excel file.
     * @param sheetName sheet name.
     * @return A {@link BigExcelReader}.
     */
    public static BigExcelReader getBigReader(final File bookFile, final String sheetName) {
        try {
            return new BigExcelReader(bookFile, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a streaming reader for large Excel files.
     *
     * @param bookStream Excel stream.
     * @return A {@link BigExcelReader}.
     */
    public static BigExcelReader getBigReader(final InputStream bookStream) {
        return getBigReader(bookStream, -1);
    }

    /**
     * Gets a streaming reader for large Excel files.
     *
     * @param bookStream Excel stream.
     * @param sheetIndex sheet index, -1 means all sheets.
     * @return A {@link BigExcelReader}.
     */
    public static BigExcelReader getBigReader(final InputStream bookStream, final int sheetIndex) {
        try {
            return new BigExcelReader(bookStream, sheetIndex);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Gets a streaming reader for large Excel files.
     *
     * @param bookStream Excel stream.
     * @param sheetName  sheet name.
     * @return A {@link BigExcelReader}.
     */
    public static BigExcelReader getBigReader(final InputStream bookStream, final String sheetName) {
        try {
            return new BigExcelReader(bookStream, sheetName);
        } catch (final NoClassDefFoundError e) {
            throw new DependencyException(ObjectKit.defaultIfNull(e.getCause(), e), Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Reads Excel with unified routing.
     *
     * @param path       Excel file path.
     * @param config     read config.
     * @param rowHandler row handler.
     */
    public static void read(final String path, final ExcelReadConfig config, final RowHandler rowHandler) {
        read(FileKit.file(path), config, rowHandler);
    }

    /**
     * Reads Excel with unified routing.
     *
     * @param file       Excel file.
     * @param config     read config.
     * @param rowHandler row handler.
     */
    public static void read(final File file, final ExcelReadConfig config, final RowHandler rowHandler) {
        read(file, null, config, rowHandler);
    }

    /**
     * Reads Excel with unified routing.
     *
     * @param inputStream Excel stream.
     * @param config      read config.
     * @param rowHandler  row handler.
     */
    public static void read(final InputStream inputStream, final ExcelReadConfig config, final RowHandler rowHandler) {
        read(null, inputStream, config, rowHandler);
    }

    /**
     * Reads Excel with unified routing.
     *
     * @param file        excel file.
     * @param inputStream excel stream.
     * @param config      read config.
     * @param rowHandler  row handler.
     */
    private static void read(
            final File file,
            final InputStream inputStream,
            final ExcelReadConfig config,
            final RowHandler rowHandler) {
        if (null == rowHandler) {
            return;
        }

        final ExcelReadConfig effectiveConfig = null == config ? new ExcelReadConfig() : config;
        final ReadMode mode = resolveReadMode(effectiveConfig, file, inputStream);

        if (ReadMode.MEMORY == mode) {
            if (null != file) {
                readInMemory(file, effectiveConfig, rowHandler);
            } else {
                readInMemory(inputStream, effectiveConfig, rowHandler);
            }
            return;
        }

        if (null != file) {
            readInStreaming(file, effectiveConfig, rowHandler);
        } else {
            readInStreaming(inputStream, effectiveConfig, rowHandler);
        }
    }

    /**
     * Writes data with unified routing.
     *
     * @param path   destination file path.
     * @param data   row data.
     * @param config write config.
     * @return Created writer instance.
     */
    public static ExcelWriter write(final String path, final Iterable<?> data, final ExcelWriteConfig config) {
        return write(FileKit.file(path), data, config);
    }

    /**
     * Writes data with unified routing.
     *
     * @param dest   destination file.
     * @param data   row data.
     * @param config write config.
     * @return Created writer instance.
     */
    public static ExcelWriter write(final File dest, final Iterable<?> data, final ExcelWriteConfig config) {
        final ExcelWriteConfig effectiveConfig = optimizeBigWriterDefaults(
                null == config ? new ExcelWriteConfig() : config);
        final boolean useBigWriter = effectiveConfig.isAutoSplitSheet()
                || effectiveConfig.getExpectedRows() >= 1_000_000;

        final ExcelWriter writer;
        if (useBigWriter) {
            final BigExcelWriter bigWriter = getBigWriter(
                    dest,
                    effectiveConfig.getBigWriterRowAccessWindowSize(),
                    effectiveConfig.isBigWriterCompressTmpFiles(),
                    effectiveConfig.isBigWriterUseSharedStringsTable(),
                    null);
            bigWriter.setConfig(effectiveConfig);
            writer = bigWriter;
        } else {
            writer = getWriter(dest);
            writer.setConfig(effectiveConfig);
        }

        if (null != data) {
            writer.write(data, false);
        }
        return writer;
    }

    /**
     * Transfers rows from source Excel to destination Excel using a bounded producer-consumer pipeline.
     *
     * @param source      Source Excel file.
     * @param dest        Destination Excel file.
     * @param readConfig  Read config controlling row filtering and transfer pipeline parameters.
     * @param writeConfig Write config controlling big-writer behavior and sheet splitting.
     */
    public static void transfer(
            final File source,
            final File dest,
            final ExcelReadConfig readConfig,
            final ExcelWriteConfig writeConfig) {
        final ExcelReadConfig effectiveReadConfig = null == readConfig ? new ExcelReadConfig() : readConfig;
        final ExcelWriteConfig effectiveWriteConfig = optimizeBigWriterDefaults(
                null == writeConfig ? new ExcelWriteConfig() : writeConfig);

        final BigExcelWriter writer = getBigWriter(
                dest,
                effectiveWriteConfig.getBigWriterRowAccessWindowSize(),
                effectiveWriteConfig.isBigWriterCompressTmpFiles(),
                effectiveWriteConfig.isBigWriterUseSharedStringsTable(),
                null);
        writer.setConfig(effectiveWriteConfig);

        final int includeColumnCount = null == effectiveReadConfig.getIncludeColumns() ? 0
                : effectiveReadConfig.getIncludeColumns().length;
        final int inferredColumnCount = inferColumnCount(effectiveReadConfig, includeColumnCount);
        final int batchSize = resolveTransferBatchSize(effectiveReadConfig, inferredColumnCount);
        final int queueCapacity = resolveTransferQueueCapacity(effectiveReadConfig, inferredColumnCount);
        final long pollTimeoutMs = resolveTransferPollTimeoutMs(effectiveReadConfig);
        final BlockingQueue<List<List<Object>>> queue = new ArrayBlockingQueue<>(queueCapacity);
        final List<List<Object>> poison = ListKit.empty();
        final AtomicReference<Throwable> error = new AtomicReference<>();

        final Thread writerThread = new Thread(() -> consumeBatches(writer, queue, poison, error, pollTimeoutMs),
                "excel-transfer-writer");
        writerThread.start();

        try (BigExcelReader reader = getBigReader(source, -1)) {
            reader.setConfig(effectiveReadConfig);
            reader.readBatch(batchSize, rows -> {
                try {
                    while (!queue.offer(rows, pollTimeoutMs, TimeUnit.MILLISECONDS)) {
                        final Throwable writerError = error.get();
                        if (null != writerError) {
                            throw new IllegalStateException("Writer thread failed", writerError);
                        }
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while queueing row batch", e);
                }
            });
        } catch (final Throwable e) {
            error.compareAndSet(null, e);
            throw e;
        } finally {
            offerPoison(queue, poison, pollTimeoutMs);
            joinWriterThread(writerThread, error);
            writer.close();
        }

        final Throwable transferError = error.get();
        if (null != transferError) {
            throw transferError instanceof RuntimeException ? (RuntimeException) transferError
                    : new IllegalStateException("Excel transfer failed", transferError);
        }
    }

    /**
     * Resolves effective column count used for transfer tuning.
     *
     * @param readConfig         read config.
     * @param includeColumnCount include-column count inferred from projection.
     * @return effective column count used by adaptive transfer sizing.
     */
    private static int inferColumnCount(final ExcelReadConfig readConfig, final int includeColumnCount) {
        if (includeColumnCount > 0) {
            return includeColumnCount;
        }
        return Math.max(1, readConfig.getTransferEstimatedColumnCount());
    }

    /**
     * Resolves transfer batch size.
     *
     * @param readConfig       read config.
     * @param effectiveColumns effective column count.
     * @return transfer batch size.
     */
    private static int resolveTransferBatchSize(final ExcelReadConfig readConfig, final int effectiveColumns) {
        if (readConfig.getBatchSize() > 0) {
            return readConfig.getBatchSize();
        }
        final int estimated = TRANSFER_TARGET_CELLS_PER_BATCH / Math.max(1, effectiveColumns);
        return Math.max(TRANSFER_BATCH_SIZE_MIN, Math.min(TRANSFER_BATCH_SIZE_MAX, estimated));
    }

    /**
     * Resolves transfer queue capacity.
     *
     * @param readConfig       read config.
     * @param effectiveColumns effective column count.
     * @return transfer queue capacity.
     */
    private static int resolveTransferQueueCapacity(final ExcelReadConfig readConfig, final int effectiveColumns) {
        if (readConfig.getTransferQueueCapacity() > 0) {
            return readConfig.getTransferQueueCapacity();
        }
        final int estimated = TRANSFER_QUEUE_CAPACITY_DEFAULT + Math.max(0, (effectiveColumns - 50) / 10);
        return Math.max(TRANSFER_QUEUE_CAPACITY_MIN, Math.min(TRANSFER_QUEUE_CAPACITY_MAX, estimated));
    }

    /**
     * Resolves transfer queue poll timeout.
     *
     * @param readConfig read config.
     * @return transfer queue poll timeout in milliseconds.
     */
    private static long resolveTransferPollTimeoutMs(final ExcelReadConfig readConfig) {
        return readConfig.getTransferPollTimeoutMs() > 0 ? readConfig.getTransferPollTimeoutMs()
                : TRANSFER_POLL_TIMEOUT_MS_DEFAULT;
    }

    /**
     * Reads data in memory mode from file source.
     *
     * @param file       excel file.
     * @param config     read config.
     * @param rowHandler row handler.
     */
    private static void readInMemory(final File file, final ExcelReadConfig config, final RowHandler rowHandler) {
        try (ExcelReader reader = getReader(file, 0)) {
            readInMemory(reader, config, rowHandler);
        }
    }

    /**
     * Reads data in memory mode from stream source.
     *
     * @param inputStream excel stream.
     * @param config      read config.
     * @param rowHandler  row handler.
     */
    private static void readInMemory(
            final InputStream inputStream,
            final ExcelReadConfig config,
            final RowHandler rowHandler) {
        try (ExcelReader reader = getReader(inputStream, 0)) {
            readInMemory(reader, config, rowHandler);
        }
    }

    /**
     * Reads data in streaming mode from file source.
     *
     * @param file       excel file.
     * @param config     read config.
     * @param rowHandler row handler.
     */
    private static void readInStreaming(final File file, final ExcelReadConfig config, final RowHandler rowHandler) {
        try (BigExcelReader reader = getBigReader(file, -1)) {
            reader.setConfig(config);
            reader.read(rowHandler);
        }
    }

    /**
     * Reads data in streaming mode from stream source.
     *
     * @param inputStream excel stream.
     * @param config      read config.
     * @param rowHandler  row handler.
     */
    private static void readInStreaming(
            final InputStream inputStream,
            final ExcelReadConfig config,
            final RowHandler rowHandler) {
        try (BigExcelReader reader = getBigReader(inputStream, -1)) {
            reader.setConfig(config);
            reader.read(rowHandler);
        }
    }

    /**
     * Reads data in memory mode and applies unified filters/projection.
     *
     * @param reader     excel reader.
     * @param config     read config.
     * @param rowHandler row handler.
     */
    private static void readInMemory(
            final ExcelReader reader,
            final ExcelReadConfig config,
            final RowHandler rowHandler) {
        // Keep empty rows during workbook scan, then apply unified filtering below.
        reader.getConfig().setIgnoreEmptyRow(false);

        final long safeStart = Math.max(0, config.getStartRow());
        final long safeEnd = config.getEndRow() < 0 ? Long.MAX_VALUE : config.getEndRow();
        if (safeStart > safeEnd) {
            rowHandler.doAfterAllAnalysed();
            return;
        }

        final int[] includeColumns = RowKit.normalizeIncludeColumns(config.getIncludeColumns());
        final boolean hasIncludeColumns = null != includeColumns && includeColumns.length > 0;
        final int sheetCount = reader.getSheetCount();

        long globalRowIndex = 0;
        boolean reachedEnd = false;
        final org.miaixz.bus.office.excel.cell.editors.CellEditor cellEditor = reader.getConfig().getCellEditor();
        for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
            reader.setSheet(sheetIndex);
            final org.apache.poi.ss.usermodel.Sheet sheet = reader.getSheet();
            final int firstRowNum = Math.max(0, sheet.getFirstRowNum());
            final int lastRowNum = sheet.getLastRowNum();

            for (int rowIndexInSheet = firstRowNum; rowIndexInSheet <= lastRowNum; rowIndexInSheet++) {
                final org.apache.poi.ss.usermodel.Row poiRow = sheet.getRow(rowIndexInSheet);
                if (null == poiRow) {
                    // Keep global cursor aligned with SAX mode: missing rows are not emitted.
                    continue;
                }

                if (globalRowIndex > safeEnd) {
                    reachedEnd = true;
                    break;
                }

                if (globalRowIndex < safeStart) {
                    globalRowIndex++;
                    continue;
                }

                final List<Object> row = RowKit.readRow(poiRow, cellEditor);
                final List<Object> projected = hasIncludeColumns ? RowKit.projectColumns(row, includeColumns) : row;
                if (config.isIgnoreEmptyRow() && RowKit.isEmptyRow(projected)) {
                    globalRowIndex++;
                    continue;
                }

                rowHandler.handle(sheetIndex, rowIndexInSheet, projected);
                globalRowIndex++;
            }

            if (reachedEnd) {
                break;
            }
        }

        rowHandler.doAfterAllAnalysed();
    }

    /**
     * Checks whether include columns are configured.
     *
     * @param includeColumns include columns.
     * @return {@code true} if at least one valid include column exists.
     */
    private static boolean hasIncludeColumns(final int[] includeColumns) {
        final int[] normalized = RowKit.normalizeIncludeColumns(includeColumns);
        return null != normalized && normalized.length > 0;
    }

    /**
     * Optimizes big writer defaults for throughput-sensitive scenarios.
     *
     * @param config Write config to optimize.
     * @return Optimized write config.
     */
    private static ExcelWriteConfig optimizeBigWriterDefaults(final ExcelWriteConfig config) {
        if (config.getBigWriterRowAccessWindowSize() <= 0) {
            config.setBigWriterRowAccessWindowSize(1024);
        }
        return config;
    }

    /**
     * Consumes row batches and writes them into the target writer.
     *
     * @param writer        Big writer consuming row batches.
     * @param queue         Shared queue carrying row batches.
     * @param poison        Poison marker batch used to stop the consumer.
     * @param error         Shared error reference for producer-consumer propagation.
     * @param pollTimeoutMs Queue poll timeout in milliseconds.
     */
    private static void consumeBatches(
            final BigExcelWriter writer,
            final BlockingQueue<List<List<Object>>> queue,
            final List<List<Object>> poison,
            final AtomicReference<Throwable> error,
            final long pollTimeoutMs) {
        try {
            while (true) {
                final List<List<Object>> rows = queue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);
                if (null == rows) {
                    final Throwable producerError = error.get();
                    if (null != producerError) {
                        return;
                    }
                    continue;
                }
                if (rows == poison) {
                    return;
                }
                for (final List<Object> row : rows) {
                    writer.writeRow(row);
                }
            }
        } catch (final Throwable e) {
            error.compareAndSet(null, e);
        }
    }

    /**
     * Offers poison batch to queue to stop consumer thread.
     *
     * @param queue         Shared queue carrying row batches.
     * @param poison        Poison marker batch.
     * @param pollTimeoutMs Queue offer timeout in milliseconds.
     */
    private static void offerPoison(
            final BlockingQueue<List<List<Object>>> queue,
            final List<List<Object>> poison,
            final long pollTimeoutMs) {
        boolean offered = false;
        while (!offered) {
            try {
                offered = queue.offer(poison, pollTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while finishing transfer pipeline", e);
            }
        }
    }

    /**
     * Waits for the writer thread and propagates interruptions as runtime errors.
     *
     * @param writerThread Writer thread.
     * @param error        Shared error reference for producer-consumer propagation.
     */
    private static void joinWriterThread(final Thread writerThread, final AtomicReference<Throwable> error) {
        try {
            writerThread.join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            error.compareAndSet(null, e);
            throw new IllegalStateException("Interrupted while waiting writer thread", e);
        }
    }

    /**
     * Resolves read mode.
     *
     * @param config      Read config.
     * @param file        Source file, optional.
     * @param inputStream Source stream, optional.
     * @return Resolved read mode.
     */
    private static ReadMode resolveReadMode(
            final ExcelReadConfig config,
            final File file,
            final InputStream inputStream) {
        if (ReadMode.AUTO != config.getReadMode()) {
            return config.getReadMode();
        }

        if (null == file && null != inputStream) {
            return ReadMode.STREAMING;
        }

        if (config.getBatchSize() > 0 || config.getStartRow() > 0 || config.getEndRow() < Long.MAX_VALUE
                || hasIncludeColumns(config.getIncludeColumns())) {
            return ReadMode.STREAMING;
        }

        if (null != file && file.exists() && file.length() >= AUTO_STREAM_FILE_SIZE_THRESHOLD) {
            return ReadMode.STREAMING;
        }

        return ReadMode.MEMORY;
    }

}
