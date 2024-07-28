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
package org.miaixz.bus.image.galaxy;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * An {@link InputStream} that counts the number of bytes read.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CountingInputStream extends FilterInputStream {

    private volatile long count;
    private volatile long mark;

    public CountingInputStream(InputStream in) {
        super(Objects.requireNonNull(in));
    }

    public long getCount() {
        return count;
    }

    @Override
    public int read() throws IOException {
        return incCount(in.read());
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return addCount(in.read(b, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
        return addCount(in.skip(n));
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        mark = count;
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        count = mark;
    }

    private int incCount(int read) {
        if (read >= 0)
            count++;
        return read;
    }

    private int addCount(int read) {
        if (read > 0)
            count += read;
        return read;
    }

    private long addCount(long skip) {
        if (skip > 0)
            count += skip;
        return skip;
    }

}
