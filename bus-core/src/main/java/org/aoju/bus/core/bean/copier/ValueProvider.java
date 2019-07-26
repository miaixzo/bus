package org.aoju.bus.core.bean.copier;

import java.lang.reflect.Type;

/**
 * 值提供者，用于提供Bean注入时参数对应值得抽象接口<br>
 * 继承或匿名实例化此接口<br>
 * 在Bean注入过程中，Bean获得字段名，通过外部方式根据这个字段名查找相应的字段值，然后注入Bean<br>
 *
 * @param <T> KEY类型，一般情况下为 {@link String}
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ValueProvider<T> {

    /**
     * 获取值<br>
     *
     * @param key       Bean对象中参数名
     * @param valueType 被注入的值得类型
     * @return 对应参数名的值
     */
    Object value(T key, Type valueType);

    /**
     * 是否包含指定KEY，如果不包含则忽略注入<br>
     * 此接口方法单独需要实现的意义在于：有些值提供者（比如Map）key是存在的，但是value为null，此时如果需要注入这个null，需要根据此方法判断
     *
     * @param key Bean对象中参数名
     * @return 是否包含指定KEY
     */
    boolean containsKey(T key);
}
