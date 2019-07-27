package org.aoju.bus.core.lang.copier;

/**
 * 拷贝接口
 *
 * @param <T> 拷贝目标类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Copier<T> {

    /**
     * 执行拷贝
     *
     * @return 拷贝的目标
     */
    T copy();

}
