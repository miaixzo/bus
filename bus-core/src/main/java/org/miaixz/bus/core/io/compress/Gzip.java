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
