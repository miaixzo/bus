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
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ZipKit;

/**
 * Wrapper for {@link ZipFile} resources.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipFileResource implements ZipResource {

    /**
     * The underlying {@link ZipFile}.
     */
    private final ZipFile zipFile;

    /**
     * Constructs a new ZipFileResource instance.
     *
     * @param zipFile The {@link ZipFile} to wrap.
     */
    public ZipFileResource(final ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    /**
     * Read method.
     */
    @Override
    public void read(final Consumer<ZipEntry> consumer, final int maxSizeDiff) {
        final Enumeration<? extends ZipEntry> em = zipFile.entries();
        while (em.hasMoreElements()) {
            consumer.accept(ZipSecurity.checkZipBomb(em.nextElement(), maxSizeDiff));
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
        final ZipFile zipFile = this.zipFile;
        final ZipEntry entry = zipFile.getEntry(path);
        if (null != entry) {
            return ZipKit.getStream(zipFile, entry);
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
        return ZipKit.getStream(this.zipFile, entry);
    }

    /**
     * Close method.
     */
    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.zipFile);
    }

}
