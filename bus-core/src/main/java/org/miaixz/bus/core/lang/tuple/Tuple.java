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
package org.miaixz.bus.core.lang.tuple;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.lang.exception.CloneException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * 不可变数组类型（元组），用于多值返回 多值可以支持每个元素值类型不同
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Tuple implements Iterable<Object>, Serializable, Cloneable {

    private static final long serialVersionUID = -1L;

    /**
     * 多值信息
     */
    private final Object[] members;
    /**
     * 多值hash
     */
    private int hashCode;
    /**
     * 缓存hash
     */
    private boolean cacheHash;

    /**
     * 构造
     *
     * @param members 成员数组
     */
    public Tuple(final Object... members) {
        this.members = members;
    }

    /**
     * 构建Tuple对象
     *
     * @param members 成员数组
     * @return Tuple
     */
    public static Tuple of(final Object... members) {
        return new Tuple(members);
    }

    /**
     * 获取指定位置元素
     *
     * @param <T>   返回对象类型
     * @param index 位置
     * @return 元素
     */
    public <T> T get(final int index) {
        return (T) members[index];
    }

    /**
     * 获得所有元素
     *
     * @return 获得所有元素
     */
    public Object[] getMembers() {
        return this.members;
    }

    /**
     * 将元组转换成列表
     *
     * @return 转换得到的列表
     */
    public final List<Object> toList() {
        return ListKit.of(this.members);
    }

    /**
     * 缓存Hash值，当为true时，此对象的hash值只被计算一次，常用于Tuple中的值不变时使用。 注意：当为true时，member变更对象后，hash值不会变更。
     *
     * @param cacheHash 是否缓存hash值
     * @return this
     */
    public Tuple setCacheHash(final boolean cacheHash) {
        this.cacheHash = cacheHash;
        return this;
    }

    /**
     * 得到元组的大小
     *
     * @return 元组的大小
     */
    public int size() {
        return this.members.length;
    }

    /**
     * 判断元组中是否包含某元素
     *
     * @param value 需要判定的元素
     * @return 是否包含
     */
    public boolean contains(final Object value) {
        return ArrayKit.contains(this.members, value);
    }

    /**
     * 将元组转成流
     *
     * @return 流
     */
    public final Stream<Object> stream() {
        return Arrays.stream(this.members);
    }

    /**
     * 将元组转成并行流
     *
     * @return 流
     */
    public final Stream<Object> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * 截取元组指定部分
     *
     * @param start 起始位置（包括）
     * @param end   终止位置（不包括）
     * @return 截取得到的元组
     */
    public final Tuple sub(final int start, final int end) {
        return new Tuple(ArrayKit.sub(this.members, start, end));
    }

    @Override
    public int hashCode() {
        if (this.cacheHash && 0 != this.hashCode) {
            return this.hashCode;
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(members);
        if (this.cacheHash) {
            this.hashCode = result;
        }
        return result;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final Tuple other = (Tuple) object;
        return false != Arrays.deepEquals(members, other.members);
    }

    @Override
    public String toString() {
        return Arrays.toString(members);
    }

    @Override
    public Iterator<Object> iterator() {
        return new ArrayIterator<>(members);
    }

    @Override
    public final Spliterator<Object> spliterator() {
        return Spliterators.spliterator(this.members, Spliterator.ORDERED);
    }

    @Override
    public Tuple clone() {
        try {
            return (Tuple) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new CloneException(e);
        }
    }

}
