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
package org.miaixz.bus.core.io;

import java.io.Reader;
import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * 快速字符串读取，相比jdk的StringReader非线程安全，速度更快。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharSequenceReader extends Reader {

    /**
     * 开始位置
     */
    private final int start;
    /**
     * 结束位置
     */
    private final int end;
    /**
     * 读取字符
     */
    private final CharSequence text;
    /**
     * 读取到的位置
     */
    private int next;
    /**
     * 读取标记位置
     */
    private int mark;

    /**
     * 构造
     *
     * @param text         {@link CharSequence}
     * @param startInclude 开始位置（包含）
     * @param endExclude   结束位置（不包含）
     */
    public CharSequenceReader(CharSequence text, final int startInclude, final int endExclude) {
        Assert.isTrue(startInclude >= 0, "Start index is less than zero: {}", startInclude);
        Assert.isTrue(endExclude > startInclude, "End index is less than start {}: {}", startInclude, endExclude);

        if (null == text) {
            text = Normal.EMPTY;
        }
        this.text = text;
        final int length = text.length();
        this.start = Math.min(length, startInclude);
        this.end = Math.min(length, endExclude);

        this.next = startInclude;
        this.mark = startInclude;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(final int readAheadLimit) {
        mark = next;
    }

    @Override
    public void reset() {
        next = mark;
    }

    @Override
    public int read() {
        if (next >= end) {
            return Normal.__1;
        }
        return text.charAt(next++);
    }

    @Override
    public int read(final char[] array, final int offset, final int length) {
        if (next >= end) {
            return Normal.__1;
        }
        Objects.requireNonNull(array, "array");
        if (length < 0 || offset < 0 || offset + length > array.length) {
            throw new IndexOutOfBoundsException(
                    "Array Size=" + array.length + ", offset=" + offset + ", length=" + length);
        }

        if (text instanceof String) {
            final int count = Math.min(length, end - next);
            ((String) text).getChars(next, next + count, array, offset);
            next += count;
            return count;
        }
        if (text instanceof StringBuilder) {
            final int count = Math.min(length, end - next);
            ((StringBuilder) text).getChars(next, next + count, array, offset);
            next += count;
            return count;
        }
        if (text instanceof StringBuffer) {
            final int count = Math.min(length, end - next);
            ((StringBuffer) text).getChars(next, next + count, array, offset);
            next += count;
            return count;
        }

        int count = 0;
        for (int i = 0; i < length; i++) {
            final int c = read();
            if (c == Normal.__1) {
                return count;
            }
            array[offset + i] = (char) c;
            count++;
        }
        return count;
    }

    @Override
    public long skip(final long n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of characters to skip is less than zero: " + n);
        }
        if (next >= end) {
            return 0;
        }
        final int dest = (int) Math.min(end, next + n);
        final int count = dest - next;
        next = dest;
        return count;
    }

    @Override
    public boolean ready() {
        return next < end;
    }

    @Override
    public void close() {
        next = start;
        mark = start;
    }

    @Override
    public String toString() {
        return text.subSequence(start, end).toString();
    }

}
