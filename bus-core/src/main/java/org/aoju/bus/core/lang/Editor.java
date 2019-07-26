package org.aoju.bus.core.lang;

/**
 * 编辑器接口，常用于对于集合中的元素做统一编辑<br>
 * 此编辑器两个作用：
 *
 * <pre>
 * 1、如果返回值为<criteria>null</criteria>，表示此值被抛弃
 * 2、对对象做修改
 * </pre>
 *
 * @param <T> 被编辑对象类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Editor<T> {
    /**
     * 修改过滤后的结果
     *
     * @param t 被过滤的对象
     * @return 修改后的对象，如果被过滤返回<criteria>null</criteria>
     */
    T edit(T t);
}
