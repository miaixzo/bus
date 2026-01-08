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
package org.miaixz.bus.extra.compress.extractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Data decompressor, used to extract data from an archive package.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamExtractor implements Extractor {

    /**
     * The underlying ArchiveInputStream from Apache Commons Compress.
     */
    private final ArchiveInputStream<?> in;

    /**
     * Constructor.
     *
     * @param charset The character encoding.
     * @param file    The archive file.
     */
    public StreamExtractor(final Charset charset, final File file) {
        this(charset, null, file);
    }

    /**
     * Constructor.
     *
     * @param charset      The character encoding.
     * @param archiverName The name of the archive format, null for auto-detection.
     * @param file         The archive file.
     */
    public StreamExtractor(final Charset charset, final String archiverName, final File file) {
        this(charset, archiverName, FileKit.getInputStream(file));
    }

    /**
     * Constructor.
     *
     * @param charset The character encoding.
     * @param in      The archive stream.
     */
    public StreamExtractor(final Charset charset, final InputStream in) {
        this(charset, null, in);
    }

    /**
     * Constructor.
     *
     * @param charset      The character encoding.
     * @param archiverName The name of the archive format, null for auto-detection.
     * @param in           The archive stream.
     */
    public StreamExtractor(final Charset charset, final String archiverName, InputStream in) {
        if (in instanceof ArchiveInputStream) {
            this.in = (ArchiveInputStream<?>) in;
            return;
        }

        final ArchiveStreamFactory factory = new ArchiveStreamFactory(charset.name());
        try {
            in = IoKit.toBuffered(in);
            if (StringKit.isBlank(archiverName)) {
                this.in = factory.createArchiveInputStream(in);
            } else if ("tgz".equalsIgnoreCase(archiverName) || "tar.gz".equalsIgnoreCase(archiverName)) {
                // Support for tgz format decompression
                try {
                    this.in = new TarArchiveInputStream(new GzipCompressorInputStream(in));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
            } else {
                this.in = factory.createArchiveInputStream(archiverName, in);
            }
        } catch (final ArchiveException e) {
            // If an error occurs, the file handle may be held, preventing file deletion.
            IoKit.closeQuietly(in);
            throw new InternalException(e);
        }
    }

    /**
     * Gets an InputStream for the first matching archive entry.
     * <p>
     * Retrieves an input stream for the first archive entry matching the given predicate. The returned stream is the
     * underlying archive stream, positioned at the matching entry.
     * </p>
     *
     * @param predicate filter to select the desired entry (may be {@code null} for the first entry)
     * @return an input stream for the matching entry, or {@code null} if no match is found
     */
    @Override
    public InputStream getFirst(final Predicate<ArchiveEntry> predicate) {
        final ArchiveInputStream<?> in = this.in;
        ArchiveEntry entry;
        try {
            while (null != (entry = in.getNextEntry())) {
                if (null != predicate && !predicate.test(entry)) {
                    continue;
                }
                if (entry.isDirectory() || !in.canReadEntryData(entry)) {
                    // Skip directories or unreadable files directly
                    continue;
                }

                return in;
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return null;
    }

    /**
     * Extracts (decompresses) to the specified directory. The stream is automatically closed after completion. This
     * method can only be called once.
     *
     * @param targetDir The target directory.
     * @param predicate A filter for extracted files, used to specify which files to extract. null means no filtering.
     *                  Extracts when {@link Predicate#test(Object)} is {@code true}.
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
     * Extracts (decompresses) to the specified directory.
     *
     * @param targetDir The target directory.
     * @param predicate A filter for extracted files, used to specify which files to extract. null means no filtering.
     *                  Extracts when {@link Predicate#test(Object)} is {@code true}.
     * @throws IOException if an I/O error occurs.
     */
    private void extractInternal(final File targetDir, final Predicate<ArchiveEntry> predicate) throws IOException {
        Assert.isTrue(null != targetDir && ((!targetDir.exists()) || targetDir.isDirectory()), "target must be dir.");
        final ArchiveInputStream<?> in = this.in;
        ArchiveEntry entry;
        File outItemFile;
        while (null != (entry = in.getNextEntry())) {
            if (null != predicate && !predicate.test(entry)) {
                continue;
            }
            if (!in.canReadEntryData(entry)) {
                // Skip unreadable files directly
                continue;
            }
            outItemFile = FileKit.file(targetDir, entry.getName());
            if (entry.isDirectory()) {
                // Create the corresponding directory
                // noinspection ResultOfMethodCallIgnored
                outItemFile.mkdirs();
            } else {
                FileKit.copy(in, outItemFile);
            }
        }
    }

    /**
     * Closes this extractor and releases any system resources.
     * <p>
     * Closes the archive stream and releases any associated resources. This method suppresses any exceptions that may
     * occur.
     * </p>
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.in);
    }

}
