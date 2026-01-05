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
package org.miaixz.bus.core.compare;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import org.miaixz.bus.core.lang.Chain;

/**
 * A chain of comparators. This chain wraps multiple comparators, and the final comparison result is determined by the
 * order of the comparators. If a comparison results in equality, the chain proceeds to the next comparator; otherwise,
 * the result is returned immediately. This class is adapted from Apache Commons Collections.
 *
 * @param <E> the type of objects to be compared.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ComparatorChain<E> implements Chain<Comparator<E>, ComparatorChain<E>>, Comparator<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852260773785L;

    /**
     * The list of comparators in the chain.
     */
    private final List<Comparator<E>> chain;
    /**
     * A BitSet to track whether the corresponding comparator is reversed.
     */
    private final BitSet orderingBits;
    /**
     * Whether the comparator chain is locked. A locked chain cannot have new comparators added. The chain is locked
     * when the first comparison begins.
     */
    private boolean lock = false;

    /**
     * Constructs an empty comparator chain. At least one comparator must be added, or a
     * {@link UnsupportedOperationException} will be thrown during comparison.
     */
    public ComparatorChain() {
        this(new ArrayList<>(), new BitSet());
    }

    /**
     * Constructs a chain with a single comparator in forward order.
     *
     * @param comparator the first comparator in the chain.
     */
    public ComparatorChain(final Comparator<E> comparator) {
        this(comparator, false);
    }

    /**
     * Constructs a chain with a single comparator and a specified order.
     *
     * @param comparator the first comparator in the chain.
     * @param reverse    if {@code true}, the comparator is reversed; otherwise, it is in forward order.
     */
    public ComparatorChain(final Comparator<E> comparator, final boolean reverse) {
        chain = new ArrayList<>(1);
        chain.add(comparator);
        orderingBits = new BitSet(1);
        if (reverse) {
            orderingBits.set(0);
        }
    }

    /**
     * Constructs a chain from a list of comparators.
     *
     * @param list the list of comparators.
     * @see #ComparatorChain(List, BitSet)
     */
    public ComparatorChain(final List<Comparator<E>> list) {
        this(list, new BitSet(list.size()));
    }

    /**
     * Constructs a chain from a list of comparators and a BitSet specifying the order for each. The boolean values in
     * the BitSet must correspond to the {@link Comparator} list, where {@code true} means reversed order and
     * {@code false} means forward order.
     *
     * @param list the list of {@link Comparator}s.
     * @param bits a {@link BitSet} where each bit corresponds to a comparator, indicating if it is reversed.
     */
    public ComparatorChain(final List<Comparator<E>> list, final BitSet bits) {
        chain = list;
        orderingBits = bits;
    }

    /**
     * Creates a new {@code ComparatorChain} with a single comparator.
     *
     * @param <E>        the type of objects to be compared.
     * @param comparator the comparator.
     * @return a new {@code ComparatorChain}.
     */
    public static <E> ComparatorChain<E> of(final Comparator<E> comparator) {
        return of(comparator, false);
    }

    /**
     * Creates a new {@code ComparatorChain} with a single comparator and a specified order.
     *
     * @param <E>        the type of objects to be compared.
     * @param comparator the comparator.
     * @param reverse    if {@code true}, the comparator is reversed.
     * @return a new {@code ComparatorChain}.
     */
    public static <E> ComparatorChain<E> of(final Comparator<E> comparator, final boolean reverse) {
        return new ComparatorChain<>(comparator, reverse);
    }

    /**
     * Creates a new {@code ComparatorChain} from an array of comparators.
     *
     * @param <E>         the type of objects to be compared.
     * @param comparators the array of comparators.
     * @return a new {@code ComparatorChain}.
     */
    @SafeVarargs
    public static <E> ComparatorChain<E> of(final Comparator<E>... comparators) {
        return of(Arrays.asList(comparators));
    }

    /**
     * Creates a new {@code ComparatorChain} from a list of comparators.
     *
     * @param <E>         the type of objects to be compared.
     * @param comparators the list of comparators.
     * @return a new {@code ComparatorChain}.
     */
    public static <E> ComparatorChain<E> of(final List<Comparator<E>> comparators) {
        return new ComparatorChain<>(comparators);
    }

    /**
     * Creates a new {@code ComparatorChain} from a list of comparators and a BitSet specifying the order for each.
     *
     * @param <E>         the type of objects to be compared.
     * @param comparators the list of comparators.
     * @param bits        a {@link BitSet} where each bit corresponds to a comparator, indicating if it is reversed.
     * @return a new {@code ComparatorChain}.
     */
    public static <E> ComparatorChain<E> of(final List<Comparator<E>> comparators, final BitSet bits) {
        return new ComparatorChain<>(comparators, bits);
    }

    /**
     * Adds a comparator to the end of the chain in forward order.
     *
     * @param comparator the {@link Comparator} to add.
     * @return this {@code ComparatorChain}.
     */
    public ComparatorChain<E> addComparator(final Comparator<E> comparator) {
        return addComparator(comparator, false);
    }

    /**
     * Adds a comparator to the end of the chain with a specified order.
     *
     * @param comparator the {@link Comparator} to add.
     * @param reverse    if {@code true}, the comparator is reversed.
     * @return this {@code ComparatorChain}.
     */
    public ComparatorChain<E> addComparator(final Comparator<E> comparator, final boolean reverse) {
        checkLocked();

        chain.add(comparator);
        if (reverse) {
            orderingBits.set(chain.size() - 1);
        }
        return this;
    }

    /**
     * Replaces the comparator at the specified index, preserving the original sort order.
     *
     * @param index      the index of the comparator to replace.
     * @param comparator the new {@link Comparator}.
     * @return this {@code ComparatorChain}.
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size()).
     */
    public ComparatorChain<E> setComparator(final int index, final Comparator<E> comparator)
            throws IndexOutOfBoundsException {
        return setComparator(index, comparator, false);
    }

    /**
     * Replaces the comparator at the specified index with a new sort order.
     *
     * @param index      the index of the comparator to replace.
     * @param comparator the new {@link Comparator}.
     * @param reverse    if {@code true}, the new comparator is reversed.
     * @return this {@code ComparatorChain}.
     */
    public ComparatorChain<E> setComparator(final int index, final Comparator<E> comparator, final boolean reverse) {
        checkLocked();

        chain.set(index, comparator);
        if (reverse) {
            orderingBits.set(index);
        } else {
            orderingBits.clear(index);
        }
        return this;
    }

    /**
     * Sets the sort order at the specified index to forward.
     *
     * @param index the index of the comparator.
     * @return this {@code ComparatorChain}.
     */
    public ComparatorChain<E> setForwardSort(final int index) {
        checkLocked();
        orderingBits.clear(index);
        return this;
    }

    /**
     * Sets the sort order at the specified index to reverse.
     *
     * @param index the index of the comparator.
     * @return this {@code ComparatorChain}.
     */
    public ComparatorChain<E> setReverseSort(final int index) {
        checkLocked();
        orderingBits.set(index);
        return this;
    }

    /**
     * Returns the number of comparators in the chain.
     *
     * @return the number of comparators.
     */
    public int size() {
        return chain.size();
    }

    /**
     * Checks if the chain is locked. The chain becomes locked after the first comparison.
     *
     * @return {@code true} if the chain is locked and cannot be modified; {@code false} otherwise.
     */
    public boolean isLocked() {
        return lock;
    }

    /**
     * Returns an iterator over elements of type T.
     *
     * @return an Iterator
     */
    @Override
    public Iterator<Comparator<E>> iterator() {
        return this.chain.iterator();
    }

    /**
     * Addchain method.
     *
     * @return the ComparatorChain&lt;E&gt; value
     */
    @Override
    public ComparatorChain<E> addChain(final Comparator<E> element) {
        return this.addComparator(element);
    }

    /**
     * Performs the comparison by iterating through the chain. If a comparison results in equality, the chain proceeds
     * to the next comparator; otherwise, the result is returned immediately.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     * @throws UnsupportedOperationException if the comparator chain is empty.
     */
    @Override
    public int compare(final E o1, final E o2) throws UnsupportedOperationException {
        if (!lock) {
            checkChainIntegrity();
            lock = true;
        }

        final Iterator<Comparator<E>> comparators = chain.iterator();
        for (int comparatorIndex = 0; comparators.hasNext(); ++comparatorIndex) {
            final Comparator<? super E> comparator = comparators.next();
            int retval = comparator.compare(o1, o2);
            if (retval != 0) {
                if (orderingBits.get(comparatorIndex)) {
                    retval = (retval > 0) ? -1 : 1;
                }
                return retval;
            }
        }

        return 0;
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        int hash = 0;
        if (null != chain) {
            hash ^= chain.hashCode();
        }
        if (null != orderingBits) {
            hash ^= orderingBits.hashCode();
        }
        return hash;
    }

    /**
     * Checks if this object equals another object.
     *
     * @param object the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (null == object) {
            return false;
        }
        if (object.getClass().equals(this.getClass())) {
            final ComparatorChain<?> otherChain = (ComparatorChain<?>) object;
            return Objects.equals(this.orderingBits, otherChain.orderingBits) && this.chain.equals(otherChain.chain);
        }
        return false;
    }

    /**
     * Throws an exception if the chain is locked.
     *
     * @throws UnsupportedOperationException if the chain is locked.
     */
    private void checkLocked() {
        if (lock) {
            throw new UnsupportedOperationException(
                    "Comparator ordering cannot be changed after the first comparison is performed");
        }
    }

    /**
     * Throws an exception if the chain is empty.
     *
     * @throws UnsupportedOperationException if the chain is empty.
     */
    private void checkChainIntegrity() {
        if (chain.isEmpty()) {
            throw new UnsupportedOperationException("ComparatorChains must contain at least one Comparator");
        }
    }

}
