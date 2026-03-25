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
package org.miaixz.bus.core.io.compress;

import java.io.Closeable;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;

/**
 * Represents a Zip resource, such as a Zip stream resource or a Zip file resource.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ZipResource extends Closeable {

    /**
     * Reads and processes each {@link ZipEntry} in the Zip file.
     *
     * @param consumer    The {@link ZipEntry} processor.
     * @param maxSizeDiff The maximum size difference multiplier for ZipBomb check. A value of -1 indicates no ZipBomb
     *                    check.
     */
    void read(final Consumer<ZipEntry> consumer, final int maxSizeDiff);

    /**
     * Retrieves the input stream for a specific entry within the Zip file. If in file mode, it directly gets the stream
     * corresponding to the entry. If in stream mode, it iterates through entries to find and return the corresponding
     * stream.
     *
     * @param path The path of the entry.
     * @return The input stream for the specified entry.
     */
    InputStream get(String path);

    /**
     * Retrieves the input stream corresponding to the specified {@link ZipEntry}.
     *
     * @param entry The {@link ZipEntry}.
     * @return The input stream for the specified entry.
     */
    InputStream get(ZipEntry entry);

}
