package org.aoju.bus.core.clone;

import org.aoju.bus.core.lang.exception.CommonException;

/**
 * <p>
 * 克隆支持类，提供默认的克隆方法
 * </p>
 *
 * @param <T> 继承类的类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Support<T> implements Cloneable<T> {

    @Override
    public T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new CommonException(e);
        }
    }

}
