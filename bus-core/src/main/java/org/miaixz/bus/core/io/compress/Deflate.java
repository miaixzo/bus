/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
