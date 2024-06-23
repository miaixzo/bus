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
package org.miaixz.bus.core.io;

import java.io.IOException;
import java.io.Reader;

/**
 * 快速字符串读取，相比jdk的StringReader非线程安全，速度更快。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastStringReader extends Reader {

    private final int length;
    private String text;
    private int next = 0;
    private int mark = 0;

    /**
     * 构造
     *
     * @param s 供读取的String
     */
    public FastStringReader(final String s) {
        this.text = s;
        this.length = s.length();
    }

    /**
     * 读取单个字符
     *
     * @return 读取的字符, -1表示读取结束
     * @throws IOException IO异常
     */
    @Override
    public int read() throws IOException {
        ensureOpen();
        if (next >= length) {
            return -1;
        }
        return text.charAt(next++);
    }

    /**
     * 将多个字符读取到提供的字符数组中
     *
     * @param charBuffer 目标buffer
     * @param off        开始位置
     * @param len        读取最大长度
     * @return 读取的字符长度, -1表示读取到了末尾
     * @throws IOException IO异常
     */
    @Override
    public int read(final char[] charBuffer, final int off, final int len) throws IOException {
        ensureOpen();
        if ((off < 0) || (off > charBuffer.length) || (len < 0) ||
                ((off + len) > charBuffer.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }

        if (len == 0) {
            return 0;
        }
        if (next >= length) {
            return -1;
        }

        final int n = Math.min(length - next, len);
        text.getChars(next, next + n, charBuffer, off);
        next += n;
        return n;
    }

    /**
     * 跳过指定长度，返回跳过的字符数。
     *
     * {@code ns} 参数可能为负数, 负数表示向前跳过，跳到开头则停止
     *
     * 如果字符串所有字符被读取或跳过, 此方法无效，始终返回0.
     *
     * @throws IOException IO异常
     */
    @Override
    public long skip(final long ns) throws IOException {
        ensureOpen();
        if (next >= length) {
            return 0;
        }

        // Bound skip by beginning and end of the source
        long n = Math.min(length - next, ns);
        n = Math.max(-next, n);
        next += n;
        return n;
    }

    @Override
    public boolean ready() throws IOException {
        ensureOpen();
        return true;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(final int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        ensureOpen();
        mark = next;
    }

    @Override
    public void reset() throws IOException {
        ensureOpen();
        next = mark;
    }

    @Override
    public void close() {
        text = null;
    }

    /**
     * Check to make sure that the stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (text == null) {
            throw new IOException("Stream closed");
        }
    }

}
