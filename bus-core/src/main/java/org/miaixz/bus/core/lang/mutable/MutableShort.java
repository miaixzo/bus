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
package org.miaixz.bus.core.lang.mutable;

import org.miaixz.bus.core.xyz.CompareKit;

/**
 * 可变 {@code short} 类型
 *
 * @author Kimi Liu
 * @see Short
 * @since Java 17+
 */
public class MutableShort extends Number implements Comparable<MutableShort>, Mutable<Number> {

    private static final long serialVersionUID = -1L;

    private short value;

    /**
     * 构造，默认值0
     */
    public MutableShort() {

    }

    /**
     * 构造
     *
     * @param value 值
     */
    public MutableShort(final short value) {
        this.value = value;
    }

    /**
     * 构造
     *
     * @param value 值
     */
    public MutableShort(final Number value) {
        this(value.shortValue());
    }

    /**
     * 构造
     *
     * @param value String值
     * @throws NumberFormatException 转为Short错误
     */
    public MutableShort(final String value) throws NumberFormatException {
        this.value = Short.parseShort(value);
    }

    @Override
    public Short get() {
        return this.value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void set(final short value) {
        this.value = value;
    }

    @Override
    public void set(final Number value) {
        this.value = value.shortValue();
    }

    /**
     * 值+1
     *
     * @return this
     */
    public MutableShort increment() {
        value++;
        return this;
    }

    /**
     * 值减一
     *
     * @return this
     */
    public MutableShort decrement() {
        value--;
        return this;
    }

    /**
     * 增加值
     *
     * @param operand 被增加的值
     * @return this
     */
    public MutableShort add(final short operand) {
        this.value += operand;
        return this;
    }

    /**
     * 增加值
     *
     * @param operand 被增加的值，非空
     * @return this
     * @throws NullPointerException if the object is null
     */
    public MutableShort add(final Number operand) {
        this.value += operand.shortValue();
        return this;
    }

    /**
     * 减去值
     *
     * @param operand 被减的值
     * @return this
     */
    public MutableShort subtract(final short operand) {
        this.value -= operand;
        return this;
    }

    /**
     * 减去值
     *
     * @param operand 被减的值，非空
     * @return this
     * @throws NullPointerException if the object is null
     */
    public MutableShort subtract(final Number operand) {
        this.value -= operand.shortValue();
        return this;
    }

    @Override
    public short shortValue() {
        return value;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * 相等需同时满足如下条件：
     * <ol>
     * <li>非空</li>
     * <li>类型为 {@code MutableShort}</li>
     * <li>值相等</li>
     * </ol>
     *
     * @param object 比对的对象
     * @return 相同返回<code>true</code>，否则 {@code false}
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof MutableShort) {
            return value == ((MutableShort) object).shortValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value;
    }

    /**
     * 比较
     *
     * @param other 其它 {@code MutableShort} 对象
     * @return x==y返回0，x&lt;y返回-1，x&gt;y返回1
     */
    @Override
    public int compareTo(final MutableShort other) {
        return CompareKit.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
