package org.aoju.bus.core.lang;

/**
 * 建造者模式接口定义
 *
 * @param <T> 建造对象类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Builder<T> {
    /**
     * 构建
     *
     * @return 被构建的对象
     */
    T build();
}