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
package org.miaixz.bus.extra.compress;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.StreamingNotSupportedException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.compress.archiver.Archiver;
import org.miaixz.bus.extra.compress.archiver.SevenZArchiver;
import org.miaixz.bus.extra.compress.archiver.StreamArchiver;
import org.miaixz.bus.extra.compress.extractor.Extractor;
import org.miaixz.bus.extra.compress.extractor.SevenZExtractor;
import org.miaixz.bus.extra.compress.extractor.StreamExtractor;

/**
 * Compression utility class. A wrapper for compression and decompression based on commons-compress.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompressBuilder {

    /**
     * Gets a compression output stream for compressing specified content. Supported formats include:
     * <ul>
     * <li>{@value CompressorStreamFactory#GZIP}</li>
     * <li>{@value CompressorStreamFactory#BZIP2}</li>
     * <li>{@value CompressorStreamFactory#XZ}</li>
     * <li>{@value CompressorStreamFactory#PACK200}</li>
     * <li>{@value CompressorStreamFactory#SNAPPY_FRAMED}</li>
     * <li>{@value CompressorStreamFactory#LZ4_BLOCK}</li>
     * <li>{@value CompressorStreamFactory#LZ4_FRAMED}</li>
     * <li>{@value CompressorStreamFactory#ZSTANDARD}</li>
     * <li>{@value CompressorStreamFactory#DEFLATE}</li>
     * </ul>
     *
     * @param compressorName The name of the compressor, see: {@link CompressorStreamFactory}.
     * @param out            The output stream, which can be to memory, a network, or a file.
     * @return A {@link CompressorOutputStream}.
     */
    public static CompressorOutputStream getOut(final String compressorName, final OutputStream out) {
        try {
            return new CompressorStreamFactory().createCompressorOutputStream(compressorName, out);
        } catch (final CompressorException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets a compression input stream for decompressing specified content. Supported formats include:
     * <ul>
     * <li>{@value CompressorStreamFactory#GZIP}</li>
     * <li>{@value CompressorStreamFactory#BZIP2}</li>
     * <li>{@value CompressorStreamFactory#XZ}</li>
     * <li>{@value CompressorStreamFactory#PACK200}</li>
     * <li>{@value CompressorStreamFactory#SNAPPY_FRAMED}</li>
     * <li>{@value CompressorStreamFactory#LZ4_BLOCK}</li>
     * <li>{@value CompressorStreamFactory#LZ4_FRAMED}</li>
     * <li>{@value CompressorStreamFactory#ZSTANDARD}</li>
     * <li>{@value CompressorStreamFactory#DEFLATE}</li>
     * </ul>
     *
     * @param compressorName The name of the compressor, see: {@link CompressorStreamFactory}. null means auto-detect.
     * @param in             The input stream.
     * @return A {@link CompressorInputStream}.
     */
    public static CompressorInputStream getIn(String compressorName, InputStream in) {
        in = IoKit.toMarkSupport(in);
        try {
            if (StringKit.isBlank(compressorName)) {
                compressorName = CompressorStreamFactory.detect(in);
            }
            return new CompressorStreamFactory().createCompressorInputStream(compressorName, in);
        } catch (final CompressorException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates an archiver. Supported formats:
     * <ul>
     * <li>{@link ArchiveStreamFactory#AR}</li>
     * <li>{@link ArchiveStreamFactory#CPIO}</li>
     * <li>{@link ArchiveStreamFactory#JAR}</li>
     * <li>{@link ArchiveStreamFactory#TAR}</li>
     * <li>{@link ArchiveStreamFactory#ZIP}</li>
     * <li>{@link ArchiveStreamFactory#SEVEN_Z}</li>
     * </ul>
     *
     * @param charset      The character encoding.
     * @param archiverName The name of the archiver format, see {@link ArchiveStreamFactory}.
     * @param file         The output archive file.
     * @return An Archiver instance.
     */
    public static Archiver createArchiver(final Charset charset, final String archiverName, final File file) {
        if (ArchiveStreamFactory.SEVEN_Z.equalsIgnoreCase(archiverName)) {
            return new SevenZArchiver(file);
        }
        return StreamArchiver.of(charset, archiverName, file);
    }

    /**
     * Creates an archiver. Supported formats:
     * <ul>
     * <li>{@link ArchiveStreamFactory#AR}</li>
     * <li>{@link ArchiveStreamFactory#CPIO}</li>
     * <li>{@link ArchiveStreamFactory#JAR}</li>
     * <li>{@link ArchiveStreamFactory#TAR}</li>
     * <li>{@link ArchiveStreamFactory#ZIP}</li>
     * <li>{@link ArchiveStreamFactory#SEVEN_Z}</li>
     * </ul>
     *
     * @param charset      The character encoding.
     * @param archiverName The name of the archiver format, see {@link ArchiveStreamFactory}.
     * @param out          The output stream for the archive.
     * @return An Archiver instance.
     */
    public static Archiver createArchiver(final Charset charset, final String archiverName, final OutputStream out) {
        if (ArchiveStreamFactory.SEVEN_Z.equalsIgnoreCase(archiverName)) {
            return new SevenZArchiver(out);
        }
        return StreamArchiver.of(charset, archiverName, out);
    }

    /**
     * Creates an archive extractor. Supported formats:
     * <ul>
     * <li>{@link ArchiveStreamFactory#AR}</li>
     * <li>{@link ArchiveStreamFactory#CPIO}</li>
     * <li>{@link ArchiveStreamFactory#JAR}</li>
     * <li>{@link ArchiveStreamFactory#TAR}</li>
     * <li>{@link ArchiveStreamFactory#ZIP}</li>
     * <li>{@link ArchiveStreamFactory#SEVEN_Z}</li>
     * </ul>
     *
     * @param charset The character encoding. This parameter is ignored for the 7z format.
     * @param file    The archive file.
     * @return An {@link Extractor}.
     */
    public static Extractor createExtractor(final Charset charset, final File file) {
        return createExtractor(charset, null, file);
    }

    /**
     * Creates an archive extractor. Supported formats:
     * <ul>
     * <li>{@link ArchiveStreamFactory#AR}</li>
     * <li>{@link ArchiveStreamFactory#CPIO}</li>
     * <li>{@link ArchiveStreamFactory#JAR}</li>
     * <li>{@link ArchiveStreamFactory#TAR}</li>
     * <li>{@link ArchiveStreamFactory#ZIP}</li>
     * <li>{@link ArchiveStreamFactory#SEVEN_Z}</li>
     * </ul>
     *
     * @param charset      The character encoding. This parameter is ignored for the 7z format.
     * @param archiverName The name of the archiver format, see {@link ArchiveStreamFactory}. null means auto-detect.
     * @param file         The archive file.
     * @return An {@link Extractor}.
     */
    public static Extractor createExtractor(final Charset charset, String archiverName, final File file) {
        if (ArchiveStreamFactory.SEVEN_Z.equalsIgnoreCase(archiverName)) {
            return new SevenZExtractor(file);
        }

        if (StringKit.isBlank(archiverName)) {
            final String name = file.getName().toLowerCase();
            if (name.endsWith(".tgz")) {
                archiverName = "tgz";
            } else if (name.endsWith(".tar.gz")) {
                archiverName = "tar.gz";
            }
        }
        try {
            return new StreamExtractor(charset, archiverName, file);
        } catch (final InternalException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof StreamingNotSupportedException && cause.getMessage().contains("7z")) {
                return new SevenZExtractor(file);
            }
            throw e;
        }
    }

    /**
     * Creates an archive extractor. Supported formats:
     * <ul>
     * <li>{@link ArchiveStreamFactory#AR}</li>
     * <li>{@link ArchiveStreamFactory#CPIO}</li>
     * <li>{@link ArchiveStreamFactory#JAR}</li>
     * <li>{@link ArchiveStreamFactory#TAR}</li>
     * <li>{@link ArchiveStreamFactory#ZIP}</li>
     * <li>{@link ArchiveStreamFactory#SEVEN_Z}</li>
     * </ul>
     *
     * @param charset The character encoding. This parameter is ignored for the 7z format.
     * @param in      The input stream for the archive.
     * @return An {@link Extractor}.
     */
    public static Extractor createExtractor(final Charset charset, final InputStream in) {
        return createExtractor(charset, null, in);
    }

    /**
     * Creates an archive extractor. Supported formats:
     * <ul>
     * <li>{@link ArchiveStreamFactory#AR}</li>
     * <li>{@link ArchiveStreamFactory#CPIO}</li>
     * <li>{@link ArchiveStreamFactory#JAR}</li>
     * <li>{@link ArchiveStreamFactory#TAR}</li>
     * <li>{@link ArchiveStreamFactory#ZIP}</li>
     * <li>{@link ArchiveStreamFactory#SEVEN_Z}</li>
     * </ul>
     *
     * @param charset      The character encoding. This parameter is ignored for the 7z format.
     * @param archiverName The name of the archiver format, see {@link ArchiveStreamFactory}. null means auto-detect.
     * @param in           The input stream for the archive.
     * @return An {@link Extractor}.
     */
    public static Extractor createExtractor(final Charset charset, final String archiverName, final InputStream in) {
        if (ArchiveStreamFactory.SEVEN_Z.equalsIgnoreCase(archiverName)) {
            return new SevenZExtractor(in);
        }

        try {
            return new StreamExtractor(charset, archiverName, in);
        } catch (final InternalException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof StreamingNotSupportedException && cause.getMessage().contains("7z")) {
                return new SevenZExtractor(in);
            }
            throw e;
        }
    }

    /**
     * Gets the archive entry name.
     *
     * @param fileName       The file name, including the main name and extension, but not the path.
     * @param path           The path.
     * @param fileNameEditor The file name editor.
     * @return The archive entry name.
     */
    public static String getEntryName(
            final String fileName,
            final String path,
            final Function<String, String> fileNameEditor) {
        String entryName = (fileNameEditor == null) ? fileName : fileNameEditor.apply(fileName);
        if (StringKit.isNotEmpty(path)) {
            // Concatenates the path if not empty, in the format: path/name
            entryName = StringKit.addSuffixIfNot(path, Symbol.SLASH) + entryName;
        }

        return entryName;
    }

}
