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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ClassKit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 协助实现{@link Differentable#diff(Object)}方法.
 *
 * <p>
 * 使用反射发现要差异的对象的所有非静态、非瞬态字段(包括继承字段)，并比较它们之间的差异.
 * </p>
 *
 * <pre>
 * public class Person implements Diffable&lt;Person&gt; {
 *   String name;
 *   int age;
 *   boolean smoker;
 *   ...
 *
 *   public DiffResult diff(Person object) {
 *     return new ReflectionDiffBuilder(this, object, ToStringStyle.SHORT_PREFIX_STYLE)
 *       .build();
 *   }
 * }
 * </pre>
 *
 * <p>
 * 传递给构造函数的{@code ToStringStyle}嵌入到返回的{@code DiffResult}中，
 * 并影响{@code DiffResult. tostring()}方法的风格。可以通过调用
 * {@link DifferentResult#toString(ToStringStyle)}覆盖此样式选择.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReflectionBuilder implements Builder<DifferentResult> {

    private final Object left;
    private final Object right;
    private final DifferentBuilder differentBuilder;

    /**
     * 使用指定样式为指定对象构造一个生成器
     *
     * <p>
     * 如果{@code lhs == rhs}或{@code lhs.equals(rhs)}，
     * 则构建器将不计算对{@code append(…)}的任何调用，
     * 并在{@link #build()}执行时返回一个空的{@link DifferentResult}.
     * </p>
     *
     * @param <T>   要区分的对象的类型
     * @param lhs   {@code this} 对象
     * @param rhs   反对的对象
     * @param style 当输出对象时将使用该样式，{@code null}使用默认值
     */
    public <T> ReflectionBuilder(final T lhs, final T rhs, final ToStringStyle style) {
        this.left = lhs;
        this.right = rhs;
        differentBuilder = new DifferentBuilder(lhs, rhs, style);
    }

    @Override
    public DifferentResult build() {
        if (left.equals(right)) {
            return differentBuilder.build();
        }

        appendFields(left.getClass());
        return differentBuilder.build();
    }

    private void appendFields(final Class<?> clazz) {
        for (final Field field : ClassKit.getAllFields(clazz)) {
            if (accept(field)) {
                try {
                    differentBuilder.append(field.getName(), ClassKit.readField(field, left, true),
                            ClassKit.readField(field, right, true));
                } catch (final IllegalAccessException ex) {
                    throw new InternalError("Unexpected IllegalAccessException: " + ex.getMessage());
                }
            }
        }
    }

    private boolean accept(final Field field) {
        if (field.getName().indexOf(Symbol.C_DOLLAR) != -1) {
            return false;
        }
        if (Modifier.isTransient(field.getModifiers())) {
            return false;
        }
        return !Modifier.isStatic(field.getModifiers());
    }

}
