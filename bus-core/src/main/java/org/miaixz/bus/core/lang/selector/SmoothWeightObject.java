/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
