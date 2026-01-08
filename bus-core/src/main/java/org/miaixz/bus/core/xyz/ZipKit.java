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
package org.miaixz.bus.core.xyz;

import java.io.*;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.zip.*;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.io.compress.*;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.io.stream.LimitedInputStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Compression utility class.
 *
 * @author Kimi Liu
 * @see ZipWriter
 * @since Java 17+
 */
public class ZipKit {

    /**
     * Default charset, using the platform's default charset.
     */
    private static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.defaultCharset();

    /**
     * Zips a source file or directory into a zip file in the same directory, using UTF-8 encoding.
     *
     * @param srcPath The path of the source file.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final String srcPath) throws InternalException {
        return zip(srcPath, DEFAULT_CHARSET);
    }

    /**
     * Zips a source file or directory into a zip file in the same directory.
     *
     * @param srcPath The path of the source file.
     * @param charset The charset to use.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final String srcPath, final java.nio.charset.Charset charset) throws InternalException {
        return zip(FileKit.file(srcPath), charset);
    }

    /**
     * Zips a source file or directory into a zip file in the same directory, using UTF-8 encoding.
     *
     * @param srcFile The source file or directory.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final File srcFile) throws InternalException {
        return zip(srcFile, DEFAULT_CHARSET);
    }

    /**
     * Zips a source file or directory into a zip file in the same directory.
     *
     * @param srcFile The source file or directory.
     * @param charset The charset to use.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final File srcFile, final java.nio.charset.Charset charset) throws InternalException {
        final File zipFile = FileKit.file(srcFile.getParentFile(), FileName.mainName(srcFile) + ".zip");
        zip(zipFile, charset, false, srcFile);
        return zipFile;
    }

    /**
     * Zips a file or directory.
     *
     * @param srcPath The source file path.
     * @param zipPath The destination zip file path.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final String srcPath, final String zipPath) throws InternalException {
        return zip(srcPath, zipPath, false);
    }

    /**
     * Zips a file or directory.
     *
     * @param srcPath    The source file path.
     * @param zipPath    The destination zip file path.
     * @param withSrcDir Whether to include the source directory itself in the zip.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final String srcPath, final String zipPath, final boolean withSrcDir)
            throws InternalException {
        return zip(srcPath, zipPath, DEFAULT_CHARSET, withSrcDir);
    }

    /**
     * Zips a file or directory.
     *
     * @param srcPath    The source file path.
     * @param zipPath    The destination zip file path.
     * @param charset    The charset to use.
     * @param withSrcDir Whether to include the source directory.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(
            final String srcPath,
            final String zipPath,
            final java.nio.charset.Charset charset,
            final boolean withSrcDir) throws InternalException {
        final File srcFile = FileKit.file(srcPath);
        final File zipFile = FileKit.file(zipPath);
        zip(zipFile, charset, withSrcDir, srcFile);
        return zipFile;
    }

    /**
     * Zips files or directories using UTF-8 encoding.
     *
     * @param zipFile    The destination zip file.
     * @param withSrcDir Whether to include the source directory.
     * @param srcFiles   The source files or directories to zip.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final File zipFile, final boolean withSrcDir, final File... srcFiles)
            throws InternalException {
        return zip(zipFile, DEFAULT_CHARSET, withSrcDir, srcFiles);
    }

    /**
     * Zips files or directories.
     *
     * @param zipFile    The destination zip file.
     * @param charset    The charset to use.
     * @param withSrcDir Whether to include the source directory.
     * @param srcFiles   The source files or directories to zip.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(
            final File zipFile,
            final java.nio.charset.Charset charset,
            final boolean withSrcDir,
            final File... srcFiles) throws InternalException {
        return zip(zipFile, charset, withSrcDir, null, srcFiles);
    }

    /**
     * Zips files or directories.
     *
     * @param zipFile    The destination zip file.
     * @param charset    The charset to use.
     * @param withSrcDir Whether to include the source directory.
     * @param filter     A file filter to exclude files or directories.
     * @param srcFiles   The source files or directories to zip.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(
            final File zipFile,
            final java.nio.charset.Charset charset,
            final boolean withSrcDir,
            final FileFilter filter,
            final File... srcFiles) throws InternalException {
        validateFiles(zipFile, srcFiles);
        try (final ZipWriter zipWriter = ZipWriter.of(zipFile, charset)) {
            zipWriter.add(withSrcDir, filter, srcFiles);
        }
        return zipFile;
    }

    /**
     * Zips files or directories to an output stream.
     *
     * @param out        The target output stream.
     * @param charset    The charset to use.
     * @param withSrcDir Whether to include the source directory.
     * @param filter     A file filter.
     * @param srcFiles   The source files or directories.
     * @throws InternalException for IO errors.
     */
    public static void zip(
            final OutputStream out,
            final java.nio.charset.Charset charset,
            final boolean withSrcDir,
            final FileFilter filter,
            final File... srcFiles) throws InternalException {
        try (final ZipWriter zipWriter = ZipWriter.of(out, charset)) {
            zipWriter.add(withSrcDir, filter, srcFiles);
        }
    }

