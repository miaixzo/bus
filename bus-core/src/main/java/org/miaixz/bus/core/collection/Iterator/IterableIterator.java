package org.miaixz.bus.core.collection.Iterator;

import java.util.Iterator;

/**
 * 提供合成接口，共同提供{@link Iterable}和{@link Iterator}功能
 *
 * @param <T> 节点类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface IterableIterator<T> extends Iterable<T>, Iterator<T> {

    @Override
    default Iterator<T> iterator() {
        return this;
    }

}
