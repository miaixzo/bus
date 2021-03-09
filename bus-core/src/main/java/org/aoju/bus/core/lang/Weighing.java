/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lang;

import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.MapKit;
import org.aoju.bus.core.toolkit.RandomKit;

import java.io.Serializable;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 权重随机算法实现
 * <p>
 * 平时,经常会遇到权重随机算法,从不同权重的N个元素中随机选择一个,并使得总体选择结果是按照权重分布的 如广告投放、负载均衡等
 * </p>
 * <p>
 * 如有4个元素A、B、C、D,权重分别为1、2、3、4,随机结果中A:B:C:D的比例要为1:2:3:4
 * </p>
 * 总体思路：累加每个元素的权重A(1)-B(3)-C(6)-D(10),则4个元素的的权重管辖区间分别为[0,1)、[1,3)、[3,6)、[6,10)
 * 然后随机出一个[0,10)之间的随机数 落在哪个区间,则该区间之后的元素即为按权重命中的元素
 *
 * @param <T> 权重随机获取的对象类型
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public class Weighing<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TreeMap<Double, T> weightMap;

    /**
     * 构造
     */
    public Weighing() {
        weightMap = new TreeMap<>();
    }

    /**
     * 构造
     *
     * @param weightObj 带有权重的对象
     */
    public Weighing(WeightObj<T> weightObj) {
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
    public Weighing(Iterable<WeightObj<T>> weightObjs) {
        this();
        if (CollKit.isNotEmpty(weightObjs)) {
            for (WeightObj<T> weightObj : weightObjs) {
                add(weightObj);
            }
        }
    }

    /**
     * 构造
     *
     * @param weightObjs 带有权重的对象
     */
    public Weighing(WeightObj<T>[] weightObjs) {
        this();
        for (WeightObj<T> weightObj : weightObjs) {
            add(weightObj);
        }
    }

    /**
     * 创建权重随机获取器
     *
     * @param <T> 对象
     * @return {@link Weighing}
     */
    public static <T> Weighing<T> create() {
        return new Weighing<>();
    }

    /**
     * 增加对象
     *
     * @param obj    对象
     * @param weight 权重
     * @return this
     */
    public Weighing<T> add(T obj, double weight) {
        return add(new WeightObj<>(obj, weight));
    }

    /**
     * 增加对象权重
     *
     * @param weightObj 权重对象
     * @return this
     */
    public Weighing<T> add(WeightObj<T> weightObj) {
        double lastWeight = (this.weightMap.size() == 0) ? 0 : this.weightMap.lastKey();
        this.weightMap.put(weightObj.getWeight() + lastWeight, weightObj.getObj());// 权重累加
        return this;
    }

    /**
     * 清空权重表
     *
     * @return this
     */
    public Weighing<T> clear() {
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
    public T next() {
        if (MapKit.isEmpty(this.weightMap)) {
            return null;
        }
        final Random random = RandomKit.getRandom();
        double randomWeight = this.weightMap.lastKey() * random.nextDouble();
        final SortedMap<Double, T> tailMap = this.weightMap.tailMap(randomWeight, false);
        return this.weightMap.get(tailMap.firstKey());
    }

    /**
     * 带有权重的对象包装
     *
     * @param <T> 对象类型
     */
    public static class WeightObj<T> {
        /**
         * 权重
         */
        private final double weight;
        /**
         * 对象
         */
        private T obj;

        /**
         * 构造
         *
         * @param obj    对象
         * @param weight 权重
         */
        public WeightObj(T obj, double weight) {
            this.obj = obj;
            this.weight = weight;
        }

        /**
         * 获取对象
         *
         * @return 对象
         */
        public T getObj() {
            return obj;
        }

        /**
         * 设置对象
         *
         * @param obj 对象
         */
        public void setObj(T obj) {
            this.obj = obj;
        }

        /**
         * 获取权重
         *
         * @return 权重
         */
        public double getWeight() {
            return weight;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((obj == null) ? 0 : obj.hashCode());
            long temp;
            temp = Double.doubleToLongBits(weight);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            WeightObj<?> other = (WeightObj<?>) obj;
            if (this.obj == null) {
                if (other.obj != null) {
                    return false;
                }
            } else if (!this.obj.equals(other.obj)) {
                return false;
            }
            return Double.doubleToLongBits(weight) == Double.doubleToLongBits(other.weight);
        }
    }

}
