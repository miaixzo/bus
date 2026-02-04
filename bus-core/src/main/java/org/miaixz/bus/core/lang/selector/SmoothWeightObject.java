/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.selector;

/**
 * Represents an object with a weight for use in smooth weighted round-robin selection.
 * <p>
 * This class extends {@link WeightObject} and adds a {@code currentWeight} property, which is a dynamic value that
 * changes during the selection process.
 *
 * @param <T> the type of the wrapped object
 * @author Kimi Liu
 * @since Java 17+
 */
public class SmoothWeightObject<T> extends WeightObject<T> {

    /**
     * The current, dynamic weight of the object.
     */
    private int currentWeight;

    /**
     * Constructs a new {@code SmoothWeightObject} with an initial current weight of 0.
     *
     * @param object the object to wrap
     * @param weight the static weight of the object
     */
    public SmoothWeightObject(final T object, final int weight) {
        this(object, weight, 0);
    }

    /**
     * Constructs a new {@code SmoothWeightObject}.
     *
     * @param object        the object to wrap
     * @param weight        the static weight of the object
     * @param currentWeight the initial current weight
     */
    public SmoothWeightObject(final T object, final int weight, final int currentWeight) {
        super(object, weight);
        this.currentWeight = currentWeight;
    }

    /**
     * Gets the current (dynamic) weight of the object.
     *
     * @return the current weight
     */
    public int getCurrentWeight() {
        return currentWeight;
    }

    /**
     * Sets the current (dynamic) weight of the object.
     *
     * @param currentWeight the new current weight
     */
    public void setCurrentWeight(final int currentWeight) {
        this.currentWeight = currentWeight;
    }

}
