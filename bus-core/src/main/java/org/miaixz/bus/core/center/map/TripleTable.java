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
package org.miaixz.bus.core.center.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * 三值表结构，可重复 用于提供三种值相互查找操作 查找方式为indexOf方式遍历查找，数据越多越慢。
 *
 * @param <L> 左值类型
 * @param <M> 中值类型
 * @param <R> 右值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class TripleTable<L, M, R> implements Serializable {

    private static final long serialVersionUID = -1L;

    private final List<L> lefts;
    private final List<M> middles;
    private final List<R> rights;

    /**
     * 构造
     *
     * @param triples 三元组列表
     */
    public TripleTable(final List<Triplet<L, M, R>> triples) {
        this(Assert.notNull(triples).size());
        for (final Triplet<L, M, R> triplet : triples) {
            put(triplet.getLeft(), triplet.getMiddle(), triplet.getRight());
        }
    }

    /**
     * 构造
     *
     * @param size 初始容量
     */
    public TripleTable(final int size) {
        this(new ArrayList<>(size), new ArrayList<>(size), new ArrayList<>(size));
    }

    /**
     * @param lefts   左列表
     * @param middles 中列表
     * @param rights  右列表
     */
    public TripleTable(final List<L> lefts, final List<M> middles, final List<R> rights) {
        Assert.notNull(lefts);
        Assert.notNull(middles);
        Assert.notNull(rights);
        final int size = lefts.size();
        if (size != middles.size() || size != rights.size()) {
            throw new IllegalArgumentException("List size must be equals!");
        }

        this.lefts = lefts;
        this.middles = middles;
        this.rights = rights;
    }

    /**
     * 通过中间值，查找左边值 如果有多个重复值，只返回找到的第一个值
     *
     * @param mValue 中间值
     * @return 左边值，未找到返回{@code null}
     */
    public L getLeftByMiddle(final M mValue) {
        final int index = this.middles.indexOf(mValue);
        if (index > -1) {
            return this.lefts.get(index);
        }
        return null;
    }

    /**
     * 通过右值，查找左边值 如果有多个重复值，只返回找到的第一个值
     *
     * @param rValue 右值
     * @return 左边值，未找到返回{@code null}
     */
    public L getLeftByRight(final R rValue) {
        final int index = this.rights.indexOf(rValue);
        if (index > -1) {
            return this.lefts.get(index);
        }
        return null;
    }

    /**
     * 通过左值，查找中值 如果有多个重复值，只返回找到的第一个值
     *
     * @param lValue 左值
     * @return 中值，未找到返回{@code null}
     */
    public M getMiddleByLeft(final L lValue) {
        final int index = this.lefts.indexOf(lValue);
        if (index > -1) {
            return this.middles.get(index);
        }
        return null;
    }

    /**
     * 通过右值，查找中值 如果有多个重复值，只返回找到的第一个值
     *
     * @param rValue 右值
     * @return 中值，未找到返回{@code null}
     */
    public M getMiddleByRight(final R rValue) {
        final int index = this.rights.indexOf(rValue);
        if (index > -1) {
            return this.middles.get(index);
        }
        return null;
    }

    /**
     * 获取指定index对应的左值
     *
     * @param index 索引
     * @return 左值
     */
    public L getLeft(final int index) {
        return this.lefts.get(index);
    }

    /**
     * 获取指定index对应的中值
     *
     * @param index 索引
     * @return 中值
     */
    public M getMiddle(final int index) {
        return this.middles.get(index);
    }

    /**
     * 获取指定index对应的右值
     *
     * @param index 索引
     * @return 右值
     */
    public R getRight(final int index) {
        return this.rights.get(index);
    }

    /**
     * 通过左值，查找右值 如果有多个重复值，只返回找到的第一个值
     *
     * @param lValue 左值
     * @return 右值，未找到返回{@code null}
     */
    public R getRightByLeft(final L lValue) {
        final int index = this.lefts.indexOf(lValue);
        if (index > -1) {
            return this.rights.get(index);
        }
        return null;
    }

    /**
     * 通过中间值，查找右值 如果有多个重复值，只返回找到的第一个值
     *
     * @param mValue 中间值
     * @return 右值，未找到返回{@code null}
     */
    public R getRightByMiddle(final M mValue) {
        final int index = this.middles.indexOf(mValue);
        if (index > -1) {
            return this.rights.get(index);
        }
        return null;
    }

    /**
     * 是否含有指定左元素
     *
     * @param left 左元素
     * @return 是否含有
     */
    public boolean containLeft(final L left) {
        return this.lefts.contains(left);
    }

    /**
     * 是否含有指定中元素
     *
     * @param middle 中元素
     * @return 是否含有
     */
    public boolean containMiddle(final M middle) {
        return this.middles.contains(middle);
    }

    /**
     * 是否含有指定右元素
     *
     * @param right 右元素
     * @return 是否含有
     */
    public boolean containRight(final R right) {
        return this.rights.contains(right);
    }

    /**
     * 获取指定左元素的索引
     *
     * @param left 左元素
     * @return 索引，未找到返回-1
     */
    public int indexOfLeft(final L left) {
        return this.lefts.indexOf(left);
    }

    /**
     * 获取指定中元素的索引
     *
     * @param middle 中元素
     * @return 索引，未找到返回-1
     */
    public int indexOfMiddle(final M middle) {
        return this.middles.indexOf(middle);
    }

    /**
     * 获取指定右元素的索引
     *
     * @param right 右元素
     * @return 索引，未找到返回-1
     */
    public int indexOfRight(final R right) {
        return this.rights.indexOf(right);
    }

    /**
     * 通过左值查找三元组（所有值）
     *
     * @param lValue 左值
     * @return 三元组（所有值）
     */
    public Triplet<L, M, R> getByLeft(final L lValue) {
        final int index = this.lefts.indexOf(lValue);
        if (index > -1) {
            return new Triplet<>(lefts.get(index), middles.get(index), rights.get(index));
        }
        return null;
    }

    /**
     * 通过中值查找三元组（所有值）
     *
     * @param mValue 中值
     * @return 三元组（所有值）
     */
    public Triplet<L, M, R> getByMiddle(final M mValue) {
        final int index = this.middles.indexOf(mValue);
        if (index > -1) {
            return new Triplet<>(lefts.get(index), middles.get(index), rights.get(index));
        }
        return null;
    }

    /**
     * 通过右值查找三元组（所有值）
     *
     * @param rValue 右值
     * @return 三元组（所有值）
     */
    public Triplet<L, M, R> getByRight(final R rValue) {
        final int index = this.rights.indexOf(rValue);
        if (index > -1) {
            return new Triplet<>(lefts.get(index), middles.get(index), rights.get(index));
        }
        return null;
    }

    /**
     * 获取左列表，不可修改
     *
     * @return 左列表
     */
    public List<L> getLefts() {
        return ListKit.view(this.lefts);
    }

    /**
     * 获取中列表，不可修改
     *
     * @return 中列表
     */
    public List<M> getMiddles() {
        return ListKit.view(this.middles);
    }

    /**
     * 获取右列表，不可修改
     *
     * @return 右列表
     */
    public List<R> getRights() {
        return ListKit.view(this.rights);
    }

    /**
     * 长度
     *
     * @return this
     */
    public int size() {
        return this.lefts.size();
    }

    /**
     * 加入值
     *
     * @param lValue 左值
     * @param mValue 中值
     * @param rValue 右值
     * @return this
     */
    public TripleTable<L, M, R> put(final L lValue, final M mValue, final R rValue) {
        this.lefts.add(lValue);
        this.middles.add(mValue);
        this.rights.add(rValue);
        return this;
    }

    /**
     * 修改指定index对应的左值
     *
     * @param index  索引
     * @param lValue 左值
     * @return this
     */
    public TripleTable<L, M, R> setLeft(final int index, final L lValue) {
        this.lefts.set(index, lValue);
        return this;
    }

    /**
     * 修改指定index对应的中值
     *
     * @param index  索引
     * @param mValue 中值
     * @return this
     */
    public TripleTable<L, M, R> setMiddle(final int index, final M mValue) {
        this.middles.set(index, mValue);
        return this;
    }

    /**
     * 修改指定index对应的右值
     *
     * @param index  索引
     * @param rValue 左值
     * @return this
     */
    public TripleTable<L, M, R> setRight(final int index, final R rValue) {
        this.rights.set(index, rValue);
        return this;
    }

    /**
     * 清空
     *
     * @return this
     */
    public TripleTable<L, M, R> clear() {
        this.lefts.clear();
        this.middles.clear();
        this.rights.clear();
        return this;
    }

    /**
     * 移除值
     *
     * @param index 序号
     * @return this
     */
    public TripleTable<L, M, R> remove(final int index) {
        this.lefts.remove(index);
        this.middles.remove(index);
        this.rights.remove(index);
        return this;
    }

}
