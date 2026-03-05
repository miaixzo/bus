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

import java.util.Objects;

/**
 * A wrapper for an object that assigns a weight to it.
 *
 * @param <T> the type of the wrapped object
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeightObject<T> {

    /**
     * The weight of the object.
     */
    protected int weight;
    /**
     * The wrapped object.
     */
    protected T object;

    /**
     * Constructs a new {@code WeightObject} with the specified object and weight.
     *
     * @param object the object to wrap
     * @param weight the weight to assign to the object
     */
    public WeightObject(final T object, final int weight) {
        this.object = object;
        this.weight = weight;
    }

    /**
     * Gets the wrapped object.
     *
     * @return the wrapped object
     */
    public T getObject() {
        return object;
    }

    /**
     * Sets the wrapped object.
     *
     * @param object the new object to wrap
     */
    public void setObject(final T object) {
        this.object = object;
    }

    /**
     * Gets the weight of the object.
     *
     * @return the weight of the object
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Compares this {@code WeightObject} to another object for equality. Two {@code WeightObject} instances are
     * considered equal if they have the same weight and their wrapped objects are equal.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final WeightObject<?> weightObj = (WeightObject<?>) o;
        return weight == weightObj.weight && Objects.equals(object, weightObj.object);
    }

    /**
     * Returns the hash code for this {@code WeightObject}. The hash code is based on the wrapped object and its weight.
     *
     * @return the hash code for this {@code WeightObject}
     */
    @Override
    public int hashCode() {
        return Objects.hash(object, weight);
    }

}
