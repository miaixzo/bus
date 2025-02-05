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
 * 列表包装类 提供列表包装，用于在执行列表方法前后自定义处理逻辑
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListWrapper<E> extends SimpleWrapper<List<E>> implements List<E> {

    /**
     * 构造
     *
     * @param raw 原始对象
     */
    public ListWrapper(final List<E> raw) {
        super(raw);
    }

    @Override
    public int size() {
        return raw.size();
    }

    @Override
    public boolean isEmpty() {
        return raw.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return raw.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return raw.iterator();
    }

    @Override
    public void forEach(final Consumer<? super E> action) {
        raw.forEach(action);
    }

    @Override
    public Object[] toArray() {
        return raw.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return raw.toArray(a);
    }

    @Override
    public boolean add(final E e) {
        return raw.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return raw.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return raw.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return raw.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        return raw.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return raw.removeAll(c);
    }

    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return raw.removeIf(filter);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return raw.retainAll(c);
    }

    @Override
    public void replaceAll(final UnaryOperator<E> operator) {
        raw.replaceAll(operator);
    }

    @Override
    public void sort(final Comparator<? super E> c) {
        raw.sort(c);
    }

    @Override
    public void clear() {
        raw.clear();
    }

    @Override
    public E get(final int index) {
        return raw.get(index);
    }

    @Override
    public E set(final int index, final E element) {
        return raw.set(index, element);
    }

    @Override
    public void add(final int index, final E element) {
        raw.add(index, element);
    }

    @Override
    public E remove(final int index) {
        return raw.remove(index);
    }

    @Override
    public int indexOf(final Object o) {
        return raw.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return raw.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return raw.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return raw.listIterator(index);
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        return raw.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<E> spliterator() {
        return raw.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return raw.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return raw.parallelStream();
    }

    @Override
    public int hashCode() {
        return this.raw.hashCode();
    }

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