    /**
     * Adds string data to a zip file using UTF-8 encoding.
     *
     * @param zipFile The destination zip file.
     * @param path    The path of the entry in the zip file.
     * @param data    The string data to compress.
     * @return The modified zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final File zipFile, final String path, final String data) throws InternalException {
        return zip(zipFile, path, data, DEFAULT_CHARSET);
    }

    /**
     * Adds string data to a zip file.
     *
     * @param zipFile The destination zip file.
     * @param path    The path of the entry in the zip file.
     * @param data    The string data to compress.
     * @param charset The charset to use.
     * @return The modified zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(
            final File zipFile,
            final String path,
            final String data,
            final java.nio.charset.Charset charset) throws InternalException {
        return zip(zipFile, path, IoKit.toStream(data, charset), charset);
    }

    /**
     * Adds data from an input stream to a zip file using UTF-8 encoding.
     *
     * @param zipFile The destination zip file.
     * @param path    The path of the entry in the zip file.
     * @param in      The input stream.
     * @return The modified zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final File zipFile, final String path, final InputStream in) throws InternalException {
        return zip(zipFile, path, in, DEFAULT_CHARSET);
    }

    /**
     * Adds data from an input stream to a zip file.
     *
     * @param zipFile The destination zip file.
     * @param path    The path of the entry in the zip file.
     * @param in      The input stream (will be closed).
     * @param charset The charset to use.
     * @return The modified zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(
            final File zipFile,
            final String path,
            final InputStream in,
            final java.nio.charset.Charset charset) throws InternalException {
        return zip(zipFile, new String[] { path }, new InputStream[] { in }, charset);
    }

    /**
     * Adds multiple streams to a zip file.
     *
     * @param zipFile The destination zip file.
     * @param paths   The entry paths.
     * @param ins     The input streams.
     * @return The modified zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final File zipFile, final String[] paths, final InputStream[] ins) throws InternalException {
        return zip(zipFile, paths, ins, DEFAULT_CHARSET);
    }

    /**
     * Adds multiple streams to a zip file.
     *
     * @param zipFile The destination zip file.
     * @param paths   The entry paths.
     * @param ins     The input streams (will be closed).
     * @param charset The charset to use.
     * @return The modified zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(
            final File zipFile,
            final String[] paths,
            final InputStream[] ins,
            final java.nio.charset.Charset charset) throws InternalException {
        try (final ZipWriter zipWriter = ZipWriter.of(zipFile, charset)) {
            zipWriter.add(paths, ins);
        }
        return zipFile;
    }

    /**
     * Zips multiple streams to a target output stream.
     *
     * @param out   The target output stream (will be closed).
     * @param paths The entry paths.
     * @param ins   The input streams (will be closed).
     */
    public static void zip(final OutputStream out, final String[] paths, final InputStream[] ins) {
        zip(getZipOutputStream(out, DEFAULT_CHARSET), paths, ins);
    }

