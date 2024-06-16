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
package org.miaixz.bus.core.io.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 限制读取最大长度的{@link FilterInputStream}实现
 *
 * <p>
 * 来自：https://github.com/skylot/jadx/blob/master/jadx-plugins/jadx-plugins-api/src/main/java/jadx/api/plugins/utils/LimitedInputStream.java
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LimitedInputStream extends FilterInputStream {

    private final long maxSize;
    private long currentPos;

    /**
     * 构造
     *
     * @param in      {@link InputStream}
     * @param maxSize 限制最大读取量，单位byte
     */
    public LimitedInputStream(final InputStream in, final long maxSize) {
        super(in);
        this.maxSize = maxSize;
    }

    @Override
    public int read() throws IOException {
        final int data = super.read();
        if (data != -1) {
            currentPos++;
            checkPos();
        }
        return data;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int count = super.read(b, off, len);
        if (count > 0) {
            currentPos += count;
            checkPos();
        }
        return count;
    }

    @Override
    public long skip(final long n) throws IOException {
        final long skipped = super.skip(n);
        if (skipped != 0) {
            currentPos += skipped;
            checkPos();
        }
        return skipped;
    }

    private void checkPos() {
        if (currentPos > maxSize) {
            throw new IllegalStateException("Read limit exceeded");
        }
    }

}
