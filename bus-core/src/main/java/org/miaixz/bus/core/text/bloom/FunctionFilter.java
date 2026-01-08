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
package org.miaixz.bus.core.text.bloom;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

import org.miaixz.bus.core.lang.Assert;

/**
 * A Bloom filter implementation based on custom hash functions provided by {@link ToIntFunction}s.
 * <p>
 * This filter extends {@link AbstractFilter} and allows the user to define one or multiple hash functions to determine
 * the bit positions for elements. Using multiple hash functions reduces the probability of false positives.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FunctionFilter extends AbstractFilter {

    @Serial
    private static final long serialVersionUID = 2852235578011L;

    /**
     * The list of hash functions used by this filter.
     */
    private final List<ToIntFunction<String>> hashFuncs;

    /**
     * Constructs a {@code FunctionFilter}.
     *
     * @param size      The maximum size (number of bits) of the filter.
     * @param hashFuncs The hash functions to use. At least one must be provided.
     */
    @SafeVarargs
    public FunctionFilter(final int size, final ToIntFunction<String>... hashFuncs) {
        super(size);
        Assert.notEmpty(hashFuncs, "Hash functions must not be empty");
        this.hashFuncs = Collections.unmodifiableList(Arrays.asList(hashFuncs));
    }

    /**
     * Creates a new {@code FunctionFilter}.
     *
     * @param size      The maximum size (number of bits) of the filter.
     * @param hashFuncs The hash functions to use. At least one must be provided.
     * @return A new {@code FunctionFilter} instance.
     */
    @SafeVarargs
    public static FunctionFilter of(final int size, final ToIntFunction<String>... hashFuncs) {
        return new FunctionFilter(size, hashFuncs);
    }

    /**
     * Calculates the hash code for a string using the first configured hash function. This method overrides the parent
     * implementation to maintain compatibility.
     *
     * @param str The string to hash.
     * @return The calculated hash code index.
     */
    @Override
    public int hash(final String str) {
        return hash(str, hashFuncs.get(0));
    }

    /**
     * Calculates the hash code for a string using a specific hash function.
     *
     * @param str      The string to hash.
     * @param hashFunc The specific hash function to use.
     * @return The calculated hash code index, ensured to be a positive integer within the filter size.
     */
    public int hash(final String str, final ToIntFunction<String> hashFunc) {
        // Use bitwise AND to ensure a positive number
        return (hashFunc.applyAsInt(str) & 0x7FFFFFFF) % size;
    }

    /**
     * Checks if the filter might contain the specified string.
     * <p>
     * Returns {@code true} if all bits corresponding to the hash functions are set. Note that false positives are
     * possible, but false negatives are not.
     * </p>
     *
     * @param str The string to check.
     * @return {@code true} if the string might be in the filter, {@code false} if it definitely is not.
     */
    @Override
    public boolean contains(final String str) {
        for (final ToIntFunction<String> hashFunc : hashFuncs) {
            if (!bitSet.get(hash(str, hashFunc))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a string to the filter.
     *
     * @param str The string to add.
     * @return {@code true} if at least one bit was changed from 0 to 1 (indicating the item was likely new),
     *         {@code false} if all bits were already set.
     */
    @Override
    public boolean add(final String str) {
        boolean added = false;
        int hash;
        for (final ToIntFunction<String> hashFunc : hashFuncs) {
            hash = hash(str, hashFunc);
            if (!bitSet.get(hash)) {
                bitSet.set(hash);
                added = true;
            }
        }
        return added;
    }

}
