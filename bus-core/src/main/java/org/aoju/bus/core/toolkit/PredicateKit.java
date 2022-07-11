/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.toolkit;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

/**
 * {@link Predicate}相关封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PredicateKit {

    /**
     * 多个条件转换为”与“复合条件，即所有条件都为true时，才返回true
     *
     * @param <T>        判断条件的对象类型
     * @param components 多个条件
     * @return 复合条件
     */
    public static <T> Predicate<T> and(final Predicate<? super T>... components) {
        return new AndPredicate<>(components);
    }

    /**
     * 多个条件转换为”与“复合条件，即所有条件都为true时，才返回true
     *
     * @param <T>        判断条件的对象类型
     * @param components 多个条件
     * @return 复合条件
     */
    public static <T> Predicate<T> and(final Iterable<? extends Predicate<? super T>> components) {
        return new AndPredicate<>(CollKit.of(components));
    }

    /**
     * 多个条件转换为”或“复合条件，即任意一个条件都为true时，返回true
     *
     * @param <T>        判断条件的对象类型
     * @param components 多个条件
     * @return 复合条件
     */
    public static <T> Predicate<T> or(final Predicate<? super T>... components) {
        return new OrPredicate<>(components);
    }

    /**
     * 多个条件转换为”或“复合条件，即任意一个条件都为true时，返回true
     *
     * @param <T>        判断条件的对象类型
     * @param components 多个条件
     * @return 复合条件
     */
    public static <T> Predicate<T> or(final Iterable<? extends Predicate<? super T>> components) {
        return new OrPredicate<>(CollKit.of(components));
    }

    public static <T> Predicate<T> negate(final Predicate<T> predicate) {
        return predicate.negate();
    }

    /**
     * 多个{@link Predicate}的与，只有所有条件满足才为true，当任意一个条件的test为false，返回false
     *
     * @param <T> 泛型类型
     */
    private static class AndPredicate<T> implements Predicate<T>, Serializable {

        private static final long serialVersionUID = 1L;

        private final List<? extends Predicate<? super T>> components;

        @SafeVarargs
        private AndPredicate(final Predicate<? super T>... components) {
            this.components = CollKit.of(components);
        }

        private AndPredicate(final List<? extends Predicate<? super T>> components) {
            this.components = components;
        }

        @Override
        public boolean test(final T t) {
            for (int i = 0; i < components.size(); i++) {
                if (false == components.get(i).test(t)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "Predicates.and(" + ArrayKit.join(",", this.components) + ")";
        }
    }

    /**
     * 多个{@link Predicate}的或操作，即当一个条件满足，则全部满足，当任意一个条件的test为true，返回true
     *
     * @param <T> 泛型类型
     */
    private static class OrPredicate<T> implements Predicate<T>, Serializable {

        private static final long serialVersionUID = 1L;

        private final List<? extends Predicate<? super T>> components;

        @SafeVarargs
        private OrPredicate(final Predicate<? super T>... components) {
            this.components = CollKit.of(components);
        }

        private OrPredicate(final List<? extends Predicate<? super T>> components) {
            this.components = components;
        }

        @Override
        public boolean test(final T t) {
            for (int i = 0; i < components.size(); i++) {
                if (false == components.get(i).test(t)) {
                    return true;
                }
            }
            return false;
        }
    }

}
