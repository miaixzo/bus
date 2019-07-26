package org.aoju.bus.core.iterator;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapter to make an {@link Iterator Iterator} instance appear to be an
 * {@link Enumeration Enumeration} instance.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class IteratorEnumeration<E> implements Enumeration<E> {

    /**
     * The iterator being decorated.
     */
    private Iterator<? extends E> iterator;

    /**
     * Constructs a new <code>IteratorEnumeration</code> that will not function
     * until {@link #setIterator(Iterator) setIterator} is invoked.
     */
    public IteratorEnumeration() {
    }

    /**
     * Constructs a new <code>IteratorEnumeration</code> that will use the given
     * iterator.
     *
     * @param iterator the iterator to use
     */
    public IteratorEnumeration(final Iterator<? extends E> iterator) {
        this.iterator = iterator;
    }

    /**
     * Returns true if the underlying iterator has more elements.
     *
     * @return true if the underlying iterator has more elements
     */
    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    /**
     * Returns the next element from the underlying iterator.
     *
     * @return the next element from the underlying iterator.
     * @throws java.util.NoSuchElementException if the underlying iterator has
     *                                          no more elements
     */
    @Override
    public E nextElement() {
        return iterator.next();
    }

    /**
     * Returns the underlying iterator.
     *
     * @return the underlying iterator
     */
    public Iterator<? extends E> getIterator() {
        return iterator;
    }

    /**
     * Sets the underlying iterator.
     *
     * @param iterator the new underlying iterator
     */
    public void setIterator(final Iterator<? extends E> iterator) {
        this.iterator = iterator;
    }

}
