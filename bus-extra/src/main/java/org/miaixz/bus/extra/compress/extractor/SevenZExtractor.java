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
package org.miaixz.bus.extra.compress.extractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.RandomAccess;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Extractor for 7z format archives, used to unpack archived data.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SevenZExtractor implements Extractor, RandomAccess {

    /**
     * The underlying SevenZFile from Apache Commons Compress.
     */
    private final SevenZFile sevenZFile;

    /**
     * Constructor.
     *
     * @param file The archive file.
     */
    public SevenZExtractor(final File file) {
        this(file, null);
    }

    /**
     * Constructor.
     *
     * @param file     The archive file.
     * @param password The password, null for no password.
     */
    public SevenZExtractor(final File file, final char[] password) {
        try {
            this.sevenZFile = SevenZFile.builder().setFile(file).setPassword(password).get();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Constructor.
     *
     * @param in The archive stream.
     */
    public SevenZExtractor(final InputStream in) {
        this(in, null);
    }

    /**
     * Constructor.
     *
     * @param in       The archive stream.
     * @param password The password, null for no password.
     */
    public SevenZExtractor(final InputStream in, final char[] password) {
        this(new SeekableInMemoryByteChannel(IoKit.readBytes(in)), password);
    }

    /**
     * Constructor.
     *
     * @param channel {@link SeekableByteChannel}.
     */
    public SevenZExtractor(final SeekableByteChannel channel) {
        this(channel, null);
    }

    /**
     * Constructor.
     *
     * @param channel  {@link SeekableByteChannel}.
     * @param password The password, null for no password.
     */
    public SevenZExtractor(final SeekableByteChannel channel, final char[] password) {
        try {
            this.sevenZFile = SevenZFile.builder().setSeekableByteChannel(channel).setPassword(password).get();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Extracts (decompresses) archive entries to the specified directory.
     * <p>
     * This implementation extracts all entries matching the predicate to the target directory and closes the archive
     * after extraction.
     * </p>
     *
     * @param targetDir the target directory for extraction
     * @param predicate filter to select which entries to extract (may be {@code null})
     */
    @Override
    public void extract(final File targetDir, final Predicate<ArchiveEntry> predicate) {
        try {
            extractInternal(targetDir, predicate);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            close();
        }
    }

    /**
     * Gets an InputStream for the first matching archive entry.
     * <p>
     * This implementation iterates through archive entries and returns an InputStream for the first matching
     * non-directory entry.
     * </p>
     *
     * @param predicate filter to select which entry to get (may be {@code null})
     * @return an InputStream for the first matching entry, or {@code null} if no match found
     */
    @Override
    public InputStream getFirst(final Predicate<ArchiveEntry> predicate) {
        final SevenZFile sevenZFile = this.sevenZFile;
        for (final SevenZArchiveEntry entry : sevenZFile.getEntries()) {
            if (null != predicate && !predicate.test(entry)) {
                continue;
            }
            if (entry.isDirectory()) {
                continue;
            }

            try {
                // Use the method to find the stream for the entry. Since it's called only once, it traverses only once.
                return sevenZFile.getInputStream(entry);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }

        return null;
    }

    /**
     * Extracts (decompresses) to the specified directory.
     *
     * @param targetDir The target directory.
     * @param predicate A filter for extracted files, used to specify which files to extract. null means no filtering.
     *                  Extracts when {@link Predicate#test(Object)} is {@code true}.
     * @throws IOException if an I/O error occurs.
     */
    private void extractInternal(final File targetDir, final Predicate<ArchiveEntry> predicate) throws IOException {
        Assert.isTrue(null != targetDir && ((!targetDir.exists()) || targetDir.isDirectory()), "target must be dir.");
        final SevenZFile sevenZFile = this.sevenZFile;
        SevenZArchiveEntry entry;
        File outItemFile;
        while (null != (entry = sevenZFile.getNextEntry())) {
            if (null != predicate && !predicate.test(entry)) {
                continue;
            }
            outItemFile = FileKit.file(targetDir, entry.getName());
            if (entry.isDirectory()) {
                // Create the corresponding directory
                // noinspection ResultOfMethodCallIgnored
                outItemFile.mkdirs();
            } else if (entry.hasStream()) {
                // Read the data stream corresponding to the entry.
                // Read directly here instead of calling sevenZFile.getInputStream(entry), because that method needs to
                // traverse to find the entry's position, which affects performance.
                FileKit.copy(new Seven7EntryInputStream(sevenZFile, entry), outItemFile);
            } else {
                // Create an empty file for entries with no data stream.
                FileKit.touch(outItemFile);
            }
        }
    }

    /**
     * Closes this extractor and releases any system resources.
     * <p>
     * This implementation closes the underlying SevenZFile quietly.
     * </p>
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.sevenZFile);
    }

}
