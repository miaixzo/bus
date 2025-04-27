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
package org.miaixz.bus.image.galaxy;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class LimitedInputStream extends FilterInputStream {

    private final boolean closeSource;
    private long remaining;
    private long mark = -1;

    public LimitedInputStream(InputStream in, long limit, boolean closeSource) {
        super(Objects.requireNonNull(in));
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be > 0");
        this.remaining = limit;
        this.closeSource = closeSource;
    }

    @Override
    public int read() throws IOException {
        int result;
        if (remaining == 0 || (result = in.read()) < 0) {
            return -1;
        }

        --remaining;
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result;
        if (remaining == 0 || (result = in.read(b, off, (int) Math.min(len, remaining))) < 0) {
            return -1;
        }

        remaining -= result;
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = in.skip(Math.min(n, remaining));
        remaining -= result;
        return result;
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(in.available(), remaining);
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        mark = remaining;
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        remaining = mark;
    }

    @Override
    public void close() throws IOException {
        if (closeSource)
            in.close();
    }

    public long getRemaining() {
        return remaining;
    }

}
