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
     * Description inherited from parent class or interface.
     */
    @Override
    public int size() {
        return raw.size();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean isEmpty() {
        return raw.isEmpty();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean contains(final Object o) {
        return raw.contains(o);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Iterator<E> iterator() {
        return raw.iterator();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void forEach(final Consumer<? super E> action) {
        raw.forEach(action);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Object[] toArray() {
        return raw.toArray();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return raw.toArray(a);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean add(final E e) {
        return raw.add(e);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean remove(final Object o) {
        return raw.remove(o);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return new HashSet<>(raw).containsAll(c);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return raw.addAll(c);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        return raw.addAll(index, c);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return raw.removeAll(c);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return raw.removeIf(filter);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return raw.retainAll(c);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void replaceAll(final UnaryOperator<E> operator) {
        raw.replaceAll(operator);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void sort(final Comparator<? super E> c) {
        raw.sort(c);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void clear() {
        raw.clear();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public E get(final int index) {
        return raw.get(index);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public E set(final int index, final E element) {
        return raw.set(index, element);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void add(final int index, final E element) {
        raw.add(index, element);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public E remove(final int index) {
        return raw.remove(index);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public int indexOf(final Object o) {
        return raw.indexOf(o);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public int lastIndexOf(final Object o) {
        return raw.lastIndexOf(o);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public ListIterator<E> listIterator() {
        return raw.listIterator();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public ListIterator<E> listIterator(final int index) {
        return raw.listIterator(index);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        return raw.subList(fromIndex, toIndex);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Spliterator<E> spliterator() {
        return raw.spliterator();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Stream<E> stream() {
        return raw.stream();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Stream<E> parallelStream() {
        return raw.parallelStream();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public int hashCode() {
        return this.raw.hashCode();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final ListWrapper<?> that = (ListWrapper<?>) object;
        return Objects.equals(raw, that.raw);
    }

}
