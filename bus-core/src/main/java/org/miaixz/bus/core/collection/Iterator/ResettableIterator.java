package org.miaixz.bus.core.collection.Iterator;

import java.util.Iterator;

/**
 * 支持重置的{@link Iterator} 接口
 * 通过实现{@link #reset()}，重置此{@link Iterator}后可实现复用重新遍历
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ResettableIterator<E> extends Iterator<E> {

    /**
     * 重置，重置后可重新遍历
     */
    void reset();

}
