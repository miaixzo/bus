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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * GZIP is a file compression format used in Unix systems. The basis of gzip is DEFLATE.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Gzip implements Closeable {

    /**
     * The source input stream.
     */
    private InputStream source;
    /**
     * The target output stream.
     */
    private OutputStream target;

    /**
     * Constructs a new Gzip instance.
     *
     * @param source The source input stream.
     * @param target The target output stream.
     */
    public Gzip(final InputStream source, final OutputStream target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Creates a new Gzip instance.
     *
     * @param source The source input stream.
     * @param target The target output stream.
     * @return A new Gzip instance.
     */
    public static Gzip of(final InputStream source, final OutputStream target) {
        return new Gzip(source, target);
    }

    /**
     * Retrieves the target output stream.
     *
     * @return The target output stream.
     */
    public OutputStream getTarget() {
        return this.target;
    }

    /**
     * Compresses the normal data stream.
     *
     * @return This Gzip instance.
     */
    public Gzip gzip() {
        try {
            target = (target instanceof GZIPOutputStream) ? (GZIPOutputStream) target : new GZIPOutputStream(target);
            IoKit.copy(source, target);
            ((GZIPOutputStream) target).finish();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Decompresses the compressed stream into the target.
     *
     * @return This Gzip instance.
     */
    public Gzip unGzip() {
        try {
            source = (source instanceof GZIPInputStream) ? (GZIPInputStream) source : new GZIPInputStream(source);
            IoKit.copy(source, target);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Close method.
     */
    @Override
    public void close() {
        IoKit.closeQuietly(this.target);
        IoKit.closeQuietly(this.source);
    }

}