    /**
     * Zips multiple streams to a target zip output stream.
     *
     * @param zipOutputStream The target zip output stream (will be closed).
     * @param paths           The entry paths.
     * @param ins             The input streams (will be closed).
     * @throws InternalException for IO errors.
     */
    public static void zip(final ZipOutputStream zipOutputStream, final String[] paths, final InputStream[] ins)
            throws InternalException {
        try (final ZipWriter zipWriter = new ZipWriter(zipOutputStream)) {
            zipWriter.add(paths, ins);
        }
    }

    /**
     * Zips multiple `Resource` objects into a zip file.
     *
     * @param zipFile   The destination zip file.
     * @param charset   The charset to use.
     * @param resources The resources to compress.
     * @return The created zip file.
     * @throws InternalException for IO errors.
     */
    public static File zip(final File zipFile, final java.nio.charset.Charset charset, final Resource... resources)
            throws InternalException {
        try (final ZipWriter zipWriter = ZipWriter.of(zipFile, charset)) {
            zipWriter.add(resources);
        }
        return zipFile;
    }

    /**
     * Unzips a file to a directory of the same name, using UTF-8 encoding.
     *
     * @param zipFilePath The path of the zip file.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final String zipFilePath) throws InternalException {
        return unzip(zipFilePath, DEFAULT_CHARSET);
    }

    /**
     * Unzips a file to a directory of the same name.
     *
     * @param zipFilePath The path of the zip file.
     * @param charset     The charset to use.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final String zipFilePath, final java.nio.charset.Charset charset)
            throws InternalException {
        return unzip(FileKit.file(zipFilePath), charset);
    }

    /**
     * Unzips a file to a directory of the same name, using UTF-8 encoding.
     *
     * @param zipFile The zip file.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final File zipFile) throws InternalException {
        return unzip(zipFile, DEFAULT_CHARSET);
    }

    /**
     * Unzips a file to a directory of the same name.
     *
     * @param zipFile The zip file.
     * @param charset The charset to use.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final File zipFile, final java.nio.charset.Charset charset) throws InternalException {
        final File destDir = FileKit.file(zipFile.getParentFile(), FileName.mainName(zipFile));
        return unzip(zipFile, destDir, charset);
    }

    /**
     * Unzips a file to a specified directory, using UTF-8 encoding.
     *
     * @param zipFilePath The path of the zip file.
     * @param outFileDir  The destination directory.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final String zipFilePath, final String outFileDir) throws InternalException {
        return unzip(zipFilePath, outFileDir, DEFAULT_CHARSET);
    }

    /**
     * Unzips a file to a specified directory.
     *
     * @param zipFilePath The path of the zip file.
     * @param outFileDir  The destination directory.
     * @param charset     The charset to use.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final String zipFilePath, final String outFileDir, final java.nio.charset.Charset charset)
            throws InternalException {
        return unzip(FileKit.file(zipFilePath), FileKit.mkdir(outFileDir), charset);
    }

    /**
     * Unzips a file to a specified directory, using UTF-8 encoding.
     *
     * @param zipFile The zip file.
     * @param outFile The destination directory.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final File zipFile, final File outFile) throws InternalException {
        return unzip(zipFile, outFile, DEFAULT_CHARSET);
    }

    /**
     * Unzips a file to a specified directory.
     *
     * @param zipFile The zip file.
     * @param outFile The destination directory.
     * @param charset The charset to use.
     * @return The destination directory.
     */
    public static File unzip(final File zipFile, final File outFile, final java.nio.charset.Charset charset) {
        return unzip(toZipFile(zipFile, charset), outFile);
    }

