/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.builder;

import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.TypeKit;

import java.lang.reflect.Type;

/**
 * {@code Diff}包含两个{@link Differentable}类字段之间的差异
 * 通常，{@code Diff}通过使用{@link DifferentBuilder}来生成
 * {@link DifferentResult}来检索，其中包含两个对象之间的差异.
 *
 * @param <T> 这个{@code Diff}中包含的对象类型.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Different<T> extends Pair<T, T> {

    /**
     * 字段类型
     */
    private final Type type;
    /**
     * 字段名称
     */
    private final String fieldName;

    /**
     * 为给定的字段名构造一个新的{@code Diff}
     *
     * @param fieldName 字段的名称
     */
    protected Different(final String fieldName) {
        this.type = ObjectKit.defaultIfNull(
                TypeKit.getTypeArguments(getClass(), Different.class).get(
                        Different.class.getTypeParameters()[0]), Object.class);
        this.fieldName = fieldName;
    }

    public final Type getType() {
        return type;
    }

    public final String getFieldName() {
        return fieldName;
    }

    @Override
    public final String toString() {
        return String.format("[%s: %s, %s]", fieldName, getLeft(), getRight());
    }

    @Override
    public final T setValue(final T value) {
        throw new UnsupportedOperationException("Cannot alter Diff object.");
    }

}
