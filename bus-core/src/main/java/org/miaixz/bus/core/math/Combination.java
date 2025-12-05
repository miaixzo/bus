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
package org.miaixz.bus.core.math;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * A class for handling combinations, noted as C(n, m).
 * <p>
 * This class provides utilities for calculating combination counts and generating specific combinations from a dataset.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Combination implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852227281795L;

    /**
     * The data array used for generating combinations.
     */
    private final String[] datas;

    /**
     * Constructs a {@code Combination} instance.
     *
     * @param datas The data array used for combinations.
     */
    public Combination(final String[] datas) {
        this.datas = datas;
    }

    /**
     * Calculates the number of combinations, i.e., C(n, m) = n! / ((n-m)! * m!).
     *
     * @param n Total number of elements (must be >= 0).
     * @param m Number of elements to select (must be >= 0).
     * @return The combination count as a {@code long}.
     * @throws ArithmeticException If the result exceeds the range of a {@code long}.
     */
    public static long count(final int n, final int m) throws ArithmeticException {
        final BigInteger big = countBig(n, m);
        return big.longValueExact();
    }

    /**
     * Calculates the exact number of combinations C(n, m) using {@link BigInteger}.
     * <p>
     * This method uses stepwise multiplication and division (rather than calculating full factorials) to prevent
     * overflow and improve performance.
     * </p>
     * <p>
     * Mathematical definition: C(n, m) = n! / (m! (n - m)!)
     * </p>
     * <p>
     * Optimizations:
     * <ol>
     * <li>Use symmetry: m = min(m, n-m).</li>
     * <li>Multiply by BigInteger first, then divide by current index {@code i} at each step to keep values
     * manageable.</li>
     * </ol>
     *
     * @param n Total number of elements (must be >= 0).
     * @param m Number of elements to select (must be >= 0).
     * @return The exact combination count as a {@link BigInteger}. Returns {@link BigInteger#ZERO} if m > n.
     */
    public static BigInteger countBig(final int n, int m) {
        if (n < 0 || m < 0) {
            throw new IllegalArgumentException("n and m must be non-negative. got n=" + n + ", m=" + m);
        }
        if (m > n) {
            return BigInteger.ZERO;
        }
        if (m == 0 || n == m) {
            return BigInteger.ONE;
        }
        // Use symmetry: C(n, m) = C(n, n-m)
        m = Math.min(m, n - m);
        BigInteger result = BigInteger.ONE;
        // Accumulate from 1 -> m
        for (int i = 1; i <= m; i++) {
            final int numerator = n - m + i;
            result = result.multiply(BigInteger.valueOf(numerator)).divide(BigInteger.valueOf(i));
        }

        return result;
    }

    /**
     * Calculates the total number of non-empty combinations (subsets), i.e., C(n, 1) + C(n, 2) + ... + C(n, n). This is
     * equivalent to $2^n - 1$.
     *
     * @param n The total number of elements.
     * @return The total number of combinations.
     * @throws IllegalArgumentException If n is not between 0 and 63.
     */
    public static long countAll(final int n) {
        if (n < 0 || n > 63) {
            throw new IllegalArgumentException(
                    StringKit.format("countAll must have n >= 0 and n <= 63, but got n={}", n));
        }
        return n == 63 ? Long.MAX_VALUE : (1L << n) - 1;
    }

    /**
     * Selects {@code m} elements from the data to form combinations.
     *
     * @param m The number of elements to select.
     * @return A list of string arrays, where each array is a combination.
     */
    public List<String[]> select(final int m) {
        final List<String[]> result = new ArrayList<>((int) count(this.datas.length, m));
        select(0, new String[m], 0, result);
        return result;
    }

    /**
     * Selects all possible combinations (from size 1 to n).
     *
     * @return A list of string arrays containing all combinations.
     */
    public List<String[]> selectAll() {
        final List<String[]> result = new ArrayList<>((int) countAll(this.datas.length));
        for (int i = 1; i <= this.datas.length; i++) {
            result.addAll(select(i));
        }
        return result;
    }

    /**
     * Recursive method to generate combinations.
     *
     * @param dataIndex   The current index in the source data array.
     * @param resultList  The array holding the current combination being built (contains resultIndex-1 elements so
     *                    far).
     * @param resultIndex The current index in the result array to fill.
     * @param result      The list to collect all valid combinations.
     */
    private void select(
            final int dataIndex,
            final String[] resultList,
            final int resultIndex,
            final List<String[]> result) {
        final int resultLen = resultList.length;
        final int resultCount = resultIndex + 1;
        if (resultCount > resultLen) {
            // When all slots are filled, add the combination to the result
            result.add(Arrays.copyOf(resultList, resultList.length));
            return;
        }

        // Recursively select the next element
        for (int i = dataIndex; i < datas.length + resultCount - resultLen; i++) {
            resultList[resultIndex] = datas[i];
            select(i + 1, resultList, resultIndex + 1, result);
        }
    }

}
