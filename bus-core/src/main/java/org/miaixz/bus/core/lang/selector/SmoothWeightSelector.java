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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.xyz.CollKit;

/**
 * A selector that implements the smooth weighted round-robin algorithm.
 * <p>
 * The algorithm is as follows: For a set of servers with weights (e.g., A:5, B:3, C:2), the total weight is 10. Each
 * server maintains a `currentWeight`, initialized to 0.
 * 
 * <pre>
 * Request | currentWeight = currentWeight + weight | Max Weight | Selected | Update: max_weight - totalWeight, others unchanged
 * ----------------------------------------------------------------------------------------------------------------------------
 *    1    |           5, 3, 2 (0,0,0 + 5,3,2)       |      5     |     A    |     -5, 3, 2
 *    2    |           0, 6, 4 (-5,3,2 + 5,3,2)      |      6     |     B    |      0,-4, 4
 *    3    |           5,-1, 6 (0,-4,4 + 5,3,2)      |      6     |     C    |      5,-1,-4
 *    4    |          10, 2,-2 (5,-1,-4 + 5,3,2)      |     10     |     A    |      0, 2,-2
 *    5    |           5, 5, 0                      |      5     |     A    |     -5, 5, 0
 *    6    |           0, 8, 2                      |      8     |     B    |      0,-2, 2
 *    7    |           5, 1, 4                      |      5     |     A    |     -5, 1, 4
 *    8    |           0, 4, 6                      |      6     |     C    |      0, 4,-4
 *    9    |           5, 7,-2                      |      7     |     B    |      5,-3,-2
 *   10    |          10, 0, 0                      |     10     |     A    |      0, 0, 0
 * </pre>
 * <p>
 * The resulting selection sequence is: A, B, C, A, A, B, A, C, B, A.
 *
 * @param <T> the type of the object to select
 * @author Kimi Liu
 * @since Java 21+
 */
public class SmoothWeightSelector<T> implements Selector<T> {

    /**
     * The list of smooth weighted objects.
     */
    private final List<SmoothWeightObject<T>> objList;

    /**
     * Constructs an empty {@code SmoothWeightSelector}.
     */
    public SmoothWeightSelector() {
        this.objList = new ArrayList<>();
    }

    /**
     * Constructs a new {@code SmoothWeightSelector} and initializes it with the given weighted objects.
     *
     * @param weightObjList an iterable of {@link WeightObject} instances
     */
    public SmoothWeightSelector(final Iterable<? extends WeightObject<T>> weightObjList) {
        this();
        for (final WeightObject<T> weightObj : weightObjList) {
            add(weightObj);
        }
    }

    /**
     * Creates a new, empty {@code SmoothWeightSelector}.
     *
     * @param <T> the type of the object
     * @return a new {@code SmoothWeightSelector} instance
     */
    public static <T> SmoothWeightSelector<T> of() {
        return new SmoothWeightSelector<>();
    }

    /**
     * Adds an object with a specified weight to the selector.
     *
     * @param object the object to add
     * @param weight the weight of the object
     * @return this {@code SmoothWeightSelector} instance
     */
    public SmoothWeightSelector<T> add(final T object, final int weight) {
        return add(new SmoothWeightObject<>(object, weight));
    }

    /**
     * Adds a weighted object to the selector.
     *
     * @param weightObj the {@link WeightObject} to add
     * @return this {@code SmoothWeightSelector} instance
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
     * Selects an object using the smooth weighted round-robin algorithm.
     *
     * @return the selected object, or {@code null} if the selector is empty
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
            // Should not happen if list is not empty
            return null;
        }

        // Update the current weight of the selected object
        selected.setCurrentWeight(selected.getCurrentWeight() - totalWeight);

        return selected.getObject();
    }

}
