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
import java.util.*;

/**
 * A class for handling arrangements (permutations), mathematically noted as $A(n, m)$ or $P(n, k)$.
 * <p>
 * This class provides utilities for calculating permutation counts and generating specific permutations from a dataset.
 * It supports both full permutations and partial permutations of a specific length.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Arrangement implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852281592361L;

    /**
     * The source data array used for generating arrangements.
     */
    private final String[] datas;

    /**
     * Constructs an {@code Arrangement} instance with the specified data.
     *
     * @param datas The data array used for arrangements.
     */
    public Arrangement(final String[] datas) {
        this.datas = datas;
    }

    /**
     * Calculates the number of permutations $A(n, n)$, which is equal to $n!$.
     *
     * @param n The total number of elements.
     * @return The permutation count as a {@code long}.
     */
    public static long count(final int n) {
        return count(n, n);
    }

    /**
     * Calculates the number of permutations $A(n, m)$, which is equal to $\frac{n!}{(n-m)!}$.
     *
     * @param n The total number of elements.
     * @param m The number of elements to select.
     * @return The permutation count as a {@code long}.
     * @throws IllegalArgumentException If parameters do not satisfy {@code n >= 0 && m >= 0 && m <= n}.
     * @throws ArithmeticException      If the result overflows a {@code long}.
     */
    public static long count(final int n, final int m) {
        if (m < 0 || m > n) {
            throw new IllegalArgumentException("n >= 0 && m >= 0 && m <= n required");
        }
        if (m == 0) {
            return 1;
        }
        long result = 1;
        // Multiply iteratively from n down to n-m+1
        for (int i = 0; i < m; i++) {
            final long next = result * (n - i);
            // Overflow check
            if (next < result) {
                throw new ArithmeticException("Overflow computing A(" + n + "," + m + ")");
            }
            result = next;
        }
        return result;
    }

    /**
     * Calculates the total number of permutations for all possible sizes, i.e., $A(n, 1) + A(n, 2) + ... + A(n, n)$.
     *
     * @param n The total number of elements.
     * @return The total permutation count.
     */
    public static long countAll(final int n) {
        long total = 0;
        for (int i = 1; i <= n; i++) {
            total += count(n, i);
        }
        return total;
    }

    /**
     * Selects all full permutations of the data (where $m = n$).
     *
     * @return A list of string arrays containing all full permutations.
     */
    public List<String[]> select() {
        return select(this.datas.length);
    }

    /**
     * Selects {@code m} elements from the data to generate all "non-repeating" permutations.
     *
     * <p>
     * <b>Description:</b>
     * <ul>
     * <li>Does not allow selecting the same element index twice (standard permutation $A(n, m)$).</li>
     * <li>Results will not contain duplicates based on index usage.</li>
     * <li>Order sensitive, e.g., {@code ["1", "2"]} and {@code ["2", "1"]} are distinct.</li>
     * </ul>
     * <p>
     * <b>Count formula:</b>
     * 
     * <pre>
     * A(n, m) = n! / (n - m)!
     * </pre>
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * datas = ["1", "2", "3"]
     * m = 2
     * Output:
     * ["1", "2"]
     * ["1", "3"]
     * ["2", "1"]
     * ["2", "3"]
     * ["3", "1"]
     * ["3", "2"]
     * Total 6 (A(3,2)=6)
     * </pre>
     *
     * @param m The number of elements to select.
     * @return A list of string arrays containing all unique permutations of length {@code m}.
     */
    public List<String[]> select(final int m) {
        if (m < 0 || m > datas.length) {
            return Collections.emptyList();
        }
        if (m == 0) {
            // A(n,0) = 1, the unique empty permutation
            return Collections.singletonList(new String[0]);
        }

        final long estimated = count(datas.length, m);
        final int capacity = estimated > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) estimated;

        final List<String[]> result = new ArrayList<>(capacity);
        final boolean[] visited = new boolean[datas.length];
        dfs(new String[m], 0, visited, result);
        return result;
    }

    /**
     * Generates all non-repeating permutations of the current data for lengths 1 to n.
     *
     * <p>
     * <b>Description:</b>
     * <ul>
     * <li>Does not allow duplicate element selection based on index.</li>
     * <li>Includes all permutations for lengths {@code m=1..n}.</li>
     * <li>Total count is $A(n,1) + A(n,2) + ... + A(n,n)$.</li>
     * </ul>
     * <p>
     * <b>Example (datas = ["1", "2", "3"]):</b>
     * 
     * <pre>
     * m=1: ["1"], ["2"], ["3"]                             → 3
     * m=2: ["1", "2"], ["1", "3"], ["2", "1"], ...         → 6
     * m=3: ["1", "2", "3"], ["1", "3", "2"], ["2", "1", "3"]... → 6
     *
     * Total: 3 + 6 + 6 = 15
     * </pre>
     *
     * @return A list of string arrays containing all permutations.
     */
    public List<String[]> selectAll() {
        final List<String[]> result = new ArrayList<>();
        for (int m = 1; m <= datas.length; m++) {
            result.addAll(select(m));
        }
        return result;
    }

    /**
     * Returns an iterator for the permutations of length {@code m}.
     *
     * @param m The number of elements to select.
     * @return An iterable for iterating over permutations.
     */
    public Iterable<String[]> iterate(final int m) {
        return () -> new ArrangementIterator(datas, m);
    }

    /**
     * Core recursive method (Backtracking algorithm) for generating permutations.
     *
     * @param current The current permutation array being built.
     * @param depth   The current recursion depth (which position is being filled).
     * @param visited An array marking which indices have been used in the current path.
     * @param result  The list to collect valid permutations.
     */
    private void dfs(final String[] current, final int depth, final boolean[] visited, final List<String[]> result) {
        if (depth == current.length) {
            result.add(Arrays.copyOf(current, current.length));
            return;
        }

        for (int i = 0; i < datas.length; i++) {
            if (!visited[i]) {
                visited[i] = true;
                current[depth] = datas[i];

                dfs(current, depth + 1, visited, result);
                visited[i] = false;
            }
        }
    }

    /**
     * Permutation Iterator.
     * <p>
     * Provides a non-recursive way to traverse permutations, which is memory efficient compared to generating all
     * results at once. This simulates the Depth-First Search (DFS) stack behavior using arrays.
     * </p>
     */
    private static class ArrangementIterator implements Iterator<String[]> {

        /**
         * The source data array.
         */
        private final String[] datas;

        /**
         * The number of elements to select for the arrangement.
         */
        private final int m;

        /**
         * The total length of the source data.
         */
        private final int n;

        /**
         * Array to keep track of visited elements to avoid duplicates in the current path.
         */
        private final boolean[] visited;

        /**
         * Buffer to store the current permutation being built.
         */
        private final String[] buffer;

        /**
         * Array to store the current index being tried at each depth level.
         * <p>
         * -1 indicates no element has been selected yet at that depth.
         * </p>
         */
        private final int[] indices;

        /**
         * The current depth of the recursion simulation (0 to m-1).
         */
        private int depth;

        /**
         * Flag indicating if the iteration has finished.
         */
        private boolean end;

        /**
         * The next permutation result, pre-calculated by {@link #prepareNext()}.
         */
        private String[] nextItem;

        /**
         * Flag indicating if the next item has been prepared/calculated.
         */
        private boolean nextPrepared;

        /**
         * Constructs an {@code ArrangementIterator}.
         *
         * @param datas The data array.
         * @param m     The number of elements to select.
         */
        ArrangementIterator(final String[] datas, final int m) {
            this.datas = datas;
            this.m = m;
            this.n = datas.length;
            this.visited = new boolean[n];
            this.nextItem = null;
            this.nextPrepared = false;

            if (m < 0 || m > n) {
                // Invalid or impossible configuration, end iteration immediately
                this.indices = new int[Math.max(1, m)];
                this.buffer = new String[Math.max(1, m)];
                this.depth = -1;
                this.end = true;
            } else if (m == 0) {
                // m == 0: Returns only one empty array as the single result
                this.indices = new int[0];
                this.buffer = new String[0];
                this.depth = 0;
                this.end = false;
            } else {
                this.indices = new int[m];
                Arrays.fill(this.indices, -1);
                this.buffer = new String[m];
                this.depth = 0;
                this.end = false;
            }
        }

        /**
         * Returns true if the iteration has more elements.
         *
         * @return true if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            if (end) {
                return false;
            }
            if (nextPrepared) {
                return nextItem != null;
            }
            prepareNext();
            return nextItem != null;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element
         */
        @Override
        public String[] next() {
            if (end && !nextPrepared) {
                throw new NoSuchElementException();
            }
            if (!nextPrepared) {
                prepareNext();
            }
            if (nextItem == null) {
                throw new NoSuchElementException();
            }
            final String[] ret = nextItem;
            // Clear prefetch buffer, need to prepare again next time
            nextItem = null;
            nextPrepared = false;
            // If m == 0, this is the unique item, end iteration
            if (m == 0) {
                end = true;
            }
            return ret;
        }

        /**
         * Advances the state to the next available permutation and places it in {@link #nextItem}.
         * <p>
         * If no more permutations exist, sets {@link #end} to {@code true} and {@link #nextItem} to {@code null}.
         * </p>
         */
        private void prepareNext() {
            // Already prepared or ended
            if (nextPrepared || end) {
                nextPrepared = true;
                return;
            }

            // Special-case m == 0
            if (m == 0) {
                nextItem = new String[0];
                nextPrepared = true;
                // Do not set end here; end will be set after returning this element in next()
                return;
            }

            // Non-recursive DFS simulation until a valid permutation is found or exhausted
            while (depth >= 0) {
                final int start = indices[depth] + 1;
                boolean found = false;
                for (int i = start; i < n; i++) {
                    if (!visited[i]) {
                        // If an element was selected previously at this depth, unmark it
                        if (indices[depth] != -1) {
                            visited[indices[depth]] = false;
                        }
                        indices[depth] = i;
                        visited[i] = true;
                        buffer[depth] = datas[i];
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // No available elements at this level, backtrack to previous depth
                    if (indices[depth] != -1) {
                        visited[indices[depth]] = false;
                        indices[depth] = -1;
                    }
                    depth--;
                    continue;
                }

                // If output depth reached, prepare output (but do not throw/return immediately from iterator logic)
                if (depth == m - 1) {
                    nextItem = Arrays.copyOf(buffer, m);
                    // Unmark current visited to allow finding next candidate at the same level in next call
                    visited[indices[depth]] = false;
                    // Keep depth unchanged (next prepare will start from indices[depth]+1)
                    nextPrepared = true;
                    return;
                } else {
                    // Go deeper: initialize next level to -1 and continue loop
                    depth++;
                    if (depth < m) {
                        indices[depth] = -1;
                    }
                }
            }

            // Loop ended, exhausted all possibilities
            end = true;
            nextItem = null;
            nextPrepared = true;
        }
    }

}
