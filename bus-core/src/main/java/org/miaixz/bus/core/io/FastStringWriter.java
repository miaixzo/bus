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

import java.io.Writer;

/**
 * 借助{@link StringBuilder} 提供快读的字符串写出，相比jdk的StringWriter非线程安全，速度更快。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class FastStringWriter extends Writer {

    private static final int DEFAULT_CAPACITY = 16;

    private final StringBuilder stringBuilder;

    /**
     * 构造
     */
    public FastStringWriter() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialSize 初始容量
     */
    public FastStringWriter(int initialSize) {
        if (initialSize < 0) {
            initialSize = DEFAULT_CAPACITY;
        }
        this.stringBuilder = new StringBuilder(initialSize);
    }

    @Override
    public FastStringWriter append(final char c) {
        this.stringBuilder.append(c);
        return this;
    }

    @Override
    public FastStringWriter append(final CharSequence csq, final int start, final int end) {
        this.stringBuilder.append(csq, start, end);
        return this;
    }

    @Override
    public FastStringWriter append(final CharSequence csq) {
        this.stringBuilder.append(csq);
        return this;
    }

    @Override
    public void write(final int c) {
        this.stringBuilder.append((char) c);
    }

    @Override
    public void write(final String text) {
        this.stringBuilder.append(text);
    }

    @Override
    public void write(final String text, final int off, final int len) {
        this.stringBuilder.append(text, off, off + len);
    }

    @Override
    public void write(final char[] c) {
        this.stringBuilder.append(c, 0, c.length);
    }

    @Override
    public void write(final char[] c, final int off, final int len) {
        if ((off < 0) || (off > c.length) || (len < 0) || ((off + len) > c.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        this.stringBuilder.append(c, off, len);
    }

    @Override
    public void flush() {
        // Nothing to be flushed
    }

    @Override
    public void close() {
        // Nothing to be closed
    }

    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }

}
