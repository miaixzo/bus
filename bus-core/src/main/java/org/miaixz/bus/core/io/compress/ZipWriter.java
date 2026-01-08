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
package org.miaixz.bus.core.io.compress;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

/**
 * Zip file writer encapsulation. This class provides methods to create and write to Zip archives.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipWriter implements Closeable {

    /**
     * The underlying {@link ZipOutputStream}.
     */
    private final ZipOutputStream out;
    /**
     * The Zip file being written to.
     */
    private File zipFile;
    /**
     * Custom buffer size for writing operations.
     */
    private int bufferSize = Normal._8192;

    /**
     * Constructs a new ZipWriter instance.
     *
     * @param zipFile The Zip file to be generated.
     * @param charset The character set to be used for Zip entry names.
     */
    public ZipWriter(final File zipFile, final Charset charset) {
        this(getZipOutputStream(zipFile, charset));
        this.zipFile = zipFile;
    }

    /**
     * Constructs a new ZipWriter instance.
     *
     * @param out     The {@link OutputStream} to write Zip data to.
     * @param charset The character set to be used for Zip entry names.
     */
    public ZipWriter(final OutputStream out, final Charset charset) {
        this(ZipKit.getZipOutputStream(out, charset));
    }

    /**
     * Constructs a new ZipWriter instance.
     *
     * @param out The {@link ZipOutputStream} to write Zip data to.
     */
    public ZipWriter(final ZipOutputStream out) {
        this.out = out;
    }

    /**
     * Creates a new ZipWriter instance.
     *
     * @param zipFile The Zip file to be generated.
     * @param charset The character set to be used for Zip entry names.
     * @return A new ZipWriter instance.
     */
    public static ZipWriter of(final File zipFile, final Charset charset) {
        return new ZipWriter(zipFile, charset);
    }

    /**
     * Creates a new ZipWriter instance.
     *
     * @param out     The Zip output stream, typically a file output stream.
     * @param charset The character set to be used for Zip entry names.
     * @return A new ZipWriter instance.
     */
    public static ZipWriter of(final OutputStream out, final Charset charset) {
        return new ZipWriter(out, charset);
    }

    /**
     * Obtains a {@link ZipOutputStream} for the given Zip file and charset.
     *
     * @param zipFile The Zip file to write to.
     * @param charset The character set for Zip entry names.
     * @return A {@link ZipOutputStream}.
     */
    private static ZipOutputStream getZipOutputStream(final File zipFile, final Charset charset) {
        return ZipKit.getZipOutputStream(FileKit.getOutputStream(zipFile), charset);
    }

    /**
     * Sets the custom buffer size for writing operations. Adjusting this can affect performance under specific
     * conditions.
     *
     * @param bufferSize The buffer size in bytes.
     * @return This ZipWriter instance.
     */
    public ZipWriter setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * Sets the compression level. Optional values are 1 to 9, where -1 indicates the default level.
     *
     * @param level The compression level.
     * @return This ZipWriter instance.
     */
    public ZipWriter setLevel(final int level) {
        this.out.setLevel(level);
        return this;
    }

    /**
     * Sets the comment for the Zip archive.
     *
     * @param comment The comment string.
     * @return This ZipWriter instance.
     */
    public ZipWriter setComment(final String comment) {
        this.out.setComment(comment);
        return this;
    }

    /**
     * Sets the compression method.
     *
     * @param method The compression method, supporting {@link ZipOutputStream#DEFLATED} and
     *               {@link ZipOutputStream#STORED}.
     * @return This ZipWriter instance.
     */
    public ZipWriter setMethod(final int method) {
        this.out.setMethod(method);
        return this;
    }

    /**
     * Retrieves the underlying {@link ZipOutputStream}.
     *
     * @return The {@link ZipOutputStream}.
     */
    public ZipOutputStream getOut() {
        return this.out;
    }

    /**
     * Adds files or directories to the Zip archive.
     *
     * @param withSrcDir Whether to include the source directory itself. This is only effective for compressing
     *                   directories. If {@code false}, only files or subdirectories within the directory are
     *                   compressed. If {@code true}, the directory itself is also compressed.
     * @param filter     A file filter to customize which files (or folders) are excluded from compression. {@code null}
     *                   means no filtering.
     * @param files      The source files or directories to be compressed. If compressing a single file, provide its
     *                   full path; if compressing a directory, provide the path to its top-level directory.
     * @return This ZipWriter instance.
     * @throws InternalException If an I/O error occurs.
     */
    public ZipWriter add(final boolean withSrcDir, final FileFilter filter, final File... files)
            throws InternalException {
        for (final File file : files) {
            // If only compressing a single file, the parent directory needs to be truncated.
            String srcRootDir;
            try {
                srcRootDir = file.getCanonicalPath();
                if ((!file.isDirectory()) || withSrcDir) {
                    // If it's a file, truncate the full parent directory path; if including the directory, truncate all
                    // parent directories, keeping the current directory name.
                    srcRootDir = file.getCanonicalFile().getParentFile().getCanonicalPath();
                }
            } catch (final IOException e) {
                throw new InternalException(e);
            }

            _add(file, srcRootDir, filter);
        }
        return this;
    }

    /**
     * Adds resources to the Zip archive. The resource streams are closed after adding.
     *
     * @param resources The resources to be compressed. The path of the resource is determined by
     *                  {@link Resource#getName()}.
     * @return This ZipWriter instance.
     * @throws InternalException If an I/O error occurs.
     */
    public ZipWriter add(final Resource... resources) throws InternalException {
        for (final Resource resource : resources) {
            if (null != resource) {
                add(resource.getName(), resource.getStream());
            }
        }
        return this;
    }

    /**
     * Adds an input stream to the Zip archive. The input stream is closed after adding. If the input stream is
     * {@code null}, only an empty directory is created.
     *
     * @param path The path for the compressed entry. {@code null} or an empty string indicates the root directory.
     * @param in   The input stream to be compressed. It will be automatically closed after use. {@code null} indicates
     *             adding an empty directory.
     * @return This ZipWriter instance.
     * @throws InternalException If an I/O error occurs.
     */
    public ZipWriter add(String path, final InputStream in) throws InternalException {
        path = StringKit.toStringOrEmpty(path);
        if (null == in) {
            // For empty directories, ensure path ends with "/"
            path = StringKit.addSuffixIfNot(path, Symbol.SLASH);
            if (StringKit.isBlank(path)) {
                return this;
            }
        }

        return putEntry(path, in);
    }

    /**
     * Adds data from multiple input streams to the Zip archive. The length of paths and input streams arrays must be
     * equal.
     *
     * @param paths An array of paths or filenames for the stream data within the compressed file.
     * @param ins   An array of input streams to be compressed. They will be automatically closed after use.
     * @return This ZipWriter instance.
     * @throws InternalException        If an I/O error occurs.
     * @throws IllegalArgumentException If paths or ins arrays are empty, or their lengths do not match.
     */
    public ZipWriter add(final String[] paths, final InputStream[] ins) throws InternalException {
        if (ArrayKit.isEmpty(paths) || ArrayKit.isEmpty(ins)) {
            throw new IllegalArgumentException("Paths or ins is empty !");
        }
        if (paths.length != ins.length) {
            throw new IllegalArgumentException("Paths length is not equals to ins length !");
        }

        for (int i = 0; i < paths.length; i++) {
            add(paths[i], ins[i]);
        }

        return this;
    }

    /**
     * Close method.
     */
    @Override
    public void close() throws InternalException {
        try {
            out.finish();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(this.out);
        }
    }

    /**
     * Recursively compresses a folder or file. The {@code srcRootDir} determines the truncation point of the path. For
     * example, if the file path is d:/a/b/c/d.txt and {@code srcRootDir} is d:/a/b, the compressed file and directory
     * structure will be c/d.txt.
     *
     * @param file       The current file or directory object being recursively compressed.
     * @param srcRootDir The root directory of the folder being compressed.
     * @param filter     A file filter to customize which files (or folders) are excluded from compression. {@code null}
     *                   means no filtering.
     * @throws InternalException If an I/O error occurs.
     */
    private void _add(final File file, final String srcRootDir, final FileFilter filter) throws InternalException {
        if (null == file || (null != filter && !filter.accept(file))) {
            return;
        }

        // Get the sub-path of the file relative to the root directory of the compressed folder.
        final String subPath = FileKit.subPath(srcRootDir, file);
        if (file.isDirectory()) {
            // If it's a directory, compress files or subdirectories within it.
            final File[] files = file.listFiles();
            if (ArrayKit.isEmpty(files)) {
                // Add directory. Only empty directories are added as directories; for non-empty ones, parent
                // directories are automatically added when files are created.
                add(subPath, null);
            } else {
                // Compress sub-files or directories within the current directory.
                for (final File childFile : files) {
                    _add(childFile, srcRootDir, filter);
                }
            }
        } else {
            // Check if the file to be added is the compressed result file itself to avoid an infinite loop.
            if (FileKit.equals(file, zipFile)) {
                return;
            }
            // If it's a file or other symbol, compress it directly.
            putEntry(subPath, FileKit.getInputStream(file));
        }
    }

    /**
     * Adds an input stream to the Zip archive. The input stream is closed after adding. If the input stream is
     * {@code null}, only an empty directory is created.
     *
     * @param path The path for the compressed entry. {@code null} or an empty string indicates the root directory.
     * @param in   The input stream to be compressed. It will be automatically closed after use. {@code null} indicates
     *             adding an empty directory.
     * @return This ZipWriter instance.
     * @throws InternalException If an I/O error occurs.
     */
    private ZipWriter putEntry(final String path, final InputStream in) throws InternalException {
        final ZipEntry entry = new ZipEntry(path);
        final ZipOutputStream out = this.out;
        try {
            out.putNextEntry(entry);
            if (null != in) {
                IoKit.copy(in, out, bufferSize);
            }
            out.closeEntry();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }

        IoKit.flush(out);
        return this;
    }

}
