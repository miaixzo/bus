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
package org.miaixz.bus.core.center.queue;

import java.util.*;

/**
 * Linked list implementation of the {@link Deque} interface where the link pointers are tightly integrated with the
 * element. Linked deques have no capacity restrictions; they grow as necessary to support usage. They are not
 * thread-safe; in the absence of external synchronization, they do not support concurrent access by multiple threads.
 * Null elements are prohibited.
 * <p>
 * Most <b>LinkedDeque</b> operations run in constant time by assuming that the {@link Linked} parameter is associated
 * with the deque instance. Any usage that violates this assumption will result in non-deterministic behavior.
 * <p>
 * The iterators returned by this class are <em>not</em> <i>fail-fast</i>: If the deque is modified at any time after
 * the iterator is created, the iterator will be in an unknown state. Thus, in the face of concurrent modification, the
 * iterator risks arbitrary, non-deterministic behavior at an undetermined time in the future.
 *
 * @param <E> the type of elements held in this collection
 * @author Kimi Liu
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">
 *      http://code.google.com/p/concurrentlinkedhashmap/</a>
 * @since Java 17+
 */
public class LinkedDeque<E extends Linked<E>> extends AbstractCollection<E> implements Deque<E> {

    /**
     * Pointer to first node. Invariant: (first == null && last == null) || (first.prev == null)
     */
    E first;

    /**
     * Pointer to last node. Invariant: (first == null && last == null) || (last.next == null)
     */
    E last;

    /**
     * Links the element to the front of the deque so that it becomes the first element.
     *
     * @param e the unlinked element
     */
    void linkFirst(final E e) {
        final E f = first;
        first = e;

        if (f == null) {
            last = e;
        } else {
            f.setPrevious(e);
            e.setNext(f);
        }
    }

    /**
     * Links the element to the back of the deque so that it becomes the last element.
     *
     * @param e the unlinked element
     */
    void linkLast(final E e) {
        final E l = last;
        last = e;

        if (l == null) {
            first = e;
        } else {
            l.setNext(e);
            e.setPrevious(l);
        }
    }

    /**
     * Unlinks the non-null first element.
     */
    E unlinkFirst() {
        final E f = first;
        final E next = f.getNext();
        f.setNext(null);

        first = next;
        if (next == null) {
            last = null;
        } else {
            next.setPrevious(null);
        }
        return f;
    }

    /**
     * Unlinks the non-null last element.
     */
    E unlinkLast() {
        final E l = last;
        final E prev = l.getPrevious();
        l.setPrevious(null);
        last = prev;
        if (prev == null) {
            first = null;
        } else {
            prev.setNext(null);
        }
        return l;
    }

