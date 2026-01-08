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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Deflate algorithm. Deflate is a lossless data compression algorithm that uses both LZ77 algorithm and Huffman coding.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Deflate implements Closeable {

    /**
     * The source input stream.
     */
    private final InputStream source;
    /**
     * Flag indicating whether to use the 'nowrap' option for Deflater/Inflater, which is compatible with Gzip.
     */
    private final boolean nowrap;
    /**
     * The target output stream.
     */
    private OutputStream target;

    /**
     * Constructs a new Deflate instance.
     *
     * @param source The source input stream.
     * @param target The target output stream.
     * @param nowrap {@code true} to be compatible with Gzip compression, {@code false} otherwise.
     */
    public Deflate(final InputStream source, final OutputStream target, final boolean nowrap) {
        this.source = source;
        this.target = target;
        this.nowrap = nowrap;
    }

    /**
     * Creates a new Deflate instance.
     *
     * @param source The source input stream.
     * @param target The target output stream.
     * @param nowrap {@code true} to be compatible with Gzip compression, {@code false} otherwise.
     * @return A new Deflate instance.
     */
    public static Deflate of(final InputStream source, final OutputStream target, final boolean nowrap) {
        return new Deflate(source, target, nowrap);
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
     * Compresses the normal data stream using Deflate algorithm.
     *
     * @param level The compression level, from 0 (no compression) to 9 (best compression).
     * @return This Deflate instance.
     * @throws InternalException if an I/O error occurs during compression.
     */
    public Deflate deflater(final int level) {
        target = (target instanceof DeflaterOutputStream) ? (DeflaterOutputStream) target
                : new DeflaterOutputStream(target, new Deflater(level, nowrap));
        IoKit.copy(source, target);
        try {
            ((DeflaterOutputStream) target).finish();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Decompresses the compressed stream into the target using Inflate algorithm.
     *
     * @return This Deflate instance.
     * @throws InternalException if an I/O error occurs during decompression.
     */
    public Deflate inflater() {
        target = (target instanceof InflaterOutputStream) ? (InflaterOutputStream) target
                : new InflaterOutputStream(target, new Inflater(nowrap));
        IoKit.copy(source, target);
        try {
            ((InflaterOutputStream) target).finish();
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