    /**
     * Unzips a `ZipFile` to a specified directory.
     *
     * @param zipFile The `ZipFile` (will be closed).
     * @param outFile The destination directory.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final ZipFile zipFile, final File outFile) throws InternalException {
        return unzip(zipFile, outFile, -1);
    }

    /**
     * Unzips a `ZipFile` to a specified directory with a size limit to prevent zip bomb attacks.
     *
     * @param zipFile The `ZipFile` (will be closed).
     * @param outFile The destination directory.
     * @param limit   The maximum allowed size of uncompressed data in bytes.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final ZipFile zipFile, final File outFile, final long limit) throws InternalException {
        if (outFile.exists() && outFile.isFile()) {
            throw new IllegalArgumentException(
                    StringKit.format("Target path [{}] exists and is a file!", outFile.getAbsolutePath()));
        }
        if (limit > 0) {
            final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            long zipFileSize = 0L;
            ZipEntry zipEntry;
            while (zipEntries.hasMoreElements()) {
                zipEntry = zipEntries.nextElement();
                zipFileSize += zipEntry.getSize();
                if (zipFileSize > limit) {
                    throw new IllegalArgumentException("The file size exceeds the limit");
                }
            }
        }
        try (final ZipReader reader = new ZipReader(zipFile)) {
            reader.readTo(outFile);
        }
        return outFile;
    }

    /**
     * Unzips a stream to a specified directory.
     *
     * @param in      The zip input stream (will be closed).
     * @param outFile The destination directory.
     * @param charset The charset to use.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final InputStream in, final File outFile, java.nio.charset.Charset charset)
            throws InternalException {
        if (null == charset) {
            charset = DEFAULT_CHARSET;
        }
        return unzip(new ZipInputStream(in, charset), outFile);
    }

    /**
     * Unzips a `ZipInputStream` to a specified directory.
     *
     * @param zipStream The zip input stream (will be closed).
     * @param outFile   The destination directory.
     * @return The destination directory.
     * @throws InternalException for IO errors.
     */
    public static File unzip(final ZipInputStream zipStream, final File outFile) throws InternalException {
        try (final ZipReader reader = new ZipReader(zipStream)) {
            reader.readTo(outFile);
        }
        return outFile;
    }

    /**
     * Extracts a specific file from a zip archive as a byte array.
     *
     * @param zipFilePath The path to the zip file.
     * @param name        The name of the entry to extract.
     * @return The content of the file as a byte array.
     */
    public static byte[] unzipFileBytes(final String zipFilePath, final String name) {
        return unzipFileBytes(zipFilePath, DEFAULT_CHARSET, name);
    }

    /**
     * Extracts a specific file from a zip archive as a byte array.
     *
     * @param zipFilePath The path to the zip file.
     * @param charset     The charset to use.
     * @param name        The name of the entry to extract.
     * @return The content of the file as a byte array.
     */
    public static byte[] unzipFileBytes(
            final String zipFilePath,
            final java.nio.charset.Charset charset,
            final String name) {
        return unzipFileBytes(FileKit.file(zipFilePath), charset, name);
    }

    /**
     * Extracts a specific file from a zip archive as a byte array.
     *
     * @param zipFile The zip file.
     * @param name    The name of the entry to extract.
     * @return The content of the file as a byte array.
     */
    public static byte[] unzipFileBytes(final File zipFile, final String name) {
        return unzipFileBytes(zipFile, DEFAULT_CHARSET, name);
    }

    /**
     * Extracts a specific file from a zip archive as a byte array.
     *
     * @param zipFile The zip file.
     * @param charset The charset to use.
     * @param name    The name of the entry to extract.
     * @return The content of the file as a byte array.
     */
    public static byte[] unzipFileBytes(final File zipFile, final java.nio.charset.Charset charset, final String name) {
        try (final ZipReader reader = ZipReader.of(zipFile, charset)) {
            return IoKit.readBytes(reader.get(name));
        }
    }

    /**
     * Gzips a string.
     *
     * @param content The string to compress.
     * @param charset The charset to use.
     * @return The compressed byte array.
     * @throws InternalException for IO errors.
     */
    public static byte[] gzip(final String content, final java.nio.charset.Charset charset) throws InternalException {
        return gzip(ByteKit.toBytes(content, charset));
    }

    /**
     * Gzips a byte array.
     *
     * @param buf The byte array to compress.
     * @return The compressed byte array.
     * @throws InternalException for IO errors.
     */
    public static byte[] gzip(final byte[] buf) throws InternalException {
        return gzip(new ByteArrayInputStream(buf), buf.length);
    }

