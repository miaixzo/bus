/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.Inflater;

/**
 * {@link java.util.zip.InflaterInputStream}包装实现，实现"deflate"算法解压 参考：org.apache.hc.client5.http.entity.DeflateInputStream
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InflaterStream extends InputStream {

    private final java.util.zip.InflaterInputStream in;

    /**
     * 构造
     *
     * @param wrapped 被包装的流
     */
    public InflaterStream(final InputStream wrapped) {
        this(wrapped, 512);
    }

    /**
     * 构造
     *
     * @param wrapped 被包装的流
     * @param size    buffer大小
     */
    public InflaterStream(final InputStream wrapped, final int size) {
        final PushbackInputStream pushback = new PushbackInputStream(wrapped, 2);
        final int i1, i2;
        try {
            i1 = pushback.read();
            i2 = pushback.read();
            if (i1 == -1 || i2 == -1) {
                throw new InternalException("Unexpected end of stream");
            }

            pushback.unread(i2);
            pushback.unread(i1);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        boolean nowrap = true;
        final int b1 = i1 & 0xFF;
        final int compressionMethod = b1 & 0xF;
        final int compressionInfo = b1 >> 4 & 0xF;
        final int b2 = i2 & 0xFF;
        if (compressionMethod == 8 && compressionInfo <= 7 && ((b1 << 8) | b2) % 31 == 0) {
            nowrap = false;
        }
        in = new java.util.zip.InflaterInputStream(pushback, new Inflater(nowrap), size);
    }

    @Override
    public int read() throws IOException {
        return this.in.read();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public long skip(final long n) throws IOException {
        return in.skip(n);
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void mark(final int readLimit) {
        in.mark(readLimit);
    }

    @Override
    public void reset() throws IOException {
        in.reset();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

}
