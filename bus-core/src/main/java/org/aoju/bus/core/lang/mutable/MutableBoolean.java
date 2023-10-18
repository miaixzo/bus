/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.lang.mutable;

/**
 * 可变 <code>boolean</code> 类型
 *
 * @author Kimi Liu
 * @see Boolean
 * @since Java 17+
 */
public class MutableBoolean implements Comparable<MutableBoolean>, Mutable<Boolean> {

    private boolean value;

    /**
     * 构造,默认值0
     */
    public MutableBoolean() {

    }

    /**
     * 构造
     *
     * @param value 值
     */
    public MutableBoolean(final boolean value) {
        this.value = value;
    }

    /**
     * 构造
     *
     * @param value String值
     * @throws NumberFormatException 转为Boolean错误
     */
    public MutableBoolean(final String value) throws NumberFormatException {
        this.value = Boolean.parseBoolean(value);
    }

    @Override
    public Boolean get() {
        return this.value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void set(final boolean value) {
        this.value = value;
    }

    @Override
    public void set(final Boolean value) {
        this.value = value;
    }

    /**
     * 相等需同时满足如下条件：
     * <ol>
     * <li>非空</li>
     * <li>类型为 {@link MutableBoolean}</li>
     * <li>值相等</li>
     * </ol>
     *
     * @param object 比对的对象
     * @return 相同返回<code>true</code>,否则 <code>false</code>
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof MutableBoolean) {
            return value == ((MutableBoolean) object).value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode();
    }

    /**
     * 比较
     *
     * @param other 其它 {@link MutableBoolean} 对象
     * @return x==y返回0,x&lt;y返回-1,x&gt;y返回1
     */
    @Override
    public int compareTo(final MutableBoolean other) {
        return Boolean.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
