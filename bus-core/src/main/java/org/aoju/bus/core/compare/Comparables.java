package org.aoju.bus.core.compare;

import java.io.Serializable;
import java.util.Comparator;

/**
 * 针对 {@link java.lang.Comparable}对象的默认比较器
 *
 * @param <E> 比较对象类型
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
public class Comparables<E extends Comparable<? super E>> implements Comparator<E>, Serializable {

    /**
     * 单例
     */
    public static final Comparables INSTANCE = new Comparables<>();
    private static final long serialVersionUID = 1L;

    /**
     * 构造
     */
    public Comparables() {
        super();
    }

    /**
     * 比较两个{@link java.lang.Comparable}对象
     *
     * <pre>
     * obj1.compareTo(obj2)
     * </pre>
     *
     * @param obj1 被比较的第一个对象
     * @param obj2 the second object to compare
     * @return obj1小返回负数, 大返回正数, 否则返回0
     */
    @Override
    public int compare(final E obj1, final E obj2) {
        return obj1.compareTo(obj2);
    }

    @Override
    public int hashCode() {
        return "Comparables".hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return this == object || null != object && object.getClass().equals(this.getClass());
    }

}
