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
package org.miaixz.bus.setting.magic;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ProxyList<E> extends List<E> {

    /**
     * get the real list.
     *
     * @return real list.
     */
    List<E> getProxyList();

    @Override
    default int size() {
        return getProxyList().size();
    }

    @Override
    default boolean isEmpty() {
        return getProxyList().isEmpty();
    }

    @Override
    default boolean contains(Object o) {
        return getProxyList().contains(o);
    }

    @Override
    default Iterator<E> iterator() {
        return getProxyList().iterator();
    }

    @Override
    default Object[] toArray() {
        return getProxyList().toArray();
    }

    @Override
    default <T> T[] toArray(T[] a) {
        return getProxyList().toArray(a);
    }

    @Override
    default boolean add(E property) {
        return getProxyList().add(property);
    }

    @Override
    default boolean remove(Object o) {
        return getProxyList().remove(o);
    }

    @Override
    default boolean containsAll(Collection<?> c) {
        return getProxyList().containsAll(c);
    }

    @Override
    default boolean addAll(Collection<? extends E> c) {
        return getProxyList().addAll(c);
    }

    @Override
    default boolean addAll(int index, Collection<? extends E> c) {
        return getProxyList().addAll(index, c);
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        return getProxyList().removeAll(c);
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        return getProxyList().retainAll(c);
    }

    @Override
    default void clear() {
        getProxyList().clear();
    }

    @Override
    default E get(int index) {
        return getProxyList().get(index);
    }

    @Override
    default E set(int index, E element) {
        return getProxyList().set(index, element);
    }

    @Override
    default void add(int index, E element) {
        getProxyList().add(index, element);
    }

    @Override
    default E remove(int index) {
        return getProxyList().remove(index);
    }

    @Override
    default int indexOf(Object o) {
        return getProxyList().indexOf(o);
    }

    @Override
    default int lastIndexOf(Object o) {
        return getProxyList().lastIndexOf(o);
    }

    @Override
    default ListIterator<E> listIterator() {
        return getProxyList().listIterator();
    }

    @Override
    default ListIterator<E> listIterator(int index) {
        return getProxyList().listIterator(index);
    }

    @Override
    default List<E> subList(int fromIndex, int toIndex) {
        return getProxyList().subList(fromIndex, toIndex);
    }

}
