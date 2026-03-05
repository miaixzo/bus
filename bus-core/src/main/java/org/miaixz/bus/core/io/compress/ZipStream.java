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

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Wrapper for {@link ZipInputStream} resources.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipStream implements ZipResource {

    /**
     * The underlying {@link ZipInputStream}.
     */
    private final ZipInputStream in;

    /**
     * Constructs a new ZipStream instance.
     *
     * @param in The {@link ZipInputStream} to wrap.
     */
    public ZipStream(final ZipInputStream in) {
        this.in = in;
    }

    /**
     * Read method.
     */
    @Override
    public void read(final Consumer<ZipEntry> consumer, final int maxSizeDiff) {
        try {
            ZipEntry zipEntry;
            while (null != (zipEntry = in.getNextEntry())) {
                consumer.accept(zipEntry);
                // ZipBomb check is performed after reading the content, so that information in the entry can be read
                // normally.
                ZipSecurity.checkZipBomb(zipEntry, maxSizeDiff);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the input stream for the given path.
     *
     * @param path the path to the entry
     * @return the input stream, or null if not found
     */
    @Override
    public InputStream get(final String path) {
        try {
            ZipEntry zipEntry;
            while (null != (zipEntry = in.getNextEntry())) {
                if (zipEntry.getName().equals(path)) {
                    return this.in;
                }
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return null;
    }

    /**
     * Gets the input stream for the given entry.
     *
     * @param entry the zip entry
     * @return the input stream
     */
    @Override
    public InputStream get(final ZipEntry entry) {
        return this.in;
    }

    /**
     * Close method.
     */
    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.in);
    }

}
