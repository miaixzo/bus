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
package org.miaixz.bus.core.center.map;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * A three-value table structure that stores triplets of (left, middle, right) values. This implementation allows for
 * duplicate entries and provides lookup operations based on any of the three values.
 * <p>
 * It is backed by three parallel {@link List}s, which means lookup operations (e.g., {@code getLeftByMiddle}) involve
 * linear scans and can be less performant than hash-based structures for large datasets.
 *
 * @param <L> The type of the left value.
 * @param <M> The type of the middle value.
 * @param <R> The type of the right value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class TripletTable<L, M, R> implements Iterable<Triplet<L, M, R>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852276252739L;

    /**
     * The list storing the left values of the triplets.
     */
    private final List<L> lefts;
    /**
     * The list storing the middle values of the triplets.
     */
    private final List<M> middles;
    /**
     * The list storing the right values of the triplets.
     */
    private final List<R> rights;

    /**
     * Constructs a {@code TripleTable} from a list of {@link Triplet} objects.
     *
     * @param triples A list of {@link Triplet} objects to populate the table. Must not be {@code null}.
     */
    public TripletTable(final List<Triplet<L, M, R>> triples) {
        this(Assert.notNull(triples, "Triplets list must not be null").size());
        for (final Triplet<L, M, R> triplet : triples) {
            put(triplet.getLeft(), triplet.getMiddle(), triplet.getRight());
        }
    }

    /**
     * Constructs an empty {@code TripleTable} with the specified initial capacity for its internal lists.
     *
     * @param size The initial capacity for the internal lists.
     */
    public TripletTable(final int size) {
        this(new ArrayList<>(size), new ArrayList<>(size), new ArrayList<>(size));
    }

    /**
     * Constructs a {@code TripleTable} with pre-existing lists for left, middle, and right values. All three lists must
     * have the same size, and their elements at corresponding indices form a triplet.
     *
     * @param lefts   The list of left values. Must not be {@code null}.
     * @param middles The list of middle values. Must not be {@code null}.
     * @param rights  The list of right values. Must not be {@code null}.
     * @throws IllegalArgumentException if the sizes of the provided lists are not equal.
     */
    public TripletTable(final List<L> lefts, final List<M> middles, final List<R> rights) {
        Assert.notNull(lefts, "Lefts list must not be null");
        Assert.notNull(middles, "Middles list must not be null");
        Assert.notNull(rights, "Rights list must not be null");
        final int size = lefts.size();
        if (size != middles.size() || size != rights.size()) {
            throw new IllegalArgumentException("All lists must have the same size!");
        }

        this.lefts = lefts;
        this.middles = middles;
        this.rights = rights;
    }

    /**
     * Retrieves the left value associated with the first occurrence of the specified middle value.
     *
     * @param mValue The middle value to search for.
     * @return The left value, or {@code null} if the middle value is not found.
     */
    public L getLeftByMiddle(final M mValue) {
        final int index = this.middles.indexOf(mValue);
        if (index > -1) {
            return this.lefts.get(index);
        }
        return null;
    }

    /**
     * Retrieves the left value associated with the first occurrence of the specified right value.
     *
     * @param rValue The right value to search for.
     * @return The left value, or {@code null} if the right value is not found.
     */
    public L getLeftByRight(final R rValue) {
        final int index = this.rights.indexOf(rValue);
        if (index > -1) {
            return this.lefts.get(index);
        }
        return null;
    }

    /**
     * Retrieves the middle value associated with the first occurrence of the specified left value.
     *
     * @param lValue The left value to search for.
     * @return The middle value, or {@code null} if the left value is not found.
     */
    public M getMiddleByLeft(final L lValue) {
        final int index = this.lefts.indexOf(lValue);
        if (index > -1) {
            return this.middles.get(index);
        }
        return null;
    }

    /**
     * Retrieves the middle value associated with the first occurrence of the specified right value.
     *
     * @param rValue The right value to search for.
     * @return The middle value, or {@code null} if the right value is not found.
     */
    public M getMiddleByRight(final R rValue) {
        final int index = this.rights.indexOf(rValue);
        if (index > -1) {
            return this.middles.get(index);
        }
        return null;
    }

    /**
     * Retrieves the left value at the specified index.
     *
     * @param index The index of the triplet.
     * @return The left value at the given index.
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= size()}).
     */
    public L getLeft(final int index) {
        return this.lefts.get(index);
    }

    /**
     * Retrieves the middle value at the specified index.
     *
     * @param index The index of the triplet.
     * @return The middle value at the given index.
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= size()}).
     */
    public M getMiddle(final int index) {
        return this.middles.get(index);
    }

    /**
     * Retrieves the right value at the specified index.
     *
     * @param index The index of the triplet.
     * @return The right value at the given index.
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= size()}).
     */
    public R getRight(final int index) {
        return this.rights.get(index);
    }

    /**
     * Retrieves the right value associated with the first occurrence of the specified left value.
     *
     * @param lValue The left value to search for.
     * @return The right value, or {@code null} if the left value is not found.
     */
    public R getRightByLeft(final L lValue) {
        final int index = this.lefts.indexOf(lValue);
        if (index > -1) {
            return this.rights.get(index);
        }
        return null;
    }

    /**
     * Retrieves the right value associated with the first occurrence of the specified middle value.
     *
     * @param mValue The middle value to search for.
     * @return The right value, or {@code null} if the middle value is not found.
     */
    public R getRightByMiddle(final M mValue) {
        final int index = this.middles.indexOf(mValue);
        if (index > -1) {
            return this.rights.get(index);
        }
        return null;
    }

    /**
     * Checks if the table contains the specified left value.
     *
     * @param left The left value to check for.
     * @return {@code true} if the left value is found in the table, {@code false} otherwise.
     */
    public boolean containLeft(final L left) {
        return this.lefts.contains(left);
    }

    /**
     * Checks if the table contains the specified middle value.
     *
     * @param middle The middle value to check for.
     * @return {@code true} if the middle value is found in the table, {@code false} otherwise.
     */
    public boolean containMiddle(final M middle) {
        return this.middles.contains(middle);
    }

    /**
     * Checks if the table contains the specified right value.
     *
     * @param right The right value to check for.
     * @return {@code true} if the right value is found in the table, {@code false} otherwise.
     */
    public boolean containRight(final R right) {
        return this.rights.contains(right);
    }

    /**
     * Retrieves the index of the first occurrence of the specified left value.
     *
     * @param left The left value to search for.
     * @return The index of the first occurrence of the left value, or -1 if not found.
     */
    public int indexOfLeft(final L left) {
        return this.lefts.indexOf(left);
    }

    /**
     * Retrieves the index of the first occurrence of the specified middle value.
     *
     * @param middle The middle value to search for.
     * @return The index of the first occurrence of the middle value, or -1 if not found.
     */
    public int indexOfMiddle(final M middle) {
        return this.middles.indexOf(middle);
    }

    /**
     * Retrieves the index of the first occurrence of the specified right value.
     *
     * @param right The right value to search for.
     * @return The index of the first occurrence of the right value, or -1 if not found.
     */
    public int indexOfRight(final R right) {
        return this.rights.indexOf(right);
    }

    /**
     * Retrieves the {@link Triplet} associated with the first occurrence of the specified left value.
     *
     * @param lValue The left value to search for.
     * @return The {@link Triplet} containing the left, middle, and right values, or {@code null} if the left value is
     *         not found.
     */
    public Triplet<L, M, R> getByLeft(final L lValue) {
        final int index = this.lefts.indexOf(lValue);
        if (index > -1) {
            return new Triplet<>(lefts.get(index), middles.get(index), rights.get(index));
        }
        return null;
    }

    /**
     * Retrieves the {@link Triplet} associated with the first occurrence of the specified middle value.
     *
     * @param mValue The middle value to search for.
     * @return The {@link Triplet} containing the left, middle, and right values, or {@code null} if the middle value is
     *         not found.
     */
    public Triplet<L, M, R> getByMiddle(final M mValue) {
        final int index = this.middles.indexOf(mValue);
        if (index > -1) {
            return new Triplet<>(lefts.get(index), middles.get(index), rights.get(index));
        }
        return null;
    }

    /**
     * Retrieves the {@link Triplet} associated with the first occurrence of the specified right value.
     *
     * @param rValue The right value to search for.
     * @return The {@link Triplet} containing the left, middle, and right values, or {@code null} if the right value is
     *         not found.
     */
    public Triplet<L, M, R> getByRight(final R rValue) {
        final int index = this.rights.indexOf(rValue);
        if (index > -1) {
            return new Triplet<>(lefts.get(index), middles.get(index), rights.get(index));
        }
        return null;
    }

    /**
     * Returns an unmodifiable list of all left values in the table.
     *
     * @return An unmodifiable {@link List} of left values.
     */
    public List<L> getLefts() {
        return ListKit.view(this.lefts);
    }

    /**
     * Returns an unmodifiable list of all middle values in the table.
     *
     * @return An unmodifiable {@link List} of middle values.
     */
    public List<M> getMiddles() {
        return ListKit.view(this.middles);
    }

    /**
     * Returns an unmodifiable list of all right values in the table.
     *
     * @return An unmodifiable {@link List} of right values.
     */
    public List<R> getRights() {
        return ListKit.view(this.rights);
    }

    /**
     * Returns the number of entries (triplets) in this table.
     *
     * @return The number of entries.
     */
    public int size() {
        return this.lefts.size();
    }

    /**
     * Adds a new triplet (left, middle, right values) to the table.
     *
     * @param lValue The left value.
     * @param mValue The middle value.
     * @param rValue The right value.
     * @return This {@code TripleTable} instance for method chaining.
     */
    public TripletTable<L, M, R> put(final L lValue, final M mValue, final R rValue) {
        this.lefts.add(lValue);
        this.middles.add(mValue);
        this.rights.add(rValue);
        return this;
    }

    /**
     * Sets the left value at the specified index.
     *
     * @param index  The index of the entry to modify.
     * @param lValue The new left value.
     * @return This {@code TripleTable} instance for method chaining.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public TripletTable<L, M, R> setLeft(final int index, final L lValue) {
        this.lefts.set(index, lValue);
        return this;
    }

    /**
     * Sets the middle value at the specified index.
     *
     * @param index  The index of the entry to modify.
     * @param mValue The new middle value.
     * @return This {@code TripleTable} instance for method chaining.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public TripletTable<L, M, R> setMiddle(final int index, final M mValue) {
        this.middles.set(index, mValue);
        return this;
    }

    /**
     * Sets the right value at the specified index.
     *
     * @param index  The index of the entry to modify.
     * @param rValue The new right value.
     * @return This {@code TripleTable} instance for method chaining.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public TripletTable<L, M, R> setRight(final int index, final R rValue) {
        this.rights.set(index, rValue);
        return this;
    }

    /**
     * Clears all entries from the table.
     *
     * @return This {@code TripleTable} instance for method chaining.
     */
    public TripletTable<L, M, R> clear() {
        this.lefts.clear();
        this.middles.clear();
        this.rights.clear();
        return this;
    }

    /**
     * Removes the entry (triplet) at the specified index from the table.
     *
     * @param index The index of the entry to remove.
     * @return This {@code TripleTable} instance for method chaining.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public TripletTable<L, M, R> remove(final int index) {
        this.lefts.remove(index);
        this.middles.remove(index);
        this.rights.remove(index);
        return this;
    }

    @Override
    public Iterator<Triplet<L, M, R>> iterator() {
        final int size = this.size();
        return new Iterator<>() {

            private int index = -1;

            @Override
            public boolean hasNext() {
                return index + 1 < size;
            }

            @Override
            public Triplet<L, M, R> next() {
                index++;
                return new Triplet<>(getLeft(index), getMiddle(index), getRight(index));
            }
        };
    }

}
