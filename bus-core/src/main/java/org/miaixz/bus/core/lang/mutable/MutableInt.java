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
package org.miaixz.bus.core.lang.mutable;

import java.io.Serial;

import org.miaixz.bus.core.xyz.CompareKit;

/**
 * 可变 {@code int} 类型
 *
 * @author Kimi Liu
 * @see Integer
 * @since Java 17+
 */
public class MutableInt extends Number implements Comparable<MutableInt>, Mutable<Number> {

    @Serial
    private static final long serialVersionUID = 2852270595187L;

    private int value;

    /**
     * 构造，默认值0
     */
    public MutableInt() {

    }

    /**
     * 构造
     *
     * @param value 值
     */
    public MutableInt(final int value) {
        this.value = value;
    }

    /**
     * 构造
     *
     * @param value 值
     */
    public MutableInt(final Number value) {
        this(value.intValue());
    }

    /**
     * 构造
     *
     * @param value String值
     * @throws NumberFormatException 数字转换错误
     */
    public MutableInt(final String value) throws NumberFormatException {
        this.value = Integer.parseInt(value);
    }

    @Override
    public Integer get() {
        return this.value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void set(final int value) {
        this.value = value;
    }

    @Override
    public void set(final Number value) {
        this.value = value.intValue();
    }

    /**
     * 值+1
     *
     * @return this
     */
    public MutableInt increment() {
        value++;
        return this;
    }

    /**
     * 值减一
     *
     * @return this
     */
    public MutableInt decrement() {
        value--;
        return this;
    }

    /**
     * 先加1, 再获取值
     *
     * @return +1后的值
     */
    public int incrementAndGet() {
        return ++value;
    }

    /**
     * 先获取原来的值, 再加1
     *
     * @return 原始值
     */
    public int getAndIncrement() {
        return value++;
    }

    /**
     * 先减1, 再获取值
     *
     * @return -1后的值
     */
    public int decrementAndGet() {
        return --value;
    }

    /**
     * 先获取原来的值, 再减1
     *
     * @return 原始值
     */
    public int getAndDecrement() {
        return value--;
    }

    /**
     * 增加值
     *
     * @param operand 被增加的值
     * @return this
     */
    public MutableInt add(final int operand) {
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
    public MutableInt add(final Number operand) {
        this.value += operand.intValue();
        return this;
    }

    /**
     * 减去值
     *
     * @param operand 被减的值
     * @return this
     */
    public MutableInt subtract(final int operand) {
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
    public MutableInt subtract(final Number operand) {
        this.value -= operand.intValue();
        return this;
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
     * <li>类型为 MutableInt</li>
     * <li>值相等</li>
     * </ol>
     *
     * @param object 比对的对象
     * @return 相同返回<code>true</code>，否则 {@code false}
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof MutableInt) {
            return value == ((MutableInt) object).intValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    /**
     * 比较
     *
     * @param other 其它 MutableInt 对象
     * @return x==y返回0，x&lt;y返回-1，x&gt;y返回1
     */
    @Override
    public int compareTo(final MutableInt other) {
        return CompareKit.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
