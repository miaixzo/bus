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
package org.miaixz.bus.core.lang.selector;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.xyz.CollKit;

/**
 * 平滑加权轮询选择器 思路: 比如 A : 5 , B : 3 , C : 2 (服务器 A,B,C 对应权重分别是 5,3,2) ip: A,B,C weight: 5,3,2 (计算得到 totalWeight = 10)
 * currentWeight: 0,0,0 (当前ip的初始权重都为0)
 * 
 * <pre>
 * 请求次数: |  currentWeight = currentWeight + weight  |  最大权重为  |  返回的ip为 |  最大的权重 - totalWeight,其余不变
 *      1   |           5,3,2    (0,0,0 + 5,3,2)       |     5      |      A     |      -5,3,2
 *      2   |           0,6,4    (-5,3,2 + 5,3,2)      |     6      |      B     |       0,-4,4
 *      3   |           5,-1,6    (0,-4,4 + 5,3,2)     |     6      |     C      |       5,-1,-4
 *      4   |          10,2,-2    (5,-1,-4 + 5,3,2)    |     10     |     A      |       0,2,-2
 *      5   |           5,5,0                          |     5      |     A      |       -5,5,0
 *      6   |           0,8,2                          |     8      |     B      |       0,-2,2
 *      7   |           5,1,4                          |     5      |     A      |       -5,1,4
 *      8   |           0,4,6                          |     6      |     C      |       0,4,-4
 *      9   |           5,7,-2                         |     7      |     B      |       5,-3,-2
 *      10  |           10,0,0                         |     10     |     A      |        0,0,0
 * </pre>
 * 
 * 至此结束: 可以看到负载轮询的策略是: A,B,C,A,A,B,A,C,B,A,
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class SmoothWeightSelector<T> implements Selector<T> {

    private final List<SmoothWeightObject<T>> objList;

    /**
     * 构造
     */
    public SmoothWeightSelector() {
        this.objList = new ArrayList<>();
    }

    /**
     * 构造
     *
     * @param weightObjList 权重对象列表
     */
    public SmoothWeightSelector(final Iterable<? extends WeightObject<T>> weightObjList) {
        this();
        for (final WeightObject<T> weightObj : weightObjList) {
            add(weightObj);
        }
    }

    /**
     * 创建平滑加权获取器
     *
     * @param <T> 对象类型
     * @return SmoothSelector
     */
    public static <T> SmoothWeightSelector<T> of() {
        return new SmoothWeightSelector<>();
    }

    /**
     * 增加对象
     *
     * @param object 对象
     * @param weight 权重
     * @return this
     */
    public SmoothWeightSelector<T> add(final T object, final int weight) {
        return add(new SmoothWeightObject<>(object, weight));
    }

    /**
     * 增加权重对象
     *
     * @param weightObj 权重对象
     * @return this
     */
    public SmoothWeightSelector<T> add(final WeightObject<T> weightObj) {
        final SmoothWeightObject<T> smoothWeightObj;
        if (weightObj instanceof SmoothWeightObject) {
            smoothWeightObj = (SmoothWeightObject<T>) weightObj;
        } else {
            smoothWeightObj = new SmoothWeightObject<>(weightObj.object, weightObj.weight);
        }
        this.objList.add(smoothWeightObj);
        return this;
    }

    /**
     * 通过平滑加权方法获取列表中的当前对象
     *
     * @return 选中的对象
     */
    @Override
    public T select() {
        if (CollKit.isEmpty(this.objList)) {
            return null;
        }
        int totalWeight = 0;
        SmoothWeightObject<T> selected = null;

        for (final SmoothWeightObject<T> object : objList) {
            totalWeight += object.getWeight();
            final int currentWeight = object.getCurrentWeight() + object.getWeight();
            object.setCurrentWeight(currentWeight);
            if (null == selected || currentWeight > selected.getCurrentWeight()) {
                selected = object;
            }
        }

        if (null == selected) {
            return null;
        }

        // 更新选择的对象的当前权重，并返回其地址
        selected.setCurrentWeight(selected.getCurrentWeight() - totalWeight);

        return selected.getObject();
    }

}
