/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.list;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;

/**
 * A wrapper class for a {@link List}, allowing for custom logic to be executed before and after its methods are called.
 * By default, it simply delegates all calls to the wrapped list. This class can be extended to add custom behaviors.
 *
 * @param <E> The type of elements in the list.
 * @author Kimi Liu
 * @since Java 21+
 */
public class ListWrapper<E> extends SimpleWrapper<List<E>> implements List<E> {

    /**
     * Constructs a new {@code ListWrapper}.
     *
     * @param raw The original list to be wrapped.
     */
    public ListWrapper(final List<E> raw) {
        super(raw);
    }

    /**
     * Returns the number of elements in the wrapped list.
     *
     * @return the number of elements in the wrapped list
     */
    @Override
    public int size() {
        return raw.size();
    }

    /**
     * Returns whether the wrapped list contains no elements.
     *
     * @return {@code true} if the wrapped list contains no elements
     */
    @Override
    public boolean isEmpty() {
        return raw.isEmpty();
    }

    /**
     * Returns whether the wrapped list contains the specified element.
     *
     * @param o the element whose presence is tested
     * @return {@code true} if the wrapped list contains the specified element
     */
    @Override
    public boolean contains(final Object o) {
        return raw.contains(o);
    }

    /**
     * Returns an iterator over the wrapped list.
     *
     * @return an iterator over the wrapped list
     */
    @Override
    public Iterator<E> iterator() {
        return raw.iterator();
    }

    /**
     * Performs the given action for each element in the wrapped list.
     *
     * @param action the action to perform
     */
    @Override
    public void forEach(final Consumer<? super E> action) {
        raw.forEach(action);
    }

    /**
     * Returns an array containing all elements in the wrapped list.
     *
     * @return an array containing all elements in the wrapped list
     */
    @Override
    public Object[] toArray() {
        return raw.toArray();
    }

    /**
     * Returns an array containing all elements in the wrapped list.
     *
     * @param a   the destination array
     * @param <T> the runtime type of the array
     * @return an array containing all elements in the wrapped list
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return raw.toArray(a);
    }

    /**
     * Adds the specified element to the wrapped list.
     *
     * @param e the element to add
     * @return {@code true} if the wrapped list changed
     */
    @Override
    public boolean add(final E e) {
        return raw.add(e);
    }

    /**
     * Removes the specified element from the wrapped list.
     *
     * @param o the element to remove
     * @return {@code true} if the wrapped list changed
     */
    @Override
    public boolean remove(final Object o) {
        return raw.remove(o);
    }

    /**
     * Returns whether the wrapped list contains all elements in the specified collection.
     *
     * @param c the collection whose elements are tested
     * @return {@code true} if the wrapped list contains all elements in the collection
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return raw.containsAll(c);
    }

    /**
     * Adds all elements in the specified collection to the wrapped list.
     *
     * @param c the collection containing elements to add
     * @return {@code true} if the wrapped list changed
     */
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return raw.addAll(c);
    }

    /**
     * Inserts all elements in the specified collection into the wrapped list.
     *
     * @param index the insertion index
     * @param c     the collection containing elements to add
     * @return {@code true} if the wrapped list changed
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        return raw.addAll(index, c);
    }

    /**
     * Removes all elements in the specified collection from the wrapped list.
     *
     * @param c the collection containing elements to remove
     * @return {@code true} if the wrapped list changed
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return raw.removeAll(c);
    }

    /**
     * Removes all elements that satisfy the specified predicate.
     *
     * @param filter the predicate used to select elements for removal
     * @return {@code true} if the wrapped list changed
     */
    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return raw.removeIf(filter);
    }

    /**
     * Retains only the elements contained in the specified collection.
     *
     * @param c the collection containing elements to retain
     * @return {@code true} if the wrapped list changed
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return raw.retainAll(c);
    }

    /**
     * Replaces each element in the wrapped list with the result of the specified operator.
     *
     * @param operator the operator applied to each element
     */
    @Override
    public void replaceAll(final UnaryOperator<E> operator) {
        raw.replaceAll(operator);
    }

    /**
     * Sorts the wrapped list with the specified comparator.
     *
     * @param c the comparator used to order the wrapped list
     */
    @Override
    public void sort(final Comparator<? super E> c) {
        raw.sort(c);
    }

    /**
     * Removes all elements from the wrapped list.
     */
    @Override
    public void clear() {
        raw.clear();
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index the index of the element to return
     * @return the element at the specified index
     */
    @Override
    public E get(final int index) {
        return raw.get(index);
    }

    /**
     * Replaces the element at the specified index.
     *
     * @param index   the index of the element to replace
     * @param element the element to store
     * @return the previous element at the specified index
     */
    @Override
    public E set(final int index, final E element) {
        return raw.set(index, element);
    }

    /**
     * Inserts the specified element at the specified index.
     *
     * @param index   the insertion index
     * @param element the element to insert
     */
    @Override
    public void add(final int index, final E element) {
        raw.add(index, element);
    }

    /**
     * Removes the element at the specified index.
     *
     * @param index the index of the element to remove
     * @return the removed element
     */
    @Override
    public E remove(final int index) {
        return raw.remove(index);
    }

    /**
     * Returns the first index of the specified element.
     *
     * @param o the element to search for
     * @return the first index of the element, or {@code -1} if it is absent
     */
    @Override
    public int indexOf(final Object o) {
        return raw.indexOf(o);
    }

    /**
     * Returns the last index of the specified element.
     *
     * @param o the element to search for
     * @return the last index of the element, or {@code -1} if it is absent
     */
    @Override
    public int lastIndexOf(final Object o) {
        return raw.lastIndexOf(o);
    }

    /**
     * Returns a list iterator over the wrapped list.
     *
     * @return a list iterator over the wrapped list
     */
    @Override
    public ListIterator<E> listIterator() {
        return raw.listIterator();
    }

    /**
     * Returns a list iterator starting at the specified index.
     *
     * @param index the iterator start index
     * @return a list iterator starting at the specified index
     */
    @Override
    public ListIterator<E> listIterator(final int index) {
        return raw.listIterator(index);
    }

    /**
     * Returns a view of the wrapped list between the specified indexes.
     *
     * @param fromIndex the start index, inclusive
     * @param toIndex   the end index, exclusive
     * @return a view of the requested range
     */
    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        return raw.subList(fromIndex, toIndex);
    }

    /**
     * Returns a spliterator over the wrapped list.
     *
     * @return a spliterator over the wrapped list
     */
    @Override
    public Spliterator<E> spliterator() {
        return raw.spliterator();
    }

    /**
     * Returns a sequential stream over the wrapped list.
     *
     * @return a sequential stream over the wrapped list
     */
    @Override
    public Stream<E> stream() {
        return raw.stream();
    }

    /**
     * Returns a parallel stream over the wrapped list.
     *
     * @return a parallel stream over the wrapped list
     */
    @Override
    public Stream<E> parallelStream() {
        return raw.parallelStream();
    }

    /**
     * Returns the hash code of the wrapped list.
     *
     * @return the hash code of the wrapped list
     */
    @Override
    public int hashCode() {
        return this.raw.hashCode();
    }

    /**
     * Compares the specified object with the wrapped list for equality.
     *
     * @param object the object to compare
     * @return {@code true} if the specified object is equal to the wrapped list
     */
    @Override
    public boolean equals(final Object object) {
        return raw.equals(object);
    }

}
