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
package org.miaixz.bus.core.lang.selector;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

import org.miaixz.bus.core.xyz.CollKit;

/**
 * 权重随机选择算法实现
 * <p>
 * 平时，经常会遇到权重随机算法，从不同权重的N个元素中随机选择一个，并使得总体选择结果是按照权重分布的。如广告投放、负载均衡等。
 * </p>
 * <p>
 * 如有4个元素A、B、C、D，权重分别为1、2、3、4，随机结果中A:B:C:D的比例要为1:2:3:4。
 * </p>
 * 总体思路：累加每个元素的权重A(1)-B(3)-C(6)-D(10)，则4个元素的的权重管辖区间分别为[0,1)、[1,3)、[3,6)、[6,10)。
 * 然后随机出一个[0,10)之间的随机数。落在哪个区间，则该区间之后的元素即为按权重命中的元素。
 *
 * <p>
 * 参考博客：https://www.cnblogs.com/waterystone/p/5708063.html
 * </p>
 *
 * @param <T> 权重随机获取的对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeightRandomSelector<T> implements Selector<T>, Serializable {

    private static final long serialVersionUID = -1L;

    private final TreeMap<Integer, T> weightMap;

    /**
     * 构造
     */
    public WeightRandomSelector() {
        weightMap = new TreeMap<>();
    }

    /**
     * 构造
     *
     * @param weightObj 带有权重的对象
     */
    public WeightRandomSelector(final WeightObject<T> weightObj) {
        this();
        if (null != weightObj) {
            add(weightObj);
        }
    }

    /**
     * 构造
     *
     * @param weightObjs 带有权重的对象
     */
    public WeightRandomSelector(final Iterable<WeightObject<T>> weightObjs) {
        this();
        if (CollKit.isNotEmpty(weightObjs)) {
            for (final WeightObject<T> weightObj : weightObjs) {
                add(weightObj);
            }
        }
    }

    /**
     * 构造
     *
     * @param weightObjs 带有权重的对象
     */
    public WeightRandomSelector(final WeightObject<T>[] weightObjs) {
        this();
        for (final WeightObject<T> weightObj : weightObjs) {
            add(weightObj);
        }
    }

    /**
     * 创建权重随机获取器
     *
     * @param <T> 权重随机获取的对象类型
     * @return WeightRandomSelector
     */
    public static <T> WeightRandomSelector<T> of() {
        return new WeightRandomSelector<>();
    }

    /**
     * 增加对象
     *
     * @param object 对象
     * @param weight 权重
     * @return this
     */
    public WeightRandomSelector<T> add(final T object, final int weight) {
        return add(new WeightObject<>(object, weight));
    }

    /**
     * 增加对象权重
     *
     * @param weightObj 权重对象
     * @return this
     */
    public WeightRandomSelector<T> add(final WeightObject<T> weightObj) {
        if (null != weightObj) {
            final int weight = weightObj.getWeight();
            if (weight > 0) {
                final int lastWeight = this.weightMap.isEmpty() ? 0 : this.weightMap.lastKey();
                this.weightMap.put(weight + lastWeight, weightObj.getObject());// 权重累加
            }
        }
        return this;
    }

    /**
     * 清空权重表
     *
     * @return this
     */
    public WeightRandomSelector<T> clear() {
        if (null != this.weightMap) {
            this.weightMap.clear();
        }
        return this;
    }

    /**
     * 下一个随机对象
     *
     * @return 随机对象
     */
    @Override
    public T select() {
        final int randomWeight = (int) (this.weightMap.lastKey() * Math.random());
        final SortedMap<Integer, T> tailMap = this.weightMap.tailMap(randomWeight, false);
        return this.weightMap.get(tailMap.firstKey());
    }

}
