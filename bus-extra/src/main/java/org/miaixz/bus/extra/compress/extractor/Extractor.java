/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
