package org.aoju.bus.core.lang;

import org.aoju.bus.core.lang.mutable.MutableObject;

/**
 * 为不可变的对象引用提供一个可变的包装，在java中支持引用传递。
 *
 * @param <T> 所持有值类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Holder<T> extends MutableObject<T> {
    
    private static final long serialVersionUID = -3119568580130118011L;

    /**
     * 构造
     */
    public Holder() {
        super();
    }

    /**
     * 构造
     *
     * @param value 被包装的对象
     */
    public Holder(T value) {
        super(value);
    }

    /**
     * 新建Holder类，持有指定值，当值为空时抛出空指针异常
     *
     * @param <T>   被持有的对象类型
     * @param value 值，不能为空
     * @return Holder
     */
    public static <T> Holder<T> of(T value) throws NullPointerException {
        if (null == value) {
            throw new NullPointerException("Holder can not hold a null value!");
        }
        return new Holder<>(value);
    }

}