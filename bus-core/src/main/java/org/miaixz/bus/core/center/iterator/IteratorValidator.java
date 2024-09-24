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
package org.miaixz.bus.core.center.iterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.CollectionValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Iterator检查工具类，提供字对象的blank和empty等检查
 * <ul>
 * <li>empty定义：{@code null} or 空字对象：{@code ""}</li>
 * <li>blank定义：{@code null} or 空字对象：{@code ""} or 空格、全角空格、制表符、换行符，等不可见字符</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IteratorValidator {

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     */
    public static boolean isEmpty(final Iterable<?> iterable) {
        return null == iterable || isEmpty(iterable.iterator());
    }

    /**
     * Iterator是否为空
     *
     * @param iterator Iterator对象
     * @return 是否为空
     */
    public static boolean isEmpty(final Iterator<?> iterator) {
        return null == iterator || !iterator.hasNext();
    }

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     */
    public static boolean isNotEmpty(final Iterable<?> iterable) {
        return null != iterable && isNotEmpty(iterable.iterator());
    }

    /**
     * Iterator是否为空
     *
     * @param iterator Iterator对象
     * @return 是否为空
     */
    public static boolean isNotEmpty(final Iterator<?> iterator) {
        return null != iterator && iterator.hasNext();
    }

    /**
     * 是否包含{@code null}元素
     * <ul>
     * <li>Iterator为{@code null}，返回{@code true}</li>
     * <li>Iterator为空集合，即元素个数为0，返回{@code false}</li>
     * <li>Iterator中元素为""，返回{@code false}</li>
     * </ul>
     *
     * @param iter 被检查的{@link Iterator}对象，如果为{@code null} 返回true
     * @return 是否包含{@code null}元素
     */
    public static boolean hasNull(final Iterator<?> iter) {
        if (null == iter) {
            return true;
        }
        while (iter.hasNext()) {
            if (null == iter.next()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否全部元素为null
     *
     * @param iter iterator 被检查的{@link Iterable}对象，如果为{@code null} 返回true
     * @return 是否全部元素为null
     */
    public static boolean isAllNull(final Iterable<?> iter) {
        return isAllNull(null == iter ? null : iter.iterator());
    }

    /**
     * 是否全部元素为null
     *
     * @param iter iterator 被检查的{@link Iterator}对象，如果为{@code null} 返回true
     * @return 是否全部元素为null
     */
    public static boolean isAllNull(final Iterator<?> iter) {
        return null == getFirstNoneNull(iter);
    }

    /**
     * 指定字符串集合中，是否包含空字符串。
     *
     * @param args 字符串列表
     * @return 批量判断字符串是否全部为空白
     */
    public static boolean hasBlank(final Iterable<? extends CharSequence> args) {
        if (CollectionValidator.isEmpty(args)) {
            return true;
        }
        for (final CharSequence text : args) {
            if (StringKit.isBlank(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param args 字符串列表
     * @return 批量判断字符串是否全部为空白
     */
    public static boolean isAllBlank(final Iterable<? extends CharSequence> args) {
        if (CollectionValidator.isNotEmpty(args)) {
            for (final CharSequence text : args) {
                if (StringKit.isNotBlank(text)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断subIter是否为iter的子集合，不考虑顺序，只考虑元素数量。
     * <ul>
     * <li>如果两个集合为同一集合或，则返回true</li>
     * <li>如果两个集合元素都相同，则返回true（无论顺序相同与否）</li>
     * </ul>
     *
     * @param subIter 第一个Iterable对象，即子集合。
     * @param iter    第二个Iterable对象，可以为任何实现了Iterable接口的集合。
     * @return 如果subIter是iter的子集合，则返回true；否则返回false。
     */
    public static boolean isSub(final Iterable<?> subIter, final Iterable<?> iter) {
        // 如果两个Iterable对象引用相同，则肯定是一个的子集合
        if (subIter == iter) {
            return true;
        }
        // 如果有任何一个Iterable对象为null，则不是子集合关系
        if (subIter == null || iter == null) {
            return false;
        }

        // 使用Map记录每个Iterable中每个元素出现的次数
        final Map<?, Integer> countMap1 = countMap(subIter.iterator());
        final Map<?, Integer> countMap2 = countMap(iter.iterator());

        // 遍历第一个Iterable中的每个元素
        for (final Object object : subIter) {
            // 比较第一个Iterable中元素的出现次数和第二个Iterable中元素的出现次数
            // 如果第一个Iterable中元素的出现次数大于第二个Iterable中元素的出现次数，则不是子集合关系
            if (MathKit.nullToZero(countMap1.get(object)) > MathKit.nullToZero(countMap2.get(object))) {
                return false;
            }
        }
        // 如果所有元素的出现次数比较都满足子集合关系，则返回true
        return true;
    }

    /**
     * 判断两个{@link Iterable}中的元素与其顺序是否相同 当满足下列情况时返回{@code true}：
     * <ul>
     * <li>两个{@link Iterable}都为{@code null}；</li>
     * <li>两个{@link Iterable}满足{@code iterable1 == iterable2}；</li>
     * <li>两个{@link Iterable}所有具有相同下标的元素皆满足{@link Objects#equals(Object, Object)}；</li>
     * </ul>
     * 此方法来自Apache-Commons-Collections4。
     *
     * @param iterable1 列表1
     * @param iterable2 列表2
     * @return 是否相同
     */
    public static boolean isEqualList(final Iterable<?> iterable1, final Iterable<?> iterable2) {
        return equals(iterable1, iterable2, false);
    }

    /**
     * 判断两个{@link Iterable}中的元素是否相同，可选是否判断顺序 当满足下列情况时返回{@code true}：
     * <ul>
     * <li>两个{@link Iterable}都为{@code null}；</li>
     * <li>两个{@link Iterable}满足{@code iterable1 == iterable2}；</li>
     * <li>如果忽略顺序，则计算两个集合中元素和数量是否相同</li>
     * <li>如果不忽略顺序，两个{@link Iterable}所有具有相同下标的元素皆满足{@link Objects#equals(Object, Object)}；</li>
     * </ul>
     *
     * @param iterable1   集合1
     * @param iterable2   集合2
     * @param ignoreOrder 是否忽略顺序
     * @return 是否相同
     */
    public static boolean equals(final Iterable<?> iterable1, final Iterable<?> iterable2, final boolean ignoreOrder) {
        // 如果两个Iterable对象引用相同，则肯定相等
        if (iterable1 == iterable2) {
            return true;
        }
        // 如果有任何一个Iterable对象为null，则不是子集合关系
        if (iterable1 == null || iterable2 == null) {
            return false;
        }

        if (ignoreOrder) {
            final Map<?, Integer> countMap1 = countMap(iterable1.iterator());
            final Map<?, Integer> countMap2 = countMap(iterable2.iterator());

            if (countMap1.size() != countMap2.size()) {
                // 如果两个Iterable中元素种类不同，则肯定不等
                return false;
            }

            for (final Object object : iterable1) {
                // 比较第一个Iterable中元素的出现次数和第二个Iterable中元素的出现次数
                if (MathKit.nullToZero(countMap1.get(object)) != MathKit.nullToZero(countMap2.get(object))) {
                    return false;
                }
            }
            // 如果所有元素的出现次数比较都满足子集合关系，则返回true
            return true;
        } else {
            final Iterator<?> iter1 = iterable1.iterator();
            final Iterator<?> iter2 = iterable2.iterator();
            Object obj1;
            Object obj2;
            while (iter1.hasNext() && iter2.hasNext()) {
                obj1 = iter1.next();
                obj2 = iter2.next();
                if (!Objects.equals(obj1, obj2)) {
                    return false;
                }
            }
            // 当两个Iterable长度不一致时返回false
            return !(iter1.hasNext() || iter2.hasNext());
        }
    }

    /**
     * 遍历{@link Iterator}，获取指定index位置的元素
     *
     * @param iterator {@link Iterator}
     * @param index    位置
     * @param <E>      元素类型
     * @return 元素，找不到元素返回{@code null}
     * @throws IndexOutOfBoundsException index &lt; 0时报错
     */
    public static <E> E get(final Iterator<E> iterator, int index) throws IndexOutOfBoundsException {
        if (null == iterator) {
            return null;
        }
        Assert.isTrue(index >= 0, "[index] must be >= 0");
        while (iterator.hasNext()) {
            index--;
            if (-1 == index) {
                return iterator.next();
            }
            iterator.next();
        }
        return null;
    }

    /**
     * 获取集合的第一个元素
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @return 第一个元素
     */
    public static <T> T getFirst(final Iterator<T> iterator) {
        return get(iterator, 0);
    }

    /**
     * 返回{@link Iterator}中第一个匹配规则的值
     *
     * @param <T>       数组元素类型
     * @param iterator  {@link Iterator}
     * @param predicate 匹配接口，实现此接口自定义匹配规则
     * @return 匹配元素，如果不存在匹配元素或{@link Iterator}为空，返回 {@code null}
     */
    public static <T> T getFirst(final Iterator<T> iterator, final Predicate<T> predicate) {
        Assert.notNull(predicate, "Matcher must be not null !");
        if (null != iterator) {
            while (iterator.hasNext()) {
                final T next = iterator.next();
                if (predicate.test(next)) {
                    return next;
                }
            }
        }
        return null;
    }

    /**
     * 获取集合的第一个非空元素
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @return 第一个非空元素，null表示未找到
     */
    public static <T> T getFirstNoneNull(final Iterator<T> iterator) {
        return getFirst(iterator, Objects::nonNull);
    }

    /**
     * 根据集合返回一个元素计数的 {@link Map} 所谓元素计数就是假如这个集合中某个元素出现了n次，那将这个元素做为key，n做为value 例如：[a,b,c,c,c] 得到： a: 1 b: 1 c: 3
     *
     * @param <T>  集合元素类型
     * @param iter {@link Iterator}，如果为null返回一个空的Map
     * @return {@link Map}
     */
    public static <T> Map<T, Integer> countMap(final Iterator<T> iter) {
        final Map<T, Integer> countMap = new HashMap<>();
        if (null != iter) {
            while (iter.hasNext()) {
                countMap.merge(iter.next(), 1, Integer::sum);
            }
        }
        return countMap;
    }

}
