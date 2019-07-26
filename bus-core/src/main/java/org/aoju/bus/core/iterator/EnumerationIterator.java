package org.aoju.bus.core.iterator;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapter to make {@link Enumeration Enumeration} instances appear
 * to be {@link Iterator Iterator} instances.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class EnumerationIterator<E> implements Iterator<E> {

    /**
     * The collection to remove elements from
     */
    private final Collection<? super E> collection;
    /**
     * The enumeration being converted
     */
    private Enumeration<? extends E> enumeration;
    /**
     * The last object retrieved
     */
    private E last;

    /**
     * Constructs a new <code>EnumerationIterator</code> that will not
     * function until {@link #setEnumeration(Enumeration)} is called.
     */
    public EnumerationIterator() {
        this(null, null);
    }

    /**
     * Constructs a new <code>EnumerationIterator</code> that provides
     * an iterator view of the given enumeration.
     *
     * @param enumeration the enumeration to use
     */
    public EnumerationIterator(final Enumeration<? extends E> enumeration) {
        this(enumeration, null);
    }

    /**
     * Constructs a new <code>EnumerationIterator</code> that will remove
     * elements from the specified collection.
     *
     * @param enumeration the enumeration to use
     * @param collection  the collection to remove elements from
     */
    public EnumerationIterator(final Enumeration<? extends E> enumeration, final Collection<? super E> collection) {
        super();
        this.enumeration = enumeration;
        this.collection = collection;
        this.last = null;
    }

    /**
     * Returns true if the underlying enumeration has more elements.
     *
     * @return true if the underlying enumeration has more elements
     * @throws NullPointerException if the underlying enumeration is null
     */
    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    /**
     * Returns the next object from the enumeration.
     *
     * @return the next object from the enumeration
     * @throws NullPointerException if the enumeration is null
     */
    @Override
    public E next() {
        last = enumeration.nextElement();
        return last;
    }

    /**
     * Removes the last retrieved element if a collection is attached.
     * <p>
     * Functions if an associated <code>Collection</code> is known.
     * If so, the first occurrence of the last returned object from this
     * iterator will be removed from the collection.
     *
     * @throws IllegalStateException         <code>next()</code> not called.
     * @throws UnsupportedOperationException if no associated collection
     */
    @Override
    public void remove() {
        if (collection != null) {
            if (last != null) {
                collection.remove(last);
            } else {
                throw new IllegalStateException("next() must have been called for remove() to function");
            }
        } else {
            throw new UnsupportedOperationException("No Collection associated with this Iterator");
        }
    }

    /**
     * Returns the underlying enumeration.
     *
     * @return the underlying enumeration
     */
    public Enumeration<? extends E> getEnumeration() {
        return enumeration;
    }

    /**
     * Sets the underlying enumeration.
     *
     * @param enumeration the new underlying enumeration
     */
    public void setEnumeration(final Enumeration<? extends E> enumeration) {
        this.enumeration = enumeration;
    }

}
