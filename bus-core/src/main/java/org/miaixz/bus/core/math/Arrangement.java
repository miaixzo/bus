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

import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MathKit;

/**
 * A class for handling arrangements (permutations), noted as A(n, m).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Arrangement implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852281592361L;

    /**
     * The array of data elements to be arranged.
     */
    private final String[] datas;

    /**
     * Constructor.
     *
     * @param datas The data to be arranged.
     */
    public Arrangement(final String[] datas) {
        this.datas = datas;
    }

    /**
     * Calculates the number of permutations, i.e., A(n, n) = n!
     *
     * @param n The total number of elements.
     * @return The number of permutations.
     */
    public static long count(final int n) {
        return count(n, n);
    }

    /**
     * Calculates the number of permutations, i.e., A(n, m) = n!/(n-m)!
     *
     * @param n The total number of elements.
     * @param m The number of elements to select.
     * @return The number of permutations.
     */
    public static long count(final int n, final int m) {
        if (n == m) {
            return MathKit.factorial(n);
        }
        return (n > m) ? MathKit.factorial(n, n - m) : 0;
    }

    /**
     * Calculates the total number of permutations for all possible selection sizes, i.e., A(n, 1) + A(n, 2) + A(n, 3) +
     * ... + A(n, n).
     *
     * @param n The total number of elements.
     * @return The total number of permutations.
     */
    public static long countAll(final int n) {
        long total = 0;
        for (int i = 1; i <= n; i++) {
            total += count(n, i);
        }
        return total;
    }

    /**
     * Selects all permutations (all elements in the list are involved in the permutation).
     *
     * @return A list of all permutations.
     */
    public List<String[]> select() {
        return select(this.datas.length);
    }

    /**
     * Selects permutations (selects m elements from the list for permutation).
     *
     * @param m The number of elements to select.
     * @return A list of all permutations.
     */
    public List<String[]> select(final int m) {
        final List<String[]> result = new ArrayList<>((int) count(this.datas.length, m));
        select(this.datas, new String[m], 0, result);
        return result;
    }

    /**
     * Selects all permutations for all possible selection sizes, i.e., A(n, 1) + A(n, 2) + A(n, 3) + ... + A(n, n).
     *
     * @return A list of all permutation results.
     */
    public List<String[]> selectAll() {
        final List<String[]> result = new ArrayList<>((int) countAll(this.datas.length));
        for (int i = 1; i <= this.datas.length; i++) {
            result.addAll(select(i));
        }
        return result;
    }

    /**
     * Recursively selects elements for permutation.
     *
     * @param datas       The base data for selection.
     * @param resultList  The permutation result of the previous (resultIndex-1) elements.
     * @param resultIndex The selection index, starting from 0.
     * @param result      The final result list.
     */
    private void select(
            final String[] datas,
            final String[] resultList,
            final int resultIndex,
            final List<String[]> result) {
        if (resultIndex >= resultList.length) { // When all elements are selected, add the permutation result.
            if (!result.contains(resultList)) {
                result.add(Arrays.copyOf(resultList, resultList.length));
            }
            return;
        }

        // Recursively select the next element.
        for (int i = 0; i < datas.length; i++) {
            resultList[resultIndex] = datas[i];
            select(ArrayKit.remove(datas, i), resultList, resultIndex + 1, result);
        }
    }

}