    /**
     * Gzips a file.
     *
     * @param file The file to compress.
     * @return The compressed byte array.
     * @throws InternalException for IO errors.
     */
    public static byte[] gzip(final File file) throws InternalException {
        BufferedInputStream in = null;
        try {
            in = FileKit.getInputStream(file);
            return gzip(in, (int) file.length());
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * Gzips an input stream.
     *
     * @param in The input stream.
     * @return The compressed byte array.
     * @throws InternalException for IO errors.
     */
    public static byte[] gzip(final InputStream in) throws InternalException {
        return gzip(in, Normal._32);
    }

    /**
     * Gzips an input stream.
     *
     * @param in     The input stream.
     * @param length The estimated length.
     * @return The compressed byte array.
     * @throws InternalException for IO errors.
     */
    public static byte[] gzip(final InputStream in, final int length) throws InternalException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
        Gzip.of(in, bos).gzip().close();
        return bos.toByteArray();
    }

    /**
     * Un-gzips a byte array into a string.
     *
     * @param buf     The compressed byte array.
     * @param charset The charset.
     * @return The decompressed string.
     * @throws InternalException for IO errors.
     */
    public static String unGzip(final byte[] buf, final java.nio.charset.Charset charset) throws InternalException {
        return StringKit.toString(unGzip(buf), charset);
    }

    /**
     * Un-gzips a byte array.
     *
     * @param buf The compressed byte array.
     * @return The decompressed byte array.
     * @throws InternalException for IO errors.
     */
    public static byte[] unGzip(final byte[] buf) throws InternalException {
        return unGzip(new ByteArrayInputStream(buf), buf.length);
    }

    /**
     * Un-gzips an input stream.
     *
     * @param in The gzipped input stream.
     * @return The decompressed byte array.
     * @throws InternalException for IO errors.
     */
    public static byte[] unGzip(final InputStream in) throws InternalException {
        return unGzip(in, Normal._32);
    }

    /**
     * Un-gzips an input stream.
     *
     * @param in     The gzipped input stream.
     * @param length The estimated length.
     * @return The decompressed byte array.
     * @throws InternalException for IO errors.
     */
    public static byte[] unGzip(final InputStream in, final int length) throws InternalException {
        final FastByteArrayOutputStream bos = new FastByteArrayOutputStream(length);
        Gzip.of(in, bos).unGzip().close();
        return bos.toByteArray();
    }

    /**
     * Zlib compresses a string.
     *
     * @param content The string to compress.
     * @param charset The charset.
     * @param level   The compression level (1-9).
     * @return The compressed byte array.
     */
    public static byte[] zlib(final String content, final java.nio.charset.Charset charset, final int level) {
        return zlib(ByteKit.toBytes(content, charset), level);
    }

    /**
     * Zlib compresses a file.
     *
     * @param file  The file to compress.
     * @param level The compression level.
     * @return The compressed byte array.
     */
    public static byte[] zlib(final File file, final int level) {
        BufferedInputStream in = null;
        try {
            in = FileKit.getInputStream(file);
            return zlib(in, level, (int) file.length());
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * Zlib compresses a byte array.
     *
     * @param buf   The data.
     * @param level The compression level (0-9).
     * @return The compressed byte array.
     */
    public static byte[] zlib(final byte[] buf, final int level) {
        return zlib(new ByteArrayInputStream(buf), level, buf.length);
    }

    /**
     * Zlib compresses an input stream.
     *
     * @param in    The input stream.
     * @param level The compression level (0-9).
     * @return The compressed byte array.
     */
    public static byte[] zlib(final InputStream in, final int level) {
        return zlib(in, level, Normal._32);
    }

    /**
     * Zlib compresses an input stream.
     *
     * @param in     The input stream.
     * @param level  The compression level (0-9).
     * @param length The estimated length.
     * @return The compressed byte array.
     */
    public static byte[] zlib(final InputStream in, final int level, final int length) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(length);
        Deflate.of(in, out, false).deflater(level).close();
        return out.toByteArray();
    }

    /**
     * Un-zlibs a byte array into a string.
     *
     * @param buf     The compressed byte array.
     * @param charset The charset.
     * @return The decompressed string.
     */
    public static String unZlib(final byte[] buf, final java.nio.charset.Charset charset) {
        return StringKit.toString(unZlib(buf), charset);
    }

    /**
     * Un-zlibs a byte array.
     *
     * @param buf The data.
     * @return The decompressed byte array.
     */
    public static byte[] unZlib(final byte[] buf) {
        return unZlib(new ByteArrayInputStream(buf), buf.length);
    }

    /**
     * Un-zlibs an input stream.
     *
     * @param in The input stream.
     * @return The decompressed byte array.
     */
    public static byte[] unZlib(final InputStream in) {
        return unZlib(in, Normal._32);
    }

    /**
     * Un-zlibs an input stream.
     *
     * @param in     The input stream.
     * @param length The estimated length.
     * @return The decompressed byte array.
     */
    public static byte[] unZlib(final InputStream in, final int length) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(length);
        Deflate.of(in, out, false).inflater();
        return out.toByteArray();
    }

    /**
     * Gets an input stream for a specific entry in a zip file.
     *
     * @param zipFile The zip file.
     * @param charset The charset.
     * @param path    The path of the entry.
     * @return The `InputStream` for the entry, or `null` if not found.
     */
    public static InputStream get(final File zipFile, final java.nio.charset.Charset charset, final String path) {
        return get(toZipFile(zipFile, charset), path);
    }

    /**
     * Gets an input stream for a specific entry in a zip file.
     *
     * @param zipFile The `ZipFile`.
     * @param path    The path of the entry.
     * @return The `InputStream` for the entry, or `null` if not found.
     */
    public static InputStream get(final ZipFile zipFile, final String path) {
        final ZipEntry entry = zipFile.getEntry(path);
        if (null != entry) {
            return getStream(zipFile, entry);
        }
        return null;
    }

    /**
     * Reads and processes each `ZipEntry` in a `ZipFile`.
     *
     * @param zipFile  The `ZipFile`.
     * @param consumer The consumer for each `ZipEntry`.
     */
    public static void read(final ZipFile zipFile, final Consumer<ZipEntry> consumer) {
        try (final ZipReader reader = new ZipReader(zipFile)) {
            reader.read(consumer);
        }
    }

    /**
     * Reads and processes each `ZipEntry` in a `ZipInputStream`.
     *
     * @param zipStream The `ZipInputStream`.
     * @param consumer  The consumer for each `ZipEntry`.
     */
    public static void read(final ZipInputStream zipStream, final Consumer<ZipEntry> consumer) {
        try (final ZipReader reader = new ZipReader(zipStream)) {
            reader.read(consumer);
        }
    }

    /**
     * Appends a new file or directory to an existing zip file.
     *
     * @param zipPath        The path to the zip file.
     * @param appendFilePath The path of the file or directory to append.
     * @param options        Copy options.
     * @throws InternalException for IO errors.
     */
    public static void append(final Path zipPath, final Path appendFilePath, final CopyOption... options)
            throws InternalException {
        try (final FileSystem zipFileSystem = FileKit.createZip(zipPath.toString())) {
            if (Files.isDirectory(appendFilePath)) {
                Path source = appendFilePath.getParent();
                if (null == source) {
                    source = appendFilePath;
                }
                Files.walkFileTree(appendFilePath, new ZipCopyVisitor(source, zipFileSystem, options));
            } else {
                Files.copy(appendFilePath, zipFileSystem.getPath(PathResolve.getName(appendFilePath)), options);
            }
        } catch (final FileAlreadyExistsException ignored) {
            // ignore if file already exists and not overwriting
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts a `File` to a `ZipFile`, handling potential encoding issues.
     *
     * @param file    The zip file.
     * @param charset The charset to use for entry names.
     * @return A `ZipFile`.
     */
    public static ZipFile toZipFile(final File file, java.nio.charset.Charset charset) {
        if (null == charset) {
            charset = Charset.UTF_8;
        }
        try {
            return new ZipFile(file, charset);
        } catch (final IOException e) {
            if (e instanceof ZipException && e.getMessage().contains("invalid CEN header")) {
                try {
                    // Try with a different encoding
                    return new ZipFile(file, Charset.UTF_8.equals(charset) ? Charset.GBK : Charset.UTF_8);
                } catch (final IOException ex) {
                    throw new InternalException(ex);
                }
            }
            throw new InternalException(e);
        }
    }

    /**
     * Gets the `InputStream` for a specific `ZipEntry`.
     *
     * @param zipFile  The `ZipFile`.
     * @param zipEntry The `ZipEntry`.
     * @return The `InputStream`.
     */
    public static InputStream getStream(final ZipFile zipFile, final ZipEntry zipEntry) {
        try {
            return new LimitedInputStream(zipFile.getInputStream(zipEntry), zipEntry.getSize());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets a `ZipOutputStream`.
     *
     * @param out     The target output stream.
     * @param charset The charset.
     * @return A `ZipOutputStream`.
     */
    public static ZipOutputStream getZipOutputStream(final OutputStream out, final java.nio.charset.Charset charset) {
        if (out instanceof ZipOutputStream) {
            return (ZipOutputStream) out;
        }
        return new ZipOutputStream(out, charset);
    }

    /**
     * Lists all file names in a specified directory within a zip file.
     *
     * @param zipFile The `ZipFile` (will not be closed).
     * @param dir     The directory prefix.
     * @return A list of file names.
     */
    public static List<String> listFileNames(final ZipFile zipFile, String dir) {
        if (StringKit.isNotBlank(dir)) {
            dir = StringKit.addSuffixIfNot(dir, Symbol.SLASH);
        }

        final List<String> fileNames = new ArrayList<>();
        String name;
        for (final ZipEntry entry : new EnumerationIterator<>(zipFile.entries())) {
            name = entry.getName();
            if (StringKit.isEmpty(dir) || name.startsWith(dir)) {
                final String nameSuffix = StringKit.removePrefix(name, dir);
                if (StringKit.isNotEmpty(nameSuffix) && !StringKit.contains(nameSuffix, Symbol.C_SLASH)) {
                    fileNames.add(nameSuffix);
                }
            }
        }
        return fileNames;
    }

    /**
     * Gets a `JarFile` from a JAR file URL.
     *
     * @param jarFileUrl The JAR file URL.
     * @return A `JarFile`.
     * @throws InternalException for IO errors.
     */
    public static JarFile ofJar(String jarFileUrl) throws InternalException {
        Assert.notBlank(jarFileUrl, "Jar file url is blank!");

        if (jarFileUrl.startsWith(Normal.FILE_URL_PREFIX)) {
            try {
                jarFileUrl = UrlKit.toURI(jarFileUrl).getSchemeSpecificPart();
            } catch (final InternalException e) {
                jarFileUrl = jarFileUrl.substring(Normal.FILE_URL_PREFIX.length());
            }
        }
        try {
            return new JarFile(jarFileUrl);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Validates that the destination zip file is not a sub-directory of the source files.
     *
     * @param zipFile  The destination zip file.
     * @param srcFiles The source files.
     */
    private static void validateFiles(final File zipFile, final File... srcFiles) throws InternalException {
        if (zipFile.isDirectory()) {
            throw new InternalException("Zip file [{}] must not be a directory !", zipFile.getAbsoluteFile());
        }

        for (final File srcFile : srcFiles) {
            if (null == srcFile) {
                continue;
            }
            if (!srcFile.exists()) {
                throw new InternalException(StringKit.format("File [{}] not exist!", srcFile.getAbsolutePath()));
            }

            File parentFile;
            try {
                parentFile = zipFile.getCanonicalFile().getParentFile();
            } catch (final IOException e) {
                parentFile = zipFile.getParentFile();
            }

            if (srcFile.isDirectory() && FileKit.isSub(srcFile, parentFile)) {
                throw new InternalException("Zip file path [{}] must not be the child directory of [{}] !",
                        zipFile.getPath(), srcFile.getPath());
            }
        }
    }

}
