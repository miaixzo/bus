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
package org.miaixz.bus.core.center.date.culture;

/**
 * An abstract class for representing cyclical information, such as days of the week, months of the year, quarters, etc.
 * It manages a list of names and a current index.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Samsara extends Loops {

    /**
     * The list of names in the cycle.
     */
    protected String[] names;

    /**
     * The current index in the cycle (0-based).
     */
    protected int index;

    /**
     * Initializes with a list of names and an index. The index will be normalized.
     *
     * @param names A list of names for the cycle.
     * @param index The initial index (supports negative numbers for reverse indexing).
     */
    protected Samsara(String[] names, int index) {
        this.names = names;
        this.index = indexOf(index);
    }

    /**
     * Initializes with a list of names and a name to find the index for.
     *
     * @param names A list of names for the cycle.
     * @param name  The name to find the initial index of.
     */
    protected Samsara(String[] names, String name) {
        this.names = names;
        this.index = indexOf(name);
    }

    /**
     * Gets the name corresponding to the current index.
     *
     * @return The name.
     */
    public String getName() {
        return names[index];
    }

    /**
     * Gets the current index (0-based).
     *
     * @return The index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the total number of elements in the cycle.
     *
     * @return The size of the cycle.
     */
    public int getSize() {
        return names.length;
    }

    /**
     * Finds the index corresponding to a given name.
     *
     * @param name The name to find.
     * @return The 0-based index.
     * @throws IllegalArgumentException if the name is not found.
     */
    protected int indexOf(String name) {
        for (int i = 0, j = getSize(); i < j; i++) {
            if (names[i].equals(name)) {
                return i;
            }
        }
        throw new IllegalArgumentException(String.format("illegal name: %s", name));
    }

    /**
     * Normalizes an index to be within the valid range [0, size-1] using the modulo operator.
     *
     * @param index The index to normalize.
     * @return The normalized 0-based index.
     */
    protected int indexOf(int index) {
        return indexOf(index, getSize());
    }

    /**
     * Calculates the index after shifting by {@code n} steps, wrapping around the cycle.
     *
     * @param n The number of steps to shift (can be negative).
     * @return The new 0-based index.
     */
    protected int nextIndex(int n) {
        return indexOf(index + n);
    }

    /**
     * Calculates the number of steps from the current index to a target index.
     *
     * @param targetIndex The target index.
     * @return The number of steps (can be positive or negative).
     */
    public int stepsTo(int targetIndex) {
        return indexOf(targetIndex - index);
    }

}
