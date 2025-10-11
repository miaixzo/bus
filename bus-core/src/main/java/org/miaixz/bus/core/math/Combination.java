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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A class for handling combinations, noted as C(n, m).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Combination implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852227281795L;

    /**
     * The array of data elements to be combined.
     */
    private final String[] datas;

    /**
     * Constructor.
     *
     * @param datas The data to be combined.
     */
    public Combination(final String[] datas) {
        this.datas = datas;
    }

    /**
     * Calculates the number of combinations, i.e., C(n, m) = n!/((n-m)! * m!).
     *
     * @param n The total number of elements.
     * @param m The number of elements to select.
     * @return The number of combinations.
     */
    public static long count(final int n, final int m) {
        if (0 == m || n == m) {
            return 1;
        }
        return (n > m) ? MathKit.factorial(n, n - m) / MathKit.factorial(m) : 0;
    }

    /**
     * Calculates the total number of combinations for all possible selection sizes, i.e., C(n, 1) + C(n, 2) + C(n, 3) +
     * ... + C(n, n).
     *
     * @param n The total number of elements.
     * @return The total number of combinations.
     */
    public static long countAll(final int n) {
        if (n < 0 || n > 63) {
            throw new IllegalArgumentException(
                    StringKit.format("countAll must have n >= 0 and n <= 63, but got n={}", n));
        }
        return n == 63 ? Long.MAX_VALUE : (1L << n) - 1;
    }

    /**
     * Selects combinations (selects m elements from the list for combination).
     *
     * @param m The number of elements to select.
     * @return A list of all combination results.
     */
    public List<String[]> select(final int m) {
        final List<String[]> result = new ArrayList<>((int) count(this.datas.length, m));
        select(0, new String[m], 0, result);
        return result;
    }

    /**
     * Selects all combinations for all possible selection sizes.
     *
     * @return A list of all combination results.
     */
    public List<String[]> selectAll() {
        final List<String[]> result = new ArrayList<>((int) countAll(this.datas.length));
        for (int i = 1; i <= this.datas.length; i++) {
            result.addAll(select(i));
        }
        return result;
    }

    /**
     * Recursively selects elements for combination.
     *
     * @param dataIndex   The starting index for selection from the source data.
     * @param resultList  The temporary array to hold the current combination.
     * @param resultIndex The current position to fill in the {@code resultList}.
     * @param result      The final list to store all generated combinations.
     */
    private void select(
            final int dataIndex,
            final String[] resultList,
            final int resultIndex,
            final List<String[]> result) {
        final int resultLen = resultList.length;
        final int resultCount = resultIndex + 1;
        if (resultCount > resultLen) { // When all elements for the combination are selected, add it to the result.
            result.add(Arrays.copyOf(resultList, resultList.length));
            return;
        }

        // Recursively select the next element.
        for (int i = dataIndex; i < datas.length + resultCount - resultLen; i++) {
            resultList[resultIndex] = datas[i];
            select(i + 1, resultList, resultIndex + 1, result);
        }
    }

}
