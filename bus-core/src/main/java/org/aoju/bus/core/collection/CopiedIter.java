package org.aoju.bus.core.collection;

import org.aoju.bus.core.utils.CollUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * 复制 {@link Iterator}为了解决并发情况下{@link Iterator}遍历导致的问题,当Iterator
 * 被修改会抛出ConcurrentModificationException）,故使用复制原Iterator的方式解决此问题
 *
 * <p>
 * 解决方法为：在构造方法中遍历Iterator中的元素,装入新的List中然后遍历之
 * 当然,修改这个复制后的Iterator是没有意义的,因此remove方法将会抛出异常
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public class CopiedIter<E> implements Iterator<E>, Iterable<E>, Serializable {

    private static final long serialVersionUID = 1L;

    private Iterator<E> listIterator;

    /**
     * 构造
     *
     * @param iterator 被复制的Iterator
     */
    public CopiedIter(Iterator<E> iterator) {
        final List<E> eleList = CollUtils.newArrayList(iterator);
        this.listIterator = eleList.iterator();
    }

    public static <V> CopiedIter<V> copyOf(Iterator<V> iterator) {
        return new CopiedIter<>(iterator);
    }

    @Override
    public boolean hasNext() {
        return this.listIterator.hasNext();
    }

    @Override
    public E next() {
        return this.listIterator.next();
    }

    /**
     * 此对象不支持移除元素
     *
     * @throws UnsupportedOperationException 当调用此方法时始终抛出此异常
     */
    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This is a read-only iterator.");
    }

    @Override
    public Iterator<E> iterator() {
        return this;
    }

}
