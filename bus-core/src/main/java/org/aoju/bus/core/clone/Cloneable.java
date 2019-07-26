package org.aoju.bus.core.clone;

/**
 * <p>
 * 克隆支持接口
 * </p>
 *
 * @param <T> 实现克隆接口的类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Cloneable<T> extends java.lang.Cloneable {

    /**
     * 克隆当前对象，浅复制
     *
     * @return 克隆后的对象
     */
    T clone();
}
