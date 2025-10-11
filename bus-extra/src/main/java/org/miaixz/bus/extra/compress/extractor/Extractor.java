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

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Archive data unpacking wrapper, used to unpack packages such as zip, tar, etc., into files.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Extractor extends Closeable {

    /**
     * Extracts (decompresses) to the specified directory. The stream is automatically closed after completion. This
     * method can only be called once.
     *
     * @param targetDir The target directory.
     */
    default void extract(final File targetDir) {
        extract(targetDir, null);
    }

    /**
     * Extracts (decompresses) to the specified directory. The stream is automatically closed after completion. This
     * method can only be called once.
     *
     * @param targetDir The target directory.
     * @param predicate A filter for extracted files, used to specify which files to extract. {@code null} means no
     *                  filtering. Extracts when {@link Predicate#test(Object)} is {@code true}.
     */
    void extract(File targetDir, Predicate<ArchiveEntry> predicate);

    /**
     * Gets the input stream for a file with the specified name.
     *
     * @param entryName The entry name.
     * @return The file stream, or {@code null} if the file does not exist.
     */
    default InputStream get(final String entryName) {
        return getFirst((entry) -> StringKit.equals(entryName, entry.getName()));
    }

    /**
     * Gets the first file stream in the compressed package that meets the specified filter requirements.
     *
     * @param predicate Used to specify the files to be extracted. null means no filtering. Returns the corresponding
     *                  stream when {@link Predicate#test(Object)} is {@code true}.
     * @return The stream of the first file that meets the filter requirements, or {@code null} if no matching file is
     *         found.
     */
    InputStream getFirst(final Predicate<ArchiveEntry> predicate);

    /**
     * Closes without throwing an exception.
     */
    @Override
    void close();

}