    /**
     * Unlinks the non-null element.
     */
    void unlink(final E e) {
        final E prev = e.getPrevious();
        final E next = e.getNext();

        if (prev == null) {
            first = next;
        } else {
            prev.setNext(next);
            e.setPrevious(null);
        }

        if (next == null) {
            last = prev;
        } else {
            next.setPrevious(prev);
            e.setNext(null);
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns {@code true} if this deque contains no elements.
     *
     * @return {@code true} if this deque contains no elements
     */
    @Override
    public boolean isEmpty() {
        return (first == null);
    }

    void checkNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Beware that, unlike in most collections, this method is <em>NOT</em> a constant-time operation.
     */
    @Override
    public int size() {
        int size = 0;
        for (E e = first; e != null; e = e.getNext()) {
            size++;
        }
        return size;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Removes all of the elements from this deque. The deque will be empty after this call returns.
     */
    @Override
    public void clear() {
        for (E e = first; e != null;) {
            final E next = e.getNext();
            e.setPrevious(null);
            e.setNext(null);
            e = next;
        }
        first = last = null;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns {@code true} if this deque contains the specified element.
     *
     * @param o the element to check for presence
     * @return {@code true} if this deque contains the specified element
     */
    @Override
    public boolean contains(final Object o) {
        return (o instanceof Linked<?>) && contains((Linked<?>) o);
    }

    // A fast-path containment check
    boolean contains(final Linked<?> e) {
        return (e.getPrevious() != null) || (e.getNext() != null) || (e == first);
    }

    /**
     * Moves the element to the front of the deque so that it becomes the first element.
     *
     * @param e the linked element
     */
    public void moveToFront(final E e) {
        if (e != first) {
            unlink(e);
            linkFirst(e);
        }
    }

    /**
     * Moves the element to the back of the deque so that it becomes the last element.
     *
     * @param e the linked element
     */
    public void moveToBack(final E e) {
        if (e != last) {
            unlink(e);
            linkLast(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves, but does not remove, the head of the queue represented by this deque (in other words, the first
     * element of this deque), or returns {@code null} if this deque is empty.
     *
     * @return the head of the queue, or {@code null} if this deque is empty
     */
    @Override
    public E peek() {
        return peekFirst();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves, but does not remove, the first element of this deque, or returns {@code null} if this deque is empty.
     *
     * @return the first element of this deque, or {@code null} if this deque is empty
     */
    @Override
    public E peekFirst() {
        return first;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves, but does not remove, the last element of this deque, or returns {@code null} if this deque is empty.
     *
     * @return the last element of this deque, or {@code null} if this deque is empty
     */
    @Override
    public E peekLast() {
        return last;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves, but does not remove, the first element of this deque. This method differs from {@link #peekFirst} only
     * in that it throws an exception if this deque is empty.
     *
     * @return the first element of this deque
     * @throws NoSuchElementException if this deque is empty
     */
    @Override
    public E getFirst() {
        checkNotEmpty();
        return peekFirst();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves, but does not remove, the last element of this deque. This method differs from {@link #peekLast} only
     * in that it throws an exception if this deque is empty.
     *
     * @return the last element of this deque
     * @throws NoSuchElementException if this deque is empty
     */
    @Override
    public E getLast() {
        checkNotEmpty();
        return peekLast();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves, but does not remove, the head of the queue represented by this deque.
     *
     * @return the head of the queue
     * @throws NoSuchElementException if this deque is empty
     */
    @Override
    public E element() {
        return getFirst();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Adds the specified element as the tail (last element) of this deque.
     *
     * @param e the element to add
     * @return {@code true} if the element was added successfully
     */
    @Override
    public boolean offer(final E e) {
        return offerLast(e);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Inserts the specified element at the front of this deque.
     *
     * @param e the element to insert
     * @return {@code true} if the element was added successfully, {@code false} if already present
     */
    @Override
    public boolean offerFirst(final E e) {
        if (contains(e)) {
            return false;
        }
        linkFirst(e);
        return true;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Inserts the specified element at the end of this deque.
     *
     * @param e the element to insert
     * @return {@code true} if the element was added successfully, {@code false} if already present
     */
    @Override
    public boolean offerLast(final E e) {
        if (contains(e)) {
            return false;
        }
        linkLast(e);
        return true;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Adds the specified element as the tail (last element) of this deque.
     *
     * @param e the element to add
     * @return {@code true}
     * @throws IllegalArgumentException if the element is already present
     */
    @Override
    public boolean add(final E e) {
        return offerLast(e);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Inserts the specified element at the front of this deque.
     *
     * @param e the element to insert
     * @throws IllegalArgumentException if the element is already present
     */
    @Override
    public void addFirst(final E e) {
        if (!offerFirst(e)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Inserts the specified element at the end of this deque.
     *
     * @param e the element to insert
     * @throws IllegalArgumentException if the element is already present
     */
    @Override
    public void addLast(final E e) {
        if (!offerLast(e)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves and removes the head of the queue represented by this deque (in other words, the first element of this
     * deque), or returns {@code null} if this deque is empty.
     *
     * @return the head of the queue, or {@code null} if this deque is empty
     */
    @Override
    public E poll() {
        return pollFirst();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves and removes the first element of this deque, or returns {@code null} if this deque is empty.
     *
     * @return the first element of this deque, or {@code null} if this deque is empty
     */
    @Override
    public E pollFirst() {
        return isEmpty() ? null : unlinkFirst();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves and removes the last element of this deque, or returns {@code null} if this deque is empty.
     *
     * @return the last element of this deque, or {@code null} if this deque is empty
     */
    @Override
    public E pollLast() {
        return isEmpty() ? null : unlinkLast();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves and removes the head of the queue represented by this deque.
     *
     * @return the head of the queue
     * @throws NoSuchElementException if this deque is empty
     */
    @Override
    public E remove() {
        return removeFirst();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Removes the first occurrence of the specified element from this deque.
     *
     * @param o the element to remove
     * @return {@code true} if the element was removed
     */
    @Override
    public boolean remove(final Object o) {
        return (o instanceof Linked<?>) && remove((E) o);
    }

    // A fast-path removal
    boolean remove(final E e) {
        if (contains(e)) {
            unlink(e);
            return true;
        }
        return false;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves and removes the first element of this deque.
     *
     * @return the first element of this deque
     * @throws NoSuchElementException if this deque is empty
     */
    @Override
    public E removeFirst() {
        checkNotEmpty();
        return pollFirst();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Removes the first occurrence of the specified element from this deque.
     *
     * @param o the element to remove
     * @return {@code true} if the element was removed
     */
    @Override
    public boolean removeFirstOccurrence(final Object o) {
        return remove(o);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Retrieves and removes the last element of this deque.
     *
     * @return the last element of this deque
     * @throws NoSuchElementException if this deque is empty
     */
    @Override
    public E removeLast() {
        checkNotEmpty();
        return pollLast();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Removes the last occurrence of the specified element from this deque.
     *
     * @param o the element to remove
     * @return {@code true} if the element was removed
     */
    @Override
    public boolean removeLastOccurrence(final Object o) {
        return remove(o);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Removes all of this deque's elements that are also contained in the specified collection.
     *
     * @param c the collection containing elements to remove
     * @return {@code true} if the deque was modified
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean modified = false;
        for (final Object o : c) {
            modified |= remove(o);
        }
        return modified;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Pushes an element onto the stack represented by this deque (in other words, at the head of this deque).
     *
     * @param e the element to push
     */
    @Override
    public void push(final E e) {
        addFirst(e);
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Pops an element from the stack represented by this deque (in other words, removes and returns the first element
     * of this deque).
     *
     * @return the element at the front of this deque
     */
    @Override
    public E pop() {
        return removeFirst();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns an iterator over the elements in this deque in proper sequence.
     *
     * @return an iterator over the elements in this deque in proper sequence
     */
    @Override
    public Iterator<E> iterator() {
        return new AbstractLinkedIterator(first) {

            /**
             * Computenext method.
             *
             * @return the E value
             */
            @Override
            E computeNext() {
                return cursor.getNext();
            }
        };
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Returns an iterator over the elements in this deque in reverse sequential order.
     *
     * @return an iterator over the elements in this deque in reverse order
     */
    @Override
    public Iterator<E> descendingIterator() {
        return new AbstractLinkedIterator(last) {

            /**
             * Computenext method.
             *
             * @return the E value
             */
            @Override
            E computeNext() {
                return cursor.getPrevious();
            }
        };
    }

    /**
     * Abstract iterator for traversing the linked deque elements.
     * <p>
     * This class provides the basic iteration functionality for the LinkedDeque, allowing traversal in either forward
     * or reverse direction depending on the implementation of the {@link #computeNext()} method.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    abstract class AbstractLinkedIterator implements Iterator<E> {

        E cursor;

        /**
         * Creates an iterator that can can traverse the deque.
         *
         * @param start the initial element to begin traversal from
         */
        AbstractLinkedIterator(final E start) {
            cursor = start;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns {@code true} if the iteration has more elements.
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return (cursor != null);
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the next element in the iteration.
         *
         * @return the next element
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final E e = cursor;
            cursor = computeNext();
            return e;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * This operation is not supported.
         *
         * @throws UnsupportedOperationException always
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Retrieves the next element to traverse to or {@code null} if there are no more elements.
         */
        abstract E computeNext();
    }

}
