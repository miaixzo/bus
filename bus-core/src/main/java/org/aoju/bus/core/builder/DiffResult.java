/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.builder;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * {@code DiffResult}包含两个{@link Diffable}对象之间差异的集合。
 * 通常，这些差异是使用{@link #toString()}方法显示的，
 * 该方法返回一个字符串，该字符串描述对象之间不同的字段.
 *
 * @author Kimi Liu
 * @version 5.3.5
 * @since JDK 1.8+
 */
public class DiffResult implements Iterable<Diff<?>> {

    public static final String OBJECTS_SAME_STRING = Normal.EMPTY;

    private static final String DIFFERS_STRING = "differs from";

    private final List<Diff<?>> diffs;
    private final Object lhs;
    private final Object rhs;
    private final ToStringStyle style;

    /**
     * 创建一个{@link DiffResult}，其中包含两个对象之间的差异
     *
     * @param lhs   左边对象
     * @param rhs   右边对象
     * @param diffs 列表中的差异，可能是空的
     * @param style 用于{@link #toString()}方法的样式。可能是{@code null}，
     *              在这种情况下使用{@link ToStringStyle#DEFAULT_STYLE}
     */
    DiffResult(final Object lhs, final Object rhs, final List<Diff<?>> diffs,
               final ToStringStyle style) {

        Assert.isTrue(lhs != null, "Left hand object cannot be null");
        Assert.isTrue(rhs != null, "Right hand object cannot be null");
        Assert.isTrue(diffs != null, "List of differences cannot be null");

        this.diffs = diffs;
        this.lhs = lhs;
        this.rhs = rhs;

        if (style == null) {
            this.style = ToStringStyle.DEFAULT_STYLE;
        } else {
            this.style = style;
        }
    }

    /**
     * 返回{@code Diff}的不可修改列表。如果对象之间没有差异，则列表可能为空
     *
     * @return 不可修改的{@code Diff}列表s
     */
    public List<Diff<?>> getDiffs() {
        return Collections.unmodifiableList(diffs);
    }

    public int getNumberOfDiffs() {
        return diffs.size();
    }

    public ToStringStyle getToStringStyle() {
        return style;
    }

    @Override
    public String toString() {
        return toString(style);
    }

    public String toString(final ToStringStyle style) {
        if (diffs.isEmpty()) {
            return OBJECTS_SAME_STRING;
        }

        final ToStringBuilder lhsBuilder = new ToStringBuilder(lhs, style);
        final ToStringBuilder rhsBuilder = new ToStringBuilder(rhs, style);

        for (final Diff<?> diff : diffs) {
            lhsBuilder.append(diff.getFieldName(), diff.getLeft());
            rhsBuilder.append(diff.getFieldName(), diff.getRight());
        }

        return String.format("%s %s %s", lhsBuilder.build(), DIFFERS_STRING,
                rhsBuilder.build());
    }

    @Override
    public Iterator<Diff<?>> iterator() {
        return diffs.iterator();
    }

}
