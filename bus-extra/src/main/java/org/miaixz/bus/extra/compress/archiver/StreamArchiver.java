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
package org.miaixz.bus.extra.compress.archiver;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.compress.CompressBuilder;

/**
 * Data archiver wrapper, which archives several files or directories into a compressed package. Supported archive file
 * formats are:
 * <ul>
 * <li>{@link ArchiveStreamFactory#AR}</li>
 * <li>{@link ArchiveStreamFactory#CPIO}</li>
 * <li>{@link ArchiveStreamFactory#JAR}</li>
 * <li>{@link ArchiveStreamFactory#TAR}</li>
 * <li>{@link ArchiveStreamFactory#ZIP}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamArchiver implements Archiver {

    /**
     * The underlying ArchiveOutputStream from Apache Commons Compress.
     */
    private final ArchiveOutputStream<? extends ArchiveEntry> out;

    /**
     * Constructor.
     *
     * @param charset      The character encoding.
     * @param archiverName The name of the archive format, see {@link ArchiveStreamFactory}.
     * @param file         The output archive file.
     */
    public StreamArchiver(final Charset charset, final String archiverName, final File file) {
        this(charset, archiverName, FileKit.getOutputStream(file));
    }

    /**
     * Constructor.
     *
     * @param charset      The character encoding.
     * @param archiverName The name of the archive format, see {@link ArchiveStreamFactory}.
     * @param targetStream The output stream for the archive.
     */
    public StreamArchiver(final Charset charset, final String archiverName, final OutputStream targetStream) {
        if ("tgz".equalsIgnoreCase(archiverName) || "tar.gz".equalsIgnoreCase(archiverName)) {
            // Support for tgz format archiving
            try {
                this.out = new TarArchiveOutputStream(new GzipCompressorOutputStream(targetStream));
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        } else {
            final ArchiveStreamFactory factory = new ArchiveStreamFactory(charset.name());
            try {
                this.out = factory.createArchiveOutputStream(archiverName, targetStream);
            } catch (final ArchiveException e) {
                throw new InternalException(e);
            }
        }

        // Special settings
        if (this.out instanceof TarArchiveOutputStream) {
            ((TarArchiveOutputStream) out).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        } else if (this.out instanceof ArArchiveOutputStream) {
            ((ArArchiveOutputStream) out).setLongFileMode(ArArchiveOutputStream.LONGFILE_BSD);
        }
    }

    /**
     * Creates an archiver.
     *
     * @param charset      The character encoding.
     * @param archiverName The name of the archive format, see {@link ArchiveStreamFactory}.
     * @param file         The output archive file.
     * @return A new StreamArchiver instance.
     */
    public static StreamArchiver of(final Charset charset, final String archiverName, final File file) {
        return new StreamArchiver(charset, archiverName, file);
    }

    /**
     * Creates an archiver.
     *
     * @param charset      The character encoding.
     * @param archiverName The name of the archive format, see {@link ArchiveStreamFactory}.
     * @param out          The output stream for the archive.
     * @return A new StreamArchiver instance.
     */
    public static StreamArchiver of(final Charset charset, final String archiverName, final OutputStream out) {
        return new StreamArchiver(charset, archiverName, out);
    }

    /**
     * Adds a file or directory to the archive with optional filtering and path editing. Directories are processed
     * recursively.
     * <p>
     * Adds a file or directory to the archive with optional filtering and path editing. Directories are processed
     * recursively.
     * </p>
     *
     * @param file           the file or directory to add
     * @param path           the initial path within the archive (may be {@code null})
     * @param fileNameEditor function to edit file names (may be {@code null})
     * @param predicate      filter to select which files to add (may be {@code null})
     * @return this {@code StreamArchiver} instance for method chaining
     * @throws InternalException if an I/O error occurs during archiving
     */
    @Override
    public StreamArchiver add(
            final File file,
            final String path,
            final Function<String, String> fileNameEditor,
            final Predicate<File> predicate) throws InternalException {
        try {
            addInternal(file, path, fileNameEditor, predicate);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Finishes archiving the added files. This method does not close the archive stream, allowing more files to be
     * added.
     *
     * @return this
     */
    @Override
    public StreamArchiver finish() {
        try {
            this.out.finish();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Closes the archive stream, ensuring all data is flushed. This method suppresses any exceptions that may occur.
     * <p>
     * Closes the archive stream, ensuring all data is flushed. This method suppresses any exceptions that may occur.
     * </p>
     */
    @Override
    public void close() {
        try {
            finish();
        } catch (final Exception ignore) {
            // ignore
        }
        IoKit.closeQuietly(this.out);
    }

    /**
     * Adds a file or directory to the archive package. Directories are added recursively level by level.
     *
     * @param file           The file or directory.
     * @param path           The initial path of the file or directory. If {@code null}, it is placed at the root level.
     * @param fileNameEditor A function to edit the file name.
     * @param predicate      A file filter that specifies which files or directories can be added. When
     *                       {@link Predicate#test(Object)} is {@code true}, the file is added.
     * @throws IOException if an I/O error occurs
     */
    private void addInternal(
            final File file,
            final String path,
            final Function<String, String> fileNameEditor,
            final Predicate<File> predicate) throws IOException {
        if (null != predicate && !predicate.test(file)) {
            return;
        }
        final ArchiveOutputStream out = this.out;

        final String entryName = CompressBuilder.getEntryName(file.getName(), path, fileNameEditor);
        out.putArchiveEntry(out.createArchiveEntry(file, entryName));

        if (file.isDirectory()) {
            // Traverse and write directory
            final File[] files = file.listFiles();
            if (ArrayKit.isNotEmpty(files)) {
                for (final File childFile : files) {
                    addInternal(childFile, entryName, fileNameEditor, predicate);
                }
            } else {
                // Empty folders also need to be closed
                out.closeArchiveEntry();
            }
        } else {
            if (file.isFile()) {
                // Write file directly
                FileKit.copy(file, out);
            }
            out.closeArchiveEntry();
        }
    }

}
