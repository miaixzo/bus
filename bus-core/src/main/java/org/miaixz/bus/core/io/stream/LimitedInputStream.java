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
package org.miaixz.bus.core.io.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

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

    private final boolean throwWhenReachLimit;
    protected long limit;

    /**
     * 构造
     *
     * @param in    {@link InputStream}
     * @param limit 限制最大读取量，单位byte
     */
    public LimitedInputStream(final InputStream in, final long limit) {
        this(in, limit, true);
    }

    /**
     * 构造
     *
     * @param in                  {@link InputStream}
     * @param limit               限制最大读取量，单位byte
     * @param throwWhenReachLimit 是否在达到限制时抛出异常，{@code false}则读取到限制后返回-1
     */
    public LimitedInputStream(final InputStream in, final long limit, final boolean throwWhenReachLimit) {
        super(Assert.notNull(in, "InputStream must not be null!"));
        this.limit = Math.max(0L, limit);
        this.throwWhenReachLimit = throwWhenReachLimit;
    }

    @Override
    public int read() throws IOException {
        final int data = (limit == 0) ? Normal.__1 : super.read();
        checkLimit(data);
        limit = (data < 0) ? 0 : limit - 1;
        return data;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int length = (limit == 0) ? Normal.__1 : super.read(b, off, len > limit ? (int) limit : len);
        checkLimit(length);
        limit = (length < 0) ? 0 : limit - length;
        return length;
    }

    @Override
    public long skip(final long len) throws IOException {
        final long length = super.skip(Math.min(len, limit));
        checkLimit(length);
        limit -= length;
        return length;
    }

    @Override
    public int available() throws IOException {
        final int length = super.available();
        return length > limit ? (int) limit : length;
    }

    /**
     * 检查读取数据是否达到限制
     *
     * @param data 读取的数据
     * @throws IOException 定义了限制并设置达到限制后抛出异常
     */
    private void checkLimit(final long data) throws IOException {
        if (data < 0 && limit > 0 && throwWhenReachLimit) {
            throw new IOException("Read limit exceeded");
        }
    }

}
