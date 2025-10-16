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
