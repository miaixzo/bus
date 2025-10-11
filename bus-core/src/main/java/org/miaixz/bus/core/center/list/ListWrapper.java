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
 * @since Java 17+
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
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return raw.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return raw.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object o) {
        return raw.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator() {
        return raw.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forEach(final Consumer<? super E> action) {
        raw.forEach(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return raw.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return raw.toArray(a);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final E e) {
        return raw.add(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(final Object o) {
        return raw.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return new HashSet<>(raw).containsAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return raw.addAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        return raw.addAll(index, c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return raw.removeAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return raw.removeIf(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return raw.retainAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceAll(final UnaryOperator<E> operator) {
        raw.replaceAll(operator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sort(final Comparator<? super E> c) {
        raw.sort(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        raw.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(final int index) {
        return raw.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E set(final int index, final E element) {
        return raw.set(index, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final int index, final E element) {
        raw.add(index, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E remove(final int index) {
        return raw.remove(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final Object o) {
        return raw.indexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(final Object o) {
        return raw.lastIndexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator() {
        return raw.listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator(final int index) {
        return raw.listIterator(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        return raw.subList(fromIndex, toIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spliterator<E> spliterator() {
        return raw.spliterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<E> stream() {
        return raw.stream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<E> parallelStream() {
        return raw.parallelStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.raw.hashCode();
    }

    /**
     * {@inheritDoc}
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
