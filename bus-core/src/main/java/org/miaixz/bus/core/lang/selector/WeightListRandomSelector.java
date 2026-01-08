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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A dynamic, list-based random pool that selects elements based on their weight.
 * <p>
 * The underlying principle is that each added {@link WeightObject} has its weight accumulated. When selecting, a random
 * value is calculated based on the total weight, and a binary search is performed to find the element whose cumulative
 * weight is less than or equal to the random value.
 *
 * <p>
 * For example, if the random pool contains four objects with weights 4, 5, 1, and 6, the pool can be visualized as:
 * 
 * <pre>{@code
 *     [obj1,  obj2,   obj3,   obj4  ]
 *     [----, -----,    -  ,  ------]
 *     [ 4  ,  9   ,   10  ,   16   ]  (Cumulative Weights)
 * }</pre>
 * <p>
 * The cumulative weight of the last element is the total weight. A random number is generated within the range of the
 * total weight. The position of this random number in the cumulative weight list determines which object is selected.
 *
 * @param <E> the type of the elements
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeightListRandomSelector<E> implements Selector<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852278633171L;

    /**
     * The pool of weighted random elements.
     */
    private final List<WeightObject<E>> randomPool;

    /**
     * Constructs a new {@code WeightListRandomSelector} with a default initial capacity.
     */
    public WeightListRandomSelector() {
        randomPool = new ArrayList<>();
    }

    /**
     * Constructs a new {@code WeightListRandomSelector} with a specified initial capacity.
     *
     * @param poolSize the initial size of the random pool
     */
    public WeightListRandomSelector(final int poolSize) {
        randomPool = new ArrayList<>(poolSize);
    }

    /**
     * Adds a random element with a given weight. The weight is added to the cumulative total.
     *
     * @param e      the random object to add
     * @param weight the weight of the object (must be > 0)
     */
    public void add(final E e, final int weight) {
        Assert.isTrue(weight > 0, "Weight must be greater than 0!");
        randomPool.add(new WeightObject<>(e, sumWeight() + weight));
    }

    /**
     * Removes a random element from the pool. After removal, the cumulative weights of all subsequent elements are
     * recalculated.
     *
     * @param e the random object to remove
     * @return {@code true} if the element was successfully removed, {@code false} otherwise
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
                // The weight of the removed element is its cumulative weight minus the previous one.
                weight = ew.weight - (i == 0 ? 0 : randomPool.get(i - 1).weight);
                removed = true;
            }
            if (removed) {
                // Recalculate the cumulative weights of subsequent elements.
                ew.weight -= weight;
            }
            i++;
        }
        return removed;
    }

    /**
     * Checks if the random pool is empty.
     *
     * @return {@code true} if the pool is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return randomPool.isEmpty();
    }

    /**
     * Selects an element based on its weight.
     *
     * @return the selected element, or {@code null} if the pool is empty
     */
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
     * Performs a binary search to find the element corresponding to the given random weight. It finds the first element
     * whose cumulative weight is greater than or equal to the random weight.
     *
     * @param randomWeight the random weight value to search for
     * @return the element corresponding to the random weight, or {@code null} if not found
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
        // If not found exactly, low is the insertion point, which is the correct element.
        if (low >= randomPool.size()) {
            // Should not happen if randomWeight is within bounds
            return null;
        }
        return randomPool.get(low).object;
    }

    /**
     * Gets the total weight of all elements in the pool.
     *
     * @return the total weight
     */
    private int sumWeight() {
        if (randomPool.isEmpty()) {
            return 0;
        }
        return randomPool.get(randomPool.size() - 1).weight;
    }

}
