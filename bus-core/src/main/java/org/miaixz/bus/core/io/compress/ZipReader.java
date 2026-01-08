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

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ZipKit;

/**
 * Zip file or stream reader, generally used for decompressing Zip files.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipReader implements Closeable {

    /**
     * Default maximum size difference for ZipBomb check.
     */
    private static final int DEFAULT_MAX_SIZE_DIFF = 100;
    /**
     * The Zip resource to be read.
     */
    private final ZipResource resource;
    /**
     * The maximum size difference multiplier for ZipBomb check. A value of -1 indicates no ZipBomb check.
     */
    private int maxSizeDiff = DEFAULT_MAX_SIZE_DIFF;

    /**
     * Constructs a new ZipReader instance from a {@link ZipFile}.
     *
     * @param zipFile The Zip file to be read.
     */
    public ZipReader(final ZipFile zipFile) {
        this(new ZipFileResource(zipFile));
    }

    /**
     * Constructs a new ZipReader instance from a {@link ZipInputStream}.
     *
     * @param zin The Zip input stream to be read.
     */
    public ZipReader(final ZipInputStream zin) {
        this(new ZipStream(zin));
    }

    /**
     * Constructs a new ZipReader instance from a {@link ZipResource}.
     *
     * @param resource The Zip resource to be read.
     */
    public ZipReader(final ZipResource resource) {
        this.resource = resource;
    }

    /**
     * Creates a new ZipReader instance from a file.
     *
     * @param zipFile The Zip file to be read.
     * @param charset The character set for the Zip file entries.
     * @return A new ZipReader instance.
     */
    public static ZipReader of(final File zipFile, final Charset charset) {
        return new ZipReader(ZipKit.toZipFile(zipFile, charset));
    }

    /**
     * Creates a new ZipReader instance from an input stream.
     *
     * @param in      The Zip input stream, typically a file input stream.
     * @param charset The character set for the Zip file entries.
     * @return A new ZipReader instance.
     */
    public static ZipReader of(final InputStream in, final Charset charset) {
        return new ZipReader(new ZipInputStream(in, charset));
    }

    /**
     * Sets the maximum size difference multiplier for ZipBomb check. A value of -1 indicates no ZipBomb check.
     *
     * @param maxSizeDiff The maximum size difference multiplier.
     * @return This ZipReader instance.
     */
    public ZipReader setMaxSizeDiff(final int maxSizeDiff) {
        this.maxSizeDiff = maxSizeDiff;
        return this;
    }

    /**
     * Retrieves the input stream for a specific entry within the Zip file. If in file mode, it directly gets the stream
     * corresponding to the entry. If in stream mode, it iterates through entries to find and return the corresponding
     * stream.
     *
     * @param path The path of the entry within the Zip file.
     * @return The input stream for the specified entry, or {@code null} if not found.
     */
    public InputStream get(final String path) {
        return this.resource.get(path);
    }

    /**
     * Decompresses the Zip file to the specified output directory.
     *
     * @param outFile The directory to which the Zip file contents will be extracted.
     * @return The output directory.
     * @throws InternalException If an I/O error occurs during decompression.
     */
    public File readTo(final File outFile) throws InternalException {
        return readTo(outFile, null);
    }

    /**
     * Decompresses the Zip file to the specified output directory, filtering entries with a {@link Predicate}.
     *
     * @param outFile     The directory to which the Zip file contents will be extracted.
     * @param entryFilter A filter to retain only entries for which {@link Predicate#test(Object)} returns {@code true}.
     * @return The output directory.
     * @throws InternalException If an I/O error occurs during decompression.
     */
    public File readTo(final File outFile, final Predicate<ZipEntry> entryFilter) throws InternalException {
        read((zipEntry) -> {
            if (null == entryFilter || entryFilter.test(zipEntry)) {
                readEntry(zipEntry, outFile);
            }
        });
        return outFile;
    }

    /**
     * Reads and processes each {@link ZipEntry} in the Zip file.
     *
     * @param consumer A {@link ZipEntry} consumer to process each entry.
     * @return This ZipReader instance.
     * @throws InternalException If an I/O error occurs during reading.
     */
    public ZipReader read(final Consumer<ZipEntry> consumer) throws InternalException {
        resource.read(consumer, this.maxSizeDiff);
        return this;
    }

    /**
     * Close method.
     */
    @Override
    public void close() throws InternalException {
        IoKit.closeQuietly(this.resource);
    }

    /**
     * Reads a ZipEntry's data into the target directory. If the entry is a directory, it creates the corresponding
     * directory; otherwise, it decompresses and writes to a file.
     *
     * @param zipEntry The Zip entry to read.
     * @param outFile  The output directory to which the entry will be written.
     */
    private void readEntry(final ZipEntry zipEntry, final File outFile) {
        String path = zipEntry.getName();
        if (FileKit.isWindows()) {
            // On Windows systems, replace '*' with '_'
            path = StringKit.replace(path, Symbol.STAR, Symbol.UNDERLINE);
        }
        // FileKit.file checks for zip slip vulnerability, see http://blog.nsfocus.net/zip-slip-2/
        final File outItemFile = FileKit.file(outFile, path);
        if (zipEntry.isDirectory()) {
            // Directory
            outItemFile.mkdirs();
        } else {
            // File
            FileKit.copy(this.resource.get(zipEntry), outItemFile);
        }
    }

}
