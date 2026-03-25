/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.selector;

import java.io.Serial;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

import org.miaixz.bus.core.xyz.CollKit;

/**
 * An implementation of a weighted random selection algorithm.
 * <p>
 * This is often used in scenarios like ad serving or load balancing, where we need to randomly select an item from a
 * collection of N items with different weights, ensuring that the overall selection results follow the weight
 * distribution.
 *
 * <p>
 * For example, if there are four elements A, B, C, and D with weights 1, 2, 3, and 4 respectively, the random selection
 * should result in a ratio of A:B:C:D close to 1:2:3:4.
 *
 * <p>
 * The general idea is to accumulate the weights of each element: A(1)-B(3)-C(6)-D(10). This creates weight ranges for
 * each element: [0,1), [1,3), [3,6), and [6,10). Then, a random number is generated between [0, 10). The element whose
 * range contains the random number is the one selected according to its weight.
 *
 * @param <T> the type of the object to be selected
 * @author Kimi Liu
 * @since Java 21+
 */
public class WeightRandomSelector<T> implements Selector<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852278757903L;

    /**
     * A map where the key is the cumulative weight and the value is the object.
     */
    private final TreeMap<Integer, T> weightMap;

    /**
     * Constructs an empty {@code WeightRandomSelector}.
     */
    public WeightRandomSelector() {
        weightMap = new TreeMap<>();
    }

    /**
     * Constructs a new {@code WeightRandomSelector} and adds a single weighted object.
     *
     * @param weightObj the weighted object to add
     */
    public WeightRandomSelector(final WeightObject<T> weightObj) {
        this();
        if (null != weightObj) {
            add(weightObj);
        }
    }

    /**
     * Constructs a new {@code WeightRandomSelector} and adds all objects from the given iterable.
     *
     * @param weightObjs an iterable of weighted objects
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
     * Constructs a new {@code WeightRandomSelector} and adds all objects from the given array.
     *
     * @param weightObjs an array of weighted objects
     */
    public WeightRandomSelector(final WeightObject<T>[] weightObjs) {
        this();
        for (final WeightObject<T> weightObj : weightObjs) {
            add(weightObj);
        }
    }

    /**
     * Creates a new, empty {@code WeightRandomSelector}.
     *
     * @param <T> the type of the object to be selected
     * @return a new {@code WeightRandomSelector} instance
     */
    public static <T> WeightRandomSelector<T> of() {
        return new WeightRandomSelector<>();
    }

    /**
     * Adds an object with a specified weight to the selector.
     *
     * @param object the object to add
     * @param weight the weight of the object (must be > 0)
     * @return this {@code WeightRandomSelector} instance
     */
    public WeightRandomSelector<T> add(final T object, final int weight) {
        return add(new WeightObject<>(object, weight));
    }

    /**
     * Adds a weighted object to the selector. The weight is accumulated to form a range.
     *
     * @param weightObj the weighted object to add
     * @return this {@code WeightRandomSelector} instance
     */
    public WeightRandomSelector<T> add(final WeightObject<T> weightObj) {
        if (null != weightObj) {
            final int weight = weightObj.getWeight();
            if (weight > 0) {
                final int lastWeight = this.weightMap.isEmpty() ? 0 : this.weightMap.lastKey();
                this.weightMap.put(weight + lastWeight, weightObj.getObject()); // Accumulate weight
            }
        }
        return this;
    }

    /**
     * Clears the weight map.
     *
     * @return this {@code WeightRandomSelector} instance
     */
    public WeightRandomSelector<T> clear() {
        if (null != this.weightMap) {
            this.weightMap.clear();
        }
        return this;
    }

    /**
     * Selects a random object based on its weight.
     *
     * @return the selected random object
     * @throws java.util.NoSuchElementException if the selector is empty
     */
    @Override
    public T select() {
        final int randomWeight = (int) (this.weightMap.lastKey() * Math.random());
        final SortedMap<Integer, T> tailMap = this.weightMap.tailMap(randomWeight, false);
        return this.weightMap.get(tailMap.firstKey());
    }

}
