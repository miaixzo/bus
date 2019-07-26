package org.aoju.bus.core.iterator;

import java.util.Iterator;

/**
 * Defines an iterator that can be reset back to an initial state.
 * <p>
 * This interface allows an iterator to be repeatedly reused.
 *
 * @param <E> the type to iterate over
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ResettableIterator<E> extends Iterator<E> {

    /**
     * Resets the iterator back to the position at which the iterator
     * was created.
     */
    void reset();

}
