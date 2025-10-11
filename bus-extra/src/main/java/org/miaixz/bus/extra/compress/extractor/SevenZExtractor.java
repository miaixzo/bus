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

    @Override
    public void close() {
        IoKit.closeQuietly(this.sevenZFile);
    }

}
