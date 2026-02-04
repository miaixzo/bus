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
package org.miaixz.bus.extra.compress.archiver;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.compress.CompressBuilder;

/**
 * 7zip format archiver wrapper.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SevenZArchiver implements Archiver {

    /**
     * The underlying SevenZOutputFile from Apache Commons Compress.
     */
    private final SevenZOutputFile sevenZOutputFile;

    /**
     * The SeekableByteChannel used for writing, especially for in-memory operations.
     */
    private SeekableByteChannel channel;
    /**
     * The target OutputStream, used when archiving to a stream.
     */
    private OutputStream out;

    /**
     * Constructor.
     *
     * @param file The archive output file.
     */
    public SevenZArchiver(final File file) {
        try {
            this.sevenZOutputFile = new SevenZOutputFile(file);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Constructor.
     *
     * @param out The archive output stream.
     */
    public SevenZArchiver(final OutputStream out) {
        this.out = out;
        this.channel = new SeekableInMemoryByteChannel();
        try {
            this.sevenZOutputFile = new SevenZOutputFile(channel);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Constructor.
     *
     * @param channel The archive output channel.
     */
    public SevenZArchiver(final SeekableByteChannel channel) {
        try {
            this.sevenZOutputFile = new SevenZOutputFile(channel);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the {@link SevenZOutputFile} to allow for custom settings.
     *
     * @return The {@link SevenZOutputFile}.
     */
    public SevenZOutputFile getSevenZOutputFile() {
        return this.sevenZOutputFile;
    }

    /**
     * Adds a file or directory to the 7-Zip archive with optional filtering and path editing. Directories are processed
     * recursively.
     * <p>
     * Adds a file or directory to the 7-Zip archive with optional filtering and path editing. Directories are processed
     * recursively.
     * </p>
     *
     * @param file           the file or directory to add
     * @param path           the initial path within the archive (may be {@code null})
     * @param fileNameEditor function to edit file names (may be {@code null})
     * @param filter         filter to select which files to add (may be {@code null})
     * @return this {@code SevenZArchiver} instance for method chaining
     */
    @Override
    public SevenZArchiver add(
            final File file,
            final String path,
            final Function<String, String> fileNameEditor,
            final Predicate<File> filter) {
        try {
            addInternal(file, path, fileNameEditor, filter);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Finishes the archive creation, writing any pending data.
     * <p>
     * Finishes the archive creation, writing any pending data.
     * </p>
     *
     * @return this {@code SevenZArchiver} instance for method chaining
     */
    @Override
    public SevenZArchiver finish() {
        try {
            this.sevenZOutputFile.finish();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Closes the archive and its underlying resources. If this archiver was created with an OutputStream, writes the
     * archive data to it.
     * <p>
     * Closes the archive and its underlying resources. If this archiver was created with an OutputStream, writes the
     * archive data to it.
     * </p>
     */
    @Override
    public void close() {
        try {
            finish();
        } catch (final Exception ignore) {
            // ignore
        }
        if (null != out && this.channel instanceof SeekableInMemoryByteChannel) {
            try {
                out.write(((SeekableInMemoryByteChannel) this.channel).array());
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
        IoKit.closeQuietly(this.sevenZOutputFile);
    }

    /**
     * Adds a file or directory to the archive package. Directories are added recursively level by level.
     *
     * @param file           The file or directory.
     * @param path           The initial path of the file or directory. If null, it is placed at the root level.
     * @param fileNameEditor A function to edit the file name.
     * @param filter         A file filter that specifies which files or directories can be added. If
     *                       {@link Predicate#test(Object)} is {@code true}, the file is kept. If null, all are kept.
     * @throws IOException if an I/O error occurs.
     */
    private void addInternal(
            final File file,
            final String path,
            final Function<String, String> fileNameEditor,
            final Predicate<File> filter) throws IOException {
        if (null != filter && !filter.test(file)) {
            return;
        }
        final SevenZOutputFile out = this.sevenZOutputFile;

        final String entryName = CompressBuilder.getEntryName(file.getName(), path, fileNameEditor);
        out.putArchiveEntry(out.createArchiveEntry(file, entryName));

        if (file.isDirectory()) {
            // Traverse and write directory contents
            final File[] files = file.listFiles();
            if (ArrayKit.isNotEmpty(files)) {
                for (final File childFile : files) {
                    addInternal(childFile, entryName, fileNameEditor, filter);
                }
            }
        } else {
            if (file.isFile()) {
                // Write file directly
                out.write(FileKit.readBytes(file));
            }
            out.closeArchiveEntry();
        }
    }

}
