package org.aoju.bus.core.lang.mutable;

import java.io.Serializable;

/**
 * 可变<code>Object</code>
 *
 * @param <T> 可变的类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MutableObject<T> implements Mutable<T>, Serializable {
    private static final long serialVersionUID = -464493129773743673L;

    private T value;

    /**
     * 构造，空值
     */
    public MutableObject() {
        super();
    }

    /**
     * 构造
     *
     * @param value 值
     */
    public MutableObject(final T value) {
        super();
        this.value = value;
    }

    @Override
    public T get() {
        return this.value;
    }

    @Override
    public void set(final T value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() == obj.getClass()) {
            final MutableObject<?> that = (MutableObject<?>) obj;
            return this.value.equals(that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }

}
