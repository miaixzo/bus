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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * 动态按权重随机的随机池，底层是list实现。 原理为加入的{@link WeightObject}依次增加权重，随机时根据权重计算随机值，然后二分查找小于等于随机值的权重，返回对应的元素。
 * 我们假设随机池中有4个对象，其权重为4，5，1，6，权重越高，'-'越多，那么随机池如下：
 * 
 * <pre>{@code
 *     [obj1,  obj2, obj3, obj4  ]
 *     [----, -----,  -  , ------]
 * }</pre>
 * 
 * 然后最后一个元素的权重值为总权重值，即obj2的权重值为obj1权重+obj2本身权重，依次类推。 我们取一个总权重范围的随机数，根据随机数在'-'列表中的位置，找到对应的obj即随机到的对象。
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeightListRandomSelector<E> implements Selector<E>, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 随机元素池
     */
    private final List<WeightObject<E>> randomPool;

    /**
     * 构造
     */
    public WeightListRandomSelector() {
        randomPool = new ArrayList<>();
    }

    /**
     * 构造
     *
     * @param poolSize 初始随机池大小
     */
    public WeightListRandomSelector(final int poolSize) {
        randomPool = new ArrayList<>(poolSize);
    }

    /**
     * 增加随机种子
     *
     * @param e      随机对象
     * @param weight 权重
     */
    public void add(final E e, final int weight) {
        Assert.isTrue(weight > 0, "权重必须大于0！");
        randomPool.add(new WeightObject<>(e, sumWeight() + weight));
    }

    /**
     * 移除随机种子
     *
     * @param e 随机对象
     * @return 是否移除成功
     */
    public boolean remove(final E e) {
        boolean removed = false;
        int weight = 0;
        int i = 0;
        final Iterator<WeightObject<E>> iterator = randomPool.iterator();
        WeightObject<E> ew;
        while (iterator.hasNext()) {
            ew = iterator.next();
            if (!removed && ObjectKit.equals(ew.object, e)) {
                iterator.remove();
                // 权重=当前权重-上一个权重
                weight = ew.weight - (i == 0 ? 0 : randomPool.get(i - 1).weight);
                removed = true;
            }
            if (removed) {
                // 重新计算后续权重
                ew.weight -= weight;
            }
            i++;
        }
        return removed;
    }

    /**
     * 判断是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return randomPool.isEmpty();
    }

    @Override
    public E select() {
        if (isEmpty()) {
            return null;
        }
        if (randomPool.size() == 1) {
            return randomPool.get(0).object;
        }
        return binarySearch((int) (sumWeight() * Math.random()));
    }

    /**
     * 二分查找小于等于key的最大值的元素
     *
     * @param randomWeight 随机权重值，查找这个权重对应的元素
     * @return 随机池的一个元素或者null 当key大于所有元素的总权重时，返回null
     */
    private E binarySearch(final int randomWeight) {
        int low = 0;
        int high = randomPool.size() - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final int midWeight = randomPool.get(mid).weight;

            if (midWeight < randomWeight) {
                low = mid + 1;
            } else if (midWeight > randomWeight) {
                high = mid - 1;
            } else {
                return randomPool.get(mid).object;
            }
        }
        return randomPool.get(low).object;
    }

    private int sumWeight() {
        if (randomPool.isEmpty()) {
            return 0;
        }
        return randomPool.get(randomPool.size() - 1).weight;
    }

}
