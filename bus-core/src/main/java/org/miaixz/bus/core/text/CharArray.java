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
package org.miaixz.bus.core.text;

import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.xyz.ArrayKit;

import java.util.Arrays;
import java.util.Iterator;

/**
 * char[]包装，提供zero-copy的数组操作
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharArray implements CharSequence, Iterable<Character> {

    private final char[] value;

    /**
     * 构造
     *
     * @param value String值
     */
    public CharArray(final String value) {
        this(value.toCharArray(), false);
    }

    /**
     * 构造，注意此方法共享数组
     *
     * @param value char数组
     * @param copy  可选是否拷贝数组，如果为{@code false}则复用数组
     */
    public CharArray(final char[] value, final boolean copy) {
        this.value = copy ? value.clone() : value;
    }

    @Override
    public int length() {
        return value.length;
    }

    @Override
    public char charAt(int index) {
        if (index < 0) {
            index += value.length;
        }
        return value[index];
    }

    /**
     * 设置字符
     *
     * @param index 位置，支持复数，-1表示最后一个位置
     * @param c     字符
     * @return this
     */
    public CharArray set(int index, final char c) {
        if (index < 0) {
            index += value.length;
        }
        value[index] = c;
        return this;
    }

    /**
     * 获取原始数组，不做拷贝
     *
     * @return array
     */
    public char[] array() {
        return this.value;
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return new CharArray(ArrayKit.sub(value, start, end), false);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CharArray charArray = (CharArray) o;
        return Arrays.equals(value, charArray.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public Iterator<Character> iterator() {
        return new ArrayIterator<>(this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

}
